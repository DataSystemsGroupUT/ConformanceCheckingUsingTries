package org.processmining.logfiltering.Juan.algo.Juan;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.utils.GraphIterator;
import org.processmining.models.graphbased.directed.utils.GraphIterator.EdgeAcceptor;
import org.processmining.models.graphbased.directed.utils.GraphIterator.NodeAcceptor;
import org.processmining.models.semantics.petrinet.Marking;


public class PetriNetTools {

	private Petrinet net;
	private HashMap<Transition, String> TauCoder;
	private HashMap<String, String> ActivityCoder;
	private HashMap<String, Transition> reverseTauCoder;
	private ArrayListValuedHashMap<String, String> visSucc;
	private String netActivities;

	public PetriNetTools(Petrinet petriNet, HashMap<String,String> ActivityCoder) {
		
		this.net = petriNet;
		this.ActivityCoder = ActivityCoder;
		this.TauCoder = new HashMap<Transition, String>();
		this.reverseTauCoder = new HashMap<String, Transition>();
		this.visSucc = new ArrayListValuedHashMap<String, String>();
		this.netActivities = "";
		int chCount = ActivityCoder.size() + 65;

		for (Transition trans : net.getTransitions()) {
			
			if (trans.isInvisible()) {
				
				String ID = Character.toString((char)chCount);
				trans.getVisibleSuccessors()
					 .forEach(vs -> visSucc.put(ID, ActivityCoder.get(vs.getLabel())));
				
				
				TauCoder.put(trans, ID);
				reverseTauCoder.put(ID, trans);
				chCount++;
			}else
				netActivities+=ActivityCoder.get(trans.getLabel());
					
		}
		

	}
	
	public ArrayListValuedHashMap<String, String> getVisSucc() {
		return visSucc;
	}

	public Petrinet getNet() {
		return net;
	}
	
	public HashMap<String, String> getActivityCoder() {
		return ActivityCoder;
	}

	public HashMap<Transition, String> getTauCoder() {
		return TauCoder;
	}

	public HashMap<String, Transition> getReverseTauCoder() {
		return reverseTauCoder;
	}

	private Collection<Transition> getFirstsSuccessors(PetrinetNode pn) {

		final NodeAcceptor<PetrinetNode> nodeAcceptor = new NodeAcceptor<PetrinetNode>() {
			public boolean acceptNode(PetrinetNode node, int depth) {

				return ((depth != 0) && (node instanceof Transition));
			}
		};

		Collection<PetrinetNode> transitions = GraphIterator.getDepthFirstSuccessors(pn, pn.getGraph(),
				new EdgeAcceptor<PetrinetNode, PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>>() {

					public boolean acceptEdge(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge,
							int depth) {

						return depth <=2;
					}
				}, nodeAcceptor);

		return Arrays.asList(transitions.toArray(new Transition[0]));
	}
	
	private Collection<Transition> getVisibleSuccessors(PetrinetNode pn) {

		final NodeAcceptor<PetrinetNode> nodeAcceptor = new NodeAcceptor<PetrinetNode>() {
			public boolean acceptNode(PetrinetNode node, int depth) {

				return ((depth != 0) && (node instanceof Transition) && !((Transition) node).isInvisible());
			}
		};

		Collection<PetrinetNode> transitions = GraphIterator.getDepthFirstSuccessors(pn, pn.getGraph(),
				new EdgeAcceptor<PetrinetNode, PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>>() {

					public boolean acceptEdge(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge,
							int depth) {
						PetrinetNode node = edge.getTarget();
						int s = 0;
						if ((node instanceof Transition)) {
							s = ((Transition) node).getGraph().getInEdges(node).size();
							//System.out.println(node.getLabel() + "   " + s);
						}
						return !nodeAcceptor.acceptNode(edge.getSource(), depth) && s <= 1;
					}
				}, nodeAcceptor);

		return Arrays.asList(transitions.toArray(new Transition[0]));
	}
	
	private Collection<Transition> getSelfLoops(PetrinetNode pn) {

		final NodeAcceptor<PetrinetNode> nodeAcceptor = new NodeAcceptor<PetrinetNode>() {
			public boolean acceptNode(PetrinetNode node, int depth) {

				return ((depth != 0) && (node instanceof Transition) && !((Transition) node).isInvisible()) && !((Transition) node).getLabel().contains("END");
			}
		};

		Collection<PetrinetNode> transitions = GraphIterator.getDepthFirstSuccessors(pn, pn.getGraph(),
				new EdgeAcceptor<PetrinetNode, PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>>() {

					public boolean acceptEdge(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge,
							int depth) {
						PetrinetNode node = edge.getTarget();
						int s = 0;
						if ((node instanceof Transition)) {
							s = ((Transition) node).getGraph().getInEdges(node).size();
							//System.out.println(node.getLabel() + "   " + s);
						}
						return s <= 1 && depth<=Integer.MAX_VALUE && !nodeAcceptor.acceptNode(node,depth);
					}
				}, nodeAcceptor);

		return Arrays.asList(transitions.toArray(new Transition[0]));
	}

	public Marking getInitialMarking() {
		Marking initMarking = new Marking();

		for (Place p : net.getPlaces()) {
			if (net.getInEdges(p).isEmpty())
				initMarking.add(p);
		}

		return initMarking;
	}

	public Marking getFinalMarking() {
		Marking finalMarking = new Marking();

		for (Place p : net.getPlaces()) {
			if (net.getOutEdges(p).isEmpty())
				finalMarking.add(p);
			
			
		}

		return finalMarking;
	}

	public String getNetActivities() {
	
		return netActivities;
	}

}
