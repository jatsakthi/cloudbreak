package com.sequenceiq.cloudbreak.cloud.azure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.microsoft.azure.CloudError;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.Deployment;
import com.sequenceiq.cloudbreak.cloud.azure.client.AzureClient;
import com.sequenceiq.cloudbreak.cloud.azure.connector.resource.AzureComputeResourceService;
import com.sequenceiq.cloudbreak.cloud.azure.connector.resource.AzureDatabaseResourceService;
import com.sequenceiq.cloudbreak.cloud.azure.upscale.AzureUpscaleService;
import com.sequenceiq.cloudbreak.cloud.azure.view.AzureCredentialView;
import com.sequenceiq.cloudbreak.cloud.azure.view.AzureStackView;
import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.context.CloudContext;
import com.sequenceiq.cloudbreak.cloud.exception.CloudConnectorException;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstance;
import com.sequenceiq.cloudbreak.cloud.model.CloudResource;
import com.sequenceiq.cloudbreak.cloud.model.CloudResource.Builder;
import com.sequenceiq.cloudbreak.cloud.model.CloudResourceStatus;
import com.sequenceiq.cloudbreak.cloud.model.CloudStack;
import com.sequenceiq.cloudbreak.cloud.model.DatabaseStack;
import com.sequenceiq.cloudbreak.cloud.model.ExternalDatabaseStatus;
import com.sequenceiq.cloudbreak.cloud.model.Group;
import com.sequenceiq.cloudbreak.cloud.model.ResourceStatus;
import com.sequenceiq.cloudbreak.cloud.model.TlsInfo;
import com.sequenceiq.cloudbreak.cloud.notification.PersistenceNotifier;
import com.sequenceiq.cloudbreak.cloud.template.AbstractResourceConnector;
import com.sequenceiq.cloudbreak.service.Retry;
import com.sequenceiq.cloudbreak.service.Retry.ActionFailedException;
import com.sequenceiq.common.api.type.AdjustmentType;
import com.sequenceiq.common.api.type.ResourceType;

@Service
public class AzureResourceConnector extends AbstractResourceConnector {

    public static final String RESOURCE_GROUP_NAME = "resourceGroupName";

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureResourceConnector.class);

    @Inject
    private AzureTemplateBuilder azureTemplateBuilder;

    @Inject
    private AzureUtils azureUtils;

    @Inject
    private AzureStorage azureStorage;

    @Inject
    private AzureVirtualMachineService azureVirtualMachineService;

    @Inject
    private AzureResourceGroupMetadataProvider azureResourceGroupMetadataProvider;

    @Inject
    @Qualifier("DefaultRetryService")
    private Retry retryService;

    @Inject
    private AzureComputeResourceService azureComputeResourceService;

    @Inject
    private AzureDatabaseResourceService azureDatabaseResourceService;

    @Inject
    private AzureUpscaleService azureUpscaleService;

    @Inject
    private AzureStackViewProvider azureStackViewProvider;

    @Inject
    private AzureCloudResourceService azureCloudResourceService;

    @Inject
    private AzureDownscaleService azureDownscaleService;

    @Override
    public List<CloudResourceStatus> launch(AuthenticatedContext ac, CloudStack stack, PersistenceNotifier notifier,
            AdjustmentType adjustmentType, Long threshold) {
        AzureCredentialView azureCredentialView = new AzureCredentialView(ac.getCloudCredential());
        CloudContext cloudContext = ac.getCloudContext();
        String stackName = azureUtils.getStackName(cloudContext);
        String resourceGroupName = azureResourceGroupMetadataProvider.getResourceGroupName(cloudContext, stack);
        AzureClient client = ac.getParameter(AzureClient.class);

        AzureStackView azureStackView = azureStackViewProvider.getAzureStack(azureCredentialView, stack, client, ac);

        String customImageId = azureStorage.getCustomImageId(client, ac, stack);
        String template = azureTemplateBuilder.build(stackName, customImageId, azureCredentialView, azureStackView,
                cloudContext, stack, AzureInstanceTemplateOperation.PROVISION);

        String parameters = azureTemplateBuilder.buildParameters(ac.getCloudCredential(), stack.getNetwork(), stack.getImage());

        try {
            if (!client.templateDeploymentExists(resourceGroupName, stackName)) {
                Deployment templateDeployment = client.createTemplateDeployment(resourceGroupName, stackName, template, parameters);
                LOGGER.debug("Created template deployment for launch: {}", templateDeployment.exportTemplate().template());

                List<CloudResource> templateResources = azureCloudResourceService.getDeploymentCloudResources(templateDeployment);
                List<CloudResource> instances =
                        azureCloudResourceService.getInstanceCloudResources(stackName, templateResources, stack.getGroups(), resourceGroupName);
                List<CloudResource> osDiskResources = azureCloudResourceService.getAttachedOsDiskResources(ac, instances, resourceGroupName);

                azureCloudResourceService.saveCloudResources(notifier, cloudContext, ListUtils.union(templateResources, osDiskResources));

                String networkName = azureUtils.getCustomNetworkId(stack.getNetwork());
                List<String> subnetNameList = azureUtils.getCustomSubnetIds(stack.getNetwork());
                List<CloudResource> networkResources = azureCloudResourceService.collectAndSaveNetworkAndSubnet(
                        resourceGroupName, stackName, notifier, cloudContext, subnetNameList, networkName, client);

                azureComputeResourceService.buildComputeResourcesForLaunch(ac, stack, adjustmentType, threshold, instances, networkResources);
            }
        } catch (CloudException e) {
            LOGGER.info("Provisioning error, cloud exception happened: ", e);
            if (e.body() != null && e.body().details() != null) {
                String details = e.body().details().stream().map(CloudError::message).collect(Collectors.joining(", "));
                throw new CloudConnectorException(String.format("Stack provisioning failed, status code %s, error message: %s, details: %s",
                        e.body().code(), e.body().message(), details));
            } else {
                throw new CloudConnectorException(String.format("Stack provisioning failed: '%s', please go to Azure Portal for detailed message", e));
            }
        } catch (Exception e) {
            LOGGER.warn("Provisioning error:", e);
            throw new CloudConnectorException(String.format("Error in provisioning stack %s: %s", stackName, e.getMessage()));
        }

        CloudResource cloudResource = new Builder()
                .type(ResourceType.ARM_TEMPLATE)
                .name(resourceGroupName)
                .build();
        List<CloudResourceStatus> resources = check(ac, Collections.singletonList(cloudResource));
        LOGGER.debug("Launched resources: {}", resources);
        return resources;
    }

    @Override
    public List<CloudResourceStatus> launchDatabaseServer(AuthenticatedContext authenticatedContext, DatabaseStack stack,
            PersistenceNotifier persistenceNotifier) {
        return azureDatabaseResourceService.buildDatabaseResourcesForLaunch(authenticatedContext, stack, persistenceNotifier);
    }

    @Override
    public void startDatabaseServer(AuthenticatedContext authenticatedContext, DatabaseStack stack) {
        throw new UnsupportedOperationException("Database server start operation is not supported for " + getClass().getName());
    }

    @Override
    public void stopDatabaseServer(AuthenticatedContext authenticatedContext, DatabaseStack stack) {
        throw new UnsupportedOperationException("Database server stop operation is not supported for " + getClass().getName());
    }

    @Override
    public ExternalDatabaseStatus getDatabaseServerStatus(AuthenticatedContext authenticatedContext, DatabaseStack stack) throws Exception {
        return azureDatabaseResourceService.getDatabaseServerStatus(authenticatedContext, stack);
    }

    @Override
    public List<CloudResourceStatus> check(AuthenticatedContext authenticatedContext, List<CloudResource> resources) {
        List<CloudResourceStatus> result = new ArrayList<>();
        AzureClient client = authenticatedContext.getParameter(AzureClient.class);
        String stackName = azureUtils.getStackName(authenticatedContext.getCloudContext());

        for (CloudResource resource : resources) {
            ResourceType resourceType = resource.getType();
            if (resourceType == ResourceType.ARM_TEMPLATE) {
                LOGGER.debug("Checking Azure stack status of: {}", stackName);
                checkTemplateDeployment(result, client, stackName, resource);
            } else {
                if (!resourceType.name().startsWith("AZURE")) {
                    throw new CloudConnectorException(String.format("Invalid resource type: %s", resourceType));
                }
            }
        }
        return result;
    }

    private void checkTemplateDeployment(List<CloudResourceStatus> result, AzureClient client, String stackName, CloudResource resource) {
        try {
            String resourceGroupName = resource.getName();
            CloudResourceStatus templateResourceStatus;
            if (client.templateDeploymentExists(resourceGroupName, stackName)) {
                Deployment resourceGroupDeployment = client.getTemplateDeployment(resourceGroupName, stackName);
                templateResourceStatus = azureUtils.getTemplateStatus(resource, resourceGroupDeployment, client, stackName);
            } else {
                templateResourceStatus = new CloudResourceStatus(resource, ResourceStatus.DELETED);
            }
            result.add(templateResourceStatus);
        } catch (CloudException e) {
            if (e.response().code() == AzureConstants.NOT_FOUND) {
                result.add(new CloudResourceStatus(resource, ResourceStatus.DELETED));
            } else {
                throw new CloudConnectorException(e.body().message(), e);
            }
        } catch (RuntimeException e) {
            throw new CloudConnectorException(String.format("Invalid resource exception: %s", e.getMessage()), e);
        }
    }

    @Override
    public List<CloudResourceStatus> terminate(AuthenticatedContext ac, CloudStack stack, List<CloudResource> resources) {
        AzureClient client = ac.getParameter(AzureClient.class);
        String resourceGroupName = azureResourceGroupMetadataProvider.getResourceGroupName(ac.getCloudContext(), stack);
        Boolean singleResourceGroup = azureResourceGroupMetadataProvider.useSingleResourceGroup(stack);

        if (singleResourceGroup) {
            List<CloudResource> cloudResourceList = collectResourcesToRemove(ac, stack, resources, azureUtils.getInstanceList(stack));
            resources.addAll(cloudResourceList);
            azureDownscaleService.terminate(ac, stack, resources);
            return check(ac, Collections.emptyList());
        } else {
            try {
                try {
                    retryService.testWith2SecDelayMax5Times(() -> {
                        if (!client.resourceGroupExists(resourceGroupName)) {
                            throw new ActionFailedException("Resource group not exists");
                        }
                        return true;
                    });
                    client.deleteResourceGroup(resourceGroupName);
                } catch (ActionFailedException ignored) {
                    LOGGER.debug("Resource group not found with name: {}", resourceGroupName);
                }
            } catch (CloudException e) {
                if (e.response().code() != AzureConstants.NOT_FOUND) {
                    throw new CloudConnectorException(String.format("Could not delete resource group: %s", resourceGroupName), e);
                } else {
                    return check(ac, Collections.emptyList());
                }
            }
            return check(ac, resources);
        }
    }

    @Override
    public List<CloudResourceStatus> terminateDatabaseServer(AuthenticatedContext authenticatedContext, DatabaseStack stack,
            List<CloudResource> resources, boolean force) {
        return azureDatabaseResourceService.terminateDatabaseServer(authenticatedContext, stack, resources, force);
    }

    @Override
    public List<CloudResourceStatus> update(AuthenticatedContext authenticatedContext, CloudStack stack, List<CloudResource> resources) {
        return new ArrayList<>();
    }

    @Override
    public List<CloudResourceStatus> upscale(AuthenticatedContext ac, CloudStack stack, List<CloudResource> resources) {
        AzureClient client = ac.getParameter(AzureClient.class);
        AzureStackView azureStackView = azureStackViewProvider.getAzureStack(new AzureCredentialView(ac.getCloudCredential()), stack, client, ac);
        return azureUpscaleService.upscale(ac, stack, resources, azureStackView, client);
    }

    @Override
    public List<CloudResourceStatus> downscale(AuthenticatedContext ac, CloudStack stack, List<CloudResource> resources, List<CloudInstance> vms,
            List<CloudResource> resourcesToRemove) {
        return azureDownscaleService.downscale(ac, stack, resources, vms, resourcesToRemove);
    }

    @Override
    public List<CloudResource> collectResourcesToRemove(AuthenticatedContext authenticatedContext, CloudStack stack,
            List<CloudResource> resources, List<CloudInstance> vms) {

        List<CloudResource> result = Lists.newArrayList();
        // This filters out resources like Availablity Sets, so needed only for downscales
        if (azureUtils.getInstanceList(stack).size() != vms.size()) {
            result.addAll(getDeletableResources(resources, vms));
        }
        result.addAll(collectProviderSpecificResources(resources, vms));
        return result;
    }

    @Override
    protected Collection<CloudResource> getDeletableResources(Iterable<CloudResource> resources, Iterable<CloudInstance> instances) {
        Collection<CloudResource> result = new ArrayList<>();
        for (CloudInstance instance : instances) {
            String instanceId = instance.getInstanceId();
            for (CloudResource resource : resources) {
                if (instanceId.equalsIgnoreCase(resource.getName()) || instanceId.equalsIgnoreCase(resource.getInstanceId())) {
                    result.add(resource);
                }
            }
        }
        return result;
    }

    @Override
    protected List<CloudResource> collectProviderSpecificResources(List<CloudResource> resources, List<CloudInstance> vms) {
        return List.of();
    }

    @Override
    public TlsInfo getTlsInfo(AuthenticatedContext authenticatedContext, CloudStack cloudStack) {
        return new TlsInfo(false);
    }

    @Override
    public String getStackTemplate() {
        return azureTemplateBuilder.getTemplateString();
    }

    @Override
    public String getDBStackTemplate() {
        return azureTemplateBuilder.getDBTemplateString();
    }

    private Map<String, String> getCustomImageNamePerInstance(AuthenticatedContext ac, CloudStack cloudStack) {
        AzureClient client = ac.getParameter(AzureClient.class);
        Map<String, String> imageNameMap = new HashMap<>();
        Map<String, String> customImageNamePerInstance = new HashMap<>();
        for (Group group : cloudStack.getGroups()) {
            for (CloudInstance instance : group.getInstances()) {
                String imageId = instance.getTemplate().getImageId();
                if (StringUtils.isNotBlank(imageId)) {
                    String imageCustomName = imageNameMap.computeIfAbsent(imageId, s -> azureStorage.getCustomImageId(client, ac, cloudStack, imageId));
                    customImageNamePerInstance.put(instance.getInstanceId(), imageCustomName);
                }
            }
        }
        return customImageNamePerInstance;
    }

    private void deleteContainer(AzureClient azureClient, String resourceGroup, String storageName, String container) {
        try {
            azureClient.deleteContainerInStorage(resourceGroup, storageName, container);
        } catch (CloudException e) {
            if (e.response().code() != AzureConstants.NOT_FOUND) {
                throw new CloudConnectorException(String.format("Could not delete container: %s", container), e);
            } else {
                LOGGER.debug("Container not found: resourcegroup={}, storagename={}, container={}",
                        resourceGroup, storageName, container);
            }
        }
    }

    private String getNameFromConnectionString(String connection) {
        return connection.split("/")[connection.split("/").length - 1];
    }

}
