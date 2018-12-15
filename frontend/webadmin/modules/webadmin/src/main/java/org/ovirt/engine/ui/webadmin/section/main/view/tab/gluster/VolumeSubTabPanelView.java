package org.ovirt.engine.ui.webadmin.section.main.view.tab.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.OvirtBreadCrumbs;
import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.common.widget.tab.DetailTabLayout;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.VolumeSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabPanelView;
import org.ovirt.engine.ui.webadmin.widget.tab.SimpleTabPanel;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class VolumeSubTabPanelView extends AbstractSubTabPanelView implements VolumeSubTabPanelPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<VolumeSubTabPanelView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final SimpleTabPanel tabPanel;

    @Inject
    public VolumeSubTabPanelView(OvirtBreadCrumbs<GlusterVolumeEntity, VolumeListModel> breadCrumbs, DetailTabLayout detailTabLayout) {
        tabPanel = new SimpleTabPanel(breadCrumbs, detailTabLayout);
        initWidget(getTabPanel());
    }

    @Override
    protected Object getContentSlot() {
        return VolumeSubTabPanelPresenter.TYPE_SetTabContent;
    }

    @Override
    protected AbstractTabPanel getTabPanel() {
        return tabPanel;
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
