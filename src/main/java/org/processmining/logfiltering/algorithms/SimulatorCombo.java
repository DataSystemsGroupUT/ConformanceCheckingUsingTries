package org.processmining.logfiltering.algorithms;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.petrinetsimulator.constants.LogConstants;
import org.processmining.petrinetsimulator.parameters.SimulationSettings;

import com.google.common.collect.Lists;

public class SimulatorCombo {

	private Petrinet petriNet;
	private Marking initialMarking;
	private SimulationSettings settings;

	private XFactory factory;

	public SimulatorCombo(Petrinet petriNet, Marking initialMarking, SimulationSettings settings, XFactory factory) {
		this.petriNet = petriNet;
		this.initialMarking = initialMarking;
		this.settings = settings;

		this.factory = factory;
	}

	public XLog simulate() {

		XLog log = factory.createLog();

		long current = settings.getStartDate().getTime();

		for (int i = settings.getFirstTraceID(); i < settings.getFirstTraceID() + settings.getNumberOfTraces(); i++) {
			log.add(simulateTrace(current, i));
			current = current + Math.round(settings.getCaseArrivalDistribution().nextDouble());
		}
		return log;
	}

	public XTrace simulateTrace(long startTime, int traceID) {

		XAttributeMap atts = factory.createAttributeMap();
		atts.put(LogConstants.TRACEID,
				factory.createAttributeLiteral(LogConstants.TRACEID, Integer.toString(traceID), null));

		XTrace trace = factory.createTrace(atts);
		long eventTime = startTime;

		//initialize markings
		Map<Place, Integer> markings = new HashMap<Place, Integer>();
		for (Place p : petriNet.getPlaces())
			markings.put(p, 0);

		//update with initial marking
		for (Place p : initialMarking.baseSet())
			markings.put(p, 1);

		//for a given max number of events
		for (int i = 0; i < settings.getMaxActivitiesPerCase(); i++) {

			//detect enabled transitions and fire a random one

			Set<Place> placesWithTokens = new HashSet<Place>();
			for (Place p : markings.keySet())
				if (markings.get(p) > 0)
					placesWithTokens.add(p);

			Set<Transition> nextTransitions = new HashSet<Transition>();
			//first add all
			for (Place p : placesWithTokens) {
				nextTransitions.addAll(PetrinetUtilsCombo.getNextTransitions(p, petriNet));
			}
			//now keep only the enabled ones
			Set<Transition> enabledTransitions = new HashSet<Transition>();
			for (Transition t : nextTransitions)
				if (PetrinetUtilsCombo.isTransitionEnabled(t, markings, petriNet))
					enabledTransitions.add(t);

			//if there are enabled normal transitions, fire a random one
			//if not, if there are enabled silent transitions, fire a random one
			//if not, finish.
			Transition t = null;
			if (!enabledTransitions.isEmpty()) {

				t = Lists.newArrayList(enabledTransitions)
						.get((int) Math.floor(Math.random() * enabledTransitions.size()));

				//if its not invisible, create the new time and the event
				if (!t.isInvisible()) {
					trace.add(createEvent(t, eventTime));
					eventTime = eventTime + Math.round(settings.getTaskDurationDistribution().nextDouble());
				}

			} else
				//there is nothing else enabled, so return.
				return trace;

			//update markup
			PetrinetUtilsCombo.fireTransition(t, markings, petriNet);

		}
		return trace;
	}

	private XEvent createEvent(Transition t, long time) {

		XAttributeMap atts = factory.createAttributeMap();

		atts.put(LogConstants.EVENTID, factory.createAttributeLiteral(LogConstants.EVENTID, t.getLabel(), null));
		atts.put(LogConstants.TIMESTAMP, factory.createAttributeTimestamp(LogConstants.TIMESTAMP, time, null));
		atts.put(LogConstants.LIFECYCLE, factory.createAttributeLiteral(LogConstants.LIFECYCLE, "complete", null));

		return factory.createEvent(atts);
	}

}
