package entry;

import java.util.Locale;

import lib.format.SystemInfo;

public class index {
    public static void main(String[] args) {
        String os = System.getProperty("os.name");
        String normalizedOs = os.toLowerCase(Locale.ROOT);

        SystemInfo currentSys;

        if (normalizedOs.contains("linux")) {
            currentSys = entry.linux.demo.readSystemInfo();
        } else if (normalizedOs.contains("windows")) {
            System.out.println("Windows behavior WIP - dang vu");
        } else {
            System.out.println("Operating system behavior for " + os + " not implemented.");
        }

    }
}
