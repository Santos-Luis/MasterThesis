#!/usr/bin/env python

import sys

inFile = open(sys.argv[1],"r")
outFile = open(sys.argv[2],"w")

for line in inFile:
	outFile.write(line)
	tokens = line.split("\t")
	for i in range(len(tokens)):
		if(i==0):
			outFile.write(tokens[i])
    		elif(i==1):
			outFile.write("\t!"+tokens[i])
		else:
    			outFile.write("\t"+tokens[i])    
inFile.close()
outFile.close()


