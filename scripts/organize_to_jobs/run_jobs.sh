
#$ -l tmem=15G
#$ -l h_vmem=15G
#$ -l h_rt=72:0:0

#$ -S /bin/bash
#$ -j y
#$ -N jobs

#$ -t 1-20

# Run Arguments
# $1: directory name 
# $2: bugs name (i.e. Chart) 
# $3: bugs location (relative from jarFly-learner i.e. tests/d4j_gp4j_setup/d4j_v1.1.0_chart_patched_bugs)
# $4: Experiment type (i.e. AP_Average_defects4j)

hostname
date

mkdir -p /scratch0/channa/$JOB_ID.$SGE_TASK_ID
cp -r /home/channa/$1/jarFly-learner /scratch0/channa/$JOB_ID.$SGE_TASK_ID

export D4J_HOME_LOCAL=/home/channa/$1/jarFly-learner/tests/defects4j
export D4J_HOME=/scratch0/channa/$JOB_ID.$SGE_TASK_ID/jarFly-learner/tests/defects4j
export GP4J_HOME=/scratch0/channa/$JOB_ID.$SGE_TASK_ID/jarFly-learner
export PATH=/share/apps/apache-maven-3.8.2/bin/:$PATH
export PATH=/share/apps/jdk-11.0.2/bin/:$PATH
export PATH=/scratch0/channa/$JOB_ID.$SGE_TASK_ID/jarFly-learner/tests/defects4j/framework/bin/:$PATH
export PATH=/share/apps/perl-5.28.0/bin:$PATH
export LD_LIBRARY_PATH=/share/apps/perl-5.28.0/lib:$LD_LIBRARY_PATH
export JAVA_HOME=/share/apps/jdk1.8.0_131

cd /scratch0/channa/$JOB_ID.$SGE_TASK_ID/jarFly-learner/tests/defects4j

echo "installing dependencies"
cpan --installdeps .

echo "initializing defects4j"
/scratch0/channa/$JOB_ID.$SGE_TASK_ID/jarFly-learner/tests/defects4j/init.sh


while read p; do

        path=$D4J_HOME_LOCAL/PatchedBugs_$2/$(echo $p | cut -d " " -f1 |  tr '[:upper:]' '[:lower:]')$(echo $p | cut -d " " -f2)Buggy/log$(echo $p | cut -d " " -f1)$(echo $p | cut -d " " -f2)Seed${SGE_TASK_ID}.txt

        if [ -f "$path" ]; then
                if (( $( cat $path | grep "Total elapsed time" | wc -l ) == 1 )) ; then
                        continue
                fi
		rm $path
        fi

	
       /scratch0/channa/$JOB_ID.$SGE_TASK_ID/jarFly-learner/scripts/prepare_bug_experiment.sh  $(echo $p | cut -d " " -f1) $(echo $p | cut -d " " -f2) humanMade 100 $D4J_HOME/patched /share/apps/jdk1.8.0_131 /share/apps/jdk1.8.0_131  \"false\" ./ \"false\" ./
	
	
	echo "Starting repair"
	
	/scratch0/channa/$JOB_ID.$SGE_TASK_ID/jarFly-learner/scripts/repair_bug_experiment.sh  $(echo $p | cut -d " " -f1) $(echo $p | cut -d " " -f2) allHuman 100 $D4J_HOME/patched ${SGE_TASK_ID} ${SGE_TASK_ID} false /share/apps/jdk1.8.0_131 /share/apps/jdk1.8.0_131  false \"\" false \"\" $4 PatchedBugs_$2

	rm -rf $D4J_HOME/patched/*
       	
	done < /scratch0/channa/$JOB_ID.$SGE_TASK_ID/jarFly-learner/$3

echo "Finishing repair"
rm -rf /scratch0/channa/$JOB_ID.$SGE_TASK_ID
date
