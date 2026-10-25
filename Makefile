# Makefile targets use GNU Make and Linux classpath separators.

.PHONY: build run build-linux run-linux

build:
	javac -cp "jar/*" -sourcepath . -d out lib/format.java entry/index.java

run: build
	java -cp "out:jar/*" entry.index

build-linux: build

run-linux: build-linux
	java -cp "out:jar/*" entry.linux.demo