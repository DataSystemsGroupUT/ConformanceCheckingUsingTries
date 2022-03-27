package org.processmining.logfiltering.algorithms.ICC;

import org.deckfour.xes.model.XTrace;


public interface IncrementalReplayer {

    boolean TraceVariantKnown(XTrace trace);

    boolean abstractAndCheckPredicate(XTrace trace, Object[] additionalInformation);

    boolean incrementAndCheckPredicate(XTrace trace);

    ReplayResultsContainer getResult();
}
