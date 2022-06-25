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
	double appendRawReward;
	double deleteRawReward;
	double replaceRawReward; 
	/***************************************************************************/

	/******  For Strategy 2: probability matching operator selection  ******/
	// Parameters
	// TODO: tune + make not hard coded
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
	
	/***************************************************************************/
	
	public MutationOperatorsRL() {
		
		this.appendRawReward = 1;
		this.deleteRawReward = 1;
		this.replaceRawReward = 1;
		
		this.Pmin = 0.01;
		this.operators_num = 3;
		this.alpha = 0.01;
		
		this.appendProb = 1 / this.operators_num;
		this.deleteProb = 1 / this.operators_num;
		this.replaceProb = 1 / this.operators_num;
		
		this.appendQuality = 1;
		this.deleteQuality = 1;
		this.replaceQuality = 1;
		
		this.Pmax = 1 - (this.operators_num - 1) * this.Pmin;
		this.beta = 0.01;
	}
	
	private void rawRewardProbability(Representation rep) {
		ArrayList<JavaEditOperation> genome =  rep.getGenome();
		if (genome.size() == 0) { //TODO: fishy- Make it an assert again + figure out why it's failing OffByOne
			return;
		}

		double currFitness = rep.getFitness(); //testFitness function in runAlgorithm calls setFitness- should be safe 		
		String currEdit = genome.get(genome.size() - 1).toString();
		
		if (currEdit.contains("Append")) {
//			System.out.println("Updating append fitness");
//			System.out.println(currFitness);
			this.appendRawReward = currFitness;
			double totalReward = this.appendRawReward + this.deleteRawReward + this.replaceRawReward;
			this.appendProb = this.appendRawReward / totalReward;
			
		} else if (currEdit.contains("Delete")) {
//			System.out.println("Updating delete fitness");
//			System.out.println(currFitness);
			this.deleteRawReward = currFitness;
			double totalReward = this.appendRawReward + this.deleteRawReward + this.replaceRawReward;
			this.deleteProb = this.deleteRawReward / totalReward;
			
		} else if (currEdit.contains("Replace")) {
//			System.out.println("Updating replace fitness");
//			System.out.println(currFitness);
			this.replaceRawReward = currFitness;
			double totalReward = this.appendRawReward + this.deleteRawReward + this.replaceRawReward;
			this.replaceProb = this.replaceRawReward / totalReward;
		}
		
		return;
	}
	
	private void probabilityMatching(Representation rep) {
		
		ArrayList<JavaEditOperation> genome =  rep.getGenome();
		if (genome.size() == 0) {
			return;
		}
		
		// Raw Reward
		double reward = rep.getFitness(); //testFitness function in runAlgorithm calls setFitness- should be safe 		
		String editType = genome.get(genome.size() - 1).toString();
		
		double quality_sum = this.appendQuality + this.deleteQuality + this.replaceQuality;

		if (editType.contains("Append")) {
			System.out.println("Updating append fitness");
			System.out.println(reward);
			double quality = this.appendQuality;
			this.appendQuality = quality + alpha * (reward - quality);
			this.appendProb = this.Pmin + (1 - this.operators_num * this.Pmin) * (quality/ quality_sum);
			
		} else if (editType.contains("Delete")) {
			System.out.println("Updating delete fitness");
			System.out.println(reward);
			double quality = this.deleteQuality;
			this.deleteQuality = quality + alpha * (reward - quality);
			this.deleteProb = this.Pmin + (1 - this.operators_num * this.Pmin) * (quality/ quality_sum);

		} else if (editType.contains("Replace")) {
			System.out.println("Updating replace fitness");
			System.out.println(reward);
			double quality = this.replaceQuality;
			this.replaceQuality = quality + alpha * (reward - quality);
			this.replaceProb = this.Pmin + (1 - this.operators_num * this.Pmin) * (quality/ quality_sum);

		} else {
			//TODO: make this throw an exception instead
			System.out.println("Unexpected Mutation Operator");
		}
		
		return;
	
	}
	
	private double pursueMaximalQuality(double maxQuality, double quality, double prob) {
		if (maxQuality == quality) {
			return prob + this.beta * (this.Pmax - prob);
		} else {
			return prob + this.beta * (this.Pmin - prob);
		}
	}
	
	private void adaptivePursuit(Representation rep) {
		
		System.out.println("DEBUGGGGG!! I'm updating the probabilities");
		
		ArrayList<JavaEditOperation> genome =  rep.getGenome();
		if (genome.size() == 0) {
			return;
		}
		
		// Raw Reward
		double reward = rep.getFitness(); //testFitness function in runAlgorithm calls setFitness- should be safe 		
		String editType = genome.get(genome.size() - 1).toString();
		
		if (editType.contains("Append")) {
			System.out.println("Updating append fitness");
			System.out.println(reward);
			double quality = this.appendQuality;
			this.appendQuality = quality + alpha * (reward - quality);
			
		} else if (editType.contains("Delete")) {
			System.out.println("Updating delete fitness");
			System.out.println(reward);
			double quality = this.deleteQuality;
			this.deleteQuality = quality + alpha * (reward - quality);

		} else if (editType.contains("Replace")) {
			System.out.println("Updating replace fitness");
			System.out.println(reward);
			double quality = this.replaceQuality;
			this.replaceQuality = quality + alpha * (reward - quality);

		} else {
			//TODO: make this throw an exception instead
			System.out.println("Unexpected Mutation Operator");
		}
		
		double maxQuality = Math.max(this.appendQuality, Math.max(this.deleteQuality, this.replaceQuality));
		
		System.out.println("START DEBUG");
		System.out.println(maxQuality);
		System.out.println(this.appendQuality);
		System.out.println(this.deleteQuality);
		System.out.println(this.replaceQuality);
		System.out.println("ENDDEBUG");

		this.appendProb = pursueMaximalQuality(maxQuality, this.appendQuality, this.appendProb);
		this.deleteProb = pursueMaximalQuality(maxQuality, this.deleteQuality, this.deleteProb);
		this.replaceProb = pursueMaximalQuality(maxQuality, this.replaceQuality, this.replaceProb);

		return;
	}
	
	public void updateOperatorProbabilities(Representation rep) {
		
		if (Search.model.endsWith("rawReward")) {
			rawRewardProbability(rep);
		} else if (Search.model.endsWith("PM")) {
			probabilityMatching(rep);
		} else if (Search.model.endsWith("AP")) {
			adaptivePursuit(rep);
		} 
	}
	
	
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



