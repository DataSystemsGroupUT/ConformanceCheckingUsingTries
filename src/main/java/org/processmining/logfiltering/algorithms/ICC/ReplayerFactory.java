package org.processmining.logfiltering.algorithms.ICC;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;

import nl.tue.alignment.Replayer;
import nl.tue.alignment.ReplayerParameters;
import nl.tue.alignment.algorithms.ReplayAlgorithm.Debug;


//TODO check if this is really needed
public class ReplayerFactory {
	
	//TODO clean up
	public static Replayer createReplayer(XLog log, PetrinetGraph net, TransEvClassMapping mapping) {
		Debug debug = Debug.NONE;
		Marking initialMarking = getInitialMarking(net);
		Marking finalMarking = getFinalMarking(net);
		XEventClassifier eventClassifier = XLogInfoImpl.STANDARD_CLASSIFIER;
		XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
		//System.out.print("Constructed Mapping: "+mapping.entrySet());
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);
		XEventClasses classes = summary.getEventClasses();
		int threads = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
		//System.out.println("Thread count: "+threads);
		//int threads=1;
		// timeout 60 minutes
		int timeout = 60 * 60 * 1000;
		int initBins = 1;
//why astar????		//ReplayerParameters parameters = new ReplayerParameters.AStarWithMarkingSplit(false, threads, false,
		//		initBins, debug, timeout, true);
		ReplayerParameters parameters = new ReplayerParameters.AStar();
		return new Replayer(parameters, (Petrinet) net, initialMarking, finalMarking, classes, mapping, true);
	}
	
	private static Marking getInitialMarking(PetrinetGraph net) {
		Marking initMarking = new Marking();

		for (Place p : net.getPlaces()) {
			if (net.getInEdges(p).isEmpty())
				initMarking.add(p);
		}

		return initMarking;
	}
	
	private static Marking getFinalMarking(PetrinetGraph net) {
		Marking finalMarking = new Marking();

		for (Place p : net.getPlaces()) {
			if (net.getOutEdges(p).isEmpty())
				finalMarking.add(p);
		}

		return finalMarking;
	}
	
	private static TransEvClassMapping constructMapping(PetrinetGraph net, XLog log, XEventClass dummyEvClass,
			XEventClassifier eventClassifier) {
		TransEvClassMapping mapping = new TransEvClassMapping(eventClassifier, dummyEvClass);

		XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);
		
		for (Transition t : net.getTransitions()) {
			for (XEventClass evClass : summary.getEventClasses().getClasses()) {
				String eventId = evClass.getId();
				//String strippedEventId=eventId.split("+")[0];
				//System.out.println(t.toString()+"<->"+eventId);
				if (eventId.startsWith(t.toString())) {
					//System.out.println(t.toString()+"<->"+eventId);
					mapping.put(t, evClass);
					break;
				}
			}

			//			if (!mapped && !t.isInvisible()) {
			//				mapping.put(t, dummyEvClass);
			//			}

		}

		return mapping;
	}
}
