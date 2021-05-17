package org.processmining.logfiltering.parameters;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.processmining.basicutils.parameters.impl.PluginParametersImpl;

public class OddRulesParameters extends PluginParametersImpl {
	private double HighSupportPattern = 0.85;
	private int OddDistance=3; 
	private double ConfHighConfRules= 0.9;
	private XEventClassifier EventAttribute = new XEventNameClassifier();
	
	public OddRulesParameters() {
		super();
	}
	
	public OddRulesParameters(double highSupportPattern, double confHighConfRules,XEventClassifier eventattribute,int oddDistance) {
		super();
		this.HighSupportPattern = highSupportPattern;
		
		this.EventAttribute = eventattribute;
		this.ConfHighConfRules= confHighConfRules;
		this.OddDistance=oddDistance;
	}
	


	public double getHighSupportPattern() {
		return HighSupportPattern;
	}

	public void setHighSupportPattern(double highSupportPattern) {
		HighSupportPattern = highSupportPattern;
	}

	public int getOddDistance() {
		return OddDistance;
	}

	public void setOddDistance(int oddDistance) {
		OddDistance = oddDistance;
	}

	public double getConfHighConfRules() {
		return ConfHighConfRules;
	}

	public void setConfHighConfRules(double confHighConfRules) {
		ConfHighConfRules = confHighConfRules;
	}

	public XEventClassifier getEventClassifier() {
		return EventAttribute;
	}

	public void setEventClassifier(XEventClassifier eventAttribute) {
		EventAttribute = eventAttribute;
	}
	

}
