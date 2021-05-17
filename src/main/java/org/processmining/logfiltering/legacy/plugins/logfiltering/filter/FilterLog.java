package org.processmining.logfiltering.legacy.plugins.logfiltering.filter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XsDateTimeFormat;
import org.processmining.logfiltering.legacy.plugins.logfiltering.LogFilteringInput;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.Combination;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.FilterLevel;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.FilterType;

public class FilterLog {
	LogFilteringInput input;
	XLog filteredLog;
	
	public FilterLog(LogFilteringInput input){
		this.input = input;
		
		printInputConfiguration();
		if(input.getFilterLevel() == FilterLevel.Trace){
			filteredLog = filterTraces();
		}else if(input.getFilterLevel() == FilterLevel.Event){
			filteredLog = filterEvents();
		}
	}
	
	private XLog filterTraces(){
		Map<String, FilterType> attributeFilterTypeMap = input.getAttributeFilterTypeMap();
		
		XAttributeMap traceAttributeMap;
		XLog log = input.getLog();
		Map<String, String> attributeTypeMap = input.getLogProperties().getTraceAttributeTypeMap();
		Combination combination = input.getCombination();
		boolean allSatisfied = true;
		boolean atleastOneSatisfied = false;
		
		Map<String, Boolean> attributeSatisifiedMap = new HashMap<String, Boolean>();
		boolean removeTrace;
		
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLog outputLog = factory.createLog();
		for(XExtension extension : log.getExtensions())
			outputLog.getExtensions().add(extension);
		
		outputLog.setAttributes(log.getAttributes());
		
		for(XTrace trace : log){
			traceAttributeMap = trace.getAttributes();
			attributeSatisifiedMap.clear();
			removeTrace = false;
			
			if(attributeFilterTypeMap.containsKey("Trace Length")){
				int minTraceLengthThreshold = new Double(input.getAttributeMinValueThresholdMap().get("Trace Length")).intValue();
				int maxTraceLengthThreshold = new Double(input.getAttributeMaxValueThresholdMap().get("Trace Length")).intValue();
				
				if(trace.size() >= minTraceLengthThreshold && trace.size() <= maxTraceLengthThreshold)
					attributeSatisifiedMap.put("Trace Length", true);
				else
					attributeSatisifiedMap.put("Trace Length", false);
			}
			if(attributeFilterTypeMap.containsKey("Trace Arrival Date")){
				Date traceStartDateThreshold = input.getAttributeDateLowerThresholdMap().get("Trace Arrival Date");
				Date traceEndDateThreshold = input.getAttributeDateUpperThresholdMap().get("Trace Arrival Date");
				
				Date traceDate = XTimeExtension.instance().extractTimestamp(trace.get(0));
				if((traceDate.after(traceStartDateThreshold)||traceDate.equals(traceStartDateThreshold)) && (traceDate.before(traceEndDateThreshold)||traceDate.equals(traceEndDateThreshold)))
					attributeSatisifiedMap.put("Trace Arrival Date", true);
				else
					attributeSatisifiedMap.put("Trace Arrival Date", false);
			}
			
			for(String attribute : traceAttributeMap.keySet()){
				if(attributeFilterTypeMap.containsKey(attribute) && attributeTypeMap.containsKey(attribute) && attributeTypeMap.get(attribute).equals("string")){
					Set<String> attributeSelectedValueSet = input.getAttributeValueSetMap().get(attribute);
					if(attributeSelectedValueSet.contains(traceAttributeMap.get(attribute).toString())){
						attributeSatisifiedMap.put(attribute, true);
					}else{
						attributeSatisifiedMap.put(attribute, false);
					}
				}else if(attributeFilterTypeMap.containsKey(attribute) && attributeTypeMap.containsKey(attribute) && attributeTypeMap.get(attribute).equals("continuous")){
					double minValue = input.getAttributeMinValueThresholdMap().get(attribute);
					double maxValue = input.getAttributeMaxValueThresholdMap().get(attribute);
					
					if(new Double(traceAttributeMap.get(attribute).toString()).doubleValue() >= minValue && new Double(traceAttributeMap.get(attribute).toString()).doubleValue() <= maxValue){
						attributeSatisifiedMap.put(attribute, true);
					}else{
						attributeSatisifiedMap.put(attribute, false);
					}
				}else if(attributeFilterTypeMap.containsKey(attribute) && attributeTypeMap.containsKey(attribute) && attributeTypeMap.get(attribute).equals("date")){
					try{
						Date startTimeStamp = input.getAttributeDateLowerThresholdMap().get(attribute);
						Date endTimeStamp = input.getAttributeDateUpperThresholdMap().get(attribute);
						
						Date attributeTimeStamp = new XsDateTimeFormat().parseObject(trace.getAttributes().get(attribute).toString());
						if((attributeTimeStamp.equals(startTimeStamp)||attributeTimeStamp.after(startTimeStamp)) && (attributeTimeStamp.equals(endTimeStamp)||attributeTimeStamp.before(endTimeStamp))){
							attributeSatisifiedMap.put(attribute, true);
						}else{
							attributeSatisifiedMap.put(attribute, false);
						}
					}catch (java.text.ParseException e) {
						e.printStackTrace();
					}
				}
			}
			
			removeTrace = false;
			allSatisfied = true;
			atleastOneSatisfied = false;
			for(String attribute : attributeSatisifiedMap.keySet()){
				if(attributeFilterTypeMap.get(attribute) == FilterType.Remove && attributeSatisifiedMap.get(attribute)){
					removeTrace = true;
					break;
				}
				if(attributeFilterTypeMap.get(attribute) == FilterType.Retain && !attributeSatisifiedMap.get(attribute)){
					allSatisfied = false;
				}else if(attributeSatisifiedMap.get(attribute)){
					atleastOneSatisfied = true;
				}
			}
			
			if(removeTrace)
				continue;
			if(allSatisfied && combination == Combination.Conjunction){
				outputLog.add(trace);
			}else if(atleastOneSatisfied && combination == Combination.Disjunction){
				outputLog.add(trace);
			}
		}
		System.out.println("No. (Filtered) Traces: "+outputLog.size());
		return outputLog;
	}

	private XLog filterEvents(){
		Map<String, FilterType> attributeFilterTypeMap = input.getAttributeFilterTypeMap();
		
		XAttributeMap eventAttributeMap;
		XLog log = input.getLog();
		Map<String, String> attributeTypeMap = input.getLogProperties().getEventAttributeTypeMap();
		
		Combination combination = input.getCombination();
		boolean allSatisfied = true;
		boolean atleastOneSatisfied = false;
		
		Map<String, Boolean> attributeSatisifiedMap = new HashMap<String, Boolean>();
		boolean removeEvent;
		
		
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLog outputLog = factory.createLog();
		for(XExtension extension : log.getExtensions())
			outputLog.getExtensions().add(extension);
		
		outputLog.setAttributes(log.getAttributes());
		
		XTrace outputTrace;
		
		for(XTrace trace : log){
			outputTrace = factory.createTrace();
			outputTrace.setAttributes((XAttributeMap)trace.getAttributes().clone());
			for(XEvent event : trace){
				eventAttributeMap = event.getAttributes();
				attributeSatisifiedMap.clear();
				
				for(String attribute : eventAttributeMap.keySet()){
					if(attributeFilterTypeMap.containsKey(attribute) && attributeTypeMap.containsKey(attribute) && attributeTypeMap.get(attribute).equals("string")){
						Set<String> attributeSelectedValueSet = input.getAttributeValueSetMap().get(attribute);
						if(attributeSelectedValueSet.contains(eventAttributeMap.get(attribute).toString())){
							attributeSatisifiedMap.put(attribute, true);
						}else{
							attributeSatisifiedMap.put(attribute, false);
						}
					}else if(attributeFilterTypeMap.containsKey(attribute) && attributeTypeMap.containsKey(attribute) && attributeTypeMap.get(attribute).equals("continuous")){
						double minValue = input.getAttributeMinValueThresholdMap().get(attribute);
						double maxValue = input.getAttributeMaxValueThresholdMap().get(attribute);
						
						if(new Double(eventAttributeMap.get(attribute).toString()).doubleValue() >= minValue && new Double(eventAttributeMap.get(attribute).toString()).doubleValue() <= maxValue){
							attributeSatisifiedMap.put(attribute, true);
						}else{
							attributeSatisifiedMap.put(attribute, false);
						}
					}else if(attributeFilterTypeMap.containsKey(attribute) && attributeTypeMap.containsKey(attribute) && attributeTypeMap.get(attribute).equals("date")){
//						System.out.println("date");
						try{
							Date startTimeStamp = input.getAttributeDateLowerThresholdMap().get(attribute);
							Date endTimeStamp = input.getAttributeDateUpperThresholdMap().get(attribute);
							
							Date attributeTimeStamp = new XsDateTimeFormat().parseObject(eventAttributeMap.get(attribute).toString());
							if((attributeTimeStamp.equals(startTimeStamp)||attributeTimeStamp.after(startTimeStamp)) && (attributeTimeStamp.equals(endTimeStamp)||attributeTimeStamp.before(endTimeStamp))){
//								System.out.println("event time stamp: "+attributeTimeStamp+" @ "+true);
								attributeSatisifiedMap.put(attribute, true);
							}else{
//								System.out.println("event time stamp: "+attributeTimeStamp+" @ "+false);
								attributeSatisifiedMap.put(attribute, false);
							}
						}catch (java.text.ParseException e) {
							e.printStackTrace();
						}
					}
				}
				
				removeEvent = false;
				allSatisfied = true;
				atleastOneSatisfied = false;
				for(String attribute : attributeSatisifiedMap.keySet()){
					if(attributeFilterTypeMap.get(attribute) == FilterType.Remove && attributeSatisifiedMap.get(attribute)){
						removeEvent = true;
						break;
					}
					if(attributeFilterTypeMap.get(attribute) == FilterType.Retain && !attributeSatisifiedMap.get(attribute)){
						allSatisfied = false;
					}else if(attributeSatisifiedMap.get(attribute)){
						atleastOneSatisfied = true;
					}
				}
				
				if(removeEvent)
					continue;
				if(allSatisfied && combination == Combination.Conjunction){
//					System.out.println("all satisfied-adding event");
					outputTrace.add(event);
				}else if(atleastOneSatisfied && combination == Combination.Disjunction){
//					System.out.println("at least one satisfied-adding event");
					outputTrace.add(event);
				}
			}
			if(outputTrace.size() > 0)
				outputLog.add(outputTrace);
		}
		
		return outputLog;
		
	}
	
	private void printInputConfiguration(){
		System.out.println("Filter Level: "+input.getFilterLevel());
		System.out.println("Combination: "+input.getCombination());
		
		System.out.println("Selected Attributes for Filtering");
		for(String attribute : input.getAttributeFilterTypeMap().keySet())
			System.out.println(attribute+" @ "+input.getAttributeFilterTypeMap().get(attribute));
		
		if(input.getAttributeValueSetMap().size() > 0){
			System.out.println("Discrete Attribute Set Configurations");
			for(String attribute : input.getAttributeValueSetMap().keySet()){
				System.out.println(attribute+" @ "+input.getAttributeValueSetMap().get(attribute));
			}
		}
		
		if(input.getAttributeMinValueThresholdMap().size() > 0){
			System.out.println("Continuous Attribute Set Configurations");
			for(String attribute : input.getAttributeMinValueThresholdMap().keySet()){
				System.out.println(attribute+" @ "+input.getAttributeMinValueThresholdMap().get(attribute)+" @ "+input.getAttributeMaxValueThresholdMap().get(attribute));
			}
		}
		
		if(input.getAttributeDateLowerThresholdMap().size()> 0){
			System.out.println("Date Attribute Set Configurations");
			for(String attribute : input.getAttributeDateLowerThresholdMap().keySet())
				System.out.println(attribute+" @ "+input.getAttributeDateLowerThresholdMap().get(attribute)+" @ "+input.getAttributeDateUpperThresholdMap().get(attribute));
		}
	}
	
	public XLog getFilteredLog(){
		return filteredLog;
	}
}
