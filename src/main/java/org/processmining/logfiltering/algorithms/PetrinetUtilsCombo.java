     package org.processmining.logfiltering.algorithms;



     import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

     public class PetrinetUtilsCombo {

     	public static Set<Transition> getNextTransitions(Place place, Petrinet net) {

     		assert net.getPlaces().contains(place);

     		Set<Transition> nextTransitions = new HashSet<Transition>();

     		for (PetrinetEdge<?, ?> e : net.getOutEdges(place)) {
     			if (e.getTarget() instanceof Transition)
     				nextTransitions.add((Transition) e.getTarget());
     		}
     		return nextTransitions;
     	}

     	public static Set<Place> getPreviousPlaces(Transition transition, Petrinet net) {

     		assert net.getTransitions().contains(transition);

     		Set<Place> previousPlaces = new HashSet<Place>();
     		for (PetrinetEdge<?, ?> e : net.getInEdges(transition)) {
     			if (e.getSource() instanceof Place)
     				previousPlaces.add((Place) e.getSource());
     		}

     		return previousPlaces;
     	}

     	public static Set<Place> getNextPlaces(Transition transition, Petrinet net) {

     		assert net.getTransitions().contains(transition);

     		Set<Place> previousPlaces = new HashSet<Place>();
     		for (PetrinetEdge<?, ?> e : net.getOutEdges(transition)) {
     			if (e.getTarget() instanceof Place)
     				previousPlaces.add((Place) e.getTarget());
     		}

     		return previousPlaces;
     	}

     	public static boolean isTransitionEnabled(Transition transition, Map<Place, Integer> markings, Petrinet net) {

     		for (Place p : getPreviousPlaces(transition, net)) {
     			//if any inboud place does not have tokens, the transition cannot be fired
     			if (markings.get(p) == 0)
     				return false;
     		}
     		return true;
     	}

     	public static Map<Place, Integer> fireTransition(Transition transition, Map<Place, Integer> markings,
     			Petrinet net) {

     		//consume tokens
     		for (Place p : getPreviousPlaces(transition, net)) {
     			markings.put(p, markings.get(p) - 1);
     		}

     		//produce tokens
     		for (Place p : getNextPlaces(transition, net)) {
     			markings.put(p, markings.get(p) + 1);
     		}

     		return markings;
     	}

     }

