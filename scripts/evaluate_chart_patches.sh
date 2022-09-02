
export D4J_HOME_LOCAL=/home/channa/epsilon_full/jarFly-learner/tests/defects4j

while read p; do

	python find_patch_files.py --dir $D4J_HOME_LOCAL/PatchedBugs_Chart | tee tmp 
	       	
done < $D4J_HOME_LOCAL/../d4j_gp4j_setup/d4j_v1.1.0_chart_patched_bugs

python compare_with_developer_patches.py --dir $D4J_HOME_LOCAL/PatchedBugs_Chart

