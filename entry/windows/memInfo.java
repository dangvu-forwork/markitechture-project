package entry.windows;

import java.lang.Math;
import lib.format.MemoryInfo;
import lib.format.ByteUnits;
import oshi.hardware.GlobalMemory;

/** Reads physical memory totals through OSHI. */
final class memInfo {
    /** Prevents creating an instance of this static helper class. */
    private memInfo() {}

    /**
     * Returns total and used physical memory in a unit that represents the power of 2.
     * Basically, powerOf2 = 10 => KiB, powerOf2 = 20 => MiB, powerOf2 = 30 => GiB, etc.
     */
    static MemoryInfo getMemoryInfo( long byteUnit ) {
        GlobalMemory memory = hardwareInfo.HARDWARE.getMemory();
        long totalBytes = memory.getTotal();
        long usedBytes = totalBytes - memory.getAvailable();
        return new MemoryInfo(totalBytes / byteUnit, usedBytes / byteUnit);
    }
}
