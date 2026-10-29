/**
 * Hardware Inspector -- the entry point of the program.
 *
 * Each part of the report lives in its own class, and each class is
 * responsible for one requirement of the brief. Keeping them separate means
 * the code is easier to read, easier to test, and easier to explain.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("           HARDWARE INSPECTOR            ");
        System.out.println("=========================================");
        System.out.println();

        ProcessorInfo.report();   // Core Requirement 1: processor
        MemoryInfo.report();      // Core Requirement 2: memory
        PciDevices.report();      // Core Requirements 3 and 5: PCI devices and names
        UsbDevices.report();      // Core Requirements 4 and 5: USB devices and names
    }
}
