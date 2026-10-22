# Makefiles only work for Linux systems for now, but the commands they contain are still useful.
# .PHONY is to declare that build-index, run-index and all of these methods aren't based off files.
.PHONY: build-index run-index build-linux run-linux

build:
	javac -cp "jar/*" -d out lib/format.java entry/index.java entry/linux/*.java

run: build
	java -cp "out:jar/*" entry.index

build-linux:
	javac -cp "jar/*" -d out lib/format.java entry/linux/*.java

run-linux: build-linux
	java -cp "out:jar/*" entry.linux.demo