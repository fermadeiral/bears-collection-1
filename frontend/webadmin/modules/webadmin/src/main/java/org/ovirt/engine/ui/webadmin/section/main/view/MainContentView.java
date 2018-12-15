package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainContentPresenter;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

public class MainContentView extends AbstractView implements MainContentPresenter.ViewDef {

    private final FlowPanel contentContainer = new FlowPanel();
    private final FlowPanel container = new FlowPanel();
    private final FlowPanel overlayContainer = new FlowPanel();

    @Inject
    public MainContentView(final ClientStorage clientStorage) {
        container.add(contentContainer);
        container.add(overlayContainer);
        initWidget(container);
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == MainContentPresenter.TYPE_SetContent) {
            overlayContainer.clear();
            setPanelContent(contentContainer, content);
            contentContainer.setVisible(true);
        } else if (slot == MainContentPresenter.TYPE_SetOverlay) {
            overlayContainer.clear();
            if (content == null) {
                contentContainer.setVisible(true);
            } else {
                overlayContainer.add(content);
                contentContainer.setVisible(false);
            }
        } else {
            super.setInSlot(slot, content);
        }
    }
}
