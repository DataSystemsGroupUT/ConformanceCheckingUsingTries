package org.processmining.logfiltering.statisticalInductiveMiner;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import javax.swing.JOptionPane;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIM;
import org.processmining.plugins.InductiveMiner.plugins.IMProcessTree;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.processtree.ProcessTree;
import org.processmining.projectedrecallandprecision.plugins.CompareLog2ProcessTreePlugin;
import org.processmining.projectedrecallandprecision.result.ProjectedRecallPrecisionResult;

public class StatisticalInductiveMiner{
	
	//for use in the algorithm
	double probabilityThreshold=0.01;
	double confidenceLevel =0.999;
	boolean eventTimeAnalysis=false;
	double epsilonInMinutes=0;


	//for use in performance analysis
	double deltaModelIncrement=1;
	double deltaEventIncrement=1;
	int NoOfExperiments=5;
	int NoOfMeasurementsPerExperiment=100;
	
	
	@Plugin(name = "Statistical sampling", returnLabels = { "XLog" }, returnTypes = { XLog.class }, parameterLabels = { "Log" }, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Martin Bauer", email = "bauermax@informatik.hu-berlin.de")
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	public XLog doSamplingOfLog(final UIPluginContext context, XLog log) throws Exception {
		//MiningParameters parameters= getMiningParameters(context, log);
		XLog newLog = statisticalPreprocess(log, probabilityThreshold, confidenceLevel);
		return newLog;
	}
	
	@Plugin(name = "Mine Process Tree with statistical Inductive Miner", returnLabels = { "Process Tree" }, returnTypes = { ProcessTree.class }, parameterLabels = { "Log" }, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Martin Bauer", email = "bauermax@informatik.hu-berlin.de")
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	//get mining parameters
	//preprocess log
	//mine with new log
	public ProcessTree mineStatisticalGuiProcessTree(final UIPluginContext context, XLog log) throws Exception {
			MiningParameters parameters= getMiningParameters(context, log);
			XLog newLog = statisticalPreprocess(log, probabilityThreshold, confidenceLevel);
			ProcessTree processTree = mineProcessTree(context, newLog, parameters);
			return processTree;
	}
	
	
	
	@Plugin(name = "Analyze performance of statistical Inductive Miner", returnLabels = { "Process Tree" }, returnTypes = { String.class }, parameterLabels = { "Log" }, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Martin Bauer", email = "bauermax@informatik.hu-berlin.de")
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	public String ExecuteAnalysis(final UIPluginContext context, XLog log) throws Exception {
		//get Mining and Fitness calculation Parameters per GUI
		MiningParameters parameters= getMiningParameters(context, log);
		MiningParameters iMParameters=new MiningParametersIM();
		iMParameters.setNoiseThreshold((float) 0.0);
		CompareLog2ProcessTreePlugin fitnessCalculator= new CompareLog2ProcessTreePlugin();
		
		for(int i=1;i<=NoOfExperiments;i++){
			System.gc();
			//set the Threshold Parameters;
			if(!eventTimeAnalysis){
				if(i>1){
					epsilonInMinutes+=deltaModelIncrement;
				}
			}	
			
			if(eventTimeAnalysis){
				if(i>1){
					epsilonInMinutes+=deltaEventIncrement;
				}
			}
			
			
			//Analysis criteria that will be measured 
			String sIMpreProcessingTimes=		"sIMi - preProcessingTime: ";
			String sIMminingTimes=				"sIMi - MiningTime:        ";
			String sIMtotalTimes=				"sIMi - TotalTime:         ";
			String sIMtotalMemoryConsumption=	"sIMi - TotalMemory:       ";
			String totalTraces=					" IM  - Total Traces:      ";
			String sIMTraces=					"sIM  - Traces:           ";
			String IMtotalTimes=				" IMi - TotalTime:         ";
			String IMTotalMemoryConsumption=	" IMi - Memory:            ";
			String IMFitness=					" IM  - Fitness:           ";
			String IMiFitness=					" IMi - Fitness:           ";	
			String sIMiFitness=					"sIMi - Fitness:           ";
		
			
			String filename = "Log_";
			if(eventTimeAnalysis){
				filename+="restrictive"+"_"+epsilonInMinutes;
			}
			else filename+="lax"+"_"+epsilonInMinutes;
			System.out.println("Starting sIM Analysis "+i+" of "+NoOfExperiments+" - Results will be written to"+filename+".txt");
			totalTraces=Integer.toString(log.size());
			
			//IM
			if(i==1){
			System.out.println("\tCalculating Fitness and Precision of unfiltered IM");
			System.out.println("\t\t IM - Mining");
			ProcessTree iMProcessTree=mineProcessTree(context, log, iMParameters);
			System.out.println("\t\t IM - Calculating Fitness");
			ProjectedRecallPrecisionResult iMFitnessResult=fitnessCalculator.measure(context, log, iMProcessTree);
			IMFitness=Double.toString(iMFitnessResult.getRecall());
			}
			
			//k times
			System.gc();
			for(int k=1;k<=NoOfMeasurementsPerExperiment;k++){
				System.gc();
				System.out.println("\tStarting Measurement "+(k)+" of "+NoOfMeasurementsPerExperiment);
				
				//sIMi
				System.out.println("\t\t sIMi - PreProcessing");
				
				long preProcessingStart = System.currentTimeMillis();
				double sIMStartMemory = (Runtime.getRuntime().totalMemory() -  Runtime.getRuntime().freeMemory())/ 1024d / 1024d;
				XLog newLog=statisticalPreprocess(log, probabilityThreshold, confidenceLevel);
				long preProcessingEnd = System.currentTimeMillis();
				System.out.println("\t\t sIMi - Mining");
				long sIMStart = System.currentTimeMillis();
				ProcessTree statisticalProcessTree = mineProcessTree(context, newLog, parameters);
				double sIMEndMemory = (Runtime.getRuntime().totalMemory() -  Runtime.getRuntime().freeMemory())/ 1024d / 1024d;
				long sIMEnd = System.currentTimeMillis();
				System.out.println("\t\t sIMi - Calculating Fitness");
				ProjectedRecallPrecisionResult sIMiFitnessResult=fitnessCalculator.measure(context, newLog, statisticalProcessTree);
				
				//IMi
				System.out.println("\t\t IMi - Mining");
				long IMStart = System.currentTimeMillis();
				double IMStartMemory = (Runtime.getRuntime().totalMemory() -  Runtime.getRuntime().freeMemory())/ 1024d / 1024d;
				ProcessTree processTree = mineProcessTree(context, log, parameters);
				double IMEndMemory = (Runtime.getRuntime().totalMemory() -  Runtime.getRuntime().freeMemory())/ 1024d / 1024d;
				long IMEnd = System.currentTimeMillis();
				if(k==1 && i==1){
					System.out.println("\t\t IMi -Calculating Fitness");
					ProjectedRecallPrecisionResult IMiFitnessResult=fitnessCalculator.measure(context, log, processTree);
					IMiFitness=IMiFitness+", "+Double.toString(IMiFitnessResult.getRecall());
				}
				long preProcessingTime=preProcessingEnd-preProcessingStart;
				long sIMMiningTime=sIMEnd-sIMStart;
				long sIMTime=preProcessingTime+sIMMiningTime;
				long IMTime=IMEnd-IMStart;
				double  sIMMemory = sIMEndMemory-sIMStartMemory;
				double  IMMemory = IMEndMemory-IMStartMemory;
	
				
				sIMpreProcessingTimes=sIMpreProcessingTimes+preProcessingTime+",";
				sIMminingTimes=sIMminingTimes+sIMMiningTime+",";
				sIMtotalTimes=sIMtotalTimes+sIMTime+",";
				IMtotalTimes=IMtotalTimes+IMTime+",";
				sIMtotalMemoryConsumption=sIMtotalMemoryConsumption+sIMMemory+",";
				IMTotalMemoryConsumption=IMTotalMemoryConsumption+IMMemory+",";
				sIMiFitness=sIMiFitness+","+Double.toString(sIMiFitnessResult.getRecall());
				
	
				
				
				sIMTraces=sIMTraces+newLog.size()+",";
			}
			try{
			    PrintWriter writer = new PrintWriter(filename, "UTF-8");
			    writer.println("===Runtime analysis===");
			    writer.println(IMtotalTimes);
			    writer.println(sIMpreProcessingTimes);
			    writer.println(sIMminingTimes);
			    writer.println(sIMtotalTimes+"\n");
			    writer.println("===Trace analysis===");
			    writer.println(totalTraces);
			    writer.println(sIMTraces+"\n");
			    writer.println("===Memory analysis===");
			    writer.println(IMTotalMemoryConsumption);
			    writer.println(sIMtotalMemoryConsumption+"\n");
			    writer.println("===Fitness analysis===");
			    writer.println(IMiFitness);
			    writer.println(sIMiFitness+"\n");
			    writer.close();
			} catch (IOException e) {
			   // do something
			}
		}
		return "Experiment finished without Errors";
	}
	
	
	protected XLog statisticalPreprocess(XLog log, double probabilityThreshold2, double confidenceLevel2) throws Exception{
		confidenceLevel=confidenceLevel2;
		probabilityThreshold=probabilityThreshold2;
		Random generator = new Random(System.currentTimeMillis());
		PriorityQueue<Integer> pickedTraces=new PriorityQueue<>();
		
		//calculate the statistical threshold
		int threshold=calculateTraceThreshold(probabilityThreshold, confidenceLevel);
		
		//new empt log with same format as original one
		XLog newLog=new XLogImpl(log.getAttributes());
		
		//Information bases of our analysis criteria
		List<String> knownEdges=new ArrayList<>();
		List<String> knownEvents=new ArrayList<>();
		List<String> knownStartingEvents=new ArrayList<>();
		List<String> knownEndingEvents=new ArrayList<>();
		
		//for cycle time one information base for model based and one for event based analyzis is created
		EventTimeObject modelTime=new EventTimeObject(epsilonInMinutes);
		Map<String, EventTimeObject> eventTimeList=new HashMap<>();
		SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

		//Variables for the binomial experiment
		boolean newInformationGained=true;
		int tracesWithoutNewInformation=0;
	
		//pick a trace
		for(int i=log.size();i>0;i--){
			newInformationGained=false;
			int randomTrace=generator.nextInt(i);
			pickedTraces.add(randomTrace);
			XTrace currentXTrace=log.get(randomTrace);
			String currentEvent="";
			String priorEvent="";
			Date priorDate=null;
			Date currentDate=null;
			
			//update model cycle time
			if(!eventTimeAnalysis){
				Date modelStartingTime=dateFormat.parse(currentXTrace.get(0).getAttributes().get("time:timestamp").toString());
				Date modelEndingTime=dateFormat.parse(currentXTrace.get(currentXTrace.size()-1).getAttributes().get("time:timestamp").toString());
				double modelDifference=(modelEndingTime.getTime()-modelStartingTime.getTime());
				boolean didModelTimechange=modelTime.addNewEventTime(modelDifference);
				
				if(didModelTimechange){
					//newInformationGained=true; // by commenting we do not consider the epsilon
					
				}
			}
			
			//iterate over the events
			for(int j=0;j<currentXTrace.size();j++){
				priorEvent=currentEvent;
				priorDate=currentDate;
				XEvent currentXEvent=currentXTrace.get(j);
				currentEvent=currentXEvent.getAttributes().get("concept:name").toString();
				currentDate=dateFormat.parse(currentXEvent.getAttributes().get("time:timestamp").toString());
				
				//is the seen event new?
				if(!knownEvents.contains(currentEvent)){
					knownEvents.add(currentEvent);
					newInformationGained=true;
	
				}
				//is the event a new starting node in the df-graph?
				if(priorEvent.equals("") && !knownStartingEvents.contains(currentEvent)){
					knownStartingEvents.add(currentEvent);
					newInformationGained=true;
				}
				//is the event a new ending node in the df-graph?
				if(j==currentXTrace.size()-1 && !knownEndingEvents.contains(currentEvent)){
					knownEndingEvents.add(currentEvent);
					newInformationGained=true;
					
				}
				//is the seen edge new?
				if(!priorEvent.equals("") && !knownEdges.contains("("+priorEvent+","+currentEvent+")")){
					knownEdges.add("("+priorEvent+","+currentEvent+")");
					newInformationGained=true;
					
				}
				
				//has the event changed the average event time of the event?
				if(eventTimeAnalysis){
					if(j!=0){
						if (eventTimeList.containsKey(currentEvent)){
							double difference=(currentDate.getTime()-priorDate.getTime());
							boolean didTimechange=eventTimeList.get(currentEvent).addNewEventTime(difference);
							
							if(didTimechange){
								newInformationGained=true;
							}
						}
						else{
							EventTimeObject newEventTime=new EventTimeObject(epsilonInMinutes);
							eventTimeList.put(currentEvent, newEventTime);
							newInformationGained=true;
						}	
					}
					else{
						if (!eventTimeList.containsKey(currentEvent)){
							EventTimeObject newEventTime=new EventTimeObject(epsilonInMinutes);
							eventTimeList.put(currentEvent, newEventTime);
							newInformationGained=true;				
						}
					}
				}
			}
			
			//trace analysis finished - update counter accordingly
			if(!newInformationGained){
				tracesWithoutNewInformation++;
			}
			else{
				tracesWithoutNewInformation=0;
			}
			if(tracesWithoutNewInformation==threshold){
				while(!pickedTraces.isEmpty()){
					//build new log with same order of traces as in original log
					newLog.add(log.get(pickedTraces.poll()));
				}
				return newLog;
			}
		}
		//if hole log is traveresed just return original one
		return log;
	}

	
	
	//calculate the trace threshold for the experiment
	protected int calculateTraceThreshold(double threshold, double confidenceLevel) {
		ThresholdCalculator thresholdCalculator=new ThresholdCalculator(threshold, confidenceLevel);
		int thresholdHistory=thresholdCalculator.getThresholdCalculation();
		return thresholdHistory;
	}
	
	
	
	//taken from IM.java - mine a ProcessTree
	protected ProcessTree mineProcessTree(final UIPluginContext context, XLog log, MiningParameters parameters) {
		context.log("Mining...");
		return IMProcessTree.mineProcessTree(log, parameters, new Canceller() {
			public boolean isCancelled() {
				return context.getProgress().isCancelled();
			}
		});
	}

	
	
	//taken from IM.java - collects the mining parameters through a gui wizard
	protected MiningParameters getMiningParameters(final UIPluginContext context, XLog log){
		//copied from IM to get user input prior to time measurement
		IMMiningDialog dialog = new IMMiningDialog(log);
		MiningParameters parameters = dialog.getMiningParameters();
		InteractionResult result = context.showWizard("Mine using Inductive Miner", true, true, dialog);
		if (result != InteractionResult.FINISHED || !confirmLargeLogs(context, log, dialog)) {
			context.getFutureResult(0).cancel(false);
			return null;
		}
		return parameters;
	}
	
	
	//taken from IM.java - user has to confirm that he wants to mine the possibly large log
	protected boolean confirmLargeLogs(final UIPluginContext context, XLog log, IMMiningDialog dialog) {
		if (dialog.getVariant().getWarningThreshold() > 0) {
			XEventClassifier classifier = dialog.getMiningParameters().getClassifier();
			XLogInfo xLogInfo = XLogInfoFactory.createLogInfo(log, classifier);
			int numberOfActivities = xLogInfo.getEventClasses().size();
			if (numberOfActivities > dialog.getVariant().getWarningThreshold()) {
				int cResult = JOptionPane
						.showConfirmDialog(
								null,
								dialog.getVariant().toString()
										+ " might take a long time, as the event log contains "
										+ numberOfActivities
										+ " activities.\nThe chosen variant of Inductive Miner is exponential in the number of activities.\nAre you sure you want to continue?",
								"Inductive Miner might take a while", JOptionPane.YES_NO_OPTION);
	
				return cResult == JOptionPane.YES_OPTION;
			}
		}
		return true;
	}

}
