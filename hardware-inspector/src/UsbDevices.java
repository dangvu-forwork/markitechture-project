import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Core Requirements 4 and 5 -- USB devices and their names.
 *
 * Requirement 4 says: list every USB device with its vendor ID and product ID,
 * stored in an array or list, and still run properly on a machine that has no
 * USB devices (a virtual machine or the cloud server often has none).
 * Requirement 5 says: turn those IDs into names with usb.ids.
 *
 * The starter classes do the hard parts:
 *
 *   usbInfo  reads the folders under /sys/bus/usb/devices
 *   idLookup reads usb.ids and turns numbers into names
 *
 * USB calls a device's ID a "product ID" instead of a "device ID"; it is the
 * same idea. Both numbers are four hexadecimal digits, e.g. 046d:c52b.
 * A USB ID is normally written without the "0x" prefix, the way the lsusb
 * command shows it, so that is how we print it here.
 */
public class UsbDevices {

    /** One USB device, holding the values we want to report. */
    public static class Device {
        public String name;        // Linux name, e.g. "usb1" or "1-2"
        public int busNumber;      // "Bus 001"
        public int deviceNumber;   // "Device 002"
        public int vendorId;       // 0x046d
        public int productId;      // 0xc52b
    }

    /** Reads every USB device once and returns them as a list. */
    public static List<Device> readAll() {
        List<Device> devices = new ArrayList<>();

        // getCount() is 0 when the machine has no USB support at all.
        for (int i = 0; i < usbInfo.getCount(); i++) {
            Device d = new Device();
            d.name = usbInfo.getName(i);
            d.busNumber = usbInfo.getBusNumber(i);
            d.deviceNumber = usbInfo.getDeviceNumber(i);
            d.vendorId = usbInfo.getVendorId(i);
            d.productId = usbInfo.getProductId(i);

            devices.add(d);
        }

        return devices;
    }

    /** Prints the USB list, using names where usb.ids knows them. */
    public static void report() {
        List<Device> devices = readAll();

        System.out.println("=== USB Devices ===");

        // Requirement 4: no USB must not be treated as an error.
        if (devices.isEmpty()) {
            System.out.println("No USB devices found (a virtual machine may have none).");
            System.out.println();
            return;
        }

        // Load the name database once. If usb.ids is missing we carry on and
        // show the raw IDs instead, so the program never crashes.
        idLookup ids = null;
        try {
            ids = idLookup.usb();
        } catch (IOException e) {
            System.out.println("(usb.ids could not be read, so only raw IDs are shown)");
        }

        System.out.printf("%-5s  %-6s  %-10s  %s%n",
                "Bus", "Device", "ID", "Vendor / product name");

        for (Device d : devices) {
            String vendorText = hex(d.vendorId);
            String productText = hex(d.productId);

            String vendorName = (ids != null) ? ids.vendorName(d.vendorId) : null;
            String productName = (ids != null) ? ids.deviceName(d.vendorId, d.productId) : null;

            // Fall back to the raw ID when usb.ids has no name for it.
            String name = (vendorName != null ? vendorName : vendorText)
                    + " / "
                    + (productName != null ? productName : productText);

            System.out.printf("%03d    %03d     %-10s  %s%n",
                    d.busNumber, d.deviceNumber,
                    vendorText + ":" + productText, name);
        }

        System.out.println("Total: " + devices.size() + " USB devices");
        System.out.println();
    }

    /**
     * Formats a USB ID as four hexadecimal digits, e.g. 0x046d -> "046d".
     * "-1" means the value could not be read, so we show "????" instead.
     */
    private static String hex(int value) {
        if (value < 0) {
            return "????";
        }
        return String.format("%04x", value);
    }
}
