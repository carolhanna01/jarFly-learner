import tarfile
import os
import sys
import argparse
import subprocess
import re

if __name__ == '__main__':


    parser = argparse.ArgumentParser()

    parser.add_argument('--dir', dest='dir')

    args = parser.parse_args()

    matches = 0
    uniqueMatches = 0
    prevBug = ""
    prevBugMatched = False			
    for patch in sorted(os.listdir(args.dir + "/our_patches")):
        projectNamelower = re.split(r"([1-9])", patch)[0]
        projectName = re.split(r"([1-9])", patch)[0].title()
        bug = patch.split(".")[0].strip().replace(projectNamelower, "")
        seed = patch.split(".")[1].strip()

        ourPatch = open(args.dir + "/our_patches/" + patch, 'r')
        devPatch = open("/home/channa/epsilon_base/jarFly-learner/tests/defects4j/framework/projects/" + projectName + "/patches/" + bug + ".src.patch")

	if bug != prevBug:
	    prevBugMatched = False
	
	ourChangedLines = open(args.dir + "/ourTmp" , 'w')
	theirChangedLines = open(args.dir + "/theirTmp" , 'w')
	resOut = open(args.dir + "/resTmp" , 'w')

	print("USSSSSSSS")
	for line in ourPatch.readlines():
	    if (line.startswith("+") or line.startswith("-")) and not line.startswith("++") and not line.startswith("--"):
		ourChangedLines.write(line)
		print(line)

	print("DEVVVVVVV")
        for line in devPatch.readlines():
            if (line.startswith("+") or line.startswith("-")) and not line.startswith("++") and not line.startswith("--"):    
		theirChangedLines.write(line)
                print(line)

	subprocess.call("diff -EZBbw --ignore-all-space --strip-trailing-cr --ignore-tab-expansion --ignore-blank-lines --ignore-trailing-space " +  args.dir + "/ourTmp" + " " + args.dir + "/theirTmp"  + " | wc -l > " + args.dir + "/resTmp", shell=True)
        readRes = open(args.dir + "/resTmp" , 'r')

	if readRes.readline().strip() == str(0):
	    matches += 1 
            if not prevBugMatched:
             	uniqueMatches += 1

	    prevBugMatched = True 

        prevBug = bug
	
        #subprocess.call("rm " + args.dir + "/ourTmp"  + " " + args.dir + "/theirTmp"  + " " + args.dir + "/resTmp" , shell=True)
    
    subprocess.call("echo " + "total matches= " + str(matches), shell=True)
    subprocess.call("echo " + "unique matches= " + str(uniqueMatches), shell=True)
	
           
		        
