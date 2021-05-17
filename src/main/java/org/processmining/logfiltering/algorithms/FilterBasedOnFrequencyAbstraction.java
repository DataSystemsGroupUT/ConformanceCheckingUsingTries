package org.processmining.logfiltering.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.processmining.logfiltering.parameters.FrequencyFilterParameter;

public class FilterBasedOnFrequencyAbstraction {

	public static XLog apply(XLog InputLog, FrequencyFilterParameter parameters) {

		/////////////////Initialization/////////////////////////////////////////////
		XEventClassifier EventCol = parameters.getEventAttribute();
		XLog OutputLog = (XLog) InputLog.clone();
		//LogProperties LogProp = new LogProperties(OutputLog);
		
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(InputLog, EventCol);
		//XLogInfo logInfo2 = XLogInfoFactory.createLogInfo
		int LogSize = 0;
		//Map<String, String> eventAttributeTypeMap = LogProp.getEventAttributeTypeMap();
		SortedSet<String> eventAttributeSet = new TreeSet<String>();
		XAttributeMap eventAttributeMap;
		//Map<String, Set<String>> eventAttributeValueSetMap = LogProp.getEventAttributeValueSetMap();
		//eventAttributeSet.addAll(eventAttributeTypeMap.keySet());
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
		FilterLevel FilteringMethod = parameters.getFilteringMethod();
		FilterSelection FilteringSelection =parameters.getFilteringSelection();
		

		//////////////////////////////// Computing Activities Frequency /////////////////////
		
		for (XTrace trace : OutputLog) {
			LogSize++;
			for (XEvent event : trace) {
				//eventAttributeMap = event.getAttributes();
				temp1 = event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString();
				//				temp1 = eventAttributeMap.get(EventCol).toString();
				ActivityCount[ActivityList.indexOf(temp1)]++;
			}
		}
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		///// Frequency of SET MultiSet and SEQUENCE
		
		Map<String, Integer> SetHashList = new HashMap<String, Integer>();
		Map<String, Integer> MultiSetHashList = new HashMap<String, Integer>();
		Map<String, Integer> SequenceHashList = new HashMap<String, Integer>();
		for (XTrace trace : OutputLog) {
			String[] TraceArray = new String[trace.size()];
			List<String> listarray = new ArrayList<String>();
			for (XEvent event : trace) {
				eventAttributeMap = event.getAttributes();
				listarray.add(event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString());
//				templist.add(eventAttributeMap.get(EventCol).toString());
			}
			TraceArray = listarray.toArray(new String[trace.size()]);
			if(TraceArray.length >= parameters.getAbsteractionLength()) {
				for (int i = 0; i < TraceArray.length-parameters.getAbsteractionLength(); i++) {
					int[] TempSet= new int[ActivitiesSize];
					int[] TempMultiSet= new int[ActivitiesSize];
					int[] TempSequence= new int [parameters.getAbsteractionLength()]; 
					for (int j = 0; j < parameters.getAbsteractionLength(); j++) {
						TempSet[ActivityList.indexOf(TraceArray[i+j])]=1;
						TempMultiSet[ActivityList.indexOf(TraceArray[i+j])]++;
						TempSequence[j]= ActivityList.indexOf(TraceArray[i+j])+1;
					}
					
					if (SetHashList.containsKey(Arrays.toString(TempSet))) {
						SetHashList.put(Arrays.toString(TempSet), SetHashList.get(Arrays.toString(TempSet))+1);
					}
					else {
						SetHashList.put(Arrays.toString(TempSet), 1);
					}
					if (MultiSetHashList.containsKey(Arrays.toString(TempMultiSet))) {
						MultiSetHashList.put(Arrays.toString(TempMultiSet), MultiSetHashList.get(Arrays.toString(TempMultiSet))+1);
					}
					else {
						MultiSetHashList.put(Arrays.toString(TempMultiSet), 1);
					}
					if (SequenceHashList.containsKey(Arrays.toString(TempSequence))) {
						SequenceHashList.put(Arrays.toString(TempSequence), SequenceHashList.get(Arrays.toString(TempSequence))+1);
					}
					else {
						SequenceHashList.put(Arrays.toString(TempSequence), 1);
					}
				}
			}
			else {
				int[] TempSet= new int[ActivitiesSize];
				int[] TempMultiSet= new int[ActivitiesSize];
				int[] TempSequence= new int [parameters.getAbsteractionLength()]; 
				for (int j = 0; j < TraceArray.length; j++) {
					TempSet[ActivityList.indexOf(TraceArray[j])]=1;
					TempMultiSet[ActivityList.indexOf(TraceArray[j])]++;
					TempSequence[j]= ActivityList.indexOf(TraceArray[j])+1;
				}
				if (SetHashList.containsKey(Arrays.toString(TempSet))) {
					SetHashList.put(Arrays.toString(TempSet), SetHashList.get(Arrays.toString(TempSet))+1);
				}
				else {
					SetHashList.put(Arrays.toString(TempSet), 1);
				}
				if (MultiSetHashList.containsKey(Arrays.toString(TempMultiSet))) {
					MultiSetHashList.put(Arrays.toString(TempMultiSet), MultiSetHashList.get(Arrays.toString(TempMultiSet))+1);
				}
				else {
					MultiSetHashList.put(Arrays.toString(TempMultiSet), 1);
				}
				if (SequenceHashList.containsKey(Arrays.toString(TempSequence))) {
					SequenceHashList.put(Arrays.toString(TempSequence), SequenceHashList.get(Arrays.toString(TempSequence))+1);
				}
				else {
					SequenceHashList.put(Arrays.toString(TempSequence), 1);
				}
			}
			
		}
		/////////////////////////////////////////////////////////////////////////////////////////////
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLog outputLog2 = factory.createLog();
		XLog RemovedLog= factory.createLog();
		for (XExtension extension : InputLog.getExtensions())
			{outputLog2.getExtensions().add(extension);
			 RemovedLog.getExtensions().add(extension);
			}
		outputLog2.setAttributes(InputLog.getAttributes());
		RemovedLog.setAttributes(InputLog.getAttributes());
		

		/////////////////////////////////////////////////////// Lets Trace Filtering /////////////////////////////////////////
		int counttt=0;
		switch (FilteringMethod) {
			case TRACE :
				for (XTrace trace : InputLog) {
					counttt++;
					int TraceFlag = 0;
					String[] TraceArray = new String[trace.size()];
					List<String> templist = new ArrayList<String>();
					for (XEvent event : trace) {
						eventAttributeMap = event.getAttributes();
						templist.add(eventAttributeMap.get(EventCol.getDefiningAttributeKeys()[0]).toString());
					}
					TraceArray = templist.toArray(new String[trace.size()]);
					if(TraceArray.length >= parameters.getAbsteractionLength()) {
						
						for (int i = 0; i < TraceArray.length-parameters.getAbsteractionLength(); i++) {
							int[] TempSet= new int[ActivitiesSize];
							int[] TempMultiSet= new int[ActivitiesSize];
							int[] TempSequence= new int [parameters.getAbsteractionLength()]; 
							for (int j = 0; j < parameters.getAbsteractionLength(); j++) {
								TempSet[ActivityList.indexOf(TraceArray[i+j])]=1;
								TempMultiSet[ActivityList.indexOf(TraceArray[i+j])]++;
								TempSequence[j]= ActivityList.indexOf(TraceArray[i+j])+1;
							}
							double test=0;
							switch(parameters.getAbstractionUsed()) {
								case SET :
									 test = 1.0/ SetHashList.get(Arrays.toString(TempSet));
									break;
								case MULTISET :
									test = 1.0/ MultiSetHashList.get(Arrays.toString(TempMultiSet));
									break;
									
								case SEQUENCE :
									test = 1.0/ SequenceHashList.get(Arrays.toString(TempSequence));
									break;
							}
							if (test  > 1.0/(parameters.getProbabilityOfRemoval() * LogSize ) ) {
								TraceFlag++;
							}
						}
					
					}
					else {
						int[] TempSet= new int[ActivitiesSize];
						int[] TempMultiSet= new int[ActivitiesSize];
						int[] TempSequence= new int [parameters.getAbsteractionLength()]; 
						
							for (int j = 0; j < TraceArray.length; j++) {
								TempSet[ActivityList.indexOf(TraceArray[j])]=1;
								TempMultiSet[ActivityList.indexOf(TraceArray[j])]++;
								TempSequence[j]= ActivityList.indexOf(TraceArray[j])+1;
							}
							double test=0;
							switch(parameters.getAbstractionUsed()) {
								case SET :
									 test = 1.0/ SetHashList.get(Arrays.toString(TempSet));
									break;
								case MULTISET :
									test = 1.0/ MultiSetHashList.get(Arrays.toString(TempMultiSet));
									break;
									
								case SEQUENCE :
									test = 1.0/ SequenceHashList.get(Arrays.toString(TempSequence));
									break;
							}
							if (test  > 1.0/(parameters.getProbabilityOfRemoval() * LogSize ) ) {
								TraceFlag++;
							}
						
					}
					if (TraceFlag < 1)
					{outputLog2.add(trace);}
				else 
					{RemovedLog.add(trace);}
				}
		
				break;
				/////////////////////////////////////////////////////// Lets Event Filtering /////////////////////////////////////////
			case EVENT :
				XTrace TempTrace;
				for (XTrace trace : InputLog) {
				}
					
				break;
		}

		///////////////////////////////////////////////////////////////////////////////////////////////
		switch (FilteringSelection) {
			case REMOVE :
				
				return outputLog2;
			case SELECT:
				
				return RemovedLog;
			
				
		}
		return outputLog2;
		
	}

}
