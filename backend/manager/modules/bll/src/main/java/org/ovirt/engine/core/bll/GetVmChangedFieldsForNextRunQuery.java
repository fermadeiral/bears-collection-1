package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.queries.GetVmChangedFieldsForNextRunParameters;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.utils.VmCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceUpdate;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;

public class GetVmChangedFieldsForNextRunQuery<P extends GetVmChangedFieldsForNextRunParameters>
        extends QueriesCommandBase<P>{

    @Inject
    private VmHandler vmHandler;

    public GetVmChangedFieldsForNextRunQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        VM srcVm = getParameters().getOriginal();
        VM dstVm = getParameters().getUpdated();
        VmStatic srcStatic = srcVm.getStaticData();
        VmStatic dstStatic = dstVm.getStaticData();

        // copy fields which are not saved as part of the OVF
        dstStatic.setExportDate(srcStatic.getExportDate());
        dstStatic.setManagedDeviceMap(srcStatic.getManagedDeviceMap());
        dstStatic.setUnmanagedDeviceList(srcStatic.getUnmanagedDeviceList());
        dstStatic.setOvfVersion(srcStatic.getOvfVersion());

        // Copy creationDate to ignore it, because it is never changed by user.
        // Without this creationDate will always show change in milliseconds,
        // because creationDate is saved without milliseconds in OVF, but
        // with milliseconds in the DB.
        dstStatic.setCreationDate(srcStatic.getCreationDate());

        // Hot plug CPU & memory are displayed separately in the confirmation dialog,
        // so it is not needed to include them into changed fields list.
        if (VmCommonUtils.isCpusToBeHotplugged(srcVm, dstVm)) {
            dstStatic.setNumOfSockets(srcStatic.getNumOfSockets());
        }
        if (VmCommonUtils.isMemoryToBeHotplugged(srcVm, dstVm)) {
            dstStatic.setMemSizeMb(srcStatic.getMemSizeMb());
            dstStatic.setMinAllocatedMem(srcStatic.getMinAllocatedMem());
        }

        VmPropertiesUtils vmPropertiesUtils = SimpleDependencyInjector.getInstance().get(VmPropertiesUtils.class);

        vmPropertiesUtils.separateCustomPropertiesToUserAndPredefined(
                srcVm.getCompatibilityVersion(), srcStatic);
        vmPropertiesUtils.separateCustomPropertiesToUserAndPredefined(
                dstVm.getCompatibilityVersion(), dstStatic);

        Set<String> result = new HashSet<>(vmHandler.getChangedFieldsForStatus(srcStatic, dstStatic, VMStatus.Up));

        for (VmDeviceUpdate device :
                vmHandler.getVmDevicesFieldsToUpdateOnNextRun(srcVm.getId(), VMStatus.Up, getParameters().getUpdateVmParameters())) {
            if (!device.getName().isEmpty()) {
                result.add(device.getName());
            } else {
                switch (device.getType()) {
                    case UNKNOWN:
                    case VIRTIO:
                        result.add(device.getGeneralType().name());
                        break;

                    default:
                        result.add(device.getType().getName());
                        break;
                }
            }
        }

        setReturnValue(new ArrayList<>(result));
    }

}
