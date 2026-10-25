package entry.windows;

import lib.format.CoreInfo;
import oshi.hardware.CentralProcessor;

import java.util.ArrayList;

/** Reads processor details and cumulative per-core counters through OSHI. */
final class cpuInfo {
    /** Prevents creating an instance of this static helper class. */
    private cpuInfo() {}

    /**
     * Returns the processor model name reported by OSHI.
     */
    static String getModel() {
        return hardwareInfo.HARDWARE.getProcessor()
                .getProcessorIdentifier().getName().trim();
    }

    /**
     * Returns cumulative busy and idle tick counters for each logical processor.
     */
    static ArrayList<CoreInfo> getCoreInfos() {
        CentralProcessor processor = hardwareInfo.HARDWARE.getProcessor();
        long[][] ticks = processor.getProcessorCpuLoadTicks();
        ArrayList<CoreInfo> cores = new ArrayList<>();
        for (long[] coreTicks : ticks) {
            long idle = coreTicks[CentralProcessor.TickType.IDLE.getIndex()]
                    + coreTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
            long busy = 0;
            for (CentralProcessor.TickType tick : CentralProcessor.TickType.values()) {
                if (tick != CentralProcessor.TickType.IDLE
                        && tick != CentralProcessor.TickType.IOWAIT)
                    busy += coreTicks[tick.getIndex()];
            }
            cores.add(new CoreInfo(idle, busy));
        }
        return cores;
    }
}
