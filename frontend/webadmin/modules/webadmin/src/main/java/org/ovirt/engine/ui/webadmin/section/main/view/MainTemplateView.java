package org.ovirt.engine.ui.webadmin.section.main.view;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.searchbackend.VmTemplateConditionFieldAutoCompleter;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractFullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLinkColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainTemplatePresenter;
import org.ovirt.engine.ui.webadmin.widget.table.column.CommentColumn;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.inject.Inject;

public class MainTemplateView extends AbstractMainWithDetailsTableView<VmTemplate, TemplateListModel> implements MainTemplatePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTemplateView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public MainTemplateView(MainModelProvider<VmTemplate, TemplateListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().enableColumnResizing();

        AbstractTextColumn<VmTemplate> nameColumn = new AbstractLinkColumn<VmTemplate>(
                new FieldUpdater<VmTemplate, String>() {

            @Override
            public void update(int index, VmTemplate template, String value) {
                //The link was clicked, now fire an event to switch to details.
                transitionHandler.handlePlaceTransition(true);
            }

        }) {
            @Override
            public String getValue(VmTemplate object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable(VmTemplateConditionFieldAutoCompleter.NAME);
        getTable().addColumn(nameColumn, constants.namePool(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<VmTemplate> versionNameColumn = new AbstractTextColumn<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                if (object.getId().equals(object.getBaseTemplateId())) {
                    return ""; //$NON-NLS-1$
                }

                return StringFormat.format("%s (%s)", //$NON-NLS-1$
                        object.getTemplateVersionName() != null ? object.getTemplateVersionName() : "", //$NON-NLS-1$
                        object.getTemplateVersionNumber());
            }
        };
        getTable().addColumn(versionNameColumn, constants.versionTemplate(), "150px"); //$NON-NLS-1$

        CommentColumn<VmTemplate> commentColumn = new CommentColumn<>();
        getTable().addColumnWithHtmlHeader(commentColumn,
                SafeHtmlUtils.fromSafeConstant(constants.commentLabel()),
                "75px"); //$NON-NLS-1$

        AbstractTextColumn<VmTemplate> creationDateColumn = new AbstractFullDateTimeColumn<VmTemplate>() {
            @Override
            protected Date getRawValue(VmTemplate object) {
                return object.getCreationDate();
            }
        };
        creationDateColumn.makeSortable(VmTemplateConditionFieldAutoCompleter.CREATIONDATE);
        getTable().addColumn(creationDateColumn, constants.creationDateTemplate(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<VmTemplate> statusColumn = new AbstractEnumColumn<VmTemplate, VmTemplateStatus>() {
            @Override
            protected VmTemplateStatus getRawValue(VmTemplate object) {
                return object.getStatus();
            }
        };
        statusColumn.makeSortable(VmTemplateConditionFieldAutoCompleter.STATUS);
        getTable().addColumn(statusColumn, constants.statusTemplate(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<VmTemplate> clusterColumn = new AbstractTextColumn<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                return object.getClusterName();
            }
        };
        clusterColumn.makeSortable(VmTemplateConditionFieldAutoCompleter.CLUSTER);
        getTable().addColumn(clusterColumn, constants.clusterTemplate(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<VmTemplate> dcColumn = new AbstractTextColumn<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                return object.getStoragePoolName();
            }
        };
        dcColumn.makeSortable(VmTemplateConditionFieldAutoCompleter.DATACENTER);
        getTable().addColumn(dcColumn, constants.dcTemplate(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<VmTemplate> descriptionColumn = new AbstractTextColumn<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                return object.getDescription();
            }
        };
        descriptionColumn.makeSortable(VmTemplateConditionFieldAutoCompleter.DESCRIPTION);
        getTable().addColumn(descriptionColumn, constants.descriptionTemplate(), "150px"); //$NON-NLS-1$
    }

}
