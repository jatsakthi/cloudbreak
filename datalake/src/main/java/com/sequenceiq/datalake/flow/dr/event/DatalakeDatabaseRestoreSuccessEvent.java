package com.sequenceiq.datalake.flow.dr.event;

import com.sequenceiq.datalake.flow.SdxEvent;

public class DatalakeDatabaseRestoreSuccessEvent extends SdxEvent
{
    public DatalakeDatabaseRestoreSuccessEvent(Long sdxId, String userId) {
        super(sdxId, userId);
    }

    @Override
    public String selector() {
        return "DatalakeDatabaseRestoreSuccessEvent";
    }
}
