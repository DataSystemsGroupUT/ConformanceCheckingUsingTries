package org.processmining.logfiltering.statisticalInductiveMiner;


import java.util.ArrayList;

import org.apache.commons.math3.stat.interval.ConfidenceInterval;
import org.apache.commons.math3.stat.interval.IntervalUtils;

public class BinomialConfidenceCalculator {

	class TestParams {
		int trails = 0;
		int successes = 0;

		TestParams(int trails, int successes, double confidenceLevel) {
			this.trails = trails;
			this.successes = successes;
		}

		@Override
		public boolean equals(Object obj) {

			TestParams t = (TestParams) obj;
			if (trails != t.trails || successes != t.successes) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			return new String(trails + "," + successes + "" + confidenceLevel).hashCode();
		}
	}

	public enum MethodType {
		CLOPPER_PEARSON, AGRESTI_COULL, WILSON_SCORE, NORMAL_APPROXIMATION
	}

	MethodType method = MethodType.CLOPPER_PEARSON;
	double confidenceLevel;
	double requiredDelta;

	ArrayList<TestParams> results = new ArrayList<TestParams>();
	boolean confidenceReached = false;
	private boolean aggregate = false;

	public BinomialConfidenceCalculator(MethodType methodType, double confidenceLevel, double requiredDelta) {
		this.method = methodType;
		this.confidenceLevel = confidenceLevel;
		this.requiredDelta = requiredDelta;
		this.aggregate = false;
		results.add(0, new TestParams(0, 0, confidenceLevel));
	}

	public void setAggregatedMode() {
		aggregate = true;
	}

	public BinomialConfidenceCalculator(MethodType methodType, double confidenceLevel, double requiredDelta,
			int trails) {
		this.method = methodType;
		this.confidenceLevel = confidenceLevel;
		this.requiredDelta = requiredDelta;
		this.aggregate = false;
		results.add(0, new TestParams(trails, 0, confidenceLevel));
	}

	public void processTrailResult(boolean success, boolean calculateInterval) {

		if (!aggregate) {
			simpleUpdate(success, calculateInterval);
		} else {
			aggregatedUpdate(success, calculateInterval);
		}

	}

	public void processTrailResult(boolean success) {
		if (!aggregate) {
			simpleUpdate(success, true);
		} else {
			aggregatedUpdate(success, true);
		}

	}

	public boolean condifenceReached() {
		return confidenceReached;
	}

	public void restart() {
		TestParams p = results.get(0);
		p.trails = 0;
		p.successes = 0;
	}

	private void simpleUpdate(boolean success, boolean calculateInterval) {

		TestParams p = results.get(0);
		p.trails++;
		if (success) {
			p.successes++;
		}
		if (!calculateInterval) {
			return;
		}
		TestParams p2 = new TestParams(p.trails, p.successes, confidenceLevel);
		ConfidenceInterval c = computeConfidence(p2);
		double delta = c.getUpperBound() - c.getLowerBound();
		if (delta <= requiredDelta) {
			confidenceReached = true;
		} else {
			confidenceReached = false;
		}
	}

	private void aggregatedUpdate(boolean success, boolean calculateInterval) {
		try {

			for (TestParams p : results) {
				p.trails++;
				if (success) {
					p.successes++;
				}
				if (calculateInterval == false) {
					continue;
				}
				ConfidenceInterval c = computeConfidence(p);
				double delta = c.getUpperBound() - c.getLowerBound();
				if (delta <= requiredDelta) {
					confidenceReached = true;
				}
			}
			results.add(0, new TestParams(1, success ? 1 : 0, confidenceLevel));
		} catch (Exception e) {
			System.out.println(e.getMessage() + "\n" + e.getCause());
		}

	}

	public ConfidenceInterval getConfidenceIntervals() {
		TestParams p = results.get(0);
		return computeConfidence(new TestParams(p.trails, p.successes, confidenceLevel));
	}

	private ConfidenceInterval computeConfidence(TestParams p) {
		if (method == MethodType.CLOPPER_PEARSON) {
			if (p.trails == p.successes) { // TODO solution to implementation
											// bug
				return IntervalUtils.getClopperPearsonInterval(p.trails + 1, p.successes, confidenceLevel);
			} else {
				return IntervalUtils.getClopperPearsonInterval(p.trails, p.successes, confidenceLevel);
			}
		}
		if (method == MethodType.AGRESTI_COULL) {
			return IntervalUtils.getAgrestiCoullInterval(p.trails, p.successes, confidenceLevel);
		}
		if (method == MethodType.WILSON_SCORE) {
			return IntervalUtils.getWilsonScoreInterval(p.trails, p.successes, confidenceLevel);
		}
		if (method == MethodType.NORMAL_APPROXIMATION) {
			if (p.trails == p.successes) { // TODO solution to implementation
											// bug
				return IntervalUtils.getNormalApproximationInterval(p.trails + 1, p.successes, confidenceLevel);
			} else {
				return IntervalUtils.getNormalApproximationInterval(p.trails, p.successes, confidenceLevel);
			}
		}
		return null;
	}

	public double getSuccess() {
		return results.get(0).successes;

	}

	public double getTrails() {
		return results.get(0).trails;

	}

	public void setSuccessfulTrails(int successes) {
		results.get(0).successes = successes - 1;
		simpleUpdate(true, true);

	}

}
