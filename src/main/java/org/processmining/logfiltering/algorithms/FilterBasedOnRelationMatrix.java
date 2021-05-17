package org.processmining.logfiltering.algorithms;

import java.util.ArrayList;
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

public class FilterBasedOnRelationMatrix {

	public static XLog apply(XLog InputLog, MatrixFilterParameter parameters) {

		/////////////////Initialization/////////////////////////////////////////////
		XEventClassifier EventCol = parameters.getEventClassifier();
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
		FilterLevel FilteringMethod = parameters.getFilterLevel();
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
		//////////////////////////////////////////////Window Size K =1 //////////////////////////////

		///////////////////////////////Computing the Direct Dependency Matrix //////////////// 
		int[] InitialPointsFrequency = new int[ActivitiesSize];
		int[] FinalPointsFrequency = new int[ActivitiesSize];
		float[] InitialPointsProbability = new float[ActivitiesSize];
		float[] FinalPointsProbability = new float[ActivitiesSize];
		int[][] DirectFrequency = new int[ActivitiesSize][ActivitiesSize];
		float[][] DirectProbability = new float[ActivitiesSize][ActivitiesSize];

		for (XTrace trace : OutputLog) {
			String[] Event1 = new String[trace.size()];
			List<String> templist = new ArrayList<String>();
			for (XEvent event : trace) {
				eventAttributeMap = event.getAttributes();
				templist.add(event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString());
//				templist.add(eventAttributeMap.get(EventCol).toString());
			}
			Event1 = templist.toArray(new String[trace.size()]);
			for (int i = 0; i < Event1.length - 1; i++) {
				temp1 = Event1[i];
				temp2 = Event1[i + 1];
				DirectFrequency[ActivityList.indexOf(temp1)][ActivityList.indexOf(temp2)]++;
			}
			InitialPointsFrequency[ActivityList.indexOf(Event1[0])]++;
			FinalPointsFrequency[ActivityList.indexOf(Event1[Event1.length-1])]++;
		}

		//////////////////////////////////////////////Computing the Direct Probability Matrix////////////////////
		for (int i = 0; i < DirectFrequency.length; i++) {
			for (int j = 0; j < DirectFrequency.length; j++) {
				DirectProbability[i][j] = (float) (DirectFrequency[i][j] * 1.0) / ActivityCount[i];
			}
			InitialPointsProbability[i] = (float) InitialPointsFrequency[i] / LogSize;
			FinalPointsProbability[i] = (float) FinalPointsFrequency[i] / LogSize;
		}

		////////////////////////////////////////////////////////Window Size K= 2 ///////////////////////////////////
		int[] InitialInderectFrequency2 = new int[ActivitiesSize * ActivitiesSize];
		float[] InitialIndirectProbability2 = new float[ActivitiesSize * ActivitiesSize];
		int[] FinalInderectFrequency2 = new int[ActivitiesSize * ActivitiesSize];
		float[] FinalIndirectProbability2 = new float[ActivitiesSize * ActivitiesSize];
		int[][] InderectFrequency2 = new int[ActivitiesSize * ActivitiesSize][ActivitiesSize];
		float[][] IndirectProbability2 = new float[ActivitiesSize * ActivitiesSize][ActivitiesSize];
		
		for (XTrace trace : OutputLog) {
			String[] Event1 = new String[trace.size()];
			List<String> templist = new ArrayList<String>();
			for (XEvent event : trace) {
				eventAttributeMap = event.getAttributes();
				templist.add(event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString());
//				templist.add(eventAttributeMap.get(EventCol).toString());
			}
			Event1 = templist.toArray(new String[trace.size()]);
			
			for (int i = 0; i < Event1.length - 2; i++) {
				temp1 = Event1[i];
				temp2 = Event1[i + 1];
				temp3 = Event1[i + 2];
				InderectFrequency2[(ActivityList.indexOf(temp1) * (ActivityCount.length))
						+ ActivityList.indexOf(temp2)][ActivityList.indexOf(temp3)]++;
			}
						if (Event1.length >1)
			InitialInderectFrequency2[(ActivityList.indexOf(Event1[0]) * (ActivityCount.length))
					+ ActivityList.indexOf(Event1[1])]++;
						
			FinalInderectFrequency2[(ActivityList.indexOf(Event1[Event1.length-2]) * (ActivityCount.length))
						      					+ ActivityList.indexOf(Event1[Event1.length-1])]++;
		}
		///////////////////////////////////////// Probability for size 2 ////////////////////
		for (int i = 0; i < InderectFrequency2.length; i++) {
			for (int j = 0; j < ActivityCount.length; j++) {
				if ((DirectFrequency[i / ActivityCount.length][i % ActivityCount.length]) != 0) {
					IndirectProbability2[i][j] = (float) (InderectFrequency2[i][j] * 1.0)
							/ (DirectFrequency[i / ActivityCount.length][i % ActivityCount.length]);
				}

			}
			InitialIndirectProbability2[i] = (float) (InitialInderectFrequency2[i] * 1.0)
					/ (InitialPointsFrequency[i / ActivityCount.length]);
			FinalIndirectProbability2[i] = (float) (FinalInderectFrequency2[i] * 1.0)
					/ (InitialPointsFrequency[i / ActivityCount.length]);

		}

		//////////////////////////////////////////////////////// Now We Start Filtering!!!!////////////////////////////////////////
		///////////////////Initialization////////////////////////////////////////////////////////////////////
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLog outputLog2 = factory.createLog();
		XLog RemovedLog= factory.createLog();
		for (XExtension extension : InputLog.getExtensions())
			{outputLog2.getExtensions().add(extension);
			 RemovedLog.getExtensions().add(extension);
			}
		outputLog2.setAttributes(InputLog.getAttributes());
		RemovedLog.setAttributes(InputLog.getAttributes());
		
		////////////////////////////////Version 2 Faster and improve the rare activities////////////////////////////////////////////
		int[] NoiseInitialPoints = new int[ActivitiesSize];
		int[] NoiseFinalPoints = new int[ActivitiesSize];
		int[][] NoiseDirectDependencies = new int[ActivitiesSize][ActivitiesSize];
		int[] NoiseInitialEdges = new int[ActivitiesSize * ActivitiesSize];
		int[] NoiseFinalEdges = new int[ActivitiesSize * ActivitiesSize];
		int[][] NoiseIndirectDependecies = new int[ActivitiesSize * ActivitiesSize][ActivitiesSize];
		////////////////Initializing Noise Matrixes ///////////////////////////////
		for (int i = 0; i < ActivitiesSize; i++) {
			if (InitialPointsProbability[i] < parameters.getProbabilityOfRemoval()) {
				if ((float) InitialPointsFrequency[i] / ActivityCount[i] < parameters.getProbabilityOfRemoval())
					NoiseInitialPoints[i] = 1;
			} else
				NoiseInitialPoints[i] = 0;
			
			if (FinalPointsProbability[i] < parameters.getProbabilityOfRemoval()) {
				if ((float) FinalPointsFrequency[i] / ActivityCount[i] < parameters.getProbabilityOfRemoval())
					NoiseFinalPoints[i] = 1;
			} else
				NoiseFinalPoints[i] = 0;
		}
		for (int i = 0; i < ActivitiesSize * ActivitiesSize; i++) {
			if (InitialIndirectProbability2[i] < parameters.getProbabilityOfRemoval()) {
				if ((float) InitialInderectFrequency2[i] / ActivityCount[i / ActivitiesSize] < parameters
						.getProbabilityOfRemoval())
					NoiseInitialEdges[i] = 1;
			} else
				NoiseInitialEdges[i] = 0;
			
			if (FinalIndirectProbability2[i] < parameters.getProbabilityOfRemoval()) {
				if ((float) FinalInderectFrequency2[i] / ActivityCount[i / ActivitiesSize] < parameters
						.getProbabilityOfRemoval())
					NoiseFinalEdges[i] = 1;
			} else
				NoiseFinalEdges[i] = 0;
		}
		
		
		for (int i = 0; i < ActivitiesSize; i++) {
			for (int j = 0; j < ActivitiesSize; j++) {
				if (DirectProbability[i][j] < parameters.getProbabilityOfRemoval()) {
					if ((float) DirectFrequency[i][j] / ActivityCount[j] < parameters.getProbabilityOfRemoval())
						NoiseDirectDependencies[i][j] = 1;
				} else
					NoiseDirectDependencies[i][j] = 0;
			}
		}

		for (int i = 0; i < ActivitiesSize * ActivitiesSize; i++) {
			for (int j = 0; j < ActivitiesSize; j++) {
				if (IndirectProbability2[i][j] < parameters.getProbabilityOfRemoval()) {
					if ((float) InderectFrequency2[i][j] / ActivityCount[j] < parameters.getProbabilityOfRemoval())
						NoiseIndirectDependecies[i][j] = 1;

				} else
					NoiseIndirectDependecies[i][j] = 0;
			}
		}

		/////////////////////////////////////////V2 //////////////////////////////////////////////////////////////////////////////////

		/////////////////////////////////////////////////////// Lets Trace Filtering /////////////////////////////////////////
		int counttt=0;
		switch (FilteringMethod) {
			case TRACE :
				for (XTrace trace : InputLog) {
					counttt++;
					int TraceFlag = 0;
					String[] Event1 = new String[trace.size()];
					List<String> templist = new ArrayList<String>();
					for (XEvent event : trace) {
						eventAttributeMap = event.getAttributes();
						templist.add(eventAttributeMap.get(EventCol.getDefiningAttributeKeys()[0]).toString());
					}
					Event1 = templist.toArray(new String[trace.size()]);
					for (int i = 0; i < Event1.length - 2; i++) {
						temp1 = Event1[i];
						temp2 = Event1[i + 1];
						temp3 = Event1[i + 2];
						
						if (NoiseIndirectDependecies[(ActivityList.indexOf(temp1) * (ActivityCount.length))
								+ ActivityList.indexOf(temp2)][ActivityList.indexOf(temp3)] == 1)
							TraceFlag++;
					}
					
					if (Event1.length >1){
						for (int i = 0; i < Event1.length - 1; i++) {
							temp1 = Event1[i];
							temp2 = Event1[i + 1];

							if (NoiseDirectDependencies[ActivityList.indexOf(temp1)][ActivityList.indexOf(temp2)] == 1)
								TraceFlag++;
						}
					
						if (NoiseInitialEdges[(ActivityList.indexOf(Event1[0]) * (ActivityCount.length))
						                      + ActivityList.indexOf(Event1[1])] == 1
						                      || NoiseInitialPoints[ActivityList.indexOf(Event1[0])] == 1
						                      || 
						                      NoiseFinalEdges[(ActivityList.indexOf(Event1[Event1.length - 2]) * (ActivityCount.length))
											                      + ActivityList.indexOf(Event1[Event1.length - 1])] == 1
											                      || NoiseFinalPoints[ActivityList.indexOf(Event1[Event1.length - 1])] == 1)
							TraceFlag++;
						
						
					}
					else 
						TraceFlag++;

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
					TempTrace = factory.createTrace();
					TempTrace.setAttributes((XAttributeMap) trace.getAttributes().clone());
					String[] Event1 = new String[trace.size()];
					List<String> templist = new ArrayList<String>();
					for (XEvent event : trace) {
						eventAttributeMap = event.getAttributes();
						templist.add(eventAttributeMap.get(EventCol.getDefiningAttributeKeys()[0]).toString());
					}
					Event1 = templist.toArray(new String[trace.size()]);
					int[] EventFlag = new int[trace.size()];
					int EventCount = 0;

					for (int i = 0; i < Event1.length - 1; i++) {
						EventCount++;
						temp1 = Event1[i];
						temp2 = Event1[i + 1];
						if (NoiseDirectDependencies[ActivityList.indexOf(temp1)][ActivityList.indexOf(temp2)] == 1) {
							EventFlag[EventCount]++;
						}
					}
					EventCount = 1;
					if (NoiseInitialPoints[ActivityList.indexOf(Event1[0])] == 1) {
						EventFlag[0]++;
					}

					for (int i = 0; i < Event1.length - 2; i++) {
						EventCount++;
						temp1 = Event1[i];
						temp2 = Event1[i + 1];
						temp3 = Event1[i + 2];
						if (NoiseIndirectDependecies[(ActivityList.indexOf(temp1) * (ActivityCount.length))
								+ ActivityList.indexOf(temp2)][ActivityList.indexOf(temp3)] == 1) {
							EventFlag[EventCount]++;
						}
					}
					if (NoiseInitialEdges[(ActivityList.indexOf(Event1[0]) * (ActivityCount.length))
							+ ActivityList.indexOf(Event1[1])] == 1
							|| NoiseInitialPoints[ActivityList.indexOf(Event1[0])] == 1) {
						EventFlag[1]++;
					}
					int counter = 0;
					for (XEvent event : trace) {
						if (EventFlag[counter] == 0) {
							TempTrace.add(event);
						}
						counter++;
					}
					if (TempTrace.size() > 0)
						outputLog2.add(TempTrace);

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
