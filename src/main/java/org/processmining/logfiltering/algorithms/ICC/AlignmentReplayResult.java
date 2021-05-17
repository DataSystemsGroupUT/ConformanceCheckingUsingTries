package org.processmining.logfiltering.algorithms.ICC;

import java.util.Map;

public class AlignmentReplayResult {
	private double fitness;
	private long time;
	private int logSize;
	private int totalNoAsynchMoves;
	private Map<String, Integer> asynchMovesAbs;
	private Map<String, Double> asynchMovesRel;
	
	public AlignmentReplayResult(double fitness, long time, int logSize, int totalNoAsynchMoves, Map<String, Integer> asynchMovesAbs, Map<String, Double> asynchMovesRel) {
		this.setFitness(fitness);
		this.setTime(time);
		this.setLogSize(logSize);
		this.setTotalNoAsynchMoves(totalNoAsynchMoves);
		this.setAsynchMovesAbs(asynchMovesAbs);
		this.setAsynchMovesRel(asynchMovesRel);
	}
	
	public String toString() {
		return this.getFitness()+"; "+this.getTime()+"; "+this.getLogSize()+"; "+this.getTotalNoAsynchMoves()+"; "+getAsynchMovesAbs().toString()+"; "+getAsynchMovesRel().toString();

	}

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	public int getLogSize() {
		return logSize;
	}

	public void setLogSize(int logSize) {
		this.logSize = logSize;
	}

	public int getTotalNoAsynchMoves() {
		return totalNoAsynchMoves;
	}

	public void setTotalNoAsynchMoves(int totalNoAsynchMoves) {
		this.totalNoAsynchMoves = totalNoAsynchMoves;
	}

	public Map<String, Integer> getAsynchMovesAbs() {
		return asynchMovesAbs;
	}

	public void setAsynchMovesAbs(Map<String, Integer> asynchMovesAbs) {
		this.asynchMovesAbs = asynchMovesAbs;
	}

	public Map<String, Double> getAsynchMovesRel() {
		return asynchMovesRel;
	}

	public void setAsynchMovesRel(Map<String, Double> asynchMovesRel) {
		this.asynchMovesRel = asynchMovesRel;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
}
