import subprocess
import os
import sys
import argparse

if __name__ == '__main__':
    
    
    parser = argparse.ArgumentParser()
    parser.add_argument('javaVM')
    parser.add_argument('experiment')

    args = parser.parse_args()

    f = open("./d4j_v1.1.0_patched_bugs", "r")
    for line in f.readlines():
        project = line.split(" ")[0].strip()
        bug = line.split(" ")[1].strip()
        print(f"./prepare_bug.sh {project} {bug} humanMade 1 ExamplesCheckedOut {args.javaVM} {args.javaVM}  \"false\" ./ \"false\" ./")
        os.system(f"./prepare_bug.sh {project} {bug} humanMade 1 ExamplesCheckedOut {args.javaVM} {args.javaVM}  \"false\" ./ \"false\" ./")
        os.system(f"./repair_bug.sh {project} {bug} allHuman 1 ExamplesCheckedOut 1 20 false {args.javaVM} {args.javaVM}  false \"\" false \"\" {args.experiment}")
