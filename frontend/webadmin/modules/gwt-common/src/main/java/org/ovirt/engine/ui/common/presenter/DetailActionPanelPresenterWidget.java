package org.ovirt.engine.ui.common.presenter;

import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;

import com.google.web.bindery.event.shared.EventBus;

public abstract class DetailActionPanelPresenterWidget<T, M extends ListWithDetailsModel, D extends HasEntity>
    extends ActionPanelPresenterWidget<T, M> {

    public DetailActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<T> view,
            SearchableDetailModelProvider<T, ?, ?> dataProvider) {
        super(eventBus, view, (SearchableTabModelProvider<T, M>) dataProvider);
    }

    protected D getDetailModel() {
        return (D) ((SearchableDetailModelProvider<T, ?, ?>)getDataProvider()).getModel();
    }

}
