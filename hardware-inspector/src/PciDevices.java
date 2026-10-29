import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Core Requirements 3 and 5 -- PCI devices and their names.
 *
 * Requirement 3 says: list every PCI device with its bus, device and function
 * numbers, its vendor ID, its device ID and its class, and store them in an
 * array or list. Requirement 5 says: turn the vendor and device IDs into names
 * using pci.ids, and show the raw ID when there is no name.
 *
 * The starter classes do the hard parts for us:
 *
 *   pciInfo  reads the folders under /sys/bus/pci/devices
 *            (one folder per device, named like "0000:00:02.0")
 *   idLookup reads pci.ids and turns numbers such as 0x8086 into text
 *
 * A PCI "address" such as 0000:00:02.0 is made of four numbers:
 *
 *     0000 : 00 : 02 . 0
 *     domain bus  device function
 *
 * The domain is almost always 0 and the brief does not ask for it, so we only
 * report bus, device and function.
 *
 * VENDOR AND DEVICE IDS
 * ---------------------
 * Every PCI device answers "who made you?" (the vendor ID) and "which model
 * are you?" (the device ID). 0x8086 is Intel, for example. The numbers are
 * hexadecimal, which is why they are printed with a 0x prefix.
 */
public class PciDevices {

    /**
     * One PCI device, as a small bundle of related values.
     *
     * A class like this is sometimes called a "record": it does not do any
     * work, it just holds data together so we can keep all the devices in one
     * list and pass them around easily.
     */
    public static class Device {
        public String address;   // "0000:00:02.0"
        public int bus;          // 0x00
        public int deviceNumber; // 0x02
        public int function;     // 0
        public int vendorId;     // 0x8086
        public int deviceId;     // 0x1237
        public int classCode;    // 0x060000
    }

    /**
     * Reads every PCI device once and returns them as a list.
     *
     * pciInfo numbers its devices 0, 1, 2, ... so we simply loop from 0 up to
     * getCount() and copy each value into our own Device object.
     */
    public static List<Device> readAll() {
        List<Device> devices = new ArrayList<>();

        for (int i = 0; i < pciInfo.getCount(); i++) {
            Device d = new Device();
            d.address = pciInfo.getAddress(i);
            d.bus = pciInfo.getBus(i);
            d.deviceNumber = pciInfo.getDevice(i);
            d.function = pciInfo.getFunction(i);
            d.vendorId = pciInfo.getVendorId(i);
            d.deviceId = pciInfo.getDeviceId(i);
            d.classCode = pciInfo.getClassCode(i);

            devices.add(d);
        }

        return devices;
    }

    /** Prints the PCI list, using names where pci.ids knows them. */
    public static void report() {
        List<Device> devices = readAll();

        System.out.println("=== PCI Devices ===");

        if (devices.isEmpty()) {
            System.out.println("No PCI devices found.");
            System.out.println();
            return;
        }

        // Load the name database once, before printing anything.
        // If pci.ids is missing we keep going with a null value and simply
        // show the raw IDs, because requirement 5 says the program must still
        // work when a name cannot be found.
        idLookup ids = null;
        try {
            ids = idLookup.pci();
        } catch (IOException e) {
            System.out.println("(pci.ids could not be read, so only raw IDs are shown)");
        }

        System.out.printf("%-12s  %-3s  %-6s  %-8s  %-8s  %-8s  %s%n",
                "Address", "Bus", "Dev.Fn", "Vendor", "Device", "Class", "Vendor / device name");

        for (Device d : devices) {
            // Hex text for the raw numbers, e.g. "0x8086".
            String vendorText = hex(d.vendorId, 4);
            String deviceText = hex(d.deviceId, 4);
            String classText = hex(d.classCode, 6);

            // Look the names up. idLookup returns null if it does not know the
            // ID, so "name != null" is our test for "a name was found".
            String vendorName = (ids != null) ? ids.vendorName(d.vendorId) : null;
            String deviceName = (ids != null) ? ids.deviceName(d.vendorId, d.deviceId) : null;

            // If there is no name, we use the raw ID instead. That is the
            // "show the raw ID when no name is found" part of requirement 5.
            String name = (vendorName != null ? vendorName : vendorText)
                    + " / "
                    + (deviceName != null ? deviceName : deviceText);

            // "%02x" prints a number in hexadecimal, padded to two digits,
            // so bus 5 is shown as "05". "%x" is enough for the function.
            String devFn = String.format("%02x.%x", d.deviceNumber, d.function);

            System.out.printf("%-12s  %02x   %-6s  %-8s  %-8s  %-8s  %s%n",
                    d.address, d.bus, devFn, vendorText, deviceText, classText, name);
        }

        System.out.println("Total: " + devices.size() + " PCI devices");
        System.out.println();
    }

    /**
     * Formats an ID as hexadecimal with a fixed number of digits.
     * For example hex(0x8086, 4) is "0x8086".
     *
     * The starter classes return -1 when a value cannot be read; we show "?"
     * in that case rather than a confusing made-up number.
     */
    private static String hex(int value, int digits) {
        if (value < 0) {
            return "?";
        }
        return String.format("0x%0" + digits + "x", value);
    }
}
