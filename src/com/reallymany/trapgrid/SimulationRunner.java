package com.reallymany.trapgrid;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import au.com.bytecode.opencsv.CSVReader;

public class SimulationRunner {
	int numberOfSimulations;
	TrapGrid tg;
	int numberOfDays;
	int numberOfFlies;
	double diffCoeff;
	double stepSize;
	int stepsPerDay;
	double turnAngleStdev;
	boolean useMDD;
	Random rng;
	public ArrayList<SimulationResultsHolder> allResults;
	boolean outbreakLocationsProvided;
	String outbreakFile;
	ArrayList<Outbreak> outbreaks;
	boolean useSingleCaptureProb;

	public SimulationRunner(TrapGrid tg, int numDays, int numFlies, double diffC, 
			double stepSize, int stepsPerDay, double turnAngleStdev, boolean useMDD, long seed, int numSims, boolean useSingleCaptureProb) {
		this.tg = tg;
		this.numberOfDays = numDays;
		this.numberOfFlies = numFlies;
		this.diffCoeff = diffC;
		this.stepSize = stepSize;
		this.stepsPerDay = stepsPerDay;
		this.turnAngleStdev = turnAngleStdev;
		this.useMDD = useMDD;
		this.rng = new Random(seed);		
		this.numberOfSimulations = numSims;
		allResults = new ArrayList<SimulationResultsHolder>();
		outbreakLocationsProvided = false;
		this.useSingleCaptureProb = useSingleCaptureProb;
	}
	
	public SimulationRunner(TrapGrid tg, String outbreakFile, int numDays, int numFlies, double diffCoeff,
			double stepSize, int stepsPerDay, double turnAngleStdev, boolean useMDD, long seed, boolean useSingleCaptureProb) {
		this.tg = tg;
		this.numberOfDays = numDays;
		this.outbreakFile = outbreakFile;
		this.numberOfFlies = numFlies;
		this.diffCoeff = diffCoeff;
		this.stepSize = stepSize;
		this.stepsPerDay = stepsPerDay;
		this.turnAngleStdev = turnAngleStdev;
		this.useMDD = useMDD;
		this.rng = new Random(seed);
		allResults = new ArrayList<SimulationResultsHolder>();
		outbreakLocationsProvided = true;
		this.useSingleCaptureProb = useSingleCaptureProb;
	}

	Simulation createSimulation(TrapGrid t, Outbreak f) {		
		return new Simulation(t, f, numberOfDays);
	}
	
	Outbreak createRandomOutbreak(TrapGrid tg, long s) {
		boolean useRandomLocation = true;
		double xMax = tg.xMax;
		double yMax = tg.yMax;
		Outbreak f = new Outbreak(xMax, yMax, numberOfFlies, diffCoeff, stepSize, stepsPerDay, turnAngleStdev, useMDD, s, useRandomLocation);
		return f;
	}

	public void runSimulations() {
		if (! outbreakLocationsProvided) {
			for (int i=0; i<numberOfSimulations; i++) {
				long nextLong = rng.nextLong();
				Outbreak ob = createRandomOutbreak(tg, nextLong);
				Simulation sim = createSimulation(tg, ob);
				SimulationResultsHolder oneResult;
				if(useSingleCaptureProb == true) {
					oneResult = sim.runSimulationSingleCapture();
				}
				else {
					oneResult = sim.runSimulationAverageDailyCapture();
				}
				allResults.add(oneResult);
			}
		} else {
			try {
				this.outbreaks = createOutbreaksFromFile(outbreakFile);
			} catch (IOException e) {
				System.err.println("Error trying to read outbreak file");
				e.printStackTrace();
				System.exit(-1);
			}
			for (Outbreak ob : this.outbreaks) {
				Simulation sim = createSimulation(tg, ob);
				SimulationResultsHolder oneResult;
				if(useSingleCaptureProb == true) {
					oneResult = sim.runSimulationSingleCapture();
				}
				else {
					oneResult = sim.runSimulationAverageDailyCapture();
				}
				allResults.add(oneResult);
			}
		}		
	}

	public ArrayList<Outbreak> createOutbreaksFromFile(String filename) throws IOException {
		ArrayList<Outbreak> myOutbreaks = new ArrayList<Outbreak>();
		boolean useRandomLocation = false;
		try {
			CSVReader reader = new CSVReader(new FileReader(filename), '\t');
			String[] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				if (nextLine.length == 2) {
					double x = Double.parseDouble(nextLine[0]);
					double y = Double.parseDouble(nextLine[1]);
					long nextLong = rng.nextLong();
					Outbreak ob = new Outbreak(x, y, numberOfFlies, diffCoeff, stepSize, 
							stepsPerDay, turnAngleStdev, useMDD, nextLong, useRandomLocation);
					myOutbreaks.add(ob);					
				} else {
					System.err.println("Invalid input! TrapGrid file must have 3 values per line (x, y, lambda).");
					reader.close();
					throw new IOException();
				}
			}
			reader.close();			
			return myOutbreaks;	
		} catch (FileNotFoundException e) {
			System.err.println("Input file " + filename + " not found");
			e.printStackTrace();
			throw new FileNotFoundException();
		} 
	}	

}
