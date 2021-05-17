package org.processmining.logfiltering.Juan.algo.Juan;

import java.util.HashMap;
import java.util.HashSet;

import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class TraceState {

	private String trace = "";
	private String codedTrace = "";
	private String codedTrieTrace = "";
	private String prevVisiblePrefix = "";
	private Transition prevTrans = null;
	private Transition prevVisibleTrans = null;
	private boolean modelHasLoops = false;
	private boolean repeatedActivity = false;

	private HashMap<String, Integer> firedTransCount = new HashMap<String, Integer>();
	private HashSet<String> firedTransitions = new HashSet<String>();
	private int prefixSize;
	
	public TraceState() {
		resetTraceState();
		modelHasLoops(false);
	}
	
	public int getPrefixSize() {
		return prefixSize;
	}

	public void setPrefixSize(int prefixSize) {
		this.prefixSize = prefixSize;
	}

	public String getTrace() {
		return trace;
	}

	public void setTrace(String trace) {
		this.trace = trace;
	}
	
	public String getCodedTrace() {
		return codedTrace;
	}

	public void setCodedTrace(String codedTrace) {
		this.codedTrace = codedTrace;
	}

	public String getCodedTrieTrace() {
		return codedTrieTrace;
	}

	public void setCodedTrieTrace(String codedTrace) {
		codedTrieTrace = codedTrace;
	}
	
	public String getPrevVisiblePrefix() {
		return prevVisiblePrefix;
	}

	public void setPrevVisiblePrefix(String prevVisibleFired) {
		prevVisiblePrefix = prevVisibleFired;
	}

	public Transition getPrevTrans() {
		return prevTrans;
	}

	public void setPrevTrans(Transition prevTrans) {
		this.prevTrans = prevTrans;
	}

	public Transition getPrevVisibleTrans() {
		return prevVisibleTrans;
	}

	public void setPrevVisibleTrans(Transition prevVisibleTrans) {
		this.prevVisibleTrans = prevVisibleTrans;
	}
	
	public void modelHasLoops(boolean val) {
		modelHasLoops = val;
	}
	
	public boolean modelHasLoops() {
		return modelHasLoops;
	}

	public boolean anyRepeatedActivity() {
		return repeatedActivity;
	}

	public HashSet<String> getFiredTransitions() {
		return firedTransitions;
	}

	public void updateTraceState(TransObject c) {

		String activity = c.getTrans().getLabel(); 
		String codedActivity = c.getCodedLabel(); 
		Transition trans = c.getTrans();
		
		if (trans.isInvisible())
			activity = "";
		else {
			
			prevVisibleTrans = trans;
			codedTrace += codedActivity;
			
			if(codedTrace.length()<=prefixSize)
				prevVisiblePrefix = codedTrace;
			else
				prevVisiblePrefix = codedTrace.substring(codedTrace.length()-prefixSize);
			
			
		}
		
		
		trace += activity;
		codedTrieTrace += codedActivity;
		prevTrans = trans;
		firedTransitions.add(codedActivity);
		
					
		if (firedTransCount.get(codedActivity) == null)
			firedTransCount.put(codedActivity, 1);
		else
			firedTransCount.put(codedActivity, firedTransCount.get(codedActivity) + 1);

		if(!modelHasLoops)
			modelHasLoops = getTransCount(codedActivity)>1;
		
		if(!repeatedActivity)
			repeatedActivity = getTransCount(codedActivity)>1;
		
	}

	public void resetTraceState() {
		trace = "";
		codedTrace="";
		codedTrieTrace = "";
		prevVisiblePrefix = "";
		prevTrans = null;
		prevVisibleTrans = null;
		firedTransCount.clear();
		firedTransitions.clear();
		repeatedActivity = false;

	}

	public int getTransCount(String t) {
		if (firedTransCount.containsKey(t))
			return firedTransCount.get(t);
		else
			return 0;
	}
	
	public void resetTransCount(String t) {
		
		if(firedTransCount.containsKey(t))
			firedTransCount.put(t, 0);
		
	}

}
