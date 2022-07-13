import subprocess
import os
import sys
import argparse

if __name__ == '__main__':
    
    
    parser = argparse.ArgumentParser()
    parser.add_argument('javaVM')
    parser.add_argument('experiment')
    parser.add_argument('destination')
    args = parser.parse_args()

    f = open("/home/channa/project/jarFly-learner/tests/d4j_gp4j_setup/d4j_v1.1.0_patched_bugs", "r")
    for line in f.readlines():
        project = line.split(" ")[0].strip()
        bug = line.split(" ")[1].strip()
        #rint(f"./prepare_bug.sh {project} {bug} humanMade 1 ExamplesCheckedOut {args.javaVM} {args.javaVM}  \"false\" ./ \"false\" ./")
        #os.system("./prepare_bug.sh {} {} humanMade 100 PatchedBugs {} {}  \"false\" ./ \"false\" ./".format(project, bug, args.javaVM, args.javaVM))
        #outfile = open("./out", "w")
	#outfile.write(out)
	os.system("/home/channa/project/jarFly-learner/tests/d4j_gp4j_setup/repair_bug.sh {} {}  allHuman 100 PatchedBugs 1 20 false {} {}  false \"\" false \"\" {} {}".format(project, bug, args.javaVM, args.javaVM, args.experiment, args.destination))
