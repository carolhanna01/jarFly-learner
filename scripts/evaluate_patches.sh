
export D4J_HOME_LOCAL=/home/channa/epsilon_full/jarFly-learner/tests/defects4j

while read p; do

	python find_patch_files.py --dir $D4J_HOME_LOCAL/$1 | tee tmp 
	       	
done < $D4J_HOME_LOCAL/../d4j_gp4j_setup/$2

python compare_with_developer_patches.py --dir $D4J_HOME_LOCAL/$1
python compare_with_jarfly_patches.py --dir $D4J_HOME_LOCAL/$1

