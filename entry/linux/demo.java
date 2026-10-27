
package entry.linux;

import lib.format.CoreInfo;
import lib.format.MemoryInfo;
import lib.format.PCIDevice;
import lib.format.SystemInfo;
import lib.format.USBDevice;
import lib.format.ByteUnits;

import java.util.ArrayList;

public class demo {
    /** 
     * the readSystemInfo() method will be used in index.java.
     * This method must return an object of type SystemInfo.
    */
    public static SystemInfo readSystemInfo() {
        return new LinuxSystemInfo();
    }

    /** 
     * Q: Why did you put main() here if its gonna be run in index.java anyways?
     * A: To test out linux/windows behavior in isolation.
     */
    public static void main(){
        System.out.print(readSystemInfo().toString());
    }

    private static class LinuxSystemInfo extends SystemInfo {
        /** Just overrides and other stuff. NOTE: You can't miss even a single override.*/
        @Override
        public String getProcessorModel() {
            return cpuInfo.getModel();
        }

        /** Refer to getMemoryInfo with a long in it called byteUnits. This method is js a wrapper. */
        @Override
        public MemoryInfo getMemoryInfo() {
            return getMemoryInfo(ByteUnits.KiB);
        }

        /** Converts Linux's KiB readings into the requested unit, such as ByteUnits.GiB. */
        @Override
        public MemoryInfo getMemoryInfo(long byteUnits) {
            if (byteUnits <= 0) {
                throw new IllegalArgumentException("Memory unit must be positive");
            }
            long total = memInfo.getTotal();
            long used = memInfo.getUsed();
            // Convert KiB to bytes first, then divide by the requested unit.
            // A double keeps fractions; -1 must stay -1 because it means unknown.
            return new MemoryInfo(total < 0 ? -1 : total * (double) ByteUnits.KiB / byteUnits,
                    used < 0 ? -1 : used * (double) ByteUnits.KiB / byteUnits);
        }


        @Override
        public ArrayList<CoreInfo> getCoreInfos() {
            ArrayList<CoreInfo> cores = new ArrayList<>();
            for (int i = 0; i < cpuInfo.getCount(); i++)
                cores.add(new CoreInfo(cpuInfo.getIdle(i), cpuInfo.getBusy(i)));
            return cores;
        }

        @Override
        public ArrayList<PCIDevice> getPCIDevices() {
            ArrayList<PCIDevice> devices = new ArrayList<>();
            idLookup lookup = null;
            try {
                lookup = idLookup.pci();
            } catch (java.io.IOException ignored) {
                // Device addresses remain usable when the optional database is missing.
            }
            for (int i = 0; i < pciInfo.getCount(); i++) {
                String address = pciInfo.getAddress(i);
                // The database maps numeric vendor/device IDs to friendly names.
                String name = null;
                if (lookup != null) {
                    name = lookup.deviceName(pciInfo.getVendorId(i), pciInfo.getDeviceId(i));
                }
                if (name == null) {
                    name = address;
                }
                devices.add(new PCIDevice(
                        address, name, pciInfo.getBus(i), pciInfo.getDevice(i),
                        pciInfo.getFunction(i), pciInfo.getVendorId(i),
                        pciInfo.getDeviceId(i), pciInfo.getClassCode(i)));
            }
            return devices;
        }

        @Override
        public ArrayList<USBDevice> getUSBDevices() {
            ArrayList<USBDevice> devices = new ArrayList<>();
            for (int i = 0; i < usbInfo.getCount(); i++) {
                devices.add(new USBDevice(
                        usbInfo.getName(i), usbInfo.getVendorId(i), usbInfo.getProductId(i),
                        usbInfo.getManufacturer(i), usbInfo.getProduct(i),
                        usbInfo.getBusNumber(i), usbInfo.getDeviceNumber(i)));
            }
            return devices;
        }
    }
}
