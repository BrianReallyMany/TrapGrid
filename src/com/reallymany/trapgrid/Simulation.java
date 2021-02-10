package com.reallymany.trapgrid;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Amazing class for simulating dispersion and calculating capture probability on a grid
 * @author bhall
 * @param t
 * @param f
 * @param n
 * @param out
 * @param r
 */
public class Simulation {
	int numberOfDays;
	double cumulativeProb = 1;
	TrapGrid tg;
	Outbreak fr;
	SimulationResultsHolder resultsHolder;	
	
	public Simulation(TrapGrid t, Outbreak f, int numDays) {
		this.tg = t;
		this.fr = f;
		this.numberOfDays = numDays;
	}	
	

	/**
	 * Main method for simulation; also writes results to output files; calculation for average daily capture probability.
	 */
	public SimulationResultsHolder runSimulationAverageDailyCapture() {
		resultsHolder = new SimulationResultsHolder();
		resultsHolder.addFlyReleaseInfo(fr.toString());
		for (int i = 1; i<=numberOfDays; i++) {
			System.err.println("Running simulation for day " + i + "...");
			double totalProbForDay = 0; //TGO
			int numberOfFlies = 0;  //TGO
			Iterator<OutbreakLocation> releasePointItr = fr.allOutbreakLocations.iterator();
			while (releasePointItr.hasNext()) {
				OutbreakLocation currentReleasePoint = releasePointItr.next();
				ArrayList<Point2D.Double> flyLocations = currentReleasePoint.locateFlies(i);
				Iterator<Point2D.Double> flyLocationItr = flyLocations.iterator();
				while (flyLocationItr.hasNext()) {
					Point2D.Double currentLocation = flyLocationItr.next();
					Double currentEscapeProb = tg.getTotalEscapeProbability(currentLocation);
					String[] results = {Integer.toString(i), currentReleasePoint.shortString(),
							locationToString(currentLocation), Double.toString(currentEscapeProb)};
					resultsHolder.addRawData(results);
					totalProbForDay += currentEscapeProb; //TGO
					numberOfFlies += 1;  //TGO
				}				
			}				
			double avgForDay = totalProbForDay / numberOfFlies; //TGO
			cumulativeProb *= avgForDay;
			resultsHolder.addAvgEscapeProbability(i, avgForDay);
		}					
		System.err.println("Simulation complete!");
		return resultsHolder;
	}
	
	
	/**
	 * Main method for simulation; also writes results to output files; calculation for one or more insects being captured.
	 */
	public SimulationResultsHolder runSimulationSingleCapture() {
		resultsHolder = new SimulationResultsHolder();
		resultsHolder.addFlyReleaseInfo(fr.toString());
		for (int i = 1; i<=numberOfDays; i++) {
			System.err.println("Running simulation for day " + i + "...");
			double totalProbForDay = 1;  //TGA
			Iterator<OutbreakLocation> releasePointItr = fr.allOutbreakLocations.iterator();
			while (releasePointItr.hasNext()) {
				OutbreakLocation currentReleasePoint = releasePointItr.next();
				ArrayList<Point2D.Double> flyLocations = currentReleasePoint.locateFlies(i);
				Iterator<Point2D.Double> flyLocationItr = flyLocations.iterator();
				while (flyLocationItr.hasNext()) {
					Point2D.Double currentLocation = flyLocationItr.next();
					Double currentEscapeProb = tg.getTotalEscapeProbability(currentLocation);
					String[] results = {Integer.toString(i), currentReleasePoint.shortString(),
							locationToString(currentLocation), Double.toString(currentEscapeProb)};
					resultsHolder.addRawData(results);
					totalProbForDay *= currentEscapeProb; //TGA
				}				
			}
			double avgForDay = totalProbForDay; //TGA
			cumulativeProb *= avgForDay;
			resultsHolder.addAvgEscapeProbability(i, avgForDay);
		}					
		System.err.println("Simulation complete!");
		return resultsHolder;
	}
	

	/**
	 * Converts a Point2D.Double to simple "(x,y)" String represenation
	 * @param p
	 * @return
	 */
	private static String locationToString(Point2D.Double p) {
		String result = "(" + p.getX() + ", " + p.getY() + ")";
		return result;
	}

}
