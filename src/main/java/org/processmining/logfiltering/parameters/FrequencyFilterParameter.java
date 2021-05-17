package org.processmining.logfiltering.parameters;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.processmining.basicutils.parameters.impl.PluginParametersImpl;
public class FrequencyFilterParameter extends PluginParametersImpl {
	
	private double probabilityOfRemoval = 0.1;
	private int AbsteractionLength = 2;
	private FilterLevel FilteringMethod = FilterLevel.TRACE;
	private AbsteractionType AbstractionUsed = AbsteractionType.SEQUENCE;
	private XEventClassifier EventAttribute = new XEventNameClassifier();
	private FilterSelection FilteringSelection = FilterSelection.REMOVE;
	private AdjustingType AdjustingThresholdMethod= AdjustingType.None;
	private ProbabilityType ProbabilitycomutingMethod= ProbabilityType.DIRECT;
	public FrequencyFilterParameter(){
		super();
		
	}

	public FrequencyFilterParameter(double Thresholdvlue, AdjustingType none, int absteractionLength, AbsteractionType absteraction,
			FilterSelection selectRemove, ProbabilityType direct, XEventClassifier classifier, FilterLevel trace) {
		super();
		this.probabilityOfRemoval=Thresholdvlue;
		this.AdjustingThresholdMethod=none;
		this.AbsteractionLength=absteractionLength;
		this.AbstractionUsed=absteraction;
		this.FilteringSelection=selectRemove;
		this.ProbabilitycomutingMethod=direct;
		this.EventAttribute=classifier;
		this. FilteringMethod=trace;
		
	}

	public double getProbabilityOfRemoval() {
		return probabilityOfRemoval;
	}

	public void setProbabilityOfRemoval(double probabilityOfRemoval) {
		this.probabilityOfRemoval = probabilityOfRemoval;
	}

	public int getAbsteractionLength() {
		return AbsteractionLength;
	}

	public void setAbsteractionLength(int absteractionLength) {
		AbsteractionLength = absteractionLength;
	}

	public FilterLevel getFilteringMethod() {
		return FilteringMethod;
	}

	public void setFilteringMethod(FilterLevel filteringMethod) {
		FilteringMethod = filteringMethod;
	}

	public AbsteractionType getAbstractionUsed() {
		return AbstractionUsed;
	}

	public void setAbstractionUsed(AbsteractionType abstractionUsed) {
		AbstractionUsed = abstractionUsed;
	}

	public XEventClassifier getEventAttribute() {
		return EventAttribute;
	}

	public void setEventAttribute(XEventClassifier eventAttribute) {
		EventAttribute = eventAttribute;
	}

	public FilterSelection getFilteringSelection() {
		return FilteringSelection;
	}

	public void setFilteringSelection(FilterSelection filteringSelection) {
		FilteringSelection = filteringSelection;
	}

	public AdjustingType getAdjustingThresholdMethod() {
		return AdjustingThresholdMethod;
	}

	public void setAdjustingThresholdMethod(AdjustingType adjustingThresholdMethod) {
		AdjustingThresholdMethod = adjustingThresholdMethod;
	}

	public ProbabilityType getProbabilitycomutingMethod() {
		return ProbabilitycomutingMethod;
	}

	public void setProbabilitycomutingMethod(ProbabilityType probabilitycomutingMethod) {
		ProbabilitycomutingMethod = probabilitycomutingMethod;
	}
}