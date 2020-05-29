package com.sequenceiq.cloudbreak.audit.converter.auditeventname.rest.datahub;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.audit.converter.auditeventname.rest.RestResourceAuditEventConverter;
import com.sequenceiq.cloudbreak.audit.model.AuditEventName;
import com.sequenceiq.cloudbreak.structuredevent.event.CloudbreakEventService;
import com.sequenceiq.cloudbreak.structuredevent.event.StructuredRestCallEvent;

@Component
public class DatahubRestResourceAuditEventConverter implements RestResourceAuditEventConverter {

    @Override
    public AuditEventName auditEventName(StructuredRestCallEvent structuredEvent) {
        String method = structuredEvent.getRestCall().getRestRequest().getMethod();
        AuditEventName eventName = null;
        String resourceType = structuredEvent.getOperation().getResourceType();
        String resourceEvent = structuredEvent.getOperation().getResourceEvent();
        if ("POST".equals(method) || "PUT".equals(method)) {
            if (resourceEvent == null) {
                eventName = creationRest(resourceType);
            } else {
                eventName = updateRest(resourceType, resourceEvent);
            }
        } else if ("DELETE".equals(method)) {
            eventName = deletionRest(resourceType, resourceEvent);
        }
        return eventName;
    }

    public AuditEventName creationRest(String resourceType) {
        if (resourceType.equals(CloudbreakEventService.DATAHUB_RESOURCE_TYPE)) {
            return AuditEventName.DATAHUB_CLUSTER_CREATION;
        }
        return null;
    }

    private AuditEventName deletionRest(String resourceType, String resourceEvent) {
        if (resourceType.equals(CloudbreakEventService.DATAHUB_RESOURCE_TYPE)) {
            if (resourceEvent == null) {
                return AuditEventName.DATAHUB_CLUSTER_DELETION;
            } else if ("instance".equals(resourceEvent) || "instances".equals(resourceEvent)) {
                return AuditEventName.DATAHUB_CLUSTER_INSTANCE_DELETION;
            }
        }
        return null;
    }

    private AuditEventName updateRest(String resourceType, String resourceEvent) {
        if (resourceType.equals(CloudbreakEventService.DATAHUB_RESOURCE_TYPE)) {
            if ("retry".equals(resourceEvent)) {
                return AuditEventName.DATAHUB_CLUSTER_RETRY;
            } else if ("stop".equals(resourceEvent)) {
                return AuditEventName.DATAHUB_CLUSTER_STOPPAGE;
            } else if ("start".equals(resourceEvent)) {
                return AuditEventName.DATAHUB_CLUSTER_STARTING;
            } else if ("scaling".equals(resourceEvent)) {
                return AuditEventName.DATAHUB_CLUSTER_RESIZING;
            } else if ("maintenance".equals(resourceEvent)) {
                return AuditEventName.DATAHUB_CLUSTER_MAINTENANCE;
            } else if ("manual_repair".equals(resourceEvent)) {
                return AuditEventName.DATAHUB_CLUSTER_MANUAL_REPAIR;
            }
        }
        return null;
    }

    @Override
    public boolean shouldAudit(StructuredRestCallEvent structuredRestCallEvent) {
        return true;
    }
}
