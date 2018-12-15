package org.ovirt.engine.ui.common.presenter;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

/**
 * Base class for presenters representing tabs within the user interface.
 *
 * @param <V>
 *            View type.
 * @param <P>
 *            Proxy type.
 */
public abstract class AbstractTabPresenter<V extends View, P extends TabContentProxyPlace<?>> extends Presenter<V, P> {

    private PluginActionButtonHandler actionButtonPluginHandler;
    private final ActionPanelPresenterWidget<?, ?> actionPanel;

    public AbstractTabPresenter(EventBus eventBus, V view, P proxy, ActionPanelPresenterWidget<?, ?> actionPanel,
            Type<RevealContentHandler<?>> slot) {
        super(eventBus, view, proxy, slot);
        this.actionPanel = actionPanel;
    }

    public ActionPanelPresenterWidget<?, ?> getActionPanelPresenterWidget() {
        return actionPanel;
    }

    public boolean hasActionPanelPresenterWidget() {
        return getActionPanelPresenterWidget() != null;
    }

    @Override
    protected void onBind() {
        super.onBind();
        Scheduler.get().scheduleDeferred(() -> addPluginActionButtons(actionButtonPluginHandler.getButtons(getProxy().getTargetHistoryToken())));
        //Register this handler for whichever tab opens first, it is bound before the plugin fires its events and
        //before the actionButtonPluginHandler is instantiated and listening to events.
        registerHandler(getEventBus().addHandler(AddTabActionButtonEvent.getType(),
            event -> {
                if (getProxy().getTargetHistoryToken().equals(event.getHistoryToken())) {
                    List<ActionButtonDefinition<?>> pluginActionButtonList = new ArrayList<>();
                    pluginActionButtonList.add(event.getButtonDefinition());
                    addPluginActionButtons(pluginActionButtonList);
                }
            }));

    }

    private void addPluginActionButtons(List<ActionButtonDefinition<?>> pluginActionButtonList) {
        if (getActionPanelPresenterWidget() != null) {
            for(ActionButtonDefinition<?> buttonDef: pluginActionButtonList) {
                getActionPanelPresenterWidget().addActionButton((ActionButtonDefinition) buttonDef);
            }
        }
    }

    @Inject
    public void setActionButtonPluginHandler(PluginActionButtonHandler actionButtonPluginHandler) {
        this.actionButtonPluginHandler = actionButtonPluginHandler;
    }
}
