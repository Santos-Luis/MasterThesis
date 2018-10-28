#!/usr/bin/env python

import sys

inFile = open(sys.argv[1],"r")
outFile = open(sys.argv[2],"w")

for line in inFile:
    tokens = line.split("\t")
    if(tokens[1].startswith("fb:people.person.profession")):
      outFile.write(tokens[0]+"\t("+tokens[1]+")\t"+tokens[2]+"\t"+tokens[3])
    else:
      outFile.write(tokens[0]+"\t(fb:type.object.type "+tokens[1]+")\t"+tokens[2]+"\t"+tokens[3])
inFile.close()
outFile.close()


