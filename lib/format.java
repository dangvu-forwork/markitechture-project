package lib;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/** Public system-information models and JSON helpers. */
public final class format {

    public static class MemoryInfo {
        public double totalMemory;
        public double usedMemory;

        public MemoryInfo(double totalMemory, double usedMemory) {
            this.totalMemory = totalMemory;
            this.usedMemory = usedMemory;
        }
    }

    public static class CoreInfo {
        public long idle;
        public long busy;

        public CoreInfo(long idle, long busy) {
            this.idle = idle;
            this.busy = busy;
        }

        public double loadPercentSince(CoreInfo earlier) {
            long busyDelta = busy - earlier.busy;
            long idleDelta = idle - earlier.idle;
            if (busyDelta < 0 || idleDelta < 0)
                throw new IllegalArgumentException("Core counters must not go backwards");

            long totalDelta = busyDelta + idleDelta;
            return totalDelta == 0 ? 0.0 : 100.0 * busyDelta / totalDelta;
        }
    }

    public static class PCIDevice {
        public String address;
        public int bus;
        public int device;
        public int function;
        public int vendorId;
        public int deviceId;
        public int classCode;

        public PCIDevice(String address, int bus, int device, int function,
                         int vendorId, int deviceId, int classCode) {
            this.address = address;
            this.bus = bus;
            this.device = device;
            this.function = function;
            this.vendorId = vendorId;
            this.deviceId = deviceId;
            this.classCode = classCode;
        }
    }

    public static class USBDevice {
        public String name;
        public int vendorId;
        public int productId;
        public String manufacturer;
        public String product;
        public int busNumber;
        public int deviceNumber;

        public USBDevice(String name, int vendorId, int productId, String manufacturer,
                         String product, int busNumber, int deviceNumber) {
            this.name = name;
            this.vendorId = vendorId;
            this.productId = productId;
            this.manufacturer = manufacturer;
            this.product = product;
            this.busNumber = busNumber;
            this.deviceNumber = deviceNumber;
        }
    }

    public interface SystemInfo_Methods {
        String getProcessorModel();
        MemoryInfo getMemoryInfo();
        ArrayList<CoreInfo> getCoreInfos();
        ArrayList<PCIDevice> getPCIDevices();
        ArrayList<USBDevice> getUSBDevices();
    }

    public abstract static class SystemInfo implements SystemInfo_Methods {
        private static final Gson GSON = new Gson();
        private static final Type INFO_MAP_TYPE =
                new TypeToken<Map<String, Object>>() {}.getType();

        public Map<String, Object> readInfo() {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("processorModel", this.getProcessorModel());
            info.put("memoryInfo", this.getMemoryInfo());
            info.put("coreInfos", this.getCoreInfos());
            info.put("pciDevices", this.getPCIDevices());
            info.put("usbDevices", this.getUSBDevices());
            return info;
        }

        public String readInfoJson() {
            return GSON.toJson(readInfo());
        }

        @Override 
        public String toString(){
            return this.readInfoJson();
        }

        public static Map<String, Object> parseInfo(String json) {
            return GSON.fromJson(json, INFO_MAP_TYPE);
        }
    }
}
