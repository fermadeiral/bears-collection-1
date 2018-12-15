package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class NetworkClusterActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<PairQueryable<Cluster, NetworkCluster>, NetworkListModel,
        NetworkClusterListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public NetworkClusterActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<PairQueryable<Cluster, NetworkCluster>> view,
            SearchableDetailModelProvider<PairQueryable<Cluster, NetworkCluster>, NetworkListModel,
                NetworkClusterListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<PairQueryable<Cluster, NetworkCluster>>(constants.assignUnassignNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getManageCommand();
            }
        });
    }

}
