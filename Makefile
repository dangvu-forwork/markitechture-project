# Makefile targets use GNU Make and Linux classpath separators.
ENTRY_SOURCES := $(shell find entry -type f -name '*.java')

.PHONY: build run build-linux run-linux

build:
	javac -cp "jar/*" -d out lib/format.java $(ENTRY_SOURCES)

run: build
	java -cp "out:jar/*" entry.index

build-linux: build

run-linux: build-linux
	java -cp "out:jar/*" entry.linux.demo