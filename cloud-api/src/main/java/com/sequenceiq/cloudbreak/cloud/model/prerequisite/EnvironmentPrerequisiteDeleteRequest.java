package com.sequenceiq.cloudbreak.cloud.model.prerequisite;

import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;

public class EnvironmentPrerequisiteDeleteRequest {

    private final CloudCredential cloudCredential;

    private final AzurePrerequisiteDeleteRequest azurePrerequisiteDeleteRequest;

    public EnvironmentPrerequisiteDeleteRequest(CloudCredential cloudCredential, AzurePrerequisiteDeleteRequest azurePrerequisiteDeleteRequest) {
        this.cloudCredential = cloudCredential;
        this.azurePrerequisiteDeleteRequest = azurePrerequisiteDeleteRequest;
    }

    public CloudCredential getCloudCredential() {
        return cloudCredential;
    }

    public AzurePrerequisiteDeleteRequest getAzurePrerequisiteDeleteRequest() {
        return azurePrerequisiteDeleteRequest;
    }
}
