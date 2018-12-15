package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.VnicProfile;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendAssignedVnicProfilesResourceTest extends AbstractBackendVnicProfilesResourceTest<BackendAssignedVnicProfilesResource> {

    public BackendAssignedVnicProfilesResourceTest() {
        super(new BackendAssignedVnicProfilesResource(NETWORK_ID.toString()),
                QueryType.GetVnicProfilesByNetworkId,
                IdQueryParameters.class);
    }

    @Override
    protected List<VnicProfile> getCollection() {
        return collection.list().getVnicProfiles();
    }
}
