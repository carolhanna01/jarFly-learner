import subprocess
import os
import sys
import argparse

if __name__ == '__main__':
    
    
    parser = argparse.ArgumentParser()
    parser.add_argument('javaVM')
    parser.add_argument('experiment')
    args = parser.parse_args()

    for filename in os.listdir("./d4j_v1.1.0_active_bugs"):
        f = open("./d4j_v1.1.0_active_bugs/" + filename, "r")
        for line in f.readlines():
            bug = line.split(",")[0].strip()
            print(f"./prepare_bug.sh {filename} {bug} humanMade 1 ExamplesCheckedOut {args.javaVM} {args.javaVM}  \"false\" ./ \"false\" ./")
            os.system(f"./prepare_bug.sh {filename} {bug} humanMade 1 ExamplesCheckedOut {args.javaVM} {args.javaVM}  \"false\" ./ \"false\" ./")
            os.system(f"./repair_bug.sh {filename} {bug} allHuman 1 ExamplesCheckedOut 1 20 false {args.javaVM} {args.javaVM}  false \"\" false \"\" {args.experiment}")
          
            



            




