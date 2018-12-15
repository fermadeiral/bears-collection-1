/*
Copyright (c) 2016 Red Hat, Inc.

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

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.IscsiDetails;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3IscsiDetails;

public class V3IscsiDetailsInAdapter implements V3Adapter<V3IscsiDetails, IscsiDetails> {
    @Override
    public IscsiDetails adapt(V3IscsiDetails from) {
        IscsiDetails to = new IscsiDetails();
        if (from.isSetAddress()) {
            to.setAddress(from.getAddress());
        }
        if (from.isSetDiskId()) {
            to.setDiskId(from.getDiskId());
        }
        if (from.isSetInitiator()) {
            to.setInitiator(from.getInitiator());
        }
        if (from.isSetLunMapping()) {
            to.setLunMapping(from.getLunMapping());
        }
        if (from.isSetPassword()) {
            to.setPassword(from.getPassword());
        }
        if (from.isSetPaths()) {
            to.setPaths(from.getPaths());
        }
        if (from.isSetPort()) {
            to.setPort(from.getPort());
        }
        if (from.isSetPortal()) {
            to.setPortal(from.getPortal());
        }
        if (from.isSetProductId()) {
            to.setProductId(from.getProductId());
        }
        if (from.isSetSerial()) {
            to.setSerial(from.getSerial());
        }
        if (from.isSetSize()) {
            to.setSize(from.getSize().intValue());
        }
        if (from.isSetStatus()) {
            to.setStatus(from.getStatus());
        }
        if (from.isSetStorageDomainId()) {
            to.setStorageDomainId(from.getStorageDomainId());
        }
        if (from.isSetTarget()) {
            to.setTarget(from.getTarget());
        }
        if (from.isSetUsername()) {
            to.setUsername(from.getUsername());
        }
        if (from.isSetVendorId()) {
            to.setVendorId(from.getVendorId());
        }
        if (from.isSetVolumeGroupId()) {
            to.setVolumeGroupId(from.getVolumeGroupId());
        }
        return to;
    }
}
