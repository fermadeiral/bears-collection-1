package org.ovirt.engine.ui.common.widget.uicommon.vm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.html.Span;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.css.PatternflyConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.PatternflyIconType;
import org.ovirt.engine.ui.common.widget.listgroup.ExpandableListViewItem;
import org.ovirt.engine.ui.common.widget.listgroup.PatternflyListViewItem;
import org.ovirt.engine.ui.common.widget.renderer.FullDateTimeRenderer;
import org.ovirt.engine.ui.common.widget.renderer.RxTxRateRenderer;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.vms.SnapshotModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

import com.google.gwt.dom.client.DListElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;

public class VmSnapshotListViewItem extends PatternflyListViewItem<Snapshot> {

    private static final String DL_HORIZONTAL = "dl-horizontal"; // $NON-NLS-1$

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();
    private static final FullDateTimeRenderer dateRenderer = new FullDateTimeRenderer();

    private static final FullDateTimeRenderer fullDateTimeRenderer = new FullDateTimeRenderer();

    private ExpandableListViewItem generalExpand;
    private ExpandableListViewItem disksExpand;
    private ExpandableListViewItem nicsExpand;
    private ExpandableListViewItem installedAppsExpand;

    public VmSnapshotListViewItem(String name, Snapshot snapshot, VmSnapshotListModel listModel,
            SnapshotModel snapshotModel) {
        super(name, snapshot);
        Container generalInfoContainer = createGeneralItemContainerPanel(snapshot, listModel);
        generalExpand.setDetails(generalInfoContainer);
        add(generalInfoContainer);
        updateValues(snapshotModel);
    }

    private Container createInstalledAppsItemContainerPanel(List<String> appList) {
        Row row = new Row();
        Column column = new Column(ColumnSize.MD_12);
        row.add(column);
        Container container = createItemContainerPanel(row);
        for (String appName: appList) {
            column.getElement().setInnerHTML(appName);
            row = new Row();
            column = new Column(ColumnSize.MD_12);
            row.add(column);
            container.add(row);
        }
        if (appList.isEmpty()) {
            column.getElement().setInnerHTML(constants.noItemsToDisplay());
        }
        return container;
    }

    private Container createNicsItemContainerPanel(List<VmNetworkInterface> nics) {
        RxTxRateRenderer rateRenderer = new RxTxRateRenderer();
        Row content = new Row();
        Container container = createItemContainerPanel(content);
        int i = 0;
        for (VmNetworkInterface nic: nics) {
            if (i % 4 == 0 && i > 0) {
                content = new Row();
                container.add(content);
            }
            Column column = new Column(ColumnSize.MD_3);
            content.add(column);
            DListElement dl = Document.get().createDLElement();
            dl.addClassName(DL_HORIZONTAL);
            addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.nameInterface()), nic.getName(), dl);
            addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.networkNameInterface()),
                    nic.getNetworkName(), dl);
            addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.profileNameInterface()),
                    nic.getVnicProfileName(), dl);
            addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.typeInterface()),
                    String.valueOf(VmInterfaceType.forValue(nic.getType())), dl);
            addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.macInterface()), nic.getMacAddress(), dl);
            addDetailItem(templates.sub(constants.rxRate(), constants.mbps()),
                    rateRenderer.render(new Double[] { nic.getStatistics().getReceiveRate(),
                            nic.getSpeed().doubleValue() }), dl);
            addDetailItem(templates.sub(constants.txRate(), constants.mbps()),
                    rateRenderer.render(new Double[] { nic.getStatistics().getTransmitRate(),
                            nic.getSpeed().doubleValue() }), dl);
            addDetailItem(templates.sub(constants.speedInterface(), constants.mbps()),
                    nic.getStatistics().getTransmittedBytes() != null ?
                    String.valueOf(nic.getStatistics().getTransmittedBytes()) :
                        constants.notAvailableLabel(), dl);
            addDetailItem(templates.sub(constants.dropsInterface(), constants.pkts()),
                    String.valueOf(nic.getStatistics().getReceiveDropRate() != null ? nic.getStatistics().getReceiveDropRate() : ""
                            + nic.getStatistics().getTransmitDropRate()), dl);
            column.getElement().appendChild(dl);
            i++;
        }
        if (nics.isEmpty()) {
            Column column = new Column(ColumnSize.MD_12);
            content.add(column);
            column.getElement().setInnerHTML(constants.noItemsToDisplay());
        }
        return container;
    }

    private Container createDisksItemContainerPanel(List<DiskImage> diskImages) {
        Row content = new Row();
        Column column = new Column(ColumnSize.MD_12);
        content.add(column);

        for (DiskImage image: diskImages) {
            DListElement dl = Document.get().createDLElement();
            dl.addClassName(DL_HORIZONTAL);
            addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.statusDisk()),
                    getImageStatus(image.getImageStatus()), dl);
            addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.aliasDisk()),
                    image.getDiskAlias(), dl);
            addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.provisionedSizeDisk()),
                    String.valueOf(image.getSize()), dl);
            addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.sizeDisk()),
                    String.valueOf(image.getActualSizeInBytes()), dl);
            addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.allocationDisk()),
                    String.valueOf(VolumeType.forValue(image.getVolumeType().getValue())), dl);
            addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.interfaceDisk()), getInterface(image), dl);
            addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.creationDateDisk()),
                    dateRenderer.render(image.getCreationDate()), dl);
            addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.diskSnapshotIDDisk()),
                    String.valueOf(image.getImageId()), dl);
            addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.typeDisk()),
                    String.valueOf(image.getDiskStorageType()), dl);
            addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.descriptionDisk()),
                    StringUtils.isNotEmpty(image.getDiskDescription()) ? image.getDiskDescription()
                            : constants.notAvailableLabel(), dl);
            column.getElement().appendChild(dl);
        }
        if (diskImages.isEmpty()) {
            column.getElement().setInnerHTML(constants.noItemsToDisplay());
        }
        return createItemContainerPanel(content);
    }

    private String getInterface(DiskImage image) {
        if (image.getDiskVmElements().size() == 1) {
            return image.getDiskVmElements().iterator().next().getDiskInterface().toString();
        }
        return constants.notAvailableLabel();
    }

    private String getImageStatus(ImageStatus status) {
        switch (status) {
        case OK:
            return constants.up();
        case LOCKED:
            return constants.imageLocked();
        case ILLEGAL:
            return constants.illegalStatus();
        default:
            return constants.notAvailableLabel();
        }
    }

    private Container createGeneralItemContainerPanel(Snapshot snapshot, VmSnapshotListModel listModel) {
        Row content = new Row();
        Column column = new Column(ColumnSize.MD_12);
        content.add(column);

        DListElement dl = Document.get().createDLElement();
        dl.addClassName(DL_HORIZONTAL);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.dateSnapshot()), getCreateDateString(snapshot), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.statusSnapshot()), snapshot.getStatus().name(), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.memorySnapshot()),
                String.valueOf(listModel.isMemorySnapshotSupported()), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.descriptionSnapshot()),
                getDescription(snapshot), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.definedMemoryVm()),
                listModel.getEntity().getVmMemSizeMb() + constants.mb(), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.physMemGauranteedVm()),
                listModel.getEntity().getMinAllocatedMem() + constants.mb(), dl);
        addDetailItem(SafeHtmlUtils.fromSafeConstant(constants.numOfCpuCoresVm()),
                messages.cpuInfoLabel(listModel.getEntity().getNumOfCpus(),
                        listModel.getEntity().getNumOfSockets(), listModel.getEntity().getCpuPerSocket(),
                        listModel.getEntity().getThreadsPerCpu()), dl);
        column.getElement().appendChild(dl);
        return createItemContainerPanel(content);
    }

    private String getDescription(Snapshot snapshot) {
        String description = SafeHtmlUtils.fromString(snapshot.getDescription()).asString();

        if (snapshot.getStatus() == SnapshotStatus.IN_PREVIEW) {
            List<String> previewedItems = new ArrayList<>(Arrays.asList(constants.vmConfiguration()));
            previewedItems.addAll(Linq.getDiskAliases(snapshot.getDiskImages()));
            description = messages.snapshotPreviewing(
                    description, StringUtils.join(previewedItems, ", ")); //$NON-NLS-1$
        }
        else if (snapshot.getType() == SnapshotType.STATELESS) {
            description = description + " (" + constants.readonlyLabel() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        else if (snapshot.getType() == SnapshotType.PREVIEW) {
            description = constants.snapshotDescriptionActiveVmBeforePreview();
        }
        else if (snapshot.getType() == SnapshotType.ACTIVE) {
            description = constants.snapshotDescriptionActiveVm();
        }
        else if (snapshot.getType() == SnapshotType.REGULAR && !snapshot.getDiskImages().isEmpty()) {
            description = messages.snapshotPreviewing(
                    description, StringUtils.join(Linq.getDiskAliases(snapshot.getDiskImages()), ", ")); //$NON-NLS-1$
        }
        else if (snapshot.isVmConfigurationBroken()) {
            description = description + " (" + constants.brokenVmConfiguration() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        return description;
    }

    private String getCreateDateString(Snapshot snapshot) {
        if (snapshot.getType() == SnapshotType.ACTIVE) {
            return constants.currentSnapshotLabel();
        }
        return fullDateTimeRenderer.render(snapshot.getCreationDate());
    }

    @Override
    public void restoreStateFromViewItem(PatternflyListViewItem<Snapshot> originalViewItem) {
        VmSnapshotListViewItem original = (VmSnapshotListViewItem) originalViewItem;
        setGeneralExpanded(original.getGeneralState());
        setDisksExpanded(original.getDisksState());
        setNicsExpanded(original.getNicsState());
        setInstalledAppsExpanded(original.getInstalledAppsState());
    }

    @Override
    protected IsWidget createIconPanel() {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName(PatternflyConstants.PF_LIST_VIEW_LEFT);
        Span iconSpan = new Span();
        iconSpan.addStyleName(Styles.FONT_AWESOME_BASE);
        iconSpan.addStyleName(IconType.CAMERA.getCssName());
        iconSpan.addStyleName(PatternflyConstants.PF_LIST_VIEW_ICON_SM);
        panel.add(iconSpan);
        return panel;
    }

    @Override
    protected IsWidget createBodyPanel(String header, Snapshot entity) {
        FlowPanel bodyPanel = new FlowPanel();
        bodyPanel.addStyleName(PatternflyConstants.PF_LIST_VIEW_BODY);
        FlowPanel descriptionPanel = new FlowPanel();
        descriptionPanel.addStyleName(PatternflyConstants.PF_LIST_VIEW_DESCRIPTION);
        FlowPanel headerPanel = new FlowPanel();
        headerPanel.getElement().setInnerHTML(header);
        headerPanel.addStyleName(Styles.LIST_GROUP_ITEM_HEADING);
        descriptionPanel.add(headerPanel);
        FlowPanel statusPanel = new FlowPanel();
        statusPanel.addStyleName(Styles.LIST_GROUP_ITEM_TEXT);
        descriptionPanel.add(statusPanel);
        bodyPanel.add(descriptionPanel);
        bodyPanel.add(createAdditionalInfoPanel());
        return bodyPanel;
    }

    private IsWidget createAdditionalInfoPanel() {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName(PatternflyConstants.PF_LIST_VIEW_ADDITIONAL_INFO);
        panel.add(createGeneralAdditionalInfo());
        panel.add(createDisksAdditionalInfo());
        panel.add(createNicsAdditionalInfo());
        panel.add(createAppsAdditionalInfo());
        return panel;
    }

    private IsWidget createGeneralAdditionalInfo() {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName(PatternflyConstants.PF_LIST_VIEW_ADDITIONAL_INFO_ITEM);
        generalExpand = new ExpandableListViewItem(constants.generalLabel(), IconType.EYE.getCssName());
        getClickHandlerRegistrations().add(generalExpand.addClickHandler(this));
        panel.add(generalExpand);
        return panel;
    }

    private IsWidget createDisksAdditionalInfo() {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName(PatternflyConstants.PF_LIST_VIEW_ADDITIONAL_INFO_ITEM);
        disksExpand = new ExpandableListViewItem(constants.disksLabel(), IconType.DATABASE.getCssName());
        getClickHandlerRegistrations().add(disksExpand.addClickHandler(this));
        panel.add(disksExpand);
        return panel;
    }

    private IsWidget createNicsAdditionalInfo() {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName(PatternflyConstants.PF_LIST_VIEW_ADDITIONAL_INFO_ITEM);
        nicsExpand = new ExpandableListViewItem(constants.nicsLabel(), PatternflyIconType.PF_NETWORK.getCssName());
        getClickHandlerRegistrations().add(nicsExpand.addClickHandler(this));
        panel.add(nicsExpand);
        return panel;
    }

    private IsWidget createAppsAdditionalInfo() {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName(PatternflyConstants.PF_LIST_VIEW_ADDITIONAL_INFO_ITEM);
        installedAppsExpand = new ExpandableListViewItem(constants.applicationsLabel(), IconType.NEWSPAPER_O.getCssName());
        getClickHandlerRegistrations().add(installedAppsExpand.addClickHandler(this));
        panel.add(installedAppsExpand);
        return panel;
    }

    @Override
    protected void hideAllDetails() {
        generalExpand.toggleExpanded(false);
        disksExpand.toggleExpanded(false);
        nicsExpand.toggleExpanded(false);
        installedAppsExpand.toggleExpanded(false);
    }

    @Override
    protected void toggleExpanded() {
        if (!generalExpand.isActive() && !disksExpand.isActive() && !nicsExpand.isActive()
                && !installedAppsExpand.isActive()) {
            removeStyleName(PatternflyConstants.PF_LIST_VIEW_EXPAND_ACTIVE);
        } else {
            addStyleName(PatternflyConstants.PF_LIST_VIEW_EXPAND_ACTIVE);
        }
    }

    public boolean getGeneralState() {
        return generalExpand.isActive();
    }

    public void setGeneralExpanded(boolean value) {
        generalExpand.toggleExpanded(value);
        toggleExpanded();
    }

    public boolean getDisksState() {
        return disksExpand.isActive();
    }

    public void setDisksExpanded(boolean value) {
        disksExpand.toggleExpanded(value);
        toggleExpanded();
    }

    public boolean getNicsState() {
        return nicsExpand.isActive();
    }

    public void setNicsExpanded(boolean value) {
        nicsExpand.toggleExpanded(value);
        toggleExpanded();
    }

    public boolean getInstalledAppsState() {
        return installedAppsExpand.isActive();
    }

    public void setInstalledAppsExpanded(boolean value) {
        installedAppsExpand.toggleExpanded(value);
        toggleExpanded();
    }

    public void updateValues(SnapshotModel snapshotModel) {
        Container currentDetails = disksExpand.getDetails();
        if (currentDetails != null) {
            remove(currentDetails);
        }
        Container disksInfoContainer = createDisksItemContainerPanel(snapshotModel.getDisks());
        disksExpand.setDetails(disksInfoContainer);
        disksExpand.toggleExpanded(disksExpand.isActive());
        add(disksInfoContainer);

        currentDetails = nicsExpand.getDetails();
        if (currentDetails != null) {
            remove(currentDetails);
        }
        Container nicsInfoContainer = createNicsItemContainerPanel(snapshotModel.getNics());
        nicsExpand.setDetails(nicsInfoContainer);
        nicsExpand.toggleExpanded(nicsExpand.isActive());
        add(nicsInfoContainer);

        currentDetails = installedAppsExpand.getDetails();
        if (currentDetails != null) {
            remove(currentDetails);
        }
        Container installedAppsInfoContainer = createInstalledAppsItemContainerPanel(snapshotModel.getApps());
        installedAppsExpand.setDetails(installedAppsInfoContainer);
        installedAppsExpand.toggleExpanded(installedAppsExpand.isActive());
        add(installedAppsInfoContainer);
    }
}
