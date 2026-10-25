# markitechture project

There are a few things to note before contributing to this repository. Please keep them in mind guys

1/ Do git rebase before doing git push. <br>
2/ Preferrably just use Pull Requests before merging changes into main. You MAY push to main, but it's **highly unrecommended** since everyone will have to rebase their code before pushing. <br>
3/ Do your work on a separate branch, push it to github and then open a PR. Someone will be there to review your code and make sure it works on their system. <br>

That's it basically <br>

## how 2 run

The Makefile uses GNU Make and Linux classpath syntax. `javac` follows the entry
point's imports and discovers the required source files under `entry/` with
`-sourcepath`, so no shell command is needed to build a source-file list:

```sh
make build       # Compile the project
make run         # Run entry.index; it selects behavior based on the OS
make build-linux # Compile the project
make run-linux   # Run entry.linux.demo directly
```

To compile with `javac` and run with `java` from the project root on any OS:

Linux commands
```sh
javac -cp "jar/*" -sourcepath . -d out lib/format.java entry/index.java
java -cp "out:jar/*" entry.index
```

Windows commands
```sh
javac -cp "jar/*" -sourcepath . -d out lib/format.java entry/index.java
java -cp "out;jar/*" entry.index
```


The `javac` command is the same across operating systems. For the `java`
command, use `:` between classpath entries on Linux/macOS and `;` on Windows.
All required third-party JARs are picked up from `jar/`.

The Windows collector uses OSHI for processor, memory, and USB information. PCI
device IDs and names are read from the Windows registry through JNA Platform.
The registry may retain records for devices that are no longer connected, and
location details may be unavailable (`-1`). USB bus/device numbers are also
unavailable through OSHI and are reported as `-1`.

Run commands from the project root so the `ids` directory is available.

## The ID files

`ids/pci.ids` is the PCI ID database (version 2026.10.12) from
https://pci-ids.ucw.cz/. `ids/usb.ids` is the USB ID list (version 2026.06.26)
from https://github.com/vcrhonek/hwdata. Both are distributed under the GNU
General Public License (version 2 or later) or the 3-clause BSD licence. Newer
copies can be downloaded from those sites.
