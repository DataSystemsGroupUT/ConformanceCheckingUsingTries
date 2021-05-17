package org.processmining.logfiltering.utils;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XsDateTimeFormat;

public class LogProperties {
	
	Map<String, String> traceAttributeTypeMap = new HashMap<String, String>();
	Map<String, String> eventAttributeTypeMap = new HashMap<String, String>();
	
	Map<String, Set<String>> traceAttributeValueSetMap = new HashMap<String, Set<String>>();
	Map<String, Set<String>> eventAttributeValueSetMap = new HashMap<String, Set<String>>();
	
	Map<String, Double> traceAttributeMinValueMap = new HashMap<String, Double>();
	Map<String, Double> eventAttributeMinValueMap = new HashMap<String, Double>();
	Map<String, Double> traceAttributeMaxValueMap = new HashMap<String, Double>();
	Map<String, Double> eventAttributeMaxValueMap = new HashMap<String, Double>();
	
	Map<String, Date> traceAttributeTimeStampStartMap = new HashMap<String, Date>();
	Map<String, Date> traceAttributeTimeStampEndMap = new HashMap<String, Date>();
	Map<String, Date> eventAttributeTimeStampStartMap = new HashMap<String, Date>();
	Map<String, Date> eventAttributeTimeStampEndMap = new HashMap<String, Date>();
	Date logStartDate, logEndDate;
	
	public LogProperties(XLog log){
		getLogProperties(log);
	}
	
	private void getLogProperties(XLog log){
		int minTraceLength=Integer.MAX_VALUE, maxTraceLength=0;
		
		SortedSet<Date> traceStartTimeSet = new TreeSet<Date>();
		
		for(XTrace trace : log){
			if(trace.size() < minTraceLength)
				minTraceLength = trace.size();
			if(trace.size() > maxTraceLength)
				maxTraceLength = trace.size();
			
			traceStartTimeSet.add(XTimeExtension.instance().extractTimestamp(trace.get(0)));
			
			traceAttributeTypeMap.put("Trace Length", "continuous");
			
			Set<Entry<String, XAttribute>> entrySet = trace.getAttributes().entrySet();
			for(Entry<String, XAttribute> entry : entrySet){
				if(traceAttributeTypeMap.containsKey(entry.getKey())){
					if(!traceAttributeTypeMap.get(entry.getKey()).equals(getType(entry.getValue()))){
						System.out.println("Something Wrong: "+entry.getKey()+" has more than one attribute type");
					}
				}else{
					traceAttributeTypeMap.put(entry.getKey(), getType(entry.getValue()));
				}
			}
			for(XEvent event : trace){
				entrySet = event.getAttributes().entrySet();
				for(Entry<String, XAttribute> entry : entrySet){
					if(eventAttributeTypeMap.containsKey(entry.getKey())){
						if(!eventAttributeTypeMap.get(entry.getKey()).equals(getType(entry.getValue()))){
							System.out.println("Something Wrong: "+entry.getKey()+" has more than one attribute type");
						}
					}else{
						eventAttributeTypeMap.put(entry.getKey(), getType(entry.getValue()));
					}
				}
			}
		}
		
		logStartDate = traceStartTimeSet.first();
		logEndDate = traceStartTimeSet.last();
		
		traceAttributeTypeMap.put("Trace Arrival Date", "date");
		
		//store the values of the attributes
		XAttributeMap traceAttributeMap, eventAttributeMap;
		String attributeType;
		Date timeStamp;

		for(XTrace trace : log){
			traceAttributeMap = trace.getAttributes();
			for(String traceAttribute : traceAttributeMap.keySet()){
				if(traceAttributeTypeMap.containsKey(traceAttribute)){
					attributeType = traceAttributeTypeMap.get(traceAttribute);
					if(attributeType.equals("date")){
						XsDateTimeFormat timeFormat = new XsDateTimeFormat();
						try {
							timeStamp = timeFormat.parseObject(traceAttributeMap.get(traceAttribute).toString());
							if(traceAttributeTimeStampStartMap.containsKey(traceAttribute)){
								if(traceAttributeTimeStampStartMap.get(traceAttribute).after(timeStamp)){
									traceAttributeTimeStampStartMap.put(traceAttribute, timeStamp);
								}
							}else{
								traceAttributeTimeStampStartMap.put(traceAttribute, timeStamp);
							}
							if(traceAttributeTimeStampEndMap.containsKey(traceAttribute)){
								if(traceAttributeTimeStampEndMap.get(traceAttribute).before(timeStamp)){
									traceAttributeTimeStampEndMap.put(traceAttribute, timeStamp);
								}
							}else{
								traceAttributeTimeStampEndMap.put(traceAttribute, timeStamp);
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}else if(attributeType.equals("continuous")){
						Double value = new Double(traceAttributeMap.get(traceAttribute).toString());
						if(traceAttributeMinValueMap.containsKey(traceAttribute)){
							if(traceAttributeMinValueMap.get(traceAttribute) > value)
								traceAttributeMinValueMap.put(traceAttribute, value);
						}else{
							traceAttributeMinValueMap.put(traceAttribute, value);
						}
						
						if(traceAttributeMaxValueMap.containsKey(traceAttribute)){
							if(traceAttributeMaxValueMap.get(traceAttribute) < value){
								traceAttributeMaxValueMap.put(traceAttribute, value);
							}
						}else{
							traceAttributeMaxValueMap.put(traceAttribute, value);
						}
					}else{
						Set<String> attributeValueSet;
						if(traceAttributeValueSetMap.containsKey(traceAttribute))
							attributeValueSet = traceAttributeValueSetMap.get(traceAttribute);
						else
							attributeValueSet = new HashSet<String>();
						attributeValueSet.add(traceAttributeMap.get(traceAttribute).toString());
						traceAttributeValueSetMap.put(traceAttribute, attributeValueSet);
					}
				}
			}
			
			traceAttributeMinValueMap.put("Trace Length", (double)minTraceLength);
			traceAttributeMaxValueMap.put("Trace Length", (double)maxTraceLength);
			
			traceAttributeTimeStampStartMap.put("Trace Arrival Date", logStartDate);
			traceAttributeTimeStampEndMap.put("Trace Arrival Date", logEndDate);
			
			
			for(XEvent event : trace){
				eventAttributeMap = event.getAttributes();
				for(String eventAttribute : eventAttributeMap.keySet()){
					if(eventAttributeTypeMap.containsKey(eventAttribute)){
						attributeType = eventAttributeTypeMap.get(eventAttribute);
						if(attributeType.equals("date")){
							XsDateTimeFormat timeFormat = new XsDateTimeFormat();
							try {
								timeStamp = timeFormat.parseObject(eventAttributeMap.get(eventAttribute).toString());
								if(eventAttributeTimeStampStartMap.containsKey(eventAttribute)){
									if(eventAttributeTimeStampStartMap.get(eventAttribute).after(timeStamp)){
										eventAttributeTimeStampStartMap.put(eventAttribute, timeStamp);
									}
								}else{
									eventAttributeTimeStampStartMap.put(eventAttribute, timeStamp);
								}
								if(eventAttributeTimeStampEndMap.containsKey(eventAttribute)){
									if(eventAttributeTimeStampEndMap.get(eventAttribute).before(timeStamp)){
										eventAttributeTimeStampEndMap.put(eventAttribute, timeStamp);
									}
								}else{
									eventAttributeTimeStampEndMap.put(eventAttribute, timeStamp);
								}
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}else if(attributeType.equals("continuous")){
							Double value = new Double(eventAttributeMap.get(eventAttribute).toString());
							if(eventAttributeMinValueMap.containsKey(eventAttribute)){
								if(eventAttributeMinValueMap.get(eventAttribute) > value)
									eventAttributeMinValueMap.put(eventAttribute, value);
							}else{
								eventAttributeMinValueMap.put(eventAttribute, value);
							}
							
							if(eventAttributeMaxValueMap.containsKey(eventAttribute)){
								if(eventAttributeMaxValueMap.get(eventAttribute) < value){
									eventAttributeMaxValueMap.put(eventAttribute, value);
								}
							}else{
								eventAttributeMaxValueMap.put(eventAttribute, value);
							}
						}else{
							Set<String> attributeValueSet;
							if(eventAttributeValueSetMap.containsKey(eventAttribute))
								attributeValueSet = eventAttributeValueSetMap.get(eventAttribute);
							else
								attributeValueSet = new HashSet<String>();
							attributeValueSet.add(eventAttributeMap.get(eventAttribute).toString());
							eventAttributeValueSetMap.put(eventAttribute, attributeValueSet);
						}
					}
				}
			}
		}
		
		System.out.println("-------------------------");
		System.out.println("No. Trace Attributes: "+traceAttributeTypeMap.size());
		for(String traceAttribute : traceAttributeTypeMap.keySet()){
			System.out.println(traceAttribute+" @ "+traceAttributeTypeMap.get(traceAttribute));
			attributeType = traceAttributeTypeMap.get(traceAttribute);
			if(attributeType.equals("date")){
				System.out.println("\t"+traceAttributeTimeStampStartMap.get(traceAttribute)+" @ "+traceAttributeTimeStampEndMap.get(traceAttribute));
			}else if(attributeType.equals("continuous")){
				System.out.println("\t"+traceAttributeMinValueMap.get(traceAttribute)+" @ "+traceAttributeMaxValueMap.get(traceAttribute));
			}else{
				System.out.println("\t"+traceAttributeValueSetMap.get(traceAttribute).size()+" @ "+traceAttributeValueSetMap.get(traceAttribute));
			}
		}
		
		System.out.println("-------------------------");
		System.out.println("No. Event Attributes: "+eventAttributeTypeMap.size());
		for(String eventAttribute : eventAttributeTypeMap.keySet()){
			System.out.println(eventAttribute+" @ "+eventAttributeTypeMap.get(eventAttribute));
			attributeType = eventAttributeTypeMap.get(eventAttribute);
			if(attributeType.equals("date")){
				System.out.println("\t"+eventAttributeTimeStampStartMap.get(eventAttribute)+" @ "+eventAttributeTimeStampEndMap.get(eventAttribute));
			}else if(attributeType.equals("continuous")){
				System.out.println("\t"+eventAttributeMinValueMap.get(eventAttribute)+" @ "+eventAttributeMaxValueMap.get(eventAttribute));
			}else{
				System.out.println("\t"+eventAttributeValueSetMap.get(eventAttribute).size()+" @ "+eventAttributeValueSetMap.get(eventAttribute));
			}
		}
	}

	private String getType(XAttribute attribute){
		if(attribute instanceof XAttributeBoolean)
			return "boolean";
		else if(attribute instanceof XAttributeDiscrete)
			return "discrete";
		else if(attribute instanceof XAttributeContinuous)
			return "continuous";
		else if(attribute instanceof XAttributeTimestamp)
			return "date";
		else 
			return "string";
	}

	public Map<String, String> getTraceAttributeTypeMap() {
		return traceAttributeTypeMap;
	}

	public Map<String, String> getEventAttributeTypeMap() {
		return eventAttributeTypeMap;
	}

	public Map<String, Set<String>> getTraceAttributeValueSetMap() {
		return traceAttributeValueSetMap;
	}

	public Map<String, Set<String>> getEventAttributeValueSetMap() {
		return eventAttributeValueSetMap;
	}

	public Map<String, Double> getTraceAttributeMinValueMap() {
		return traceAttributeMinValueMap;
	}

	public Map<String, Double> getEventAttributeMinValueMap() {
		return eventAttributeMinValueMap;
	}

	public Map<String, Double> getTraceAttributeMaxValueMap() {
		return traceAttributeMaxValueMap;
	}

	public Map<String, Double> getEventAttributeMaxValueMap() {
		return eventAttributeMaxValueMap;
	}

	public Map<String, Date> getTraceAttributeTimeStampStartMap() {
		return traceAttributeTimeStampStartMap;
	}

	public Map<String, Date> getTraceAttributeTimeStampEndMap() {
		return traceAttributeTimeStampEndMap;
	}

	public Map<String, Date> getEventAttributeTimeStampStartMap() {
		return eventAttributeTimeStampStartMap;
	}

	public Map<String, Date> getEventAttributeTimeStampEndMap() {
		return eventAttributeTimeStampEndMap;
	}

	public Date getLogStartDate() {
		return logStartDate;
	}

	public void setLogStartDate(Date logStartDate) {
		this.logStartDate = logStartDate;
	}

	public Date getLogEndDate() {
		return logEndDate;
	}

	public void setLogEndDate(Date logEndDate) {
		this.logEndDate = logEndDate;
	}
}