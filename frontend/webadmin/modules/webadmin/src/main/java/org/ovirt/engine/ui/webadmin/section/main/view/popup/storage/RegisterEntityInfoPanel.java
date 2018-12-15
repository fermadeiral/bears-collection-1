package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.GuestContainer;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.label.NoItemsLabel;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractDiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractFullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractRxTxRateColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractSumUpColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.DiskImageStatusColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.RegisterEntityModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportEntityData;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.table.cell.CustomSelectionCell;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.view.client.NoSelectionModel;

public abstract class RegisterEntityInfoPanel<T, D extends ImportEntityData<T>, M extends RegisterEntityModel<T, D>>
        extends TabLayoutPanel implements RequiresResize, SelectionHandler<Integer> {

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    protected EntityModelCellTable<ListModel> disksTable;
    protected EntityModelCellTable<ListModel> nicsTable;
    protected EntityModelCellTable<ListModel> appsTable;
    protected EntityModelCellTable<ListModel> containersTable;

    protected M registerEntityModel;

    public RegisterEntityInfoPanel(M registerEntityModel) {
        super(ApplicationTemplates.TAB_BAR_HEIGHT, Style.Unit.PX);
        this.registerEntityModel = registerEntityModel;

        init();
        addStyles();
        addSelectionHandler(this);
    }

    protected abstract void init();

    public abstract void updateTabsData(ImportEntityData<T> importEntityData);

    private void addStyles() {
        getElement().getStyle().setPosition(Style.Position.STATIC);
    }

    @Override
    public void onResize() {
        setHeight(getParent().getOffsetHeight() + Unit.PX.getType());
    }

    @Override
    public void onSelection(SelectionEvent<Integer> event) {
        setHeight(getParent().getOffsetHeight() + Unit.PX.getType());
    }

    @Override
    public void setHeight(String height) {
        super.setHeight(height);
        // 14 for the padding and border of the layout panel defined in .gwt-TabLayoutPanel css class.
        int heightInt = Integer.parseInt(height.substring(0, height.length() - 2)) - 14;
        Scheduler.get().scheduleDeferred(() -> {
            if (disksTable != null) {
                disksTable.setHeight(heightInt - disksTable.getGridHeaderHeight() + Unit.PX.getType());
            }
            if (nicsTable != null) {
                nicsTable.setHeight(heightInt - nicsTable.getGridHeaderHeight() + Unit.PX.getType());
            }
            if (appsTable != null) {
                appsTable.setHeight(heightInt - appsTable.getGridHeaderHeight() + Unit.PX.getType());
            }
            if (containersTable != null) {
                containersTable.setHeight(heightInt - containersTable.getGridHeaderHeight() + Unit.PX.getType());
            }
        });
    }

    protected void initDisksTable() {
        disksTable = new EntityModelCellTable<>(false, true);
        disksTable.enableColumnResizing();

        disksTable.addColumn(new DiskImageStatusColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumn<DiskImage> aliasColumn = new AbstractTextColumn<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getDiskAlias();
            }
        };
        disksTable.addColumn(aliasColumn, constants.aliasDisk(), "80px"); //$NON-NLS-1$

        AbstractDiskSizeColumn<DiskImage> sizeColumn = new AbstractDiskSizeColumn<DiskImage>() {
            @Override
            protected Long getRawValue(DiskImage object) {
                return object.getSize();
            }
        };
        disksTable.addColumn(sizeColumn, constants.provisionedSizeDisk(), "80px"); //$NON-NLS-1$

        AbstractDiskSizeColumn<DiskImage> actualSizeColumn = new AbstractDiskSizeColumn<DiskImage>() {
            @Override
            protected Long getRawValue(DiskImage object) {
                return object.getActualSizeInBytes();
            }
        };
        disksTable.addColumn(actualSizeColumn, constants.sizeDisk(), "80px"); //$NON-NLS-1$

        AbstractTextColumn<DiskImage> allocationColumn = new AbstractEnumColumn<DiskImage, VolumeType>() {
            @Override
            protected VolumeType getRawValue(DiskImage object) {
                return VolumeType.forValue(object.getVolumeType().getValue());
            }
        };
        disksTable.addColumn(allocationColumn, constants.allocationDisk(), "110px"); //$NON-NLS-1$

        AbstractTextColumn<DiskImage> statusColumn = new AbstractEnumColumn<DiskImage, ImageStatus>() {
            @Override
            protected ImageStatus getRawValue(DiskImage object) {
                return object.getImageStatus();
            }
        };
        disksTable.addColumn(statusColumn, constants.statusDisk(), "65px"); //$NON-NLS-1$

        AbstractTextColumn<DiskImage> dateCreatedColumn = new AbstractFullDateTimeColumn<DiskImage>() {
            @Override
            protected Date getRawValue(DiskImage object) {
                return object.getCreationDate();
            }
        };
        disksTable.addColumn(dateCreatedColumn, constants.creationDateDisk(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<DiskImage> descriptionColumn = new AbstractTextColumn<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getDiskDescription();
            }
        };
        disksTable.addColumn(descriptionColumn, constants.descriptionDisk(), "100px"); //$NON-NLS-1$

        if (registerEntityModel.isQuotaEnabled()) {
            disksTable.addColumn(getDiskQuotaColumn(), constants.quotaVm(), "100px"); //$NON-NLS-1$
        }

        disksTable.setRowData(new ArrayList<EntityModel>());
        disksTable.setWidth("100%"); // $NON-NLS-1$
        disksTable.setSelectionModel(new NoSelectionModel());
        disksTable.setEmptyTableWidget(new NoItemsLabel());
    }

    private Column<DiskImage, String> getDiskQuotaColumn() {
        CustomSelectionCell customSelectionCell = new CustomSelectionCell(new ArrayList<String>());
        customSelectionCell.setStyle("input-group col-xs-11"); //$NON-NLS-1$

        AbstractColumn<DiskImage, String> column = new AbstractColumn<DiskImage, String>(customSelectionCell) {
            @Override
            public String getValue(DiskImage disk) {
                List<Quota> quotas = (List<Quota>) registerEntityModel.getStorageQuota().getItems();
                if (quotas == null || quotas.isEmpty()) {
                    return constants.empty();
                }

                Map<Guid, Quota> diskQuotaMap = registerEntityModel.getDiskQuotaMap().getEntity();
                if (diskQuotaMap.get(disk.getId()) == null) {
                    diskQuotaMap.put(disk.getId(), quotas.get(0));
                    ((CustomSelectionCell) getCell()).setOptions(registerEntityModel.getQuotaNames(quotas));
                }

                return diskQuotaMap.get(disk.getId()).getQuotaName();
            }
        };
        column.setFieldUpdater((index, disk, value) -> {
            Quota quota = registerEntityModel.getQuotaByName(value, (List<Quota>) registerEntityModel.getStorageQuota().getItems());
            registerEntityModel.getDiskQuotaMap().getEntity().put(disk.getId(), quota);
        });

        return column;
    }

    protected void initNicsTable() {
        nicsTable = new EntityModelCellTable<>(false, true);
        nicsTable.enableColumnResizing();

        AbstractTextColumn<VmNetworkInterface> nameColumn = new AbstractTextColumn<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getName();
            }
        };
        nicsTable.addColumn(nameColumn, constants.nameInterface(), "90px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> networkNameColumn = new AbstractTextColumn<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getNetworkName();
            }
        };
        nicsTable.addColumn(networkNameColumn, constants.networkNameInterface(), "90px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> profileNameColumn = new AbstractTextColumn<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getVnicProfileName();
            }
        };
        nicsTable.addColumn(profileNameColumn, constants.profileNameInterface(), "90px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> typeColumn = new AbstractEnumColumn<VmNetworkInterface, VmInterfaceType>() {
            @Override
            protected VmInterfaceType getRawValue(VmNetworkInterface object) {
                return VmInterfaceType.forValue(object.getType());
            }
        };
        nicsTable.addColumn(typeColumn, constants.typeInterface(), "90px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> macColumn = new AbstractTextColumn<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getMacAddress();
            }
        };
        nicsTable.addColumn(macColumn, constants.macInterface(), "90px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> speedColumn = new AbstractTextColumn<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                if (object.getSpeed() != null) {
                    return object.getSpeed().toString();
                } else {
                    return null;
                }
            }
        };
        nicsTable.addColumn(speedColumn, templates.sub(constants.speedInterface(), constants.mbps()), "90px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> rxColumn = new AbstractRxTxRateColumn<VmNetworkInterface>() {
            @Override
            protected Double getRate(VmNetworkInterface object) {
                return object.getStatistics().getReceiveRate();
            }

            @Override
            protected Double getSpeed(VmNetworkInterface object) {
                if (object.getSpeed() != null) {
                    return object.getSpeed().doubleValue();
                } else {
                    return null;
                }
            }
        };
        nicsTable.addColumn(rxColumn, templates.sub(constants.rxRate(), constants.mbps()), "90px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> txColumn = new AbstractRxTxRateColumn<VmNetworkInterface>() {
            @Override
            protected Double getRate(VmNetworkInterface object) {
                return object.getStatistics().getTransmitRate();
            }

            @Override
            protected Double getSpeed(VmNetworkInterface object) {
                if (object.getSpeed() != null) {
                    return object.getSpeed().doubleValue();
                } else {
                    return null;
                }
            }
        };
        nicsTable.addColumn(txColumn, templates.sub(constants.txRate(), constants.mbps()), "90px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> dropsColumn = new AbstractSumUpColumn<VmNetworkInterface>() {
            @Override
            protected Double[] getRawValue(VmNetworkInterface object) {
                Double receiveDropRate = object != null ? object.getStatistics().getReceiveDropRate() : null;
                Double transmitDropRate = object != null ? object.getStatistics().getTransmitDropRate() : null;
                return new Double[] { receiveDropRate, transmitDropRate };
            }
        };
        nicsTable.addColumn(dropsColumn, templates.sub(constants.dropsInterface(), constants.pkts()), "90px"); //$NON-NLS-1$

        nicsTable.setRowData(new ArrayList<EntityModel>());
        nicsTable.setWidth("100%"); // $NON-NLS-1$
        nicsTable.setSelectionModel(new NoSelectionModel());
        nicsTable.setEmptyTableWidget(new NoItemsLabel());
    }

    protected void initAppsTable() {
        appsTable = new EntityModelCellTable<>(false, true);

        AbstractTextColumn<String> appNameColumn = new AbstractTextColumn<String>() {
            @Override
            public String getValue(String appName) {
                return appName;
            }
        };
        appsTable.addColumn(appNameColumn, constants.nameSnapshot());

        appsTable.setRowData(new ArrayList<EntityModel>());
        appsTable.setWidth("100%"); // $NON-NLS-1$
        appsTable.setSelectionModel(new NoSelectionModel());
        appsTable.setEmptyTableWidget(new NoItemsLabel());
    }


    protected void initContainersTable() {
        containersTable = new EntityModelCellTable<>(false, true);

        containersTable.addColumn(new AbstractTextColumn<GuestContainer>() {
            @Override
            public String getValue(GuestContainer row) {
                return row.getId().toString();
            }
        }, constants.idContainer());
        containersTable.addColumn(new AbstractTextColumn<GuestContainer>() {
            @Override
            public String getValue(GuestContainer row) {
                return String.join(", ", row.getNames()); //$NON-NLS-1$
            }
        }, constants.namesContainer());
        containersTable.addColumn(new AbstractTextColumn<GuestContainer>() {
            @Override
            public String getValue(GuestContainer row) {
                return row.getImage();
            }
        }, constants.imageContainer());
        containersTable.addColumn(new AbstractTextColumn<GuestContainer>() {
            @Override
            public String getValue(GuestContainer row) {
                return row.getCommand();
            }
        }, constants.commandContainer());
        containersTable.addColumn(new AbstractTextColumn<GuestContainer>() {
            @Override
            public String getValue(GuestContainer row) {
                return row.getStatus();
            }
        }, constants.statusContainer());

        containersTable.setRowData(new ArrayList<EntityModel>());
        containersTable.setWidth("100%"); // $NON-NLS-1$
        containersTable.setSelectionModel(new NoSelectionModel());
        containersTable.setEmptyTableWidget(new NoItemsLabel());
    }
}
