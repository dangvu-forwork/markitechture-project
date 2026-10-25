
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

        /** 
         * Gets the memory info (Busy + idle cores) and returns it in the byteUnits format.
         * NOTE: Directly avoid using powers of 2, since ByteUnits.B, ByteUnits.KiB and whatever exists.
         * Using powers of 2 makes the code less readable for others.
         * 
         * additional QnA
         * Q: Why did you divide byteUnits by ByteUnits.KiB?
         * A: .getTotal() and .getUsed() already returns in a KiB format. So in a way, KiB is the default format.
        */
        @Override
        public MemoryInfo getMemoryInfo(long byteUnits) {
            return new MemoryInfo(memInfo.getTotal() / (byteUnits / ByteUnits.KiB), memInfo.getUsed() / (byteUnits / ByteUnits.KiB));
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
            for (int i = 0; i < pciInfo.getCount(); i++) {
                devices.add(new PCIDevice(
                        pciInfo.getAddress(i), pciInfo.getBus(i), pciInfo.getDevice(i),
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
