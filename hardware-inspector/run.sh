#!/usr/bin/env bash
set -e
mkdir -p bin
javac -d bin -sourcepath src:hw-starter/src src/*.java
java -cp bin Main "$@"
