import tarfile
import os
import sys
import argparse
import subprocess

if __name__ == '__main__':


    parser = argparse.ArgumentParser()

    parser.add_argument('--dir', dest='dir')

    args = parser.parse_args()

    projectName = args.dir.split("PatchedBugs_")[1].strip()
    matches = 0
    uniqueMatches = 0
    prevBug = ""
    prevBugMatched = False			
    for patch in sorted(os.listdir(args.dir + "/our_patches")):

        ourPatch = open(args.dir + "/our_patches/" + patch, 'r')
        bug = patch.split(".")[0].strip()
	seed = patch.split(".")[1].strip()
        devPatch = open(args.dir + "/../framework/projects/" + projectName + "/patches/" + bug + ".src.patch")

	if bug != prevBug:
	    prevBugMatched = False
	
	ourChangedLines = open(args.dir + "/ourTmp" , 'w')
	theirChangedLines = open(args.dir + "/theirTmp" , 'w')
	resOut = open(args.dir + "/resTmp" , 'w')

	for line in ourPatch.readlines():
	    if (line.startswith("+") or line.startswith("-")) and not line.startswith("++") and not line.startswith("--"):
		ourChangedLines.write(line)

        for line in devPatch.readlines():
            if (line.startswith("+") or line.startswith("-")) and not line.startswith("++") and not line.startswith("--"):    
		theirChangedLines.write(line)

	subprocess.call("diff -EZbw " +  args.dir + "/ourTmp" + " " + args.dir + "/theirTmp"  + " | wc -l > " + args.dir + "/resTmp", shell=True)
        readRes = open(args.dir + "/resTmp" , 'r')

	if readRes.readline() == 0:
	    matches += 1 
            if not prevBugMatched:
             	uniqueMatches += 1

	    prevBugMatched = True 

        prevBug = bug
	
        #subprocess.call("rm " + args.dir + "/ourTmp"  + " " + args.dir + "/theirTmp"  + " " + args.dir + "/resTmp" , shell=True)
    
    subprocess.call("echo " + "total matches= " + str(matches), shell=True)
    subprocess.call("echo " + "unique matches= " + str(uniqueMatches), shell=True)
	
           
		        
