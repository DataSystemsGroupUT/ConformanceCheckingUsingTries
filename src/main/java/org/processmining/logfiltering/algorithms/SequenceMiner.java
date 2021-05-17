package org.processmining.logfiltering.algorithms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.processmining.logfiltering.algorithms.SPMF.AlgoPrefixSpan;
import org.processmining.logfiltering.algorithms.SPMF.PatternSequenceDatabase;
import org.processmining.logfiltering.algorithms.SPMF.SequentialPattern;
import org.processmining.logfiltering.algorithms.SPMF.SequentialPatterns;

public class SequenceMiner {
	public static int MaximumPatternLength =2 ;

	public static void setMaximumPatternLength(int maximumPatternLength) {
		MaximumPatternLength = maximumPatternLength;
	}

	public static  String [] ResultedPatterns ;
	public static List<List<Integer>> TraceIDs=new ArrayList<List<Integer>>(0); 
	
	public  void apply(PatternSequenceDatabase patternSequenceDatabase, double HighThreshold) throws IOException{  
		// input file
		//String inputFile = TempFileAddress;
		
		// Create an instance of the algorithm 
		AlgoPrefixSpan algo = new AlgoPrefixSpan(); 
		algo.setMaximumPatternLength(MaximumPatternLength);
		
        // if you set the following parameter to true, the sequence ids of the sequences where
        // each pattern appears will be shown in the result
        algo.setShowSequenceIdentifiers(false);
		
		// execute the algorithm with minsup = 50 %
        
		SequentialPatterns patterns = algo.runAlgorithm(patternSequenceDatabase, HighThreshold);   
		
		int counter=0;
		for(List<SequentialPattern> level : patterns.levels) {
			for(SequentialPattern pattern : level){
				counter++;
			}
		}
		ResultedPatterns= new String [counter];
		int [] PatternSupports= new int [counter];
		TraceIDs=new ArrayList<List<Integer>>(counter);
		counter=0;
		System.out.println(" == PATTERNS FOUND ***************==");
		for(List<SequentialPattern> level : patterns.levels) {
			for(SequentialPattern pattern : level){
				//System.out.println(level.toString()  + pattern + " support: " + pattern.getAbsoluteSupport());
				List<Integer> temp = pattern.getSequenceIDs();
				TraceIDs.add(temp);
				ResultedPatterns[counter]=pattern.toString() ;
				PatternSupports[counter]=pattern.getAbsoluteSupport();
				counter++;
			}
		}
		
		// print statistics
		//algo.printStatistics();
		//return  ResultedPatterns;
		//return ResultedPatterns ;
	}
	public static String[] getResultedPatterns() {
		return ResultedPatterns;
	}

	public static List<List<Integer>> getTraceIDs() {
		return TraceIDs;
	}
	
	
	
	
}
