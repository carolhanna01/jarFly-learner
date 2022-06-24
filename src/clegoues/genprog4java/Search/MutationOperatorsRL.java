package clegoues.genprog4java.Search;

import java.util.ArrayList;
import java.util.List;

import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.WeightedMutation;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.mut.edits.java.JavaEditOperation;

public class MutationOperatorsRL {
	
	// For the probability matching operator selection
	double appendRawReward;
	double deleteRawReward;
	double replaceRawReward; 

	
	public MutationOperatorsRL() {
		this.appendRawReward = 1;
		this.deleteRawReward = 1;
		this.replaceRawReward = 1;
	}
	
	
	// Raw Fitness
	public void operatorCreditAssignment(Representation rep) {
		
		ArrayList<JavaEditOperation> genome =  rep.getGenome();
		if (genome.size() == 0) { //TODO: fishy- Make it an assert again + figure out why it's failing OffByOne
			return;
		}

		double currFitness = rep.getFitness(); //testFitness function in runAlgorithm calls setFitness- should be safe 		
		String currEdit = genome.get(genome.size() - 1).toString();
		
		if (currEdit.contains("Append")) {
			System.out.println("Updating append fitness");
			System.out.println(currFitness);
			this.appendRawReward = currFitness;
		} else if (currEdit.contains("Delete")) {
			System.out.println("Updating delete fitness");
			System.out.println(currFitness);
			this.deleteRawReward = currFitness;
		} else if (currEdit.contains("Replace")) {
			System.out.println("Updating replace fitness");
			System.out.println(currFitness);
			this.replaceRawReward = currFitness;
		} else {
			System.out.println("None!!!!!!!!!!!!!");
		}
		
		return;
	
	}
	
	
	public List<WeightedMutation> rescaleMutationsBasedOnPM(List<WeightedMutation> availableMutations) {
		assert(availableMutations.size() > 0);
		List<WeightedMutation> retVal = new ArrayList<WeightedMutation>();
		double totalReward = this.appendRawReward + this.deleteRawReward + this.replaceRawReward;
		
		if (totalReward == 0) {
			this.appendRawReward = 1;
			this.deleteRawReward = 1;
			this.replaceRawReward = 1;
			totalReward = 3;
		}
		
		
		for(WeightedMutation wmut: availableMutations){
			Mutation mutation = (Mutation) ((WeightedMutation)wmut).getLeft();
			double prob = 0;
			if(mutation == Mutation.REPLACE){
				prob = this.replaceRawReward / totalReward;
				System.out.println(mutation);
				System.out.println(prob);
			}else if(mutation == Mutation.APPEND){
				prob = this.appendRawReward / totalReward;
				System.out.println(mutation);
				System.out.println(prob);
			}else if(mutation == Mutation.DELETE){
				prob = this.deleteRawReward / totalReward;
				System.out.println(mutation);
				System.out.println(prob);
			}else if(mutation == Mutation.SWAP){
				prob = this.replaceRawReward / totalReward;
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
