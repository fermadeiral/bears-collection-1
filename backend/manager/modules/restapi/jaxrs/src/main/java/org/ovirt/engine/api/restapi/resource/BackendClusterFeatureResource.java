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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.ClusterFeature;
import org.ovirt.engine.api.resource.ClusterFeatureResource;
import org.ovirt.engine.core.common.businessentities.AdditionalFeature;

public class BackendClusterFeatureResource extends AbstractBackendSubResource<ClusterFeature, AdditionalFeature> implements ClusterFeatureResource {
    private String version;

    public BackendClusterFeatureResource(String version, String id) {
        super(id, ClusterFeature.class, AdditionalFeature.class);
        this.version = version;
    }

    @Override
    public ClusterFeature get() {
        AdditionalFeature feature = BackendClusterFeatureHelper.getClusterFeature(this, version, guid);
        if (feature == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return addLinks(map(feature, null));
    }
}
