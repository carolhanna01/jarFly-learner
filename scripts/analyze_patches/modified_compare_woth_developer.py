import tarfile
import os
import sys
import argparse
import subprocess
import re


def cleanpatch(patch, dev):
  p = patch.splitlines()
  patch = ''
  for line in p:
    l = line.lstrip(' \t\n\r')
    l = l.replace('\t','')
    l = l.replace('\n','')
    l = l.replace('\r','')
    l = l.replace(' ','')
    if l.strip("+-")== "{" or l.strip("+-") == "}": continue
    if not (l.startswith('+') or l.startswith('-')): continue 
    elif l.startswith('+++'): continue
    elif l.startswith('---'): continue
    elif l.strip('-').startswith('//'): continue
    elif l.strip('+').startswith('//'): continue
    elif l.strip('+-').startswith('/*'): continue
    elif l.strip('+-').startswith('*'): continue
    
    elif l.strip('+-').endswith('*/'): continue
 
    elif 'No newline at end of file' in l: continue
    elif len(l.strip('+- \t\n\r'))==0: continue
    else:
     #  if l.startswith("+") and (l.replace("+", "-")  in patch):
#	    patch.replace(l.replace("+", "-") + "\n", "")
#	    continue
#        if l.startswith("-") and (l.replace("-", "+")  in patch):
#            patch.replace(l.replace("-", "+") + "\n", "")
#            continue
	
		
        patch += l+'\n'
  
  return patch

def devFix(projectName):
    for fname in sorted(os.listdir("/home/channa/epsilon_base/jarFly-learner/tests/defects4j/framework/projects/" + projectName + "/patches")):
        if fname.endswith('src.patch'):
                bugnr = fname.split('.')[0]
                bug = projectName+'-'+bugnr
                if projectName+bugnr not in devFixes.keys(): devFixes[projectName+bugnr]=""
                with open("/home/channa/epsilon_base/jarFly-learner/tests/defects4j/framework/projects/" + projectName + "/patches"  +'/'+fname) as f:
                        patch = cleanpatch(f.read(), True)
                        if patch not in devFixes[projectName+bugnr]: devFixes[projectName+bugnr] = patch

if __name__ == '__main__':

    devFixes = {}
    devFix("Chart")
    devFix("Closure")
    devFix("Lang")
    devFix("Math")
    devFix("Time")
    parser = argparse.ArgumentParser()
    parser.add_argument('--dir', dest='dir')

    args = parser.parse_args()
    matches = 0
    uniqueMatches = 0
    prevBug = ""
    prevBugMatched = False
    allPatches = os.listdir(args.dir + "/our_patches_100/")		
    for patch in allPatches:
        projectNamelower = re.split(r"([1-9])", patch)[0]
        projectName = re.split(r"([1-9])", patch)[0].title()
        bug = patch.split(".")[0].strip().replace(projectNamelower, "")
        seed = patch.split(".")[1].strip()

        ourPatch = open(args.dir + "/our_patches/" + patch, 'r')

	if bug != prevBug:
	    prevBugMatched = False
	
	oursClean = cleanpatch(ourPatch.read(), False)
	print("USSSSSSS")
	print(oursClean)
	print("THEMMMMM")
	print(devFixes[projectName + bug])
	if oursClean in devFixes[projectName + bug]:
	    matches += 1 
            if not prevBugMatched:
             	uniqueMatches += 1
		print(projectName + " " + bug + " " + seed)
	    	print(oursClean)
		#print(theirsClean)
	    prevBugMatched = True 

        prevBug = bug
	
        #subprocess.call("rm " + args.dir + "/ourTmp"  + " " + args.dir + "/theirTmp"  + " " + args.dir + "/resTmp" , shell=True)
    
    subprocess.call("echo " + "total matches= " + str(matches), shell=True)
    subprocess.call("echo " + "unique matches= " + str(uniqueMatches), shell=True)
	
           
		        
