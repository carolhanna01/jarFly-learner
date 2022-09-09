This repository is for the paper "Optimising Mutation Operator Selection for Automated Program Repair with Reinforcement Learning" paper by Carol Hanna, Justyna Petke, and Aymeric Blot, as a part of an MSc Software Systems Engineering final dissertation at University College London.

This is a modified version of the JarFly framework in which we implement our approach.

Our additions to the repository structure:

Reports: the dissertation report (pending) + systematic overview of the tools that we considered as well as the results of our literature review.

Results: includes the plots and the patches that were produced using our technique. 

Scripts: the scripts we used for running the probability matching, adaptive pursuit, UCB, and epsilon-greedy experiments. Additionally, the scripts that we used for evaluating the patches.

The implementation of the approach are in the source files of JarFly. Files added/modified are:

* (Added)- src/clegoues/genprog4java/Search/MutationOperatorsRL.java 
* (Modidied)- src/clegoues/genprog4java/Search/GeneticProgramming.java
* (Modified)- src/clegoues/genprog4java/Search/Search.java
* (Modified)- src/clegoues/util/GlobalUtils.java
