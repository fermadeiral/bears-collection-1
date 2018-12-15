/*
Copyright (c) 2017 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.restapi.resource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.NetworkFilterParameter;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveVmNicFilterParameterParameters;
import org.ovirt.engine.core.common.action.VmNicFilterParameterParameters;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmNicFilterParameterResourceTest
        extends AbstractBackendSubResourceTest<NetworkFilterParameter, VmNicFilterParameter, BackendVmNicFilterParameterResource> {

    private static final int DEFAULT_INDEX = 0;
    private static final Guid PARAMETER_ID = GUIDS[DEFAULT_INDEX];
    private static final Guid VM_ID = GUIDS[1];
    private static final Guid VM_NIC_ID = GUIDS[2];

    private static final String[] VALUES = { "10.11.12.13", "13.12.11.10", "10.01.10.01" };

    public BackendVmNicFilterParameterResourceTest() {
        super(new BackendVmNicFilterParameterResource(VM_ID, VM_NIC_ID, PARAMETER_ID.toString()));
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(
            VdcQueryType.GetVmInterfaceFilterParameterById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { PARAMETER_ID },
            Collections.emptyList()
        );
        try {
            resource.get();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1);

        NetworkFilterParameter parameter = resource.get();
        verifyModel(parameter, DEFAULT_INDEX);
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(
            VdcQueryType.GetVmInterfaceFilterParameterById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { PARAMETER_ID },
            new ArrayList<VmNetworkInterface>()
        );
        try {
            resource.update(getParameter());
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        setUpGetEntityExpectations(1);
        setUriInfo(
            setUpActionExpectations(
                ActionType.UpdateVmNicFilterParameter,
                VmNicFilterParameterParameters.class,
                new String[] { "VmId" },
                new Object[] { VM_ID },
                true,
                true
            )
        );
        NetworkFilterParameter parameter = resource.update(getParameter());
        assertNotNull(parameter);
    }

    @Test
    public void testRemove() throws Exception {
        setUriInfo(
            setUpActionExpectations(
                ActionType.RemoveVmNicFilterParameter,
                    RemoveVmNicFilterParameterParameters.class,
                new String[] { "VmId", "FilterParameterId" },
                new Object[] { VM_ID, PARAMETER_ID },
                true,
                true
            )
        );
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean valid, boolean success, String detail) throws Exception {
        setUriInfo(
                setUpActionExpectations(
                        ActionType.RemoveVmNicFilterParameter,
                        RemoveVmNicFilterParameterParameters.class,
                        new String[] { "VmId", "FilterParameterId" },
                        new Object[] { VM_ID, PARAMETER_ID },
                        valid,
                        success
                )
        );
        try {
            resource.remove();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    protected NetworkFilterParameter getParameter() {
        NetworkFilterParameter parameter = new NetworkFilterParameter();
        parameter.setId(PARAMETER_ID.toString());
        //parameter.setVmInterfaceId(VM_NIC_ID);
        parameter.setValue(VALUES[DEFAULT_INDEX]);
        parameter.setName(NAMES[DEFAULT_INDEX]);
        return parameter;
    }

    @Override
    protected VmNicFilterParameter getEntity(int index) {
        return setUpEntityExpectations(mock(VmNicFilterParameter.class),
                                       index);
    }

    protected List<VmNicFilterParameter> getEntityList() {
        List<VmNicFilterParameter> entities = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;

    }

    protected void setUpEntityQueryExpectations(int times) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(
                VdcQueryType.GetVmInterfaceFilterParameterById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[times] },
                getEntityList()
            );
        }
    }

    protected void setUpGetEntityExpectations(int times) throws Exception {
        setUpGetEntityExpectations(times, getEntity(DEFAULT_INDEX));
    }

    protected void setUpGetEntityExpectations(int times, VmNicFilterParameter entity) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(
                VdcQueryType.GetVmInterfaceFilterParameterById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { entity.getId()},
                entity
            );
        }
    }

    private VmNicFilterParameter setUpEntityExpectations(
            VmNicFilterParameter entity,
            int index) {
        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getName()).thenReturn(NAMES[index]);
        when(entity.getValue()).thenReturn(VALUES[index]);
        when(entity.getVmInterfaceId()).thenReturn(VM_NIC_ID);
        return entity;
    }

    @Override
    protected void verifyModel(NetworkFilterParameter model, int index) {
        verifyModelSpecific(model, index);
        verifyLinks(model);
    }

    private void verifyModelSpecific(NetworkFilterParameter model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index], model.getName());
        assertEquals(VALUES[index], model.getValue());
        assertTrue(model.isSetNic());
        assertEquals(VM_NIC_ID.toString(), model.getNic().getId());
    }
}
