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

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.DataCenterNetworkResource;
import org.ovirt.engine.api.resource.NetworkResource;
import org.ovirt.engine.api.restapi.resource.BackendApiResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Network;

@Produces({"application/xml", "application/json"})
public class V3DataCenterNetworkServer extends V3Server<DataCenterNetworkResource> {
    private String networkId;

    public V3DataCenterNetworkServer(String networkId, DataCenterNetworkResource delegate) {
        super(delegate);
        this.networkId = networkId;
    }

    @GET
    public V3Network get() {
        return adaptGet(getDelegate()::get);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3Network update(V3Network network) {
        return adaptUpdate(getDelegate()::update, network);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }

    @Path("permissions")
    public V3AssignedPermissionsServer getPermissionsResource() {
        return new V3AssignedPermissionsServer(getNetworkResource().getPermissionsResource());
    }

    @Path("vnicprofiles")
    public V3AssignedVnicProfilesServer getVnicProfilesResource() {
        return new V3AssignedVnicProfilesServer(getNetworkResource().getVnicProfilesResource());
    }

    @Path("labels")
    public V3LabelsServer getLabelsResource() {
        return new V3LabelsServer(getNetworkResource().getNetworkLabelsResource());
    }

    private NetworkResource getNetworkResource() {
        return BackendApiResource.getInstance()
            .getNetworksResource()
            .getNetworkResource(networkId);
    }
}
