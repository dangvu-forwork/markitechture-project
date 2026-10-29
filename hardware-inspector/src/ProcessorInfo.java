/**
 * Core Requirement 1 -- Processor.
 *
 * This class does not read any files itself. The starter class "cpuInfo"
 * (note the lower-case c) already does all of the file reading for us, so this
 * class just asks it for the numbers and presents them nicely.
 *
 * Two things are reported:
 *   1. The processor model and how many cores the machine has.
 *   2. The load on each core, plus the average load.
 *
 * HOW THE LOAD IS MEASURED
 * ------------------------
 * Linux keeps a running total, per core, of how many "ticks" it has spent
 * doing work (busy) and how many it has spent doing nothing (idle). These
 * totals count from the moment the machine booted, so one reading on its own
 * only tells you the average since boot - not what is happening right now.
 *
 * To get the CURRENT load we therefore read the totals twice, one second
 * apart, and look at how much they changed:
 *
 *     load % = busy ticks gained / (busy ticks gained + idle ticks gained) * 100
 *
 * If a core gained 150 busy ticks and 850 idle ticks it was busy for
 * 150 / 1000 = 15% of that second.
 */
public class ProcessorInfo {

    public static void report() {
        int cores = cpuInfo.getCount();

        System.out.println("=== Processor Information ===");

        // On Windows or macOS these files do not exist, so getCount() is 0.
        // We say so instead of dividing by zero or printing nonsense.
        if (cores <= 0) {
            System.out.println("Could not read /proc/cpuinfo (this program must run on Linux).");
            System.out.println();
            return;
        }

        System.out.println("Model: " + modelName());
        System.out.println("Cores: " + cores + " logical processors");

        // ---------------------------------------------------------------
        // READING 1: write down the busy and idle totals for every core.
        // ---------------------------------------------------------------
        System.out.println();
        System.out.println("Measuring CPU load over 1 second...");

        // One slot in each array for each core. core 0 uses index 0, etc.
        long[] busyBefore = new long[cores];
        long[] idleBefore = new long[cores];

        for (int core = 0; core < cores; core++) {
            busyBefore[core] = cpuInfo.getBusy(core);
            idleBefore[core] = cpuInfo.getIdle(core);
        }

        // Wait one second so that the totals have time to change.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // If someone interrupts the wait we carry on anyway; the numbers
            // may just cover a slightly different length of time.
            System.out.println("(the one-second wait was interrupted)");
        }

        // ---------------------------------------------------------------
        // READING 2: read the same totals again and compare.
        // ---------------------------------------------------------------
        System.out.println();
        System.out.println("Load per core:");

        double totalLoad = 0.0;

        for (int core = 0; core < cores; core++) {
            long busyAfter = cpuInfo.getBusy(core);
            long idleAfter = cpuInfo.getIdle(core);

            // -1 means the starter class could not read that value.
            if (busyBefore[core] < 0 || idleBefore[core] < 0
                    || busyAfter < 0 || idleAfter < 0) {
                System.out.printf("  core %-2d : not available%n", core);
                continue;
            }

            // How much each total grew during our one-second wait.
            long busyGained = busyAfter - busyBefore[core];
            long idleGained = idleAfter - idleBefore[core];
            long totalGained = busyGained + idleGained;

            // Protect against dividing by zero (for example if the clock is
            // too fast to tick during our short wait).
            double load = (totalGained > 0)
                    ? (100.0 * busyGained / totalGained)
                    : 0.0;

            totalLoad += load;

            // %-2d left-aligns a number in 2 characters, %.2f prints a
            // decimal number with 2 digits after the point, %% prints "%".
            System.out.printf("  core %-2d : %6.2f%%   (busy ticks +%d, idle ticks +%d)%n",
                    core, load, busyGained, idleGained);
        }

        double averageLoad = totalLoad / cores;
        System.out.printf("Average across all cores: %.2f%%%n", averageLoad);
        System.out.println();
    }

    /**
     * Works out a name for the processor, including the arm64 case.
     *
     * On most PCs /proc/cpuinfo has a "model name" line with a friendly name.
     * Some machines (especially arm64 ones) do not. In that case the starter
     * class returns "unknown", and we fall back to the CPU implementer and
     * part numbers so the output is still useful.
     */
    private static String modelName() {
        String model = cpuInfo.getModel();

        if (!model.equals("unknown")) {
            return model;
        }

        String implementer = cpuInfo.getField("CPU implementer");
        String part = cpuInfo.getField("CPU part");

        if (implementer.isEmpty() && part.isEmpty()) {
            return "unknown (this machine has no model name in /proc/cpuinfo)";
        }

        return "unknown (implementer " + orQuestionMark(implementer)
                + ", part " + orQuestionMark(part) + ")";
    }

    /** Returns "?" when a field was missing, so the sentence still reads well. */
    private static String orQuestionMark(String text) {
        return text.isEmpty() ? "?" : text;
    }
}
