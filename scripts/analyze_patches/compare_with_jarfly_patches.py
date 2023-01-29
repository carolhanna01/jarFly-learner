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

        relevantPatches = []

        for f in sorted(os.listdir(args.dir + "../jarfly_patches")):
            if f.startswith(projectNamelower + bug):
                relevantPatches.append(f)
        for f in relevantPatches:
            jarPatch = open(args.dir + "../jarfly_patches/" + f)
            ourPatch = open(args.dir + "/our_patches/" + patch, 'r')

            if bug != prevBug:
                prevBugMatched = False

            ourChangedLines = open(args.dir + "/ourTmp" , 'w')
            theirChangedLines = open(args.dir + "/theirTmp" , 'w')
            resOut = open(args.dir + "/resTmp" , 'w')
            for line in ourPatch.readlines():
                if (line.startswith("+") or line.startswith("-")) and not line.startswith("++") and not line.startswith("--"):
                    ourChangedLines.write(line)

            for line in jarPatch.readlines():
                if (line.startswith("+") or line.startswith("-")) and not line.startswith("++") and not line.startswith("--"):
                    theirChangedLines.write(line)

            p = subprocess.Popen("diff -EZBbw " +  args.dir + "/ourTmp" + " " + args.dir + "/theirTmp"  + " | wc -l > " + args.dir + "/resTmp", shell=True)
            p.wait()
	    readRes = open(args.dir + "/resTmp" , 'r')

            if readRes.readline().strip() == str(0):
                matches += 1
                if not prevBugMatched:
                    uniqueMatches += 1

                prevBugMatched = True

            prevBug = bug

    subprocess.call("echo " + "total matches= " + str(matches), shell=True)
    subprocess.call("echo " + "unique matches= " + str(uniqueMatches), shell=True)

