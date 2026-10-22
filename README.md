# markitechture project

There are a few things to note before contributing to this repository. Please keep them in mind guys

1/ Do git rebase before doing git push. <br>
2/ Preferrably just use Pull Requests before merging changes into main. You MAY push to main, but it's **highly unrecommended** since everyone will have to rebase their code before pushing. <br>
3/ Do your work on a separate branch, push it to github and then open a PR. Someone will be there to review your code and make sure it works on their system. <br>

That's it basically <br>

## how 2 run

Well there's the Makefile behavior and the standalone behavior.
The Makefile behavior is for those who have access to a Linux system
Here are the following commands you can run with Makefile

```sh
make build       # Compile the project
make run         # Run entry.index; it selects behavior based on the OS
make build-linux # Compile the project... Literally identical to make build for now but maybe this will find its use
make run-linux   # Run entry.linux.demo directly, without regard for your current OS
```

If you don't have a Linux system, you can also run these commands in a Windows type system (for building)
```powershell
New-Item -ItemType Directory -Force out | Out-Null
$sources = Get-ChildItem entry -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -cp "jar\*" -d out lib\format.java $sources
```

Yes, a clanker wrote the powershell commands above.
If you want to run the OS Interpreter / directly run an OS behavior, just copy the code used to run them in Makefile and paste it in terminal

## The ID files

`ids/pci.ids` is the PCI ID database (version 2026.10.12) from
https://pci-ids.ucw.cz/. `ids/usb.ids` is the USB ID list (version 2026.06.26)
from https://github.com/vcrhonek/hwdata. Both are distributed under the GNU
General Public License (version 2 or later) or the 3-clause BSD licence. Newer
copies can be downloaded from those sites.
