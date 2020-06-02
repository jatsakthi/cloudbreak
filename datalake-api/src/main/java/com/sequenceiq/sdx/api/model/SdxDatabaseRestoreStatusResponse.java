package com.sequenceiq.sdx.api.model;

public class SdxDatabaseRestoreStatusResponse {
    public SdxDatabaseRestoreStatusResponse(String status) {
        this.status = status;
    }

    public SdxDatabaseRestoreStatusResponse(String status, String statusReason) {
        this.status = status;
        this.statusReason = statusReason;

    }

    private String status;

    private String statusReason;

    public String getStatus() {
        return status;
    }

    public String getStatusReason() {
        return statusReason;
    }
}
