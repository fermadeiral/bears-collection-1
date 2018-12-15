package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.gluster.HostGlusterSwiftListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabHostGlusterSwiftPresenter
    extends AbstractSubTabHostPresenter<HostGlusterSwiftListModel, SubTabHostGlusterSwiftPresenter.ViewDef,
        SubTabHostGlusterSwiftPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.hostGlusterSwiftSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabHostGlusterSwiftPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VDS> {
    }

    @TabInfo(container = HostSubTabPanelPresenter.class)
    static TabData getTabData(
            SearchableDetailModelProvider<GlusterServerService, HostListModel<Void>, HostGlusterSwiftListModel> modelProvider) {
        return new ModelBoundTabData(constants.hostGlusterSwiftSubTabLabel(), 8, modelProvider);
    }

    @Inject
    public SubTabHostGlusterSwiftPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, HostMainTabSelectedItems selectedItems,
            SearchableDetailModelProvider<GlusterServerService, HostListModel<Void>, HostGlusterSwiftListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems,
                HostSubTabPanelPresenter.TYPE_SetTabContent);
    }
}
