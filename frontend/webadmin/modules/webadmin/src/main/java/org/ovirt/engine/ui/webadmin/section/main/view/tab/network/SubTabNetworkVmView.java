package org.ovirt.engine.ui.webadmin.section.main.view.tab.network;

import java.util.Arrays;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.ViewRadioGroup;
import org.ovirt.engine.ui.common.widget.table.column.AbstractNullableNumberColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractRxTxRateColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.NicActivateStatusColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkVmFilter;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkVmListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmStatusColumn;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.RadioButton;

public class SubTabNetworkVmView extends AbstractSubTabTableView<NetworkView, PairQueryable<VmNetworkInterface, VM>, NetworkListModel, NetworkVmListModel>
        implements SubTabNetworkVmPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabNetworkVmView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final ViewRadioGroup<NetworkVmFilter> viewRadioGroup;

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabNetworkVmView(SearchableDetailModelProvider<PairQueryable<VmNetworkInterface, VM>, NetworkListModel, NetworkVmListModel> modelProvider) {
        super(modelProvider);
        viewRadioGroup = new ViewRadioGroup<>(Arrays.asList(NetworkVmFilter.values()));
        viewRadioGroup.setSelectedValue(NetworkVmFilter.running);
        viewRadioGroup.addStyleName("stnvmv_radioGroup_pfly_fix"); //$NON-NLS-1$
        initTable();
        initWidget(getTable());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    private void initTableOverhead() {
        viewRadioGroup.addClickHandler(event -> {
            if (((RadioButton) event.getSource()).getValue()) {
                handleRadioButtonClick(event);
            }
        });

        getTable().setTableOverhead(viewRadioGroup);
        getTable().setTableTopMargin(20);
    }

    private void handleRadioButtonClick(ClickEvent event) {
        getDetailModel().setViewFilterType(viewRadioGroup.getSelectedValue());

        boolean running = viewRadioGroup.getSelectedValue() == NetworkVmFilter.running;

        getTable().ensureColumnVisible(vmStatusColumn, constants.empty(), true, "30px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(nameColumn, constants.nameVm(), true, "150px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(clusterColumn, constants.clusterVm(), true, "150px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(ipColumn, constants.ipVm(), true, "150px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(fqdnColumn, constants.fqdn(), true, "150px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(nicActivateStatusColumn, constants.vnicStatusNetworkVM(), true, "100px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(vnicNameColumn, constants.vnicNetworkVM(), true, "100px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(rxColumn,
                templates.sub(constants.rxNetworkVM(), constants.mbps()).asString(),
                running, "100px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(txColumn,
                templates.sub(constants.txNetworkVM(), constants.mbps()).asString(),
                running, "100px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(totalRxColumn,
                templates.sub(constants.rxTotal(), constants.bytes()).asString(),
                running,
                "150px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(totalTxColumn,
                templates.sub(constants.txTotal(), constants.bytes()).asString(),
                running,
                "150px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(descriptionColumn, constants.descriptionVm(), true, "150px"); //$NON-NLS-1$
    }

    private final VmStatusColumn<PairQueryable<VmNetworkInterface, VM>> vmStatusColumn =
            new VmStatusColumn<PairQueryable<VmNetworkInterface, VM>>() {
                {
                    setContextMenuTitle(constants.statusVm());
                }
            };
    private final AbstractTextColumn<PairQueryable<VmNetworkInterface, VM>> nameColumn =
            new AbstractTextColumn<PairQueryable<VmNetworkInterface, VM>>() {
                @Override
                public String getValue(PairQueryable<VmNetworkInterface, VM> object) {
                    return object.getSecond().getName();
                }
            };
    private final AbstractTextColumn<PairQueryable<VmNetworkInterface, VM>> clusterColumn =
            new AbstractTextColumn<PairQueryable<VmNetworkInterface, VM>>() {
                @Override
                public String getValue(PairQueryable<VmNetworkInterface, VM> object) {
                    return object.getSecond().getClusterName();
                }
            };
    private final AbstractTextColumn<PairQueryable<VmNetworkInterface, VM>> ipColumn =
            new AbstractTextColumn<PairQueryable<VmNetworkInterface, VM>>() {
                @Override
                public String getValue(PairQueryable<VmNetworkInterface, VM> object) {
                    return object.getSecond().getIp();
                }
            };

    private final AbstractTextColumn<PairQueryable<VmNetworkInterface, VM>> fqdnColumn =
            new AbstractTextColumn<PairQueryable<VmNetworkInterface, VM>>() {
                @Override
                public String getValue(PairQueryable<VmNetworkInterface, VM> object) {
                    return object.getSecond().getFqdn();
                }

                @Override
                public SafeHtml getTooltip(
                        PairQueryable<VmNetworkInterface, VM> object) {
                    String tooltipContent = object.getSecond().getFqdn();
                    return tooltipContent == null ?
                            SafeHtmlUtils.fromString("") : SafeHtmlUtils.fromString(tooltipContent);
                }
            };

    private final NicActivateStatusColumn<PairQueryable<VmNetworkInterface, VM>> nicActivateStatusColumn =
            new NicActivateStatusColumn<>();

    private final AbstractTextColumn<PairQueryable<VmNetworkInterface, VM>> vnicNameColumn =
            new AbstractTextColumn<PairQueryable<VmNetworkInterface, VM>>() {
                @Override
                public String getValue(PairQueryable<VmNetworkInterface, VM> object) {
                    return object.getFirst().getName();
                }
            };

    private final AbstractTextColumn<PairQueryable<VmNetworkInterface, VM>> rxColumn =
            new AbstractRxTxRateColumn<PairQueryable<VmNetworkInterface, VM>>() {
                @Override
                protected Double getRate(PairQueryable<VmNetworkInterface, VM> object) {
                    return object.getFirst().getStatistics().getReceiveRate();
                }

                @Override
                protected Double getSpeed(PairQueryable<VmNetworkInterface, VM> object) {
                    if (object.getFirst().getSpeed() != null) {
                        return object.getFirst().getSpeed().doubleValue();
                    } else {
                        return null;
                    }
                }
            };

    private final AbstractTextColumn<PairQueryable<VmNetworkInterface, VM>> txColumn =
            new AbstractRxTxRateColumn<PairQueryable<VmNetworkInterface, VM>>() {
                @Override
                protected Double getRate(PairQueryable<VmNetworkInterface, VM> object) {
                    return object.getFirst().getStatistics().getTransmitRate();
                }

                @Override
                protected Double getSpeed(PairQueryable<VmNetworkInterface, VM> object) {
                    if (object.getFirst().getSpeed() != null) {
                        return object.getFirst().getSpeed().doubleValue();
                    } else {
                        return null;
                    }
                }
            };

    private final AbstractNullableNumberColumn<PairQueryable<VmNetworkInterface, VM>> totalRxColumn =
            new AbstractNullableNumberColumn<PairQueryable<VmNetworkInterface, VM>>() {
        @Override
        protected Number getRawValue(PairQueryable<VmNetworkInterface, VM> object) {
            return object.getFirst() == null ? null : object.getFirst().getStatistics().getReceivedBytes();
        }
    };

    private final AbstractNullableNumberColumn<PairQueryable<VmNetworkInterface, VM>> totalTxColumn =
            new AbstractNullableNumberColumn<PairQueryable<VmNetworkInterface, VM>>() {
        @Override
        protected Number getRawValue(PairQueryable<VmNetworkInterface, VM> object) {
            return object.getFirst() == null ? null : object.getFirst().getStatistics().getTransmittedBytes();
        }
    };

    private final AbstractTextColumn<PairQueryable<VmNetworkInterface, VM>> descriptionColumn =
            new AbstractTextColumn<PairQueryable<VmNetworkInterface, VM>>() {
                @Override
                public String getValue(PairQueryable<VmNetworkInterface, VM> object) {
                    return object.getSecond().getDescription();
                }
            };

    private void initTable() {
        getTable().enableColumnResizing();
        initTableOverhead();
        handleRadioButtonClick(null);
        initSorting();

        getTable().addActionButton(new WebAdminButtonDefinition<PairQueryable<VmNetworkInterface, VM>>(constants.removeInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });

    }

    private void initSorting() {
        vmStatusColumn.makeSortable();
        nameColumn.makeSortable();
        clusterColumn.makeSortable();
        ipColumn.makeSortable();
        fqdnColumn.makeSortable();
        nicActivateStatusColumn.makeSortable();
        vnicNameColumn.makeSortable();
        rxColumn.makeSortable();
        txColumn.makeSortable();
        totalRxColumn.makeSortable();
        totalTxColumn.makeSortable();
        descriptionColumn.makeSortable();
    }

}

