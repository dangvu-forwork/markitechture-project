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
            StringBuilder output = new StringBuilder();
            output.append("SYSTEM INFORMATION\n");
            output.append("==================\n\n");
            output.append("PROCESSOR\n");
            output.append("  Model: ").append(getProcessorModel()).append('\n');

            ArrayList<CoreInfo> cores = getCoreInfos();
            output.append("  Logical processors: ").append(cores.size()).append("\n\n");
            output.append("  Current cumulative counters (not load percentages):\n");
            for (int i = 0; i < cores.size(); i++) {
                CoreInfo core = cores.get(i);
                output.append(String.format(Locale.ROOT,
                        "    Core %d: busy=%d, idle=%d%n", i, core.busy, core.idle));
            }

            MemoryInfo memory = getMemoryInfo();
            output.append("\nMEMORY\n");
            output.append(String.format(Locale.ROOT,
                    "  Total: %.0f KiB%n  Used:  %.0f KiB%n",
                    memory.totalMemory, memory.usedMemory));

            ArrayList<PCIDevice> pciDevices = getPCIDevices();
            output.append("\nPCI DEVICES\n");
            if (pciDevices.isEmpty()) {
                output.append("  No PCI devices found.\n");
            } else {
                output.append("  Found ").append(pciDevices.size()).append(" device(s):\n");
                for (PCIDevice device : pciDevices) {
                    output.append(String.format(Locale.ROOT,
                            "  - %s%n"
                                    + "      Location: bus %s, device %s, function %s%n"
                                    + "      Vendor ID: %s  Device ID: %s  Class: %s%n",
                            device.name == null || device.name.isBlank() ? device.address : device.name,
                            formatNumber(device.bus),
                            formatNumber(device.device),
                            formatNumber(device.function),
                            formatId(device.vendorId),
                            formatId(device.deviceId),
                            device.classCode < 0
                                    ? "unknown"
                                    : String.format(Locale.ROOT, "0x%06x", device.classCode)));
                }
            }

            ArrayList<USBDevice> usbDevices = getUSBDevices();
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
            return id < 0 ? "unknown" : String.format(Locale.ROOT, "%04x", id);
        }

        /**
         * Displays an available PCI location number or marks it unavailable.
         */
        private static String formatNumber(int value) {
            return value < 0 ? "unknown" : Integer.toString(value);
        }
    }
}
