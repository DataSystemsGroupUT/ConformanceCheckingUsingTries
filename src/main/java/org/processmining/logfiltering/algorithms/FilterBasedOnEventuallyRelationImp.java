package org.processmining.logfiltering.algorithms;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import org.processmining.logfiltering.parameters.MatrixFilterParameter;

public class FilterBasedOnEventuallyRelationImp {

	public static XLog apply(XLog InputLog, MatrixFilterParameter parameters) throws IOException {
	
		/////////////////Initialization/////////////////////////////////////////////
		XEventClassifier EventCol = parameters.getEventClassifier();
		XLog OutputLog = (XLog) InputLog.clone();

		XLogInfo logInfo = XLogInfoFactory.createLogInfo(InputLog, EventCol);
		int LogSize = 0;
		XAttributeMap eventAttributeMap;
		int KLength =parameters.getSubsequenceLength(); 
//		Set<int[]> ParikhVectors= new HashSet<int[]>();
		HashMap<String, Integer> ParikhVectors= new HashMap<String, Integer>();
		Set<String> ActivitySet = new HashSet<String>();
		HashMap<String, Integer> ActivitiesCounterMaper= new HashMap<String, Integer>();
		HashMap<Integer, String> MapperActivity= new HashMap<Integer, String>();
		for (XEventClass clazz : logInfo.getNameClasses().getClasses()){
			ActivitySet.add(clazz.toString());
			ActivitiesCounterMaper.put(clazz.toString(), LogSize);
			MapperActivity.put(LogSize, clazz.toString());
			LogSize++;//LogSize is only a temp integer
		}
		
		LogSize=0;
		int ActivitiesSize = ActivitySet.size();
		for (XTrace trace : OutputLog) {
			int[] parikhVector = new int[ActivitiesSize+1];
			String temp1="";
			for (XEvent event : trace) {
				temp1 = event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString();
				parikhVector [ActivitiesSize]= ActivitiesCounterMaper.get(temp1);
				String mapp= "";
				for (int i = 0; i < parikhVector.length-1; i++) {
					if (parikhVector[i]>0) {
						mapp=mapp.concat(MapperActivity.get(i)+ "="+ parikhVector[i]);
					}
				}
				mapp=mapp.concat("==>"+temp1);
				if(ParikhVectors.get(mapp)==null) {
					ParikhVectors.put(mapp, 1);
				}
				else {
					ParikhVectors.put(mapp, ParikhVectors.get(mapp)+1);
				}
					
				parikhVector[ActivitiesCounterMaper.get(temp1)]++;

			}
		}
		int tt= ParikhVectors.size();
		String[] Activities = ActivitySet.toArray(new String[ActivitiesSize]);
		List<String> ActivityList = java.util.Arrays.asList(Activities);    
		String temp1;
		int[] ActivityCount = new int[ActivitiesSize+1];
		int [][] EventualyFootprint = new int[ActivitiesSize+1][ActivitiesSize+1];
		int [][] DFFootprint = new int[ActivitiesSize+1][ActivitiesSize+1];
		
		//////////////////////////////// Computing  Frequency of relations ////////////////
		for (XTrace trace : OutputLog) {
			int [][] tempEventualyFootprint = new int[ActivitiesSize+1][ActivitiesSize+1];
			int [] PreviousActivities = new int[ActivitiesSize+1];
			PreviousActivities[0]=1;
			LogSize++;
			for (XEvent event : trace) {
				temp1 = event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString();
				
				for (int i = 0; i < PreviousActivities.length; i++) {
					if (PreviousActivities[i]==1) {
						tempEventualyFootprint [i][ActivityList.indexOf(temp1)+1]=1;
					}
				}

				PreviousActivities[ActivityList.indexOf(temp1)+1]=1;
				ActivityCount[ActivityList.indexOf(temp1)+1]++;
			}
			for (int i = 0; i < PreviousActivities.length; i++) {
				for (int j = 0; j < PreviousActivities.length; j++) {
					EventualyFootprint[i][j]=EventualyFootprint[i][j] + tempEventualyFootprint[i][j];
				}
			}
			 
			///DF
			String[] Event1 = new String[trace.size()];
			List<String> templist = new ArrayList<String>();
			for (XEvent event : trace) {
				eventAttributeMap = event.getAttributes();
				templist.add(event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString());
//				templist.add(eventAttributeMap.get(EventCol).toString());
			}
			Event1 = templist.toArray(new String[trace.size()]);
			for (int i = 0; i < Event1.length - 1; i++) {
				String temp0 = Event1[i];
				String temp2 = Event1[i + 1];
				DFFootprint[ActivityList.indexOf(temp0)][ActivityList.indexOf(temp2)]++;
			}
			
		}
		String Logname="Log1";
		String outputFile1 = "Training"+Logname+"EF.csv";
        FileWriter fileWriter = new FileWriter("D:\\PHD\\Discovery Contest\\Training Logs\\March\\"+outputFile1, true);
        BufferedWriter DFw = new BufferedWriter(fileWriter);
		for (int i = 0; i < EventualyFootprint.length-1; i++) {
			for (int j = 0; j < EventualyFootprint.length-1; j++) {
				if (EventualyFootprint[i][j]>0) {
					DFw.write(ActivityList.get(i)+ ">" +ActivityList.get(j)+ ">"+EventualyFootprint[i][j]);
					DFw.newLine();
				}
			
			}
			
		}
		 DFw.flush();
	     DFw.close();
		 outputFile1 = "Training"+Logname+"DF.csv";
         fileWriter = new FileWriter("D:\\PHD\\Discovery Contest\\Training Logs\\March\\"+outputFile1, true);
         DFw = new BufferedWriter(fileWriter);
		for (int i = 0; i < EventualyFootprint.length-1; i++) {
			for (int j = 0; j < EventualyFootprint.length-1; j++) {
				if (DFFootprint[i][j]>0) {
					DFw.write(ActivityList.get(i)+ ">" +ActivityList.get(j)+ ">"+DFFootprint[i][j]);
					DFw.newLine();
				}
			
			}
			
		}
		 DFw.flush();
	     DFw.close();
        
    
       
		///////////////////////////////////////////Creating Filtering Matrixes  /////////////////////////////////////
		 
double[][] RulesConfidence= new double [ActivitiesSize+1][ActivitiesSize+1];	
int [][] HighConfidenceRules = new int[ActivitiesSize+1][ActivitiesSize+1];
for (int i = 0; i < RulesConfidence.length; i++) {
	for (int j = 0; j < RulesConfidence.length; j++) {
		RulesConfidence[i][j]=(EventualyFootprint[i][j]*1.0)/LogSize;
		if (EventualyFootprint[i][j] > LogSize*parameters.getSubsequenceLength() * 0.01 && (EventualyFootprint[i][j] * 1.0 )/ ActivityCount[i] > parameters.getSecondDoubleVariable()  ) {
			HighConfidenceRules[i][j] = 1;
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
//////////////////////////////////////Now filter/////////////////////////////////////////////


int counttt=0;
switch (parameters.getFilterLevel()) {
	case TRACE :
		for (XTrace trace : InputLog) { // for each trace 
			counttt++;
			int TraceFlag = 0;
			String[] TraceArray = new String[trace.size()];
			List<String> templist = new ArrayList<String>();
			int [] PreviousActivities = new int[ActivitiesSize+1];
			PreviousActivities[0]=1;
			int [][] tempEventualyFootprint = new int[ActivitiesSize+1][ActivitiesSize+1];
			for (XEvent event : trace) { // for each event
				int [] Antecedence=new  int [ActivitiesSize+1];
				temp1 = event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString();
				
				for (int i = 1; i < PreviousActivities.length; i++) {
					if (PreviousActivities[i]==1) {
						tempEventualyFootprint [i][ActivityList.indexOf(temp1)+1]=1;
					}
				}
				PreviousActivities[ActivityList.indexOf(temp1)+1]=1;
			}		 // for each event
			for (int i = 1; i < PreviousActivities.length; i++) { ///for each activity
				if(PreviousActivities[i]==1) { /// For executed activities
				for (int j = 1; j < tempEventualyFootprint.length; j++) { /// relation of different activities with an executed activity
					
					if (EventualyFootprint[i][j]< LogSize* parameters.getProbabilityOfRemoval() && tempEventualyFootprint[i][j]==1) { //if Odd rule exist
						TraceFlag++;
					}
					if (HighConfidenceRules[i][j]==1 && tempEventualyFootprint[i][j]==0) { /// If a high Probable rule did not exi
						TraceFlag++;
					}
				}
			  }/// For executed activities
				
			}///////for each activity
			if (TraceFlag < 1)
			{outputLog2.add(trace);}
		else 
			{RemovedLog.add(trace);}
		} // for each trace 

		break;
		/////////////////////////////////////////////////////// Lets Event Filtering /////////////////////////////////////////
	case EVENT :
		XTrace TempTrace;
		for (XTrace trace : InputLog) {
		}
			
		break;
}

///////////////////////////////////////////////////////////////////////////////////////////////
switch (parameters.getFilteringSelection()) {
	case REMOVE :
		
		return outputLog2;
	case SELECT:
		
		return RemovedLog;
	
		
}
return outputLog2;

}

		
		
	
}
	
	

