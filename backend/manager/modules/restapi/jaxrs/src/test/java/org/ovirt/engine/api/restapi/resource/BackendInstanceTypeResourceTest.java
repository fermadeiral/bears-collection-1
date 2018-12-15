package org.ovirt.engine.api.restapi.resource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.api.restapi.resource.BackendInstanceTypesResourceTest.getModel;
import static org.ovirt.engine.api.restapi.resource.BackendInstanceTypesResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendInstanceTypesResourceTest.verifyModelSpecific;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.ovirt.engine.api.model.InstanceType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.action.VmTemplateManagementParameters;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.VmDeviceType;

public class BackendInstanceTypeResourceTest
    extends AbstractBackendSubResourceTest<InstanceType, org.ovirt.engine.core.common.businessentities.InstanceType, BackendInstanceTypeResource> {

    public BackendInstanceTypeResourceTest() {
        super(new BackendInstanceTypeResource(GUIDS[0].toString()));
    }

    @Test
    public void testBadGuid() throws Exception {
        try {
            new BackendTemplateResource("foo");
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetGraphicsExpectations(1);
        setUpGetEntityExpectations(1);
        setUpGetBallooningExpectations();

        verifyModel(resource.get(), 0);
    }

    protected void setUpGetEntityExpectations(int times) throws Exception {
        setUpGetEntityExpectations(times, false);
    }

    @Test
    public void testGetWithConsoleSet() throws Exception {
        testGetConsoleAware(true);
    }

    @Test
    public void testGetWithConsoleNotSet() throws Exception {
        testGetConsoleAware(false);
    }

    public void testGetConsoleAware(boolean allContent) throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);
        setUpGetBallooningExpectations();

        if (allContent) {
            List<String> populates = new ArrayList<>();
            populates.add("true");
            when(httpHeaders.getRequestHeader(BackendResource.POPULATE)).thenReturn(populates);
            setUpGetConsoleExpectations(0);
            setUpGetVirtioScsiExpectations(0);
            setUpGetSoundcardExpectations(0);
            setUpGetRngDeviceExpectations(0);
        }
        setUpGetGraphicsExpectations(1);

        InstanceType response = resource.get();
        verifyModel(response, 0);

        List<String> populateHeader = httpHeaders.getRequestHeader(BackendResource.POPULATE);
        boolean populated = populateHeader != null ? populateHeader.contains("true") : false;
        assertTrue(populated ? response.isSetConsole() : !response.isSetConsole());
    }

    protected void setUpGetVirtioScsiExpectations(int ... idxs) throws Exception {
        for (int i = 0; i < idxs.length; i++) {
            setUpGetEntityExpectations(QueryType.GetVirtioScsiControllers,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[idxs[i]] },
                    new ArrayList<>());
        }
    }

    protected void setUpGetSoundcardExpectations(int ... idxs) throws Exception {
        for (int i = 0; i < idxs.length; i++) {
            setUpGetEntityExpectations(QueryType.GetSoundDevices,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[idxs[i]] },
                    new ArrayList<>());
        }
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        try {
            resource.update(getRestModel(0));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        setUpGetGraphicsExpectations(1);
        setUpUpdateExpectations();
        setUpGetBallooningExpectations();

        setUriInfo(setUpActionExpectations(ActionType.UpdateVmTemplate,
                UpdateVmTemplateParameters.class,
                new String[]{},
                new Object[]{},
                true,
                true));

        verifyModel(resource.update(getRestModel(0)), 0);
    }

    @Test
    public void testUpdateCantDo() throws Exception {
        doTestBadUpdate(false, true, CANT_DO);
    }

    @Test
    public void testUpdateFailed() throws Exception {
        doTestBadUpdate(true, false, FAILURE);
    }

    protected void doTestBadUpdate(boolean valid, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations(1);
        setUriInfo(setUpActionExpectations(ActionType.UpdateVmTemplate,
                UpdateVmTemplateParameters.class,
                new String[]{},
                new Object[]{},
                valid,
                success));

        try {
            resource.update(getRestModel(0));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testConflictedUpdate() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);

        InstanceType model = getRestModel(1);
        model.setId(GUIDS[1].toString());
        try {
            resource.update(model);
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyImmutabilityConstraint(wae);
        }
    }

    protected void setUpUpdateExpectations() throws Exception {
        setUpGetEntityExpectations(2);
        setUpGetConsoleExpectations(0);
        setUpGetVirtioScsiExpectations(0);
        setUpGetSoundcardExpectations(0);
        setUpGetRngDeviceExpectations(0);
    }

    protected void setUpGetGraphicsExpectations(int times) throws Exception {
        for (int i = 0; i < times; i++) {
            setUpGetEntityExpectations(QueryType.GetGraphicsDevices,
                    IdQueryParameters.class,
                    new String[]{"Id"},
                    new Object[]{GUIDS[i]},
                    Collections.singletonList(new GraphicsDevice(VmDeviceType.SPICE)));
        }
    }

    protected void setUpGetBallooningExpectations() throws Exception {
        setUpGetEntityExpectations(QueryType.IsBalloonEnabled,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                true);
    }
    @Test
    public void testRemove() throws Exception {
        setUpGetGraphicsExpectations(1);
        setUpGetEntityExpectations(1);
        setUpGetBallooningExpectations();
        setUriInfo(setUpActionExpectations(
                ActionType.RemoveVmTemplate,
                VmTemplateManagementParameters.class,
                new String[] { "VmTemplateId" },
                new Object[] { GUIDS[0] },
                true,
                true));
        Response response = resource.remove();
        verifyRemove(response);
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.InstanceType getEntity(int index) {
        return setUpEntityExpectations(mock(VmTemplate.class), index);
    }

    private InstanceType getRestModel(int index) {
        return getModel(index);
    }

    @Override
    protected void verifyModel(InstanceType model, int index) {
        super.verifyModel(model, index);
        verifyModelSpecific(model);
    }

    private void setUpGetEntityExpectations(int times, boolean notFound) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(QueryType.GetInstanceType,
                    GetVmTemplateParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[0] },
                    notFound ? null : getEntity(0));
        }
    }
}
