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
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.logfiltering.algorithms.SPMF.PatternSequenceDatabase;
import org.processmining.logfiltering.algorithms.SPMF.RuleGrowth.AlgoRULEGROWTH;
import org.processmining.logfiltering.algorithms.SPMF.RuleGrowth.Sequence;
import org.processmining.logfiltering.algorithms.SPMF.RuleGrowth.SequenceDatabase;
import org.processmining.logfiltering.parameters.SequenceFilterParameter;





public class FilterBasedOnSequence {
	
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
		SequenceDatabase ReverseRuleSequencesDatabase= new SequenceDatabase(); 
		SequenceDatabase ReverseRuleSequencesDatabase2= new SequenceDatabase(); 
		SequenceDatabase RuleSequencesDatabase= new SequenceDatabase();
		SequenceDatabase RuleSequencesDatabase2= new SequenceDatabase(); 
		PatternSequenceDatabase patternSequenceDatabase = new PatternSequenceDatabase();
		PatternSequenceDatabase patternSequenceDatabase2 = new PatternSequenceDatabase();
		PatternSequenceDatabase Reverse_patternSequenceDatabase = new PatternSequenceDatabase();
		PatternSequenceDatabase Reverse_patternSequenceDatabase2 = new PatternSequenceDatabase();
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
			Sequence sequence2 = new Sequence(RuleSequencesDatabase2.size());
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
				sequence2.addItemset(Ruleitemset2);
				patternSequence[eventcounter] = i;
				eventcounter++;
				patternSequence[eventcounter] = -1;
				eventcounter++;
				}
			patternSequence[eventcounter] = -2;
			RuleSequencesDatabase.addSequence(sequence);
			RuleSequencesDatabase2.addSequence(sequence2);
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
		patternSequenceDatabase2.setSequences(PatternSequences2);
		Reverse_patternSequenceDatabase.setSequences(ReveresePatternSequences);
		Reverse_patternSequenceDatabase2.setSequences(ReveresePatternSequences2);
		String [] Traces = SequeceDatabase.split("\r\n");
		for (int j = 0; j < Traces.length; j++) {
			Sequence sequence = new Sequence(ReverseRuleSequencesDatabase.size());
			Sequence sequence2 = new Sequence(ReverseRuleSequencesDatabase2.size());
			String[] tempTrace= Traces[j].split(" ");
			for (int k = tempTrace.length-1; k >= 0; k--) {
				Ruleitemset= new ArrayList<Integer>();
				Ruleitemset.add(Integer.parseInt(tempTrace[k]));
				Ruleitemset2= new ArrayList<Integer>();
				Ruleitemset2.add(Integer.parseInt(tempTrace[k]));
				sequence.addItemset(Ruleitemset);
				sequence2.addItemset(Ruleitemset2);
			}
			ReverseRuleSequencesDatabase.addSequence(sequence);
			ReverseRuleSequencesDatabase2.addSequence(sequence2);
		}
	
		//////////////////////////////////////Odd Patterns/////////////////////////////////////
		SequenceMiner Allpatterns  = new SequenceMiner();
		Allpatterns.setMaximumPatternLength(parameters.getOddDistance());
		Allpatterns.apply (patternSequenceDatabase, 0.0);
		String[] AllPatternsItems=Allpatterns.getResultedPatterns();
		List<List<Integer>> AllPatternsTraces = Allpatterns.getTraceIDs();
		
		
		SequenceMiner ordinarypatterns  = new SequenceMiner();	
		ordinarypatterns.setMaximumPatternLength(parameters.getOddDistance());
		ordinarypatterns.apply (patternSequenceDatabase2, parameters.getHighSupportPattern());
		String[] OrdinaryPatternsItems=ordinarypatterns.getResultedPatterns();
		List<List<Integer>> OridnaryPatternsTraces = ordinarypatterns.getTraceIDs();
	
		 				///////////////Find Odds from All and ordinaries//////////////
	
		List<List<Integer>> OddPatterns=new ArrayList<List<Integer>>(AllPatternsItems.length-OrdinaryPatternsItems.length); 
		for (int j = 0; j < AllPatternsItems.length; j++) {
			 counter =0;
			for (int j2 = 0; j2 < OrdinaryPatternsItems.length; j2++) {
				if (OrdinaryPatternsItems[j2].equals(AllPatternsItems[j])) {
					counter++;
				}
			}
			if(counter==0) {
				OddPatterns.add(AllPatternsTraces.get(j));
			}
		}
	 
		////////////////////////////Odd Patterns REVERSE////////////////////////////
		
		SequenceMiner RevereseAllpatterns  = new SequenceMiner();
		RevereseAllpatterns.setMaximumPatternLength(parameters.getOddDistance());
		RevereseAllpatterns.apply (Reverse_patternSequenceDatabase,  0.0);
		String[] ReverseAllPatternsItems = RevereseAllpatterns.getResultedPatterns();
		List<List<Integer>> RevereseAllPatternsTraces = RevereseAllpatterns.getTraceIDs();
		
		
		SequenceMiner Reverseordinarypatterns  = new SequenceMiner();
		Reverseordinarypatterns.setMaximumPatternLength(parameters.getOddDistance());
		Reverseordinarypatterns.apply (Reverse_patternSequenceDatabase2, parameters.getHighSupportPattern());
		String[] RevereseOrdinaryPatternsItems=Reverseordinarypatterns.getResultedPatterns();
		List<List<Integer>> REvereseOridnaryPatternsTraces = Reverseordinarypatterns.getTraceIDs();
	
		 				///////////////Find Odds from All and ordinaries//////////////
	
		List<List<Integer>> RevereseOddPatterns=new ArrayList<List<Integer>>(ReverseAllPatternsItems.length-RevereseOrdinaryPatternsItems.length); 
		for (int j = 0; j < ReverseAllPatternsItems.length; j++) {
			 counter =0;
			for (int j2 = 0; j2 < RevereseOrdinaryPatternsItems.length; j2++) {
				if (RevereseOrdinaryPatternsItems[j2].equals(ReverseAllPatternsItems[j])) {
					counter++;
				}
			}
			if(counter==0) {
				RevereseOddPatterns.add(RevereseAllPatternsTraces.get(j));
			}
		}
		Set<Integer> OddTraces = new HashSet<>();
		List<Integer> tempp=null;
		if (OddTraces.size()==0 &&  !RevereseOddPatterns.isEmpty()) {
			tempp= RevereseOddPatterns.get(0);
			OddTraces.addAll(tempp);
		}
		for (int j = 0; j < RevereseOddPatterns.size(); j++) {
			tempp= RevereseOddPatterns.get(j);
			OddTraces.addAll(tempp);
		}
		if (OddTraces.size()==0 &&  !OddPatterns.isEmpty()) {
			tempp= OddPatterns.get(0);
			OddTraces.addAll(tempp);
		}
		for (int j = 0; j < OddPatterns.size(); j++) {
			tempp= OddPatterns.get(j);
			OddTraces.addAll(tempp);
		}
		
		
	    /////////////////////////////////////////////////Rules//////////////////////////////////////
		//double minsup = 0.15;
		//double minconf = 0.9;
		//int windowSize = 15;
		
		//AlgoTRuleGrowth algo = new AlgoTRuleGrowth();
		//algo.runAlgorithm(minsup, minconf, TempSeqAddress, OutputRules, windowSize);
		//String [] inputRules =algo.inputRules();
		//String [] outputRules= algo.outputRules();

		////////////////////////////////////////////////Rule Miner Using RuleGrowth  for High Confidence Rules//////////////////////////////
	 
		int RuleGrowthMinSupp_Relative = (int)(SuppHighConfRules * RuleSequencesDatabase.size());
		double RuleGrowthMinConf = ConfHighConfRules;
		AlgoRULEGROWTH RuleGrowthalgo = new AlgoRULEGROWTH();
		if(parameters.getOddDistance()>2) {
			RuleGrowthalgo.setMaxAntecedentSize(parameters.getOddDistance()-1);
		}
		RuleGrowthalgo.runAlgorithm(RuleSequencesDatabase, RuleGrowthMinSupp_Relative, RuleGrowthMinConf);
		String [] inputRules =RuleGrowthalgo.inputRules();
		String [] outputRules= RuleGrowthalgo.outputRules();
		List<Set<Integer>> highFittedTraces = RuleGrowthalgo.FittedTraces();
		List<Set<Integer>> AntecedentsTraces = RuleGrowthalgo.AntecedenceTraces();
		
		for (int j = 0; j < AntecedentsTraces.size(); j++) {
			//Integer[] Ant = AntecedentsTraces.get(j).toArray(new Integer[AntecedentsTraces.get(j).size()]);
			//Integer[] rule = highFittedTraces.get(j).toArray(new Integer[highFittedTraces.get(j).size()]);
			Set<Integer> Ant = AntecedentsTraces.get(j);
			Set<Integer> rule = highFittedTraces.get(j);
			for(Integer a : Ant) {
				if(!rule.contains(a)) {
					OddTraces .add(a);
				}
			}
			
		}
	/*	Set<Integer> fittedTraces=null;
		// fittedTraces = Arrays.asList(highFittedTraces.get(0).toArray(new Integer[highFittedTraces.get(0).size()]));
		if (!highFittedTraces.isEmpty()) {
			fittedTraces = highFittedTraces.get(0);
		}
		for (int j = 0; j < highFittedTraces.size(); j++) {
			Set<Integer> s2 = highFittedTraces.get(j);
			fittedTraces.retainAll(s2);
		}
	*/	
		//////////////////////////ReverseHighSupport Rules//////////////////
		AlgoRULEGROWTH RuleGrowthalgoReverse = new AlgoRULEGROWTH();
		if(parameters.getOddDistance()>2) {
			RuleGrowthalgoReverse.setMaxAntecedentSize(parameters.getOddDistance()-1);
		}
		RuleGrowthalgoReverse.runAlgorithm(ReverseRuleSequencesDatabase, RuleGrowthMinSupp_Relative, RuleGrowthMinConf);
		String [] HighReverseinputRules =RuleGrowthalgoReverse.inputRules();
		String [] HighReverseoutputRules= RuleGrowthalgoReverse.outputRules();
		List<Set<Integer>> ReveresehighFittedTraces = RuleGrowthalgoReverse.FittedTraces();
		List<Set<Integer>> ReverseAntecedentsTraces = RuleGrowthalgoReverse.AntecedenceTraces();
		
		for (int j = 0; j < ReverseAntecedentsTraces.size(); j++) {
			//Integer[] Ant = AntecedentsTraces.get(j).toArray(new Integer[AntecedentsTraces.get(j).size()]);
			//Integer[] rule = highFittedTraces.get(j).toArray(new Integer[highFittedTraces.get(j).size()]);
			Set<Integer> Ant = ReverseAntecedentsTraces.get(j);
			Set<Integer> rule = ReveresehighFittedTraces.get(j);
			for(Integer a : Ant) {
				if(!rule.contains(a)) {
					OddTraces .add(a);
				}
			}
			
		}
		
	/*	List<Set<Integer>> ReverseHighFittedTraces = RuleGrowthalgoReverse.FittedTraces();	
		if (fittedTraces==null &&!ReverseHighFittedTraces.isEmpty()) {
			fittedTraces = ReverseHighFittedTraces.get(0);
		} 
		for (int j = 0; j < ReverseHighFittedTraces.size(); j++) {
			Set<Integer> s2 = ReverseHighFittedTraces.get(j);
			fittedTraces.retainAll(s2);
		}
		
////////////////////////////////////////////////Rule Miner Using RuleGrowth  for Ordinary Rules//////////////////////////////
		
		
		//////////////All direct rules/////////////////
		AlgoRULEGROWTH AllRuleGrowthalgo = new AlgoRULEGROWTH();
		if(parameters.getOddDistance()>2) {
			AllRuleGrowthalgo.setMaxAntecedentSize(parameters.getOddDistance()-1);
		}
		AllRuleGrowthalgo.runAlgorithm(RuleSequencesDatabase2, 1, 0);
		String [] AllRuleInputRules =AllRuleGrowthalgo.inputRules();
		String [] AllRuleOutputRules= AllRuleGrowthalgo.outputRules();
		List<Set<Integer>> AllRuleFittedTraces = AllRuleGrowthalgo.FittedTraces();
		
		////////////////////All reverse rules///////////////////
		AlgoRULEGROWTH ReverseAllRuleGrowthalgo = new AlgoRULEGROWTH();
		if(parameters.getOddDistance()>2) {
			ReverseAllRuleGrowthalgo.setMaxAntecedentSize(parameters.getOddDistance()-1);
		}
		ReverseAllRuleGrowthalgo.runAlgorithm(ReverseRuleSequencesDatabase2, 1, 0);
		String [] ReverseAllRuleInputRules =ReverseAllRuleGrowthalgo.inputRules();
		String [] ReverseAllRuleOutputRules= ReverseAllRuleGrowthalgo.outputRules();
		List<Set<Integer>> ReverseAllRuleFittedTraces = ReverseAllRuleGrowthalgo.FittedTraces();
		
		//////////////////////Ordinary direct rules/////////////////
		int OrdinaryRulesMinSupp_Relative = (int)(SuppOrdinaryRules * RuleSequencesDatabase.size());
		double OrdinaryRuleMinConf = ConfOridnaryRules;
		AlgoRULEGROWTH OrdinaryRuleGrowthalgo = new AlgoRULEGROWTH();
		if(parameters.getOddDistance()>2) {
			OrdinaryRuleGrowthalgo.setMaxAntecedentSize(parameters.getOddDistance()-1);
		}
		OrdinaryRuleGrowthalgo.runAlgorithm(RuleSequencesDatabase2, OrdinaryRulesMinSupp_Relative, OrdinaryRuleMinConf);
		String [] OrdinaryRuleInputRules =OrdinaryRuleGrowthalgo.inputRules();
		String [] OrdinaryRuleOutputRules= OrdinaryRuleGrowthalgo.outputRules();
		List<Set<Integer>> OrdinaryRuleFittedTraces = OrdinaryRuleGrowthalgo.FittedTraces();	
		
		
		//////////////////////Ordinary Reverse Rules////////////
		AlgoRULEGROWTH ReverseOrdinaryRuleGrowthalgo = new AlgoRULEGROWTH();
		if(parameters.getOddDistance()>2) {
			ReverseOrdinaryRuleGrowthalgo.setMaxAntecedentSize(parameters.getOddDistance()-1);
		}
		ReverseOrdinaryRuleGrowthalgo.runAlgorithm(ReverseRuleSequencesDatabase2, OrdinaryRulesMinSupp_Relative, OrdinaryRuleMinConf);
		String [] ReverseOrdinaryRuleInputRules =ReverseOrdinaryRuleGrowthalgo.inputRules();
		String [] ReverseOrdinaryRuleOutputRules= ReverseOrdinaryRuleGrowthalgo.outputRules();
		List<Set<Integer>> ReverseOrdinaryRuleFittedTraces = ReverseOrdinaryRuleGrowthalgo.FittedTraces();
		
		
		String [] OddRuleInputRules= new String [AllRuleInputRules.length - OrdinaryRuleInputRules.length ];
		String [] OddRuleOutputRules= new String [AllRuleInputRules.length - OrdinaryRuleInputRules.length ];
		
		String [] ReverseOddRuleInputRules= new String [ReverseAllRuleInputRules.length - ReverseOrdinaryRuleInputRules.length ];
		String [] ReverseOddRuleOutputRules= new String [ReverseAllRuleInputRules.length - ReverseOrdinaryRuleInputRules.length ];
		
		Set <Integer> OddRuleFittedTraces = new HashSet<Integer> ();
	
		
		////////////////////Finding and Adding direct Odd Rules//////////////////////////////
		int TempChecker=0;
		int OddRuleCounter=0;
		for (int j = 0; j < AllRuleInputRules.length; j++) {
				TempChecker=0;
				 Loop8: for (int j2 = 0; j2 < OrdinaryRuleInputRules.length; j2++) {
						if (OrdinaryRuleInputRules[j2] .equals(AllRuleInputRules[j]) &&  AllRuleOutputRules[j].equals(OrdinaryRuleOutputRules[j2]) ){
							TempChecker++;
							break Loop8;
					}
			}
				if(TempChecker==0){
					OddRuleInputRules [OddRuleCounter]= AllRuleInputRules[j];
					OddRuleOutputRules [OddRuleCounter]= AllRuleOutputRules[j];
					OddRuleCounter++;
					Set<Integer> temp= AllRuleFittedTraces.get(j);
					for (int j2 = 0; j2 < AllRuleFittedTraces.get(j).size(); j2++) {
						OddRuleFittedTraces.addAll(AllRuleFittedTraces.get(j));
					}
					
				}
		}

		
		
		/////////////////////Finding Adding reverse Odd rules///////////////
		OddRuleCounter=0;
		for (int j = 0; j < ReverseAllRuleInputRules.length; j++) {
			TempChecker=0;
			 Loop8: for (int j2 = 0; j2 < ReverseOrdinaryRuleInputRules.length; j2++) {
					if (ReverseOrdinaryRuleInputRules[j2] .equals(ReverseAllRuleInputRules[j]) &&  ReverseAllRuleOutputRules[j].equals(ReverseOrdinaryRuleOutputRules[j2]) ){
						TempChecker++;
						break Loop8;
				}
		}
			if(TempChecker==0){
				ReverseOddRuleInputRules [OddRuleCounter]= ReverseAllRuleInputRules[j];
				ReverseOddRuleOutputRules [OddRuleCounter]= ReverseAllRuleOutputRules[j];
				OddRuleCounter++;
				Set<Integer> temp= ReverseAllRuleFittedTraces.get(j);
				for (int j2 = 0; j2 < ReverseAllRuleFittedTraces.get(j).size(); j2++) {
					OddRuleFittedTraces.addAll(ReverseAllRuleFittedTraces.get(j));
				}
				
			}
	}
		
*/		
		
/////////////////////////////////////////////Start Phase 1 Filtering//////////////////////////////////////////

		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLog outputLog = factory.createLog();
		for (XExtension extension : InputLog.getExtensions()) {
			outputLog.getExtensions().add(extension);
		}
		
		outputLog.setAttributes((XAttributeMap) InputLog.getAttributes().clone());
		//////////////////////////////////Pattern with high Support  And  Rules with high confidence////////////////////////////////////	 
		counter=0;
		for (XTrace trace : OutputLog){
		int TraceFlag =0;
		if(OddTraces.contains(counter)) {
			TraceFlag++;
		}
		
		
		
		/*String Temp= Traces[counter].replace("-2","ZZ");
		String[] Trace = Temp.split(" ");
		int NumberOfOccuredPatterns=0; 
		int TempOccured;
		///////////////////////////////////////////////////
		loop1: for (int j = 0; j < SequentialRules2.length; j++) {
			String [] pattern = SequentialRules2[j].split("   ");
			
			
			int indexer= 0;
					for ( int k =1; k<pattern.length; k++){
						TempOccured=0;
						
						loop9: for (int k2 = indexer; k2 < Trace.length; k2++) {
							if (Trace[k2].equals(pattern[k].replaceFirst("^ ", ""))){
								TempOccured++;
								indexer = k2;
								break loop9;
							}
						}
						
						if (TempOccured == 0)
						{
							TraceFlag++;
							break loop1;
						}
					}
			NumberOfOccuredPatterns ++;
		}
		///////////////////////////////////////////Rule With High Confidence//////////////////////////////////////
		if (TraceFlag ==0){
	loop3: for (int j = 0; j < inputRules.length; j++) {
				String[]  RuleInputEllements =inputRules[j].split(" ");
				int indexer= 0;
				int rulehappend=1;
				loop2:	for (int k = 0; k < RuleInputEllements.length; k++) {
					TempOccured=0;
						loop10:	for (int k2 = indexer; k2 <Trace.length; k2++) {
								if (Trace[k2].equals( RuleInputEllements[k].replaceFirst("^ ", ""))){
									TempOccured++;
									indexer = k2;
									break loop10;
								}
								
							}
						
						if (TempOccured ==0){
							rulehappend=0;
							break loop2;
						}
					}
				if (rulehappend==1){
					String[]  RuleOutputEllements =outputRules[j].split(" ");
					indexer= 0;
					for (int k = 0; k < RuleOutputEllements.length; k++) {
						TempOccured=0;
						loop11: for( int k2 = indexer; k2 <Trace.length; k2++) {
							if (Trace[k2].equals( RuleOutputEllements[k].replaceFirst("^ ", ""))){
								TempOccured++;
								indexer = k2;
								break loop11;
							}
						}
						
						
						if (TempOccured ==0){
							TraceFlag++;
							break loop3;
						}
					}
				}
			}
		}
		
		
		/////////////////////////////Odd Rules //////////////////////////
		if (TraceFlag ==0){
			
loop4:		for (int j = 0; j < Trace.length-1; j++) {
				for (int k = j+1; k <= j+oddDistance; k++) {
					if (Trace[0].equals(Trace[1])) {
						TraceFlag=0;
					}
					if (k<Trace.length) {
						int OddRuleFlage=0; 
						loop5:	for (int l = 0; l < OddRuleInputRules.length; l++) {
								if (Trace[j].equals( OddRuleInputRules[l])&& Trace[k].equals(OddRuleOutputRules[l])) {
									OddRuleFlage++;
									break loop5;
								}
							}
							if (OddRuleFlage!=0  ) {
								TraceFlag++;
								break loop4;

							}
					}
				
				}
				
			}

		}


*/		
		////////////////////////////////////////////////////////
		//	System.out.println(" ***************=="+ counter + "***************"+ TraceFlag);
		counter++;  
		if (TraceFlag < 1)
			outputLog.add(trace);
	}
	
	
	return outputLog;
	}
	
	
}
