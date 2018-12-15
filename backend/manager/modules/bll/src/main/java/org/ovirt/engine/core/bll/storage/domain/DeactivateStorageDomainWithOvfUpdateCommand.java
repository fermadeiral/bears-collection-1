package org.ovirt.engine.core.bll.storage.domain;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class DeactivateStorageDomainWithOvfUpdateCommand<T extends StorageDomainPoolParametersBase> extends
        DeactivateStorageDomainCommand<T> {

    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private StoragePoolIsoMapDao storagePoolIsoMapDao;

    public DeactivateStorageDomainWithOvfUpdateCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setCommandShouldBeLogged(false);
    }

    private boolean shouldUseCallback() {
        CommandEntity commandEntity = CommandCoordinatorUtil.getCommandEntity(getCommandId());
        return (commandEntity != null && commandEntity.isCallbackEnabled()) || shouldPerformOvfUpdate();
    }

    @Override
    public CommandCallback getCallback() {
        if (shouldUseCallback()) {
            return new ConcurrentChildCommandsExecutionCallback();
        }

        return null;
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     */

    public DeactivateStorageDomainWithOvfUpdateCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        StoragePoolIsoMap map = loadStoragePoolIsoMap();
        changeDomainStatusWithCompensation(map, StorageDomainStatus.Unknown, StorageDomainStatus.Locked, getCompensationContext());

        if (shouldPerformOvfUpdate()) {
            StoragePoolParametersBase parameters = new StoragePoolParametersBase(getStoragePoolId());
            runInternalAction(ActionType.ProcessOvfUpdateForStoragePool, parameters, null);
            runInternalAction(ActionType.UpdateOvfStoreForStorageDomain, createUpdateOvfStoreParams(), getContext());
        }

        if (noAsyncOperations()) {
            executeDeactivateCommand();
        }

        setSucceeded(true);
    }

    private StorageDomainParametersBase createUpdateOvfStoreParams() {
        StorageDomainParametersBase params = new StorageDomainParametersBase(getStorageDomainId());
        params.setParentCommand(getActionType());
        params.setParentParameters(getParameters());
        params.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        return params;
    }

    protected boolean shouldPerformOvfUpdate() {
        return !getParameters().isInactive() &&
                StorageConstants.monitoredDomainStatuses.contains(getStorageDomain().getStatus())
                && getStorageDomain().getStorageDomainType().isDataDomain();
    }

    private StoragePoolIsoMap loadStoragePoolIsoMap() {
        return  storagePoolIsoMapDao.get
                        (new StoragePoolIsoMapId(getParameters().getStorageDomainId(),
                                getParameters().getStoragePoolId()));
    }

    private void executeDeactivateCommand() {
        final StorageDomainPoolParametersBase params = new StorageDomainPoolParametersBase(getStorageDomainId(), getStoragePoolId());
        params.setSkipChecks(true);
        params.setSkipLock(true);
        params.setShouldBeLogged(true);
        runInternalActionWithTasksContext(ActionType.DeactivateStorageDomain, params);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Command);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return AuditLogType.UNASSIGNED;
    }

    @Override
    protected void endSuccessfully() {
        executeDeactivateCommand();
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        if (CommandCoordinatorUtil.getCommandExecutionStatus(getCommandId()) != CommandExecutionStatus.EXECUTED) {
            changeStorageDomainStatusInTransaction(loadStoragePoolIsoMap(), StorageDomainStatus.Unknown);
            auditLogDirector.log(this, AuditLogType.USER_DEACTIVATE_STORAGE_DOMAIN_OVF_UPDATE_INCOMPLETE);
        } else {
            executeDeactivateCommand();
        }

        setSucceeded(true);
    }
}
