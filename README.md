# markitechture project

There are a few things to note before contributing to this repository. Please keep them in mind guys

1/ Do git rebase before doing git push. <br>
2/ Preferrably just use Pull Requests before merging changes into main. You MAY push to main, but it's **highly unrecommended** since everyone will have to rebase their code before pushing. <br>
3/ Do your work on a separate branch, push it to github and then open a PR. Someone will be there to review your code and make sure it works on their system. <br>

That's it basically <br>

## how 2 run

The Makefile uses GNU Make, a JDK, and the Gson JAR in `jar/`. Its source list
automatically discovers Java files under `entry/`, including both `entry/linux`
and `entry/windows`.

From the project root on Linux:

```sh
make build       # Compile the project
make run         # Run entry.index; it selects behavior based on the OS
make build-linux # Compile the project
make run-linux   # Run entry.linux.demo directly
```

The Makefile uses Linux shell and classpath syntax. On Windows, compile and run
with PowerShell instead:

```powershell
javac -cp "jar\*" -d out lib\format.java entry\index.java entry\linux\*.java
java -cp "out;jar\*" entry.index
```

Run commands from the project root so the `ids` directory is available.

## The ID files

`ids/pci.ids` is the PCI ID database (version 2026.10.12) from
https://pci-ids.ucw.cz/. `ids/usb.ids` is the USB ID list (version 2026.06.26)
from https://github.com/vcrhonek/hwdata. Both are distributed under the GNU
General Public License (version 2 or later) or the 3-clause BSD licence. Newer
copies can be downloaded from those sites.
