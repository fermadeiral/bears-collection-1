package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.action.CommandLocation;
import org.ovirt.engine.ui.common.widget.action.ImageUiCommandButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDiskListModel;
import com.google.gwt.event.logical.shared.InitializeEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class VmDiskListModelTable extends BaseVmDiskListModelTable<VmDiskListModel> {

    private final boolean showMoveButton;

    private ImageUiCommandButtonDefinition<Disk> plugButtonDefinition;
    private ImageUiCommandButtonDefinition<Disk> unPlugButtonDefinition;

    private static final CommonApplicationResources resources = AssetProvider.getResources();
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public VmDiskListModelTable(
            SearchableTableModelProvider<Disk, VmDiskListModel> modelProvider,
            EventBus eventBus, ClientStorage clientStorage,
            boolean showMoveButton) {
        super(modelProvider, eventBus, clientStorage);
        this.showMoveButton = showMoveButton;
    }

    @Override
    public void initTable() {
        super.initTable();

        getTable().addActionButton(new UiCommandButtonDefinition<Disk>(getEventBus(), constants.newDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewCommand();
            }
        });

        getTable().addActionButton(new UiCommandButtonDefinition<Disk>(getEventBus(), constants.attachDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getAttachCommand();
            }
        });

        getTable().addActionButton(new UiCommandButtonDefinition<Disk>(getEventBus(), constants.editDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });

        getTable().addActionButton(new UiCommandButtonDefinition<Disk>(getEventBus(), constants.removeDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });

        getTable().addActionButton(new UiCommandButtonDefinition<Disk>(getEventBus(), constants.sparsifyDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getSparsifyCommand();
            }
        });

        plugButtonDefinition = new ImageUiCommandButtonDefinition<Disk>(getEventBus(), constants.activateDisk(),
                resources.upImage(), resources.upDisabledImage(), true, false) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getPlugCommand();
            }

            @Override
            public SafeHtml getTooltip() {
                SafeHtml tooltip = null;
                if (getModel().isHotPlugAvailable() && !getModel().isPlugAvailableByDisks(true)) {
                    tooltip = SafeHtmlUtils.fromString(constants.diskHotPlugNotSupported());
                }
                return tooltip;
            }
        };
        getTable().addActionButton(plugButtonDefinition);

        unPlugButtonDefinition = new ImageUiCommandButtonDefinition<Disk>(getEventBus(), constants.deactivateDisk(),
                resources.downImage(), resources.downDisabledImage(), true, false) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getUnPlugCommand();
            }

            @Override
            public SafeHtml getTooltip() {
                SafeHtml tooltip = null;
                if (getModel().isHotPlugAvailable() && !getModel().isPlugAvailableByDisks(false)) {
                    tooltip = SafeHtmlUtils.fromString(constants.diskHotPlugNotSupported());
                }
                return tooltip;
            }
        };
        getTable().addActionButton(unPlugButtonDefinition);

        attachActivationListenersForModel();

        if (showMoveButton) {
            getTable().addActionButton(new UiCommandButtonDefinition<Disk>(getEventBus(), constants.moveDisk()) {
                @Override
                protected UICommand resolveCommand() {
                    return getModel().getMoveCommand();
                }
            });
        }

        getTable().addActionButton(new UiCommandButtonDefinition<Disk>(getEventBus(), constants.getDiskAlignment(),
                CommandLocation.OnlyFromContext) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getScanAlignmentCommand();
            }
        });

        getTable().addActionButton(new UiCommandButtonDefinition<Disk>(getEventBus(), constants.assignQuota()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getChangeQuotaCommand();
            }
        });
    }

    protected void attachActivationListenersForModel() {
        getModel().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if ("IsDiskHotPlugAvailable".equals(args.propertyName)) { //$NON-NLS-1$
                InitializeEvent.fire(plugButtonDefinition);
                InitializeEvent.fire(unPlugButtonDefinition);
            }
        });

        getModel().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            InitializeEvent.fire(plugButtonDefinition);
            InitializeEvent.fire(unPlugButtonDefinition);
        });
    }

}
