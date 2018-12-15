package org.ovirt.engine.core.utils.ovf;

import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.compat.Match;
import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.compat.RegexOptions;
import org.ovirt.engine.core.compat.Version;

public class OvfVmWriter extends OvfWriter {
    private static final String EXPORT_ONLY_PREFIX = "exportonly_";
    private VM vm;

    public OvfVmWriter(VM vm, List<DiskImage> images, Version version, OsRepository osRepository) {
        super(vm.getStaticData(), images, version, osRepository);
        this.vm = vm;
    }

    @Override
    protected void writeGeneralData() {
        super.writeGeneralData();
        _writer.writeElement(TEMPLATE_ID, vm.getVmtGuid().toString());
        _writer.writeElement(TEMPLATE_NAME, vm.getVmtName());
        if (vm.getInstanceTypeId() != null ) {
            _writer.writeElement(INSTANCE_TYPE_ID, vm.getInstanceTypeId().toString());
        }
        if (vm.getImageTypeId() != null ) {
            _writer.writeElement(IMAGE_TYPE_ID, vm.getImageTypeId().toString());
        }
        _writer.writeElement(IS_INITIALIZED, String.valueOf(vm.isInitialized()));
        _writer.writeElement(ORIGIN, String.valueOf(vm.getOrigin().getValue()));
        if (!StringUtils.isBlank(vm.getAppList())) {
            _writer.writeElement(APPLICATIONS_LIST, vm.getAppList());
        }
        if (vm.getQuotaId() != null) {
            _writer.writeElement(QUOTA_ID, vm.getQuotaId().toString());
        }
        _writer.writeElement(VM_DEFAULT_DISPLAY_TYPE, String.valueOf(vm.getDefaultDisplayType().getValue()));
        _writer.writeElement(TRUSTED_SERVICE, String.valueOf(vm.isTrustedService()));

        if (vm.getOriginalTemplateGuid() != null) {
            _writer.writeElement(ORIGINAL_TEMPLATE_ID, vm.getOriginalTemplateGuid().toString());
        }

        if (vm.getOriginalTemplateName() != null) {
            _writer.writeElement(ORIGINAL_TEMPLATE_NAME, vm.getOriginalTemplateName());
        }

        _writer.writeElement(USE_HOST_CPU, String.valueOf(vm.isUseHostCpuFlags()));
        _writer.writeElement(USE_LATEST_VERSION, String.valueOf(vm.isUseLatestVersion()));

        OvfLogEventHandler<VmStatic> handler = new VMStaticOvfLogHandler(vm.getStaticData());
        // Gets a map that its keys are aliases to fields that should be OVF
        // logged.
        Map<String, String> aliasesValuesMap = handler.getAliasesValuesMap();
        for (Map.Entry<String, String> entry : aliasesValuesMap.entrySet()) {
            writeLogEvent(entry.getKey(), entry.getValue());
        }

        if (vm.getLastStopTime() != null) {
            _writer.writeElement(STOP_TIME, OvfParser.localDateToUtcDateString(vm.getLastStopTime()));
        }

        if (vm.getBootTime() != null) {
            _writer.writeElement(BOOT_TIME, OvfParser.localDateToUtcDateString(vm.getBootTime()));
            _writer.writeElement(DOWNTIME, String.valueOf(vm.getDowntime()));
        }

    }

    private void writeLogEvent(String name, String value) {
        StringBuilder fullNameSB = new StringBuilder(EXPORT_ONLY_PREFIX);
        fullNameSB.append(name);
        _writer.writeElement(fullNameSB.toString(), value);
    }

    @Override
    protected Integer maxNumOfVcpus() {
        return VmCpuCountHelper.calcMaxVCpu(vm, getVersion());
    }

    @Override
    protected void writeAppList() {
        if (_images.size() > 0) {
            if (StringUtils.isBlank(_images.get(0).getAppList())) {
                return;
            }

            String[] apps = _images.get(0).getAppList().split("[,]", -1);
            for (String app : apps) {
                String product = app;
                String version = "";
                Match match = Regex.match(app, "(.*) ([0-9.]+)", RegexOptions.Singleline | RegexOptions.IgnoreCase);

                if (match.groups().size() > 1) {
                    product = match.groups().get(1).getValue();
                }
                if (match.groups().size() > 2) {
                    version = match.groups().get(2).getValue();
                }

                _writer.writeStartElement("ProductSection");
                _writer.writeAttributeString(OVF_URI, "class", product);
                _writer.writeElement("Info", app);
                _writer.writeElement("Product", product);
                _writer.writeElement("Version", version);
                _writer.writeEndElement();
            }
        }
    }

    @Override
    protected void writeContentItems() {
        super.writeContentItems();
        writeSnapshotsSection();
    }

    @Override
    protected void writeMacAddress(VmNetworkInterface iface) {
        _writer.writeStartElement(RASD_URI, "MACAddress");
        _writer.writeRaw(iface.getMacAddress());
        _writer.writeEndElement();
    }

    /**
     * Write the snapshots of the VM.<br>
     * If no snapshots were set to be written, this section will not be written.
     */
    private void writeSnapshotsSection() {
        List<Snapshot> snapshots = vm.getSnapshots();
        if (snapshots == null || snapshots.isEmpty()) {
            return;
        }

        _writer.writeStartElement("Section");
        _writer.writeAttributeString(XSI_URI, "type", "ovf:SnapshotsSection_Type");

        for (Snapshot snapshot : snapshots) {
            _writer.writeStartElement("Snapshot");
            _writer.writeAttributeString(OVF_URI, "id", snapshot.getId().toString());
            _writer.writeElement("Type", snapshot.getType().name());
            _writer.writeElement("Description", snapshot.getDescription());
            _writer.writeElement("CreationDate", OvfParser.localDateToUtcDateString(snapshot.getCreationDate()));

            if (!snapshot.getMemoryVolume().isEmpty()) {
                _writer.writeElement("Memory", snapshot.getMemoryVolume());
            }

            if (snapshot.getAppList() != null) {
                _writer.writeElement("ApplicationList", snapshot.getAppList());
            }

            if (snapshot.getVmConfiguration() != null) {
                _writer.writeElement("VmConfiguration",
                        Base64.encodeBase64String(snapshot.getVmConfiguration().getBytes()));
            }

            _writer.writeEndElement();
        }

        _writer.writeEndElement();
    }
}
