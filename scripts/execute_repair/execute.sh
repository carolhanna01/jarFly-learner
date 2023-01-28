#The variable D4J_HOME should be directed to the folder where defects4j is installed.
#The variable D4J_HOME_HOME should be directed to the folder where outputs will be stored.
#The variable GP4J_HOME should be directed to the folder where genprog4java is installed.

# $1: location in D4J_HOME_HOME to store outputs
# $2: location of file with all bug names to execute
# $3: Name of config file (e.g. Epsilon_MAB_Average_Relative_defects4j)

while read p; do

        path=$D4J_HOME_LOCAL/PatchedBugs_$1/$(echo $p | cut -d " " -f1 |  tr '[:upper:]' '[:lower:]')$(echo $p | cut -d " " -f2)Buggy/log$(echo $p | cut -d " " -f1)$(echo $p | cut -d " " -f2)Seed${SGE_TASK_ID}.txt

        if [ -f "$path" ]; then
                if (( $( cat $path | grep "Total elapsed time" | wc -l ) == 1 )) ; then
                        continue
                fi
		rm $path
        fi

	
       /scratch0/channa/$JOB_ID.$SGE_TASK_ID/jarFly-learner/scripts/prepare_bug_experiment.sh  $(echo $p | cut -d " " -f1) $(echo $p | cut -d " " -f2) humanMade 100 $D4J_HOME/patched /share/apps/jdk1.8.0_131 /share/apps/jdk1.8.0_131  \"false\" ./ \"false\" ./
	
	
	echo "Starting repair"
	
	/scratch0/channa/$JOB_ID.$SGE_TASK_ID/jarFly-learner/scripts/repair_bug_experiment.sh  $(echo $p | cut -d " " -f1) $(echo $p | cut -d " " -f2) allHuman 100 $D4J_HOME/patched ${SGE_TASK_ID} ${SGE_TASK_ID} false /share/apps/jdk1.8.0_131 /share/apps/jdk1.8.0_131  false \"\" false \"\" $3 PatchedBugs_$1

	rm -rf $D4J_HOME/patched/*
       	
	done < $2

echo "Finishing repair"