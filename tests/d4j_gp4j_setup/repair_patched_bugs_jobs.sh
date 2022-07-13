
#$ -l tmem=30G
#$ -l h_vmem=30G
#$ -l h_rt=24:0:0

#$ -S /bin/bash
#$ -j y
#$ -N Base_patched_repair 

#$ -t 1-20


hostname
date

#mkdir -p /scratch0/channa/$JOB_ID.$SGE_TASK_ID
#cd /scratch0/channa/$JOB_ID.$SGE_TASK_ID

export D4J_HOME=/home/channa/project/jarFly-learner/tests/defects4j
export GP4J_HOME=/home/channa/project/jarFly-learner
export PATH=/share/apps/apache-maven-3.8.2/bin/:$PATH
export PATH=/share/apps/jdk-11.0.2/bin/:$PATH
export PATH=/home/channa/project/jarFly-learner/tests/defects4j/framework/bin/:$PATH
export PATH=/share/apps/perl-5.28.0/bin:$PATH
export LD_LIBRARY_PATH=/share/apps/perl-5.28.0/lib:$LD_LIBRARY_PATH
export JAVA_HOME=/share/apps/jdk1.8.0_131

cd /home/channa/project/jarFly-learner/tests/defects4j
cpan --installdeps .
/home/channa/project/jarFly-learner/tests/defects4j/init.sh


while read p; do
        DIR="/home/channa/project/jarFly-learner/tests/defects4j/PatchedBugs/$(echo $p | tr '[:upper:]' '[:lower:]' | sed 's/ //g')Buggy"
	
	if [ "${SGE_TASK_ID}" != "1" ]; then
		until -d "$DIR"
		do
			sleep 5
		done
	else

	       /home/channa/project/jarFly-learner/tests/d4j_gp4j_setup/prepare_bug.sh  $(echo $p | cut -d " " -f1) $(echo $p | cut -d " " -f2) humanMade 100 PatchedBugs /share/apps/jdk1.8.0_131 /share/apps/jdk1.8.0_131  \"false\" ./ \"false\" ./
	
	fi
	
	echo "Starting repair"
	
	/home/channa/project/jarFly-learner/tests/d4j_gp4j_setup/repair_bug.sh  $(echo $p | cut -d " " -f1) $(echo $p | cut -d " " -f2) allHuman 100 PatchedBugs ${SGE_TASK_ID} ${SGE_TASK_ID} false /share/apps/jdk1.8.0_131 /share/apps/jdk1.8.0_131  false \"\" false \"\" Base_defects4j PatchedBugs_Base
       	
	#rm -rf /home/channa/project/jarFly-learner/tests/defects4j/PatchedBugs/*
done < /home/channa/project/jarFly-learner/tests/d4j_gp4j_setup/d4j_v1.1.0_patched_bugs

echo "Finishing repair"
#rm -rf /scratch0/channa/$JOB_ID.$SGE_TASK_ID
date
