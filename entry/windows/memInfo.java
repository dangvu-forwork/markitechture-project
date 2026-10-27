package entry.windows;

import lib.format.MemoryInfo;
import oshi.hardware.GlobalMemory;

/** Reads physical memory totals through OSHI. */
final class memInfo {
    /** Prevents creating an instance of this static helper class. */
    private memInfo() {}

    /**
     * OSHI returns bytes. Divide by ByteUnits.KiB, MiB or GiB to choose the result unit.
     */
    static MemoryInfo getMemoryInfo( long byteUnit ) {
        if (byteUnit <= 0) {
            throw new IllegalArgumentException("Memory unit must be positive");
        }
        GlobalMemory memory = hardwareInfo.HARDWARE.getMemory();
        long totalBytes = memory.getTotal();
        long availableBytes = memory.getAvailable();
        // Only subtract valid readings; -1 is the shared value for unknown data.
        long usedBytes = totalBytes < 0 || availableBytes < 0 ? -1 : totalBytes - availableBytes;
        // Casting to double before division preserves decimal places (e.g. 7.5 GiB).
        return new MemoryInfo(totalBytes < 0 ? -1 : totalBytes / (double) byteUnit,
                usedBytes < 0 ? -1 : usedBytes / (double) byteUnit);
    }
}
