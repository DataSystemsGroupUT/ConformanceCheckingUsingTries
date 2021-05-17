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
import org.processmining.logfiltering.parameters.FilterLevel;
import org.processmining.logfiltering.parameters.FilterSelection;
import org.processmining.logfiltering.parameters.MatrixFilterParameter;

public class FilterBaseOnSimilarityImp {

	public static XLog apply(XLog InputLog, MatrixFilterParameter parameters) {
		 
		/////////////////Initialization/////////////////////////////////////////////
		XEventClassifier EventCol = parameters.getEventClassifier();
		XLog OutputLog = (XLog) InputLog.clone();
		
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(InputLog, EventCol);
		HashMap<String,String >ActivityCoder =new HashMap<String, String>();
		int LogSize = 0;

		SortedSet<String> eventAttributeSet = new TreeSet<String>();
		XAttributeMap eventAttributeMap;
		int KLength =parameters.getSubsequenceLength(); 
		int charcounter=65;
		Set<String> ActivitySet = new HashSet<String>();
		for (XEventClass clazz : logInfo.getNameClasses().getClasses()){
			ActivitySet.add(clazz.toString());
			ActivityCoder.put(clazz.toString(), Character.toString((char)charcounter));
			charcounter++;
		}
		//int ActivitiesSize = eventAttributeValueSetMap.get(EventCol.getDefiningAttributeKeys()[0]).size();
		int ActivitiesSize = ActivitySet.size();
		//Set<String> ActivitySet = eventAttributeValueSetMap.get(EventCol.getDefiningAttributeKeys()[0]);
		String[] Activities = ActivitySet.toArray(new String[ActivitiesSize]);
		List<String> ActivityList = java.util.Arrays.asList(Activities);
		int[] ActivityCount = new int[ActivitiesSize];
		
		FilterLevel FilteringMethod = parameters.getFilterLevel();
		FilterSelection FilteringSelection =parameters.getFilteringSelection();
		HashMap<String,Integer >HashMaper =new HashMap<String, Integer>();
		HashMap<String,String >TraceHash =new HashMap<String, String>();
		HashMap<String,Integer >FilterHashMaper =new HashMap<String, Integer>();
		HashMap<Integer, String> ReverseMapper =new HashMap<Integer, String>();
		HashMap<String, Integer> ActionMaper= new HashMap<String, Integer>();
	
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
					String TraceinChar="";
					for (int i =0; i < Trace.length; i++){
						tr= tr.concat(" "+Trace[i]);
						TraceinChar= TraceinChar.concat(ActivityCoder.get(Trace[i]));
					}
					if (ActionMaper.get(tr)==null ){
						ActionMaper.put(tr,1);
						ReverseMapper.put(chCount, tr);
						HashMaper.put(tr, chCount);
						TraceHash.put(tr, TraceinChar);
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
			 for (int i=0; i< chCount; i++){
				 if (sum <= LogSize*((double)(parameters.getSubsequenceLength()*1.0)/100)){
					 sum = sum+ ActionMaper.get(ReverseMapper.get(SortList[i][0]));
					 FilterHashMaper.put(ReverseMapper.get(i), i);
				 }
			 }
			
			 /////////////////////////////Find Similarity///////////////////////
			 HashMap<String, Double> VariantSimilarity = new HashMap<String, Double>();
			 for (int i = 0; i < chCount; i++) {
				 String String1= TraceHash.get(ReverseMapper.get(i));
				 double temp=0;
				for (int j = 0; j < chCount; j++) {
					String String2= TraceHash.get(ReverseMapper.get(j));
					  temp+= (levenshteinDistance(String1,String2) * ActionMaper.get(ReverseMapper.get(j)));
				}
				temp= temp/LogSize;
				VariantSimilarity.put(ReverseMapper.get(i), temp);
			 }
			 
			 
			 ///////////////////////////Filtering //////////
			 XLog outputLog2 = factory.createLog();
			 XLog outputLog3 = factory.createLog();
				for (XExtension extension : InputLog.getExtensions()){
					outputLog2.getExtensions().add(extension);
					outputLog3.getExtensions().add(extension);
					}
				outputLog2.setAttributes(InputLog.getAttributes());
				outputLog3.setAttributes(InputLog.getAttributes());
			
			
				
				
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
						tr= tr.concat(" "+Trace[i]);
					}
					if (VariantSimilarity.get(tr)< parameters.getProbabilityOfRemoval()){
						outputLog2.add(trace);
					}
					else {
						outputLog3.add(trace);
					}
			 }
			 
			switch (parameters.getFilteringSelection()) {
				case REMOVE :
					return outputLog2;
				case SELECT :
					return outputLog3;
			}
			return outputLog2;
	}

	public static double levenshteinDistance (CharSequence lhs, CharSequence rhs) {                          
	    int len0 = lhs.length() + 1;                                                     
	    int len1 = rhs.length() + 1;                                                     
	     int maxLen= 0;
	     if (len0>len1) {
	    	 maxLen=len0;
	     }
	     else {
	    	  	maxLen= len1;
	     }
	    // the array of distances                                                       
	    int[] cost = new int[len0];                                                     
	    int[] newcost = new int[len0];                                                  
	                                                                                    
	    // initial cost of skipping prefix in String s0                                 
	    for (int i = 0; i < len0; i++) cost[i] = i;                                     
	                                                                                    
	    // dynamically computing the array of distances                                  
	                                                                                    
	    // transformation cost for each letter in s1                                    
	    for (int j = 1; j < len1; j++) {                                                
	        // initial cost of skipping prefix in String s1                             
	        newcost[0] = j;                                                             
	                                                                                    
	        // transformation cost for each letter in s0                                
	        for(int i = 1; i < len0; i++) {                                             
	            // matching current letters in both strings                             
	            int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;             
	                                                                                    
	            // computing cost for each transformation                               
	            int cost_replace = cost[i - 1] + match;                                 
	            int cost_insert  = cost[i] + 1;                                         
	            int cost_delete  = newcost[i - 1] + 1;                                  
	                                                                                    
	            // keep minimum cost                                                    
	            newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
	        }                                                                           
	                                                                                    
	        // swap cost/newcost arrays                                                 
	        int[] swap = cost; cost = newcost; newcost = swap;                          
	    }                                                                               
	                                                                                    
	    // the distance is the cost for transforming all letters in both strings        
	    return (cost[len0 - 1]*1.0 )/ maxLen ;                                                          
	}
	
}
	

	
	
	

