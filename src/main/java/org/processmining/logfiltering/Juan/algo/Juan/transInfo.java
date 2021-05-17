package org.processmining.logfiltering.Juan.algo.Juan;

import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class transInfo implements Comparable<transInfo> {

	private Transition trans;
	private double prob;
	private String codedLabel;
	private String pref = "";
	
	public transInfo(Transition trans, double prob) {
		this.trans = trans;
		this.prob = prob;
	}

	public transInfo(String label, double prob) {
		this.codedLabel = label;
		this.prob = prob;
	}

	public transInfo(String label, Transition trans, double prob) {
		this.codedLabel = label;
		this.trans = trans;
		this.prob = prob;
	}

	public String getCodedLabel() {
		return codedLabel;
	}

	public void setCodedLabel(String codedLabel) {
		this.codedLabel = codedLabel;
	}

	public String getPref() {
		return pref;
	}

	public void setPref(String pref) {
		this.pref = pref;
	}

	public Transition getTrans() {
		return trans;
	}

	public void setTrans(Transition trans) {
		this.trans = trans;
	}

	public double getProb() {
		return prob;
	}

	public void setProb(double prob) {
		this.prob = prob;
	}

	public int compareTo(transInfo tr) {
		return (this.getProb() < tr.getProb() ? -1 : (this.getProb() == tr.getProb() ? 0 : 1));
	}

}