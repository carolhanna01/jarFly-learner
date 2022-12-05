package clegoues.genprog4java.Search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.WeightedMutation;
import clegoues.genprog4java.rep.JavaRepresentation;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.mut.edits.java.JavaEditOperation;

public class MutationOperatorsRL {
	
	// For TD Credit Assignment
	Map<Mutation, Double> qualities;
	
	//For Average Credit Assignment
	Map<Mutation, Double> totalRewards;
	
	Map<Mutation, Double> probabilities;

	/*********************** Operator Selection ********************************/

	/*****  For Strategy 1: raw reward probability operator selection  *****/
	Map<Mutation, Double> rewards;

	/******  For Strategy 2: probability matching operator selection  ******/
	double Pmin;
	double operators_num;
	double alpha;
	
	/******  For Strategy 3: adaptive pursuit operator selection  ******/
	double Pmax;
	double beta;
	boolean maxFound;
	
	/******  For Strategy 4: multi-armed bandit operator selection  ******/
	Map<Mutation, Integer> chosenCount;
	double exploreExploitTradeoff; 
	
	
	// For the Page-Hinkley (PH) test:
	double ph_gamma;
	double ph_delta;
	double current_m;
	double max_m;
	
	/***************************************************************************/

	public MutationOperatorsRL() {
		
		// TODO: fine tune the annotated parameters!
		
		List<WeightedMutation>  allMutations = Search.availableMutations;
		this.operators_num = allMutations.size();
		this.Pmin = 1 / (2 * this.operators_num); // fine tune (1/2K was recommended)
		this.alpha = 0.8;// Adaption rate: fine tune (this value is from AP paper)
		
		this.probabilities = new HashMap<Mutation, Double>();
		this.rewards = new HashMap<Mutation, Double>();
		this.qualities = new HashMap<Mutation, Double>();
		this.chosenCount = new HashMap<Mutation, Integer>();
		this.totalRewards = new HashMap<Mutation, Double>();
		
		for(WeightedMutation wmut: allMutations){
			Mutation mutation = (Mutation) ((WeightedMutation)wmut).getLeft();
			this.probabilities.put(mutation, 1 / this.operators_num);
			this.rewards.put(mutation, 1.0);
			this.qualities.put(mutation, 1.0);
			this.chosenCount.put(mutation, 0);
			this.chosenCount.put(mutation, 0);
			this.totalRewards.put(mutation, 0.0);
		}
		
		this.Pmax = 1 - ((this.operators_num - 1) * this.Pmin);
		this.beta = 0.8; // Learning rate: fine tune (this value is from AP paper)
		this.maxFound = false;

		this.exploreExploitTradeoff = 10; // Constant balancing exploration-exploitation tradeoff: fine-tune
		
		this.ph_gamma = 25; // For sensitivity tradeoff: fine-tune 
		this.ph_delta = 0.15; // For test robustness: fine-tune
		this.current_m = 0;
		this.max_m = 0;
	}
	
	
	/***************************************************************************/

	/***********************   Helper Functions  *******************************/
	
	
	// Helper function for the adaptive pursuit strategy: implements algorithm formula
	
	private double pursueMaximalQuality(double quality, double prob) {
		
		double maxQuality = Integer.MIN_VALUE;
		for (Mutation key: this.qualities.keySet()){
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
	
	private double upperConfidenceBound(Mutation editType) {
		
		double chosenCount = this.chosenCount.get(editType);
		double totalReward = this.totalRewards.get(editType);
		double quality = this.qualities.get(editType);
		
		double chosenCountTotal = 0;
		for (Mutation key: this.chosenCount.keySet()){
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
		
		double totalQualities = 0;
		for (Mutation key: this.qualities.keySet()){
			totalQualities += this.qualities.get(key);
		}
		double chosenCountTotal = 0;
		for (Mutation key: this.chosenCount.keySet()){
			chosenCountTotal += this.chosenCount.get(key);
		}
		double overallAverageQuality = totalQualities / chosenCountTotal;
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
		
		for (Mutation key: this.chosenCount.keySet()){
			this.chosenCount.put(key, 0);
		}
		
		for (Mutation key: this.probabilities.keySet()){
			this.probabilities.put(key, 1 / this.operators_num);
		}
		
		for (Mutation key: this.totalRewards.keySet()){
			this.totalRewards.put(key, 0.0);
		}
			
		this.current_m = 0;
		this.max_m = 0;
	}
	
	// Returns the reward value based on the specified reward type
	private double getReward(Representation rep, Mutation editType, Representation parentRep, int gen) {	
		double fitness = rep.getFitness();
		
		if (Search.fitnessType.startsWith("relative") && gen != 0) {
			Fitness fitnessEngine = new Fitness();
			fitnessEngine.testFitness(gen-1, parentRep);
			double parentFitness = parentRep.getFitness();
			if (parentFitness != 0) { // else: use fitness value as it (will be a high reward which is what we want anyway for this scenario)
				fitness /= parentFitness;
			}
			System.out.println("Parent fitness is: " + parentFitness);
			System.out.println("Relative fitness is: " + fitness);

		}
		
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
	
	private double updateQuality(double quality, double reward) {
		if (Search.rewardType.startsWith("average")) {
			return reward;
		}
		
		// Default is temporal difference
		return quality + alpha * (reward - quality);
	}


	private Mutation translateEditType(String editType) throws IllegalArgumentException{

		if (editType.contains("StmtAppend")) {
			return Mutation.APPEND;
		} else if (editType.contains("StmtDelete")) {
			return Mutation.DELETE;
		} else if (editType.contains("StmtReplace")) {
			return Mutation.REPLACE;
		} else if (editType.contains("StmtSwap")) {
			return Mutation.SWAP;
		} else if (editType.contains("MethodReplacer")) {
			return Mutation.FUNREP;
		} else if (editType.contains("ParameterRep")) {
			return Mutation.PARREP;
		} else if (editType.contains("ParameterAdd")) {
			return Mutation.PARADD;
		} else if (editType.contains("ParameterRem")) {
			return Mutation.PARREM;
		} else if (editType.contains("ExpressionReplace")) {
			return Mutation.EXPREP;
		} else if (editType.contains("ExpressionAdd")) {
			return Mutation.EXPADD;
		} else if (editType.contains("ExpressionRemove")) {
			return Mutation.EXPREM;
		} else if (editType.contains("NullCheck")) {
			return Mutation.NULLCHECK;
		} else if (editType.contains("Object initializer")) {
			return Mutation.OBJINIT;
		} else if (editType.contains("RangeChecker")) {
			return Mutation.RANGECHECK;
		} else if (editType.contains("CollectionSizeChecker")) {
			return Mutation.SIZECHECK;
		} else if (editType.contains("ClassCastChecker")) {
			return Mutation.CASTCHECK;
		} else if (editType.contains("LowerBoundSet")) {
			return Mutation.LBOUNDSET;
		} else if (editType.contains("UpperBoundSet")) {
			return Mutation.UBOUNDSET;
		} else if (editType.contains("OffByOne")) {
			return Mutation.OFFBYONE;
		} else if (editType.contains("SequenceExchanger")) {
			return Mutation.SEQEXCH;
		} else if (editType.contains("CasterMutator")) {
			return Mutation.CASTERMUT;
		} else if (editType.contains("CasteeMutator")) {
			return Mutation.CASTEEMUT;
		} else if (editType.contains("StmtSingleAppend")) {
			return Mutation.APPENDSINGLE;
		} else if (editType.contains("StmtSpecialAppend")) {
			return Mutation.APPENDSPECIAL;
		} else if (editType.contains("StmtMultiAppend")) {
			return Mutation.APPENDMULTI;
		} else if (editType.contains("StmtSingleDelete")) {
			return Mutation.DELETESINGLE;
		} else if (editType.contains("StmtSpecialDelete")) {
			return Mutation.DELETESPECIAL;
		} else if (editType.contains("StmtMultiDelete")) {
			return Mutation.DELETEMULTI;
		} else if (editType.contains("StmtSingleReplace")) {
			return Mutation.REPLACESINGLE;
		} else if (editType.contains("StmtSpecialReplace")) {
			return Mutation.REPLACESPECIAL;
		} else if (editType.contains("StmtMultieplace")) {
			return Mutation.REPLACEMULTI;
		}
		
		throw new IllegalArgumentException("Unsupported Mutation Operation Detected");
	}
	
	/***************************************************************************/


	/***************************   Strategy 1   ********************************/
	
	private void rawRewardProbability(Representation rep, Mutation editType, double reward) {
		
		
		// Update current reward
		this.rewards.put(editType, reward);
		double totalReward = 0;
		for (Mutation key: this.probabilities.keySet()){
			totalReward += this.rewards.get(key);
		}
		
		if (totalReward == 0) {
			return;
		}
		
		// Update probabilities
		for (Mutation key: this.probabilities.keySet()){
			this.probabilities.put(key, this.rewards.get(key) / totalReward);
		}
	}
	
	/***************************   Strategy 2   ********************************/
	
	private void probabilityMatching(Representation rep, Mutation editType, double reward) {
		
		double totalQualities = 0;
		for (Mutation key: this.qualities.keySet()){
			totalQualities += this.qualities.get(key);
		}

		System.out.printf("Updating %s fitness", editType);
		System.out.println(reward);
		
		double oldQuality = this.qualities.get(editType);
		double newQuality = updateQuality(oldQuality, reward);
		double newProbability = this.Pmin + (1 - this.operators_num * this.Pmin) * (oldQuality/ totalQualities);
		this.qualities.put(editType, newQuality);
		this.probabilities.put(editType, newProbability);
		
		return;
	
	}
	
	/***************************   Strategy 3   ********************************/

	private void adaptivePursuit(Representation rep, Mutation editType, double reward) {

		System.out.printf("Updating %s fitness", editType);
		System.out.println(reward);
		
		double oldQuality = this.qualities.get(editType);
		double newQuality = updateQuality(oldQuality, reward);
		this.qualities.put(editType, newQuality);
	
		for (Mutation key: this.probabilities.keySet()){
			double currQuality = this.qualities.get(key);
			double currProbability = this.probabilities.get(key);
			this.probabilities.put(key, pursueMaximalQuality(currQuality, currProbability));
		}

		this.maxFound = false;
		
		return;
	}
	
	/***************************   Strategy 4   ********************************/

	private void epsilonGreedy(Representation rep, Mutation editType, double reward) {
	
		System.out.printf("Updating %s fitness", editType);
		System.out.println(reward);
		
		double oldQuality = this.qualities.get(editType);
		double newQuality = updateQuality(oldQuality, reward);
		this.qualities.put(editType, newQuality);
		this.probabilities.put(editType, newQuality);
		
		return;	
	}
	/***************************   Strategy 5   ********************************/

	private void multiArmedBandit(Representation rep, Mutation editType, double reward) {
		
		System.out.printf("Updating %s fitness", editType);
		System.out.println(reward);
		
		int chosenCountTotal = 0;
		for (Mutation key: this.chosenCount.keySet()){
			chosenCountTotal += this.chosenCount.get(key);
		}
		
		// Update Quality
		double oldQuality = this.qualities.get(editType);
		double newQuality = updateQuality(oldQuality, reward);
		this.qualities.put(editType, newQuality);
		
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
	
	public void updateOperatorQualities(int gen, Representation rep) {
		
		ArrayList<JavaEditOperation> genome =  rep.getGenome();
		if (genome.size() == 0) {
			return;
		}
		String edit = genome.get(genome.size() - 1).toString();
		Mutation mut = translateEditType(edit);
		
		Representation repCopy = rep.copy();
		ArrayList<JavaEditOperation> parentGenome =  repCopy.getGenome();
		parentGenome.remove(genome.size() - 1);

		Representation parentRep = new JavaRepresentation(parentGenome, rep.getLocalization());
		double reward = getReward(rep, mut, parentRep, gen);
		
		if (Search.model.endsWith("rawReward")) {
			rawRewardProbability(rep, mut, reward);
		} else if (Search.model.endsWith("PM")) {
			probabilityMatching(rep, mut, reward);
		} else if (Search.model.endsWith("AP")) {
			adaptivePursuit(rep, mut, reward);
		} else if (Search.model.endsWith("Epsilon_MAB")) {
			epsilonGreedy(rep, mut, reward); // For both MAB and DMAB. Differentiation in conditional inside the function
		} else if (Search.model.endsWith("MAB")) {
			multiArmedBandit(rep, mut, reward); // For both MAB and DMAB. Differentiation in conditional inside the function
		} 
	}
	
	// Assigns the saved class probabilities to the  operators
	
	public List<WeightedMutation> rescaleMutationsBasedOnRL(List<WeightedMutation> availableMutations) {
		assert(availableMutations.size() > 0);
		List<WeightedMutation> retVal = new ArrayList<WeightedMutation>();
			
		for(WeightedMutation wmut: availableMutations){
			Mutation mutation = (Mutation) ((WeightedMutation)wmut).getLeft();
			double prob = this.probabilities.get(mutation);
			wmut.setValue(prob);
			retVal.add(wmut);
			System.out.println("Mutation is: " + mutation + " Probability is: " + prob);
		}
		return retVal;
	}

}



