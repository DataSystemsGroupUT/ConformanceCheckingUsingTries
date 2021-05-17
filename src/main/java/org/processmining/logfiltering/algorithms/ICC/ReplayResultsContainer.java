package org.processmining.logfiltering.algorithms.ICC;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.deckfour.xes.model.XTrace;

import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;

//TODO change to Multibag?
public class ReplayResultsContainer{

	Map<String, TraceReplayResult> alignmentResults;
	private Double petriNetShortestPath;
	Double logFitness;
	
	public ReplayResultsContainer() {
		alignmentResults =new TreeMap<String, TraceReplayResult>();
	}
	
	public void setShortestPathLength(double length) {
		this.setPetriNetShortestPath(length);
	}
	
	public int getTraceVariantCount() {
		return alignmentResults.size();
	}
	
	public String convertToString(XTrace trace) {
		String toReturn="";
		
		if (trace.get(0).getAttributes().toString().contains("lifecycle:transition")) {
			for(int i=0;i<trace.size();i++) {
				toReturn=toReturn+trace.get(i).getAttributes().get("concept:name").toString()+"+"+trace.get(i).getAttributes().get("lifecycle:transition").toString() +">";
			}
		}else {
			for(int i=0;i<trace.size();i++) {
			toReturn=toReturn+trace.get(i).getAttributes().get("concept:name").toString()+"+complete>";
			}
		}
		
		return toReturn.substring(0, toReturn.length()-1);
	}
	
	public boolean contains(String traceActivities) {
		if (alignmentResults.containsKey(traceActivities)){
			return true;
		}
		return false;
		}
	
	public void put(String key, TraceReplayResult value) {
		alignmentResults.put(key, value);
	}

	public void incrementMultiplicity(String key) {
		alignmentResults.get(key).multiplicity++;
	}
	
	public TraceReplayResult get(String trace) {
		return alignmentResults.get(trace);
	}
	
	public Collection<TraceReplayResult> values() {
		return alignmentResults.values();
	}
	
	public Set keys() {
		return alignmentResults.keySet();
	}
	
	//TODO Update fitness upon insertion, make it synchronous to how asynchmoves are accessed
	public double getFitness() {
		double totalTraces=0;
		double fitness=0;
		for (TraceReplayResult traceVariant : this.alignmentResults.values()) {
			totalTraces+=traceVariant.multiplicity;	
			fitness+=traceVariant.fitness*traceVariant.multiplicity;
		}
		if(totalTraces==0) {
			return 0;
		}
		return fitness/totalTraces;
	}
	
	public Multiset<String> getAsynchMoves(){
		Multiset<String> toReturn = TreeMultiset.create();
		for (TraceReplayResult result : this.alignmentResults.values()) {
			toReturn.addAll(result.getAsynchMoves());
		}
		return toReturn;
	}
	

	public String toString() {
		String toReturn="";
		for(String key : alignmentResults.keySet()) {
			toReturn=toReturn+alignmentResults.get(key).toString()+"\n";
		}
		return toReturn;
	}

	public Double getPetriNetShortestPath() {
		return petriNetShortestPath;
	}

	public void setPetriNetShortestPath(Double petriNetShortestPath) {
		this.petriNetShortestPath = petriNetShortestPath;
	}
}
