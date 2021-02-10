package com.reallymany.trapgrid;

import java.io.File;
import java.io.IOException;

public class Driver {	
	static String trapGridFile = null;
	static String outbreakFile = null;
	static long randomSeed = 0;
	static String releasePointFile = null;
	static int numberOfDays = 0;
	static int numberOfSimulations = 0;
	static double tolerance = 0.0001;
	static int numberOfFlies = 0;		
	static double diffCoeff = 0.0;	
	static double stepSize = 0.0;
	static int stepsPerDay = 0;
	static double turnAngleStdev = 0.0;
	static boolean useMDD = false;
	static boolean calculateAverageEscapeProbability = false;
	static boolean calculateOneOrMoreCapture = false;
	static boolean outbreakLocationsProvided = false;
	static TrapGrid tg;
	static Outbreak fr;
	static SimulationRunner simRunner;
	static String usageMessage = "Usage: java -jar TrapGrid.jar -tg <TrapGrid file> [-ob <outbreak file>] " +
			"[-nd <number of days>] [-ns <number of simulations>]\n[-nf <number of flies per outbreak>] " +
			"[-dc <diffusion coefficient>] [-s <seed for random number generator>]\n" +
			"[--step-size <step size>] [--steps-per-day <steps per day>]\n" +
			"[--turn-angle-stdev <standard deviation for turn angles>]\n" +
			"[-t <tolerance for TrapGrid average escape probability calculator>] " +
			"[--calculateAvgEscProb]\n" + "[--calculateOneOrMoreCapture]\n" +
			"(Type 'java -jar TrapGrid.jar --help' for more detailed info or --version for version info.)\n";

	static String helpMessage = usageMessage + "\n" + "Parameters in [brackets] are optional.\n\n" +
			"TrapGrid file is a tab-separated file. The first line should give x and y values " + 
			"representing the upper-right corner of a rectangle (whose lower-left corner is (0,0)).\n" +
			"Each subsequent line should have three entries -- x, y and lambda representing the " + 
			"position and attractiveness of each trap.\n" + 
			"You may supply an optional Outbreak file, which is a two-column tab-delimited file" + 
			"containing the x and y locations of outbreaks to be simulated.\n" + 
			"If you provide this file, the program will run one simulation per location.\n" +
			"The default mode uses a diffusion model for insect dispersal. However, if you\n" + 
			"provide a step size, steps per day and turn angle standard deviation, a\n" + 
			"Mean Dispersal Distance model is used.\n\n";
	
	static String version = "2020-09-23";
			
	/**
	 * Processes arguments and runs simulation.
	 * @param args
	 */
	public static void main(String[] args) {	
		
		processArguments(args);
		
		// TrapGrid file must be supplied
		if (trapGridFile == null) {
			System.err.println("Sorry, the argument '-tg <TrapGrid file>' is required.");
			System.exit(-1);
		}
		
		
		// Set defaults for any arguments not provided...		
		if (randomSeed == 0) {
			randomSeed = System.currentTimeMillis();
			System.err.println("No seed provided for random number generator; using " + randomSeed);
		}
		
		if (numberOfDays == 0) {
			numberOfDays = 10;
			System.err.println("No value specified for number of days; will run simulation for 10 days.\n");
		}
		
		if (numberOfSimulations == 0) {
			numberOfSimulations = 1;
			System.err.println("No value specified for number of simulations; will run 1 simulation.\n");
		}
		
		if (numberOfFlies == 0) {
			numberOfFlies = 500;
			System.err.println("No value specified for number of flies; will use 500 flies per release.\n");
		}
		
		if (diffCoeff == 0.0) {
			diffCoeff = 30.0;
			System.err.println("No value specified for diffusion coefficient; will use 30.0\n");
		}
		
		// If stepSize, stepsPerDay and turnAngleStdev supplied, use MDD model. Otherwise, it's diffusion.
		if ( (stepSize != 0) && (stepsPerDay != 0) && (turnAngleStdev != 0) ) {
			useMDD = true;
		}
				
		// Create TrapGrid
		try {
			tg = new TrapGrid(trapGridFile);			
		} catch (NumberFormatException e) {
			System.err.println("NumberFormatException with TrapGrid file, sorry.");
			e.printStackTrace();
			return;
		} catch (IOException e) {
			System.err.println("IOException with TrapGrid file, sorry.");
			e.printStackTrace();
			return;
		}		
		
		// Calculate and print TrapGrid info...
		System.out.println("######################## TrapGrid information ####################");
		System.out.println("#" + tg.toString());
		System.out.println("#Version "+version);
		if (calculateAverageEscapeProbability) {
			System.err.println("Calculating average escape probability for TrapGrid using tolerance of " + tolerance);
			System.err.println("This could take a while...");
			Double escapeProb = tg.calculateAverageEscapeProbability(randomSeed, tolerance);
			System.out.println("#...average escape probability for TrapGrid is " + escapeProb);
		}		
		System.out.println("##################################################################\n");
		// Are we using TGO or TGA (@NCM)
		if (calculateOneOrMoreCapture) {
			System.err.println("Calculating probability of one or more capture ");
		}
		
		// Print header lines to summarize parameters
		System.out.println("######################## Parameters ##############################");
		System.out.println("#Number of days: " + numberOfDays);
		System.out.println("#Number of simulations: " + numberOfSimulations);
		System.out.println("#Number of flies per outbreak: " + numberOfFlies);
		if (useMDD) {
			System.out.println("#Step size: " + stepSize);
			System.out.println("#Steps per day: " + stepsPerDay);
			System.out.println("#Turn angle stdev: " + turnAngleStdev);
		} else {
			System.out.println("#Diffusion coefficient: " + diffCoeff);
		}
		if (calculateOneOrMoreCapture) {
			System.out.println("Escape Probability Calculation: One or more capture (TGA)");
		}
		else {
			System.out.println("Escape Probability Calculation: Average capture (TGO)");
		}
		System.out.println("#Random seed: " + randomSeed);
		System.out.println("##################################################################\n");

		// Now run simulations
		if (! outbreakLocationsProvided) {
			simRunner = 
					new SimulationRunner(tg, numberOfDays, numberOfFlies, diffCoeff, 
							stepSize, stepsPerDay, turnAngleStdev, useMDD, randomSeed, numberOfSimulations, calculateOneOrMoreCapture);
			simRunner.runSimulations();
		} else {	
			simRunner = 
					new SimulationRunner(tg, outbreakFile, numberOfDays, numberOfFlies, diffCoeff, 
							stepSize, stepsPerDay, turnAngleStdev, useMDD, randomSeed, calculateOneOrMoreCapture);
			simRunner.runSimulations();
		}
		
		
		// Print results
		// first the average cumulative probabilities
		System.out.println("\n#Averaged Simulation Results\n");
		SimulationResultsHolderAggregator agg = new SimulationResultsHolderAggregator(simRunner.allResults);
		System.out.println(agg.aggregrateSimulationResultsHolders());
		System.out.println("##################################################################\n");

		
		// then the daily summaries
		System.out.println("#Simulation Results:\n");
		for (SimulationResultsHolder sr : simRunner.allResults) {
			System.out.println(sr.summarize());
		}
		
	}

	/**
	 * Reads command line arguments and updates variables accordingly; does some input validation.
	 * @param args
	 */
	private static void processArguments(String[] args) {
		if (args.length == 0) {
			System.err.println(usageMessage);
			System.exit(1);
		} else if (args.length == 1 && args[0].equals("--help")) {
			System.out.println(helpMessage);
			System.exit(0);
		} else if (args.length == 1 && args[0].equals("--version")) {
			System.out.println(version);
			System.exit(0);
		} else {
			for (int i=0; i<args.length; i++) {
				if (args[i].equals("-tg")) {
					confirmFileExists(args[i+1]);
					trapGridFile = args[i+1];
				} else if (args[i].equals("-rp")) {
					confirmFileExists(args[i+1]);
					releasePointFile = args[i+1];					
				} else if (args[i].equals("-nd")) {
					try {
						numberOfDays = Integer.parseInt(args[i+1]);
					} catch (NumberFormatException e) {
						System.out.println("Sorry, " + args[i+1] + " is not a valid number of days. Exiting.");
						e.printStackTrace();
						System.exit(-1);
					}
				} else if (args[i].equals("-ns")) {
					try {
						numberOfSimulations = Integer.parseInt(args[i+1]);
					} catch (NumberFormatException e) {
						System.err.println("Sorry, " + args[i+1] + " is not a valid number of simulations. Exiting.");
						e.printStackTrace();
						System.exit(-1);
					}
				} else if (args[i].equals("-nf")) {
					try {
						numberOfFlies = Integer.parseInt(args[i+1]);
					} catch (NumberFormatException e) {
						System.err.println("Sorry, " + args[i+1] + " is not a valid number of flies. Exiting.");
						e.printStackTrace();
						System.exit(-1);
					}
				} else if (args[i].equals("-dc")) {
					try {
						diffCoeff = Double.parseDouble(args[i+1]);
					} catch (NumberFormatException e) {
						System.err.println("Sorry, " + args[i+1] + " is not a valid diffusion coefficient. Exiting.");
						e.printStackTrace();
						System.exit(-1);
					}
				} else if (args[i].equals("-s")) {
					try {
						randomSeed = Long.parseLong(args[i+1]);
					} catch (NumberFormatException e) {
						System.err.println("Sorry, " + args[i+1] + " is not a valid seed. Exiting.");
						e.printStackTrace();
						System.exit(-1);
					}
				} else if (args[i].equals("-ob")) {
					outbreakLocationsProvided = true;
					outbreakFile = args[i+1];
				} else if (args[i].equals("-t")) {
					try {
						tolerance = Double.parseDouble(args[i+1]);
					} catch (NumberFormatException e) {
						System.err.println("Sorry, " + args[i+1] + " is not a valid tolerance. Exiting.");
						e.printStackTrace();
						System.exit(-1);
					}
				} else if (args[i].equals("--calculateAvgEscProb")) {
					calculateAverageEscapeProbability = true;
				} else if (args[i].equals("--calculateOneOrMoreCapture")){
					calculateOneOrMoreCapture = true;
				} else if (args[i].equals("--step-size")) {
					try {
						stepSize = Double.parseDouble(args[i+1]);
					} catch (NumberFormatException e) {
						System.err.println("Sorry, " + args[i+1] + " is not a valid step size. Exiting.");
						e.printStackTrace();
						System.exit(-1);
					}
				} else if (args[i].equals("--steps-per-day")) {
					try {
						stepsPerDay = Integer.parseInt(args[i+1]);
					} catch (NumberFormatException e) {
						System.err.println("Sorry, " + args[i+1] + " is not a valid number of steps per day. Exiting.");
						e.printStackTrace();
						System.exit(-1);
					}
				} else if (args[i].equals("--turn-angle-stdev")) {
					try {
						turnAngleStdev = Double.parseDouble(args[i+1]);
					} catch (NumberFormatException e) {
						System.err.println("Sorry, " + args[i+1] + " is not a valid turn angle stdev. Exiting.");
						e.printStackTrace();
						System.exit(-1);
					}
				} else {
					continue;
				}				
			}
		}		
	}

	/**
	 * Does what it says.
	 * @param filename
	 */
	private static void confirmFileExists(String filename) {
		if (new File(filename).isFile()) {
			return;
		} else {
			System.err.println("Looks like file " + filename + " isn't a file? Exiting now.");
			System.exit(-1);
		}
	}

}
