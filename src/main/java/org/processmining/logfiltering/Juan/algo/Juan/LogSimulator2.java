package org.processmining.logfiltering.Juan.algo.Juan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections4.MultiValuedMap;
import org.processmining.logfiltering.Juan.trie.Trie;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.petrinetsimulator.parameters.SimulationSettings;

public abstract class LogSimulator2 {

	protected Petrinet petriNet;
	protected Marking initialMarking;
	protected SimulationSettings settings;
	protected HashMap<String, String> ActivityCoder;
	protected HashMap<String, Double> probs;
	protected HashMap<String, Double> kPrefix;
	protected double uniqueness;
	protected int maxTraceLength;
	protected HashSet<String> Variants;
	protected	Trie trieDic ;
	protected HashSet<String> cVariants;
	protected HashMap<Transition, String> TauCoder;
	protected HashMap<String, Double> invisProb;
	protected MultiValuedMap<String, String> visSucc;
	protected int numOfTraces = 500;
	protected double diffThreshold = 0.001;
	protected double deltaRepRatio = 1.0;
	protected int maxRep = 2;
	protected int batchSize = 100;
	protected int timeOut = 30;
	protected TraceState traceState;
	protected String netActivites;

		
	protected void assignProbs(Set<Transition> enabledTransitions, ArrayList<TransObject> visi,
			ArrayList<TransObject> invisi) {

		TransObject candidate;		
		String currentPrefix = traceState.getPrevVisiblePrefix();
		int size = enabledTransitions.size();

		for (Transition et : enabledTransitions) {
			
			//double num = minProb *Math.random();
			
			if (et.isInvisible()) {					
				candidate = new TransObject(TauCoder.get(et), et, 0.0);
				invisi.add(candidate);
			} else {

				String codedLabel = ActivityCoder.get(et.getLabel());
				String prefix = currentPrefix + codedLabel;

				if(probs.containsKey(prefix))			
					candidate = new TransObject(codedLabel, et, probs.get(prefix));
				else if(kPrefix.containsKey(currentPrefix))
					candidate = new TransObject(codedLabel, et, 1.0/kPrefix.get(currentPrefix));
				else
					candidate = new TransObject(codedLabel, et, 1.0/netActivites.length());
				
				visi.add(candidate);
			}
		}


		//compute invisible trans prob (distribute according to # of invis trans)
		if (!invisi.isEmpty()) {

			for(TransObject to: invisi) {
				to.setProb(getVisibleSuccessorsProb(to.getCodedLabel(),size));
			}
			//invisi.forEach(x-> x.setProb(getVisibleSuccessorsProb(x.getCodedLabel(),size)));
		}
		

			
	}
	
	protected TransObject choseRandom(ArrayList<TransObject> trans) {

		//If there is just one object on the list, pick that one
		if(trans.size()==1)
			return trans.get(0);
		
		TransObject result = null;
		TreeMap<Double, TransObject> map = new TreeMap<>();

		//Total sum of probs of the enabled transitions
		double total = 0.0;

		for (int i = 0; i < trans.size(); i++)
			if(trans.get(i).getProb()>0.0)
				map.put(total += trans.get(i).getProb(), trans.get(i));
			
		if(map.isEmpty()) 
			return trans.get((int) Math.floor(Math.random() * trans.size()));
		
		// Generate a random value between 0 and 1
		double value = Math.random();
		
		//Scale random number according to total sum of probs
		value *= total;
		
		// Get the object that matches with the generated number
		result = map.ceilingEntry(value).getValue();

		return result;
	}

	protected double getVisibleSuccessorsProb(String codedLabel, int n) {
		
		double max = -1;
		String currentPrefix = traceState.getPrevVisiblePrefix();
		String invPref = currentPrefix + codedLabel;
		
		if(invisProb.containsKey(invPref))
			max = invisProb.get(invPref);
		else {
			if(!visSucc.get(codedLabel).isEmpty()) {
				for(String vs: visSucc.get(codedLabel)) {
					String prefix = currentPrefix + vs;
					if(probs.containsKey(prefix))
						max = Math.max(max, probs.get(prefix));
					else if(kPrefix.containsKey(currentPrefix))
						max = Math.max(max, 1.0/kPrefix.get(currentPrefix));
					else {
						max = Math.max(max, 1.0/netActivites.length());
						break;
					}
				}
				invisProb.put(invPref, max);
			}
			
		}
		
		if(max==-1)
			System.out.println("Neg");
	
		return max;
	}
	
	public HashSet<String> getVariants() {
		return Variants;
	}
	
	public Trie getDicPrefix() {
		return trieDic;
	}
	
	public HashSet<String> getcompressedVariants() {
		return cVariants;
	}
}
