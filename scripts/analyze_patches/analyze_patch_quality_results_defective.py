# $1- patch quality results file for cosolidated + combined evoSuite 3 and 6 to analyze

import sys

def getFailedTestsForDefect(defect):
	f = open(sys.argv[2], "r")

	for line in f.readlines():
	        if "project" in line:
	                continue

		if line.split(",")[1].strip() == defect:
			return float(line.split(",")[3]), float(line.split(",")[2])


lineCount=0
totalScore=0
minimum=1000
maximum=-1000
curr="" 
best=-10000
patched = open(sys.argv[1], "r")
res = open("quality_scores_calculations", "w")
for line in patched.readlines():
	record= line.split(",")
	if "project" in record:	
		continue
	project=record[1]
	if project != curr and curr != "":
		res.write(str(best) + "\n")
		lineCount+=1
       		totalScore+=best
        	if best < minimum:
                	minimum = best
        	if best > maximum:
			maximum = best

		best=-1000
	curr=project
	total=float(record[3]) 
  	failed=float(record[4])
  	success=total-failed
	score=success / total * 100
	defectiveFailed, defectiveTotal=getFailedTestsForDefect(record[1].strip())
	defectiveScore= (defectiveTotal - defectiveFailed)/defectiveTotal * 100
	toWrite=score-defectiveScore
  	if toWrite > best:
		best = toWrite
	if toWrite > maximum:
		print(defectiveScore)
		print(line) 


print("Average for repair quality")
print(str(totalScore / lineCount)) 

print("Minimum for repair quality")
print(str(minimum))

print("Maximum for repair quality")
print(str(maximum))


