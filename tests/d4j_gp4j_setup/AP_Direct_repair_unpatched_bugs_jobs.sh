
#$ -l tmem=15G
#$ -l h_vmem=15G
#$ -l h_rt=72:0:0

#$ -S /bin/bash
#$ -j y
#$ -N un_AP_D

#$ -t 1-20


hostname
date

mkdir -p /scratch0/channa/$JOB_ID.$SGE_TASK_ID
cp -r /home/channa/project/jarFly-learner /scratch0/channa/$JOB_ID.$SGE_TASK_ID

export D4J_HOME_LOCAL=/home/channa/project/jarFly-learner/tests/defects4j
export D4J_HOME=/scratch0/channa/$JOB_ID.$SGE_TASK_ID/jarFly-learner/tests/defects4j
export GP4J_HOME=/scratch0/channa/$JOB_ID.$SGE_TASK_ID/jarFly-learner
export PATH=/share/apps/apache-maven-3.8.2/bin/:$PATH
export PATH=/share/apps/jdk-11.0.2/bin/:$PATH
export PATH=/scratch0/channa/$JOB_ID.$SGE_TASK_ID/jarFly-learner/tests/defects4j/framework/bin/:$PATH
export PATH=/share/apps/perl-5.28.0/bin:$PATH
export LD_LIBRARY_PATH=/share/apps/perl-5.28.0/lib:$LD_LIBRARY_PATH
export JAVA_HOME=/share/apps/jdk1.8.0_131

cd /scratch0/channa/$JOB_ID.$SGE_TASK_ID/jarFly-learner/tests/defects4j
cpan --installdeps .
/scratch0/channa/$JOB_ID.$SGE_TASK_ID/jarFly-learner/tests/defects4j/init.sh


while read p; do

	
       /scratch0/channa/$JOB_ID.$SGE_TASK_ID/jarFly-learner/tests/d4j_gp4j_setup/prepare_bug_experiment.sh  $(echo $p | cut -d " " -f1) $(echo $p | cut -d " " -f2) humanMade 100 $D4J_HOME/unpatched /share/apps/jdk1.8.0_131 /share/apps/jdk1.8.0_131  \"false\" ./ \"false\" ./
	
	
	echo "Starting repair"
	
	/scratch0/channa/$JOB_ID.$SGE_TASK_ID/jarFly-learner/tests/d4j_gp4j_setup/repair_bug_experiment.sh  $(echo $p | cut -d " " -f1) $(echo $p | cut -d " " -f2) allHuman 100 $D4J_HOME/unpatched ${SGE_TASK_ID} ${SGE_TASK_ID} false /share/apps/jdk1.8.0_131 /share/apps/jdk1.8.0_131  false \"\" false \"\" AP_Direct_defects4j UnPatchedBugs_AP_Direct

	rm -rf $D4J_HOME/unpatched/*
       	
	done < /scratch0/channa/$JOB_ID.$SGE_TASK_ID/jarFly-learner/tests/d4j_gp4j_setup/d4j_v1.1.0_unpatched_bugs

echo "Finishing repair"
rm -rf /scratch0/channa/$JOB_ID.$SGE_TASK_ID
date
