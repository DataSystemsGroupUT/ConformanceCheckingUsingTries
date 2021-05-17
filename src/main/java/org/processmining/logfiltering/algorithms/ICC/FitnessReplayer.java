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

public class FitnessReplayer implements IncrementalReplayer {

	ReplayResultsContainer traceFitnessHistory;
	IccParameters iccparameters;
	
	public FitnessReplayer(IccParameters parameters) {
		this.traceFitnessHistory=new ReplayResultsContainer();
		this.iccparameters=parameters;
	}
	
	public boolean TraceVariantKnown(XTrace trace) {
		if(this.traceFitnessHistory.contains(this.traceFitnessHistory.convertToString(trace))) {
			return true;
		}
		return false;
	}

	public boolean abstractAndCheckPredicate(XTrace trace, Object[] additionalInformation) {
		PetrinetGraph net=(PetrinetGraph) additionalInformation[0];
		XAttributeMap logAttributes=(XAttributeMap) additionalInformation[1];
		TransEvClassMapping mapping =(TransEvClassMapping) additionalInformation[2];
		XLog log=new XLogImpl(logAttributes);
		log.add(trace);

		
		int nThreads = 2;
		int costUpperBound = Integer.MAX_VALUE;
		
		XEventClassifier eventClassifier=XLogInfoImpl.NAME_CLASSIFIER;
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);
		XEventClasses classes = summary.getEventClasses();
		
		Marking initialMarking = getInitialMarking(net);
		Marking finalMarking = getFinalMarking(net);
		
		ReplayerParameters parameters = new ReplayerParameters.Default(nThreads, costUpperBound, Debug.NONE);
		Replayer replayer = new Replayer(parameters, (Petrinet) net, initialMarking, finalMarking, classes, mapping, false);
		PNRepResult pnrresult;
		Double rawFitness=-1.0;
		try {
			pnrresult = replayer.computePNRepResult(Progress.INVISIBLE, log);
			rawFitness= (Double) pnrresult.getInfo().get("Raw Fitness Cost");
			
			//TODO shortest petrinet length should not be saved in this class
			if (traceFitnessHistory.getTraceVariantCount()<=0) { 
		          Object shortestPathLength=pnrresult.getInfo().get("Model move cost empty trace"); 
		          traceFitnessHistory.setShortestPathLength(new Double(shortestPathLength.toString())); 
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		
		double fitness= 1-rawFitness/((int) (log.get(0).size()+traceFitnessHistory.getPetriNetShortestPath()));
		TraceReplayResult result = new TraceReplayResult(this.traceFitnessHistory.convertToString(trace),trace,trace.size(),false,false,false,rawFitness,fitness,null);
		
		
		//information predicate
		double oldHistoryFitness=traceFitnessHistory.getFitness();
		traceFitnessHistory.put(result.getActivities(), result);
		double newHistoryFitness=traceFitnessHistory.getFitness();
		
		if(Math.abs(oldHistoryFitness-newHistoryFitness)>iccparameters.getEpsilon()) {
			return true;
		}
		else return false;
	}

	public boolean incrementAndCheckPredicate(XTrace trace) {
		double oldHistoryFitness=traceFitnessHistory.getFitness();
		this.traceFitnessHistory.incrementMultiplicity(this.traceFitnessHistory.convertToString(trace));
		double newHistoryFitness=traceFitnessHistory.getFitness();
		if(Math.abs(oldHistoryFitness-newHistoryFitness)>iccparameters.getEpsilon()) {
			return true;
		}
		else return false;
	}
	

	public ReplayResultsContainer getResult() {
		return traceFitnessHistory;
	}
	
	
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
