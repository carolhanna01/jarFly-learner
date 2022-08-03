package clegoues.genprog4java.Search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.WeightedMutation;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.mut.edits.java.JavaEditOperation;

public class MutationOperatorsRL {
	
	// Probability values
	Map<String, Double> probabilities;

	/*****  For Strategy 1: raw reward probability operator selection  *****/
	Map<String, Double> rewards;

	/***************************************************************************/

	/******  For Strategy 2: probability matching operator selection  ******/
	// Parameters
	double Pmin;
	double operators_num;
	double alpha;
	
	// Quality values
	Map<String, Double> qualities;

	/***************************************************************************/
	
	/******  For Strategy 3: adaptive pursuit operator selection  ******/
	double Pmax;
	double beta;
	boolean maxFound;
	
	/***************************************************************************/
	
	/******  For Strategy 4: multi-armed bandit operator selection  ******/
	Map<String, Integer> chosenCount;
	Map<String, Double> totalQualities;
	double exploreExploitTradeoff; 
	
	
	// For the Page-Hinkley (PH) test:
	double ph_gamma;
	double ph_delta;
	double current_m;
	double max_m;
	
	/******  Average Reward Credit Assignment  ******/
	Map<String, Double> totalRewards;
	
	/***************************************************************************/

	public MutationOperatorsRL() {
		
		// TODO: fine tune the annotated parameters!
		
		this.operators_num = Search.availableMutations.size();
		this.Pmin = 1 / (2 * this.operators_num); // fine tune (1/2K was recommended)
		this.alpha = 0.8;// Adaption rate: fine tune (this value is from AP paper)
		
		this.probabilities = new HashMap<String, Double>();
		this.probabilities.put("Append", 1 / this.operators_num);
		this.probabilities.put("Delete", 1 / this.operators_num);
		this.probabilities.put("Replace", 1 / this.operators_num);
		
		this.rewards = new HashMap<String, Double>();
		this.rewards.put("Append", 1.0);
		this.rewards.put("Delete", 1.0);
		this.rewards.put("Replace", 1.0);
		
		this.qualities = new HashMap<String, Double>();
		this.qualities.put("Append", 1.0);
		this.qualities.put("Delete", 1.0);
		this.qualities.put("Replace", 1.0);
		
		this.Pmax = 1 - ((this.operators_num - 1) * this.Pmin);
		this.beta = 0.8; // Learning rate: fine tune (this value is from AP paper)
		this.maxFound = false;
		
		this.chosenCount = new HashMap<String, Integer>();
		this.chosenCount.put("Append", 0);
		this.chosenCount.put("Delete", 0);
		this.chosenCount.put("Replace", 0);

		this.totalQualities = new HashMap<String, Double>();
		this.totalQualities.put("Append", 0.0);
		this.totalQualities.put("Delete", 0.0);
		this.totalQualities.put("Replace", 0.0);
		
		this.exploreExploitTradeoff = 10; // Constant balancing exploration-exploitation tradeoff: fine-tune
		
		this.ph_gamma = 25; // For sensitivity tradeoff: fine-tune 
		this.ph_delta = 0.15; // For test robustness: fine-tune
		this.current_m = 0;
		this.max_m = 0;
		
		this.totalRewards = new HashMap<String, Double>();
		this.totalRewards.put("Append", 0.0);
		this.totalRewards.put("Delete", 0.0);
		this.totalRewards.put("Replace", 0.0);
	}
	
	
	/***************************************************************************/

	/***********************   Helper Functions  *******************************/
	
	
	// Helper function for the adaptive pursuit strategy: implements algorithm formula
	
	private double pursueMaximalQuality(double quality, double prob) {
		
		double maxQuality = Integer.MIN_VALUE;
		for (String key: this.qualities.keySet()){
			if (this.qualities.get(key) > maxQuality) {
				maxQuality = this.qualities.get(key);
			}
		}
		
		if (maxQuality == quality && !this.maxFound) {
			this.maxFound = true;
			return prob + this.beta * (this.Pmax - prob);
			
		} else {
			return prob + this.beta * (this.Pmin - prob);
		}
	}
	
	
	// Helper function for the MAB strategy: implements UCB algorithm
	
	private double upperConfidenceBound(String editType) {
		
		double chosenCount = this.chosenCount.get(editType);
		double totalReward = this.totalRewards.get(editType);
		double quality = this.qualities.get(editType);
		
		double chosenCountTotal = 0;
		for (String key: this.chosenCount.keySet()){
			chosenCountTotal += this.chosenCount.get(key);
		}
		double probability = quality;
		if (Search.rewardType.startsWith("average")) {
			probability = totalReward / chosenCount;
		}
		return  probability + this.exploreExploitTradeoff * Math.sqrt((Math.log(chosenCountTotal)/ chosenCount));
	}
	
	
	// Helper function for the Dynamic MAB strategy: implements Page-Hinkley statistical test
	
	private boolean pageHinkley(double reward) {
		
//		System.out.println("Activating Page Hinkley test");
		
		double allTotalQualities = 0;
		for (String key: this.totalQualities.keySet()){
			allTotalQualities += this.totalQualities.get(key);
		}
		double chosenCountTotal = 0;
		for (String key: this.chosenCount.keySet()){
			chosenCountTotal += this.chosenCount.get(key);
		}
		double overallAverageQuality = allTotalQualities / chosenCountTotal;
		this.current_m += reward - overallAverageQuality + this.ph_delta;
		
		if(this.current_m > this.max_m) {
			this.max_m = this.current_m;
		}
		
		if( Math.abs(this.max_m - this.current_m)  > this.ph_gamma) {
			return true;
		}
		
		return false;
	}
	
	
	// Helper function for the Dynamic MAB strategy: re-initialises the algorithm parameters (after PH test detects best operator changed)
	
	private void restartDMAB() {
		
//		System.out.println("Restarting the DMAB Algorithm");
		
		for (String key: this.chosenCount.keySet()){
			this.chosenCount.put(key, 0);
		}
		
		for (String key: this.probabilities.keySet()){
			this.probabilities.put(key, 1 / this.operators_num);
		}
		
		for (String key: this.totalQualities.keySet()){
			this.totalQualities.put(key, 0.0);
		}
		
		for (String key: this.totalRewards.keySet()){
			this.totalRewards.put(key, 0.0);
		}
			
		this.current_m = 0;
		this.max_m = 0;
	}
	
	// Returns the reward value based on the specified reward type
	private double getReward(Representation rep, String editType) {	
		double fitness = rep.getFitness();
		
		int currentChosenCount = this.chosenCount.get(editType);
		this.chosenCount.put(editType, currentChosenCount + 1);
		
		double currentTotalRewad = this.totalRewards.get(editType);
		this.totalRewards.put(editType, currentTotalRewad + fitness);

		if (Search.rewardType.startsWith("average")) {
			return this.totalRewards.get(editType) / this.chosenCount.get(editType);
		}
		
		// Default is raw fitness
		return fitness; 
	}
	
	private double getQuality(double quality, double reward) {
		if (Search.rewardType.startsWith("average")) {
			return reward;
		}
		
		// Default is temporal difference
		return quality + alpha * (reward - quality);
	}

	
	private String getEditName(String editType) {
		if (editType.contains("Append")) {
			return "Append";
		} else if (editType.contains("Delete")) {
			return "Delete";
		} else if (editType.contains("Replace")) {
			return "Replace";
		}
		return "error";
	}
	
	/***************************************************************************/


	/***************************   Strategy 1   ********************************/
	
	private void rawRewardProbability(Representation rep, String editType, double reward) {
		
		
		// Update current reward
		this.rewards.put(editType, reward);
		double totalReward = 0;
		for (String key: this.probabilities.keySet()){
			totalReward += this.rewards.get(key);
		}
		
		if (totalReward == 0) {
			return;
		}
		
		// Update probabilities
		for (String key: this.probabilities.keySet()){
			this.probabilities.put(key, this.rewards.get(key) / totalReward);
		}
	}
	
	/***************************   Strategy 2   ********************************/
	
	private void probabilityMatching(Representation rep, String editType, double reward) {
		
		double totalQualities = 0;
		for (String key: this.qualities.keySet()){
			totalQualities += this.qualities.get(key);
		}

		System.out.printf("Updating %s fitness", editType);
		System.out.println(reward);
		
		double oldQuality = this.qualities.get(editType);
		double newQuality = getQuality(oldQuality, reward);
		double newProbability = this.Pmin + (1 - this.operators_num * this.Pmin) * (oldQuality/ totalQualities);
		this.qualities.put(editType, newQuality);
		this.probabilities.put(editType, newProbability);
		
		return;
	
	}
	
	/***************************   Strategy 3   ********************************/

	private void adaptivePursuit(Representation rep, String editType, double reward) {

		System.out.printf("Updating %s fitness", editType);
		System.out.println(reward);
		
		double oldQuality = this.qualities.get(editType);
		double newQuality = getQuality(oldQuality, reward);
		this.qualities.put(editType, newQuality);
	
		for (String key: this.probabilities.keySet()){
			double currQuality = this.qualities.get(key);
			double currProbability = this.probabilities.get(key);
			this.probabilities.put(key, pursueMaximalQuality(currQuality, currProbability));
		}

		this.maxFound = false;
		
		return;
	}
	
	/***************************   Strategy 4   ********************************/

	private void epsilonGreedy(Representation rep, String editType, double reward) {
	
		System.out.printf("Updating %s fitness", editType);
		System.out.println(reward);
		
		double oldQuality = this.qualities.get(editType);
		double newQuality = getQuality(oldQuality, reward);
		this.qualities.put(editType, newQuality);
		this.probabilities.put(editType, newQuality);
		
		return;	
	}
	/***************************   Strategy 5   ********************************/

	private void multiArmedBandit(Representation rep, String editType, double reward) {
		
		System.out.printf("Updating %s fitness", editType);
		System.out.println(reward);
		
		int chosenCountTotal = 0;
		for (String key: this.chosenCount.keySet()){
			chosenCountTotal += this.chosenCount.get(key);
		}
		
		// Update Quality
		double oldQuality = this.qualities.get(editType);
		double newQuality = getQuality(oldQuality, reward);
		this.qualities.put(editType, newQuality);
		
		// Update Total Qualities
		double currTotalQuality = this.totalQualities.get(editType);
		this.totalQualities.put(editType, currTotalQuality + newQuality);
		
		// Update Probability
		double newProb = upperConfidenceBound(editType);
		this.probabilities.put(editType, newProb);
		
		if (Search.model.endsWith("DMAB")) {
			if(pageHinkley(reward)) {
				restartDMAB();
			}
		}
		
		return;	
	}
	
	// Activates the specified algorithm
	
	public void updateOperatorQualities(Representation rep) {
		
		ArrayList<JavaEditOperation> genome =  rep.getGenome();
		if (genome.size() == 0) {
			return;
		}
		
		String edit = genome.get(genome.size() - 1).toString();
		String editType = getEditName(edit);
		double reward = getReward(rep, editType);
		
		if (Search.model.endsWith("rawReward")) {
			rawRewardProbability(rep, editType, reward);
		} else if (Search.model.endsWith("PM")) {
			probabilityMatching(rep, editType, reward);
		} else if (Search.model.endsWith("AP")) {
			adaptivePursuit(rep, editType, reward);
		} else if (Search.model.endsWith("Epsilon_MAB")) {
			epsilonGreedy(rep, editType, reward); // For both MAB and DMAB. Differentiation in conditional inside the function
		} else if (Search.model.endsWith("MAB")) {
			multiArmedBandit(rep, editType, reward); // For both MAB and DMAB. Differentiation in conditional inside the function
		} 
	}
	
	// Assigns the saved class probabilities to the  operators
	
	public List<WeightedMutation> rescaleMutationsBasedOnRL(List<WeightedMutation> availableMutations) {
		assert(availableMutations.size() > 0);
		List<WeightedMutation> retVal = new ArrayList<WeightedMutation>();
				
		for(WeightedMutation wmut: availableMutations){
			Mutation mutation = (Mutation) ((WeightedMutation)wmut).getLeft();
			double prob = 0;
			if(mutation == Mutation.REPLACE){
				prob = this.probabilities.get("Replace");
				System.out.println(mutation);
				System.out.println(prob);
			}else if(mutation == Mutation.APPEND){
				prob = this.probabilities.get("Append");
				System.out.println(mutation);
				System.out.println(prob);
			}else if(mutation == Mutation.DELETE){
				prob = this.probabilities.get("Delete");
				System.out.println(mutation);
				System.out.println(prob);
			}else if(mutation == Mutation.SWAP){
				prob = this.probabilities.get("Replace");
				System.out.println(mutation);
				System.out.println(prob);
			}else{
				//TODO: See if we want to extend to other operators (the PAR templates)
			}
			wmut.setValue(prob);
			retVal.add(wmut);
		}
		return retVal;
	}

}



