package org.ovirt.engine.ui.common.widget.uicommon.disks;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.RadioButton;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class DisksViewRadioGroup extends Composite {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final String GROUP_NAME = "diskTypeView"; //$NON-NLS-1$

    public interface DisksViewChangeHandler {
        /**
         * Called when the selected disks storage type changes.
         */
        void disksViewChanged(DiskStorageType newType);
    }

    private final List<DisksViewChangeHandler> changeHandlers = new ArrayList<>();

    RadioButton allButton;
    RadioButton imagesButton;
    RadioButton lunsButton;
    RadioButton cinderButton;

    public DisksViewRadioGroup() {
        initWidget(getRadioGroupPanel());
    }

    private Widget getRadioGroupPanel() {
        allButton = new RadioButton(GROUP_NAME);
        allButton.setText(constants.allDisksLabel());
        allButton.setActive(true);
        allButton.addClickHandler(event -> fireChangeHandlers(null));

        imagesButton = new RadioButton(GROUP_NAME);
        imagesButton.setText(constants.imageDisksLabel());
        imagesButton.addClickHandler(event -> fireChangeHandlers(DiskStorageType.IMAGE));

        lunsButton = new RadioButton(GROUP_NAME);
        lunsButton.setText(constants.lunDisksLabel());
        lunsButton.addClickHandler(event -> fireChangeHandlers(DiskStorageType.LUN));

        cinderButton = new RadioButton(GROUP_NAME);
        cinderButton.setText(constants.cinderDisksLabel());
        cinderButton.addClickHandler(event -> fireChangeHandlers(DiskStorageType.CINDER));

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.setDataToggle(Toggle.BUTTONS);
        buttonGroup.add(allButton);
        buttonGroup.add(imagesButton);
        buttonGroup.add(lunsButton);
        buttonGroup.add(cinderButton);

        FlowPanel buttonsPanel = new FlowPanel();
        buttonsPanel.add(buttonGroup);
        return buttonsPanel;
    }

    public void addChangeHandler(DisksViewChangeHandler handler) {
        if (!changeHandlers.contains(handler)) {
            changeHandlers.add(handler);
        }
    }

    private void fireChangeHandlers(DiskStorageType type) {
        for (DisksViewChangeHandler disksViewChangeHandler : changeHandlers) {
            disksViewChangeHandler.disksViewChanged(type);
        }
    }

    public DiskStorageType getDiskStorageType() {
        return imagesButton.getValue() ? DiskStorageType.IMAGE :
               lunsButton.getValue() ? DiskStorageType.LUN :
               cinderButton.getValue() ? DiskStorageType.CINDER :
               null;
    }

    public void setDiskStorageType(DiskStorageType diskStorageType) {
        allButton.setValue(diskStorageType == null);
        imagesButton.setValue(diskStorageType == DiskStorageType.IMAGE);
        lunsButton.setValue(diskStorageType == DiskStorageType.LUN);
        cinderButton.setValue(diskStorageType == DiskStorageType.CINDER);
    }

}
