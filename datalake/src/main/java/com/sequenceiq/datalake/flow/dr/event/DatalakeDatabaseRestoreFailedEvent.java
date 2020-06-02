package com.sequenceiq.datalake.flow.dr.event;

import com.sequenceiq.datalake.flow.SdxEvent;

public class DatalakeDatabaseRestoreFailedEvent extends SdxEvent {

    private final Exception exception;

    public DatalakeDatabaseRestoreFailedEvent(Long sdxId, String userId, Exception exception) {
        super(sdxId, userId);
        this.exception = exception;
    }

    public static DatalakeDatabaseRestoreFailedEvent from(SdxEvent event, Exception exception) {
        return new DatalakeDatabaseRestoreFailedEvent(event.getResourceId(), event.getUserId(), exception);
    }

    @Override
    public String selector() {
        return "DatalakeDatabaseRestoreFailedEvent";
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DatalakeDatabaseRestoreFailedEvent{");
        sb.append("exception=").append(exception);
        sb.append('}');
        return sb.toString();
    }
}
