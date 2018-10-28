#!/usr/bin/env python

import sys

inFile = open(sys.argv[1],"r")
outFile = open(sys.argv[2],"w")

for line in inFile:
	tokens = line.split("\t")
	if(tokens[1]=="fb:type.object.name"):
		outFile.write(line);
	else:
		name = tokens[2].split(" ")
		if(len(name)>1):
			outFile.write(line)
inFile.close()
outFile.close()


