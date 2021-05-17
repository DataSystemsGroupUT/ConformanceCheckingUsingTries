package org.processmining.logfiltering.algorithms;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.logfiltering.algorithms.SPMF.PatternSequenceDatabase;
import org.processmining.logfiltering.algorithms.SPMF.RuleGrowth.Sequence;
import org.processmining.logfiltering.algorithms.SPMF.RuleGrowth.SequenceDatabase;
import org.processmining.logfiltering.parameters.SequenceFilterParameter;





public class YaguangAlgorithm {
	
	public static XLog apply(XLog InputLog, SequenceFilterParameter parameters) throws IOException{
		
		
		/////////////////Initialization/////////////////////////////////////////////
		XEventClassifier EventCol =parameters.getEventClassifier();
		int oddDistance= parameters.getOddDistance();
		XLog OutputLog = (XLog) InputLog.clone();
//		LogProperties LogProp = new LogProperties(OutputLog); 
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(InputLog, parameters.getEventClassifier());
		int LogSize= 0;
//		Map<String, String> eventAttributeTypeMap = LogProp. getEventAttributeTypeMap();
//		SortedSet<String> eventAttributeSet = new TreeSet<String>();
//		XAttributeMap eventAttributeMap;
//		Map<String, Set<String>> eventAttributeValueSetMap = LogProp.getEventAttributeValueSetMap();
//		eventAttributeSet.addAll(eventAttributeTypeMap.keySet());
//		int ActivitiesSize =eventAttributeValueSetMap.get(EventCol.getDefiningAttributeKeys()[0]).size();
//		Set<String> ActivitySet = eventAttributeValueSetMap.get(EventCol.getDefiningAttributeKeys()[0]);
//		String [] Activities = ActivitySet.toArray(new String[ActivitiesSize]);
//		List<String> ActivityList = java.util.Arrays.asList(Activities); 
		double HighSupportThreshold=parameters.getHighSupportPattern();
		double ConfHighConfRules = parameters.getConfHighConfRules();
		double SuppHighConfRules = parameters. getSuppHighConfRules();
		double SuppOrdinaryRules= parameters.getSuppOrdinaryRules();
		double ConfOridnaryRules= parameters.getConfOridnaryRules();
		//FilterLevel FilteringMethod=parameters.getFilterLevel();
		int i=0;
		int i2=0;
		//////////////////////////////// Computing Activities Frequency /////////////////////
		String SequeceDatabase = "";
		String SequeceDatabase2 = "";
		String ReverseSequenecDatabase= "";
		int counter =0;
		
		int index = 1;
		Set<String> EventNames = new HashSet<String>();
		for (XEventClass clazz : logInfo.getNameClasses().getClasses()){
			EventNames.add(clazz.toString());
		}
		
		Map<String, Integer> encodeMap = new HashMap<String, Integer>();
		Map<Integer, String> decodeMap = new HashMap<Integer, String>();
		
		for(String eventname : EventNames){
			encodeMap.put(eventname, index);
			decodeMap.put(index, eventname);
			index++;
		}

		SequenceDatabase RuleSequencesDatabase= new SequenceDatabase(); 
		PatternSequenceDatabase patternSequenceDatabase = new PatternSequenceDatabase();

//		for(XTrace trace : OutputLog){
//			LogSize++;
//			for (XEvent event : trace){
//				eventAttributeMap = event.getAttributes();
//				i= ActivityList.indexOf(eventAttributeMap.get(EventCol.getDefiningAttributeKeys()[0]).toString())+1;
//				SequeceDatabase+=i + " ";
//				SequeceDatabase2+=i + " -1 ";
//				}
//			SequeceDatabase+=-2+"\r\n";
//			SequeceDatabase2+=-2+"\r\n";
//		}
		List<int[]> PatternSequences = new ArrayList<int[]>();
		List<int[]> PatternSequences2 = new ArrayList<int[]>();
		List<int[]> ReveresePatternSequences = new ArrayList<int[]>();
		List<int[]> ReveresePatternSequences2 = new ArrayList<int[]>();
		PatternSequences = new ArrayList<int[]>();
		List<Integer> Ruleitemset = new ArrayList<Integer>();
		List<Integer> Ruleitemset2 = new ArrayList<Integer>();
		for(XTrace trace : OutputLog){
			LogSize++;
			Sequence sequence = new Sequence(RuleSequencesDatabase.size());
			int eventcounter=0;
			int[] patternSequence = new int [(trace.size()*2)+1] ; 
			//ReverseSequenecDatabase=-2 + "\r\n"+ ReverseSequenecDatabase ;
			for (XEvent event : trace){
				
				i= encodeMap.get(event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString());
				i2= encodeMap.get(event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString());
				//ReverseSequenecDatabase= i +" -1 "+ ReverseSequenecDatabase  ;
				SequeceDatabase+=i + " ";
				SequeceDatabase2+=i + " ";
				Ruleitemset= new ArrayList<Integer>();
				Ruleitemset.add(i);
				Ruleitemset2= new ArrayList<Integer>();
				Ruleitemset2.add(i2);
				sequence.addItemset(Ruleitemset);
				patternSequence[eventcounter] = i;
				eventcounter++;
				patternSequence[eventcounter] = -1;
				eventcounter++;
				}
			patternSequence[eventcounter] = -2;
			RuleSequencesDatabase.addSequence(sequence);
			int[] patternSequence2 = patternSequence.clone();
			int[] reversePatternSequence=new int [patternSequence2.length];
			for (int j = 0; j < reversePatternSequence.length-2; j++) {
				reversePatternSequence[j]=patternSequence2[patternSequence2.length-3-j];
			}
			reversePatternSequence[reversePatternSequence.length-1]=-2;
			reversePatternSequence[reversePatternSequence.length-2]=-1;
			int [] reversePatternSequence2=reversePatternSequence.clone();
			PatternSequences.add(patternSequence);
			PatternSequences2.add(patternSequence2);
			ReveresePatternSequences.add(reversePatternSequence);
			ReveresePatternSequences2.add(reversePatternSequence2);
			SequeceDatabase+="\r\n";
			SequeceDatabase2+="\r\n";
 			
		}
		
		patternSequenceDatabase.setSequences(PatternSequences);
		String [] Traces = SequeceDatabase.split("\r\n");
		for (int j = 0; j < Traces.length; j++) {
			String[] tempTrace= Traces[j].split(" ");
			for (int k = tempTrace.length-1; k >= 0; k--) {
				Ruleitemset= new ArrayList<Integer>();
				Ruleitemset.add(Integer.parseInt(tempTrace[k]));
				Ruleitemset2= new ArrayList<Integer>();
				Ruleitemset2.add(Integer.parseInt(tempTrace[k]));
			}

		}
	
		//////////////////////////////////////Odd Patterns/////////////////////////////////////
		int logElham= LogSize/100;
		SequenceMiner ordinarypatterns  = new SequenceMiner();	
		ordinarypatterns.setMaximumPatternLength(parameters.getOddDistance());
		ordinarypatterns.apply (patternSequenceDatabase, parameters.getHighSupportPattern());
		
		String[] OrdinaryPatternsItems=ordinarypatterns.getResultedPatterns();
		List<List<Integer>> OridnaryPatternsTraces = ordinarypatterns.getTraceIDs();
	
		 				///////////////Find Direct Relations//////////////
		int[][] DF= new int[encodeMap.size()][encodeMap.size()];
		XAttributeMap eventAttributeMap;
		for(XTrace trace : OutputLog){
			String[] Event1 = new String[trace.size()];
			List<String> templist = new ArrayList<String>();
			for (XEvent event : trace) {
				eventAttributeMap = event.getAttributes();
				templist.add(event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString());
			}
			
			Event1 = templist.toArray(new String[trace.size()]);
			for (int j = 0; j < Event1.length-1; j++) {
				DF[encodeMap.get(Event1[j])-1][encodeMap.get(Event1[j+1])-1]++;
			}
		}
		
		for (int j = 0; j < DF.length; j++) {
			for (int j2 = 0; j2 < DF.length; j2++) {
				if (DF[j][j2]> LogSize*parameters.getHighSupportPattern()) {
					System.out.println(decodeMap.get(j+1).toString() + "   ===>    "+ decodeMap.get(j2+1).toString() + "   With Support   "+DF[j][j2] );
				}
			}
		}
		System.out.println("===============");
		for (int f = 1; f < decodeMap.size(); f++) {
			System.out.println(decodeMap.get(f)+ "       =          "+f) ;
		}
	
	return OutputLog;
	}
	
	
}
