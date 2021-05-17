package org.processmining.logfiltering.algorithms.ICC;

import org.deckfour.xes.model.XTrace;



public interface IncrementalReplayer {
		
	public boolean TraceVariantKnown(XTrace trace);
		
	public boolean abstractAndCheckPredicate(XTrace trace, Object[] additionalInformation);
	
	public boolean incrementAndCheckPredicate(XTrace trace);
	
	public ReplayResultsContainer getResult();
}
