package org.processmining.logfiltering.Juan.algo.Juan;

import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class TransObject implements Comparable<TransObject> {

	private Transition trans = null;
	private String codedLabel = "";
	private String prefix = "";
	private double prob = 0.0;

	public TransObject(String label, Transition trans, double prob) {
		this.codedLabel = label;
		this.trans = trans;
		this.prob = prob;
	}

	public Transition getTrans() {
		return trans;
	}

	public String getCodedLabel() {
		return codedLabel;
	}

	public void setCodedLabel(String codedLabel) {
		this.codedLabel = codedLabel;
	}
	
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	public double getProb() {
		return prob;
	}
	
	public void setProb(double prob) {
		this.prob = prob;
	}

	public int compareTo(TransObject tr) {
		return (this.getProb() < tr.getProb() ? -1 : (this.getProb() == tr.getProb() ? 0 : 1));
	}

}