package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.Guid;

public class DetachUserFromTagCommand<T extends AttachEntityToTagParameters> extends UserTagMapBase<T> {

    public DetachUserFromTagCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        if (getTagId() != null) {
            for (Guid userGuid : getUserList()) {
                DbUser user = dbUserDao.get(userGuid);
                if (tagDao.getTagUserByTagIdAndByuserId(getTagId(), userGuid) != null) {
                    if (user != null) {
                        appendCustomCommaSeparatedValue("DetachUsersNames", user.getLoginName());
                    }
                    tagDao.detachUserFromTag(getTagId(), userGuid);
                    noActionDone = false;
                    setSucceeded(true);
                }
            }
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return noActionDone ? AuditLogType.UNASSIGNED : getSucceeded() ? AuditLogType.USER_DETACH_USER_FROM_TAG
                : AuditLogType.USER_DETACH_USER_FROM_TAG_FAILED;
    }
}
