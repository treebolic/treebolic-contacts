#!/bin/bash

apps="treebolicContacts"

for a in $apps; do
	echo "*** $a"
	./gradlew :${a}:assembleRelease
done
