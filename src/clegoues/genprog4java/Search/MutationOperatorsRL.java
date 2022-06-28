package clegoues.genprog4java.Search;

import java.util.ArrayList;
import java.util.List;

import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.WeightedMutation;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.mut.edits.java.JavaEditOperation;

public class MutationOperatorsRL {
	
	// Probability values
	double appendProb;
	double deleteProb;
	double replaceProb; 
	
	/*****  For Strategy 1: raw reward probability operator selection  *****/
	double appendReward;
	double deleteReward;
	double replaceReward; 
	/***************************************************************************/

	/******  For Strategy 2: probability matching operator selection  ******/
	// Parameters
	double Pmin;
	double operators_num;
	double alpha;
	
	// Quality values
	double appendQuality;
	double deleteQuality;
	double replaceQuality;
	/***************************************************************************/
	
	/******  For Strategy 3: adaptive pursuit operator selection  ******/
	double Pmax;
	double beta;
	boolean maxFound;
	
	/***************************************************************************/
	
	/******  For Strategy 3: adaptive pursuit operator selection  ******/
	double appendChosenCount;
	double deleteChosenCount;
	double replaceChosenCount;

	double appendTotalReward;
	double deleteTotalReward;
	double replaceTotalReward;
	
	double exploreExploitTradeoff; 
	
	
	// For the Page-Hinkley (PH) test:
	double ph_gamma;
	double ph_delta;
	double current_m;
	double max_m;
	
	/***************************************************************************/

	public MutationOperatorsRL() {
		
		// TODO: fine tune the annotated parameters!

		this.appendReward = 1;
		this.deleteReward = 1;
		this.replaceReward = 1;
		
		this.Pmin = 0.1; // fine tune (1/2K was recommended)
		this.operators_num = Search.availableMutations.size();
		this.alpha = 0.01; // Adaption rate: fine tune
		
		this.appendProb = 1 / this.operators_num;
		this.deleteProb = 1 / this.operators_num;
		this.replaceProb = 1 / this.operators_num;
		
		this.appendQuality = 1;
		this.deleteQuality = 1;
		this.replaceQuality = 1;
		
		this.Pmax = 1 - ((this.operators_num - 1) * this.Pmin);
		this.beta = 0.01; // Learning rate: fine tune
		this.maxFound = false;
		
		this.appendChosenCount = 0;
		this.deleteChosenCount = 0;
		this.replaceChosenCount = 0;

		this.appendTotalReward = 0;
		this.deleteTotalReward = 0;
		this.replaceTotalReward = 0;
		
		this.exploreExploitTradeoff = 0.3; // Constant balancing exploration-exploitation tradeoff: fine-tune
		
		this.ph_gamma = 0.15; // For sensitivity tradeoff: fine-tune 
		this.ph_delta = 0.15; // For test robustness: fine-tune
		this.current_m = 0;
		this.max_m = 0;
	}
	
	
	/***************************************************************************/

	/***********************   Helper Functions  *******************************/
	
	
	// Helper function for the adaptive pursuit strategy: implements algorithm formula
	
	private double pursueMaximalQuality(double maxQuality, double quality, double prob) {
		
		if (maxQuality == quality && !this.maxFound) {
			this.maxFound = true;
			return prob + this.beta * (this.Pmax - prob);
			
		} else {
			return prob + this.beta * (this.Pmin - prob);
		}
	}
	
	
	// Helper function for the MAB strategy: implements UCB algorithm
	
	private double upperConfidenceBound(double chosenCount, double totalReward) {
		double chosenCountTotal = this.appendChosenCount + this.deleteChosenCount + this.replaceChosenCount;
		return (totalReward / chosenCount) + this.exploreExploitTradeoff * Math.sqrt((Math.log(chosenCountTotal)/ chosenCount));
	}
	
	
	// Helper function for the Dynamic MAB strategy: implements Page-Hinkley statistical test
	
	private boolean pageHinkley(double reward) {
		
//		System.out.println("Activating Page Hinkley test");
		
		double totalReward = this.appendTotalReward + this.deleteTotalReward + this.replaceTotalReward;
		double totalChosenCount = this.appendChosenCount + this.deleteChosenCount+ this.replaceChosenCount;
		double overallAverageReward = totalReward / totalChosenCount;
		this.current_m += reward - overallAverageReward + this.ph_delta;
		
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
		
		this.appendChosenCount = 0;
		this.deleteChosenCount = 0;
		this.replaceChosenCount = 0;

		this.appendTotalReward = 0;
		this.deleteTotalReward = 0;
		this.replaceTotalReward = 0;
		
		this.appendProb = 1 / this.operators_num;
		this.deleteProb = 1 / this.operators_num;
		this.replaceProb = 1 / this.operators_num;
		
		this.current_m = 0;
		this.max_m = 0;
	}
	
	// Returns the reward value based on the specified reward type
	private double getReward(Representation rep, String editType) {	
		double fitness = rep.getFitness();
		
		if (editType.contains("Append")){
			this.appendChosenCount += 1;
			this.appendTotalReward += fitness;
			if (Search.rewardType.startsWith("average")) {
				return this.appendTotalReward / this.appendChosenCount;
			} else if (Search.rewardType.startsWith("extreme")) {
				//TODO
			}
			
		} else if (editType.contains("Delete")){
			this.deleteChosenCount += 1;
			this.deleteTotalReward += fitness;
			if (Search.rewardType.startsWith("average")) {
				return this.deleteTotalReward / this.deleteChosenCount;
			} else if (Search.rewardType.startsWith("extreme")) {
				//TODO
			}
			
		} else if (editType.contains("Replace")){
			this.replaceChosenCount += 1;
			this.replaceTotalReward += fitness;
			if (Search.rewardType.startsWith("average")) {
				return this.replaceTotalReward / this.replaceChosenCount;
			} else if (Search.rewardType.startsWith("extreme")) {
				//TODO
			}
		}

		return fitness; // Default is raw fitness
		
	}
	
	/***************************************************************************/


	/***************************   Strategy 1   ********************************/
	
	private void rawRewardProbability(Representation rep) {
		ArrayList<JavaEditOperation> genome =  rep.getGenome();
		if (genome.size() == 0) { //TODO: fishy- Make it an assert again + figure out why it's failing OffByOne
			return;
		}

		String editType = genome.get(genome.size() - 1).toString();
		double reward = getReward(rep, editType);
		
		if (editType.contains("Append")) {
//			System.out.println("Updating append fitness");
//			System.out.println(currFitness);
			this.appendReward = reward;
			double totalReward = this.appendReward + this.deleteReward + this.replaceReward;
			this.appendProb = this.appendReward / totalReward;
			
		} else if (editType.contains("Delete")) {
//			System.out.println("Updating delete fitness");
//			System.out.println(currFitness);
			this.deleteReward = reward;
			double totalReward = this.appendReward + this.deleteReward + this.replaceReward;
			this.deleteProb = this.deleteReward / totalReward;
			
		} else if (editType.contains("Replace")) {
//			System.out.println("Updating replace fitness");
//			System.out.println(currFitness);
			this.replaceReward = reward;
			double totalReward = this.appendReward + this.deleteReward + this.replaceReward;
			this.replaceProb = this.replaceReward / totalReward;
		}
		
		return;
	}
	
	/***************************   Strategy 2   ********************************/
	
	private void probabilityMatching(Representation rep) {
		
		ArrayList<JavaEditOperation> genome =  rep.getGenome();
		if (genome.size() == 0) {
			return;
		}
		
		String editType = genome.get(genome.size() - 1).toString();
		double reward = getReward(rep, editType);
		
		double qualitySum = this.appendQuality + this.deleteQuality + this.replaceQuality;

		if (editType.contains("Append")) {
//			System.out.println("Updating append fitness");
//			System.out.println(reward);
			double quality = this.appendQuality;
			this.appendQuality = quality + alpha * (reward - quality);
			this.appendProb = this.Pmin + (1 - this.operators_num * this.Pmin) * (quality/ qualitySum);
			
		} else if (editType.contains("Delete")) {
//			System.out.println("Updating delete fitness");
//			System.out.println(reward);
			double quality = this.deleteQuality;
			this.deleteQuality = quality + alpha * (reward - quality);
			this.deleteProb = this.Pmin + (1 - this.operators_num * this.Pmin) * (quality/ qualitySum);

		} else if (editType.contains("Replace")) {
//			System.out.println("Updating replace fitness");
//			System.out.println(reward);
			double quality = this.replaceQuality;
			this.replaceQuality = quality + alpha * (reward - quality);
			this.replaceProb = this.Pmin + (1 - this.operators_num * this.Pmin) * (quality/ qualitySum);

		} else {
			//TODO: make this throw an exception instead
			System.out.println("Unexpected Mutation Operator");
		}
		
		return;
	
	}
	
	/***************************   Strategy 3   ********************************/

	private void adaptivePursuit(Representation rep) {
				
		ArrayList<JavaEditOperation> genome =  rep.getGenome();
		if (genome.size() == 0) {
			return;
		}
		
		String editType = genome.get(genome.size() - 1).toString();
		double reward = getReward(rep, editType);

		if (editType.contains("Append")) {
//			System.out.println("Updating append fitness");
//			System.out.println(reward);
			double quality = this.appendQuality;
			this.appendQuality = quality + alpha * (reward - quality);
			
		} else if (editType.contains("Delete")) {
//			System.out.println("Updating delete fitness");
//			System.out.println(reward);
			double quality = this.deleteQuality;
			this.deleteQuality = quality + alpha * (reward - quality);

		} else if (editType.contains("Replace")) {
//			System.out.println("Updating replace fitness");
//			System.out.println(reward);
			double quality = this.replaceQuality;
			this.replaceQuality = quality + alpha * (reward - quality);

		} else {
			//TODO: make this throw an exception instead
			System.out.println("Unexpected Mutation Operator");
		}
		
		double maxQuality = Math.max(this.appendQuality, Math.max(this.deleteQuality, this.replaceQuality));

		this.appendProb = pursueMaximalQuality(maxQuality, this.appendQuality, this.appendProb);
		this.deleteProb = pursueMaximalQuality(maxQuality, this.deleteQuality, this.deleteProb);
		this.replaceProb = pursueMaximalQuality(maxQuality, this.replaceQuality, this.replaceProb);
		
		this.maxFound = false;
		
		return;
	}
	
	/***************************   Strategy 4   ********************************/

	private void multiArmedBandit(Representation rep) {
		ArrayList<JavaEditOperation> genome =  rep.getGenome();
		if (genome.size() == 0) {
			return;
		}
		
		String editType = genome.get(genome.size() - 1).toString();
		double reward = getReward(rep, editType);

		double chosenCountTotal = this.appendChosenCount + this.deleteChosenCount + this.replaceChosenCount;
		
		if (editType.contains("Append")) {
			System.out.println("Updating append fitness");
			System.out.println(reward);
			this.appendProb = upperConfidenceBound(this.appendChosenCount, this.appendTotalReward);
			
		} else if (editType.contains("Delete")) {
			System.out.println("Updating delete fitness");
			System.out.println(reward);
			this.deleteProb = upperConfidenceBound(this.deleteChosenCount, this.deleteTotalReward);

		} else if (editType.contains("Replace")) {
			System.out.println("Updating replace fitness");
			System.out.println(reward);
			this.replaceProb = upperConfidenceBound(this.replaceChosenCount, this.replaceTotalReward);

		} else {
			//TODO: make this throw an exception instead
			System.out.println("Unexpected Mutation Operator");
		}
		
		if (Search.model.endsWith("DMAB")) {
			if(pageHinkley(reward)) {
				restartDMAB();
			}
		}
		
		return;	
	}
	
	
	// Activates the specified algorithm
	
	public void updateOperatorProbabilities(Representation rep) {
		
		if (Search.model.endsWith("rawReward")) {
			rawRewardProbability(rep);
		} else if (Search.model.endsWith("PM")) {
			probabilityMatching(rep);
		} else if (Search.model.endsWith("AP")) {
			adaptivePursuit(rep);
		} else if (Search.model.endsWith("MAB")) {
			multiArmedBandit(rep); // For both MAB and DMAB. Differentiation in conditional inside the function
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
				prob = this.replaceProb;
				System.out.println(mutation);
				System.out.println(prob);
			}else if(mutation == Mutation.APPEND){
				prob = this.appendProb;
				System.out.println(mutation);
				System.out.println(prob);
			}else if(mutation == Mutation.DELETE){
				prob = this.deleteProb;
				System.out.println(mutation);
				System.out.println(prob);
			}else if(mutation == Mutation.SWAP){
				prob = this.replaceProb;
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



