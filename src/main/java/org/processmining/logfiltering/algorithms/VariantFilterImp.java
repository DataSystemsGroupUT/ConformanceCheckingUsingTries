package org.processmining.logfiltering.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
import org.processmining.logfiltering.parameters.AdjustingType;
import org.processmining.logfiltering.parameters.FilterLevel;
import org.processmining.logfiltering.parameters.FilterSelection;
import org.processmining.logfiltering.parameters.MatrixFilterParameter;
import org.processmining.logfiltering.parameters.ProbabilityType;

public class VariantFilterImp {

	public static XLog apply(XLog InputLog, MatrixFilterParameter parameters) {
		 
		/////////////////Initialization/////////////////////////////////////////////
		XEventClassifier EventCol = parameters.getEventClassifier();
		XLog OutputLog = (XLog) InputLog.clone();
		
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(InputLog, EventCol);
		
		int LogSize = 0;

		SortedSet<String> eventAttributeSet = new TreeSet<String>();
		XAttributeMap eventAttributeMap;
		int KLength =parameters.getSubsequenceLength(); 
		
		Set<String> ActivitySet = new HashSet<String>();
		for (XEventClass clazz : logInfo.getNameClasses().getClasses()){
			ActivitySet.add(clazz.toString());
		}
		//int ActivitiesSize = eventAttributeValueSetMap.get(EventCol.getDefiningAttributeKeys()[0]).size();
		int ActivitiesSize = ActivitySet.size();
		//Set<String> ActivitySet = eventAttributeValueSetMap.get(EventCol.getDefiningAttributeKeys()[0]);
		String[] Activities = ActivitySet.toArray(new String[ActivitiesSize]);
		List<String> ActivityList = java.util.Arrays.asList(Activities);
		int[] ActivityCount = new int[ActivitiesSize];
		String temp1 = new String();
		String temp2 = new String();
		String temp3 = new String();
		FilterLevel FilteringMethod = parameters.getFilterLevel();
		FilterSelection FilteringSelection =parameters.getFilteringSelection();
		HashMap<String,Integer >HashMaper =new HashMap<String, Integer>();
		HashMap<String,Integer >FilterHashMaper =new HashMap<String, Integer>();
		HashMap<Integer, String> ReverseMapper =new HashMap<Integer, String>();
		HashMap<String, Integer> ActionMaper= new HashMap<String, Integer>();
		ArrayList<int[]> action = new ArrayList<int[]>();
		ArrayList<int[]> WinAction = new ArrayList<int []>();
		AdjustingType adjustingType = parameters.getAdjustingThresholdMethod();
		ProbabilityType probabilityType= parameters.getProbabilitycomutingMethod();
		int SimilarityWindow=3;
		//////////////////////////////// Computing Activities Frequency /////////////////////


		int chCount=0;
		
			 for (XTrace trace : InputLog) { // for each trace
				 LogSize++;
				 /// Put trace to array
				 String[] Trace = new String[trace.size()];
					List<String> templist = new ArrayList<String>();
					for (XEvent event : trace) { 
						eventAttributeMap = event.getAttributes();
						templist.add(event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString());
					}
					Trace = templist.toArray(new String[trace.size()]);
					String tr= "";
					for (int i =0; i < Trace.length; i++){
						tr= tr.concat(Trace[i]);
					}
					if (ActionMaper.get(tr)==null ){
						ActionMaper.put(tr,1);
						ReverseMapper.put(chCount, tr);
						HashMaper.put(tr, chCount);
						chCount++;
					}else{
						ActionMaper.put(tr, ActionMaper.get(tr)+1);
					}
					
			 }
			 HashMap<String, Integer> tempMap = (HashMap<String, Integer>) ActionMaper.clone();
			 int [] [] SortList= new int [chCount][2];
			 
			 for (int j = 0; j< chCount ; j++){
				 int Max =-1;
				 int index=-1;
				 for (int i =0; i< chCount ; i++ ){
				 	if ( tempMap.get(ReverseMapper.get(i))!= null &&  tempMap.get(ReverseMapper.get(i)) >= Max){
					 	Max=ActionMaper.get(ReverseMapper.get(i));
					 	index = i;
				 	}
			 	}
				 SortList[j][0]= index;
				 SortList[j][1]= tempMap.get(ReverseMapper.get(index));
				 tempMap.remove(ReverseMapper.get(index));
			 }
			 int sum=0;
			 int checker=0;
			 for (int i=0; i< chCount; i++){
				
				 if (sum + ActionMaper.get(ReverseMapper.get(SortList[i][0])) <= LogSize*((double)(parameters.getSubsequenceLength()*1.0)/100) && checker ==0 ){
					 sum = sum+ ActionMaper.get(ReverseMapper.get(SortList[i][0]));
					 FilterHashMaper.put(ReverseMapper.get(SortList[i][0]), i);
				 }else {
					 checker++;
				 }
			 }

			 ///////////////////////////Filtering //////////
			 
			 XLog outputLog2 = factory.createLog();
				
				for (XExtension extension : InputLog.getExtensions()){
					outputLog2.getExtensions().add(extension);
					}
				outputLog2.setAttributes(InputLog.getAttributes());
				
				
				
			 for (XTrace trace : InputLog) { // for each trace
				 /// Put trace to array
				 String[] Trace = new String[trace.size()];
					List<String> templist = new ArrayList<String>();
					for (XEvent event : trace) { 
						eventAttributeMap = event.getAttributes();
						templist.add(event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString());
					}
					Trace = templist.toArray(new String[trace.size()]);
					String tr= "";
					for (int i =0; i < Trace.length; i++){
						tr= tr.concat(Trace[i]);
					}
					if (FilterHashMaper.get(tr)!=null ){
						outputLog2.add(trace);
					}
			 }
			 
			return outputLog2;
	}

	
	
}
	

	
	
	

