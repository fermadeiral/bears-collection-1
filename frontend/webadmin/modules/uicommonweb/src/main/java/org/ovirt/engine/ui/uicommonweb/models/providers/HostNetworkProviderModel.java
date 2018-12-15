package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class HostNetworkProviderModel extends EntityModel {

    private ListModel<Provider<OpenstackNetworkProviderProperties>> networkProviders = new ListModel<>();
    private ListModel<ProviderType> networkProviderType = new ListModel<>();
    private NeutronAgentModel neutronAgentModel = new HostNeutronAgentModel();
    private EntityModel<Boolean> useClusterDefaultNetworkProvider = new EntityModel<>(true);
    private Guid defaultProviderId = null;

    public ListModel<Provider<OpenstackNetworkProviderProperties>> getNetworkProviders() {
        return networkProviders;
    }

    public ListModel<ProviderType> getNetworkProviderType() {
        return networkProviderType;
    }

    public ListModel<String> getProviderPluginType() {
        return getNeutronAgentModel().getPluginType();
    }

    public NeutronAgentModel getNeutronAgentModel() {
        return neutronAgentModel;
    }

    public EntityModel<String> getInterfaceMappings() {
        return getNeutronAgentModel().getInterfaceMappings();
    }

    public EntityModel<Boolean> getUseClusterDefaultNetworkProvider() {
        return useClusterDefaultNetworkProvider;
    }

    public void setDefaultProviderId(Guid defaultProviderId) {
        this.defaultProviderId = defaultProviderId;
        selectDefaultProvider();
    }

    public HostNetworkProviderModel() {
        getNetworkProviders().setIsChangeable(!getUseClusterDefaultNetworkProvider().getEntity());
        getUseClusterDefaultNetworkProvider().getEntityChangedEvent().addListener((ev, sender, args) -> {
                    getNetworkProviders().setIsChangeable(!getUseClusterDefaultNetworkProvider().getEntity());
                    selectDefaultProvider();
                });

        getNetworkProviders().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            Provider<OpenstackNetworkProviderProperties> provider = getNetworkProviders().getSelectedItem();
            getNetworkProviderType().setIsAvailable(provider != null);
            getNetworkProviderType().setSelectedItem(provider == null ? null : provider.getType());
            boolean isNeutron = getNetworkProviderType().getSelectedItem() == ProviderType.OPENSTACK_NETWORK;
            getNeutronAgentModel().init(isNeutron && provider != null ? provider : new Provider<>(),
                getNetworkProviderType().getSelectedItem());
            getNeutronAgentModel().setIsAvailable(isNeutron);
        });

        getNetworkProviderType().setIsChangeable(false);
        getNetworkProviderType().setIsAvailable(false);
        getNeutronAgentModel().setIsAvailable(false);

        initNetworkProvidersList();
    }

    private void initNetworkProvidersList() {
        startProgress();
        AsyncDataProvider.getInstance().getAllProvidersByType(new AsyncQuery<>(result -> {
            stopProgress();
            List<Provider<OpenstackNetworkProviderProperties>> providers = (List) result;
            providers.add(0, null);
            getNetworkProviders().setItems(providers);
            selectDefaultProvider();
        }), ProviderType.OPENSTACK_NETWORK, ProviderType.EXTERNAL_NETWORK);
    }

    private void selectDefaultProvider() {
        if (getNetworkProviders().getItems() != null && getUseClusterDefaultNetworkProvider().getEntity()) {
            Provider defaultProvider= getNetworkProviders().getItems().stream()
                    .filter(provider -> provider != null)
                    .filter(provider -> provider.getId().equals(defaultProviderId))
                    .findFirst().orElse(null);
            getNetworkProviders().setSelectedItem(defaultProvider);
        }
    }

    public boolean validate() {
        setIsValid(getNeutronAgentModel().validate());
        return getIsValid();
    }

}
