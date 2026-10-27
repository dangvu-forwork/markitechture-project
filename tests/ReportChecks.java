import java.nio.file.*;
import java.util.*;
import lib.HtmlReport;
import lib.format.*;

/** Standalone checks: check() throws on failure, so the Java -ea flag is not required. */
public class ReportChecks {
    static void check(boolean ok, String message) {
        if (!ok) {
            throw new AssertionError(message);
        }
    }
    public static void main(String[] args) throws Exception {
        // A fixture is a small set of fake input files with predictable values.
        // Hyphens replace colons in the PCI address because Windows disallows
        // colons in filenames. The bus/device/function character positions match.
        Path root = Path.of("report-checks", "linux-fixture").toAbsolutePath();
        Path proc = Files.createDirectories(root.resolve("proc"));
        Path device = Files.createDirectories(root.resolve("sys/bus/pci/devices/0000-00-02.0"));
        Files.writeString(proc.resolve("cpuinfo"), "processor : 0\nmodel name : Test <CPU>\n");
        Files.writeString(proc.resolve("stat"), "cpu0 10 0 20 30 4 1 2 0\n");
        Files.writeString(proc.resolve("meminfo"), "MemTotal: 1537 kB\nMemAvailable: 512 kB\n");
        Files.writeString(device.resolve("vendor"), "0x8086");
        Files.writeString(device.resolve("device"), "0x1237");
        Files.writeString(device.resolve("class"), "0x060000");
        // Set this BEFORE loading Linux collectors: their paths are static fields.
        System.setProperty("hw.root", root.toString().replace('\\', '/'));
        SystemInfo linux = entry.linux.demo.readSystemInfo();
        for (long unit : new long[]{ByteUnits.B, ByteUnits.KiB, ByteUnits.MiB, ByteUnits.GiB}) {
            MemoryInfo m = linux.getMemoryInfo(unit);
            check(Math.abs(m.totalMemory - 1537.0 * 1024 / unit) < 1e-10, "Total conversion " + unit);
            check(Math.abs(m.usedMemory - 1025.0 * 1024 / unit) < 1e-10, "Used conversion " + unit);
        }
        check(linux.getPCIDevices().get(0).name.contains("440FX"), "PCI database name");
        check(linux.getCoreInfos().get(0).busy == 33, "CPU counters");
        String html = HtmlReport.render(linux, "Linux");
        check(html.contains("Test &lt;CPU&gt;"), "HTML escaping");
        check(html.contains("No USB devices reported."), "Empty USB list");
        // Change the fixture to exercise fallback behavior for an unknown device.
        Files.writeString(device.resolve("device"), "0xffff");
        check(linux.getPCIDevices().get(0).name.equals("0000-00-02.0"), "PCI address fallback");
        Files.writeString(proc.resolve("meminfo"), "");
        for (long unit : new long[]{1, 1024, 1048576, 1073741824}) {
            check(linux.getMemoryInfo(unit).totalMemory == -1, "Unknown total");
            check(linux.getMemoryInfo(unit).usedMemory == -1, "Unknown used");
        }
        check(HtmlReport.render(linux, "Linux").contains("Memory usage unavailable."), "Unknown memory display");
        check(HtmlReport.escape("<&\"'>").equals("&lt;&amp;&quot;&#39;&gt;"), "All HTML delimiters");
        check(HtmlReport.escape(null).equals("Unknown"), "Null text");
        try {
            linux.getMemoryInfo(0);
            throw new AssertionError("Unit validation");
        } catch (IllegalArgumentException expected) {
            // Passing zero should be rejected before any division happens.
        }
        // This fake collector supplies unsafe-looking text and counts calls.
        // No real hardware is needed to test escaping or single collection.
        final int[] calls = new int[5];
        SystemInfo hostile = new SystemInfo() {
            @Override
            public String getProcessorModel() {
                calls[0]++;
                return "<script>alert(1)</script>";
            }

            @Override
            public MemoryInfo getMemoryInfo() {
                calls[1]++;
                return new MemoryInfo(1024, 512);
            }

            @Override
            public MemoryInfo getMemoryInfo(long unit) {
                return getMemoryInfo();
            }

            @Override
            public ArrayList<CoreInfo> getCoreInfos() {
                calls[2]++;
                return new ArrayList<>();
            }

            @Override
            public ArrayList<PCIDevice> getPCIDevices() {
                calls[3]++;
                PCIDevice device = new PCIDevice("<address>", "<PCI>", -1, -1, -1, -1, -1, -1);
                return new ArrayList<>(List.of(device));
            }

            @Override
            public ArrayList<USBDevice> getUSBDevices() {
                calls[4]++;
                USBDevice device = new USBDevice("name", -1, -1, "<vendor>",
                        "</script><script>alert(2)</script>", -1, -1);
                return new ArrayList<>(List.of(device));
            }
        };
        String escaped = HtmlReport.render(hostile, "Windows <test>");
        check(!escaped.contains("<script>alert"), "Hardware text cannot execute");
        check(escaped.contains("&lt;vendor&gt;") && escaped.contains("&lt;PCI&gt;"), "Device escaping");
        check(escaped.contains("previously connected"), "Windows inventory notice");
        for (int count : calls) {
            check(count == 1, "Collect each section once");
        }
        Files.writeString(Path.of("report-checks", "test-report.html"), escaped);
        System.out.println("Report and Linux collector checks passed.");
    }
}
