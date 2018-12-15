package org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyLineModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class VmsSelectionModel extends KeyModel {

    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();
    private boolean initialized;

    public VmsSelectionModel() {
        super(constants.selectVm(), constants.noAvailableVms());
    }

    final Map<String, VM> allVmNamesMap = new HashMap<>();

    public void init(List<VM> vms, List<Guid> usedVms) {
        if (vms == null) {
            return;
        }

        // Create maps for identifying VMs by name or id
        Map<Guid, VM> allVmIdsMap = new HashMap<>();
        populateVmMaps(vms, allVmNamesMap, allVmIdsMap);

        Set<String> usedVmNames = getUsedVmNamesFromIds(usedVms, allVmIdsMap);

        super.init(allVmNamesMap.keySet(), usedVmNames);

        setInitialized();
    }

    private void populateVmMaps(List<VM> vms, Map<String, VM> allVmNamesMap, Map<Guid, VM> allVmIdsMap) {
        vms.forEach(vm -> {
            allVmNamesMap.put(vm.getName(), vm);
            allVmIdsMap.put(vm.getId(), vm);
        });
    }

    private Set<String> getUsedVmNamesFromIds(List<Guid> usedVms, Map<Guid, VM> allVmIdsMap) {
        return usedVms
                .stream()
                .map(guid -> allVmIdsMap.get(guid).getName())
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    protected void initLineModel(KeyLineModel keyValueLineModel, String key) {
        // no implementation
    }

    public List<Guid> getSelectedVmIds() {
        List<Guid> list = new ArrayList<>();
        for (KeyLineModel keyModel : getItems()) {
            String selectedItem = keyModel.getKeys().getSelectedItem();
            if (isKeyValid(selectedItem)) {
                list.add(allVmNamesMap.get(selectedItem).getId());
            }
        }
        return list;
    }

    private void setInitialized() {
        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
