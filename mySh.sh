#!/bin/bash

unameOut="$(uname -s)"
case "${unameOut}" in
    Linux*)     EXEC=xdg-open;;
    Darwin*)    EXEC=open;;
    CYGWIN*)    EXEC=cygstart;;
esac

rm -rf classes
rm -rf libsempre
ant -buildfile build.xml

sleep 1 && $EXEC "Intro.html"

if [ -z "$1" ]; then
	./shrdlurn/runFloat
else
	./shrdlurn/runFloat $1
fi



