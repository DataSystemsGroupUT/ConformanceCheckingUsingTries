package org.processmining.logfiltering.legacy.plugins.logfiltering;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.Combination;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.FilterLevel;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.FilterType;

public class LogFilteringInput {
	XLog log;
	FilterLevel filterLevel;
	Combination combination;
	LogProperties logProperties;
	
	Map<String, FilterType> attributeFilterTypeMap;
	Map<String, Set<String>> attributeValueSetMap;
	Map<String, Double> attributeMinValueThresholdMap;
	Map<String, Double> attributeMaxValueThresholdMap;
	Map<String, Date> attributeDateLowerThresholdMap;
	Map<String, Date> attributeDateUpperThresholdMap;
	
	public LogFilteringInput(){
		
	}

	public FilterLevel getFilterLevel() {
		return filterLevel;
	}

	public void setFilterLevel(FilterLevel filterLevel) {
		this.filterLevel = filterLevel;
	}

	public XLog getLog() {
		return log;
	}

	public void setLog(XLog log) {
		this.log = log;
	}

	public Map<String, FilterType> getAttributeFilterTypeMap() {
		return attributeFilterTypeMap;
	}

	public void setAttributeFilterTypeMap(Map<String, FilterType> attributeFilterTypeMap) {
		this.attributeFilterTypeMap = attributeFilterTypeMap;
	}

	public Map<String, Set<String>> getAttributeValueSetMap() {
		return attributeValueSetMap;
	}

	public void setAttributeValueSetMap(
			Map<String, Set<String>> attributeValueSetMap) {
		this.attributeValueSetMap = attributeValueSetMap;
	}

	public Map<String, Date> getAttributeDateLowerThresholdMap() {
		return attributeDateLowerThresholdMap;
	}

	public void setAttributeDateLowerThresholdMap(
			Map<String, Date> attributeDateLowerThresholdMap) {
		this.attributeDateLowerThresholdMap = attributeDateLowerThresholdMap;
	}

	public Map<String, Date> getAttributeDateUpperThresholdMap() {
		return attributeDateUpperThresholdMap;
	}

	public void setAttributeDateUpperThresholdMap(
			Map<String, Date> attributeDateUpperThresholdMap) {
		this.attributeDateUpperThresholdMap = attributeDateUpperThresholdMap;
	}

	public Map<String, Double> getAttributeMinValueThresholdMap() {
		return attributeMinValueThresholdMap;
	}

	public void setAttributeMinValueThresholdMap(
			Map<String, Double> attributeMinValueThresholdMap) {
		this.attributeMinValueThresholdMap = attributeMinValueThresholdMap;
	}

	public Map<String, Double> getAttributeMaxValueThresholdMap() {
		return attributeMaxValueThresholdMap;
	}

	public void setAttributeMaxValueThresholdMap(
			Map<String, Double> attributeMaxValueThresholdMap) {
		this.attributeMaxValueThresholdMap = attributeMaxValueThresholdMap;
	}

	public Combination getCombination() {
		return combination;
	}

	public void setCombination(Combination combination) {
		this.combination = combination;
	}

	public LogProperties getLogProperties() {
		return logProperties;
	}

	public void setLogProperties(LogProperties logProperties) {
		this.logProperties = logProperties;
	}
}
