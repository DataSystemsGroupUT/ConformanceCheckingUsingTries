package org.processmining.logfiltering.parameters; 


import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.processmining.basicutils.parameters.impl.PluginParametersImpl;
public class SequenceFilterParameter extends PluginParametersImpl {
	
	private double HighSupportPattern = 0.85;
	private int OddDistance=2; 
	private double ConfHighConfRules= 0.95;
	private double SuppHighConfRules=0.3;
	private double SuppOrdinaryRules=0.001;
	private double ConfOridnaryRules=0.0;
	private XEventClassifier EventAttribute = new XEventNameClassifier();
	
	
	public SequenceFilterParameter() {
		super();
	}

	public SequenceFilterParameter(double highSupportPattern, XEventClassifier eventattribute) {
		super();
		this.HighSupportPattern = highSupportPattern;
		
		this.EventAttribute = eventattribute;
	}
	public SequenceFilterParameter(double highSupportPattern, double confHighConfRules, double suppHighConfRules, 
			double suppOrdinaryRules, double confOridnaryRules,int oddDistance,XEventClassifier eventattribute) {
		super();
		this.HighSupportPattern = highSupportPattern;
		
		this.EventAttribute = eventattribute;
		this.ConfHighConfRules= confHighConfRules;
		this.SuppHighConfRules = suppHighConfRules;
		this.SuppOrdinaryRules= suppOrdinaryRules;
		this.ConfOridnaryRules= confOridnaryRules;
		this.OddDistance=oddDistance;
	}
	public int getOddDistance() {
		return OddDistance;
	}

	public void setOddDistance(int oddDistance) {
		OddDistance = oddDistance;
	}

	public double getConfOridnaryRules() {
		return ConfOridnaryRules;
	}

	public void setConfOridnaryRules(double confOridnaryRules) {
		ConfOridnaryRules = confOridnaryRules;
	}

	public double getSuppOrdinaryRules() {
		return SuppOrdinaryRules;
	}

	public void setSuppOrdinaryRules(double suppOrdinaryRules) {
		SuppOrdinaryRules = suppOrdinaryRules;
	}

	public SequenceFilterParameter( XEventClassifier eventattribute) {
		super();
		this.EventAttribute = eventattribute;
	}

	public XEventClassifier getEventClassifier() {
		return EventAttribute;
	}

	public void setEventClassifier(XEventClassifier eventAttribute) {
		EventAttribute = eventAttribute;
	}

	public double getHighSupportPattern() {
		return HighSupportPattern;
	}

	public void setHighSupportPattern(double highSupportPattern) {
		HighSupportPattern = highSupportPattern;
	}

	public double getConfHighConfRules() {
		return ConfHighConfRules;
	}

	public void setConfHighConfRules(double confHighConfRules) {
		ConfHighConfRules = confHighConfRules;
	}

	public double getSuppHighConfRules() {
		return SuppHighConfRules;
	}

	public void setSuppHighConfRules(double suppHighConfRules) {
		SuppHighConfRules = suppHighConfRules;
	}

	
	


	

	

}
