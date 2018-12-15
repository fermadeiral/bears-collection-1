package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.OvirtBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.VirtualMachineActionPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.uicommon.model.TagActivationChangeEvent;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class MainVirtualMachinePresenter
    extends AbstractMainWithDetailsPresenter<VM, VmListModel<Void>, MainVirtualMachinePresenter.ViewDef,
        MainVirtualMachinePresenter.ProxyDef> implements TagActivationChangeEvent.TagActivationChangeHandler {

    @GenEvent
    public class VirtualMachineSelectionChange {

        List<VM> selectedItems;

    }

    private final TagEventCollector tagEventCollector;

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.virtualMachineMainPlace)
    public interface ProxyDef extends ProxyPlace<MainVirtualMachinePresenter> {
    }

    public interface ViewDef extends AbstractMainWithDetailsPresenter.ViewDef<VM> {
        void setActiveTags(List<TagModel> tags);
    }

    @Inject
    public MainVirtualMachinePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, MainModelProvider<VM, VmListModel<Void>> modelProvider,
            SearchPanelPresenterWidget<VM, VmListModel<Void>> searchPanelPresenterWidget,
            OvirtBreadCrumbsPresenterWidget<VM, VmListModel<Void>> breadCrumbs,
            TagEventCollector tagEventCollector,
            VirtualMachineActionPanelPresenterWidget actionPanel) {
        super(eventBus, view, proxy, placeManager, modelProvider, searchPanelPresenterWidget, breadCrumbs, actionPanel);
        this.tagEventCollector = tagEventCollector;
    }

    @Override
    protected void fireTableSelectionChangeEvent() {
        VirtualMachineSelectionChangeEvent.fire(this, getSelectedItems());
    }

    @Override
    protected PlaceRequest getMainViewRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.virtualMachineMainPlace);
    }

    @Override
    protected void onBind() {
        super.onBind();
        registerHandler(getEventBus().addHandler(TagActivationChangeEvent.getType(), this));
        tagEventCollector.getActivationEvents().forEach(e -> onTagActivationChange(e));
        tagEventCollector.activateVms();
    }

    @Override
    public void onTagActivationChange(TagActivationChangeEvent event) {
        getView().setActiveTags(event.getActiveTags());
        setTags(event.getActiveTags());
    }

}
