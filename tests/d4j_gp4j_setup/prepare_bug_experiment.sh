#!/bin/bash


#The purpose of this script is to set up the environment to run Genprog of a particular defects4j bug.

#Preconditions:
#The variable D4J_HOME should be directed to the folder where defects4j is installed.
#The variable GP4J_HOME should be directed to the folder where genprog4java is installed.

#Output
#The output is a txt file with the output of running the coverage analysis of the test suite on each of the folders indicated. 

#Input
# 1st param: project name, sentence case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param: bug number (ex: 1,2,3,4,...)
# 3th param: testing option (ex: humanMade, generated)
# 4th param: test suite sample size (ex: 1, 100)
# 5th param is the folder where the bug files will be cloned to. Starting from $D4J_HOME (Ex: ExamplesCheckedOut)
# 6th param is the folder where the java 7 instalation is located
# 7th param is the folder where the java 8 instalation is located
# 8th param is set to \"true\" if negative tests are to be specified using sampled tests else set this to \"false\"
# 9th param is the path to file containing sampled negative tests
# 10th param is set to \"true\" if positive tests are to be specified using sampled tests else set this to \"false\"
# 11th param is the path to file containing sampled positive tests

# Example usage, VM:
#./prepare_bug.sh Math 2 allHuman 100 ExamplesCheckedOut /usr/lib/jvm/java-7-oracle/ /usr/lib/jvm/java-8-oracle/ true <path to neg.test> true <path to pos.test>

if [ "$#" -ne 11 ]; then
    echo "This script should be run with 11 parameters:"
	echo "1st param: project name, sentence case (ex: Lang, Chart, Closure, Math, Time)"
	echo "2nd param: bug number (ex: 1,2,3,4,...)"
	echo "3th param: testing option (ex: humanMade, generated)"
	echo "4th param: test suite sample size (ex: 1, 100)"
	echo "5th param is the folder where the bug files will be cloned to. Starting from $D4J_HOME"
	echo "6th param is the folder where the java 7 instalation is located"
	echo "7th param is the folder where the java 8 instalation is located"
	echo "8th param is set to \"true\" if negative tests are to be specified using sampled tests else set this to \"false\""
	echo "9th param is the path to file containing sampled negative tests"
	echo "10th param is set to \"true\" if positive tests are to be specified using sampled tests else set this to \"false\""
	echo "11th param is the path to file containing sampled positive tests"
    exit 0
fi

PROJECT="$1"
BUGNUMBER="$2"
OPTION="$3"
TESTSUITESAMPLE="$4"
BUGSFOLDER="$5"
DIROFJAVA7="$6"
DIROFJAVA8="$7"
SAMPLENEGTESTS="$8"
NEGTESTPATH="$9"
SAMPLEPOSTESTS="${10}"
POSTESTPATH="${11}"

#Add the path of defects4j so the defects4j's commands run 
export PATH=$PATH:"$D4J_HOME"/framework/bin/
export PATH=$PATH:"$D4J_HOME"/framework/util/
export PATH=$PATH:"$D4J_HOME"/major/bin/

#copy these files to the source control

mkdir -p $BUGSFOLDER

LOWERCASEPACKAGE=`echo $PROJECT | tr '[:upper:]' '[:lower:]'`

# directory with the checked out buggy project
BUGWD=$BUGSFOLDER"/"$LOWERCASEPACKAGE"$BUGNUMBER"Buggy

#Checkout the buggy and fixed versions of the code (latter to make second testsuite
defects4j checkout -p $1 -v "$BUGNUMBER"b -w $BUGWD

##defects4j checkout -p $1 -v "$BUGNUMBER"f -w $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE"$2"Fixed

#Compile the both buggy and fixed code
for dir in Buggy
do
    pushd $BUGSFOLDER"/"$LOWERCASEPACKAGE$BUGNUMBER$dir
    defects4j compile
    popd
done
# Common genprog libs: junit test runner and the like
CONFIGLIBS=$GP4J_HOME"/lib/junittestrunner.jar"
#:"$GP4J_HOME"/lib/commons-io-1.4.jar:"$GP4J_HOME"/lib/junit-4.12.jar:"$GP4J_HOME"/lib/hamcrest-core-1.3.jar"

cd $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/
TESTWD=`defects4j export -p dir.src.tests`
SRCFOLDER=`defects4j export -p dir.bin.classes`
COMPILECP=`defects4j export -p cp.compile`
TESTCP=`defects4j export -p cp.test`
WD=`defects4j export -p dir.src.classes`
cd $BUGWD/$WD

#Create file to run defects4j compile

FILE=$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/runCompile.sh
/bin/cat <<EOM >$FILE
#!/bin/bash
export JAVA_HOME=$DIROFJAVA7
export PATH=$DIROFJAVA7/bin/:$PATH
#sudo update-java-alternatives -s java-7-oracle
cd $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/
$D4J_HOME/framework/bin/defects4j compile
if [[ \$? -ne 0 ]]; then
      echo "error compiling defect"
      exit 1
fi
export JAVA_HOME=$DIROFJAVA8
export PATH=$DIROFJAVA8/bin/:$PATH
#sudo update-java-alternatives -s java-8-oracle
EOM

chmod 777 $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/runCompile.sh


cd $BUGWD

#Create config files 
Base_FILE=$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/Base_defects4j.config
/bin/cat <<EOM >$Base_FILE
seed = 0
sanity = yes
popsize = 40
javaVM = $DIROFJAVA7/jre/bin/java
workingDir = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/
outputDir = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/tmp
classSourceFolder =$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/$SRCFOLDER
libs = $CONFIGLIBS
sourceDir = $WD
positiveTests = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/pos.tests
negativeTests = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/neg.tests
jacocoPath = $GP4J_HOME/lib/jacocoagent.jar
testClassPath=$TESTCP
srcClassPath=$COMPILECP
compileCommand = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/runCompile.sh
targetClassName = $BUGWD/bugfiles.txt
testGranularity=method
# 0.1 for GenProg and 1.0 for TrpAutoRepair and PAR
sample=1.0  
# edits for PAR, GenProg, TrpAutoRepair
#edits=append;replace;delete;FUNREP;PARREP;PARADD;PARREM;EXPREP;EXPADD;EXPREM;NULLCHECK;OBJINIT;RANGECHECK;SIZECHECK;CASTCHECK;LBOUNDSET;UBOUNDSET;OFFBYONE;SEQEXCH;CASTERMUT;CASTEEMUT
edits=append;replace;delete
#edits=FUNREP;PARREP;PARADD;PARREM;EXPREP;EXPADD;EXPREM;NULLCHECK;OBJINIT;RANGECHECK;SIZECHECK;CASTCHECK
# don't know whats this used for. Ask Mau.
#model=probabilistic
#modelPath=/home/mausoto/probGenProg/genprog4java/overallModel.txt
# use 1.0,0.1 for TrpAutoRepair and PAR. Use 0.65 and 0.35 for GenProg
negativePathWeight=1.0
positivePathWeight=0.1
# trp for TrpAutoRepair, gp for GenProg and PAR 
search=ga
# used only for TrpAutoRepair. value=400
maxVariants=400
EOM

RawReward_Direct_FILE=$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/RawReward_Direct_defects4j.config
/bin/cat <<EOM >$RawReward_Direct_FILE
seed = 0
sanity = yes
popsize = 40
javaVM = $DIROFJAVA7/jre/bin/java
workingDir = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/
outputDir = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/tmp
classSourceFolder = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/$SRCFOLDER
libs = $CONFIGLIBS
sourceDir = $WD
positiveTests = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/pos.tests
negativeTests = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/neg.tests
jacocoPath = $GP4J_HOME/lib/jacocoagent.jar
testClassPath=$TESTCP
srcClassPath=$COMPILECP
compileCommand =$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/runCompile.sh
targetClassName = $BUGWD/bugfiles.txt
testGranularity=method
# 0.1 for GenProg and 1.0 for TrpAutoRepair and PAR
sample=1.0  
# edits for PAR, GenProg, TrpAutoRepair
#edits=append;replace;delete;FUNREP;PARREP;PARADD;PARREM;EXPREP;EXPADD;EXPREM;NULLCHECK;OBJINIT;RANGECHECK;SIZECHECK;CASTCHECK;LBOUNDSET;UBOUNDSET;OFFBYONE;SEQEXCH;CASTERMUT;CASTEEMUT
edits=append;replace;delete
#edits=FUNREP;PARREP;PARADD;PARREM;EXPREP;EXPADD;EXPREM;NULLCHECK;OBJINIT;RANGECHECK;SIZECHECK;CASTCHECK
# don't know whats this used for. Ask Mau.
#model=probabilistic
#modelPath=/home/mausoto/probGenProg/genprog4java/overallModel.txt
# use 1.0,0.1 for TrpAutoRepair and PAR. Use 0.65 and 0.35 for GenProg
negativePathWeight=1.0
positivePathWeight=0.1
# trp for TrpAutoRepair, gp for GenProg and PAR 
search=ga
model= RL_rawReward
# used only for TrpAutoRepair. value=400
maxVariants=400
EOM

RawReward_Average_FILE=$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/RawReward_Average_defects4j.config
/bin/cat <<EOM >$RawReward_Average_FILE
seed = 0
sanity = yes
popsize = 40
javaVM = $DIROFJAVA7/jre/bin/java
workingDir = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/
outputDir = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/tmp
classSourceFolder =$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/$SRCFOLDER
libs = $CONFIGLIBS
sourceDir = $WD
positiveTests = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/pos.tests
negativeTests = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/neg.tests
jacocoPath = $GP4J_HOME/lib/jacocoagent.jar
testClassPath=$TESTCP
srcClassPath=$COMPILECP
compileCommand = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/runCompile.sh
targetClassName = $BUGWD/bugfiles.txt
testGranularity=method
# 0.1 for GenProg and 1.0 for TrpAutoRepair and PAR
sample=1.0  
# edits for PAR, GenProg, TrpAutoRepair
#edits=append;replace;delete;FUNREP;PARREP;PARADD;PARREM;EXPREP;EXPADD;EXPREM;NULLCHECK;OBJINIT;RANGECHECK;SIZECHECK;CASTCHECK;LBOUNDSET;UBOUNDSET;OFFBYONE;SEQEXCH;CASTERMUT;CASTEEMUT
edits=append;replace;delete
#edits=FUNREP;PARREP;PARADD;PARREM;EXPREP;EXPADD;EXPREM;NULLCHECK;OBJINIT;RANGECHECK;SIZECHECK;CASTCHECK
# don't know whats this used for. Ask Mau.
#model=probabilistic
#modelPath=/home/mausoto/probGenProg/genprog4java/overallModel.txt
# use 1.0,0.1 for TrpAutoRepair and PAR. Use 0.65 and 0.35 for GenProg
negativePathWeight=1.0
positivePathWeight=0.1
# trp for TrpAutoRepair, gp for GenProg and PAR 
search=ga
model= RL_rawReward
rewardType = average
# used only for TrpAutoRepair. value=400
maxVariants=400
EOM

PM_Direct_FILE=$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/PM_Direct_defects4j.config
/bin/cat <<EOM >$PM_Direct_FILE
seed = 0
sanity = yes
popsize = 40
javaVM = $DIROFJAVA7/jre/bin/java
workingDir = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/
outputDir = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/tmp
classSourceFolder = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/$SRCFOLDER
libs = $CONFIGLIBS
sourceDir = $WD
positiveTests = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/pos.tests
negativeTests = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/neg.tests
jacocoPath = $GP4J_HOME/lib/jacocoagent.jar
testClassPath=$TESTCP
srcClassPath=$COMPILECP
compileCommand = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/runCompile.sh
targetClassName = $BUGWD/bugfiles.txt
testGranularity=method
# 0.1 for GenProg and 1.0 for TrpAutoRepair and PAR
sample=1.0  
# edits for PAR, GenProg, TrpAutoRepair
#edits=append;replace;delete;FUNREP;PARREP;PARADD;PARREM;EXPREP;EXPADD;EXPREM;NULLCHECK;OBJINIT;RANGECHECK;SIZECHECK;CASTCHECK;LBOUNDSET;UBOUNDSET;OFFBYONE;SEQEXCH;CASTERMUT;CASTEEMUT
edits=append;replace;delete
#edits=FUNREP;PARREP;PARADD;PARREM;EXPREP;EXPADD;EXPREM;NULLCHECK;OBJINIT;RANGECHECK;SIZECHECK;CASTCHECK
# don't know whats this used for. Ask Mau.
#model=probabilistic
#modelPath=/home/mausoto/probGenProg/genprog4java/overallModel.txt
# use 1.0,0.1 for TrpAutoRepair and PAR. Use 0.65 and 0.35 for GenProg
negativePathWeight=1.0
positivePathWeight=0.1
# trp for TrpAutoRepair, gp for GenProg and PAR 
search=ga
model= RL_PM
# used only for TrpAutoRepair. value=400
maxVariants=400
EOM

PM_Average_FILE=$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/PM_Average_defects4j.config
/bin/cat <<EOM >$PM_Average_FILE
seed = 0
sanity = yes
popsize = 40
javaVM = $DIROFJAVA7/jre/bin/java
workingDir = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/
outputDir = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/tmp
classSourceFolder = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/$SRCFOLDER
libs = $CONFIGLIBS
sourceDir = $WD
positiveTests = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/pos.tests
negativeTests = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/neg.tests
jacocoPath = $GP4J_HOME/lib/jacocoagent.jar
testClassPath=$TESTCP
srcClassPath=$COMPILECP
compileCommand = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/runCompile.sh
targetClassName = $BUGWD/bugfiles.txt
testGranularity=method
# 0.1 for GenProg and 1.0 for TrpAutoRepair and PAR
sample=1.0  
# edits for PAR, GenProg, TrpAutoRepair
#edits=append;replace;delete;FUNREP;PARREP;PARADD;PARREM;EXPREP;EXPADD;EXPREM;NULLCHECK;OBJINIT;RANGECHECK;SIZECHECK;CASTCHECK;LBOUNDSET;UBOUNDSET;OFFBYONE;SEQEXCH;CASTERMUT;CASTEEMUT
edits=append;replace;delete
#edits=FUNREP;PARREP;PARADD;PARREM;EXPREP;EXPADD;EXPREM;NULLCHECK;OBJINIT;RANGECHECK;SIZECHECK;CASTCHECK
# don't know whats this used for. Ask Mau.
#model=probabilistic
#modelPath=/home/mausoto/probGenProg/genprog4java/overallModel.txt
# use 1.0,0.1 for TrpAutoRepair and PAR. Use 0.65 and 0.35 for GenProg
negativePathWeight=1.0
positivePathWeight=0.1
# trp for TrpAutoRepair, gp for GenProg and PAR 
search=ga
model= RL_PM
rewardType = average
# used only for TrpAutoRepair. value=400
maxVariants=400
EOM

AP_Direct_FILE=$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/AP_Direct_defects4j.config
/bin/cat <<EOM >$AP_Direct_FILE
seed = 0
sanity = yes
popsize = 40
javaVM = $DIROFJAVA7/jre/bin/java
workingDir = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/
outputDir = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/tmp
classSourceFolder = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/$SRCFOLDER
libs = $CONFIGLIBS
sourceDir = $WD
positiveTests = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/pos.tests
negativeTests = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/neg.tests
jacocoPath = $GP4J_HOME/lib/jacocoagent.jar
testClassPath=$TESTCP
srcClassPath=$COMPILECP
compileCommand = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/runCompile.sh
targetClassName = $BUGWD/bugfiles.txt
testGranularity=method
# 0.1 for GenProg and 1.0 for TrpAutoRepair and PAR
sample=1.0  
# edits for PAR, GenProg, TrpAutoRepair
#edits=append;replace;delete;FUNREP;PARREP;PARADD;PARREM;EXPREP;EXPADD;EXPREM;NULLCHECK;OBJINIT;RANGECHECK;SIZECHECK;CASTCHECK;LBOUNDSET;UBOUNDSET;OFFBYONE;SEQEXCH;CASTERMUT;CASTEEMUT
edits=append;replace;delete
#edits=FUNREP;PARREP;PARADD;PARREM;EXPREP;EXPADD;EXPREM;NULLCHECK;OBJINIT;RANGECHECK;SIZECHECK;CASTCHECK
# don't know whats this used for. Ask Mau.
#model=probabilistic
#modelPath=/home/mausoto/probGenProg/genprog4java/overallModel.txt
# use 1.0,0.1 for TrpAutoRepair and PAR. Use 0.65 and 0.35 for GenProg
negativePathWeight=1.0
positivePathWeight=0.1
# trp for TrpAutoRepair, gp for GenProg and PAR 
search=ga
model= RL_AP
# used only for TrpAutoRepair. value=400
maxVariants=400
EOM

AP_Average_FILE=$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/AP_Average_defects4j.config
/bin/cat <<EOM >$AP_Average_FILE
seed = 0
sanity = yes
popsize = 40
javaVM = $DIROFJAVA7/jre/bin/java
workingDir =$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/
outputDir = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/tmp
classSourceFolder = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/$SRCFOLDER
libs = $CONFIGLIBS
sourceDir = $WD
positiveTests = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/pos.tests
negativeTests = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/neg.tests
jacocoPath = $GP4J_HOME/lib/jacocoagent.jar
testClassPath=$TESTCP
srcClassPath=$COMPILECP
compileCommand = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/runCompile.sh
targetClassName = $BUGWD/bugfiles.txt
testGranularity=method
# 0.1 for GenProg and 1.0 for TrpAutoRepair and PAR
sample=1.0  
# edits for PAR, GenProg, TrpAutoRepair
#edits=append;replace;delete;FUNREP;PARREP;PARADD;PARREM;EXPREP;EXPADD;EXPREM;NULLCHECK;OBJINIT;RANGECHECK;SIZECHECK;CASTCHECK;LBOUNDSET;UBOUNDSET;OFFBYONE;SEQEXCH;CASTERMUT;CASTEEMUT
edits=append;replace;delete
#edits=FUNREP;PARREP;PARADD;PARREM;EXPREP;EXPADD;EXPREM;NULLCHECK;OBJINIT;RANGECHECK;SIZECHECK;CASTCHECK
# don't know whats this used for. Ask Mau.
#model=probabilistic
#modelPath=/home/mausoto/probGenProg/genprog4java/overallModel.txt
# use 1.0,0.1 for TrpAutoRepair and PAR. Use 0.65 and 0.35 for GenProg
negativePathWeight=1.0
positivePathWeight=0.1
# trp for TrpAutoRepair, gp for GenProg and PAR 
search=ga
model= RL_AP
rewardType = average
# used only for TrpAutoRepair. value=400
maxVariants=400
EOM

MAB_Direct_FILE=$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/MAB_Direct_defects4j.config
/bin/cat <<EOM >$MAB_Direct_FILE
seed = 0
sanity = yes
popsize = 40
javaVM = $DIROFJAVA7/jre/bin/java
workingDir = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/
outputDir = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/tmp
classSourceFolder = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/$SRCFOLDER
libs = $CONFIGLIBS
sourceDir = $WD
positiveTests = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/pos.tests
negativeTests = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/neg.tests
jacocoPath = $GP4J_HOME/lib/jacocoagent.jar
testClassPath=$TESTCP
srcClassPath=$COMPILECP
compileCommand = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/runCompile.sh
targetClassName = $BUGWD/bugfiles.txt
testGranularity=method
# 0.1 for GenProg and 1.0 for TrpAutoRepair and PAR
sample=1.0  
# edits for PAR, GenProg, TrpAutoRepair
#edits=append;replace;delete;FUNREP;PARREP;PARADD;PARREM;EXPREP;EXPADD;EXPREM;NULLCHECK;OBJINIT;RANGECHECK;SIZECHECK;CASTCHECK;LBOUNDSET;UBOUNDSET;OFFBYONE;SEQEXCH;CASTERMUT;CASTEEMUT
edits=append;replace;delete
#edits=FUNREP;PARREP;PARADD;PARREM;EXPREP;EXPADD;EXPREM;NULLCHECK;OBJINIT;RANGECHECK;SIZECHECK;CASTCHECK
# don't know whats this used for. Ask Mau.
#model=probabilistic
#modelPath=/home/mausoto/probGenProg/genprog4java/overallModel.txt
# use 1.0,0.1 for TrpAutoRepair and PAR. Use 0.65 and 0.35 for GenProg
negativePathWeight=1.0
positivePathWeight=0.1
# trp for TrpAutoRepair, gp for GenProg and PAR 
search=ga
model= RL_MAB
# used only for TrpAutoRepair. value=400
maxVariants=400
EOM

MAB_Average_FILE=$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/MAB_Average_defects4j.config
/bin/cat <<EOM >$MAB_Average_FILE
seed = 0
sanity = yes
popsize = 40
javaVM = $DIROFJAVA7/jre/bin/java
workingDir = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/
outputDir = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/tmp
classSourceFolder = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/$SRCFOLDER
libs = $CONFIGLIBS
sourceDir = $WD
positiveTests = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/pos.tests
negativeTests = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/neg.tests
jacocoPath = $GP4J_HOME/lib/jacocoagent.jar
testClassPath=$TESTCP
srcClassPath=$COMPILECP
compileCommand = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/runCompile.sh
targetClassName = $BUGWD/bugfiles.txt
testGranularity=method
# 0.1 for GenProg and 1.0 for TrpAutoRepair and PAR
sample=1.0  
# edits for PAR, GenProg, TrpAutoRepair
#edits=append;replace;delete;FUNREP;PARREP;PARADD;PARREM;EXPREP;EXPADD;EXPREM;NULLCHECK;OBJINIT;RANGECHECK;SIZECHECK;CASTCHECK;LBOUNDSET;UBOUNDSET;OFFBYONE;SEQEXCH;CASTERMUT;CASTEEMUT
edits=append;replace;delete
#edits=FUNREP;PARREP;PARADD;PARREM;EXPREP;EXPADD;EXPREM;NULLCHECK;OBJINIT;RANGECHECK;SIZECHECK;CASTCHECK
# don't know whats this used for. Ask Mau.
#model=probabilistic
#modelPath=/home/mausoto/probGenProg/genprog4java/overallModel.txt
# use 1.0,0.1 for TrpAutoRepair and PAR. Use 0.65 and 0.35 for GenProg
negativePathWeight=1.0
positivePathWeight=0.1
# trp for TrpAutoRepair, gp for GenProg and PAR 
search=ga
model= RL_MAB
rewardType = average
# used only for TrpAutoRepair. value=400
maxVariants=400
EOM

DMAB_Direct_FILE=$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/DMAB_Direct_defects4j.config
/bin/cat <<EOM >$DMAB_Direct_FILE
seed = 0
sanity = yes
popsize = 40
javaVM = $DIROFJAVA7/jre/bin/java
workingDir = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/
outputDir = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/tmp
classSourceFolder = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/$SRCFOLDER
libs = $CONFIGLIBS
sourceDir = $WD
positiveTests = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/pos.tests
negativeTests = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/neg.tests
jacocoPath = $GP4J_HOME/lib/jacocoagent.jar
testClassPath=$TESTCP
srcClassPath=$COMPILECP
compileCommand = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/runCompile.sh
targetClassName = $BUGWD/bugfiles.txt
testGranularity=method
# 0.1 for GenProg and 1.0 for TrpAutoRepair and PAR
sample=1.0  
# edits for PAR, GenProg, TrpAutoRepair
#edits=append;replace;delete;FUNREP;PARREP;PARADD;PARREM;EXPREP;EXPADD;EXPREM;NULLCHECK;OBJINIT;RANGECHECK;SIZECHECK;CASTCHECK;LBOUNDSET;UBOUNDSET;OFFBYONE;SEQEXCH;CASTERMUT;CASTEEMUT
edits=append;replace;delete
#edits=FUNREP;PARREP;PARADD;PARREM;EXPREP;EXPADD;EXPREM;NULLCHECK;OBJINIT;RANGECHECK;SIZECHECK;CASTCHECK
# don't know whats this used for. Ask Mau.
#model=probabilistic
#modelPath=/home/mausoto/probGenProg/genprog4java/overallModel.txt
# use 1.0,0.1 for TrpAutoRepair and PAR. Use 0.65 and 0.35 for GenProg
negativePathWeight=1.0
positivePathWeight=0.1
# trp for TrpAutoRepair, gp for GenProg and PAR 
search=ga
model= RL_DMAB
# used only for TrpAutoRepair. value=400
maxVariants=400
EOM

DMAB_Average_FILE=$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/DMAB_Average_defects4j.config
/bin/cat <<EOM >$DMAB_Average_FILE
seed = 0
sanity = yes
popsize = 40
javaVM = $DIROFJAVA7/jre/bin/java
workingDir = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/
outputDir = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/tmp
classSourceFolder = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/$SRCFOLDER
libs = $CONFIGLIBS
sourceDir = $WD
positiveTests = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/pos.tests
negativeTests = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/neg.tests
jacocoPath = $GP4J_HOME/lib/jacocoagent.jar
testClassPath=$TESTCP
srcClassPath=$COMPILECP
compileCommand = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/runCompile.sh
targetClassName = $BUGWD/bugfiles.txt
testGranularity=method
# 0.1 for GenProg and 1.0 for TrpAutoRepair and PAR
sample=1.0  
# edits for PAR, GenProg, TrpAutoRepair
#edits=append;replace;delete;FUNREP;PARREP;PARADD;PARREM;EXPREP;EXPADD;EXPREM;NULLCHECK;OBJINIT;RANGECHECK;SIZECHECK;CASTCHECK;LBOUNDSET;UBOUNDSET;OFFBYONE;SEQEXCH;CASTERMUT;CASTEEMUT
edits=append;replace;delete
#edits=FUNREP;PARREP;PARADD;PARREM;EXPREP;EXPADD;EXPREM;NULLCHECK;OBJINIT;RANGECHECK;SIZECHECK;CASTCHECK
# don't know whats this used for. Ask Mau.
#model=probabilistic
#modelPath=/home/mausoto/probGenProg/genprog4java/overallModel.txt
# use 1.0,0.1 for TrpAutoRepair and PAR. Use 0.65 and 0.35 for GenProg
negativePathWeight=1.0
positivePathWeight=0.1
# trp for TrpAutoRepair, gp for GenProg and PAR 
search=ga
model= RL_DMAB
rewardType = average
# used only for TrpAutoRepair. value=400
maxVariants=400
EOM

#  get passing and failing tests as well as files
#info about the bug

if [ "$SAMPLENEGTESTS" = "true" ]; then
        if [ "$NEGTESTPATH" = "" ]; then
                echo "please enter path to file containing negative test cases"
                exit 1
        fi
        cp $NEGTESTPATH $BUGWD/neg.tests
else	
	defects4j export -p tests.trigger > $BUGWD/neg.tests
fi

currentpid="$currentpid $!"
wait $currentpid

case "$OPTION" in
"humanMade" ) 
	if [ "$SAMPLEPOSTESTS" = "true" ]; then
        	if [ "$POSTESTPATH" = "" ]; then
                	echo "please enter path to file containing positive test cases"
  	   		exit 1
        	fi
	        cp $POSTESTPATH $BUGWD/pos.tests
	else
	     	defects4j export -p tests.all > $BUGWD/pos.tests
	fi
;;
"allHuman" ) 
	if [ "$SAMPLEPOSTESTS" = "true" ]; then
        	if [ "$POSTESTPATH" = "" ]; then
                	echo "please enter path to file containing positive test cases"
  	   		exit 1
        	fi
	        cp $POSTESTPATH  $BUGWD/pos.tests
	else
		defects4j export -p tests.all > $BUGWD/pos.tests
	fi
;;
"onlyRelevant" ) 
	if [ "$SAMPLEPOSTESTS" = "true" ]; then
        	if [ "$POSTESTPATH" = "" ]; then
                	echo "please enter path to file containing positive test cases"
  	   		exit 1
        	fi
	        cp $POSTESTPATH  $BUGWD/pos.tests
	else
		defects4j export -p tests.relevant > $BUGWD/pos.tests
        fi
;;
esac

currentpid="$currentpid $!"
wait $currentpid

#Remove a percentage of the positive tests in the test suite
cd $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/

if [[ $TESTSUITESAMPLE -ne 100 ]]
then
    PERCENTAGETOREMOVE=$(echo "$TESTSUITESAMPLE * 0.01" | bc -l )
    echo "sample = $PERCENTAGETOREMOVE" >> $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/Base_defects4j.config
    echo "sample = $PERCENTAGETOREMOVE" >> $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/RawReward_Direct_defects4j.config
    echo "sample = $PERCENTAGETOREMOVE" >> $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/RawReward_Average_defects4j.config
    echo "sample = $PERCENTAGETOREMOVE" >> $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/PM_Direct_defects4j.config
    echo "sample = $PERCENTAGETOREMOVE" >> $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/PM_Average_defects4j.config
    echo "sample = $PERCENTAGETOREMOVE" >> $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/AP_Direct_defects4j.config
    echo "sample = $PERCENTAGETOREMOVE" >> $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/AP_Average_defects4j.config
    echo "sample = $PERCENTAGETOREMOVE" >> $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/MAB_Direct_defects4j.config
    echo "sample = $PERCENTAGETOREMOVE" >> $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/MAB_Average_defects4j.config
    echo "sample = $PERCENTAGETOREMOVE" >> $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/DMAB_Direct_defects4j.config
    echo "sample = $PERCENTAGETOREMOVE" >> $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/DMAB_Average_defects4j.config
fi

# get the class names to be repaired

defects4j export -p classes.modified > $BUGWD/bugfiles.txt

echo "This is the working directory: "
echo $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/$WD
