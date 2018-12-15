package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.common.vdscommands.VmLeaseVDSParameters;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;

public class GetVmLeaseInfoVDSCommand<T extends VmLeaseVDSParameters> extends IrsBrokerCommand<T> {

    private LeaseInfoReturn result;

    public GetVmLeaseInfoVDSCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        result = getIrsProxy().getVmLeaseInfo(
                getParameters().getLeaseId().toString(),
                getParameters().getStorageDomainId().toString());
        proceedProxyReturnValue();
        Map<String, String> leaseInfo = result.getLeaseInfo();
        leaseInfo.remove(VdsProperties.VmLeaseId);
        leaseInfo.remove(VdsProperties.VmLeaseSdId);
        setReturnValue(leaseInfo);
    }

    @Override
    protected Status getReturnStatus() {
        return result.getStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return result;
    }
}
