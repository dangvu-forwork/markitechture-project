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
    }

    /**
     * Converts an OSHI hexadecimal ID string to an integer, or returns -1 if unavailable.
     */
    private static int parseId(String id) {
        if (id == null || id.isBlank())
            return -1;
        try {
            return Integer.parseInt(id.replaceFirst("^(0[xX])", ""), 16);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
