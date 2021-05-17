package org.processmining.logfiltering.algorithms;


import java.awt.BorderLayout;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.logfiltering.parameters.SequenceFilterParameter;





public class SequenceExtractorAlgorithm {
	
	public static JPanel apply(XLog InputLog, SequenceFilterParameter parameters) throws IOException{
		
		/////////////////Initialization/////////////////////////////////////////////
		XEventClassifier EventCol =parameters.getEventClassifier();
		int oddDistance= parameters.getOddDistance();
		XLog OutputLog = (XLog) InputLog.clone();
//		LogProperties LogProp = new LogProperties(OutputLog); 
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(InputLog, parameters.getEventClassifier());
		int LogSize= 0;

		double HighSupportThreshold=parameters.getHighSupportPattern();
		double ConfHighConfRules = parameters.getConfHighConfRules();
		double SuppHighConfRules = parameters. getSuppHighConfRules();
		double SuppOrdinaryRules= parameters.getSuppOrdinaryRules();
		double ConfOridnaryRules= parameters.getConfOridnaryRules();
		//FilterLevel FilteringMethod=parameters.getFilterLevel();
		int i=0;
		//////////////////////////////// Computing Activities Frequency /////////////////////
		StringBuilder SequeceDatabase = new StringBuilder();
		
		int counter =0;
		
		int index = 0;
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
		int [] ActivityCount= new int[encodeMap.size()];
		int [] ActivityDistinctCount= new int[encodeMap.size()];
		String [] OccuredTraces= new String[encodeMap.size()];
		for (int j = 0; j < OccuredTraces.length; j++) {
			OccuredTraces[j]="";
		}
		
		
		////////////////////////////////// Convert to numeric && Frequency of Activities///////////	 
		
		for(XTrace trace : OutputLog){
			LogSize++;
			int [] TempActivity= new int[encodeMap.size()];
			for (XEvent event : trace){
				i= encodeMap.get(event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString());
				ActivityCount[i]++;
				TempActivity[i]=1;
				SequeceDatabase.append(i);
				SequeceDatabase.append(" ");
			}
			SequeceDatabase.append("\r\n");
			for (int j = 0; j < TempActivity.length; j++) {
				ActivityDistinctCount[j]=ActivityDistinctCount[j]+TempActivity[j];
				if (TempActivity[j]==1)
					OccuredTraces[j]+= LogSize-1 + " ";
			}
		}

////////////////////////////////Building the Core Eventually Matrix //////////////////////////
		index=0;
		Map<String, Integer> FrequentencodeMap = new HashMap<String, Integer>();
		Map<Integer, String> FrequentdecodeMap = new HashMap<Integer, String>();
		
		
			
		///////////////////Finding frequent Activities//////////
	for (int j = 0; j < ActivityCount.length; j++) {
		if (ActivityCount[j] > HighSupportThreshold*LogSize) {
			FrequentencodeMap.put( String.valueOf(j), index);
			FrequentdecodeMap.put(index, String.valueOf(j));
			index++;
		}
	}
		///////////////////// Eventually Matrix////////////////////
		int [][] EventuallyMatrix = new int [FrequentdecodeMap.size()*2][FrequentdecodeMap.size()];
		int [][] EventuallyDistinctMatrix = new int [FrequentdecodeMap.size()*2][FrequentdecodeMap.size()];
		String [][] OccuranceTraceMatrix = new String [FrequentdecodeMap.size()*2][FrequentdecodeMap.size()];
		for (int j = 0; j < FrequentdecodeMap.size()*2; j++) {
			for (int j2 = 0; j2 < FrequentdecodeMap.size(); j2++) {
				OccuranceTraceMatrix[j][j2]="";
			}
		}
		LogSize=-1;
		String [] LogTraces = SequeceDatabase.toString().split("\r\n");
		
		for (int j = 0; j < LogTraces.length; j++) { /// for each trace 
			LogSize++;
			String[] Trace = LogTraces[j].split(" ");
			int[] TempFrequentActivity= new int[FrequentdecodeMap.size()];
			int [][] Tempfordistincts =new int [FrequentdecodeMap.size()][FrequentdecodeMap.size()];
			int [][] TempfordistinctsNot =new int [FrequentdecodeMap.size()][FrequentdecodeMap.size()];

			
			
			for (int k = 0; k < Trace.length; k++) {  /// for each event
				if	(FrequentencodeMap.get(Trace[k])!=null){  /// if event is frequent 
					for (int k2 = 0; k2 < TempFrequentActivity.length; k2++) { // for previous frequent items 
						if (TempFrequentActivity[k2]==1){
							EventuallyMatrix[k2][FrequentencodeMap.get(Trace[k])]++;
							Tempfordistincts[k2][FrequentencodeMap.get(Trace[k])]=1;
						}
						else  {
							EventuallyMatrix[k2+ FrequentencodeMap.size()][FrequentencodeMap.get(Trace[k])]++;
							TempfordistinctsNot[k2][FrequentencodeMap.get(Trace[k])]=1;
							 }
					}
					TempFrequentActivity[FrequentencodeMap.get(Trace[k])]=1;
				}
			}
			
			//////////////////////Distinct part for each trace and adding trace numbers 
			for (int k3 = 0; k3 < Tempfordistincts.length; k3++) {
				for (int k4 = 0; k4 < Tempfordistincts.length; k4++) {
					TempfordistinctsNot[k3][k3]=0;
					if (Tempfordistincts[k3][k4]==1){
						EventuallyDistinctMatrix[k3][k4]++;
						OccuranceTraceMatrix[k3][k4]+=LogSize + " ";
					}
					if (TempfordistinctsNot[k3][k4]==1){
						EventuallyDistinctMatrix[k3+FrequentencodeMap.size()][k4]++;
						OccuranceTraceMatrix[k3+FrequentencodeMap.size()][k4]+=LogSize + " ";
					}
				}
				
			}
		}
		
		
		///////////////////////////////Support and Confidence/////////////////
		double [][] SupportofRulesMatrix = new double [FrequentdecodeMap.size()*2][FrequentdecodeMap.size()*2];
		double [][] ConfidenceofRulesMatrix = new double [2*FrequentdecodeMap.size()][2*FrequentdecodeMap.size()];
		String [][] OccuranceTraceMatrix2 = new String [2*FrequentdecodeMap.size()][2*FrequentdecodeMap.size()];
		
		for (int j = 0; j <FrequentdecodeMap.size(); j++) {
			for (int k = 0; k < FrequentdecodeMap.size(); k++) {
				
				SupportofRulesMatrix[j][k]=(double)EventuallyDistinctMatrix[j][k]/(LogSize+1); /// a ==> b 
				SupportofRulesMatrix[j][k+FrequentdecodeMap.size()]=(double)(ActivityDistinctCount[Integer.parseInt(FrequentdecodeMap.get(j))]-EventuallyDistinctMatrix[j][k])/(LogSize+1); /// a ==> ~b
				SupportofRulesMatrix[j+FrequentdecodeMap.size()][k]=(double)EventuallyDistinctMatrix[j + FrequentdecodeMap.size()][k]/(LogSize+1);//// ~a==>b
				SupportofRulesMatrix[j+FrequentdecodeMap.size()][k+FrequentdecodeMap.size()]= (double)EventuallyDistinctMatrix[j][k]/(LogSize+1); ////a<==b
				
				ConfidenceofRulesMatrix[j][k]=(double)EventuallyMatrix[j][k]/ActivityCount[Integer.parseInt(FrequentdecodeMap.get(j))]; /// a ==> b
				ConfidenceofRulesMatrix[j][k+FrequentdecodeMap.size()]=(double)(ActivityCount[Integer.parseInt(FrequentdecodeMap.get(j))] - EventuallyMatrix[j][k])/ActivityCount[Integer.parseInt(FrequentdecodeMap.get(j))];/// a==> ~b
				ConfidenceofRulesMatrix[j+FrequentdecodeMap.size()][k]=(double)EventuallyMatrix[j+FrequentdecodeMap.size()][k]/ActivityCount[Integer.parseInt(FrequentdecodeMap.get(j))]; /// a ==> ~b
				ConfidenceofRulesMatrix[j+FrequentdecodeMap.size()][k+FrequentdecodeMap.size()]= (double)EventuallyMatrix[j][k]/ActivityCount[Integer.parseInt(FrequentdecodeMap.get(k))]; ////a<==b
				
				OccuranceTraceMatrix2[j][k]= decodeMap.get(Integer.parseInt(FrequentdecodeMap.get(j)))+ " ==> "+decodeMap.get(Integer.parseInt(FrequentdecodeMap.get(k))); ///a==>b
				OccuranceTraceMatrix2[j][k+FrequentdecodeMap.size()]= decodeMap.get(Integer.parseInt(FrequentdecodeMap.get(j)))+ " ==> ~"+decodeMap.get(Integer.parseInt(FrequentdecodeMap.get(k))); ///a===>~b
				OccuranceTraceMatrix2[j+FrequentdecodeMap.size()][k]= "~"+decodeMap.get(Integer.parseInt(FrequentdecodeMap.get(j)))+ " ==> "+decodeMap.get(Integer.parseInt(FrequentdecodeMap.get(k))); /// ~a==>b
				OccuranceTraceMatrix2[j+FrequentdecodeMap.size()][k+FrequentdecodeMap.size()]= decodeMap.get(Integer.parseInt(FrequentdecodeMap.get(j)))+ " <== "+decodeMap.get(Integer.parseInt(FrequentdecodeMap.get(k))); ///a<==b
			}
		} 
		
JPanel jp4 = new JPanel();
String col[] = {"Sequence Rules", "Support","Confidence"};
DefaultTableModel tableModel = new DefaultTableModel(col, 0);
JTable table1 = new JTable(tableModel);

for (int j = 0; j < FrequentdecodeMap.size()*2; j++) {
	for(int k = 0; k < FrequentdecodeMap.size()*2; k++){
		if (ConfidenceofRulesMatrix[j][k] > ConfHighConfRules && SupportofRulesMatrix[j][k]>HighSupportThreshold ){
			Object[] data= { OccuranceTraceMatrix2[j][k],SupportofRulesMatrix[j][k],ConfidenceofRulesMatrix[j][k]};
			tableModel.addRow(data);
	}
	}
}

		
		JScrollPane tableContainer = new JScrollPane(table1);
		 jp4.add(tableContainer, BorderLayout.CENTER);
	return jp4;
	}
	
    
 
	
	
	
}


