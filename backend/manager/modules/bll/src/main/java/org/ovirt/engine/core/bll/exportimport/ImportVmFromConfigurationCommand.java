package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.ovfstore.OvfHelper;
import org.ovirt.engine.core.bll.validator.ImportValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachDetachVmDiskParameters;
import org.ovirt.engine.core.common.action.ImportVmFromConfParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.UnregisteredDisksDao;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class ImportVmFromConfigurationCommand<T extends ImportVmFromConfParameters> extends ImportVmCommand<T> {

    private static final Logger log = LoggerFactory.getLogger(ImportVmFromConfigurationCommand.class);
    private Collection<Disk> vmDisksToAttach;
    private OvfEntityData ovfEntityData;
    private VM vmFromConfiguration;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private OvfHelper ovfHelper;

    @Inject
    private ExternalVnicProfileMappingValidator externalVnicProfileMappingValidator;

    @Inject
    private ClusterDao clusterDao;
    @Inject
    private ImportedNetworkInfoUpdater importedNetworkInfoUpdater;
    @Inject
    private UnregisteredOVFDataDao unregisteredOVFDataDao;
    @Inject
    private UnregisteredDisksDao unregisteredDisksDao;
    @Inject
    private AffinityGroupDao affinityGroupDao;

    public ImportVmFromConfigurationCommand(Guid commandId) {
        super(commandId);
    }

    public ImportVmFromConfigurationCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setCommandShouldBeLogged(false);
    }

    @Override
    protected boolean validate() {
        if (isImagesAlreadyOnTarget()) {
            if (!validateExternalVnicProfileMapping()) {
                return false;
            }

            ImportValidator importValidator = getImportValidator();
            if (!validate(importValidator.validateUnregisteredEntity(vmFromConfiguration, ovfEntityData))) {
                return false;
            }
            if (!validate(importValidator.validateStorageExistForUnregisteredEntity(getImages(),
                    getParameters().isAllowPartialImport(),
                    imageToDestinationDomainMap,
                    failedDisksToImportForAuditLog))) {
                return false;
            }
            if (!validate(importValidator.validateStorageExistsForMemoryDisks(getVm().getSnapshots(),
                    getParameters().isAllowPartialImport(),
                    failedDisksToImportForAuditLog))) {
                return false;
            }
            setImagesWithStoragePoolId(getParameters().getStoragePoolId(), getVm().getImages());
        }
        return super.validate();
    }

    private boolean validateExternalVnicProfileMapping() {
        final ValidationResult validationResult =
                externalVnicProfileMappingValidator.validateExternalVnicProfileMapping(
                        getParameters().getExternalVnicProfileMappings(),
                        getParameters().getClusterId());
        return validate(validationResult);
    }

    @Override
    protected boolean isExternalMacsToBeReported() {
        return !getParameters().isReassignBadMacs();
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected void init() {
        VM vmFromConfiguration = getParameters().getVm();
        if (vmFromConfiguration != null) {
            vmFromConfiguration.getStaticData().setClusterId(getParameters().getClusterId());
            if (!isImagesAlreadyOnTarget()) {
                setDisksToBeAttached(vmFromConfiguration);
            }
            getParameters().setContainerId(vmFromConfiguration.getId());
        } else {
            initUnregisteredVM();
        }

        if (Guid.Empty.equals(getParameters().getVmId()) && getParameters().getVm() != null) {
            getParameters().setVmId(getParameters().getVm().getId());
        }
        setClusterId(getParameters().getClusterId());
        getParameters().setStoragePoolId(getCluster().getStoragePoolId());
        super.init();
    }

    private void initUnregisteredVM() {
        List<OvfEntityData> ovfEntityDataList =
                unregisteredOVFDataDao.getByEntityIdAndStorageDomain(getParameters().getContainerId(),
                        getParameters().getStorageDomainId());
        if (!ovfEntityDataList.isEmpty()) {
            try {
                // We should get only one entity, since we fetched the entity with a specific Storage Domain
                ovfEntityData = ovfEntityDataList.get(0);
                FullEntityOvfData fullEntityOvfData = ovfHelper.readVmFromOvf(ovfEntityData.getOvfData());
                vmFromConfiguration = fullEntityOvfData.getVm();
                mapCluster(fullEntityOvfData.getClusterName());
                vmFromConfiguration.setClusterId(getParameters().getClusterId());
                mapVnicProfiles(vmFromConfiguration.getInterfaces());
                getParameters().setVm(vmFromConfiguration);
                getParameters().setDestDomainId(ovfEntityData.getStorageDomainId());
                getParameters().setSourceDomainId(ovfEntityData.getStorageDomainId());
                getParameters().setAffinityGroups(fullEntityOvfData.getAffinityGroups());

                // For quota, update disks when required
                if (getParameters().getDiskMap() != null) {
                    vmFromConfiguration.setDiskMap(getParameters().getDiskMap());
                    vmFromConfiguration.setImages(getDiskImageListFromDiskMap(getParameters().getDiskMap()));
                }
                mapExternalLunDisks(DisksFilter.filterLunDisks(vmFromConfiguration.getDiskMap().values()));
            } catch (OvfReaderException e) {
                log.error("Failed to parse a given ovf configuration: {}:\n{}",
                        e.getMessage(),
                        ovfEntityData.getOvfData());
                log.debug("Exception", e);
            }
        }
    }

    private void mapVnicProfiles(List<VmNetworkInterface> vnics) {
        vnics.forEach(vnic ->
                importedNetworkInfoUpdater.updateNetworkInfo(vnic, getParameters().getExternalVnicProfileMappings()));
    }

    private void mapCluster(String clusterName) {
        if (getParameters().getClusterMap() != null) {
            String clusterDest = getParameters().getClusterMap().get(clusterName);
            Cluster cluster = clusterDao.getByName(clusterDest);
            if (cluster == null) {
                cluster = clusterDao.getByName(clusterName);
            }
            if (cluster != null) {
                getParameters().setClusterId(cluster.getId());
            }
        }
    }

    private void mapExternalLunDisks(List<LunDisk> luns) {
        luns.forEach(lunDisk -> {
            if (getParameters().getExternalLunMap() != null) {
                LunDisk targetLunDisk = (LunDisk) getParameters().getExternalLunMap().get(lunDisk.getId().toString());
                if (targetLunDisk != null) {
                    lunDisk.setLun(targetLunDisk.getLun());
                    lunDisk.getLun()
                            .getLunConnections()
                            .forEach(conn -> conn.setStorageType(lunDisk.getLun().getLunType()));
                }
            }
        });
    }

    @Override
    protected List<AffinityGroup> mapAffinityGroups() {
        List<AffinityGroup> affinityGroups = new ArrayList<>();
        Map<String, String> affinityGroupMap = getParameters().getAffinityGroupMap();
        getParameters().getAffinityGroups().forEach(affinityGroup -> {
            AffinityGroup originalAffinityGroup = affinityGroupDao.getByName(affinityGroup.getName());

            if (affinityGroupMap != null) {
                String destName = affinityGroupMap.get(affinityGroup.getName());
                if (destName != null) {
                    AffinityGroup destAffinityGroup = affinityGroupDao.getByName(destName);
                    addAffinityGroup(affinityGroups, destAffinityGroup, originalAffinityGroup);
                } else {
                    addAffinityGroup(affinityGroups, originalAffinityGroup, null);
                }
            } else {
                addAffinityGroup(affinityGroups, originalAffinityGroup, null);
            }
        });

        return affinityGroups;
    }

    private void addAffinityGroup(List<AffinityGroup> affinityGroups,
                                  AffinityGroup affinityGroup,
                                  AffinityGroup alternativeAffinityGroup) {
        if (affinityGroup != null) {
            affinityGroups.add(affinityGroup);
        } else if (alternativeAffinityGroup != null) {
            affinityGroups.add(alternativeAffinityGroup);
        }
    }

    @Override
    public void addVmToAffinityGroups() {
        mapAffinityGroups().forEach(affinityGroup -> {
            affinityGroup.setClusterId(getParameters().getClusterId());
            Set<Guid> vmIds = new HashSet<>(affinityGroup.getVmIds());
            vmIds.add(getParameters().getVm().getId());
            affinityGroup.setVmIds(new ArrayList<>(vmIds));
            affinityGroupDao.update(affinityGroup);
        });
    }

    private static ArrayList<DiskImage> getDiskImageListFromDiskMap(Map<Guid, Disk> diskMap) {
        return diskMap.values().stream().map(disk -> (DiskImage) disk).collect(Collectors.toCollection(ArrayList::new));
    }

    private void setDisksToBeAttached(VM vmFromConfiguration) {
        vmDisksToAttach = vmFromConfiguration.getDiskMap().values();
        clearVmDisks(vmFromConfiguration);
        getParameters().setCopyCollapse(true);
    }

    @Override
    public void executeVmCommand() {
        addAuditLogForPartialVMs();
        super.executeVmCommand();
        if (getSucceeded()) {
            if (isImagesAlreadyOnTarget()) {
                getImages().stream().forEach(diskImage -> {
                    initQcowVersionForDisks(diskImage.getId());
                });
                unregisteredOVFDataDao.removeEntity(ovfEntityData.getEntityId(), null);
                unregisteredDisksDao.removeUnregisteredDiskRelatedToVM(ovfEntityData.getEntityId(), null);
                auditLogDirector.log(this, AuditLogType.VM_IMPORT_FROM_CONFIGURATION_EXECUTED_SUCCESSFULLY);
            } else if (!vmDisksToAttach.isEmpty()) {
                auditLogDirector.log(this, attemptToAttachDisksToImportedVm(vmDisksToAttach));
            }
        }
        setActionReturnValue(getVm().getId());
    }

    private void addAuditLogForPartialVMs() {
        if (getParameters().isAllowPartialImport() && !failedDisksToImportForAuditLog.isEmpty()) {
            addCustomValue("DiskAliases", StringUtils.join(failedDisksToImportForAuditLog.values(), ", "));
            auditLogDirector.log(this, AuditLogType.IMPORTEXPORT_PARTIAL_VM_DISKS_NOT_EXISTS);
        }
    }

    private static void clearVmDisks(VM vm) {
        vm.setDiskMap(Collections.emptyMap());
        vm.getImages().clear();
        vm.getDiskList().clear();
    }

    private AuditLogType attemptToAttachDisksToImportedVm(Collection<Disk> disks) {
        List<String> failedDisks = new LinkedList<>();
        for (Disk disk : disks) {
            DiskVmElement dve = disk.getDiskVmElements().iterator().next();
            AttachDetachVmDiskParameters params = new AttachDetachVmDiskParameters(
                    dve, dve.isPlugged());
            ActionReturnValue
                    returnVal = runInternalAction(ActionType.AttachDiskToVm, params, cloneContextAndDetachFromParent());
            if (!returnVal.getSucceeded()) {
                failedDisks.add(disk.getDiskAlias());
            }
        }

        if (!failedDisks.isEmpty()) {
            this.addCustomValue("DiskAliases", StringUtils.join(failedDisks, ","));
            return AuditLogType.VM_IMPORT_FROM_CONFIGURATION_ATTACH_DISKS_FAILED;
        }

        return AuditLogType.VM_IMPORT_FROM_CONFIGURATION_EXECUTED_SUCCESSFULLY;
    }

    @Override
    protected boolean validateAndSetVmFromExportDomain() {
        // We have the VM configuration so there is no need to get it from the export domain.
        return true;
    }

    @Override
    protected Guid getSourceDomainId(DiskImage image) {
        return image.getStorageIds().get(0);
    }
}
