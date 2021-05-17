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

public class FilterBasedOnRelationMatrixK {

	public static XLog apply(XLog InputLog, MatrixFilterParameter parameters) {
	
		/////////////////Initialization/////////////////////////////////////////////
		XEventClassifier EventCol = parameters.getEventClassifier();
		XLog OutputLog = (XLog) InputLog.clone();
		//LogProperties LogProp = new LogProperties(OutputLog);
		HashMap<String, Integer>patternMapperCounter= new HashMap<String, Integer>();
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(InputLog, EventCol);
		//XLogInfo logInfo2 = XLogInfoFactory.createLogInfo
		int LogSize = 0;
		//Map<String, String> eventAttributeTypeMap = LogProp.getEventAttributeTypeMap();
		SortedSet<String> eventAttributeSet = new TreeSet<String>();
		XAttributeMap eventAttributeMap;
		int KLength =parameters.getSubsequenceLength(); 
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
		HashMap<String, String> HashMaper =new HashMap<String, String>();
		ArrayList<int[]> action = new ArrayList<int[]>();
		AdjustingType adjustingType = parameters.getAdjustingThresholdMethod();
		ProbabilityType probabilityType= parameters.getProbabilitycomutingMethod();
		//////////////////////////////// Computing Activities Frequency /////////////////////
		int [] NewInsert= new int [ActivitiesSize+4];
		for (XTrace trace : OutputLog) {
			LogSize++;
			List<String> templist = new ArrayList<String>();
			for (XEvent event : trace) {
				//eventAttributeMap = event.getAttributes();
				temp1 = event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString();
				//				temp1 = eventAttributeMap.get(EventCol).toString();
				ActivityCount[ActivityList.indexOf(temp1)]++;
				
				
				eventAttributeMap = event.getAttributes();
				templist.add(event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString());
				
				
				
			}
			String[] Event1 = new String[trace.size()];
			Event1 = templist.toArray(new String[trace.size()]);
			NewInsert[ActivityList.indexOf(Event1[0])]++;
			
		}

		
		
		
		int [][] RealtionMatrixFrequency = new int[1][ActivitiesSize+4]; 
		int [][] TempMatrixFrequency = RealtionMatrixFrequency;
		float [][] RelationMatrixProbabilities = new float [1][ActivitiesSize+4];
		float [][] TempMatrixProbability = RelationMatrixProbabilities;
		
		int chCount =0;
		action.add(NewInsert);
		int GlobalCounter=0;
		
		for (int Len = 1; Len < KLength+1; Len++) {
			String [] Window = new String[Len];

			traceloop:
			 for (XTrace trace : InputLog) {
				
					String[] Event1 = new String[trace.size()];
					List<String> templist = new ArrayList<String>();
					for (XEvent event : trace) {
						eventAttributeMap = event.getAttributes();
						templist.add(event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString());
					}
					
					Event1 = templist.toArray(new String[trace.size()]);
					if(Event1.length >= Len){
						
					for (int i = 0; i < Event1.length - Len-1; i++) {
						chCount=0;
						for (int j = Len-1; j >= 0; j--) {
							Window[j]= Event1[i+j];
							chCount= (int) (chCount+ (ActivityList.indexOf(Window[j])+1)*java.lang.Math.pow(ActivitiesSize , j)); 
						}
						if (HashMaper.get("user1")==HashMaper.get(Integer.toString(chCount))) {  //// if this row is not inserted yet (such a great definition for null :D)
							GlobalCounter ++;
							HashMaper.put(Integer.toString(chCount), Integer.toString(GlobalCounter));
							NewInsert= new int [ActivitiesSize+4];
							NewInsert[ActivitiesSize+2]=Len;
							action.add(NewInsert);
						}
						 NewInsert= action.get(Integer.parseInt(HashMaper.get(Integer.toString(chCount))));
						//// NewInsert instruction: Activities, start, end, length, happening]
						 if (Event1.length-1 > i+Len) /// if the trace length is more than the current position
							NewInsert[ActivityList.indexOf(Event1[i+Len])]++;	// increase the frequency of happening the next activity
						 else 
							 NewInsert[ActivitiesSize+1]++;     //// increase the frequency of being the last subsequence 
						 if (i==0) 
							 NewInsert[ActivitiesSize]++; // increase the frequency of being the first subsequence
						 NewInsert[ActivitiesSize+3]++;	 	
						 action.set(Integer.parseInt(HashMaper.get(Integer.toString(chCount))), NewInsert)	;
						
					}//for
					
					}//if
				
				}
			
			
		}
		
		///////////////////////////////////////Finding Probabilities///////////////////////////////////////////////
		int InitialSum=0;
		int FinalSum=0;
		float [][] ProbabilityValues =new float [action.size()][ActivitiesSize+4]; 
		int [][] FrequencyValues = new int [action.size()][ActivitiesSize+4] ;
		action.toArray(FrequencyValues);
		int[] ActivitiesFreuencies = new int[ActivitiesSize+2];
		
		for (int i = 0; i < ActivitiesSize; i++) {/// number of happening single activities 
			if (i+1 <FrequencyValues.length && FrequencyValues[i+1][ActivitiesSize+2]==1){
			ActivitiesFreuencies[i]= FrequencyValues[i+1][ActivitiesSize+3];
			}
		}
		ActivitiesFreuencies[ActivitiesSize]= LogSize;   /// start points 
		ActivitiesFreuencies[ActivitiesSize+1]= LogSize; /// end points
		
		
		float Normalizer=1;
		for (int i = 1; i < action.size(); i++) {
			NewInsert= action.get(i);
			InitialSum = InitialSum + NewInsert[ActivitiesSize] ; /// sum of all starts
			FinalSum=FinalSum + NewInsert[ActivitiesSize+1];   //// Sum of all ends
			ProbabilityValues[i][ActivitiesSize+2]= NewInsert[ActivitiesSize+2];
			for (int j = 0; j < ActivitiesSize+2; j++) { //// for some purpose we could remove +2 and make probabilities different for end and start 
				switch (probabilityType) {
					case DIRECT :
						Normalizer =NewInsert[ActivitiesSize+3];
						break;
					case REVERSE:
						 Normalizer =  ActivitiesFreuencies[j] ;
						break;
					case MIX: 
						Normalizer=(float)(ActivitiesFreuencies[j]+ NewInsert[ActivitiesSize+3])/2;
						break;
					case AFA:
						Normalizer= (float)(NewInsert[ActivitiesSize+3])/ (ActivitiesFreuencies[j]+ NewInsert[ActivitiesSize+3]);
					default :
						Normalizer =NewInsert[ActivitiesSize+3];
						break;
				}
				 
				 
				ProbabilityValues[i][j]= (float) ((NewInsert[j]*1.0) /Normalizer);
			}
		}
/*	 the reason for commenting is to make unified probability, some times it is better to diffrenciate for start and end
 * to make it different uncomment and for above for loop ActivitiesSize+2-----> ActivitiesSize
 * 	for (int i = 1; i < action.size(); i++) {
			NewInsert= action.get(i);
		ProbabilityValues[i][ActivitiesSize]= (float) ((NewInsert[ActivitiesSize]*1.0) / InitialSum);//// being the first subsequence probability
		ProbabilityValues[i][ActivitiesSize+1]= (float) ((NewInsert[ActivitiesSize+1]*1.0) /FinalSum);//// being the last subsequence probability
		}
*/		NewInsert= action.get(0);
		for (int j = 0; j < ActivitiesSize; j++) {
			ProbabilityValues[0][j]= (float) ((NewInsert[j]*1.0) / LogSize);
		}
		//////////////////////////////Initialization////////////////////////////////////////////////////////////
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLog outputLog2 = factory.createLog();
		XLog RemovedLog= factory.createLog();
		for (XExtension extension : InputLog.getExtensions())
			{outputLog2.getExtensions().add(extension);
			 RemovedLog.getExtensions().add(extension);
			}
		
		outputLog2.setAttributes(InputLog.getAttributes());
		RemovedLog.setAttributes(InputLog.getAttributes());
		
		///////////////////////////////////////Lets Filtering////////////////////////////////////////////////////  
		float[][] TraceStatistic = new float [action.size()][3];
		for (int i = 1; i < FrequencyValues.length; i++) {
			int Max=0, Sum=0 ,CountN0=0;
			for (int j = 0; j < ActivitiesSize+1; j++) {
				if ( j !=ActivitiesSize  && FrequencyValues[i][j]>0){ /// Do not need to consider the first happening
					
						Sum= Sum+FrequencyValues[i][j];
						CountN0++;
						if (FrequencyValues[i][j]> Max){
							Max=FrequencyValues[i][j];
						}
					

				}
			} 
			if (FrequencyValues[i][ActivitiesSize+3]!=0 && CountN0!=-0)
			TraceStatistic[i][0]= (float) (Sum/ActivitiesSize+1)/FrequencyValues[i][ActivitiesSize+3];
			TraceStatistic[i][1]= (float) (Sum/CountN0)/FrequencyValues[i][ActivitiesSize+3];
			TraceStatistic[i][2]= (float) Max/FrequencyValues[i][ActivitiesSize+3]; 
			
		}
		float [] AdjustedThreshold= new float [action.size()];
		switch (adjustingType) {
			case None :
				 for (int i = 1; i < AdjustedThreshold.length; i++) {
					 AdjustedThreshold[i]= (float) parameters.getProbabilityOfRemoval();
				}
				break;
			case Mean :
				for (int i = 1; i < AdjustedThreshold.length; i++) {
					 AdjustedThreshold[i]= (float) parameters.getProbabilityOfRemoval() * TraceStatistic[i][1];
				}
				break;
			case MaxMean:
				for (int i = 1; i < AdjustedThreshold.length; i++) {
					 AdjustedThreshold[i]= (float) parameters.getProbabilityOfRemoval() * (TraceStatistic[i][2]- TraceStatistic[i][0]);
				}
				break;
			case MaxVMean:
				for (int i = 1; i < AdjustedThreshold.length; i++) {
					 AdjustedThreshold[i]= (float) parameters.getProbabilityOfRemoval() * (TraceStatistic[i][2]- TraceStatistic[i][1]);
				}
				break;
			default : /// None
				 for (int i = 1; i < AdjustedThreshold.length; i++) {
					 AdjustedThreshold[i]= (float) parameters.getProbabilityOfRemoval();
				}
				break;
		}

		////////////////////Threshold/////////////////
		
		
		
		
		///////////////////////////////////////TRACE
		int counttt=0;
		switch (FilteringMethod) {
			case TRACE :
				
				for (XTrace trace : InputLog) {
					counttt++;
					String[] Event1 = new String[trace.size()];
					List<String> templist = new ArrayList<String>();
					for (XEvent event : trace) {
						eventAttributeMap = event.getAttributes();
						templist.add(eventAttributeMap.get(EventCol.getDefiningAttributeKeys()[0]).toString());
					}
					Event1 = templist.toArray(new String[trace.size()]);
					int TraceFlag = 0;
					/////Len =0;
					if (ProbabilityValues[0][ActivityList.indexOf(Event1[0])]< parameters.getProbabilityOfRemoval())
						TraceFlag++;
					/////////
					
					FilterLengthLoop:
					for (int Len = 1; Len < KLength+1; Len++) {
						String [] Window = new String[Len];
						
						
						if(Event1.length < Len)
							break FilterLengthLoop;
						for (int i = 0; i < Event1.length - Len-1; i++) {
							chCount=0;
							for (int j = Len-1; j >= 0; j--) {
								Window[j]= Event1[i+j];
								chCount= (int) (chCount+ (ActivityList.indexOf(Window[j])+1)*java.lang.Math.pow(ActivitiesSize , j)); 
							}//chCount
							HashMaper.get(Integer.toString(chCount));
							if (Event1.length-1 > i+Len) //Ordinal
								if(ProbabilityValues[Integer.parseInt(HashMaper.get(Integer.toString(chCount)))][ActivityList.indexOf(Event1[i+Len])]<AdjustedThreshold[Integer.parseInt(HashMaper.get(Integer.toString(chCount)))]){
									TraceFlag++;
									break FilterLengthLoop;
								}
							 else //Final ir End 
								 if(ProbabilityValues[Integer.parseInt(HashMaper.get(Integer.toString(chCount)))][ActivitiesSize+1]!=0 
								 && ProbabilityValues[Integer.parseInt(HashMaper.get(Integer.toString(chCount)))][ActivitiesSize+1] < AdjustedThreshold[Integer.parseInt(HashMaper.get(Integer.toString(chCount)))]){ //parameters.getProbabilityOfRemoval()
										TraceFlag++;
										break FilterLengthLoop;
									}
							 if (i==0) // Initial or Start 
								 if(ProbabilityValues[Integer.parseInt(HashMaper.get(Integer.toString(chCount)))][ActivitiesSize]!=0 
										 && ProbabilityValues[Integer.parseInt(HashMaper.get(Integer.toString(chCount)))][ActivitiesSize] < AdjustedThreshold[Integer.parseInt(HashMaper.get(Integer.toString(chCount)))]){//parameters.getProbabilityOfRemoval()
										TraceFlag++;
										break FilterLengthLoop;
									}
						}
					}//Len
				if (TraceFlag < 1){
				outputLog2.add(trace);
					}
				else if (TraceFlag >0){ 
				RemovedLog.add(trace);
					}
				}//Trace
				break;
		
		
			case EVENT:
				XTrace TempTrace;
				XTrace InfrequentTrace;
				for (XTrace trace : InputLog) {
					InfrequentTrace = factory.createTrace();
					InfrequentTrace.setAttributes((XAttributeMap) trace.getAttributes().clone());
					TempTrace = factory.createTrace();
					TempTrace.setAttributes((XAttributeMap) trace.getAttributes().clone());
					String[] Event1 = new String[trace.size()];
					int [] EventFlage = new int [trace.size()];
					List<String> templist = new ArrayList<String>();
					for (XEvent event : trace) {
						eventAttributeMap = event.getAttributes();
						templist.add(eventAttributeMap.get(EventCol.getDefiningAttributeKeys()[0]).toString());
					}
					Event1 = templist.toArray(new String[trace.size()]);
				/////Len =0;
					if (ProbabilityValues[0][ActivityList.indexOf(Event1[0])]< parameters.getProbabilityOfRemoval())
						EventFlage[0]++;
					/////////
					
					FilterLengthLoop:
					for (int Len = 1; Len < KLength+1; Len++) {
						int EventCount=0;
						String [] Window = new String[Len];
						
						if(Event1.length < Len)
							break FilterLengthLoop;
						for (int i = 0; i < Event1.length - Len-1; i++) {
							chCount=0;
							for (int j = Len-1; j >= 0; j--) {
								Window[j]= Event1[i+j];
								chCount= (int) (chCount+ (ActivityList.indexOf(Window[j])+1)*java.lang.Math.pow(ActivitiesSize , j)); 
							}//chCount
							HashMaper.get(Integer.toString(chCount));
							if (Event1.length-1 > i+Len) //Ordinal
								if(ProbabilityValues[Integer.parseInt(HashMaper.get(Integer.toString(chCount)))][ActivityList.indexOf(Event1[i+Len])]< AdjustedThreshold[Integer.parseInt(HashMaper.get(Integer.toString(chCount)))]){
									EventFlage[i+Len+1]++;
								}
							
						}
					}//Len
					int counter = 0;
					for (XEvent event : trace) {
						if (EventFlage[counter] == 0) {
							TempTrace.add(event);
						}
						else {
							InfrequentTrace.add(event);
						}
						counter++;
					}
					if (TempTrace.size() > 1)
						outputLog2.add(TempTrace);
					if (InfrequentTrace.size() >1)
						RemovedLog.add(InfrequentTrace);

				
					
				}//Event
				
				break;
				
	}//Switch
		
		
		switch (FilteringSelection) {
			case REMOVE :
				
				return outputLog2;
			case SELECT:
				
				outputLog2=RemovedLog;
			
		}
		return outputLog2;

	}
	

	
}
