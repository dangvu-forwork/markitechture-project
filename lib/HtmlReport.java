package lib;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import lib.format.CoreInfo;
import lib.format.MemoryInfo;
import lib.format.PCIDevice;
import lib.format.SystemInfo;
import lib.format.USBDevice;

/** Offline HTML presentation, independent of platform collectors. */
public final class HtmlReport {
    private HtmlReport() {}

    /** Collects one snapshot and builds a complete page that needs no server. */
    public static String render(SystemInfo system, String os) {
        // Collect each section once; rendering never re-reads live hardware.
        String processor = system.getProcessorModel();
        MemoryInfo memory = system.getMemoryInfo(); // shared default: KiB
        ArrayList<CoreInfo> cores = system.getCoreInfos();
        ArrayList<PCIDevice> pci = system.getPCIDevices();
        ArrayList<USBDevice> usb = system.getUSBDevices();
        String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM uuuu, HH:mm:ss z", Locale.ROOT));
        StringBuilder html = new StringBuilder(HEADER);
        html.append("<header><div class='eyebrow'>MARKITECTURE / SYSTEM INSPECTOR</div><h1>Your machine.<br><span>At a glance.</span></h1><p>")
            .append(escape(os)).append(" &middot; ").append(escape(System.getProperty("os.arch")))
            .append("</p><p class='muted'>Snapshot collected ").append(escape(timestamp)).append("</p></header>");
        // The four cards give a quick overview before the detailed tables.
        html.append("<div class='summary'>");
        appendSummaryCard(html, "PROCESSOR", processor, cores.size() + " logical processors");
        appendSummaryCard(html, "PHYSICAL MEMORY", formatMemory(memory.totalMemory), formatMemory(memory.usedMemory) + " in use");
        appendSummaryCard(html, "PCI DEVICES", Integer.toString(pci.size()), "Hardware inventory");
        appendSummaryCard(html, "USB DEVICES", Integer.toString(usb.size()), "Devices reported by the OS");
        html.append("</div><section><h2>Memory overview</h2>");
        // Only calculate a percentage when both readings are valid; avoid dividing by zero.
        boolean valid = Double.isFinite(memory.totalMemory) && Double.isFinite(memory.usedMemory)
                && memory.totalMemory > 0 && memory.usedMemory >= 0 && memory.usedMemory <= memory.totalMemory;
        if (valid) {
            String percent = String.format(Locale.ROOT, "%.1f", 100 * memory.usedMemory / memory.totalMemory);
            html.append("<p>").append(percent).append("% used &middot; ").append(formatMemory(memory.totalMemory - memory.usedMemory))
                .append(" available</p><progress aria-label='Memory used' max='100' value='").append(percent).append("'>")
                .append(percent).append("%</progress>");
        } else {
            html.append("<p>Memory usage unavailable.</p>");
        }
        html.append("</section><section><h2>Processor</h2><p>").append(escape(processor))
            .append("</p><details><summary>Cumulative counters by logical processor</summary><p class='muted'>Busy and idle counters since boot, not current load percentages. Counter units depend on the operating system.</p>");
        startTable(html, "Logical processor", "Busy counter", "Idle counter");
        for (int i = 0; i < cores.size(); i++) {
            CoreInfo core = cores.get(i);
            appendRow(html, "Core " + i, formatNumber(core.busy), formatNumber(core.idle));
        }
        endTable(html, cores.size(), 3, "No processor counters available.");
        html.append("</details></section>");
        startDeviceSection(html, "PCI devices", "pci-search");
        // Windows reads PCI records from the registry, which can retain old devices.
        if (os.toLowerCase(Locale.ROOT).contains("windows")) {
            html.append("<p class='muted'>Windows registry records can include previously connected devices. Location details may be unavailable.</p>");
        }
        startTable(html, "Device", "Address", "Vendor ID", "Device ID", "Class", "Bus / Device / Function");
        for (PCIDevice device : pci) {
            String location = formatNumber(device.bus) + " / " + formatNumber(device.device)
                    + " / " + formatNumber(device.function);
            appendRow(html, fallback(device.name, device.address), device.address,
                    formatHexId(device.vendorId, 4), formatHexId(device.deviceId, 4),
                    formatHexId(device.classCode, 6), location);
        }
        endTable(html, pci.size(), 6, "No PCI devices reported.");
        html.append("<p class='no-matches' hidden>No matching devices.</p></section>");
        startDeviceSection(html, "USB devices", "usb-search");
        startTable(html, "Product", "Manufacturer", "Vendor ID", "Product ID", "Bus", "Device");
        for (USBDevice device : usb) {
            appendRow(html, fallback(device.product, device.name), device.manufacturer,
                    formatHexId(device.vendorId, 4), formatHexId(device.productId, 4),
                    formatNumber(device.busNumber), formatNumber(device.deviceNumber));
        }
        endTable(html, usb.size(), 6, "No USB devices reported.");
        html.append("<p class='no-matches' hidden>No matching devices.</p></section><footer>Generated locally &middot; Offline snapshot &middot; Run System Inspector again to refresh</footer></main>");
        html.append(SCRIPT).append("</body></html>");
        return html.toString();
    }

    /**
     * Treat device names as text, never as HTML code. Replace ampersands first so
     * the ampersands introduced by later replacements are not escaped twice.
     */
    public static String escape(String value) {
        if (value == null || value.isBlank()) {
            return "Unknown";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /** Use an address or system name if a friendly device name is missing. */
    private static String fallback(String value, String alternative) {
        if (value == null || value.isBlank()) {
            return alternative;
        }
        return value;
    }

    /** Collectors use negative numbers to mean that a reading is unavailable. */
    private static String formatNumber(long value) {
        if (value < 0) {
            return "Unknown";
        }
        return Long.toString(value);
    }

    /** For example, vendor 32902 becomes 0x8086; class IDs use six hex digits. */
    private static String formatHexId(int value, int width) {
        if (value < 0) {
            return "Unknown";
        }
        return String.format(Locale.ROOT, "0x%0" + width + "x", value);
    }

    /** The shared collector returns KiB. Display larger amounts in MiB or GiB. */
    private static String formatMemory(double kibibytes) {
        if (!Double.isFinite(kibibytes) || kibibytes < 0) {
            return "Unknown";
        }
        double kibibytesPerGiB = 1024.0 * 1024;
        if (kibibytes >= kibibytesPerGiB) {
            return String.format(Locale.ROOT, "%.2f GiB", kibibytes / kibibytesPerGiB);
        }
        return String.format(Locale.ROOT, "%.2f MiB", kibibytes / 1024);
    }

    /** Reuse one card layout for all four summary values. */
    private static void appendSummaryCard(StringBuilder html, String label, String value, String detail) {
        html.append("<article><div class='eyebrow'>").append(escape(label))
                .append("</div><h2>").append(escape(value))
                .append("</h2><p class='muted'>").append(escape(detail))
                .append("</p></article>");
    }

    /** Each device section gets its own labelled search box. */
    private static void startDeviceSection(StringBuilder html, String title, String id) {
        html.append("<section class='devices'><div class='section-head'><h2>")
                .append(escape(title)).append("</h2><div class='search'><label for='")
                .append(escape(id)).append("'>Search ").append(escape(title))
                .append("</label><input type='search' id='").append(escape(id))
                .append("' placeholder='Name, manufacturer or ID'></div></div>");
    }

    /** String... accepts any number of column headings as separate arguments. */
    private static void startTable(StringBuilder html, String... headings) {
        html.append("<div class='table-wrap' tabindex='0' role='region' aria-label='")
                .append(escape(headings[0])).append(" table'><table><thead><tr>");
        for (String heading : headings) {
            html.append("<th scope='col'>").append(escape(heading)).append("</th>");
        }
        html.append("</tr></thead><tbody>");
    }

    /** Escape every cell here so callers cannot accidentally insert unsafe text. */
    private static void appendRow(StringBuilder html, String... values) {
        html.append("<tr class='data-row'>");
        for (String value : values) {
            html.append("<td>").append(escape(value)).append("</td>");
        }
        html.append("</tr>");
    }

    /** If no readings exist, show a message spanning all columns of the table. */
    private static void endTable(StringBuilder html, int count, int columns, String emptyMessage) {
        if (count == 0) {
            html.append("<tr><td colspan='").append(columns).append("'>")
                    .append(escape(emptyMessage)).append("</td></tr>");
        }
        html.append("</tbody></table></div>");
    }

    // Java text blocks (three double quotes) keep HTML/CSS inside the Java source.
    // This makes the generated report portable: no separate stylesheet is needed.
    private static final String HEADER = """
        <!doctype html><html lang="en"><head><meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Markitecture | System Inspector</title><style>
        /* Colour variables are reused throughout the dashboard. */
        :root{
            color-scheme:dark;
            --bg:#0c111b;
            --panel:#141d2b;
            --line:#2b394e;
            --text:#e6edf7;
            --muted:#a5b4ca;
            --accent:#74e2c2
        }
        *{
            box-sizing:border-box
        }
        body{
            margin:0;
            background:var(--bg);
            color:var(--text);
            font:15px/1.6 system-ui,sans-serif
        }
        main{
            max-width:1400px;
            margin:auto;
            padding:56px 32px
        }
        header{
            margin-bottom:36px
        }
        h1{
            font-size:clamp(36px,6vw,66px);
            line-height:1.08;
            letter-spacing:-.045em;
            margin:20px 0
        }
        h1 span{
            color:var(--accent)
        }
        h2{
            font-size:21px;
            line-height:1.4;
            margin:0 0 12px;
            overflow-wrap:anywhere
        }
        p{
            margin:8px 0
        }
        .eyebrow{
            font-size:11px;
            letter-spacing:.16em;
            color:var(--accent);
            font-weight:700
        }
        .muted,footer{
            color:var(--muted)
        }
        .summary{
            display:grid;
            grid-template-columns:2fr 1.3fr 1fr 1fr;
            gap:16px
        }
        article,section{
            background:var(--panel);
            border:1px solid var(--line);
            border-radius:16px;
            padding:24px;
            min-width:0
        }
        article h2{
            margin-top:18px
        }
        section{
            margin-top:20px
        }
        progress{
            display:block;
            width:100%;
            height:18px;
            margin-top:20px;
            accent-color:var(--accent)
        }
        progress::-webkit-progress-bar{
            background:var(--line);
            border-radius:8px
        }
        progress::-webkit-progress-value{
            background:var(--accent);
            border-radius:8px
        }
        .section-head{
            display:flex;
            justify-content:space-between;
            gap:20px;
            align-items:center
        }
        .search label{
            display:block;
            font-size:12px;
            color:var(--muted)
        }
        input{
            font:inherit;
            background:var(--bg);
            border:1px solid var(--line);
            border-radius:8px;
            padding:9px 12px;
            color:var(--text);
            max-width:100%;
            width:280px
        }
        input:focus-visible,summary:focus-visible,.table-wrap:focus-visible{
            outline:2px solid var(--accent);
            outline-offset:3px
        }
        .table-wrap{
            overflow-x:auto;
            margin-top:18px
        }
        table{
            border-collapse:collapse;
            width:100%;
            text-align:left;
            font-size:13px
        }
        th{
            color:var(--muted);
            font-size:11px;
            text-transform:uppercase;
            letter-spacing:.07em
        }
        td,th{
            padding:13px 12px;
            border-bottom:1px solid var(--line);
            vertical-align:top
        }
        td{
            overflow-wrap:anywhere;
            max-width:380px
        }
        summary{
            cursor:pointer;
            color:var(--accent);
            padding:8px 0
        }
        footer{
            font-size:12px;
            text-align:center;
            margin-top:36px
        }
        [hidden]{
            display:none!important
        }
        @media(max-width:1000px){
            .summary{
                grid-template-columns:1fr 1fr
            }
        }
        @media(max-width:600px){
            main{
                padding:28px 16px
            }
            .summary{
                grid-template-columns:1fr
            }
            .section-head{
                display:block
            }
            .search{
                margin-top:16px
            }
            article,section{
                padding:18px
            }
            input{
                width:100%
            }
        }
        @media print{
            :root{
                color-scheme:light;
                --bg:white;
                --panel:white;
                --text:#111;
                --muted:#444;
                --line:#ccc;
                --accent:#175d4b
            }
            main{
                padding:0;
                max-width:none
            }
            h1{
                font-size:32px
            }
            .search{
                display:none
            }
            .summary{
                grid-template-columns:1fr 1fr
            }
            .table-wrap{
                overflow:visible
            }
            table{
                table-layout:fixed;
                font-size:10px
            }
            td,th{
                padding:6px
            }
            article{
                break-inside:avoid
            }
            section{
                border-radius:0
            }
            tr{
                break-inside:avoid
            }
        }
        </style></head><body><main>
        """;
    // JavaScript runs in the browser after Java has finished writing the file.
    private static final String SCRIPT = """
        <script>
        // Search only the table in the section containing the changed input.
        const deviceSections = document.querySelectorAll('.devices');
        for (const section of deviceSections) {
            const searchInput = section.querySelector('input');
            searchInput.addEventListener('input', function () {
                const query = searchInput.value.trim().toLocaleLowerCase();
                const rows = section.querySelectorAll('.data-row');
                let matchingRows = 0;

                for (const row of rows) {
                    const rowText = row.textContent.toLocaleLowerCase();
                    const matches = rowText.includes(query);
                    row.hidden = !matches;
                    if (matches) {
                        matchingRows++;
                    }
                }

                // Empty inventories already have their own "No devices" message.
                const message = section.querySelector('.no-matches');
                message.hidden = rows.length === 0 || matchingRows > 0;
            });
        }

        // Include collapsed CPU details when printing, then restore their state.
        let savedDetailStates = [];
        window.addEventListener('beforeprint', function () {
            savedDetailStates = [];
            for (const details of document.querySelectorAll('details')) {
                savedDetailStates.push({element: details, wasOpen: details.open});
                details.open = true;
            }
        });
        window.addEventListener('afterprint', function () {
            for (const savedState of savedDetailStates) {
                savedState.element.open = savedState.wasOpen;
            }
        });
        </script>
        """;
}