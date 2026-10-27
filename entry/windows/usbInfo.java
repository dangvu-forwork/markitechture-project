package entry.windows;

import lib.format.USBDevice;
import oshi.hardware.UsbDevice;

import java.util.ArrayList;

/** Reads USB devices through OSHI. */
final class usbInfo {
    /** Prevents creating an instance of this static helper class. */
    private usbInfo() {}

    /**
     * Returns the USB devices OSHI reports; bus and device numbers are unavailable and set to -1.
     */
    static ArrayList<USBDevice> getDevices() {
        ArrayList<USBDevice> devices = new ArrayList<>();
        for (UsbDevice device : hardwareInfo.HARDWARE.getUsbDevices(false)) 
            // the false argument makes getUsbDevices() returns a flat list/representation of devices
            // as opposed to, well, returning a hierarchical tree structure, which would be a PAIN to work with.
            addDevice(devices, device);
        return devices;
    }

    /**
     * Converts one OSHI USB record into the project's common USB model.
     */
    private static void addDevice(ArrayList<USBDevice> devices, UsbDevice device) {
        devices.add(new USBDevice(
                device.getUniqueDeviceId(),
                parseId(device.getVendorId()),
                parseId(device.getProductId()),
                device.getVendor(),
                device.getName(),
                -1,
                -1));
        // the last -1s are busNumber and deviceNumber.
        // OSHI's UsbDevice API provides device names and vendor/product IDs already. It doesn't provide linux-style USB bus/device numbers.
        // We are working on a windows system after all.
    }

    /**
     * Converts an OSHI hexadecimal ID string to an integer, or returns -1 if unavailable.
     */
    private static int parseId(String id) {
        if (id == null || id.isBlank())
            return -1;
        try {
            /* Basically, it converts an USB ID string (e.g. 0x046d) -> 046d. Just removes the beginning 0x at the start of the string. */
            /* The number is base-16, so the 16 at the end tells parseInt this is base-16 (hexadecimal). */
            return Integer.parseInt(id.replaceFirst("^(0[xX])", ""), 16);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
