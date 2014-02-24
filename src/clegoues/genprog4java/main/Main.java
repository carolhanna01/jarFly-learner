/*
 * Copyright (c) 2014-2015, 
 *  Claire Le Goues     <clegoues@cs.cmu.edu>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. The names of the contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package clegoues.genprog4java.main;
import java.io.File;
import java.io.IOException;

import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.mut.JavaEditOperation;
import clegoues.genprog4java.rep.JavaRepresentation;
import clegoues.genprog4java.rep.LocalizationRep;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.rep.UnexpectedCoverageResultException;
import clegoues.genprog4java.search.Population;
import clegoues.genprog4java.search.RepairFoundException;
import clegoues.genprog4java.search.Search;

public class Main {

	public static void main(String[] args) throws IOException, UnexpectedCoverageResultException
	{
		Search searchEngine = null;
		Representation baseRep = null;
		Fitness fitnessEngine = null;
		Population incomingPopulation = null;
		assert(args.length > 0);
		long startTime = System.currentTimeMillis();

		Configuration.setProperties(args[0]);
		File workDir = new File(Configuration.outputDir);
		if(!workDir.exists())
			workDir.mkdir();
		System.out.println("Configuration file loaded");
		if(Configuration.globalExtension == ".java") {
			if(Search.searchStrategy == "io") {
				baseRep = (Representation) new LocalizationRep();
			} else {
			baseRep = (Representation) new JavaRepresentation();
			}
			fitnessEngine = new Fitness<JavaEditOperation>();
			searchEngine = new Search<JavaEditOperation>(fitnessEngine);
			incomingPopulation = new Population<JavaEditOperation>(); // FIXME: read from incoming if applicable!
		}
		baseRep.load(Configuration.targetClassName);
		try {
			switch(Search.searchStrategy) {
			case "ga": searchEngine.geneticAlgorithm(baseRep, incomingPopulation);
				break;
			case "brute": searchEngine.bruteForceOne(baseRep);
				break;
			case "oracle": searchEngine.oracleSearch(baseRep);
				break;
			case "io" : searchEngine.ioSearch(baseRep);
				break;
			}
		} catch(RepairFoundException e) {
			// FIXME: this is stupid
		} catch (CloneNotSupportedException e) {
			e.printStackTrace(); 
		}
		int elapsed = getElapsedTime(startTime);
		System.out.printf("\nTotal elapsed time: " + elapsed + "\n");
		Runtime.getRuntime().exit(0);

	}

	private static int getElapsedTime(long start)
	{
		return (int) ( System.currentTimeMillis() - start ) / 1000;
	}
}
