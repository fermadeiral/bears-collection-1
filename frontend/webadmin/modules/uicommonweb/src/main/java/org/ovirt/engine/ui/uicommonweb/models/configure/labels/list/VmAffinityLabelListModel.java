package org.ovirt.engine.ui.uicommonweb.models.configure.labels.list;

import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class VmAffinityLabelListModel extends AffinityLabelListModel<VM> {

    public VmAffinityLabelListModel() {
        super(VdcQueryType.GetLabelByEntityId);
    }

    @Override
    protected Label getNewAffinityLabel() {
        Label affinityLabel = super.getNewAffinityLabel();
        affinityLabel.addVm(getEntity());
        return affinityLabel;
    }

    @Override
    protected Guid getClusterId() {
        return getEntity().getClusterId();
    }

    @Override
    protected String getClusterName() {
        return getEntity().getClusterName();
    }
}
