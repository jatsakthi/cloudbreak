package com.sequenceiq.cloudbreak.cloud.azure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.microsoft.azure.management.compute.VirtualMachine;
import com.sequenceiq.cloudbreak.cloud.azure.client.AzureClient;
import com.sequenceiq.cloudbreak.cloud.azure.connector.resource.AzureComputeResourceService;
import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.exception.CloudConnectorException;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstance;
import com.sequenceiq.cloudbreak.cloud.model.CloudResource;
import com.sequenceiq.cloudbreak.cloud.model.CloudResourceStatus;
import com.sequenceiq.cloudbreak.cloud.model.CloudStack;
import com.sequenceiq.cloudbreak.cloud.notification.PersistenceNotifier;
import com.sequenceiq.cloudbreak.cloud.transform.CloudResourceHelper;
import com.sequenceiq.common.api.type.ResourceType;

@Service
public class AzureDownscaleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureDownscaleService.class);

    @Inject
    private AzureStorage azureStorage;

    @Inject
    private CloudResourceHelper cloudResourceHelper;

    @Inject
    private AzureResourceGroupMetadataProvider azureResourceGroupMetadataProvider;

    @Inject
    private AzureVirtualMachineService azureVirtualMachineService;

    @Inject
    private AzureUtils azureUtils;

    @Inject
    private AzureComputeResourceService azureComputeResourceService;

    @Inject
    private AzureResourceConnector azureResourceConnector;

    @Inject
    private AzureCloudResourceService azureCloudResourceService;

    @Inject
    private PersistenceNotifier resourceNotifier;

    public List<CloudResourceStatus> downscale(AuthenticatedContext ac, CloudStack stack, List<CloudResource> resources, List<CloudInstance> vms,
            List<CloudResource> resourcesToRemove) {
        return terminateResources(ac, stack, resources, vms, resourcesToRemove, false);
    }

    public List<CloudResourceStatus> terminate(AuthenticatedContext ac, CloudStack stack, List<CloudResource> resources, List<CloudInstance> vms,
            List<CloudResource> resourcesToRemove) {
        return terminateResources(ac, stack, resources, vms, resourcesToRemove, true);
    }

    public List<CloudResourceStatus> terminate(AuthenticatedContext ac, CloudStack stack, List<CloudResource> resourcesToRemove) {
        List<CloudInstance> vms = new ArrayList<>();
        stack.getGroups().forEach(group -> vms.addAll(group.getInstances()));
        List<CloudResource> networkResources = azureCloudResourceService.getNetworkResources(resourcesToRemove);
        return terminateResources(ac, stack, networkResources, vms, resourcesToRemove, true);
    }

    private List<CloudResourceStatus> terminateResources(AuthenticatedContext ac, CloudStack stack, List<CloudResource> resources, List<CloudInstance> vms,
            List<CloudResource> resourcesToRemove, boolean deleteWholeDeployment) {
        LOGGER.debug("Terminating the following resources: {}", resourcesToRemove);
        LOGGER.debug("Operation is: {}", deleteWholeDeployment ? "terminate" : "downscale");
        AzureClient client = ac.getParameter(AzureClient.class);

        List<CloudResource> networkResources = azureCloudResourceService.getNetworkResources(resources);
        String resourceGroupName = azureResourceGroupMetadataProvider.getResourceGroupName(ac.getCloudContext(), stack);

        Map<String, VirtualMachine> vmsFromAzure = azureVirtualMachineService.getVmsFromAzureAndFillStatuses(ac, vms, new ArrayList<>());
        List<CloudInstance> cloudInstancesSyncedWithAzure = vms.stream()
                .filter(cloudInstance -> vmsFromAzure.containsKey(cloudInstance.getInstanceId()))
                .collect(Collectors.toList());
        azureUtils.deleteInstances(ac, cloudInstancesSyncedWithAzure);
        azureCloudResourceService.deleteCloudResource(resourceNotifier, ac.getCloudContext(), getCloudResourcesByType(resources, ResourceType.AZURE_INSTANCE));

        List<String> networkInterfaceNames = getResourceNamesByResourceType(resourcesToRemove, ResourceType.AZURE_NETWORK_INTERFACE);
        azureUtils.waitForDetachNetworkInterfaces(ac, client, resourceGroupName, networkInterfaceNames);
        azureUtils.deleteNetworkInterfaces(client, resourceGroupName, networkInterfaceNames);
        azureCloudResourceService.deleteCloudResource(resourceNotifier, ac.getCloudContext(), getCloudResourcesByType(resources, ResourceType.AZURE_INSTANCE));


        List<String> publicAddressNames = getResourceNamesByResourceType(resourcesToRemove, ResourceType.AZURE_PUBLIC_IP);
        azureUtils.deletePublicIps(client, resourceGroupName, publicAddressNames);
        azureCloudResourceService.deleteCloudResource(resourceNotifier, ac.getCloudContext(), getCloudResourcesByType(resources, ResourceType.AZURE_PUBLIC_IP));

        List<String> managedDiskIds = getResourceNamesByResourceType(resourcesToRemove, ResourceType.AZURE_DISK);
        azureUtils.deleteManagedDisks(client, resourceGroupName, managedDiskIds);
        azureCloudResourceService.deleteCloudResource(resourceNotifier, ac.getCloudContext(), getCloudResourcesByType(resources, ResourceType.AZURE_DISK));


//        for (CloudInstance instance : vms) {
        try {
            List<CloudResource> resourcesToDownscale = resources.stream()
                    .filter(resource -> vms.stream()
                            .map(CloudInstance::getInstanceId)
                            .collect(Collectors.toList())
                            .contains(resource.getInstanceId()))
                    .collect(Collectors.toList());
            azureComputeResourceService.deleteComputeResources(ac, stack, resourcesToDownscale, networkResources);
        } catch (CloudConnectorException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new CloudConnectorException(String.format("Failed to delete resources during downscale: %s", resourceGroupName), e);
        }
//        }

        if (deleteWholeDeployment) {
            List<String> availabiltySetNames = getResourceNamesByResourceType(resourcesToRemove, ResourceType.AZURE_AVAILABILITY_SET);
            azureUtils.deleteAvailabilitySets(client, resourceGroupName, availabiltySetNames);
            azureCloudResourceService.deleteCloudResource(resourceNotifier, ac.getCloudContext(),
                    getCloudResourcesByType(resources, ResourceType.AZURE_AVAILABILITY_SET));

            List<String> networkIds = getResourceIdsByResourceType(resourcesToRemove, ResourceType.AZURE_NETWORK);
            azureUtils.deleteNetworks(client, networkIds);
            azureCloudResourceService.deleteCloudResource(resourceNotifier, ac.getCloudContext(),
                    getCloudResourcesByType(resources, ResourceType.AZURE_NETWORK));
        }
        List<String> securityGroupIds = getResourceIdsByResourceType(resourcesToRemove, ResourceType.AZURE_SECURITY_GROUP);
        azureUtils.deleteSecurityGroups(client, securityGroupIds);
        azureCloudResourceService.deleteCloudResource(resourceNotifier, ac.getCloudContext(),
                getCloudResourcesByType(resources, ResourceType.AZURE_SECURITY_GROUP));

        LOGGER.debug("All the necessary resources have been deleted successfully");
        return azureResourceConnector.check(ac, resources);
    }

    private List<CloudResource> getCloudResourcesByType(List<CloudResource> resourcesToRemove, ResourceType resourceType) {
        return resourcesToRemove.stream()
                .filter(cloudResource -> resourceType.equals(cloudResource.getType()))
                .filter(cloudResource -> Objects.nonNull(cloudResource.getReference()))
                .collect(Collectors.toList());
    }

    private List<String> getResourceNamesByResourceType(List<CloudResource> resourcesToRemove, ResourceType resourceType) {
        return getCloudResourcesByType(resourcesToRemove, resourceType)
                .stream()
                .map(CloudResource::getName)
                .collect(Collectors.toList());
    }

    private List<String> getResourceIdsByResourceType(List<CloudResource> resourcesToRemove, ResourceType resourceType) {
        return getCloudResourcesByType(resourcesToRemove, resourceType)
                .stream()
                .map(CloudResource::getReference)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
