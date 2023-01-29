# $1- patch quality results file for cosolidated + combined evoSuite 3 and 6 to analyze

import sys
import subprocess

lineCount=0
allPassingCount=0
totalScore=0
minimum=100
maximum=0

f = open(sys.argv[1], "r")
res = open("quality_scores_calculations", "w")
for line in f.readlines():
	record= line.split(",")
	if "project" in record:	
		continue
	total=float(record[3]) 
  	failed=float(record[4])
  	success=total-failed
	score=success / total * 100
	if score == 0:
		continue
	print(score)
	res.write(str(score) + "\n")
  	lineCount+=1
  	totalScore+=score
  	if score == 100:
		subprocess.call("cp /SAN/crest/RLinGI/full_experiments/PatchedBugs_par_group_relative_revDiff/our_patches/" + record[2] + " /SAN/crest/RLinGI/full_experiments/PatchedBugs_par_group_relative_revDiff/our_patches_100", shell=True )
    	  	allPassingCount+=1
	if score < minimum:
	        if score == 0:
        	        print(line)
		else:
			minimum = score
  	if score > maximum:
      		maximum = score

allPassingCount*=100
#echo "Median for repair quality"
#sort -n quality_scores_calculations | awk -f median.awk 

print("Average for repair quality")
print(str(totalScore / lineCount)) 

print("Minimum for repair quality")
print(str(minimum))

print("Maximum for repair quality")
print(str(maximum))

print("100% repair quality")
print(str(allPassingCount))
print(str(lineCount))
print(str(float(allPassingCount / lineCount)))

