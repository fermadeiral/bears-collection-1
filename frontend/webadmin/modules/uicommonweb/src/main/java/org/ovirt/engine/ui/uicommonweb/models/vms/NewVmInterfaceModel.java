package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.IModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NewVmInterfaceModel extends VmInterfaceModel {

    public static NewVmInterfaceModel createInstance(VmBase vm,
            VMStatus vmStatus,
            Guid dcId,
            Version clusterCompatibilityVersion,
            ArrayList<VmNetworkInterface> vmNicList,
            IModel sourceModel) {
        NewVmInterfaceModel instance = new NewVmInterfaceModel(vm,
                vmStatus,
                dcId,
                clusterCompatibilityVersion,
                vmNicList,
                sourceModel);
        instance.init();
        return instance;
    }

    protected NewVmInterfaceModel(VmBase vm,
            VMStatus vmStatus,
            Guid dcId,
            Version clusterCompatibilityVersion,
            ArrayList<VmNetworkInterface> vmNicList,
            IModel sourceModel) {
        super(vm,
                vmStatus,
                dcId,
                clusterCompatibilityVersion,
                vmNicList, sourceModel,
                new NewProfileBehavior());
        setTitle(ConstantsManager.getInstance().getConstants().newNetworkInterfaceTitle());
        setHelpTag(HelpTag.new_network_interface_vms);
        setHashName("new_network_interface_vms"); //$NON-NLS-1$
    }

    @Override
    protected void init() {
        AsyncDataProvider.getInstance().getNicTypeList(getVm().getOsId(), getClusterCompatibilityVersion(), new AsyncQuery<>(new AsyncCallback<List<VmInterfaceType>>() {
            @Override
            public void onSuccess(List<VmInterfaceType> returnValue) {
                setSupportedVnicTypes(returnValue);
                postNicInit();
            }
        }));
    }

    private void postNicInit() {
        getName().setEntity(AsyncDataProvider.getInstance().getNewNicName(getVmNicList()));
        initMAC();

        if (!allowPlug()) {
            getPlugged().setChangeProhibitionReason(ConstantsManager.getInstance()
                    .getMessages()
                    .nicHotPlugNotSupported(getClusterCompatibilityVersion().toString()));
        }
        getPlugged().setIsChangeable(allowPlug());
        getPlugged().setEntity(allowPlug());

        initLinked();

        initProfiles();
        initCommands();
    }

    @Override
    protected VmNetworkInterface createBaseNic() {
        return new VmNetworkInterface();
    }

    @Override
    protected VdcActionType getVdcActionType() {
        return VdcActionType.AddVmInterface;
    }

    @Override
    protected String getDefaultMacAddress() {
        return ""; //$NON-NLS-1$
    }

    @Override
    protected void initSelectedType() {
        final VmInterfaceType defaultNicType = getDefaultNicTypeByProfile();

        if (getNicType().getItems() == null) {
            getNicType().setItems(getSupportedVnicTypes(), defaultNicType);
        } else {
            getNicType().setSelectedItem(defaultNicType);
        }
    }

    @Override
    protected void initMAC() {
        getMAC().setIsChangeable(false);

    }

    @Override
    protected void initLinked() {
        getLinked().setEntity(true);
    }

    @Override
    protected VdcActionParametersBase createVdcActionParameters(VmNetworkInterface nicToSave) {
        return new AddVmInterfaceParameters(getVm().getId(), nicToSave);
    }

    protected VmNetworkInterface getNic() {
        // no nic for new
        return null;
    }
}
