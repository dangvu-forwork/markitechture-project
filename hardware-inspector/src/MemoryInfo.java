/**
 * Core Requirement 2 -- Memory.
 *
 * Like ProcessorInfo, this class does no file reading of its own. The starter
 * class "memInfo" reads /proc/meminfo and gives us the numbers in KiB.
 *
 * WHAT THE NUMBERS MEAN
 * ---------------------
 * KiB means kibibytes: 1 KiB = 1024 bytes. Linux reports memory in KiB, so we
 * divide by 1024 twice to turn KiB into GiB (gibibytes), which is the unit
 * people normally use: 1 GiB = 1024 MiB = 1024 * 1024 KiB.
 *
 * "Available" memory is not just memory that is completely empty. Linux keeps
 * recently used files in RAM to make things faster, and it can hand that memory
 * back when a program needs it. memInfo therefore uses MemAvailable, which is a
 * realistic answer to "how much could a new program use?" - MemFree on its own
 * would look misleadingly small.
 *
 *     used     = total - available
 *     percent  = used / total * 100
 */
public class MemoryInfo {

    public static void report() {
        long totalKiB = memInfo.getTotal();
        long availableKiB = memInfo.getAvailable();
        long usedKiB = memInfo.getUsed();

        System.out.println("=== Memory Information ===");

        // getTotal() returns -1 when /proc/meminfo cannot be read.
        if (totalKiB <= 0 || availableKiB < 0 || usedKiB < 0) {
            System.out.println("Could not read /proc/meminfo (this program must run on Linux).");
            System.out.println();
            return;
        }

        // KiB -> MiB -> GiB. We divide by 1024.0 (not 1024) so that Java keeps
        // the decimal places instead of throwing them away.
        double totalGiB = totalKiB / 1024.0 / 1024.0;
        double usedGiB = usedKiB / 1024.0 / 1024.0;
        double availableGiB = availableKiB / 1024.0 / 1024.0;

        // Multiply by 100.0 first so the division produces a decimal number.
        double percentUsed = 100.0 * usedKiB / totalKiB;
        double percentAvailable = 100.0 * availableKiB / totalKiB;

        System.out.printf("Total memory:     %6.2f GiB%n", totalGiB);
        System.out.printf("Used memory:      %6.2f GiB  (%.1f%% of total)%n", usedGiB, percentUsed);
        System.out.printf("Available memory: %6.2f GiB  (%.1f%% of total)%n", availableGiB, percentAvailable);
        System.out.println();
    }
}
