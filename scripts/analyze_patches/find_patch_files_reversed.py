import re
import tarfile
import os
import sys
import argparse
import subprocess

if __name__ == '__main__':


    parser = argparse.ArgumentParser()

    parser.add_argument('--dir', dest='dir')

    args = parser.parse_args()

    if os.path.isdir(args.dir + "/" + "our_patches_closure"):
        subprocess.call("rm -rf " + args.dir + "/" + "our_patches_closure", shell=True)

    os.mkdir(args.dir + "/" + "our_patches_closure")
    for bug in os.listdir(args.dir):
	#print(bug)
        for file in os.listdir(args.dir + "/" + bug):

            if file.startswith("log"):
        	projectNamelower = re.split(r"([1-9])", file)[0]
                projectName = re.split(r"([1-9])", file)[0].title()
		if projectName != "Logclosure":
			continue
                print(projectName)

        	seed = file.split(".")[1].strip()
                handler = open(args.dir + "/" + bug + "/" + file, 'r')
                seed = file.split("Seed")[1].split(".")[0].strip()
                for line in handler.readlines():
                    if line.startswith("Repair Found"):
			print(file)
			print(line)
			currVar = int(line.split("(in variant")[1].split(")")[0].strip())
                        variantsOutDir = args.dir + "/" + bug + "/" + seed
                        if os.path.isdir(variantsOutDir):
			    subprocess.call("rm -rf " + variantsOutDir, shell=True)
			os.mkdir(variantsOutDir)
                        curr_tar = tarfile.open(args.dir + "/" + bug + "/" + "variants" + bug.split("Buggy")[0].title() + "Seed" + seed + ".tar")
                        curr_tar.extractall(variantsOutDir)
                        curr_tar.close()

                        subprocess.call("find " +  variantsOutDir + "/scratch0/channa/$(ls  "+ variantsOutDir + "/scratch0/channa/" + " | head -n 1)/jarFly-learner/tests/defects4j/patched/" + bug + "/tmp/" + "original | grep '\.java' >> tmp" , shell=True)
                        subprocess.call("find " +  variantsOutDir + "/scratch0/channa/$(ls  "+ variantsOutDir + "/scratch0/channa/" + " | head -n 1)/jarFly-learner/tests/defects4j/patched/" + bug + "/tmp/" + "variant" + str(currVar)  + " | grep '\.java' >> tmp" , shell=True)
			f = open("./tmp", "r")
			origLine = f.readline().strip()
                        varLine = f.readline().strip()
			if origLine == "":
			    subprocess.call("rm -rf " + variantsOutDir, shell=True)
		            subprocess.call("rm tmp", shell=True)
			    continue;	
			bugNum = file.split("Seed")[0].split("log")[1].strip().lower()

			#bugNum = origLine.split(projectName)[1].split("Buggy/")[0].strip()
                        dstPatch = args.dir + "/" + "our_patches_closure" + "/" + bugNum + "." + seed + ".patch"
		        
			subprocess.call("chmod 777 " + origLine + " " + varLine, shell=True)	
			subprocess.call("diff -u " + varLine + " " + origLine + " > " + dstPatch, shell=True)

			subprocess.call("rm -rf " + variantsOutDir, shell=True)
			subprocess.call("rm tmp", shell=True)
    subprocess.call("dos2unix " + args.dir + "/" + "our_patches_closure/*", shell=True)

