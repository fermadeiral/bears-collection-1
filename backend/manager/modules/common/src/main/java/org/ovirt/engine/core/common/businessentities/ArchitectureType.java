package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

public enum ArchitectureType implements Identifiable {
    // Base architectures
    undefined(0),
    /* Guest architecture */
    x86(4),
    /* Guest architecture */
    ppc(3),

    // Specific architectures
    /* Host & Guest architecture */
    x86_64(1, x86),
    /* Host & Guest architecture */
    ppc64(2, ppc),
    /* Guest architecture */
    ppc64le(5, ppc),
    /* Guest architecture */
    ppcle(6, ppc);

    public static final int HOTPLUG_MEMORY_FACTOR_PPC_MB = 256;
    public static final int HOTPLUG_MEMORY_FACTOR_X86_MB = 128;
    private int value;
    private int family;
    private static final HashMap<Integer, ArchitectureType> valueToArchitecture = new HashMap<>();

    static {
        for (ArchitectureType architecture : values()) {
            valueToArchitecture.put(architecture.getValue(), architecture);
        }
    }

    private ArchitectureType(int value) {
        this.value = value;
        this.family = value;
    }

    private ArchitectureType(int value, ArchitectureType family) {
        this.value = value;
        this.family = family.getValue();
    }

    @Override
    public int getValue() {
        return value;
    }

    public ArchitectureType getFamily() {
        return forValue(family);
    }

    public static ArchitectureType forValue(int value) {
        return valueToArchitecture.get(value);
    }

    /**
     * @return Factor hot plugged memory needs to be dividable by for given architecture.
     */
    public int getHotplugMemorySizeFactorMb() {
        switch (this) {
            case x86:
                return HOTPLUG_MEMORY_FACTOR_X86_MB;
            case ppc:
                return HOTPLUG_MEMORY_FACTOR_PPC_MB;
            default:
                return getFamily().getHotplugMemorySizeFactorMb();
        }
    }
}
