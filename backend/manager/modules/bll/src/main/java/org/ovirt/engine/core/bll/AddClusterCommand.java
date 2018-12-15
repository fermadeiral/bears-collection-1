package org.ovirt.engine.core.bll;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterValidatorBase;
import org.ovirt.engine.core.bll.profiles.CpuProfileHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.ClusterValidator;
import org.ovirt.engine.core.bll.validator.HasStoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CpuProfileParameters;
import org.ovirt.engine.core.common.action.ManagementNetworkOnClusterOperationParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.ClusterFeatureDao;
import org.ovirt.engine.core.dao.MacPoolDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;

public class AddClusterCommand<T extends ManagementNetworkOnClusterOperationParameters>
        extends ClusterOperationCommandBase<T> {

    public static final String DefaultNetworkDescription = "Default Management Network";

    @Inject
    protected MacPoolDao macPoolDao;
    @Inject
    private ClusterFeatureDao clusterFeatureDao;
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private NetworkClusterDao networkClusterDao;
    @Inject
    private DbFacade dbFacade;

    public AddClusterCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void init() {
        setStoragePoolId(getCluster().getStoragePoolId());
        updateMigrateOnError();
    }

    @Override
    protected void executeCommand() {
        Cluster cluster = getCluster();
        cluster.setArchitecture(getArchitecture());
        setDefaultSwitchTypeIfNeeded();
        setDefaultFirewallTypeIfNeeded();

        checkMaxMemoryOverCommitValue();
        cluster.setDetectEmulatedMachine(true);
        cluster.setMacPoolId(calculateMacPoolIdToUse());
        clusterDao.save(cluster);

        alertIfFencingDisabled();

        // add default network
        if (getParameters().getCluster().getStoragePoolId() != null) {
            attachManagementNetwork();
        }

        // create default CPU profile for supported clusters.
        addDefaultCpuProfile();

        if (CollectionUtils.isNotEmpty(cluster.getAddtionalFeaturesSupported())) {
            for (SupportedAdditionalClusterFeature feature : cluster.getAddtionalFeaturesSupported()) {
                feature.setClusterId(cluster.getId());
            }
            clusterFeatureDao.addAllSupportedClusterFeature(cluster.getAddtionalFeaturesSupported());
        }

        setActionReturnValue(cluster.getId());
        setSucceeded(true);
    }

    private void addDefaultCpuProfile() {
        CpuProfile cpuProfile = CpuProfileHelper.createCpuProfile(getParameters().getCluster().getId(),
                getParameters().getCluster().getName());

        CpuProfileParameters cpuProfileAddParameters = new CpuProfileParameters(cpuProfile);
        cpuProfileAddParameters.setAddPermissions(true);
        cpuProfileAddParameters.setParametersCurrentUser(getCurrentUser());
        cpuProfileAddParameters.setSessionId(getContext().getEngineContext().getSessionId());

        ActionReturnValue addCpuProfileReturnValue = getBackend().runAction(ActionType.AddCpuProfile,
                cpuProfileAddParameters);
        cpuProfile.setId(addCpuProfileReturnValue.getActionReturnValue());
    }

    private Guid calculateMacPoolIdToUse() {
        Cluster cluster = getCluster();
        Guid requestedMacPoolId = cluster == null ? null : cluster.getMacPoolId();
        return requestedMacPoolId == null ? macPoolDao.getDefaultPool().getId() : requestedMacPoolId;
    }

    private void attachManagementNetwork() {
        final NetworkCluster networkCluster = createManagementNetworkCluster();
        networkCluster.setClusterId(getClusterId());
        networkClusterDao.save(networkCluster);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_CLUSTER
                : AuditLogType.USER_ADD_CLUSTER_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__CREATE);
    }

    @Override
    protected boolean validate() {
        HasStoragePoolValidator hspValidator = new HasStoragePoolValidator(getCluster());
        final ClusterValidator validator = new ClusterValidator(
                dbFacade, getCluster(), getCpuFlagsManagerHandler());

        return validate(validator.nameNotUsed())
                && validate(validator.cpuTypeSupportsVirtService())
                && validate(validator.versionSupported())
                && validate(validator.dataCenterVersionMismatch())
                && validate(hspValidator.storagePoolExists())
                && validate(validator.localStoragePoolAttachedToSingleCluster())
                && validate(validator.clusterServiceDefined())
                && validate(validator.mixedClusterServicesSupported())
                && validate(validator.attestationServerConfigured())
                && validate(validator.migrationSupported(getArchitecture()))
                && validate(validator.rngSourcesAllowed())
                && validateClusterPolicy(null)
                && validateManagementNetwork()
                && validate(validator.memoryOptimizationConfiguration())
                && validateDefaultNetworkProvider();
    }

    @Override
    protected boolean validateInputManagementNetwork(NetworkClusterValidatorBase networkClusterValidator) {
        return validate(networkClusterValidator.networkBelongsToClusterDataCenter(getCluster(), getManagementNetwork()))
                && validate(networkClusterValidator.managementNetworkRequired(getManagementNetwork()))
                && validate(networkClusterValidator.managementNetworkNotExternal(getManagementNetwork()));
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Arrays.asList(
                new PermissionSubject(getCluster().getStoragePoolId(),
                        VdcObjectType.StoragePool,
                        getActionType().getActionGroup()),
                new PermissionSubject(calculateMacPoolIdToUse(), VdcObjectType.MacPool, ActionGroup.CONFIGURE_MAC_POOL)
        );
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

}
