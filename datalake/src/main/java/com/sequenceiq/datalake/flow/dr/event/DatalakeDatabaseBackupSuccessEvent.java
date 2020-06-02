package com.sequenceiq.datalake.flow.dr.event;

import com.sequenceiq.datalake.flow.SdxEvent;

public class DatalakeDatabaseBackupSuccessEvent extends SdxEvent {

    public DatalakeDatabaseBackupSuccessEvent(Long sdxId, String userId) {
        super(sdxId, userId);
    }

    @Override
    public String selector() {
        return "DatalakeDatabaseBackupSuccessEvent";
    }
}
