package org.ovirt.engine.core.bll.memory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collector;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.scheduling.VmOverheadCalculator;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.compat.Guid;

public class MemoryUtils {

    /** The size for the snapshot's meta data which is vm related properties at the
     *  time the snapshot was taken */
    public static final long METADATA_SIZE_IN_BYTES = 10 * 1024;

    private static final String VM_HIBERNATION_METADATA_DISK_DESCRIPTION = "metadata for VM hibernation";
    private static final String VM_HIBERNATION_MEMORY_DISK_DESCRIPTION = "memory dump for VM hibernation";
    private static final String VM_HIBERNATION_METADATA_DISK_ALIAS_PATTERN = "%s_hibernation_metadata";
    private static final String VM_HIBERNATION_MEMORY_DISK_ALIAS_PATTERN = "%s_hibernation_memory";

    private static final String VM_SNAPSHOT_METADATA_DISK_ALIAS = "snapshot_metadata";
    private static final String VM_SNAPSHOT_MEMORY_DISK_ALIAS = "snapshot_memory";
    private static final String MEMORY_DISK_DESCRIPTION = "Memory snapshot disk for snapshot '%s' of VM '%s' (VM ID: '%s')";

    /**
     * Modified the given memory volume String representation to have the given storage
     * pool and storage domain
     */
    public static String changeStorageDomainAndPoolInMemoryState(
            String originalMemoryVolume, Guid storageDomainId, Guid storagePoolId) {
        List<Guid> guids = Guid.createGuidListFromString(originalMemoryVolume);
        return createMemoryStateString(storageDomainId, storagePoolId,
                guids.get(2), guids.get(3), guids.get(4), guids.get(5));
    }

    public static String createMemoryStateString(
            Guid storageDomainId, Guid storagePoolId, Guid memoryImageId,
            Guid memoryVolumeId, Guid confImageId, Guid confVolumeId) {
        return String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s",
                storageDomainId,
                storagePoolId,
                memoryImageId,
                memoryVolumeId,
                confImageId,
                confVolumeId);
    }

    public static Set<String> getMemoryVolumesFromSnapshots(List<Snapshot> snapshots) {
        Set<String> memories = new HashSet<>();
        for (Snapshot snapshot : snapshots) {
            memories.add(snapshot.getMemoryVolume());
        }
        memories.remove(StringUtils.EMPTY);
        return memories;
    }

    public static List<DiskImage> createDiskDummies(long memorySize, long metadataSize) {
        DiskImage memoryVolume = new DiskImage();
        memoryVolume.setDiskAlias("memory");
        memoryVolume.setVolumeFormat(VolumeFormat.RAW);
        memoryVolume.setSize(memorySize);
        memoryVolume.setActualSizeInBytes(memorySize);
        memoryVolume.getSnapshots().add(memoryVolume);

        DiskImage dataVolume = new DiskImage();
        dataVolume.setDiskAlias("metadata");
        dataVolume.setVolumeFormat(VolumeFormat.RAW);
        dataVolume.setVolumeType(VolumeType.Preallocated);
        dataVolume.setSize(metadataSize);
        dataVolume.setActualSizeInBytes(metadataSize);
        dataVolume.getSnapshots().add(dataVolume);

        return Arrays.asList(memoryVolume, dataVolume);
    }

    public static DiskImage createSnapshotMetadataDisk(String diskDescription) {
        DiskImage image = createMetadataDisk(diskDescription);
        image.setDiskAlias(VM_SNAPSHOT_METADATA_DISK_ALIAS);
        image.setImageStatus(ImageStatus.OK);
        return image;
    }

    public static DiskImage createHibernationMetadataDisk(VM vm) {
        DiskImage image = createMetadataDisk(null);
        image.setDiskAlias(generateHibernationMetadataDiskAlias(vm.getName()));
        image.setDiskDescription(VM_HIBERNATION_METADATA_DISK_DESCRIPTION);
        return image;
    }

    private static DiskImage createMetadataDisk(String diskDescription) {
        DiskImage image = new DiskImage();
        image.setSize(MemoryUtils.METADATA_SIZE_IN_BYTES);
        image.setVolumeType(VolumeType.Preallocated);
        image.setVolumeFormat(VolumeFormat.RAW);
        image.setContentType(DiskContentType.MEMORY_METADATA_VOLUME);
        image.setDiskDescription(diskDescription);
        return image;
    }

    private static String generateHibernationMetadataDiskAlias(String vmName) {
        return String.format(VM_HIBERNATION_METADATA_DISK_ALIAS_PATTERN, vmName);
    }

    public static DiskImage createSnapshotMemoryDisk(VM vm, StorageType storageType, VmOverheadCalculator vmOverheadCalculator, String diskDescription) {
        DiskImage image = createMemoryDisk(vm, storageType, vmOverheadCalculator, diskDescription);
        image.setDiskAlias(VM_SNAPSHOT_MEMORY_DISK_ALIAS);
        image.setImageStatus(ImageStatus.OK);
        return image;
    }

    public static DiskImage createHibernationMemoryDisk(VM vm, StorageType storageType, VmOverheadCalculator vmOverheadCalculator) {
        DiskImage image = createMemoryDisk(vm, storageType, vmOverheadCalculator, null);
        image.setDiskAlias(generateHibernationMemoryDiskAlias(vm.getName()));
        image.setDiskDescription(VM_HIBERNATION_MEMORY_DISK_DESCRIPTION);
        return image;
    }

    private static DiskImage createMemoryDisk(VM vm, StorageType storageType, VmOverheadCalculator vmOverheadCalculator, String diskDescription) {
        DiskImage image = new DiskImage();
        image.setSize(vmOverheadCalculator.getSnapshotMemorySizeInBytes(vm));
        image.setVolumeType(storageTypeToMemoryVolumeType(storageType));
        image.setVolumeFormat(VolumeFormat.RAW);
        image.setContentType(DiskContentType.MEMORY_DUMP_VOLUME);
        image.setDiskDescription(diskDescription);
        return image;
    }

    private static String generateHibernationMemoryDiskAlias(String vmName) {
        return String.format(VM_HIBERNATION_MEMORY_DISK_ALIAS_PATTERN, vmName);
    }

    /**
     * Returns whether to use Sparse or Preallocation. If the storage type is file system devices ,it would be more
     * efficient to use Sparse allocation. Otherwise for block devices we should use Preallocated for faster allocation.
     *
     * @return - VolumeType of allocation type to use.
     */
    public static VolumeType storageTypeToMemoryVolumeType(StorageType storageType) {
        return storageType.isFileDomain() ? VolumeType.Sparse : VolumeType.Preallocated;
    }

    public static Guid getMemoryDiskId(String memoryVolume) {
        return StringUtils.isEmpty(memoryVolume) ? null
                : Guid.createGuidListFromString(memoryVolume).get(2);
    }

    public static Guid getMetadataDiskId(String memoryVolume) {
        return StringUtils.isEmpty(memoryVolume) ? null
                : Guid.createGuidListFromString(memoryVolume).get(4);
    }

    public static String generateMemoryDiskDescription(VM vm, String snapshotDescription) {
        return String.format(MEMORY_DISK_DESCRIPTION, snapshotDescription, vm.getName(), vm.getId());
    }

    /**
     * It computes memory devices to hot unplug. Total size of selected devices is always <= requested memory decrement.
     *
     * Simplified solution of rucksack problem.
     */
    public static List<VmDevice> computeMemoryDevicesToHotUnplug(
            List<VmDevice> vmMemoryDevices,
            int currentMemoryMb,
            int desiredMemoryMb) {
        final int memoryToHotUnplugMb = currentMemoryMb - desiredMemoryMb;
        final List<VmDevice> memoryDevicesToUnplug = vmMemoryDevices.stream()
                .filter(VmDeviceCommonUtils::isMemoryDeviceHotUnpluggable)
                .filter(memoryDevice ->
                        memoryToHotUnplugMb >= VmDeviceCommonUtils.getSizeOfMemoryDeviceMb(memoryDevice).get())
                .sorted(Comparator.comparing(
                        (VmDevice memoryDevice) -> VmDeviceCommonUtils.getSizeOfMemoryDeviceMb(memoryDevice).get())
                        .reversed())
                .collect(Collector.of(
                        ArrayList::new,
                        new GreedyMemoryDeviceAccumulator(memoryToHotUnplugMb),
                        (listA, listB) -> {
                            listA.addAll(listB);
                            return listA;
                        }));
        return memoryDevicesToUnplug;
    }

    /**
     * It selects devices until the sum of size of selected devices is larger than {@link #memorySizeMax}.
     */
    private static class GreedyMemoryDeviceAccumulator implements BiConsumer<List<VmDevice>, VmDevice> {

        private final int memorySizeMax;
        private int selectedDevicesTotalSize = 0;

        private GreedyMemoryDeviceAccumulator(int memorySizeMax) {
            this.memorySizeMax = memorySizeMax;
        }

        @Override
        public void accept(List<VmDevice> sum, VmDevice memoryDevice) {
            final int deviceSizeMb = VmDeviceCommonUtils.getSizeOfMemoryDeviceMb(memoryDevice).get();
            if (selectedDevicesTotalSize + deviceSizeMb <= memorySizeMax) {
                sum.add(memoryDevice);
                selectedDevicesTotalSize += deviceSizeMb;
            }
        }
    }
}
