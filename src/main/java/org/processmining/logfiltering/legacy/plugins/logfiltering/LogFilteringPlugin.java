//Om Ganesayanamaha
package org.processmining.logfiltering.legacy.plugins.logfiltering;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logfiltering.legacy.plugins.logfiltering.filter.FilterLog;
import org.processmining.logfiltering.legacy.plugins.logfiltering.ui.LogFilteringUI;

@Plugin(name = "Log Filtering", parameterLabels = {"Event Log"}, returnLabels = { "Filtered Log" }, returnTypes = {XLog.class }, userAccessible = true, help = "Log Filtering Plug-in")
public class LogFilteringPlugin {
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "R.P. Jagadeesh Chandra 'JC' Bose", email = "j.c.b.rantham.prabhakara@tue.nl", website = "www.processmining.org")
	@PluginVariant(variantLabel = "Default Summary, user-specified clusters.", requiredParameterLabels = { 0 })
	
	public static XLog analyze(UIPluginContext context, XLog log){
		LogFilteringUI logFilteringUI = new LogFilteringUI(context, log);
		LogFilteringInput input = logFilteringUI.getInput();
		FilterLog filterLog = new FilterLog(input);
		return filterLog.getFilteredLog();
	}
}
