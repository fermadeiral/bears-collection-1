package org.ovirt.engine.core.bll.storage.disk;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleDiskVmElementValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageOperation;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmDao;

public class MoveOrCopyDiskCommandTest extends BaseCommandTest {

    private final Guid diskImageGuid = Guid.newGuid();
    private Guid destStorageId = Guid.newGuid();
    private final Guid srcStorageId = Guid.newGuid();
    private final VmDevice vmDevice = new VmDevice();

    @Mock
    private DiskDao diskDao;
    @Mock
    private DiskImageDao diskImageDao;
    @Mock
    private StorageDomainDao storageDomainDao;
    @Mock
    private VmDao vmDao;
    @Mock
    private MultipleDiskVmElementValidator multipleDiskVmElementValidator;
    @Mock
    private SnapshotsValidator snapshotsValidator;
    @Mock
    private StorageDomainValidator storageDomainValidator;
    @Mock
    private DiskValidator diskValidator;

    /**
     * The command under test.
     */
    @Spy
    @InjectMocks
    protected MoveOrCopyDiskCommand<MoveOrCopyImageGroupParameters> command =
            new MoveOrCopyDiskCommand<>(new MoveOrCopyImageGroupParameters(diskImageGuid,
                    srcStorageId,
                    destStorageId,
                    ImageOperation.Move),
                    null);

    @Test
    public void validateImageNotFound() {
        initializeCommand(new DiskImage(), VmEntityType.VM);
        when(diskValidator.isDiskExists())
                .thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST));
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);

    }

    @Test
    public void validateWrongDiskImageTypeTemplate() {
        initializeCommand(new DiskImage(), VmEntityType.TEMPLATE);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISK_IS_NOT_VM_DISK);
    }

    @Test
    public void moveShareableDiskToGlusterDomain() {
        DiskImage disk = new DiskImage();
        disk.setShareable(true);
        initializeCommand(disk, VmEntityType.VM);
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.GLUSTERFS);

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_CANT_MOVE_SHAREABLE_DISK_TO_GLUSTERFS);
    }

    @Test
    public void moveShareableDisk() {
        DiskImage disk = new DiskImage();
        disk.setShareable(true);
        initializeCommand(disk, VmEntityType.VM);
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.NFS);

        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void moveDiskToGluster() {
        DiskImage disk = new DiskImage();
        initializeCommand(disk, VmEntityType.VM);
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.GLUSTERFS);
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void validateSameSourceAndDest() {
        destStorageId = srcStorageId;
        initializeCommand(new DiskImage(), VmEntityType.VM);
        command.getParameters().setStorageDomainId(destStorageId);
        command.setStorageDomainId(destStorageId);
        initSrcStorageDomain();
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_SOURCE_AND_TARGET_SAME);
    }

    @Test
    public void validateVmIsNotDown() {
        initializeCommand(new DiskImage(), VmEntityType.VM);
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.NFS);

        when(diskValidator.isDiskPluggedToAnyNonDownVm(anyBoolean()))
                .thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN));

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
    }

    @Test
    public void validateDiskIsLocked() {
        initializeCommand(new DiskImage(), VmEntityType.VM);
        command.getImage().setImageStatus(ImageStatus.LOCKED);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED);
    }

    @Test
    public void validateDiskInBackupDomainForDownVM() {
        initializeCommand(new DiskImage(), VmEntityType.VM);
        initSrcStorageDomain();
        StorageDomain destDomain = new StorageDomain();
        destDomain.setStorageType(StorageType.NFS);
        destDomain.setStatus(StorageDomainStatus.Active);
        destDomain.setBackup(true);
        doReturn(destDomain).when(command).getStorageDomain();
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void validateDiskIsOvfStore() {
        testMoveOrCopyForContentTypeFails(DiskContentType.OVF_STORE);
    }

    @Test
    public void testMoveOrCopyMemoryDiskFails() {
        testMoveOrCopyForContentTypeFails(DiskContentType.MEMORY_DUMP_VOLUME);
    }

    private void testMoveOrCopyForContentTypeFails(DiskContentType contentType) {
        initializeCommand(new DiskImage(), VmEntityType.VM);
        command.getImage().setContentType(contentType);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISK_CONTENT_TYPE_NOT_SUPPORTED_FOR_OPERATION);
    }

    @Test
    public void validateTemplateImageIsLocked() {
        initializeCommand(new DiskImage(), VmEntityType.TEMPLATE);
        command.getParameters().setOperation(ImageOperation.Copy);
        command.getImage().setImageStatus(ImageStatus.LOCKED);
        doReturn(new VmTemplate()).when(command).getTemplateForImage();

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.VM_TEMPLATE_IMAGE_IS_LOCKED);
    }

    @Test
    public void validateNotEnoughSpace() {
        initializeCommand(new DiskImage(), VmEntityType.VM);
        initVmForSpace();
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.NFS);
        mockStorageDomainValidatorWithoutSpace();
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN);
    }

    @Test
    public void validateEnoughSpace() {
        initializeCommand(new DiskImage(), VmEntityType.VM);
        initVmForSpace();
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.NFS);
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void successVmInPreviewForAttachedSnapshot() {
        initializeCommand(new DiskImage(), VmEntityType.VM);
        initVmForSpace();
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.NFS);
        vmDevice.setSnapshotId(Guid.newGuid());
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void validateVmInPreview() {
        initializeCommand(new DiskImage(), VmEntityType.VM);
        initVmForSpace();
        initSrcStorageDomain();
        initDestStorageDomain(StorageType.NFS);
        when(snapshotsValidator.vmNotInPreview(any())).thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IN_PREVIEW));
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_IN_PREVIEW);
    }

    @Test
    public void passDiscardSupportedForDestSdMoveOp() {
        initializeCommand(new DiskImage(), VmEntityType.VM);
        mockPassDiscardSupportedForDestSd(ValidationResult.VALID, ImageOperation.Move);
        assertTrue(command.validatePassDiscardSupportedForDestinationStorageDomain());
    }

    @Test
    public void passDiscardSupportedForDestSdCopyTemplateDiskOp() {
        initializeCommand(new DiskImage(), VmEntityType.TEMPLATE);
        mockPassDiscardSupportedForDestSd(ValidationResult.VALID, ImageOperation.Copy);
        assertTrue(command.validatePassDiscardSupportedForDestinationStorageDomain());
    }

    @Test
    public void passDiscardSupportedForCopyFloatingDiskOp() {
        initializeCommand(new DiskImage(), VmEntityType.TEMPLATE);
        command.getParameters().setOperation(ImageOperation.Copy);
        assertTrue(command.validatePassDiscardSupportedForDestinationStorageDomain());
    }

    @Test
    public void passDiscardNotSupportedForDestSd() {
        initializeCommand(new DiskImage(), VmEntityType.VM);
        mockPassDiscardSupportedForDestSd(new ValidationResult(
                EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DISK_INTERFACE), ImageOperation.Move);
        assertFalse(command.validatePassDiscardSupportedForDestinationStorageDomain());
    }

    protected void initVmForSpace() {
        VM vm = new VM();
        vm.setStatus(VMStatus.Down);

        // Re-mock the vmDao to return this specific VM for it to be correlated with the vm list mocked by getVmsWithPlugInfo(..).
        when(vmDao.get(any())).thenReturn(vm);
        List<Pair<VM, VmDevice>> vmList = Collections.singletonList(new Pair<>(vm, vmDevice));
        when(vmDao.getVmsWithPlugInfo(any())).thenReturn(vmList);
    }

    private void mockStorageDomainValidatorWithoutSpace() {
        when(storageDomainValidator.hasSpaceForDiskWithSnapshots(any())).thenReturn(
                new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
    }

    private void initSrcStorageDomain() {
        StorageDomain stDomain = new StorageDomain();
        stDomain.setStatus(StorageDomainStatus.Active);
        when(storageDomainDao.getForStoragePool(any(), any())).thenReturn(stDomain);
    }

    private void initDestStorageDomain(StorageType storageType) {
        StorageDomain destDomain = new StorageDomain();
        destDomain.setStorageType(storageType);
        destDomain.setStatus(StorageDomainStatus.Active);
        doReturn(destDomain).when(command).getStorageDomain();
    }

    protected void initializeCommand(DiskImage disk, VmEntityType vmEntityType) {
        disk.setVmEntityType(vmEntityType);
        when(diskDao.get(any())).thenReturn(disk);
        when(diskImageDao.get(any())).thenReturn(disk);

        VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        when(vmDao.get(any())).thenReturn(vm);

        doReturn(storageDomainValidator).when(command).createStorageDomainValidator();
        doReturn(multipleDiskVmElementValidator).when(command).createMultipleDiskVmElementValidator();
        doReturn(diskValidator).when(command).createDiskValidator(disk);
        doReturn(true).when(command).setAndValidateDiskProfiles();
        doReturn(disk.getId()).when(command).getImageGroupId();
        doReturn(ActionType.MoveOrCopyDisk).when(command).getActionType();
        command.init();
    }

    private void mockPassDiscardSupportedForDestSd(ValidationResult validationResult, ImageOperation imageOperation) {
        command.getParameters().setOperation(imageOperation);
        MultipleDiskVmElementValidator multipleDiskVmElementValidator = mock(MultipleDiskVmElementValidator.class);
        doReturn(multipleDiskVmElementValidator).when(command).createMultipleDiskVmElementValidator();
        when(multipleDiskVmElementValidator.isPassDiscardSupportedForDestSd(any())).thenReturn(validationResult);
    }
}
