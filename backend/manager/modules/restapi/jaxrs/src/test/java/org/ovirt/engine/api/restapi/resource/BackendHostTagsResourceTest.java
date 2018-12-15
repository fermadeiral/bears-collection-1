package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.queries.GetTagsByVdsIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendHostTagsResourceTest extends AbstractBackendAssignedTagsResourceTest<BackendHostTagsResource> {
    public BackendHostTagsResourceTest() {
        super(new BackendHostTagsResource(PARENT_GUID.toString()));
        parentIdName = "VdsId";
        queryType = VdcQueryType.GetTagsByVdsId;
        queryParams = GetTagsByVdsIdParameters.class;
        attachAction = ActionType.AttachVdsToTag;
        attachParams = AttachEntityToTagParameters.class;
    }
}
