package org.processmining.logfiltering.plugins;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import com.raffaeleconforti.noisefiltering.event.InfrequentBehaviourFilter;

@Plugin(name = "Call Conforti plugin", parameterLabels = { "Event Log" }, returnLabels = { "Filtered Event Log" }, returnTypes = { XLog.class })
public class CallConfortiPlugin {
	@UITopiaVariant(affiliation = "RWTH Aachen University", author = "Mohammadreza", email = "FaniSani@pads.rwth-aachen.de")
	@PluginVariant(requiredParameterLabels = { 0 })
	public XLog filterLogUsingConforti(UIPluginContext context, XLog rawLog) {
        XEventClassifier xEventClassifier = XLogInfoImpl.STANDARD_CLASSIFIER;
        /*
                
        XLog filteredLog = iffilter.filterLog(context, initialLog, nfr);*/
        
        /*InfrequentBehaviourFilter iffilter = new InfrequentBehaviourFilter(classifier);
                
        XLog filteredLog = iffilter.filterLog(initialLog);*/
        
        //InfrequentBehaviourFilterPlugin plugin = new InfrequentBehaviourFilterPluginLPSolve();
        
        /*AutomatonFactory automatonFactory = new AutomatonFactory(xEventClassifier);
        
        XLog log = rawLog;
        XFactory factory = new XFactoryNaiveImpl();
        LogOptimizer logOptimizer = new LogOptimizer(factory);
        log = logOptimizer.optimizeLog(log);

        XFactoryRegistry.instance().setCurrentDefault(factory);
        LogModifier logModifier = new LogModifier(factory, XConceptExtension.instance(), XTimeExtension.instance(), logOptimizer);
        logModifier.insertArtificialStartAndEndEvent(log);

        Automaton<String> automatonOriginal = automatonFactory.generate(log);

        InfrequentBehaviourFilter infrequentBehaviourFilter = new InfrequentBehaviourFilter(xEventClassifier, false, false, false, true, false, 0.125, 0.5, false, -1);
        double[] arcs = infrequentBehaviourFilter.discoverArcs(automatonOriginal, 1.0);
        
        System.out.println("arcs=");
        System.out.println(Arrays.toString(arcs));*/

        /*NoiseFilterUI noiseUI = new NoiseFilterUI();
        NoiseFilterResult result = noiseUI.showGUI(context, iffilter, arcs, automatonOriginal.getNodes());

        if (!result.isRemoveTraces() || result.isRemoveNodes()) {
            infrequentBehaviourFilter = new InfrequentBehaviourFilter(xEventClassifier, false, false, false, result.isRemoveTraces(), result.isRemoveNodes(), 0.125, 0.5, false, -1);
        }*/

        //return infrequentBehaviourFilter.filterLog(context, rawLog, result);
        
        //NoiseFilterResult result = new NoiseFilterResult();
        //result.setNoiseLevel(0.05);
        
        //InfrequentBehaviourFilter iffilter = new InfrequentBehaviourFilter(xEventClassifier, false, false, false, false, false, 0.125, 0.5, false, -1);
                
        //return iffilter.filterLog(context, log, result);
        
        InfrequentBehaviourFilter filter = new InfrequentBehaviourFilter(xEventClassifier, false, true, true, true, false, 0.125, 0.9, false, -1);
        
        XLog filterLog =  filter.filterLog(rawLog);
        filterLog.size();
        return filterLog;
	}
}