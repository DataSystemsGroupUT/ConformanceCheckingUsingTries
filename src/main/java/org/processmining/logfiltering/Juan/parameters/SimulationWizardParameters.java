package org.processmining.logfiltering.Juan.parameters;


import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.processmining.basicutils.parameters.impl.PluginParametersImpl;
import org.processmining.logfiltering.Juan.enumtypes.SimulationType;
public class SimulationWizardParameters extends PluginParametersImpl {
	
	private int numOfTraces = 500;
	private XEventClassifier EventAttribute = new XEventNameClassifier();
	private SimulationType simulationType = SimulationType.CombinedLog;
	private int batchSize = 0;
	private double repRatioThreshold = 0.001;
	private int prefixSize = 2;
	private int timeOut = 30;

	public SimulationWizardParameters(int numOfTraces, int batchSize, int timeOut,int prefixSize, double repRatioThreshold,
			XEventClassifier eventattribute, SimulationType simulationType) {
		super();
		this.numOfTraces=numOfTraces;
		this.prefixSize=prefixSize;
		this.batchSize = batchSize;
		this.repRatioThreshold = repRatioThreshold;
		this.timeOut = timeOut;
		this.EventAttribute = eventattribute;
		this.simulationType = simulationType;
	}

	public void setSimulationType(SimulationType prototypeType) {
		this.simulationType = prototypeType;
	}	
	
	public XEventClassifier getEventAttribute() {
		return EventAttribute;
	}

	public void setEventAttribute(XEventClassifier eventAttribute) {
		EventAttribute = eventAttribute;
	}

	public int getPrefixSize() {
		return prefixSize;
	}

	public void setPrefixSize(int windowSize) {
		this.prefixSize = windowSize;
	}

	public SimulationType getSimulationType() {
		return simulationType;
	}

	public void setRepRatioThreshold(double repRatioThreshold) {
		this.repRatioThreshold = repRatioThreshold;
	}

	public int getNumOfTraces() {
		return numOfTraces;
	}

	public void setNumOfTraces(int numOfTraces) {
		this.numOfTraces = numOfTraces;
	}

	public double getRepRatioThreshold() {
		return repRatioThreshold;
	}

	public void setRepRatioThreshold(int repRatioThreshold) {
		this.repRatioThreshold = repRatioThreshold;
	}

	public XEventClassifier getEventClassifier() {
		return EventAttribute;
	}

	public void setEventClassifier(XEventClassifier eventAttribute) {
		EventAttribute = eventAttribute;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}


	public int getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

}
