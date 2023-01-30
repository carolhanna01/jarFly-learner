Artifact for the paper "Reinforcement Learning for Mutation Operator Selection in Automated Program Repair". This repository includes a modified version of the [JarFly](https://github.com/squaresLab/genprog4java) framework that implements our approach, our results, as well as instructions and scripts for replication.

# Repository structure:

**src**: modified version of the JarFly framework that includes our implementations.

**reports**: supplementary material including systematic overview of the tools that we considered as well as the results of our literature review.

**results**: experiment results including logs, patches, and plots. 

**scripts**: the scripts we used for executing the repair attempts with the different settings and evaluating their outputs. We also include replication instructions for all of our experiments.

# Replication Instructions:

sh ./scripts/execute_repair/execute.sh *output_dir* *bug_file* *config_file*

sh ./scripts/statistical_analysis/countSeedSucess.sh.sh *output_dir*

sh ./scripts/analyze_patches/find_patch_files.py --dir *output_dir*

**output_dir**: path to location for storing outputs

**bug_file**: path to bug file that you wish to launch repair attempts on (e.g. ./bug/d4j_v1.1.0_patched_bugs to lanch repair attempts for the set of 49 bugs that JarFly reports to patch using GenProg)

**config**: name of config file depending on the experiment that you wish to run (e.g. Epsilon_MAB_Average_Relative_defects4j for epsilon greedy with average credit assignment and relative fitness as rewards)

## RQ1 + 2:

### **bug_file**:

./scripts/bugs/d4j_v1.1.0_patched_bugs

### **config**:

Depending on experiment you want to lanch choose config name. Config name format: selectionAlgorithm_creditAssignment_defects4j

PM_Direct_defects4j

PM_Average_defects4j

AP_Direct_defects4j

AP_Average_defects4j

MAB_Direct_defects4j

MAB_Average_defects4j

Epsilon_MAB_Direct_defects4j

Epsilon_MAB_Average_defects4j

## RQ3:

### **bug_file**:

Use ./scripts/bugs/d4j_v1.1.0_par_patched_bugs

### **config**:

Use Epsilon_MAB_Average_Par_defects4j for epsilon-greedy with average credit assignment and relative fitness value as reward with par mutations.

## RQ4:

### **bug_file**:

Use ./scripts/bugs/d4j_v1.1.0_patched_bugs and d4j_v1.1.0_unpatched_bugs for the full 353 bug evaluation.

### **config**:

Use Epsilon_MAB_Average_Relative_Grouped_defects4j for epsilon-greedy with average credit assignment and relative fitness value as reward with grouped par mutations.

