package org.processmining.logfiltering.algorithms.ICC;



public class IccParameters {

	private double epsilon;
	private double delta;
	private double alpha;
	private double k;
	private int initialSize;
	private String goal;
	private boolean approximate;
	
	public IccParameters(double delta, double alpha, double epsilon, double k, int initialSize, String goal, boolean approximate) {
		this.setEpsilon(epsilon);
		this.setDelta(delta);
		this.setAlpha(alpha);
		this.setK(k);
		this.setInitialSize(initialSize);
		this.setGoal(goal);
		this.setApproximate(approximate);
	}
	
	public String toString() {
		return "Delta: "+this.getDelta()+", Alpha: "+this.getAlpha()+", Epsilon: "+this.getEpsilon()+", K: "+this.getK()+", initial Size: "+this.getInitialSize()+", goal: "+this.getGoal()+", approximate: "+this.isApproximate();
	}

	public double getEpsilon() {
		return epsilon;
	}

	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}

	public int getInitialSize() {
		return initialSize;
	}

	public void setInitialSize(int initialSize) {
		this.initialSize = initialSize;
	}

	public String getGoal() {
		return goal;
	}

	public void setGoal(String goal) {
		this.goal = goal;
	}

	public boolean isApproximate() {
		return approximate;
	}

	public void setApproximate(boolean approximate) {
		this.approximate = approximate;
	}

	public double getDelta() {
		return delta;
	}

	public void setDelta(double delta) {
		this.delta = delta;
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	public double getK() {
		return k;
	}

	public void setK(double k) {
		this.k = k;
	}
}
