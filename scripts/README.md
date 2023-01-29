Here you can find the relevant bugs from the Defects4J bachmark that we used in our experiments, as well as the scripts to launch the repair attempts and evaluate the resulting patched.

# Directory structure:

**analyze_patches**: scripts for creating and evaluating the patch files.
**bugs**: for each RQ, the names of bugs from the Defects4J benchmark that were used for evaluation.
**execute_repair**: scripts for executing the repair attemps on specific bugs.
**organize_to_jobs**: not relevant for replication purposes as they are suitable for the compute node cluster environment that we were specifically using.
**statistical_analysis**: scripts for collecting statistics from results.


# Replication Instructions:

sh ./execute_repair/execute.sh output_dir bug_file config_file
sh ./statistical_analysis.sh output_dir

**output_dir**: path to location for storing outputs
**bug_file**: path to bug file that you wish to launch repair attempts on (e.g. ./bug/d4j_v1.1.0_patched_bugs to lanch repair attempts for the set of 49 bugs that JarFly reports to patch using GenProg)
**config_file**: name of config file depending on the experiment that you wish to run (e.g. Epsilon_MAB_Average_Relative_defects4j for epsilon greedy with average credit assignment and relative fitness as rewards. See ./execute_repair/prepare_bug_experiment.sh for full list)


