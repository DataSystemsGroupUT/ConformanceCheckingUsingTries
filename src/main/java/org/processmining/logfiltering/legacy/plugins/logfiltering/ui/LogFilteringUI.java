package org.processmining.logfiltering.legacy.plugins.logfiltering.ui;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.logfiltering.legacy.plugins.logfiltering.LogFilteringInput;
import org.processmining.logfiltering.legacy.plugins.logfiltering.LogProperties;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.Combination;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.FilterLevel;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.FilterType;
import org.processmining.logfiltering.legacy.plugins.logfiltering.listener.LogFilteringSettingsListener;

public class LogFilteringUI  implements LogFilteringSettingsListener{
	UIPluginContext context;
	int noSteps;
	int currentStep;
	
	int attributeThresholdConfiguationStep; 	
	myStep[] mySteps;
	
	LogFilteringInput input;
	
	public LogFilteringUI(UIPluginContext context, XLog log){
		this.context = context;
		LogProperties logProperties = new LogProperties(log);
		
		InteractionResult result = InteractionResult.NEXT;
		
		input = new LogFilteringInput();
		input.setLog(log);
		input.setLogProperties(logProperties);
		
		attributeThresholdConfiguationStep = noSteps++;
		
		mySteps = new myStep[noSteps];
		
		mySteps[attributeThresholdConfiguationStep] = new AttributeThresholdConfigurationStep(logProperties);
		mySteps[attributeThresholdConfiguationStep].setListener(this);
		

		while (true) {
			if (currentStep < 0) {
				currentStep = 0;
			}
			if (currentStep >= noSteps) {
				currentStep = noSteps - 1;
			}
			result = context.showWizard("Log Filtering Plugin", currentStep == 0, currentStep == noSteps - 1, mySteps[currentStep]);
			
			switch (result) {

				case NEXT :
					go(1);
					break;
				case PREV :
					go(-1);
					break;
				case FINISHED :
					readSettings();
					return;
				case CANCEL :
					return;
				default :
					break;
			}
		}
	}
	
	private int go(int direction) {
		currentStep += direction;
		if (currentStep >= 0 && currentStep < noSteps) {
			if (mySteps[currentStep].precondition()) {
				return currentStep;
			} else {
				return go(direction);
			}
		}
		return currentStep;
	}
	
	private void readSettings(){
		for(int currentStep = 0; currentStep < noSteps; currentStep++){
			System.out.println("Reading Settings for Step: "+currentStep);
			context.log("Reading Settings for Step: "+currentStep);
			mySteps[currentStep].readSettings();
		}
	}
	
	public void setFilterLevel(FilterLevel filterLevel) {
		input.setFilterLevel(filterLevel);
	}

	public void setAttributeFilterTypeMap(
			Map<String, FilterType> attributeFilterTypeMap) {
		input.setAttributeFilterTypeMap(attributeFilterTypeMap);
		
	}

	public void setAttributeSelectedValueSetMap(
			Map<String, Set<String>> attributeValueSetMap) {
		input.setAttributeValueSetMap(attributeValueSetMap);
	}

	public void setAttributeStartDateThresholdMap(
			Map<String, Date> attributeDateUpperThresholdMap) {
		input.setAttributeDateLowerThresholdMap(attributeDateUpperThresholdMap);
	}

	public void setAttributeEndDateThresholdMap(
			Map<String, Date> attributeDateLowerThresholdMap) {
		input.setAttributeDateUpperThresholdMap(attributeDateLowerThresholdMap);
	}

	public void setAttributeMaxValueThresholdMap(
			Map<String, Double> attributeMaxValueThresholdMap) {
		input.setAttributeMaxValueThresholdMap(attributeMaxValueThresholdMap);
	}

	public void setAttributeMinValueThresholdMap(
			Map<String, Double> attributeMinValueThresholdMap) {
		input.setAttributeMinValueThresholdMap(attributeMinValueThresholdMap);
	}

	public void setCombination(Combination combination) {
		input.setCombination(combination);
	}
	
	public LogFilteringInput getInput(){
		return input;
	}
}
