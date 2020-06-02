package com.sequenceiq.datalake.flow.dr.event;

import com.sequenceiq.datalake.flow.SdxContext;
import com.sequenceiq.datalake.flow.SdxEvent;

public class DatalakeDatabaseRestoreWaitRequest extends SdxEvent {


    public DatalakeDatabaseRestoreWaitRequest(Long sdxId, String userId) {
        super(sdxId, userId);
    }

    public static DatalakeDatabaseBackupWaitRequest from(SdxContext context) {
        return new DatalakeDatabaseBackupWaitRequest(context.getSdxId(), context.getUserId());
    }

    @Override
    public String selector() {
        return "DatalakeDatabaseRestoreWaitRequest";
    }
}
