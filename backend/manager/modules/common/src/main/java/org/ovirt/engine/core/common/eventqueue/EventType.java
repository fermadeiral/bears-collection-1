package org.ovirt.engine.core.common.eventqueue;

public enum EventType {
    RECONSTRUCT,
    RECOVERY,
    DOMAINFAILOVER,
    DOMAINNOTOPERATIONAL,
    VDSSTORAGEPROBLEMS,
    DOMAINMONITORING,
    VDSCLEARCACHE,
    VDSCONNECTTOPOOL,
    POOLREFRESH;
}
