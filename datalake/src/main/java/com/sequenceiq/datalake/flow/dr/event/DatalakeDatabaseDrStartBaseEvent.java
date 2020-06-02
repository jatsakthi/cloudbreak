package com.sequenceiq.datalake.flow.dr.event;

import com.sequenceiq.datalake.entity.SdxDatabaseDrStatus;
import com.sequenceiq.datalake.flow.SdxEvent;

public class DatalakeDatabaseDrStartBaseEvent extends SdxEvent  {
    protected SdxDatabaseDrStatus drStatus;

    public DatalakeDatabaseDrStartBaseEvent(String selector, Long sdxId, String userId) {
        super(selector, sdxId, userId);
    }

    public SdxDatabaseDrStatus getDrStatus() {
        return drStatus;
    }
}
