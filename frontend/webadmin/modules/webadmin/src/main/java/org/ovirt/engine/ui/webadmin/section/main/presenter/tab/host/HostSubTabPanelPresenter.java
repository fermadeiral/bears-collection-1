package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.presenter.DynamicTabContainerPresenter.DynamicTabPanel;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DetailTabDataIndex;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ChangeTabHandler;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.ChangeTab;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.RequestTabs;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class HostSubTabPanelPresenter extends
    AbstractSubTabPanelPresenter<HostSubTabPanelPresenter.ViewDef, HostSubTabPanelPresenter.ProxyDef> {

    @ProxyCodeSplit
    public interface ProxyDef extends Proxy<HostSubTabPanelPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPanelPresenter.ViewDef, DynamicTabPanel {
    }

    @RequestTabs
    public static final Type<RequestTabsHandler> TYPE_RequestTabs = new Type<>();

    @ChangeTab
    public static final Type<ChangeTabHandler> TYPE_ChangeTab = new Type<>();

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetTabContent = new Type<>();

    @Inject
    private MainModelProvider<VDS, HostListModel<Void>> modelProvider;

    @Inject
    public HostSubTabPanelPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            HostMainSelectedItems selectedItems) {
        super(eventBus, view, proxy, TYPE_SetTabContent, TYPE_RequestTabs, TYPE_ChangeTab, selectedItems);
    }

    @Override
    protected void initDetailTabToModelMapping(Map<TabData, Model> mapping) {
        HostListModel<Void> mainModel = modelProvider.getModel();
        mapping.put(DetailTabDataIndex.HOSTS_GENERAL, mainModel.getGeneralModel());
        mapping.put(DetailTabDataIndex.HOSTS_VMS, mainModel.getVmListModel());
        mapping.put(DetailTabDataIndex.HOSTS_IFACE, mainModel.getInterfaceListModel());
        mapping.put(DetailTabDataIndex.HOSTS_DEVICES, mainModel.getDeviceListModel());
        mapping.put(DetailTabDataIndex.HOSTS_HOOKS, mainModel.getHooksListModel());
        mapping.put(DetailTabDataIndex.HOSTS_BRICKS, mainModel.getBricksListModel());
        mapping.put(DetailTabDataIndex.HOSTS_GLUSTER_STORAGE_DEVICES, mainModel.getGlusterStorageDeviceListModel());
        mapping.put(DetailTabDataIndex.HOSTS_PERMISSIONS, mainModel.getPermissionListModel());
        mapping.put(DetailTabDataIndex.HOSTS_AFFINITY_LABELS, mainModel.getAffinityLabelListModel());
        mapping.put(DetailTabDataIndex.HOSTS_ERRATA, mainModel.getErrataCountModel());
        mapping.put(DetailTabDataIndex.HOSTS_EVENTS, mainModel.getEventListModel());
        mapping.put(DetailTabDataIndex.HOSTS_GLUSTER_SWIFT, mainModel.getGlusterSwiftModel());
    }

}
