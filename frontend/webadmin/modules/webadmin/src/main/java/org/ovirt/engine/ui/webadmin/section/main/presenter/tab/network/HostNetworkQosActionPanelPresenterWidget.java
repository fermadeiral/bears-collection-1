package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.DataCenterHostNetworkQosListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class HostNetworkQosActionPanelPresenterWidget extends
    ActionPanelPresenterWidget<HostNetworkQos, DataCenterHostNetworkQosListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public HostNetworkQosActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<HostNetworkQos> view,
            SearchableDetailModelProvider<HostNetworkQos, DataCenterListModel,
                DataCenterHostNetworkQosListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<HostNetworkQos>(constants.newQos()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<HostNetworkQos>(constants.editQos()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<HostNetworkQos>(constants.removeQos()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
    }

}
