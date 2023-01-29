Artifact for the paper "Reinforcement Learning for Mutation Operator Selection in Automated Program Repair". This repository includes a modified version of the [JarFly](https://github.com/squaresLab/genprog4java) framework that implements our approach, our results, as well as instructions and scripts for replication.

# Repository structure:

**src**: modified version of the JarFly framework that includes our implementations.

**reports**: supplementary material including systematic overview of the tools that we considered as well as the results of our literature review.

**results**: experiment results including logs, patches, and plots. 

**scripts**: the scripts we used for executing the repair attempts with the different settings and evaluating their outputs. We also include replication instructions for all of our experiments.

# Replication Instructions:

sh ./scripts/execute_repair/execute.sh *output_dir* *bug_file* *config_file*

sh ./scripts/statistical_analysis.sh *output_dir*

sh ./scripts/analyze_patches/find_patch_files.py --dir *output_dir*

**output_dir**: path to location for storing outputs

**bug_file**: path to bug file that you wish to launch repair attempts on (e.g. ./bug/d4j_v1.1.0_patched_bugs to lanch repair attempts for the set of 49 bugs that JarFly reports to patch using GenProg)

**config_file**: name of config file depending on the experiment that you wish to run (e.g. Epsilon_MAB_Average_Relative_defects4j for epsilon greedy with average credit assignment and relative fitness as rewards. See ./execute_repair/prepare_bug_experiment.sh for full list)
