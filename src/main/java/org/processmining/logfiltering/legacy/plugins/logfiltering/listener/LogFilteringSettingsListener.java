package org.processmining.logfiltering.legacy.plugins.logfiltering.listener;

import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.Combination;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.FilterLevel;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.FilterType;

import java.util.Date;
import java.util.Map;
import java.util.Set;

public interface LogFilteringSettingsListener {
    void setFilterLevel(FilterLevel filterLevel);

    void setCombination(Combination combination);

    void setAttributeFilterTypeMap(Map<String, FilterType> attributeFilterType);

    void setAttributeSelectedValueSetMap(Map<String, Set<String>> attributeValueSetMap);

    void setAttributeStartDateThresholdMap(
            Map<String, Date> attributeStartDateMap);

    void setAttributeEndDateThresholdMap(
            Map<String, Date> attributeEndDateMap);

    void setAttributeMaxValueThresholdMap(
            Map<String, Double> attributeMaxValueThresholdMap);

    void setAttributeMinValueThresholdMap(
            Map<String, Double> attributeMinValueThresholdMap);
}
