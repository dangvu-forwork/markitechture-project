package entry.windows;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import lib.format.PCIDevice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Welcome to hell.
 * Honestly, I'll just explain this with a live demonstration later. 
 * Getting into detail about how all this code works just isn't a good idea. - dang vu
  */
/** Reads PCI device information from the Windows Enum registry. */
final class pciInfo {
    private static final String PCI_REGISTRY_PATH = "SYSTEM\\CurrentControlSet\\Enum\\PCI";
    private static final Pattern VENDOR_AND_DEVICE = Pattern.compile(
            "VEN_([0-9A-F]{4})&DEV_([0-9A-F]{4})", Pattern.CASE_INSENSITIVE);
            //VEN_1002&DEV_145A
    private static final Pattern CLASS_CODE =
            Pattern.compile("CC_([0-9A-F]{4,6})", Pattern.CASE_INSENSITIVE);
    private static final Pattern LOCATION =
            Pattern.compile("\\((\\d+),\\s*(\\d+),\\s*(\\d+)\\)");

    /** Prevents creating an instance of this static helper class. */
    private pciInfo() {}

    /**
     * Reads installed PCI device records from the Windows registry.
     * The registry can include devices that were connected in the past.
     */
    static ArrayList<PCIDevice> getDevices() {
        TreeMap<String, PCIDevice> devices = new TreeMap<>();
        String[] deviceTypes = registryKeys(PCI_REGISTRY_PATH);
        for (String deviceType : deviceTypes) {
            Matcher ids = VENDOR_AND_DEVICE.matcher(deviceType);
            if (!ids.find())
                continue;

            String typePath = PCI_REGISTRY_PATH + "\\" + deviceType;
            for (String instance : registryKeys(typePath)) {
                String instancePath = typePath + "\\" + instance;
                String address = "PCI\\" + deviceType + "\\" + instance;
                String name = readDeviceName(instancePath);
                int[] location = readLocation(instancePath);
                int classCode = readClassCode(instancePath);

                devices.put(address, new PCIDevice(
                        address, name, location[0], location[1], location[2],
                        Integer.parseInt(ids.group(1), 16),
                        Integer.parseInt(ids.group(2), 16),
                        classCode));
            }
        }
        ArrayList<PCIDevice> result = new ArrayList<>(devices.values());
        result.sort(Comparator.comparing(device -> device.address));
        return result;
    }

    /**
     * Lists subkeys of a registry path, returning an empty array when the path is absent.
     */
    private static String[] registryKeys(String path) {
        if (!Advapi32Util.registryKeyExists(WinReg.HKEY_LOCAL_MACHINE, path))
            return new String[0];
        return Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, path);
    }

    /**
     * Reads a friendly name, falling back to the device description stored by Windows.
     */
    private static String readDeviceName(String path) {
        if (Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, path, "FriendlyName"))
            return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, path, "FriendlyName");
        if (!Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, path, "DeviceDesc"))
            return "";

        String description = Advapi32Util.registryGetStringValue(
                WinReg.HKEY_LOCAL_MACHINE, path, "DeviceDesc");
        int separator = description.indexOf(';');
        return separator >= 0 ? description.substring(separator + 1) : description;
    }

    /**
     * Reads and parses bus, device, and function values from optional registry location text.
     */
    private static int[] readLocation(String path) {
        String valueName = "LocationInformation";
        if (!Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, path, valueName))
            return new int[] {-1, -1, -1};
        String location = Advapi32Util.registryGetStringValue(
                WinReg.HKEY_LOCAL_MACHINE, path, valueName);
        Matcher match = LOCATION.matcher(location);
        if (!match.find())
            return new int[] {-1, -1, -1};
        return new int[] {
                Integer.parseInt(match.group(1)),
                Integer.parseInt(match.group(2)),
                Integer.parseInt(match.group(3))
        };
    }

    /**
     * Reads a class code from the registry's compatible-ID list when one is available.
     */
    private static int readClassCode(String path) {
        if (!Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, path, "CompatibleIDs"))
            return -1;
        for (String compatibleId : Advapi32Util.registryGetStringArray(
                WinReg.HKEY_LOCAL_MACHINE, path, "CompatibleIDs")) {
            Matcher match = CLASS_CODE.matcher(compatibleId);
            if (match.find()) {
                int code = Integer.parseInt(match.group(1), 16);
                return match.group(1).length() == 4 ? code << 8 : code;
            }
        }
        return -1;
    }
}
