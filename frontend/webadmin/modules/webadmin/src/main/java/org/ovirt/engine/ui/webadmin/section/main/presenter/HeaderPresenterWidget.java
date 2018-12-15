package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.Collection;

import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.AbstractHeaderPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.OptionsProvider;
import org.ovirt.engine.ui.uicommonweb.models.events.AlertListModel;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationDynamicMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.overlay.BookmarkPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.overlay.TagsPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.overlay.TasksPresenterWidget;
import org.ovirt.engine.ui.webadmin.uicommon.model.AlertModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.EventModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.TaskModelProvider;
import org.ovirt.engine.ui.webadmin.widget.alert.ActionWidget;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.gwtplatform.mvp.client.proxy.RevealRootPopupContentEvent;

public class HeaderPresenterWidget extends AbstractHeaderPresenterWidget<HeaderPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractHeaderPresenterWidget.ViewDef {

        HasClickHandlers getConfigureLink();

        HasClickHandlers getAboutLink();

        HasData<AuditLog> getEventDropdown();

        HasData<AuditLog> getAlertDropdown();

        HasClickHandlers getTasksWidget();

        HasClickHandlers getBookmarkLink();

        HasClickHandlers getTagsLink();

        void setRunningTaskCount(int count);

        void setAlertCount(int count);

        ActionWidget getEventActionWidget();

        ActionWidget getAlertActionWidget();

    }

    private final ApplicationConstants constants = AssetProvider.getConstants();

    private final Provider<AboutPopupPresenterWidget> aboutPopupProvider;
    private final TaskModelProvider taskModelProvider;
    private final AlertModelProvider alertModelProvider;
    private final EventModelProvider eventModelProvider;
    private final TasksPresenterWidget tasksPresenter;
    private final BookmarkPresenterWidget bookmarksPresenter;
    private final TagsPresenterWidget tagsPresenter;

    @Inject
    public HeaderPresenterWidget(EventBus eventBus, ViewDef view, CurrentUser user,
            OptionsProvider optionsProvider,
            Provider<AboutPopupPresenterWidget> aboutPopupProvider,
            ApplicationDynamicMessages dynamicMessages,
            TasksPresenterWidget tasksPresenter,
            BookmarkPresenterWidget bookmarksPresenter,
            TagsPresenterWidget tagsPresenter,
            @Named("notification") EventModelProvider eventModelProvider,
            AlertModelProvider alertModelProvider,
            TaskModelProvider taskModelProvider) {
        super(eventBus, view, user, optionsProvider, dynamicMessages.applicationDocTitle(), dynamicMessages.guideUrl());
        this.aboutPopupProvider = aboutPopupProvider;
        this.taskModelProvider = taskModelProvider;
        this.alertModelProvider = alertModelProvider;
        this.eventModelProvider = eventModelProvider;
        this.tasksPresenter = tasksPresenter;
        this.bookmarksPresenter = bookmarksPresenter;
        this.tagsPresenter = tagsPresenter;
        eventModelProvider.addDataDisplay(getView().getEventDropdown());
        alertModelProvider.addDataDisplay(getView().getAlertDropdown());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onBind() {
        super.onBind();

        registerHandler(getView().getAboutLink().addClickHandler(event ->
                RevealRootPopupContentEvent.fire(HeaderPresenterWidget.this, aboutPopupProvider.get())));

        registerHandler(getView().getTasksWidget().addClickHandler(event -> toggleOverlayPresenter(tasksPresenter)));

        registerHandler(getView().getBookmarkLink().addClickHandler(event ->
            toggleOverlayPresenter(bookmarksPresenter)));

        registerHandler(getView().getTagsLink().addClickHandler(event -> toggleOverlayPresenter(tagsPresenter)));

        getView().getAlertActionWidget().addAction(constants.dismissAlert(),
                this.alertModelProvider.getModel().getDismissCommand(), (command, log) -> {
                AlertListModel model = alertModelProvider.getModel();
                if (log != null) {
                    model.setSelectedItem(log);
                    model.executeCommand(command);
                }
        });
        getView().getAlertActionWidget().addAllAction(constants.clearAllDismissedAlerts(),
                this.alertModelProvider.getModel().getClearAllCommand(), (command, log) -> {
                alertModelProvider.getModel().executeCommand(command);
        });

        getView().getEventActionWidget().addAction(constants.dismissEvent(),
                this.eventModelProvider.getModel().getDismissCommand(), (command, log) -> {
                EventListModel<?> model = eventModelProvider.getModel();
                if (log != null) {
                    model.setSelectedItem(log);
                    model.executeCommand(command);
                }
        });
        getView().getEventActionWidget().addAllAction(constants.clearAllDismissedEvents(),
                this.eventModelProvider.getModel().getClearAllCommand(), (command, log) -> {
                eventModelProvider.getModel().executeCommand(command);
        });

        taskModelProvider.getModel().getItemsChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                Collection<Job> jobs = taskModelProvider.getModel().getItems();
                long count = jobs.stream().filter(job -> JobExecutionStatus.STARTED.equals(job.getStatus())).count();
                // I know I will never have more than 100 items, so I can cast to int.
                getView().setRunningTaskCount((int)count);
            }

        });

        alertModelProvider.getModel().getItemsChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                Collection<AuditLog> alerts = alertModelProvider.getModel().getItems();
                long count = alerts.stream().filter(alert -> AuditLogSeverity.ALERT.equals(alert.getSeverity())).count();
                // I know I will never have more than 100 items, so I can cast to int.
                getView().setAlertCount((int) count);
            }

        });
    }

    private void toggleOverlayPresenter(AbstractOverlayPresenterWidget<? extends AbstractOverlayPresenterWidget.ViewDef> presenterWidget) {
        if (presenterWidget.isVisible()) {
            RevealOverlayContentEvent.fire(this, new RevealOverlayContentEvent(null));
        } else {
            RevealOverlayContentEvent.fire(this, presenterWidget);
        }
    }
}
