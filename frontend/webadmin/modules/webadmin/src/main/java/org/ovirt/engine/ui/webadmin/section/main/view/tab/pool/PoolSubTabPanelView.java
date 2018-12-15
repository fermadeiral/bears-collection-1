package org.ovirt.engine.ui.webadmin.section.main.view.tab.pool;

import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.OvirtBreadCrumbs;
import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.common.widget.tab.DetailTabLayout;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool.PoolSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabPanelView;
import org.ovirt.engine.ui.webadmin.widget.tab.SimpleTabPanel;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class PoolSubTabPanelView extends AbstractSubTabPanelView implements PoolSubTabPanelPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<PoolSubTabPanelView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final SimpleTabPanel tabPanel;

    @Inject
    public PoolSubTabPanelView(OvirtBreadCrumbs<VmPool, PoolListModel> breadCrumbs, DetailTabLayout detailTabLayout) {
        tabPanel = new SimpleTabPanel(breadCrumbs, detailTabLayout);
        initWidget(getTabPanel());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    protected Object getContentSlot() {
        return PoolSubTabPanelPresenter.TYPE_SetTabContent;
    }

    @Override
    protected AbstractTabPanel getTabPanel() {
        return tabPanel;
    }

}
