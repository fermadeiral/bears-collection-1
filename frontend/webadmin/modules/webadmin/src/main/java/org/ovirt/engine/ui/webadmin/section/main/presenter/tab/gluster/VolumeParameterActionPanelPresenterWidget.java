package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeParameterListModel;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class VolumeParameterActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<GlusterVolumeOptionEntity, VolumeListModel, VolumeParameterListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public VolumeParameterActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<GlusterVolumeOptionEntity> view,
            SearchableDetailModelProvider<GlusterVolumeOptionEntity, VolumeListModel,
                VolumeParameterListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<GlusterVolumeOptionEntity>(constants.addVolumeParameter()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getAddParameterCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<GlusterVolumeOptionEntity>(constants.editVolumeParameter()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getEditParameterCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<GlusterVolumeOptionEntity>(constants.resetVolumeParameter()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getResetParameterCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<GlusterVolumeOptionEntity>(constants.resetAllVolumeParameter()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getResetAllParameterCommand();
            }
        });
    }

}
