package entry.windows;

import lib.format.CoreInfo;
import lib.format.MemoryInfo;
import lib.format.PCIDevice;
import lib.format.SystemInfo;
import lib.format.USBDevice;
import lib.format.ByteUnits;

import java.util.Locale;
import java.util.ArrayList;

public class demo {
    /**
     * Creates a Windows-backed system information object.
     */
    public static SystemInfo readSystemInfo() {
        return new WindowsSystemInfo();
    }

    /**
     * Runs this platform's collector directly and prints its JSON snapshot.
     */
    public static void main(String[] args) {
        System.out.println(readSystemInfo().toString());
    }

    private static class WindowsSystemInfo extends SystemInfo {
        /**
         * Returns the processor model reported by Windows.
         */
        @Override
        public String getProcessorModel() {
            return cpuInfo.getModel();
        }

        /**
         * Returns total and used physical memory in KiB.
         * Read getMemoryInfo(long byteUnit) for a better idea, as this method is just here as a generic
         */
        @Override
        public MemoryInfo getMemoryInfo() {
            return getMemoryInfo(ByteUnits.KiB);
        }

        /**
         * Returns total and used physical memory but with a custom unit (KiB, MiB, etc.)
         * @param byteUnit Either ByteUnits.B, ByteUnits.KiB, etc. Fun fact: These are actual long values.
         * @return MemoryInfo
         */
        @Override
        public MemoryInfo getMemoryInfo(long byteUnit) {
            return memInfo.getMemoryInfo(byteUnit);
        }

        /**
         * Returns raw cumulative busy and idle counters for each logical processor.
         */
        @Override
        public ArrayList<CoreInfo> getCoreInfos() {
            return cpuInfo.getCoreInfos();
        }

        /**
         * Returns PCI devices Windows identifies through Plug and Play.
         */
        @Override
        public ArrayList<PCIDevice> getPCIDevices() {
            return pciInfo.getDevices();
        }

        /**
         * Returns USB devices Windows identifies through Plug and Play.
         * Windows does not report Linux-style bus and device numbers, so those are -1.
         */
        @Override
        public ArrayList<USBDevice> getUSBDevices() {
            return usbInfo.getDevices();
        }

        /**
         * Formats the collected Windows data as labeled, human-readable text.
         */
        @Override
        public String toString() {
            // A string builder. Used to construct complex strings.
            StringBuilder output = new StringBuilder();
            output.append("SYSTEM INFORMATION\n"); // \n means "Line break", which basically tells the output to move to a new line.
            output.append("==================\n\n");
            output.append("PROCESSOR\n");
            output.append("  Model: ").append(getProcessorModel()).append('\n'); 
            // Calls the "getProcessorModel() method in the windows class. the method references cpuInfo.java
            // Also appends a line break too."
            // This is why StringBuilders are useful, because you can chain things together like above.

            ArrayList<CoreInfo> cores = getCoreInfos(); // Gets all total cores. Reference cpuInfo.java
            output.append("  Logical processors: ").append(cores.size()).append("\n\n"); // cores.size() is the size of the core ArrayList, which kinda equates to total size
            output.append("  Current cumulative counters (not load percentages or memory sizes):\n");
            for (int i = 0; i < cores.size(); i++) {
                CoreInfo core = cores.get(i);
                output.append(String.format(Locale.ROOT, 
                    // Uses the system root locale, a language/country neutral locale.
                    // e.g. Your system uses, god forbid, a Japanese locale. JVM will see this and unfortunately set your Locale to Japanese...
                    // And I wouldn't want a Japanese locale when dealing with outputs. That's why I used the ROOT locale.
                    // Yes, Japanese locale exists.
                        "    Core %d: busy=%d, idle=%d%n", i, core.busy, core.idle));
            }

            long byteUnit     = ByteUnits.MiB;
            String bUnitName  = ByteUnits.unitName(byteUnit);

            MemoryInfo memory = getMemoryInfo(byteUnit); // Read: memInfo.java for more information
            output.append("\nMEMORY\n");
            output.append(String.format(Locale.ROOT,
                    "  Total: %.1f %s%n  Used:  %.1f %s%n",
                    memory.totalMemory, bUnitName, memory.usedMemory, bUnitName)); // For now it doesn't support dynamically changing units.

            ArrayList<PCIDevice> pciDevices = getPCIDevices(); // Refer to pciInfo.java
            output.append("\nPCI DEVICES\n");
            if (pciDevices.isEmpty()) {
                output.append("  No PCI devices found.\n"); // If it's empty, just dont waste time
            } else {
                output.append("  Found ").append(pciDevices.size()).append(" device(s):\n"); // Appends size of the ArrayList and a custom string at the end
                for (PCIDevice device : pciDevices) {
                    output.append(String.format(Locale.ROOT,
                            "  - %s%n"
                                    + "      Location: bus %s, device %s, function %s%n"
                                    + "      Vendor ID: %s  Device ID: %s  Class: %s%n",
                            device.name == null || device.name.isBlank() ? device.address : device.name,
                            // Above line: If its name doesn't exist / is null, use the address of the device instead. Otherwise, use the device's name.
                            formatNumber(device.bus), // formatNumber and formatId are basically wrappers that allow for "unknown"s.
                            formatNumber(device.device),
                            formatNumber(device.function),
                            formatId(device.vendorId), // formatId, in particular, makes it so that the number has to have at least 4 digits.
                            formatId(device.deviceId), // of course it also allows for unknowns too.
                            device.classCode < 0
                                    ? "unknown"
                                    : String.format(Locale.ROOT, "0x%06x", device.classCode))); // 0x(print at least 6 hex digits, here's device.classCode!) basically.
                                    // result looks like 0x040300 for example
                }
            }

            ArrayList<USBDevice> usbDevices = getUSBDevices(); // Read: usbInfo.java
            output.append("\nUSB DEVICES\n");
            if (usbDevices.isEmpty()) {
                output.append("  No USB devices found.\n");
            } else {
                output.append("  Found ").append(usbDevices.size()).append(" device(s):\n");
                for (USBDevice device : usbDevices) {
                    output.append(String.format(Locale.ROOT, "  - %s  ID %s:%s  %s%n",
                            device.product,
                            formatId(device.vendorId),
                            formatId(device.productId),
                            device.manufacturer));
                }
            }
            return output.toString();
        }

        /**
         * Displays a valid device ID as four hexadecimal digits or marks it unavailable.
         */
        private static String formatId(int id) {
            return id < 0 ? "unknown" : String.format(Locale.ROOT, "%04x", id); // %x means hexadecimal, 04 means "print at least 4 digits"
        }

        /**
         * Displays an available PCI location number or marks it unavailable.
         */
        private static String formatNumber(int value) {
            return value < 0 ? "unknown" : Integer.toString(value);
        }
    }
}
