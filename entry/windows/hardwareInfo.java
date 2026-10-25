package entry.windows;

import oshi.hardware.HardwareAbstractionLayer;

/** Shared OSHI connection to hardware information for this process. */
final class hardwareInfo {
    private static final oshi.SystemInfo OSHI = new oshi.SystemInfo();
    static final HardwareAbstractionLayer HARDWARE = OSHI.getHardware();

    /** Prevents creating an instance of this shared hardware helper. */
    private hardwareInfo() {}
}
