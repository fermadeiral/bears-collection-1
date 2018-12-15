package org.ovirt.engine.ui.uicommonweb.models.configure.labels.list;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class ClusterAffinityLabelListModel extends AffinityLabelListModel<Cluster> {

    public ClusterAffinityLabelListModel() {
        super(VdcQueryType.GetAllLabels);
    }

    @Override
    protected Guid getClusterId() {
        return getEntity().getId();
    }

    @Override
    protected String getClusterName() {
        return getEntity().getName();
    }
}
