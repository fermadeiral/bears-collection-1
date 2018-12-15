package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.table.cell.AbstractToggleButtonCell;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public abstract class AbstractLunRemoveColumn extends AbstractColumn<LunModel, LunModel> {

    private static final UIConstants uiConstants = ConstantsManager.getInstance().getConstants();
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public AbstractLunRemoveColumn(SanStorageModelBase model) {
        super(new AbstractToggleButtonCell<LunModel>() {
            @Override
            public void onClickEvent(LunModel lunModel) {
                if (lunModel != null && !model.getMetadataDevices().contains(lunModel.getLunId())) {
                    lunModel.setRemoveLunSelected(!lunModel.isRemoveLunSelected());
                }
            }

            @Override
            public void render(Context context, LunModel value, SafeHtmlBuilder sb, String id) {
                boolean exactlyOneLunLeft = false;
                if (model.getMetadataDevices().size() == 0) {
                    exactlyOneLunLeft = model.getNumOfLUNsToRemove() == model.getItems().size() - 1;
                }

                model.getRequireTableRefresh().setEntity(exactlyOneLunLeft);
                String inputId = id + "_input"; //$NON-NLS-1$
                SafeHtml input;

                if (model.getMetadataDevices().contains(value.getLunId()) ||
                        model.getIncludedLuns().size() == 1 ||
                        exactlyOneLunLeft) {
                    input = templates.disabled(inputId, uiConstants.notAvailableLabel());
                } else if (value.isRemoveLunSelected()) {
                    input = templates.toggledDown(inputId, constants.removeSanStorage());
                } else {
                    input = templates.toggledUp(inputId, constants.removeSanStorage());
                }

                sb.append(templates.span(id, input));
            }
        });
    }
}
