package org.processmining.logfiltering.algorithms;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.logfiltering.algorithms.SPMF.PatternSequenceDatabase;
import org.processmining.logfiltering.algorithms.SPMF.RuleGrowth.SequenceDatabase;
import org.processmining.logfiltering.parameters.SequenceFilterParameter;





public class Xes2CSVAlgorithm {
	
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

		//FilterLevel FilteringMethod=parameters.getFilterLevel();
		int i=0;
		int i2=0;
		//////////////////////////////// Computing Activities Frequency /////////////////////

		
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
		
		
		//// the structure for saving time of start and complete
		
		Map<String, String> liveCycleMap = new HashMap<String, String>(); ////// new structure for saving time of starts 
		Map<String, Integer>  activityCaseHappenStart = new HashMap<String, Integer>();/// contain the activity or not 
		Map<String, Integer>  activityCaseHappenComplete = new HashMap<String, Integer>();/// contain the activity or not 
		String csvName=InputLog.getAttributes().get("concept:name").toString();
		PrintWriter writer = new PrintWriter(new File("D:\\PHD\\papers\\2021\\Predict\\Data for training BPIC_2012_50RWith_Training.csv"));
		StringBuilder sb = new StringBuilder();
	      sb.append("CASE_ID");
	      sb.append(',');
	      sb.append("Activity");
	      sb.append(',');
	      sb.append("Resource");
	      sb.append(',');
	      sb.append("StartTimestamp");
	      sb.append(',');
	      sb.append("Amount");
	      sb.append(',');
	      sb.append("CompleteTimestamp");
	      sb.append(',');
	      sb.append("Duration");
	      sb.append('\n');

	     


	      SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
	      TimeUnit timeUnit = TimeUnit.MINUTES;
	      long duration; 
		for(XTrace trace : OutputLog){
			XAttributeMap attributes = trace.getAttributes();
			LogSize++;
		      
			int activityCounter= 0;
			
			//ReverseSequenecDatabase=-2 + "\r\n"+ ReverseSequenecDatabase ;
			for (XEvent event : trace){
				String activity= event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString();
				String  activityCase= LogSize+ activity;
				if (event.getAttributes().get("lifecycle:transition").toString().equals("START")){
					
					 
					
					if(activityCaseHappenStart.get(activityCase)==null) {
						activityCaseHappenStart.put(activityCase, 0);
						liveCycleMap.put(activityCase+activityCaseHappenStart.get(activityCase), event.getAttributes().get("time:timestamp").toString());
					}else {
						activityCaseHappenStart.put(activityCase, activityCaseHappenStart.get(activityCase)+1);
						liveCycleMap.put(activityCase+activityCaseHappenStart.get(activityCase), event.getAttributes().get("time:timestamp").toString());

					}
				}
				
				if (event.getAttributes().get("lifecycle:transition").toString().equals("COMPLETE")
						&&   activity.substring(0,1).equals("W") ){ 
					if (activityCaseHappenComplete.get(LogSize+activity)==null) {
						activityCaseHappenComplete.put(LogSize+activity, 0);
						
					}else {
						activityCaseHappenComplete.put(LogSize+activity, activityCaseHappenComplete.get(LogSize+activity)+ 1);
					}
				
				  sb.append(LogSize);
			      sb.append(',');
			      sb.append(activity);
			      sb.append(','); 
			      String tempTime="";
			      if(event.getAttributes().containsKey("org:resource"))
			    	  sb.append(event.getAttributes().get("org:resource").toString());
			      else
			    	  sb.append("no resuource");
			      sb.append(',');
			    
			      if(liveCycleMap.get(activityCase+activityCaseHappenStart.get(activityCase))==null) { ///// we do not have any start time
			      if(event.getAttributes().containsKey("time:timestamp")) {
			    	  tempTime= event.getAttributes().get("time:timestamp").toString().substring(0,19).replace('T', ' ').replace('-', '.');
			    	  sb.append(tempTime);
			      }
			      else
			    	  sb.append("");
			      sb.append(',');
			      if(trace.getAttributes().containsKey("AMOUNT_REQ")) //////////////////Just for BPIC 2012
			    	  sb.append(trace.getAttributes().get("AMOUNT_REQ").toString());
			      else
			    	  sb.append("");
			      sb.append(',');
			      if(event.getAttributes().containsKey("time:timestamp")) {
			    	  tempTime= event.getAttributes().get("time:timestamp").toString().substring(0,19).replace('T', ' ').replace('-', '.');
			    	  sb.append(tempTime);
			      }
			      else
			    	  sb.append("");
			      
			      sb.append(',');
			      sb.append(0);
			      
			      }else { ////if we have start time 
			    	  Date dateStart = null;
			    	  Date dateComplete = null;
			    	  if(event.getAttributes().containsKey("time:timestamp")) {
			    		  tempTime= liveCycleMap.get(activityCase+activityCaseHappenStart.get(activityCase)).toString().substring(0,19).replace('T', ' ').replace('-', '.');
			    		  sb.append(tempTime);
			    		  try {
							dateStart=format.parse(tempTime);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				      }
				      else
				    	  sb.append("");
				      sb.append(',');
				      if(trace.getAttributes().containsKey("AMOUNT_REQ")) //////////////////Just for BPIC 2012
				    	  sb.append(trace.getAttributes().get("AMOUNT_REQ").toString());
				      else
				    	  sb.append("");
				      sb.append(',');
				      if(event.getAttributes().containsKey("time:timestamp")) {
				    	  tempTime= event.getAttributes().get("time:timestamp").toString().substring(0,19).replace('T', ' ').replace('-', '.');
				    	  sb.append(tempTime);
				    	  try {
							dateComplete=format.parse(tempTime);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				      }
				      else
				    	  sb.append("");
				      
				      sb.append(',');

				      long difference = dateComplete.getTime() - dateStart.getTime();
				      
				      if (difference>=0) {
				    	  duration=	timeUnit.convert(difference,TimeUnit.MILLISECONDS);
				      }else {
				    	  duration= timeUnit.convert(difference,TimeUnit.MILLISECONDS);
				      }
				      sb.append(duration);
			      }
			      
			     
			      sb.append('\n');
			      
			    	 
				 }
				
				}
			
 			
		}
		
			
		  writer.write(sb.toString());
		  writer.close();
	      System.out.println("done!");

	
	return OutputLog;
	}
	
	
}
