#!/bin/bash
# 1st param is the path to genprog for java
# 2nd param is the RL algorithm
# 3rd param is the credit assignment strategy
# claire note to self: junit jars on her machine are: /Applications/eclipse/external-jars/
#Example of how to run it:
#./prepareOffByOne.sh /home/mau/Research/genprog4java

# Does the compile script build the test files?

PATHTOGENPROG="$1"
JUNITJARS="$PATHTOGENPROG"/lib

PATHTOOFFBYONE=`pwd`

if [ ! -d bin/ ] ; then
    mkdir bin
fi

javac -d bin/ src/packageOffByOne/OffByOne.java 
javac -classpath $JUNITJARS/junit-4.12.jar:$JUNITJARS/hamcrest-core-1.3.jar:bin/ -sourcepath src/tests/*java -d bin/ src/tests/*java
#rm -rf bin/packageOffByOne/

#PACKAGEDIR=${JAVADIR//"/"/"."}

#Create config file 
FILE=./offByOne.config
/bin/cat <<EOM >$FILE
javaVM = /usr/bin/java
popsize = 20
seed = 0
classTestFolder = bin/
workingDir = $PATHTOOFFBYONE/
outputDir = $PATHTOOFFBYONE/tmp/
libs = $PATHTOGENPROG/lib/junit-4.12.jar:$PATHTOGENPROG/lib/junittestrunner.jar:$JUNITJARS/hamcrest-core-1.3.jar:$PATHTOOFFBYONE/bin/
sanity = yes
regenPaths
sourceDir = src/
positiveTests = $PATHTOOFFBYONE/pos.tests
negativeTests = $PATHTOOFFBYONE/neg.tests
jacocoPath = $PATHTOGENPROG/lib/jacocoagent.jar
classSourceFolder = bin/
targetClassName = packageOffByOne.OffByOne
search=ga
edits=append;replace;delete
negativePathWeight=0.35
positivePathWeight=0.65
sample=0.1 
maxVariants=400
model=$2
rewardType=$3
EOM