package com.sequenceiq.datalake.flow.dr;

import static com.sequenceiq.datalake.flow.dr.DatalakeDatabaseDrEvent.DATALAKE_DATABASE_BACKUP_FAILED_EVENT;
import static com.sequenceiq.datalake.flow.dr.DatalakeDatabaseDrEvent.DATALAKE_DATABASE_BACKUP_FINALIZED_EVENT;
import static com.sequenceiq.datalake.flow.dr.DatalakeDatabaseDrEvent.DATALAKE_DATABASE_BACKUP_IN_PROGRESS_EVENT;
import static com.sequenceiq.datalake.flow.dr.DatalakeDatabaseDrEvent.DATALAKE_DATABASE_RESTORE_FAILED_EVENT;
import static com.sequenceiq.datalake.flow.dr.DatalakeDatabaseDrEvent.DATALAKE_DATABASE_RESTORE_FINALIZED_EVENT;
import static com.sequenceiq.datalake.flow.dr.DatalakeDatabaseDrEvent.DATALAKE_DATABASE_RESTORE_IN_PROGRESS_EVENT;

import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

import com.sequenceiq.datalake.entity.SdxDatabaseDrStatus;
import com.sequenceiq.datalake.flow.SdxContext;
import com.sequenceiq.datalake.flow.SdxEvent;
import com.sequenceiq.datalake.flow.dr.event.DatalakeDatabaseBackupFailedEvent;
import com.sequenceiq.datalake.flow.dr.event.DatalakeDatabaseBackupStartEvent;
import com.sequenceiq.datalake.flow.dr.event.DatalakeDatabaseBackupSuccessEvent;
import com.sequenceiq.datalake.flow.dr.event.DatalakeDatabaseBackupWaitRequest;
import com.sequenceiq.datalake.flow.dr.event.DatalakeDatabaseRestoreFailedEvent;
import com.sequenceiq.datalake.flow.dr.event.DatalakeDatabaseRestoreSuccessEvent;
import com.sequenceiq.datalake.flow.dr.event.DatalakeDatabaseRestoreWaitRequest;
import com.sequenceiq.datalake.service.AbstractSdxAction;
import com.sequenceiq.datalake.service.sdx.dr.SdxDrService;
import com.sequenceiq.flow.core.FlowEvent;
import com.sequenceiq.flow.core.FlowParameters;
import com.sequenceiq.flow.core.FlowState;

@Configuration
public class DatalakeDatabaseDrActions {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatalakeDatabaseDrActions.class);

    private static final String TARGET_IMAGE = "TARGET_IMAGE";

    @Inject
    private SdxDrService sdxDrService;

    @Bean(name = "DATALAKE_DATABSE_BACKUP_START_STATE")
    public Action<?, ?> datalakeBackup() {
        return new AbstractSdxAction<>(DatalakeDatabaseBackupStartEvent.class) {
            @Override
            protected SdxContext createFlowContext(FlowParameters flowParameters, StateContext<FlowState, FlowEvent> stateContext,
                    DatalakeDatabaseBackupStartEvent payload) {
                return SdxContext.from(flowParameters, payload);
            }

            @Override
            protected void prepareExecution(DatalakeDatabaseBackupStartEvent payload, Map<Object, Object> variables) {
                super.prepareExecution(payload, variables);
            }

            @Override
            protected void doExecute(SdxContext context, DatalakeDatabaseBackupStartEvent payload, Map<Object, Object> variables) throws Exception {
                LOGGER.info("Datalake database backup has been started for {}", payload.getResourceId());
                sdxDrService.databaseBackup(payload.getResourceId(), payload.getDatabaseHost(), payload.getBackupLocation());
                sendEvent(context, DATALAKE_DATABASE_BACKUP_IN_PROGRESS_EVENT.event(), payload);
            }

            @Override
            protected Object getFailurePayload(DatalakeDatabaseBackupStartEvent payload, Optional<SdxContext> flowContext, Exception ex) {
                //TODO fix it
                return null;
            }
        };
    }

    @Bean(name = "DATALAKE_DATABSE_RESTORE_START_STATE")
    public Action<?, ?> datalakeRestore() {
        return new AbstractSdxAction<>(DatalakeDatabaseBackupStartEvent.class) {
            @Override
            protected SdxContext createFlowContext(FlowParameters flowParameters, StateContext<FlowState, FlowEvent> stateContext,
                    DatalakeDatabaseBackupStartEvent payload) {
                return SdxContext.from(flowParameters, payload);
            }

            @Override
            protected void prepareExecution(DatalakeDatabaseBackupStartEvent payload, Map<Object, Object> variables) {
                super.prepareExecution(payload, variables);
            }

            @Override
            protected void doExecute(SdxContext context, DatalakeDatabaseBackupStartEvent payload, Map<Object, Object> variables) throws Exception {
                LOGGER.info("Datalake database restore has been started for {}", payload.getResourceId());
                sdxDrService.databaseRestore(payload.getResourceId(), payload.getDatabaseHost(), payload.getBackupLocation());
                sendEvent(context, DATALAKE_DATABASE_RESTORE_IN_PROGRESS_EVENT.event(), payload);
            }

            @Override
            protected Object getFailurePayload(DatalakeDatabaseBackupStartEvent payload, Optional<SdxContext> flowContext, Exception ex) {
                //TODO fix it
                return null;            }
        };
    }

    @Bean(name = "DATALAKE_DATABSE_BACKUP_IN_PROGRESS_STATE")
    public Action<?, ?> datalakebackupInProgress() {
        return new AbstractSdxAction<>(SdxEvent.class) {

            @Override
            protected SdxContext createFlowContext(FlowParameters flowParameters, StateContext<FlowState, FlowEvent> stateContext, SdxEvent payload) {
                return SdxContext.from(flowParameters, payload);
            }

            @Override
            protected void doExecute(SdxContext context, SdxEvent payload, Map<Object, Object> variables) {
                LOGGER.info("Datalake database backup is in progress for {} ", payload.getResourceId());
                sendEvent(context, DatalakeDatabaseBackupWaitRequest.from(context));
            }

            @Override
            protected Object getFailurePayload(SdxEvent payload, Optional<SdxContext> flowContext, Exception ex) {
                //TODO fix it
                return null;            }
        };
    }

    @Bean(name = "DATALAKE_DATABSE_RESTORE_IN_PROGRESS_STATE")
    public Action<?, ?> datalakeRestoreInProgress() {
        return new AbstractSdxAction<>(SdxEvent.class) {

            @Override
            protected SdxContext createFlowContext(FlowParameters flowParameters, StateContext<FlowState, FlowEvent> stateContext, SdxEvent payload) {
                return SdxContext.from(flowParameters, payload);
            }

            @Override
            protected void doExecute(SdxContext context, SdxEvent payload, Map<Object, Object> variables) {
                LOGGER.info("Datalake database restore is in progress for {} ", payload.getResourceId());
                sendEvent(context, DatalakeDatabaseRestoreWaitRequest.from(context));
            }

            @Override
            protected Object getFailurePayload(SdxEvent payload, Optional<SdxContext> flowContext, Exception ex) {
                //TODO fix it
                return null;
            }
        };
    }

    @Bean(name = "DATALAKE_DATABSE_BACKUP_FINISHED_STATE")
    public Action<?, ?> finishedBackupAction() {
        return new AbstractSdxAction<>(DatalakeDatabaseBackupSuccessEvent.class) {

            @Override
            protected SdxContext createFlowContext(FlowParameters flowParameters, StateContext<FlowState, FlowEvent> stateContext,
                    DatalakeDatabaseBackupSuccessEvent payload) {
                return SdxContext.from(flowParameters, payload);
            }

            @Override
            protected void doExecute(SdxContext context, DatalakeDatabaseBackupSuccessEvent payload, Map<Object, Object> variables) throws Exception {
                LOGGER.info("Sdx database backup is finalized with sdx id: {}", payload.getResourceId());
                sdxDrService.updateDatabaseStatusEntry(payload.getResourceId(), SdxDatabaseDrStatus.Status.SUCCEEDED, null);
                sendEvent(context, DATALAKE_DATABASE_BACKUP_FINALIZED_EVENT.event(), payload);
            }

            @Override
            protected Object getFailurePayload(DatalakeDatabaseBackupSuccessEvent payload, Optional<SdxContext> flowContext, Exception ex) {
                return null;
            }
        };
    }

    @Bean(name = "DATALAKE_DATABSE_RESTORE_FINISHED_STATE")
    public Action<?, ?> finishedRestoreAction() {
        return new AbstractSdxAction<>(DatalakeDatabaseRestoreSuccessEvent.class) {

            @Override
            protected SdxContext createFlowContext(FlowParameters flowParameters, StateContext<FlowState, FlowEvent> stateContext,
                    DatalakeDatabaseRestoreSuccessEvent payload) {
                return SdxContext.from(flowParameters, payload);
            }

            @Override
            protected void doExecute(SdxContext context, DatalakeDatabaseRestoreSuccessEvent payload, Map<Object, Object> variables) throws Exception {
                LOGGER.info("Sdx database backup is finalized with sdx id: {}", payload.getResourceId());
                sdxDrService.updateDatabaseStatusEntry(payload.getResourceId(), SdxDatabaseDrStatus.Status.SUCCEEDED, null);
                sendEvent(context, DATALAKE_DATABASE_RESTORE_FINALIZED_EVENT.event(), payload);
            }

            @Override
            protected Object getFailurePayload(DatalakeDatabaseRestoreSuccessEvent payload, Optional<SdxContext> flowContext, Exception ex) {
                return null;
            }
        };
    }


    @Bean(name = "DATALAKE_DATABASE_BACKUP_FAILED_STATE")
    public Action<?, ?> backupFailed() {
        return new AbstractSdxAction<>(DatalakeDatabaseBackupFailedEvent.class) {
            @Override
            protected SdxContext createFlowContext(FlowParameters flowParameters, StateContext<FlowState, FlowEvent> stateContext,
                    DatalakeDatabaseBackupFailedEvent payload) {
                return SdxContext.from(flowParameters, payload);
            }

            @Override
            protected void doExecute(SdxContext context, DatalakeDatabaseBackupFailedEvent payload, Map<Object, Object> variables) throws Exception {
                Exception exception = payload.getException();
                LOGGER.error("Datalake database backup failed for datalake with id: {}", payload.getResourceId(), exception);
                sdxDrService.updateDatabaseStatusEntry(payload.getResourceId(), SdxDatabaseDrStatus.Status.FAILED, exception.getLocalizedMessage());
                sendEvent(context, DATALAKE_DATABASE_BACKUP_FAILED_EVENT.event(), payload);
            }

            @Override
            protected Object getFailurePayload(DatalakeDatabaseBackupFailedEvent payload, Optional<SdxContext> flowContext, Exception ex) {
                return null;
            }
        };
    }

    @Bean(name = "DATALAKE_DATABSE_RESTORE_FAILED_STATE")
    public Action<?, ?> restoreFailed() {
        return new AbstractSdxAction<>(DatalakeDatabaseRestoreFailedEvent.class) {
            @Override
            protected SdxContext createFlowContext(FlowParameters flowParameters, StateContext<FlowState, FlowEvent> stateContext,
                    DatalakeDatabaseRestoreFailedEvent payload) {
                return SdxContext.from(flowParameters, payload);
            }

            @Override
            protected void doExecute(SdxContext context, DatalakeDatabaseRestoreFailedEvent payload, Map<Object, Object> variables) throws Exception {
                Exception exception = payload.getException();
                LOGGER.error("Datalake upgrade could not be started for datalake with id: {}", payload.getResourceId(), exception);
                sdxDrService.updateDatabaseStatusEntry(payload.getResourceId(), SdxDatabaseDrStatus.Status.FAILED, exception.getLocalizedMessage());
                sendEvent(context, DATALAKE_DATABASE_RESTORE_FAILED_EVENT.event(), payload);
            }

            @Override
            protected Object getFailurePayload(DatalakeDatabaseRestoreFailedEvent payload, Optional<SdxContext> flowContext, Exception ex) {
                return null;
            }
        };
    }
}
