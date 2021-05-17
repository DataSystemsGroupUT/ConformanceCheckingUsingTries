package org.processmining.logfiltering.statisticalInductiveMiner;


//This class calculates the amount of traces that have to be read in without new information
//
public class ThresholdCalculator {
	private double threshold;			//how small should p be
	private double confidencelevel;		//how confident are you in the correctness of the experiment
	
	public ThresholdCalculator(double threshold, double confidencelevel){
		this.threshold=threshold;
		this.confidencelevel=confidencelevel;
	}
	
	public int calculateThreshold(){
		int tracesRequired=0;
		boolean thresholdReached=false;
		BinomialConfidenceCalculator binComputer = new
				BinomialConfidenceCalculator(
				BinomialConfidenceCalculator.MethodType.WILSON_SCORE, confidencelevel, threshold);
		while(!thresholdReached){
			tracesRequired++;
			binComputer.processTrailResult(false);
			thresholdReached=binComputer.getConfidenceIntervals().getUpperBound() < threshold; 
		}
		return tracesRequired;
	}
	
	public int getThresholdCalculation(){
		String ThresholdCalculation="";
		int tracesRequired=0;
		boolean thresholdReached=false;
		BinomialConfidenceCalculator binComputer = new
				BinomialConfidenceCalculator(
				BinomialConfidenceCalculator.MethodType.WILSON_SCORE, confidencelevel, threshold);
		while(!thresholdReached){
			tracesRequired++;
			binComputer.processTrailResult(false);
			//ThresholdCalculation=ThresholdCalculation+"[ " +
				//	binComputer.getConfidenceIntervals().getLowerBound() + "," +
					//binComputer.getConfidenceIntervals().getUpperBound()
					 //+" ]\n";
			thresholdReached=binComputer.getConfidenceIntervals().getUpperBound() < threshold; 
		}
		ThresholdCalculation=ThresholdCalculation+"Required traces: "+tracesRequired;
		return tracesRequired;
	}
}
