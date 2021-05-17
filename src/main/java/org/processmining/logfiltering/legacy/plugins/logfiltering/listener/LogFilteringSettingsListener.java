package org.processmining.logfiltering.legacy.plugins.logfiltering.listener;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.Combination;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.FilterLevel;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.FilterType;

public interface LogFilteringSettingsListener {
	public void setFilterLevel(FilterLevel filterLevel);
	public void setCombination(Combination combination);
	public void setAttributeFilterTypeMap(Map<String, FilterType> attributeFilterType);
	public void setAttributeSelectedValueSetMap(Map<String, Set<String>> attributeValueSetMap);
	public void setAttributeStartDateThresholdMap(
			Map<String, Date> attributeStartDateMap);
	public void setAttributeEndDateThresholdMap(
			Map<String, Date> attributeEndDateMap);
	public void setAttributeMaxValueThresholdMap(
			Map<String, Double> attributeMaxValueThresholdMap);
	public void setAttributeMinValueThresholdMap(
			Map<String, Double> attributeMinValueThresholdMap);
}
