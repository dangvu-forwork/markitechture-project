package entry;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import lib.HtmlReport;
import lib.format.SystemInfo;

/** Starts the collector and chooses between an HTML report and terminal JSON. */
public class index {
    private static final String USAGE =
            "Usage: java entry.index [--output <path>] [--no-open] | --json | --help";

    /** Java passes the words after the class name into the args array. */
    public static void main(String[] args) {
        try {
            run(args);
        } catch (Exception error) {
            // A non-zero exit code tells a terminal or script that the command failed.
            System.err.println("System Inspector: " + error.getMessage());
            System.exit(1);
        }
    }

    private static void run(String[] args) throws IOException {
        Path outputPath = Path.of("index.html");
        boolean openBrowser = true;
        boolean jsonOutput = false;
        boolean customOutput = false;

        // Read options before collecting hardware, so invalid commands fail quickly.
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--output":
                    boolean missingPath = i + 1 == args.length;
                    if (customOutput || missingPath) {
                        throw new IllegalArgumentException(USAGE);
                    }
                    String filename = args[i + 1];
                    if (filename.startsWith("--") || filename.isBlank()) {
                        throw new IllegalArgumentException(USAGE);
                    }
                    outputPath = Path.of(filename);
                    customOutput = true;
                    i++; // The filename belongs to --output; skip it on the next loop.
                    break;
                case "--no-open":
                    openBrowser = false;
                    break;
                case "--json":
                    jsonOutput = true;
                    break;
                case "--help":
                    System.out.println(USAGE);
                    return;
                default:
                    throw new IllegalArgumentException("Unknown option: " + args[i] + "\n" + USAGE);
            }
        }

        if (jsonOutput && (customOutput || !openBrowser)) {
            throw new IllegalArgumentException("--json cannot be combined with HTML options.\n" + USAGE);
        }

        String operatingSystem = System.getProperty("os.name");
        SystemInfo system = selectCollector(operatingSystem);
        if (jsonOutput) {
            System.out.println(system.readInfoJson());
            return;
        }

        String html = HtmlReport.render(system, operatingSystem);
        // An absolute path is easier to find and can be converted into a browser URL.
        outputPath = outputPath.toAbsolutePath().normalize();
        try {
            // UTF-8 supports device names containing accents or non-English letters.
            // writeString creates the file, or replaces its contents if it exists.
            Files.writeString(outputPath, html, StandardCharsets.UTF_8);
        } catch (IOException error) {
            throw new IOException("Cannot write report to " + outputPath + ": " + error.getMessage(), error);
        }
        System.out.println("Report saved to " + outputPath);

        if (openBrowser) {
            openReport(outputPath);
        }
    }

    /** Both platform collectors implement the same shared SystemInfo base class. */
    private static SystemInfo selectCollector(String operatingSystem) {
        String normalizedName = operatingSystem.toLowerCase(Locale.ROOT);
        if (normalizedName.contains("linux")) {
            return entry.linux.demo.readSystemInfo();
        } else if (normalizedName.contains("windows")) {
            return entry.windows.demo.readSystemInfo();
        }
        throw new UnsupportedOperationException(
                "Operating system behavior for " + operatingSystem + " not implemented.");
    }

    /** Opening is optional: the report is already saved even if no browser exists. */
    private static void openReport(Path outputPath) {
        try {
            if (!Desktop.isDesktopSupported()
                    || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                throw new UnsupportedOperationException("Desktop browsing is unavailable");
            }
            Desktop.getDesktop().browse(outputPath.toUri());
        } catch (Exception error) {
            System.err.println("Open the saved report manually in your browser. Automatic opening failed: "
                    + error.getMessage());
        }
    }
}