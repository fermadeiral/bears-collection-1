package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.AuditLogDao;

/** A query to return all the Audit Logs according to a given VM ID */
public class GetAllAuditLogsByVMIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private AuditLogDao auditLogDao;

    public GetAllAuditLogsByVMIdQuery(P parameters) {
        super(parameters);
    }

    /** Actually executes the query, and stores the result in {@link #getQueryReturnValue()} */
    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                auditLogDao.getAllByVMId(getParameters().getId(), getUserID(), getParameters().isFiltered()));
    }
}
