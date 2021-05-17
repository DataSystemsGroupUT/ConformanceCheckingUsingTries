package org.processmining.logfiltering.algorithms.ICC;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;

import nl.tue.alignment.Replayer;
import nl.tue.alignment.ReplayerParameters;
import nl.tue.alignment.TraceReplayTask;
import nl.tue.alignment.algorithms.ReplayAlgorithm.Debug;
/**
 * 
 * @author Martin Bauer
 *
 */
public class AlignmentReplayer implements IncrementalReplayer {

	ReplayResultsContainer traceAlignmentHistory;
	//Multiset<String> asynchMovesHistory;
	Replayer replayer;
	IccParameters iccParameters;
	
	public AlignmentReplayer(IccParameters iccParameters) {
		this.iccParameters = iccParameters;
		this.traceAlignmentHistory=new ReplayResultsContainer();
	}
	
	public boolean TraceVariantKnown(XTrace trace) {
		if(this.traceAlignmentHistory.contains(this.traceAlignmentHistory.convertToString(trace))) {
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
		
		Multiset<String> asynchronousMoveBag=TreeMultiset.create();
		
		int nThreads = 2;
		int costUpperBound = Integer.MAX_VALUE;
		
		XEventClassifier eventClassifier=XLogInfoImpl.STANDARD_CLASSIFIER;
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);
		XEventClasses classes = summary.getEventClasses();
		
		Marking initialMarking = getInitialMarking(net);
		Marking finalMarking = getFinalMarking(net);
		
		ReplayerParameters parameters = new ReplayerParameters.Default(nThreads, costUpperBound, Debug.NONE);
		Replayer replayer = new Replayer(parameters, (Petrinet) net, initialMarking, finalMarking, classes, mapping, false);

		// timeout per trace in milliseconds
		int timeoutMilliseconds = 10 * 1000;
		// preprocessing time to be added to the statistics if necessary
		long preProcessTimeNanoseconds = 0;
		
		ExecutorService service = Executors.newFixedThreadPool(parameters.nThreads);
		
		@SuppressWarnings("unchecked")
		Future<TraceReplayTask>[] futures = new Future[log.size()];

		for (int i = 0; i < log.size(); i++) {
			// Setup the trace replay task
			TraceReplayTask task = new TraceReplayTask(replayer, parameters, log.get(i), i, timeoutMilliseconds,
					parameters.maximumNumberOfStates, preProcessTimeNanoseconds);

			// submit for execution
			futures[i] = service.submit(task);
		}
		// initiate shutdown and wait for termination of all submitted tasks.
		service.shutdown();
		
		// obtain the results one by one.
		for (int i = 0; i < log.size(); i++) {

			TraceReplayTask result;
			try {
				result = futures[i].get();
			} catch (Exception e) {
				// execution os the service has terminated.
				assert false;
				throw new RuntimeException("Error while executing replayer in ExecutorService. Interrupted maybe?", e);
			}
			SyncReplayResult replayResult = result.getSuccesfulResult();
			for (int j=0;j<replayResult.getStepTypes().size();j++) {
				if(replayResult.getStepTypes().get(j).toString().equals("Log move") || replayResult.getStepTypes().get(j).toString().equals("Model move")) {
					//System.out.println(replayResult.getNodeInstance().get(j).toString());
					if(replayResult.getStepTypes().get(j).toString().equals("Model move")) {
						if(mapping.containsKey(replayResult.getNodeInstance().get(j))) {
							asynchronousMoveBag.add(mapping.get(replayResult.getNodeInstance().get(j)).toString());
						}
						else {
							asynchronousMoveBag.add((replayResult.getNodeInstance().get(j)).toString());
						}
					}
					if(replayResult.getStepTypes().get(j).toString().equals("Log move")) {
						asynchronousMoveBag.add((replayResult.getNodeInstance().get(j)).toString());
					}
				}
			}
		}
		TraceReplayResult result = new TraceReplayResult(this.traceAlignmentHistory.convertToString(trace),trace, trace.size(), false, false, false,-1, -1, asynchronousMoveBag);
		Multiset<String> oldAsynchMoves = traceAlignmentHistory.getAsynchMoves();
		traceAlignmentHistory.put(result.getActivities(), result);
		Multiset<String> newAsynchMoves = traceAlignmentHistory.getAsynchMoves();
		
		double difference=0.0;
		for(String activity : newAsynchMoves.elementSet()) {
			double relativeFrequencyInNew = (double)newAsynchMoves.count(activity)/(double)newAsynchMoves.size();
			double relativeFrequencyInOld;
			if (!oldAsynchMoves.contains(activity)) {
				relativeFrequencyInOld=0.0;
			}
			else relativeFrequencyInOld = (double)oldAsynchMoves.count(activity)/(double)oldAsynchMoves.size();
			double dif=Math.abs(relativeFrequencyInOld-relativeFrequencyInNew);
			
			difference+=dif;
		}		
		if(difference>this.iccParameters.getEpsilon()) {
			return true;
		}
		else return false;
	}

	
	
	public boolean incrementAndCheckPredicate(XTrace trace) {
		Multiset<String> oldAsynchMoves = traceAlignmentHistory.getAsynchMoves();
		traceAlignmentHistory.incrementMultiplicity(this.traceAlignmentHistory.convertToString(trace));
		Multiset<String> newAsynchMoves = traceAlignmentHistory.getAsynchMoves();
		
		double difference=0.0;
		for(String activity : newAsynchMoves.elementSet()) {
			double relativeFrequencyInNew = (double)newAsynchMoves.count(activity)/(double)newAsynchMoves.size();
			double relativeFrequencyInOld;
			if (!oldAsynchMoves.contains(activity)) {
				relativeFrequencyInOld=0.0;
			}
			else relativeFrequencyInOld = (double)oldAsynchMoves.count(activity)/(double)oldAsynchMoves.size();
			double dif=Math.abs(relativeFrequencyInOld-relativeFrequencyInNew);
			difference+=dif;		
		}
		if(difference>this.iccParameters.getEpsilon()) {
			return true;
		}
		else return false;
	}
	
	
	
	public ReplayResultsContainer getResult() {
		return traceAlignmentHistory;
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

	public void init(UIPluginContext context, PetrinetGraph net, XLog log) {
		// TODO Auto-generated method stub
		
	}

}
