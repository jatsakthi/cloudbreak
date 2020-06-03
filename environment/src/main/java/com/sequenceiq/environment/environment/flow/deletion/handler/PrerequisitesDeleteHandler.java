package com.sequenceiq.environment.environment.flow.deletion.handler;

import static com.sequenceiq.cloudbreak.common.mappable.CloudPlatform.AZURE;
import static com.sequenceiq.environment.environment.flow.deletion.event.EnvDeleteHandlerSelectors.DELETE_PREREQUISITES_EVENT;
import static com.sequenceiq.environment.environment.flow.deletion.event.EnvDeleteStateSelectors.FINISH_ENV_DELETE_EVENT;
import static com.sequenceiq.environment.parameters.dao.domain.ResourceGroupCreation.CREATE_NEW;
import static com.sequenceiq.environment.parameters.dao.domain.ResourceGroupUsagePattern.USE_MULTIPLE;

import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.cloud.Setup;
import com.sequenceiq.cloudbreak.cloud.exception.CloudConnectorException;
import com.sequenceiq.cloudbreak.cloud.init.CloudPlatformConnectors;
import com.sequenceiq.cloudbreak.cloud.model.CloudPlatformVariant;
import com.sequenceiq.cloudbreak.cloud.model.Platform;
import com.sequenceiq.cloudbreak.cloud.model.Variant;
import com.sequenceiq.cloudbreak.cloud.model.prerequisite.AzurePrerequisiteDeleteRequest;
import com.sequenceiq.cloudbreak.cloud.model.prerequisite.EnvironmentPrerequisiteDeleteRequest;
import com.sequenceiq.cloudbreak.exception.BadRequestException;
import com.sequenceiq.environment.credential.v1.converter.CredentialToCloudCredentialConverter;
import com.sequenceiq.environment.environment.domain.Environment;
import com.sequenceiq.environment.environment.dto.EnvironmentDto;
import com.sequenceiq.environment.environment.flow.creation.event.EnvCreationEvent;
import com.sequenceiq.environment.environment.flow.deletion.event.EnvDeleteFailedEvent;
import com.sequenceiq.environment.environment.service.EnvironmentService;
import com.sequenceiq.environment.parameters.dto.AzureParametersDto;
import com.sequenceiq.environment.parameters.dto.AzureResourceGroupDto;
import com.sequenceiq.environment.parameters.dto.ParametersDto;
import com.sequenceiq.flow.reactor.api.event.EventSender;
import com.sequenceiq.flow.reactor.api.handler.EventSenderAwareHandler;

import reactor.bus.Event;

@Component
public class PrerequisitesDeleteHandler extends EventSenderAwareHandler<EnvironmentDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrerequisitesDeleteHandler.class);

    private final EnvironmentService environmentService;

    private final CredentialToCloudCredentialConverter credentialToCloudCredentialConverter;

    private final CloudPlatformConnectors cloudPlatformConnectors;

    protected PrerequisitesDeleteHandler(EventSender eventSender, EnvironmentService environmentService,
            CredentialToCloudCredentialConverter credentialToCloudCredentialConverter, CloudPlatformConnectors cloudPlatformConnectors) {
        super(eventSender);
        this.environmentService = environmentService;
        this.credentialToCloudCredentialConverter = credentialToCloudCredentialConverter;
        this.cloudPlatformConnectors = cloudPlatformConnectors;
    }

    @Override
    public String selector() {
        return DELETE_PREREQUISITES_EVENT.selector();
    }

    @Override
    public void accept(Event<EnvironmentDto> environmentDtoEvent) {
        EnvironmentDto environmentDto = environmentDtoEvent.getData();
        environmentService.findEnvironmentById(environmentDto.getId())
                .ifPresentOrElse(environment -> {
                            try {
                                if (AZURE.name().equals(environmentDto.getCloudPlatform())) {
                                    deleteResourceGroupIfEmpty(environmentDto, environment);
                                } else {
                                    LOGGER.debug("Cloudplatform not azure, not creating resource group.");
                                }
                                goToFinishedState(environmentDtoEvent);
                            } catch (Exception e) {
                                goToFailedState(environmentDtoEvent, e.getMessage());
                            }
                        }, () -> goToFailedState(environmentDtoEvent, String.format("Environment was not found with id '%s'.", environmentDto.getId()))
                );
    }

    private void deleteResourceGroupIfEmpty(EnvironmentDto environmentDto, Environment environment) {
        Optional<AzureResourceGroupDto> azureResourceGroupDtoOptional = getAzureResourceGroupDto(environmentDto);
        if (azureResourceGroupDtoOptional.isEmpty()) {
            LOGGER.debug("No azure resource group dto defined, not creating resource group.");
            return;
        }

        AzureResourceGroupDto azureResourceGroupDto = azureResourceGroupDtoOptional.get();
        LOGGER.debug("Azure resource group dto: {}", azureResourceGroupDto);
        if (USE_MULTIPLE.equals(azureResourceGroupDto.getResourceGroupUsagePattern()) || !CREATE_NEW.equals(azureResourceGroupDto.getResourceGroupCreation())) {
            LOGGER.debug("Not deleting resource group.");
            return;
        }

        String resourceGroupName = azureResourceGroupDto.getName();
        deleteResourceGroupIfEmpty(environmentDto, resourceGroupName);
        LOGGER.debug("Azure resource group created successfully.");
    }

    private void deleteResourceGroupIfEmpty(EnvironmentDto environmentDto, String resourceGroupName) {
        try {
            Optional<Setup> setupOptional = getSetupConnector(environmentDto.getCloudPlatform());
            if (setupOptional.isEmpty()) {
                LOGGER.debug("No setup defined for platform {}, resource group not created.", environmentDto.getCloudPlatform());
                return;
            }

            EnvironmentPrerequisiteDeleteRequest environmentPrerequisiteDeleteRequest = new EnvironmentPrerequisiteDeleteRequest(
                    credentialToCloudCredentialConverter.convert(environmentDto.getCredential()),
                    AzurePrerequisiteDeleteRequest.builder().withResourceGroupName(resourceGroupName).build()
            );
            setupOptional.get().deleteEnvironmentPrerequisites(environmentPrerequisiteDeleteRequest);
        } catch (Exception e) {
            throw new CloudConnectorException("Could not delete resource group" + ExceptionUtils.getRootCauseMessage(e), e);
        }
    }

    private Optional<Setup> getSetupConnector(String cloudPlatform) {
        CloudPlatformVariant cloudPlatformVariant = new CloudPlatformVariant(Platform.platform(cloudPlatform), Variant.variant(cloudPlatform));
        return Optional.ofNullable(cloudPlatformConnectors.get(cloudPlatformVariant).setup());
    }

    private void goToFailedState(Event<EnvironmentDto> environmentDtoEvent, String message) {
        LOGGER.debug("Going to failed state, message: {}.", message);
        EnvironmentDto environmentDto = environmentDtoEvent.getData();
        EnvDeleteFailedEvent failureEvent = new EnvDeleteFailedEvent(
                environmentDto.getId(),
                environmentDto.getName(),
                new BadRequestException(message),
                environmentDto.getResourceCrn());

        eventSender().sendEvent(failureEvent, environmentDtoEvent.getHeaders());
    }

    private void goToFinishedState(Event<EnvironmentDto> environmentDtoEvent) {
        EnvironmentDto environmentDto = environmentDtoEvent.getData();
        EnvCreationEvent envCreationEvent = EnvCreationEvent.builder()
                .withResourceId(environmentDto.getResourceId())
                .withSelector(FINISH_ENV_DELETE_EVENT.selector())
                .withResourceCrn(environmentDto.getResourceCrn())
                .withResourceName(environmentDto.getName())
                .build();
        LOGGER.debug("Proceeding to next flow step: {}.", envCreationEvent.selector());
        eventSender().sendEvent(envCreationEvent, environmentDtoEvent.getHeaders());
    }

    private Optional<AzureResourceGroupDto> getAzureResourceGroupDto(EnvironmentDto environmentDto) {
        return Optional.ofNullable(environmentDto.getParameters())
                .map(ParametersDto::getAzureParametersDto)
                .map(AzureParametersDto::getAzureResourceGroupDto);
    }
}
