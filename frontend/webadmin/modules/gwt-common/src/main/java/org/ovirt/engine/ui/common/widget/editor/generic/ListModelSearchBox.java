package org.ovirt.engine.ui.common.widget.editor.generic;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.TextBox;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent.Handler;
import com.google.gwt.view.client.SelectionModel;

public class ListModelSearchBox<T, M extends SearchableListModel<?, T>> extends Composite implements HasData<T>,
    ClickHandler {

    interface WidgetUiBinder extends UiBinder<Widget, ListModelSearchBox<?, ?>> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    TextBox searchBox;

    @UiField
    Button searchButton;

    @UiField
    DropDownMenu menu;

    private List<HandlerRegistration> menuHandlers = new ArrayList<>();

    private List<ListModelSelectedCallback<T>> callbacks = new ArrayList<>();

    private int rowCount;

    private Range range = new Range(0, 10);

    private String currentSearch;

    private final MainModelProvider<T, M> listModelProvider;
    private int currentFocusIndex = -1;

    public ListModelSearchBox(MainModelProvider<T, M> listModelProvider) {
        this.listModelProvider = listModelProvider;
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        configureDropDown();
        listModelProvider.addDataDisplay(this);
    }

    private void configureDropDown() {
        searchBox.addKeyPressHandler(event -> {
            if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_TAB) {
                stopPropagation(event);
            }
        });
        searchBox.addKeyUpHandler(event -> {
            if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                stopPropagation(event);
                startSearch(searchBox.getText());
            } else if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_DOWN) {
                stopPropagation(event);
                currentFocusIndex = 0;
                getAnchorListItem(currentFocusIndex).setFocus(true);
            } else if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_UP) {
                stopPropagation(event);
                currentFocusIndex = menu.getWidgetCount() - 1;
                getAnchorListItem(currentFocusIndex).setFocus(true);
            } else if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_TAB) {
                stopPropagation(event);
                currentFocusIndex = 0;
                if (event.getNativeEvent().getShiftKey()) {
                    currentFocusIndex = menu.getWidgetCount() - 1;
                }
                getAnchorListItem(currentFocusIndex).setFocus(true);
            } else if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
                for (ListModelSelectedCallback<T> callback: this.callbacks) {
                    callback.modelSelected(null);
                }
            }
        });
        searchBox.addFocusHandler(event -> currentFocusIndex = -1);
        searchButton.addClickHandler(event -> {
            startSearch(searchBox.getText());
        });
    }

    private AnchorListItem getAnchorListItem(int index) {
        return (AnchorListItem)menu.getWidget(index);
    }

    private void stopPropagation(DomEvent<?> event) {
        event.preventDefault();
        event.stopPropagation();
    }

    private void startSearch(final String searchText) {
        currentSearch = searchText;
        M model = listModelProvider.getModel();
        if (!StringUtils.isEmpty(searchText)) {
            model.setSearchString(model.getDefaultSearchString()
                    + "name = *" + searchText + "*"); // $NON-NLS-1$ $NON-NLS-2$
        } else {
            model.setSearchString(model.getDefaultSearchString()); // $NON-NLS-1$
        }
        model.executeCommand(model.getSearchCommand());
    }

    @Override
    public void setVisible(boolean value) {
        if (value) {
            Scheduler.get().scheduleDeferred( ()-> {
                searchBox.setFocus(true);
                currentFocusIndex = -1;
            });
        }
    }

    @Override
    public void onClick(ClickEvent event) {
        if (event.getSource() instanceof Anchor) {
            Anchor selectedItem = (Anchor) event.getSource();
            selectItem(selectedItem.getText());
        }
    }

    private void selectItem(String name) {
        M listModel = listModelProvider.getModel();
        for (T model: listModel.getItems()) {
            if(getName(model).asString().equals(name)) {
                for (ListModelSelectedCallback<T> callback: this.callbacks) {
                    callback.modelSelected(model);
                }
            }
        }
    }

    @Override
    public HandlerRegistration addRangeChangeHandler(Handler handler) {
        return null;
    }

    @Override
    public HandlerRegistration addRowCountChangeHandler(
            com.google.gwt.view.client.RowCountChangeEvent.Handler handler) {
        return null;
    }

    @Override
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public Range getVisibleRange() {
        return this.range;
    }

    @Override
    public boolean isRowCountExact() {
        return true;
    }

    @Override
    public void setRowCount(int count) {
        this.rowCount = count;
        range = new Range(0, count);
    }

    @Override
    public void setRowCount(int count, boolean isExact) {
        setRowCount(count);
    }

    @Override
    public void setVisibleRange(int start, int length) {
        range = new Range(start, length);
    }

    @Override
    public void setVisibleRange(Range range) {
        this.range = range;
    }

    @Override
    public HandlerRegistration addCellPreviewHandler(com.google.gwt.view.client.CellPreviewEvent.Handler<T> handler) {
        return null;
    }

    @Override
    public SelectionModel<T> getSelectionModel() {
        return null;
    }

    @Override
    public T getVisibleItem(int indexOnPage) {
        return null;
    }

    @Override
    public int getVisibleItemCount() {
        return 100;
    }

    @Override
    public Iterable<T> getVisibleItems() {
        return null;
    }

    private void emptyMenuHandlers() {
        for (HandlerRegistration reg: menuHandlers) {
            reg.removeHandler();
        }
        menuHandlers.clear();
    }

    protected SafeHtml getName(T item) {
        String result = "";
        if (item instanceof Nameable) {
            result = ((Nameable)item).getName();
        }
        return SafeHtmlUtils.fromString(result);
    }

    @Override
    public void setRowData(int start, List<? extends T> values) {
        final int oldCount = menu.getWidgetCount();
        menu.clear();
        emptyMenuHandlers();
        for (T model: values) {
            if(model instanceof Nameable) {
                final String text = getName(model).asString();
                final AnchorListItem item = new SearchBoxAnchorListItem();
                item.setText(text);
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        if (!StringUtils.isEmpty(currentSearch)) {
                            String newText = text.replaceAll(currentSearch, "<strong>" // $NON-NLS-1$
                                + currentSearch + "</strong>"); // $NON-NLS-1$
                            item.getWidget(0).getElement().setInnerHTML(newText);
                        }
                    }
                });
                menuHandlers.add(item.addClickHandler(this));
                menu.add(item);
            }
        }
        if (oldCount != menu.getWidgetCount()) {
            currentFocusIndex = 0;
        }
        if (currentFocusIndex >= menu.getWidgetCount() - 1) {
            currentFocusIndex = menu.getWidgetCount() - 1;
        }
        if (currentFocusIndex >= 0) {
            ((AnchorListItem)menu.getWidget(currentFocusIndex)).setFocus(true);
        }
    }

    @Override
    public void setSelectionModel(SelectionModel<? super T> selectionModel) {
        // No-op we use the search models own selection model. Just here to satisfy the interface contract.
    }

    @Override
    public void setVisibleRangeAndClearData(Range range, boolean forceRangeChangeEvent) {
        this.range = range;
    }

    public void addModelSelectedCallback(ListModelSelectedCallback<T> callback) {
        this.callbacks.add(callback);
    }

    public void onKeyUp(int keycode, boolean shiftKey) {
        if (keycode == KeyCodes.KEY_UP || (keycode == KeyCodes.KEY_TAB && shiftKey)) {
            decrementCurrentIndex();
        } else if (keycode == KeyCodes.KEY_DOWN || (keycode == KeyCodes.KEY_TAB && !shiftKey)) {
            incrementCurrentIndex();
        } else if (keycode == KeyCodes.KEY_ENTER) {
            selectItem(((AnchorListItem)menu.getWidget(currentFocusIndex)).getText());
        } else if (keycode == KeyCodes.KEY_ESCAPE) {
            for (ListModelSelectedCallback<T> callback: this.callbacks) {
                callback.modelSelected(null);
            }
        }
    }

    private void incrementCurrentIndex() {
        currentFocusIndex += 1;
        if (currentFocusIndex == menu.getWidgetCount()) {
            searchBox.setFocus(true);
            currentFocusIndex = -1;
        } else {
            ((AnchorListItem)menu.getWidget(currentFocusIndex)).setFocus(true);
        }
    }

    private void decrementCurrentIndex() {
        currentFocusIndex -= 1;
        if (currentFocusIndex == -1) {
            searchBox.setFocus(true);
        } else {
            ((AnchorListItem)menu.getWidget(currentFocusIndex)).setFocus(true);
        }
    }

    private class SearchBoxAnchorListItem extends AnchorListItem {

        SearchBoxAnchorListItem() {
            sinkEvents(Event.ONKEYUP);
        }

        @Override
        public void onBrowserEvent(Event event) {
            super.onBrowserEvent(event);
            if (Event.ONKEYUP == DOM.eventGetType(event)) {
                onKeyUp(event.getKeyCode(), event.getShiftKey());
                event.preventDefault();
                event.stopPropagation();
            } else if (Event.ONKEYPRESS == DOM.eventGetType(event)) {
                if (event.getKeyCode() == KeyCodes.KEY_TAB) {
                    event.preventDefault();
                    event.stopPropagation();
                }
            }
        }
    }
}
