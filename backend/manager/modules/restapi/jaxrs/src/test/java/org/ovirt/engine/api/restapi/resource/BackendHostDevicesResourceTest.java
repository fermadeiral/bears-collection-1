package org.ovirt.engine.api.restapi.resource;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendHostDevicesResourceTest
    extends AbstractBackendHostDevicesResourceTest<BackendHostDevicesResource, HostDevice> {

    public BackendHostDevicesResourceTest() {
        super(new BackendHostDevicesResource(HOST_ID));
    }

    @Test
    public void testList() {
        resource.setUriInfo(setUpBasicUriExpectations());

        setUpEntityQueryExpectations(
                QueryType.GetHostDevicesByHostId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { HOST_ID },
                getHostDeviceCollection());


        verifyHostDevices(resource.list().getHostDevices());
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.HostDevice createDevice() {
        return new org.ovirt.engine.core.common.businessentities.HostDevice();
    }
}
