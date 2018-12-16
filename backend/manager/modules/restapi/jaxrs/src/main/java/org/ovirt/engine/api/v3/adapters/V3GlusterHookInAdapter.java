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

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.GlusterHook;
import org.ovirt.engine.api.model.GlusterHookStatus;
import org.ovirt.engine.api.model.GlusterServerHooks;
import org.ovirt.engine.api.model.HookContentType;
import org.ovirt.engine.api.model.HookStage;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GlusterHook;

public class V3GlusterHookInAdapter implements V3Adapter<V3GlusterHook, GlusterHook> {
    @Override
    public GlusterHook adapt(V3GlusterHook from) {
        GlusterHook to = new GlusterHook();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetChecksum()) {
            to.setChecksum(from.getChecksum());
        }
        if (from.isSetCluster()) {
            to.setCluster(adaptIn(from.getCluster()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetConflictStatus()) {
            to.setConflictStatus(from.getConflictStatus());
        }
        if (from.isSetConflicts()) {
            to.setConflicts(from.getConflicts());
        }
        if (from.isSetContent()) {
            to.setContent(from.getContent());
        }
        if (from.isSetContentType()) {
            to.setContentType(HookContentType.fromValue(from.getContentType()));
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetGlusterCommand()) {
            to.setGlusterCommand(from.getGlusterCommand());
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetServerHooks()) {
            to.setServerHooks(new GlusterServerHooks());
            to.getServerHooks().getGlusterServerHooks().addAll(adaptIn(from.getServerHooks().getGlusterServerHooks()));
        }
        if (from.isSetStage()) {
            to.setStage(HookStage.fromValue(from.getStage()));
        }
        if (from.isSetStatus() && from.getStatus().isSetState()) {
            to.setStatus(GlusterHookStatus.fromValue(from.getStatus().getState()));
        }
        return to;
    }
}
