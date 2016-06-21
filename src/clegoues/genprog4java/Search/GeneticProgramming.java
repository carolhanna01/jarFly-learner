package clegoues.genprog4java.Search;

import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.rep.JavaRepresentation;
import clegoues.genprog4java.rep.Representation;

public class GeneticProgramming<G extends EditOperation> extends Search<G>{
	private int generationsRun = 0;

	public GeneticProgramming(Fitness<G> engine) {
		super(engine);
	}


	/*
	 * prepares for GA by registering available mutations (including templates
	 * if applicable) and reducing the search space, and then generates the
	 * initial population, using [incoming_pop] if non-empty, or by randomly
	 * mutating the [original]. The resulting population is evaluated for
	 * fitness before being returned. This may terminate early if a repair is
	 * found in the initial population (by [calculate_fitness]).
	 * 
	 * @param original original variant
	 * 
	 * @param incoming_pop possibly empty, incoming population
	 * 
	 * @return initial_population generated by mutating the original
	 */
	protected Population<G> initialize(Representation<G> original,
			Population<G> incomingPopulation) throws RepairFoundException {
		original.reduceSearchSpace();

		Population<G> initialPopulation = incomingPopulation;

		if (incomingPopulation != null
				&& incomingPopulation.size() > incomingPopulation.getPopsize()) {
			initialPopulation = incomingPopulation.firstN(incomingPopulation
					.getPopsize());
		} 
		int stillNeed = initialPopulation.getPopsize()
				- initialPopulation.size();
		if (stillNeed > 0) {
			initialPopulation.add(original.copy());
			stillNeed--;
		}
		for (int i = 0; i < stillNeed; i++) {
			Representation<G> newItem = original.copy();
			boolean itIsTheSame = false;

			this.mutate(newItem);
			itIsTheSame = populationIsTheSameAsAPreviousOne(initialPopulation, newItem);
			if(itIsTheSame){
				//if it is making repeated representattions in the same population change the possible faulty stmts to all the possible ones and re make it
				//newItem = original.copy();
				((JavaRepresentation)newItem).setAllPossibleStmtsToFaultyLocalization();
				((JavaRepresentation)newItem).setAllPossibleStmtsToFixLocalization();
				this.mutate(newItem);
			}

			initialPopulation.add(newItem);
		}

		for (Representation<G> item : initialPopulation) {
			if (fitnessEngine.testFitness(0, item)) {
				this.noteSuccess(item, original, 0);
				if(!continueSearch) {
					throw new RepairFoundException();
				}
			}
		}
		return initialPopulation;
	}

	private boolean populationIsTheSameAsAPreviousOne(Population<G> pop, Representation<G> newItem){
		for (Representation<G> representation : pop) {
			for(int i = 0; i< representation.getGenome().size() && i < newItem.getGenome().size(); ++i){
				boolean locIsTheSame = representation.getGenome().get(i).getLocation().getId() == newItem.getGenome().get(i).getLocation().getId();
				boolean mutIsTheSame = representation.getGenome().get(i).getType().equals(newItem.getGenome().get(i).getType());
				boolean holesIsTheSame = representation.getGenome().get(i).getHoles().toString().equalsIgnoreCase(newItem.getGenome().get(i).getHoles().toString());
				if(locIsTheSame && mutIsTheSame && holesIsTheSame){
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * runs the genetic algorithm for a certain number of iterations, given the
	 * most recent/previous generation as input. Returns the last generation,
	 * unless it is killed early by the search strategy/fitness evaluation. The
	 * optional parameters are set to the obvious defaults if omitted.
	 * 
	 * @param start_gen optional; generation to start on (defaults to 1)
	 * 
	 * @param num_gens optional; number of generations to run (defaults to
	 * [generations])
	 * 
	 * @param incoming_population population produced by the previous iteration
	 * 
	 * @raise Found_Repair if a repair is found
	 * 
	 * @raise Max_evals if the maximum fitness evaluation count is reached
	 * 
	 * @return population produced by this iteration *)
	 */
	protected void runAlgorithm(Representation<G> original, Population<G> initialPopulation) throws RepairFoundException {
		/*
		 * the bulk of run_ga is performed by the recursive inner helper
		 * function, which Claire modeled off the MatLab code sent to her by the
		 * UNM team
		 */
		logger.info("search: genetic algorithm begins\n");

		assert (Search.generations >= 0);
		Population<G> incomingPopulation = this.initialize(original,
				initialPopulation);
		int gen = 1;
		while (gen < Search.generations) {
			logger.info("search: generation" + gen);
			generationsRun++;
			assert (initialPopulation.getPopsize() > 0);
			// Step 1: selection
			incomingPopulation.selection(incomingPopulation.getPopsize());
			// step 2: crossover
			incomingPopulation.crossover(original);

			// step 3: mutation
			for (Representation<G> item : incomingPopulation) {
				Representation<G> newItem = original.copy();
				boolean itIsTheSame = false;
				this.mutate(item);
				itIsTheSame = populationIsTheSameAsAPreviousOne(initialPopulation, newItem);
				if(itIsTheSame){
					//if it is making repeated representattions in the same population change the possible faulty stmts to all the possible ones and re make it
					newItem = original.copy();
					((JavaRepresentation)newItem).setAllPossibleStmtsToFaultyLocalization();
					((JavaRepresentation)newItem).setAllPossibleStmtsToFixLocalization();
					this.mutate(newItem);
				}
			}

			// step 4: fitness
			int count = 0;
			for (Representation<G> item : incomingPopulation) {
				count++;
				if (fitnessEngine.testFitness(gen, item)) {
					this.noteSuccess(item, original, gen);
					if(!continueSearch) 
						return;
				}
			}
			logger.info("Generation: " + gen + " I think I tested " + count + " variants.");
			gen++;
		}
	}
}
