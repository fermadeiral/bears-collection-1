package org.ovirt.engine.core.utils.ovf;

public interface OvfProperties {

    String VMD_DEVICE = "Device";
    String VMD_TYPE = "Type";
    String VMD_ADDRESS = "rasd:Address";
    String VMD_NAME = "rasd:Name";
    String VMD_CONNECTION = "rasd:Connection";
    String VMD_VNIC_PROFILE_NAME = "rasd:OtherResourceType";
    String VMD_LINKED = "rasd:Linked";
    String VMD_RESOURCE_TYPE = "rasd:ResourceType";
    String VMD_SUB_RESOURCE_TYPE = "rasd:ResourceSubType";
    String VMD_VIRTUAL_QUANTITY = "rasd:VirtualQuantity";
    String VMD_BOOT_ORDER = "BootOrder";
    String VMD_IS_PLUGGED = "IsPlugged";
    String VMD_IS_READONLY = "IsReadOnly";
    String VMD_SPEC_PARAMS = "SpecParams";
    String VMD_ALIAS = "Alias";
    String VMD_CUSTOM_PROP = "CustomProperties";
    String VMD_SNAPSHOT_PROP = "SnapshotId";

    String AUTO_STARTUP = "AutoStartup";
    String PRIORITY = "Priority";
    String SSO_METHOD = "SsoMethod";
    String DELETE_PROTECTED = "DeleteProtected";
    String IS_SMARTCARD_ENABLED = "IsSmartcardEnabled";
    String TUNNEL_MIGRATION = "TunnelMigration";
    String VNC_KEYBOARD_LAYOUT = "VncKeyboardLayout";
    String MIN_ALLOCATED_MEMORY = "MinAllocatedMem";
    String NUM_OF_IOTHREADS = "NumOfIoThreads";
    String IS_STATELESS = "IsStateless";
    String IS_RUN_AND_PAUSE = "IsRunAndPause";
    String CREATED_BY_USER_ID = "CreatedByUserId";
    String MIGRATION_DOWNTIME = "MigrationDowntime";
    String ORIGIN = "Origin";
    String VM_TYPE = "VmType";
    String KERNEL_PARAMS = "kernel_params";
    String KERNEL_URL = "kernel_url";
    String INITRD_URL = "initrd_url";
    String DEFAULT_BOOT_SEQUENCE = "default_boot_sequence";
    String TIMEZONE = "TimeZone";
    String EXPORT_DATE = "ExportDate";
    String CREATION_DATE = "CreationDate";
    String DOMAIN = "Domain";
    String DESCRIPTION = "Description";
    String GENERATION = "Generation";
    String CUSTOM_COMPATIBILITY_VERSION = "CustomCompatibilityVersion";
    String CLUSTER_COMPATIBILITY_VERSION = "ClusterCompatibilityVersion";
    String CLUSTER_NAME = "ClusterName";
    String NAME = "Name";
    String TEMPLATE_ID = "TemplateId";
    String TEMPLATE_NAME = "TemplateName";
    String INSTANCE_TYPE_ID = "InstanceTypeId";
    String IMAGE_TYPE_ID = "ImageTypeId";
    String IS_INITIALIZED = "IsInitilized";
    String APPLICATIONS_LIST = "app_list";
    String QUOTA_ID = "quota_id";
    String VM_DEFAULT_DISPLAY_TYPE = "DefaultDisplayType";
    String TEMPLATE_DEFAULT_DISPLAY_TYPE = "default_display_type";
    String TRUSTED_SERVICE = "TrustedService";
    String ORIGINAL_TEMPLATE_ID = "OriginalTemplateId";
    String ORIGINAL_TEMPLATE_NAME = "OriginalTemplateName";
    String USE_LATEST_VERSION = "UseLatestVersion";
    String IS_DISABLED = "IsDisabled";
    String TEMPLATE_TYPE = "TemplateType";
    String BASE_TEMPLATE_ID = "BaseTemplateId";
    String TEMPLATE_VERSION_NUMBER = "TemplateVersionNumber";
    String TEMPLATE_VERSION_NAME = "TemplateVersionName";
    String SERIAL_NUMBER_POLICY = "SerialNumberPolicy";
    String CUSTOM_SERIAL_NUMBER = "CustomSerialNumber";
    String IS_BOOT_MENU_ENABLED = "IsBootMenuEnabled";
    String IS_SPICE_FILE_TRANSFER_ENABLED = "IsSpiceFileTransferEnabled";
    String IS_SPICE_COPY_PASTE_ENABLED = "IsSpiceCopyPasteEnabled";
    String ALLOW_CONSOLE_RECONNECT = "AllowConsoleReconnect";
    String COMMENT = "Comment";
    String IS_AUTO_CONVERGE = "IsAutoConverge";
    String IS_MIGRATE_COMPRESSED = "IsMigrateCompressed";
    String MIGRATION_POLICY_ID = "MigrationPolicyId";
    String CUSTOM_EMULATED_MACHINE = "CustomEmulatedMachine";
    String CUSTOM_CPU_NAME = "CustomCpuName";
    String MIGRATION_SUPPORT = "MigrationSupport";
    String DEDICATED_VM_FOR_VDS = "DedicatedVmForVds";
    String USE_HOST_CPU = "UseHostCpu";
    String PREDEFINED_PROPERTIES = "PredefinedProperties";
    String USER_DEFINED_PROPERTIES = "UserDefinedProperties";
    String MAX_MEMORY_SIZE_MB = "MaxMemorySizeMb";
    String VM_LEASE = "LeaseDomainId";
    String STOP_TIME = "StopTime";
    String BOOT_TIME = "BootTime";
    String DOWNTIME = "Downtime";

    // Luns
    String LUN_DISCARD_ZEROES_DATA = "discard_zeroes_data";
    String LUN_DISCARD_MAX_SIZE = "discard_max_size";
    String LUN_DEVICE_SIZE = "device_size";
    String LUN_PRODUCT_ID = "product_id";
    String LUN_VENDOR_ID = "vendor_id";
    String LUN_MAPPING = "lun_mapping";
    String LUN_SERIAL = "serial";
    String LUN_VOLUME_GROUP_ID = "volume_group_id";
    String LUN_ID = "lun_id";
    String LUN_PHYSICAL_VOLUME_ID = "physical_volume_id";
    String LUN_CONNECTION = "Connection";
    String LUNS_CONNECTION = "connection";
    String LUNS_IQN = "iqn";
    String LUNS_PORT = "port";
    String LUNS_STORAGE_TYPE = "storage_type";
    String LUNS_PORTAL = "portal";

    // Affinity groups
    String AFFINITY_GROUP = "AffinityGroup";
    }
