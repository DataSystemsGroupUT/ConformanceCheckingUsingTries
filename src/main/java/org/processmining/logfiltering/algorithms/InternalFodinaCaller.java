package org.processmining.logfiltering.algorithms;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.bpmnminer.causalnet.CausalNet;
import org.processmining.plugins.bpmnminer.converter.CausalNetToPetrinet;
import org.processmining.plugins.bpmnminer.plugins.FodinaMinerPlugin;
import org.processmining.plugins.bpmnminer.types.MinerSettings;

@Plugin(name = "Calling Fodina", 
parameterLabels = { "Log" }, 
returnLabels = {"Petri net", "Marking" }, 
returnTypes = { Petrinet.class, Marking.class }, 
userAccessible = true, 
help = "CUSTOM get Petri net from log using Fodina")
public class InternalFodinaCaller {
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, 
			author = "Mohammadreza", // the author of the main plugin is Sepppe K.L.M. vanden Broucke
			email = "fanisani@pads.rwth-aachen.de")
	@PluginVariant(variantLabel = "Default Settings", requiredParameterLabels = { 0 })
	public Object[] getFodinaPetriNet(PluginContext context, XLog log) {
		MinerSettings settings = new MinerSettings();
		double nom = log.size() / ((double)log.size() + (double)settings.dependencyDivisor);
		if (nom <= 0D) nom = 0D;
		if (nom >= 0.9D) nom = 0.9D;
		settings.dependencyThreshold = nom;
		settings.l1lThreshold = nom;
		settings.l2lThreshold = nom;
		settings.suppressFitnessReport = false;
		
		FodinaMinerPlugin plugin = new FodinaMinerPlugin();
		
		Object[] netPlusSettings = plugin.runMiner(context, log, settings);
		
		CausalNet net = (CausalNet) netPlusSettings[0];
		
		CausalNetToPetrinet converter = new CausalNetToPetrinet();
		
		Object[] petriNetPlusMarking = converter.convert(context, net);
		
		return petriNetPlusMarking;
	}
}
