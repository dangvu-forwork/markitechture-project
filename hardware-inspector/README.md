# Hardware Inspector (CS4421)

A self-contained Java program that reports the processor, memory, PCI and USB
devices of a Linux machine, using the text files Linux keeps in `/proc` and
`/sys`. It follows the five core requirements of the project brief.

This folder is independent of the rest of this repository: it has its own
`Makefile`, its own `src/`, and a copy of the course starter classes in
`hw-starter/src/`, so it builds and runs without the `entry/` and `lib/` code.

## Build and run

Run these from inside this folder:

```sh
make        # compile into bin/
make run    # run the report
```

or, without make:

```sh
./run.sh
```

`ids/pci.ids` and `ids/usb.ids` are read from the folder you start the program
in. If they are missing, the report prints the raw vendor and device numbers
instead of names.

## Files

| Path | What it does |
| --- | --- |
| `src/Main.java` | Runs the four reports in brief order |
| `src/ProcessorInfo.java` | Requirement 1: model, core count, per-core load |
| `src/MemoryInfo.java` | Requirement 2: total, used and available memory |
| `src/PciDevices.java` | Requirements 3 and 5: PCI devices with names |
| `src/UsbDevices.java` | Requirements 4 and 5: USB devices with names |
| `hw-starter/src/` | Course starter classes (`cpuInfo`, `memInfo`, `pciInfo`, `usbInfo`, `idLookup`, `sysFile`) |
| `ids/` | The PCI and USB ID databases |

Requirement 6 (sorting devices and summarising them by vendor and class) is
not implemented yet.

## Testing without Linux

`sysFile` reads from `-Dhw.root=<folder>` instead of the real root, so the
report can be tested against a copied `/proc` and `/sys` tree:

```sh
java -Dhw.root=/path/to/fake-root -cp bin Main
```
