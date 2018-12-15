package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.common.widget.AlertManager;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class MainSectionPresenter extends Presenter<MainSectionPresenter.ViewDef, MainSectionPresenter.ProxyDef> {

    @ProxyCodeSplit
    public interface ProxyDef extends Proxy<MainSectionPresenter> {
    }

    public interface ViewDef extends View {
    }

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetHeader = new Type<>();

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetMainContent = new Type<>();

    private final HeaderPresenterWidget header;
    private final AlertManager alertManager;

    @Inject
    public MainSectionPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            HeaderPresenterWidget header, AlertManager alertManager) {
        super(eventBus, view, proxy, RevealType.Root);
        this.header = header;
        this.alertManager = alertManager;
    }

    @Override
    protected void onReveal() {
        super.onReveal();

        setInSlot(TYPE_SetHeader, header);

        // Remove the loading page placeholder
        removeHostPagePlaceholder();

        // Enable alerts within the scope of main section
        alertManager.setCanShowAlerts(true);
    }

    @Override
    protected void onHide() {
        super.onHide();

        // Disable alerts outside the scope of main section
        alertManager.setCanShowAlerts(false);
    }

    protected void removeHostPagePlaceholder() {
        Document.get().getElementById("host-page-placeholder").removeFromParent(); //$NON-NLS-1$
    }

}
