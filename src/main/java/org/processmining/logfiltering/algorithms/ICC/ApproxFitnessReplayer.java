package org.processmining.logfiltering.algorithms.ICC;

import java.util.concurrent.ExecutionException;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

import nl.tue.alignment.Progress;
import nl.tue.alignment.Replayer;
import nl.tue.alignment.ReplayerParameters;
import nl.tue.alignment.algorithms.ReplayAlgorithm.Debug;

public class ApproxFitnessReplayer implements IncrementalReplayer {
	ReplayResultsContainer traceFitnessHistory;
	IccParameters parameters;
	int properCalculated=0;
	
	
	
	public ApproxFitnessReplayer(IccParameters parameters) {
		this.traceFitnessHistory=new ReplayResultsContainer();
		this.parameters=parameters;
	}
	
	
	
	public boolean TraceVariantKnown(XTrace trace) {
		if(this.traceFitnessHistory.contains(this.traceFitnessHistory.convertToString(trace))) {
			return true;
		}
		return false;
	}
	

	//TODO change AlignmentInformation, so that it only contains trace and fitness and additional stats per trace
	//TODO add additional stats as extra fields in AlignmentInformation, recalculated, inInitialSize, etc.
	public boolean abstractAndCheckPredicate(XTrace trace, Object[] additionalInformation) {
		//abstraction function
		PetrinetGraph net=(PetrinetGraph) additionalInformation[0];
		XAttributeMap logAttributes=(XAttributeMap) additionalInformation[1];
		TransEvClassMapping mapping=(TransEvClassMapping) additionalInformation[2];
		XLog log=new XLogImpl(logAttributes);
		log.add(trace);
		double rawFitness= getRawFitness(log, net, mapping);
		double fitness= 1-rawFitness/((int) (log.get(0).size()+traceFitnessHistory.getPetriNetShortestPath()));
		TraceReplayResult result = new TraceReplayResult(this.traceFitnessHistory.convertToString(trace),trace,trace.size(),false,false,false,rawFitness,fitness,null);
		
		//information predicate
		double oldHistoryFitness=traceFitnessHistory.getFitness();
		traceFitnessHistory.put(result.getActivities(), result);
		double newHistoryFitness=traceFitnessHistory.getFitness();
		
		if(Math.abs(oldHistoryFitness-newHistoryFitness)>parameters.getEpsilon()) {
			return true;
		}
		else return false;
	}
	
	
	//TODO string conversion of object in own class
	public boolean incrementAndCheckPredicate(XTrace trace) {
		double oldHistoryFitness=traceFitnessHistory.getFitness();
		this.traceFitnessHistory.incrementMultiplicity(this.traceFitnessHistory.convertToString(trace));
		double newHistoryFitness=traceFitnessHistory.getFitness();
		if(Math.abs(oldHistoryFitness-newHistoryFitness)>parameters.getEpsilon()) {
			return true;
		}
		else return false;
	}
	
	
	

	
	public ReplayResultsContainer getResult() {
		return traceFitnessHistory;
	}
	
	
	
	private double getRawFitness(XLog currentTraceAsLog, PetrinetGraph net, TransEvClassMapping mapping) {
		//if initial size reached, approximate trace fitness
		if (this.parameters.getInitialSize()<=traceFitnessHistory.keys().size()) {
			double estimatedFitness=estimateFitness(currentTraceAsLog);
			if(estimatedFitness>this.parameters.getK()) {
				return replayTraceOnNet(currentTraceAsLog, net, mapping);
			}
			else return estimatedFitness;
		}
		else return replayTraceOnNet(currentTraceAsLog, net, mapping);
		
	}
	
	
	
	private double estimateFitness(XLog currentTraceAsLog) {
		double minRawFitness=Integer.MAX_VALUE;
		XTrace trace=currentTraceAsLog.get(0);
		for (TraceReplayResult alignmentInfo : traceFitnessHistory.values()) {
			double distance=new TraceEditDistance(trace,alignmentInfo.getTrace()).getDistance();
			double estRawFitness=distance+alignmentInfo.getRawFitness();
			if(estRawFitness<minRawFitness) {
				minRawFitness=estRawFitness;
			}
		}

		int maximalPossibleCost=(int) (currentTraceAsLog.get(0).size()+traceFitnessHistory.getPetriNetShortestPath());
		if(minRawFitness>maximalPossibleCost) {
			minRawFitness=maximalPossibleCost;
		}
		return minRawFitness;
	}	
	
	
	
	private double replayTraceOnNet(XLog currentTraceAsLog, PetrinetGraph net, TransEvClassMapping mapping) {
		int nThreads = 2;
		int costUpperBound = Integer.MAX_VALUE;
		
		XEventClassifier eventClassifier=XLogInfoImpl.STANDARD_CLASSIFIER;
		XLogInfo summary = XLogInfoFactory.createLogInfo(currentTraceAsLog, eventClassifier);
		XEventClasses classes = summary.getEventClasses();
		
		Marking initialMarking = getInitialMarking(net);
		Marking finalMarking = getFinalMarking(net);
		
		
		ReplayerParameters parameters = new ReplayerParameters.Default(nThreads, costUpperBound, Debug.NONE);
		Replayer replayer = new Replayer(parameters, (Petrinet) net, initialMarking, finalMarking, classes, mapping, false);		
		PNRepResult result;
		Double rawFitness=-1.0;
		try {
			result = replayer.computePNRepResult(Progress.INVISIBLE, currentTraceAsLog);
			rawFitness= (Double) result.getInfo().get("Raw Fitness Cost");
			
			//TODO shortest petrinet length should not be saved in this class
			if (traceFitnessHistory.getTraceVariantCount()<=0) { 
		          Object shortestPathLength=result.getInfo().get("Model move cost empty trace"); 
		          traceFitnessHistory.setShortestPathLength(new Double(shortestPathLength.toString())); 
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return rawFitness;
	}
			//AlignmentInformation traceInformation=new AlignmentInformation(false, currentTraceAsLog.get(0), trueFitness ,rawFitness);
			//alignmentContainer.put(currentTraceAsLog.get(0), traceInformation);

	private static Marking getFinalMarking(PetrinetGraph net) {
		Marking finalMarking = new Marking();

		for (Place p : net.getPlaces()) {
			if (net.getOutEdges(p).isEmpty())
				finalMarking.add(p);
		}

		return finalMarking;
	}

	private static Marking getInitialMarking(PetrinetGraph net) {
		Marking initMarking = new Marking();

		for (Place p : net.getPlaces()) {
			if (net.getInEdges(p).isEmpty())
				initMarking.add(p);
		}

		return initMarking;
	}

}
