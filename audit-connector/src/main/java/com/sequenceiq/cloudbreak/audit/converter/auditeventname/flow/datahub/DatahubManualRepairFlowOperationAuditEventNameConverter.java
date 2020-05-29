package com.sequenceiq.cloudbreak.audit.converter.auditeventname.flow.datahub;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.audit.converter.auditeventname.flow.FlowOperationAuditEventNameConverter;
import com.sequenceiq.cloudbreak.audit.model.AuditEventName;
import com.sequenceiq.cloudbreak.structuredevent.event.StructuredFlowEvent;

@Component
public class DatahubManualRepairFlowOperationAuditEventNameConverter implements FlowOperationAuditEventNameConverter {

    @Override
    public boolean isInit(StructuredFlowEvent structuredEvent) {
        String flowEvent = structuredEvent.getFlow().getFlowEvent();
        return "MANUAL_STACK_REPAIR_TRIGGER_EVENT".equals(flowEvent);
    }

    @Override
    public boolean isFinal(StructuredFlowEvent structuredEvent) {
        String flowEvent = structuredEvent.getFlow().getFlowEvent();
        return "REPAIR_SERVICE_NOTIFIED_EVENT".equals(flowEvent);
    }

    @Override
    public boolean isFailed(StructuredFlowEvent structuredEvent) {
        return false;
    }

    @Override
    public AuditEventName eventName() {
        return AuditEventName.DATAHUB_CLUSTER_MANUAL_REPAIR;
    }
}
