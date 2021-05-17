package org.processmining.logfiltering.algorithms.ICC;

public class IccResult {

	private int traces;
	private long time;
	private double fitness;
	private ReplayResultsContainer alignmentContainer;
	
	public IccResult(int traces, long time, double fitness, ReplayResultsContainer alignmentContainer) {
		this.setTraces(traces);
		this.setTime(time);
		this.setFitness(fitness);
		this.setAlignmentContainer(alignmentContainer);
	}
	
	public String toString() {
		return "Fitness: "+this.getFitness()+", Traces: "+this.getTraces()+", Time (ms): "+this.getTime()+"\n"+
				"AsynchMoves: "+this.getAlignmentContainer().getAsynchMoves();
	}

	public ReplayResultsContainer getAlignmentContainer() {
		return alignmentContainer;
	}

	public void setAlignmentContainer(ReplayResultsContainer alignmentContainer) {
		this.alignmentContainer = alignmentContainer;
	}

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public int getTraces() {
		return traces;
	}

	public void setTraces(int traces) {
		this.traces = traces;
	}
	
}
