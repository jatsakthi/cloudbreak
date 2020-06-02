package com.sequenceiq.datalake.flow.dr;

import com.sequenceiq.flow.core.FlowState;
import com.sequenceiq.flow.core.RestartAction;
import com.sequenceiq.flow.core.restart.DefaultRestartAction;

public enum DatalakeDatabaseDrState implements FlowState {

    INIT_STATE,
    DATALAKE_DATABSE_BACKUP_START_STATE,
    DATALAKE_DATABSE_BACKUP_IN_PROGRESS_STATE,
    DATALAKE_DATABSE_BACKUP_FAILED_STATE,
    DATALAKE_DATABSE_BACKUP_FINISHED_STATE,
    DATALAKE_DATABSE_RESTORE_START_STATE,
    DATALAKE_DATABSE_RESTORE_IN_PROGRESS_STATE,
    DATALAKE_DATABSE_RESTORE_FAILED_STATE,
    DATALAKE_DATABSE_RESTORE_FINISHED_STATE,
    FINAL_STATE;

    private Class<? extends DefaultRestartAction> restartAction = DefaultRestartAction.class;

    DatalakeDatabaseDrState() {
    }

    DatalakeDatabaseDrState(Class<? extends DefaultRestartAction> restartAction) {
        this.restartAction = restartAction;
    }

    @Override
    public Class<? extends RestartAction> restartAction() {
        return restartAction;
    }
}
