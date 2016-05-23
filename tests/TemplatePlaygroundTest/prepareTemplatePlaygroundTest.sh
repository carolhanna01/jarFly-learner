#!/bin/bash
# 1st param is the path to genprog for java
# 2nd param is the path to the junit jars on your machine
# claire note to self: junit jars on her machine are: /Applications/eclipse/external-jars/
#Example of how to run it:
#./prepareTemplatePlaygroundTest.sh /home/mau/Research/genprog4java

# Does the compile script build the test files?

PATHTOGENPROG="$1"
JUNITJARS="$PATHTOGENPROG"/lib

PATHTOTEMPLATEPLAYGROUNDTEST=`pwd`

if [ ! -d bin/ ] ; then
    mkdir bin
fi

javac -d bin/ src/packageTemplatePlaygroundTest/TemplatePlaygroundTest.java 
javac -classpath $JUNITJARS/junit-4.12.jar:$JUNITJARS/hamcrest-core-1.3.jar:bin/ -sourcepath src/tests/*java -d bin/ src/tests/*java
rm -rf bin/packageTemplatePlaygroundTest/

#PACKAGEDIR=${JAVADIR//"/"/"."}

#Create config file 
FILE=./templatePlaygroundTest.config
/bin/cat <<EOM >$FILE
javaVM = /usr/bin/java
popsize = 20
seed = 0
classTestFolder = bin/
workingDir = $PATHTOTEMPLATEPLAYGROUNDTEST/
outputDir = $PATHTOTEMPLATEPLAYGROUNDTEST/tmp/
libs = $PATHTOGENPROG/lib/junit-4.12.jar:$PATHTOGENPROG/lib/junittestrunner.jar:$JUNITJARS/hamcrest-core-1.3.jar:$PATHTOTEMPLATEPLAYGROUNDTEST/bin/
sanity = yes
regenPaths
sourceDir = src/
positiveTests = $PATHTOTEMPLATEPLAYGROUNDTEST/pos.tests
negativeTests = $PATHTOTEMPLATEPLAYGROUNDTEST/neg.tests
jacocoPath = $PATHTOGENPROG/lib/jacocoagent.jar
classSourceFolder = bin/
targetClassName = packageTemplatePlaygroundTest.TemplatePlaygroundTest
EOM
