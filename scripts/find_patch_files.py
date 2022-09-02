import tarfile
import os
import sys
import argparse
import subprocess

if __name__ == '__main__':


    parser = argparse.ArgumentParser()

    parser.add_argument('--dir', dest='dir')

    args = parser.parse_args()

    if os.path.isdir(args.dir + "/" + "our_patches"):
        subprocess.call("rm -rf " + args.dir + "/" + "our_patches", shell=True)

    os.mkdir(args.dir + "/" + "our_patches")
    projectName = args.dir.split("PatchedBugs_")[1].strip().lower()
    for bug in os.listdir(args.dir):

        for file in os.listdir(args.dir + "/" + bug):

            if file.startswith("log"):

                handler = open(args.dir + "/" + bug + "/" + file, 'r')
                seed = file.split("Seed")[1].split(".")[0].strip()
                for line in handler.readlines():
                    if line.startswith("Repair Found"):

			currVar = int(line.split("(in variant")[1].split(")")[0].strip())
                        variantsOutDir = args.dir + "/" + bug + "/" + seed
                        if os.path.isdir(variantsOutDir):
			    subprocess.call("rm -rf " + variantsOutDir, shell=True)
			os.mkdir(variantsOutDir)
                        curr_tar = tarfile.open(args.dir + "/" + bug + "/" + "variants" + bug.split("Buggy")[0].title() + "Seed" + seed + ".tar")
                        curr_tar.extractall(variantsOutDir)
                        curr_tar.close()

                        subprocess.call("find " +  variantsOutDir + "/scratch0/channa/$(ls  "+ variantsOutDir + "/scratch0/channa/" + " | head -n 1)/jarFly-learner/tests/defects4j/patched/" + bug + "/tmp/" + "original | grep java >> tmp" , shell=True)
                        subprocess.call("find " +  variantsOutDir + "/scratch0/channa/$(ls  "+ variantsOutDir + "/scratch0/channa/" + " | head -n 1)/jarFly-learner/tests/defects4j/patched/" + bug + "/tmp/" + "variant" + str(currVar)  + " | grep java >> tmp" , shell=True)
			f = open("./tmp", "r")
			origLine = f.readline().strip()
                        varLine = f.readline().strip()
			bugNum = origLine.split(projectName)[1].split("Buggy/")[0].strip()
                        dstPatch = args.dir + "/" + "our_patches" + "/" + bugNum + "." + seed + ".patch"
		        
			subprocess.call("chmod 777 " + origLine + " " + varLine, shell=True)	
			subprocess.call("diff -u " + origLine + " " + varLine + " > " + dstPatch, shell=True)

			subprocess.call("rm -rf " + variantsOutDir, shell=True)
			subprocess.call("rm tmp", shell=True)
			subprocess.call("dos2unix " + args.dir + "/" + "our_patches/*", shell=True)

