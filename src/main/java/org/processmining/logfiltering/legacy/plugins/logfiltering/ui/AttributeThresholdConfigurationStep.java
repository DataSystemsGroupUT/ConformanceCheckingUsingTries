package org.processmining.logfiltering.legacy.plugins.logfiltering.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.processmining.logfiltering.legacy.plugins.logfiltering.LogProperties;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.Combination;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.FilterLevel;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.FilterType;
import org.processmining.logfiltering.legacy.plugins.logfiltering.listener.LogFilteringSettingsListener;
import org.processmining.logfiltering.legacy.plugins.logfiltering.swingx.ScrollableGridLayout;

import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.toedter.calendar.JDateChooser;

import csplugins.id.mapping.ui.CheckComboBox;

@SuppressWarnings("serial")
public class AttributeThresholdConfigurationStep extends myStep {
	LogFilteringSettingsListener listener;
	LogProperties logProperties;
	FilterLevel filterLevel;
	Combination combination;
	
	Map<String, Set<String>> attributeSelectedValueSetMap = new HashMap<String, Set<String>>();
	Map<String, Double> attributeMinValueThresholdMap = new HashMap<String, Double>();
	Map<String, Double> attributeMaxValueThresholdMap = new HashMap<String, Double>();
	Map<String, Date> attributeStartDateThresholdMap = new HashMap<String, Date>();
	Map<String, Date> attributeEndDateThresholdMap = new HashMap<String, Date>();
	
	JPanel traceLevelAttributeConfigurationPanel;
	JPanel eventLevelAttributeConfigurationPanel;

	List<JCheckBox> traceLevelAttributeCheckBoxList;
	List<JRadioButton> traceLevelRetainRadioButtonList;
	List<JRadioButton> traceLevelRemoveRadioButtonList;
	List<Component> traceLevelAttributeValueSelectionComponentList;
	
	List<JCheckBox> eventLevelAttributeCheckBoxList;
	List<JRadioButton> eventLevelRetainRadioButtonList;
	List<JRadioButton> eventLevelRemoveRadioButtonList;
	List<Component> eventLevelAttributeValueSelectionComponentList;
	
	Map<String, FilterType> attributeFilterTypeMap = new HashMap<String, FilterType>();
	
	public AttributeThresholdConfigurationStep(LogProperties logProperties){
		this.logProperties = logProperties;
		initComponents();
	}
	
	private void initComponents(){
		int noRows = 4;
		ScrollableGridLayout attributeThresholdConfigurationStepLayout = new ScrollableGridLayout(this, 1, noRows, 0, 0);
		for(int i = 0; i < noRows; i++)
			attributeThresholdConfigurationStepLayout.setRowFixed(i, true);
		this.setLayout(attributeThresholdConfigurationStepLayout);
		
		JPanel filterLevelPanel = prepareFilterLevelPanel();
		JPanel combinationPanel = prepareCombinationPanel();
		
		prepareTraceLevelAttributeConfigurationPanel();
		prepareEventLevelAttributeConfigurationPanel();
		
		int yPos = 0;
		attributeThresholdConfigurationStepLayout.setPosition(filterLevelPanel, 0, yPos++);
		add(filterLevelPanel);
		attributeThresholdConfigurationStepLayout.setPosition(combinationPanel, 0, yPos++);
		add(combinationPanel);
		
		attributeThresholdConfigurationStepLayout.setPosition(traceLevelAttributeConfigurationPanel, 0, yPos++);
		add(traceLevelAttributeConfigurationPanel);
		attributeThresholdConfigurationStepLayout.setPosition(eventLevelAttributeConfigurationPanel, 0, yPos++);
		add(eventLevelAttributeConfigurationPanel);
	}
	
	private JPanel prepareCombinationPanel(){
		JPanel combinationPanel = SlickerFactory.instance().createRoundedPanel();
		combinationPanel.setBorder(BorderFactory.createTitledBorder("How would you like to combine the various attribute configurations?"));
		
		ScrollableGridLayout combinationPanelLayout = new ScrollableGridLayout(combinationPanel, 2, 1, 0, 0);
		combinationPanelLayout.setColumnFixed(0, true);
		combinationPanelLayout.setColumnFixed(1, true);
		
		combinationPanel.setLayout(combinationPanelLayout);
		
		
		final JRadioButton conjunctionRadioButton = SlickerFactory.instance().createRadioButton("Conjunction");
		final JRadioButton disjunctionRadioButton = SlickerFactory.instance().createRadioButton("Disjunction");
		conjunctionRadioButton.setSelected(true);
		combination = Combination.Conjunction;
		
		ButtonGroup combinationButtonGroup = new ButtonGroup();
		combinationButtonGroup.add(conjunctionRadioButton);
		combinationButtonGroup.add(disjunctionRadioButton);
		
		conjunctionRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(conjunctionRadioButton.isSelected()){
					combination = Combination.Conjunction;
				}else{
					combination = Combination.Disjunction;
				}
			}
		});
		
		disjunctionRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(disjunctionRadioButton.isSelected()){
					combination = Combination.Disjunction;
				}else{
					combination = Combination.Conjunction;
				}
			}
		});
		
		combinationPanelLayout.setPosition(conjunctionRadioButton, 0, 0);
		combinationPanel.add(conjunctionRadioButton);
		combinationPanelLayout.setPosition(disjunctionRadioButton, 1, 0);
		combinationPanel.add(disjunctionRadioButton);
		
		return combinationPanel;

	}
	
	private JPanel prepareFilterLevelPanel(){
		JPanel filterLevelPanel = SlickerFactory.instance().createRoundedPanel();
		filterLevelPanel.setBorder(BorderFactory.createTitledBorder("Select the Level at Which You Want to Apply Filtering"));
		
		ScrollableGridLayout filterLevelPanelLayout = new ScrollableGridLayout(filterLevelPanel, 2, 1, 0, 0);
		filterLevelPanelLayout.setColumnFixed(0, true);
		filterLevelPanelLayout.setColumnFixed(1, true);
		
		filterLevelPanel.setLayout(filterLevelPanelLayout);
		
		
		final JRadioButton traceLevelRadioButton = SlickerFactory.instance().createRadioButton("Trace Level");
		final JRadioButton eventLevelRadioButton = SlickerFactory.instance().createRadioButton("Event Level");
		traceLevelRadioButton.setSelected(true);
		filterLevel = FilterLevel.Trace;
		
		ButtonGroup filterLevelButtonGroup = new ButtonGroup();
		filterLevelButtonGroup.add(traceLevelRadioButton);
		filterLevelButtonGroup.add(eventLevelRadioButton);
		
		traceLevelRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(traceLevelRadioButton.isSelected()){
					filterLevel = FilterLevel.Trace;
					traceLevelAttributeConfigurationPanel.setVisible(true);
					eventLevelAttributeConfigurationPanel.setVisible(false);
				}else{
					filterLevel = FilterLevel.Event;
					traceLevelAttributeConfigurationPanel.setVisible(false);
					eventLevelAttributeConfigurationPanel.setVisible(true);
				}
			}
		});
		
		eventLevelRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(eventLevelRadioButton.isSelected()){
					filterLevel = FilterLevel.Event;
					traceLevelAttributeConfigurationPanel.setVisible(false);
					eventLevelAttributeConfigurationPanel.setVisible(true);
				}else{
					filterLevel = FilterLevel.Trace;
					traceLevelAttributeConfigurationPanel.setVisible(true);
					eventLevelAttributeConfigurationPanel.setVisible(false);
				}
			}
		});
		
		filterLevelPanelLayout.setPosition(traceLevelRadioButton, 0, 0);
		filterLevelPanel.add(traceLevelRadioButton);
		filterLevelPanelLayout.setPosition(eventLevelRadioButton, 1, 0);
		filterLevelPanel.add(eventLevelRadioButton);
		
		return filterLevelPanel;
		
	}
	
	private void prepareTraceLevelAttributeConfigurationPanel(){
		traceLevelAttributeConfigurationPanel = SlickerFactory.instance().createRoundedPanel();
		traceLevelAttributeConfigurationPanel.setBorder(BorderFactory.createTitledBorder("Trace Level Attribute Configuration"));
		
		int noRows = logProperties.getTraceAttributeTypeMap().size();
		int noCols = 6;
		
		ScrollableGridLayout traceLevelAttributeConfigurationPanelLayout = new ScrollableGridLayout(traceLevelAttributeConfigurationPanel, noCols, noRows, 0, 0);
		for(int i = 0; i < noRows; i++){
			traceLevelAttributeConfigurationPanelLayout.setRowFixed(i, true);
		}
		
		for(int i = 0; i < noCols; i++)
			traceLevelAttributeConfigurationPanelLayout.setColumnFixed(i, true);
		
		traceLevelAttributeConfigurationPanel.setLayout(traceLevelAttributeConfigurationPanelLayout);
		
		traceLevelAttributeCheckBoxList = new ArrayList<JCheckBox>();
		traceLevelRetainRadioButtonList = new ArrayList<JRadioButton>();
		traceLevelRemoveRadioButtonList = new ArrayList<JRadioButton>();
		traceLevelAttributeValueSelectionComponentList = new ArrayList<Component>();
		
		
		
		int yPos = 0;
		Map<String, String> traceAttributeTypeMap = logProperties.getTraceAttributeTypeMap();
		SortedSet<String> traceAttributeSet = new TreeSet<String>();
		traceAttributeSet.addAll(traceAttributeTypeMap.keySet());
		
		for(String traceAttribute : traceAttributeSet){
			JCheckBox attributeCheckBox = SlickerFactory.instance().createCheckBox(traceAttribute, false);
			attributeCheckBox.setName(traceAttribute);
			traceLevelAttributeCheckBoxList.add(attributeCheckBox);
			
			JRadioButton retainRadioButton = SlickerFactory.instance().createRadioButton("Retain");
			traceLevelRetainRadioButtonList.add(retainRadioButton);
			JRadioButton removeRadioButton = SlickerFactory.instance().createRadioButton("Remove");
			traceLevelRemoveRadioButtonList.add(removeRadioButton);
			ButtonGroup buttonGroup = new ButtonGroup();
			buttonGroup.add(retainRadioButton);
			buttonGroup.add(removeRadioButton);
			
			retainRadioButton.setSelected(true);
			
			Component attributeValueComponent = null;
			if(traceAttributeTypeMap.get(traceAttribute).equals("string") || traceAttributeTypeMap.get(traceAttribute).equals("discrete")){
				attributeValueComponent = prepareLiteralComponent(logProperties.getTraceAttributeValueSetMap().get(traceAttribute));
			}else if(traceAttributeTypeMap.get(traceAttribute).equals("continuous")){
				attributeValueComponent = prepareContinuousComponent(logProperties.getTraceAttributeMinValueMap().get(traceAttribute),logProperties.getTraceAttributeMaxValueMap().get(traceAttribute));
			}else if(traceAttributeTypeMap.get(traceAttribute).equals("date")){
				attributeValueComponent = prepareDateComponent(logProperties.getTraceAttributeTimeStampStartMap().get(traceAttribute), logProperties.getTraceAttributeTimeStampEndMap().get(traceAttribute));
			}
			traceLevelAttributeValueSelectionComponentList.add(attributeValueComponent);
			
			
			traceLevelAttributeConfigurationPanelLayout.setPosition(attributeCheckBox,0,yPos);
			traceLevelAttributeConfigurationPanel.add(attributeCheckBox);
			
			Component horizontalStrut1 = Box.createHorizontalStrut(25);
			
			traceLevelAttributeConfigurationPanelLayout.setPosition(horizontalStrut1,1,yPos);
			traceLevelAttributeConfigurationPanel.add(horizontalStrut1);
			
			traceLevelAttributeConfigurationPanelLayout.setPosition(retainRadioButton,2,yPos);
			traceLevelAttributeConfigurationPanel.add(retainRadioButton);
			traceLevelAttributeConfigurationPanelLayout.setPosition(removeRadioButton,3,yPos);
			traceLevelAttributeConfigurationPanel.add(removeRadioButton);
			
			Component horizontalStrut2 = Box.createHorizontalStrut(25);
			
			traceLevelAttributeConfigurationPanelLayout.setPosition(horizontalStrut2,4,yPos);
			traceLevelAttributeConfigurationPanel.add(horizontalStrut2);
			
			traceLevelAttributeConfigurationPanelLayout.setPosition(attributeValueComponent,5,yPos++);
			traceLevelAttributeConfigurationPanel.add(attributeValueComponent);
		}
		
		traceLevelAttributeConfigurationPanel.setVisible(true);
	}
	
	private CheckComboBox prepareLiteralComponent(Set<String> attributeValueSet){
		 SortedSet<String> sortedAttributeValueSet = new TreeSet<String>();
		 sortedAttributeValueSet.addAll(attributeValueSet);
		 CheckComboBox attributeValueSelectionComboBox = new CheckComboBox(sortedAttributeValueSet);
		 return attributeValueSelectionComboBox;
	}
	
	private JPanel prepareContinuousComponent(double minValue, double maxValue){
		JPanel continuousComponentPanel = SlickerFactory.instance().createRoundedPanel();
		
		int noCols = 5;
		ScrollableGridLayout continuousComponentPanelLayout = new ScrollableGridLayout(continuousComponentPanel, noCols, 1, 0, 0);
		for(int i = 0; i < noCols; i++)
			continuousComponentPanelLayout.setColumnFixed(i, true);
		continuousComponentPanel.setLayout(continuousComponentPanelLayout);
		
		JLabel minValueLabel = SlickerFactory.instance().createLabel("Min. Val  ");
		JLabel maxValueLabel = SlickerFactory.instance().createLabel("Max. Val  ");
		JTextField minValueTextField = new JTextField(minValue+"    ");
		minValueTextField.setName("MIN_VALUE");
		JTextField maxValueTextField = new JTextField(maxValue+"    ");
		maxValueTextField.setName("MAX_VALUE");
		
		Component horizontalStrut = Box.createHorizontalStrut(15);
		
		continuousComponentPanelLayout.setPosition(minValueLabel, 0, 0);
		continuousComponentPanel.add(minValueLabel);
		continuousComponentPanelLayout.setPosition(minValueTextField, 1, 0);
		continuousComponentPanel.add(minValueTextField);
		
		continuousComponentPanelLayout.setPosition(horizontalStrut, 2, 0);
		continuousComponentPanel.add(horizontalStrut);
		
		continuousComponentPanelLayout.setPosition(maxValueLabel, 3, 0);
		continuousComponentPanel.add(maxValueLabel);
		continuousComponentPanelLayout.setPosition(maxValueTextField, 4, 0);
		continuousComponentPanel.add(maxValueTextField);
		
		return continuousComponentPanel;
	}
	
	private JPanel prepareDateComponent(Date startDate, Date endDate){
		JPanel dateComponentPanel = SlickerFactory.instance().createRoundedPanel();
		
		int noCols = 5;
		ScrollableGridLayout dateComponentPanelLayout = new ScrollableGridLayout(dateComponentPanel, noCols, 1, 0, 0);
		for(int i = 0; i < noCols; i++)
			dateComponentPanelLayout.setColumnFixed(i, true);
		dateComponentPanel.setLayout(dateComponentPanelLayout);
		
		JLabel startDateLabel = SlickerFactory.instance().createLabel("Start Date");
		JLabel endDateLabel = SlickerFactory.instance().createLabel("End Date");
		JDateChooser startDateChooser = new JDateChooser(startDate);
		startDateChooser.setName("StartDate");
		JDateChooser endDateChooser = new JDateChooser(endDate);
		endDateChooser.setName("EndDate");
		
		Component horizontalStrut = Box.createHorizontalStrut(15);
		
		dateComponentPanelLayout.setPosition(startDateLabel, 0, 0);
		dateComponentPanel.add(startDateLabel);
		dateComponentPanelLayout.setPosition(startDateChooser, 1, 0);
		dateComponentPanel.add(startDateChooser);
		
		dateComponentPanelLayout.setPosition(horizontalStrut, 2, 0);
		dateComponentPanel.add(horizontalStrut);
		
		dateComponentPanelLayout.setPosition(endDateLabel, 3, 0);
		dateComponentPanel.add(endDateLabel);
		dateComponentPanelLayout.setPosition(endDateChooser, 4, 0);
		dateComponentPanel.add(endDateChooser);
		
		return dateComponentPanel;
	}
	
	protected JPanel prepareDatePanel(final String attributeName, Date startDate, Date endDate){
		JPanel datePanel = SlickerFactory.instance().createRoundedPanel();
		datePanel.setBorder(BorderFactory.createTitledBorder(""));
		
		ScrollableGridLayout datePanelLayout = new ScrollableGridLayout(datePanel, 7, 1, 0, 0);
		for(int i = 0; i < 7; i++)
			datePanelLayout.setColumnFixed(i, true);
		
		datePanel.setLayout(datePanelLayout);
		
		final JCheckBox attributeSelectionCheckBox = SlickerFactory.instance().createCheckBox(attributeName+fillString(' ',attributeName.length() < 30 ? 30-attributeName.length(): 0), false);
		attributeSelectionCheckBox.setName(attributeName);
		System.out.println("CHECK :"+attributeSelectionCheckBox.getName());
		final JRadioButton retainRadioButton = SlickerFactory.instance().createRadioButton("Retain");
		retainRadioButton.setName(FilterType.Retain+"");
		final JRadioButton removeRadioButton = SlickerFactory.instance().createRadioButton("Remove");
		removeRadioButton.setName(FilterType.Remove+"");
		
		retainRadioButton.setSelected(true);
		
		ButtonGroup filterTypeButtonGroup = new ButtonGroup();
		filterTypeButtonGroup.add(retainRadioButton);
		filterTypeButtonGroup.add(removeRadioButton);
		
		retainRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(retainRadioButton.isSelected())
					attributeFilterTypeMap.put(attributeName, FilterType.Retain);
				else
					attributeFilterTypeMap.put(attributeName, FilterType.Remove);
			}
		});
		
		removeRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(removeRadioButton.isSelected())
					attributeFilterTypeMap.put(attributeName, FilterType.Remove);
				else
					attributeFilterTypeMap.put(attributeName, FilterType.Retain);
			}
		});
		
		JLabel startDateLabel = SlickerFactory.instance().createLabel("Start Date");
		JLabel endDateLabel = SlickerFactory.instance().createLabel("End Date");
		JDateChooser startDateChooser = new JDateChooser(startDate);
		startDateChooser.setName("StartDate");
		JDateChooser endDateChooser = new JDateChooser(endDate);
		endDateChooser.setName("EndDate");
		
		attributeSelectionCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(attributeSelectionCheckBox.isSelected()){
					retainRadioButton.setEnabled(true);
					removeRadioButton.setEnabled(true);
					
					if(retainRadioButton.isSelected())
						attributeFilterTypeMap.put(attributeName, FilterType.Retain);
					if(removeRadioButton.isSelected())
						attributeFilterTypeMap.put(attributeName, FilterType.Remove);
				}else{
					retainRadioButton.setEnabled(false);
					removeRadioButton.setEnabled(false);
					
					
					attributeFilterTypeMap.remove(attributeName);
				}
			}
		});
		
		datePanelLayout.setPosition(attributeSelectionCheckBox, 0, 0);
		datePanel.add(attributeSelectionCheckBox);
		
		datePanelLayout.setPosition(retainRadioButton, 1, 0);
		datePanel.add(retainRadioButton);
		datePanelLayout.setPosition(removeRadioButton, 2, 0);
		datePanel.add(removeRadioButton);
		
		datePanelLayout.setPosition(startDateLabel, 3, 0);
		datePanel.add(startDateLabel);
		
		
		datePanelLayout.setPosition(startDateChooser, 4, 0);
		datePanel.add(startDateChooser);
		
		datePanelLayout.setPosition(endDateLabel, 5, 0);
		datePanel.add(endDateLabel);
		
		datePanelLayout.setPosition(endDateChooser, 6, 0);
		datePanel.add(endDateChooser);
		
		return datePanel;
	}
	
	protected JPanel prepareContinuousPanel (final String attributeName, double minValue, double maxValue){
		JPanel continuousPanel = SlickerFactory.instance().createRoundedPanel();
		continuousPanel.setBorder(BorderFactory.createTitledBorder(""));
		
		ScrollableGridLayout continuousPanelLayout = new ScrollableGridLayout(continuousPanel, 7, 1, 0, 0);
		for(int i = 0; i < 7; i++)
			continuousPanelLayout.setColumnFixed(i, true);
	
		
		continuousPanel.setLayout(continuousPanelLayout);
		
		final JCheckBox attributeSelectionCheckBox = SlickerFactory.instance().createCheckBox(attributeName+fillString(' ',attributeName.length() < 30 ? 30-attributeName.length(): 0), false);
		attributeSelectionCheckBox.setName(attributeName);
		System.out.println("CHECK :"+attributeSelectionCheckBox.getName());
		final JRadioButton retainRadioButton = SlickerFactory.instance().createRadioButton("Retain");
		retainRadioButton.setName(FilterType.Retain+"");
		final JRadioButton removeRadioButton = SlickerFactory.instance().createRadioButton("Remove");
		removeRadioButton.setName(FilterType.Remove+"");
		
		retainRadioButton.setSelected(true);
		
		ButtonGroup filterTypeButtonGroup = new ButtonGroup();
		filterTypeButtonGroup.add(retainRadioButton);
		filterTypeButtonGroup.add(removeRadioButton);
		
		retainRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(retainRadioButton.isSelected())
					attributeFilterTypeMap.put(attributeName, FilterType.Retain);
				else
					attributeFilterTypeMap.put(attributeName, FilterType.Remove);
			}
		});
		
		removeRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(removeRadioButton.isSelected())
					attributeFilterTypeMap.put(attributeName, FilterType.Remove);
				else
					attributeFilterTypeMap.put(attributeName, FilterType.Retain);
			}
		});
		
		JLabel minValueLabel = SlickerFactory.instance().createLabel("Min. Val  ");
		JLabel maxValueLabel = SlickerFactory.instance().createLabel("Max. Val  ");
		JTextField minValueTextField = new JTextField(minValue+"    ");
		minValueTextField.setName("MIN_VALUE");
		JTextField maxValueTextField = new JTextField(maxValue+"    ");
		maxValueTextField.setName("MAX_VALUE");
	
		attributeSelectionCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(attributeSelectionCheckBox.isSelected()){
					retainRadioButton.setEnabled(true);
					removeRadioButton.setEnabled(true);
					
					if(retainRadioButton.isSelected())
						attributeFilterTypeMap.put(attributeName, FilterType.Retain);
					if(removeRadioButton.isSelected())
						attributeFilterTypeMap.put(attributeName, FilterType.Remove);
				}else{
					retainRadioButton.setEnabled(false);
					removeRadioButton.setEnabled(false);
					
					
					attributeFilterTypeMap.remove(attributeName);
				}
			}
		});
		
		continuousPanelLayout.setPosition(attributeSelectionCheckBox, 0, 0);
		continuousPanel.add(attributeSelectionCheckBox);
		
		continuousPanelLayout.setPosition(retainRadioButton, 1, 0);
		continuousPanel.add(retainRadioButton);
		continuousPanelLayout.setPosition(removeRadioButton, 2, 0);
		continuousPanel.add(removeRadioButton);
		
		continuousPanelLayout.setPosition(minValueLabel, 3, 0);
		continuousPanel.add(minValueLabel);
		
		continuousPanelLayout.setPosition(minValueTextField, 4, 0);
		continuousPanel.add(minValueTextField);
		
		continuousPanelLayout.setPosition(maxValueLabel, 5, 0);
		continuousPanel.add(maxValueLabel);
		
		continuousPanelLayout.setPosition(maxValueTextField, 6, 0);
		continuousPanel.add(maxValueTextField);
		
		return continuousPanel;
	}
	
	protected JPanel prepareLiteralPanel(final String attributeName, Set<String> attributeValueSet){
		JPanel literalPanel = SlickerFactory.instance().createRoundedPanel();
		literalPanel.setBorder(BorderFactory.createTitledBorder(""));
		
		ScrollableGridLayout literalPanelLayout = new ScrollableGridLayout(literalPanel, 4, 1, 0, 0);
		literalPanelLayout.setColumnFixed(0, true);
		literalPanelLayout.setColumnFixed(1, true);
		literalPanelLayout.setColumnFixed(2, true);
		literalPanelLayout.setColumnFixed(3, true);
		
		literalPanel.setLayout(literalPanelLayout);
		
		final JCheckBox attributeSelectionCheckBox = SlickerFactory.instance().createCheckBox(attributeName+fillString(' ',attributeName.length() < 30 ? 30-attributeName.length(): 0), false);
		attributeSelectionCheckBox.setName(attributeName);
		System.out.println("CHECK :"+attributeSelectionCheckBox.getName());
		final JRadioButton retainRadioButton = SlickerFactory.instance().createRadioButton("Retain");
		retainRadioButton.setName(FilterType.Retain+"");
		final JRadioButton removeRadioButton = SlickerFactory.instance().createRadioButton("Remove");
		removeRadioButton.setName(FilterType.Remove+"");
		
		retainRadioButton.setSelected(true);
		
		ButtonGroup filterTypeButtonGroup = new ButtonGroup();
		filterTypeButtonGroup.add(retainRadioButton);
		filterTypeButtonGroup.add(removeRadioButton);
		
		retainRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(retainRadioButton.isSelected())
					attributeFilterTypeMap.put(attributeName, FilterType.Retain);
				else
					attributeFilterTypeMap.put(attributeName, FilterType.Remove);
			}
		});
		
		removeRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(removeRadioButton.isSelected())
					attributeFilterTypeMap.put(attributeName, FilterType.Remove);
				else
					attributeFilterTypeMap.put(attributeName, FilterType.Retain);
			}
		});
		
		
		
		final CheckComboBox attributeValueSelectionComboBox = new CheckComboBox(attributeValueSet);
	
		attributeSelectionCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(attributeSelectionCheckBox.isSelected()){
					retainRadioButton.setEnabled(true);
					removeRadioButton.setEnabled(true);
					attributeValueSelectionComboBox.setEnabled(true);
					if(retainRadioButton.isSelected())
						attributeFilterTypeMap.put(attributeName, FilterType.Retain);
					if(removeRadioButton.isSelected())
						attributeFilterTypeMap.put(attributeName, FilterType.Remove);
				}else{
					retainRadioButton.setEnabled(false);
					removeRadioButton.setEnabled(false);
					attributeValueSelectionComboBox.setEnabled(false);
					
					attributeFilterTypeMap.remove(attributeName);
				}
			}
		});
		
		literalPanelLayout.setPosition(attributeSelectionCheckBox, 0, 0);
		literalPanel.add(attributeSelectionCheckBox);
		
		literalPanelLayout.setPosition(retainRadioButton, 1, 0);
		literalPanel.add(retainRadioButton);
		literalPanelLayout.setPosition(removeRadioButton, 2, 0);
		literalPanel.add(removeRadioButton);
		
		literalPanelLayout.setPosition(attributeValueSelectionComboBox, 3, 0);
		literalPanel.add(attributeValueSelectionComboBox);
		
		return literalPanel;
	}
	
	private void prepareEventLevelAttributeConfigurationPanel(){
		eventLevelAttributeConfigurationPanel = SlickerFactory.instance().createRoundedPanel();
		eventLevelAttributeConfigurationPanel.setBorder(BorderFactory.createTitledBorder("Event Level Attribute Configuration"));
		
		int noCols = 6;
		int noRows = logProperties.getEventAttributeTypeMap().size();
		
		ScrollableGridLayout eventLevelAttributeConfigurationPanelLayout = new ScrollableGridLayout(eventLevelAttributeConfigurationPanel, 6, noRows, 0, 0);
		for(int i = 0; i < noRows; i++){
			eventLevelAttributeConfigurationPanelLayout.setRowFixed(i, true);
		}
		for(int i = 0; i < noCols; i++){
			eventLevelAttributeConfigurationPanelLayout.setColumnFixed(i, true);
		}
		
		eventLevelAttributeConfigurationPanel.setLayout(eventLevelAttributeConfigurationPanelLayout);
		
		eventLevelAttributeCheckBoxList = new ArrayList<JCheckBox>();
		eventLevelRetainRadioButtonList = new ArrayList<JRadioButton>();
		eventLevelRemoveRadioButtonList = new ArrayList<JRadioButton>();
		eventLevelAttributeValueSelectionComponentList = new ArrayList<Component>();
		
		int yPos = 0;
		Map<String, String> eventAttributeTypeMap = logProperties.getEventAttributeTypeMap();
		SortedSet<String> eventAttributeSet = new TreeSet<String>();
		eventAttributeSet.addAll(eventAttributeTypeMap.keySet());
		
		for(String eventAttribute : eventAttributeSet){
			JCheckBox attributeCheckBox = SlickerFactory.instance().createCheckBox(eventAttribute, false);
			attributeCheckBox.setName(eventAttribute);
			eventLevelAttributeCheckBoxList.add(attributeCheckBox);
			
			JRadioButton retainRadioButton = SlickerFactory.instance().createRadioButton("Retain");
			eventLevelRetainRadioButtonList.add(retainRadioButton);
			JRadioButton removeRadioButton = SlickerFactory.instance().createRadioButton("Remove");
			eventLevelRemoveRadioButtonList.add(removeRadioButton);
			ButtonGroup buttonGroup = new ButtonGroup();
			buttonGroup.add(retainRadioButton);
			buttonGroup.add(removeRadioButton);
			
			retainRadioButton.setSelected(true);
			
			Component attributeValueComponent = null;
			if(eventAttributeTypeMap.get(eventAttribute).equals("string") || eventAttributeTypeMap.get(eventAttribute).equals("discrete")){
				attributeValueComponent = prepareLiteralComponent(logProperties.getEventAttributeValueSetMap().get(eventAttribute));
			}else if(eventAttributeTypeMap.get(eventAttribute).equals("continuous")){
				attributeValueComponent = prepareContinuousComponent(logProperties.getEventAttributeMinValueMap().get(eventAttribute),logProperties.getEventAttributeMaxValueMap().get(eventAttribute));
			}else if(eventAttributeTypeMap.get(eventAttribute).equals("date")){
				attributeValueComponent = prepareDateComponent(logProperties.getEventAttributeTimeStampStartMap().get(eventAttribute), logProperties.getEventAttributeTimeStampEndMap().get(eventAttribute));
			}else{
				System.out.println("WRONG Shouldn't be here: Event Attribute TYpe is not found");
			}
			
			eventLevelAttributeValueSelectionComponentList.add(attributeValueComponent);
			
			eventLevelAttributeConfigurationPanelLayout.setPosition(attributeCheckBox,0,yPos);
			eventLevelAttributeConfigurationPanel.add(attributeCheckBox);
			
			Component horizontalStrut1 = Box.createHorizontalStrut(25);
			
			eventLevelAttributeConfigurationPanelLayout.setPosition(horizontalStrut1,1,yPos);
			eventLevelAttributeConfigurationPanel.add(horizontalStrut1);
			
			eventLevelAttributeConfigurationPanelLayout.setPosition(retainRadioButton,2,yPos);
			eventLevelAttributeConfigurationPanel.add(retainRadioButton);
			eventLevelAttributeConfigurationPanelLayout.setPosition(removeRadioButton,3,yPos);
			eventLevelAttributeConfigurationPanel.add(removeRadioButton);
			
			Component horizontalStrut2 = Box.createHorizontalStrut(25);
			
			eventLevelAttributeConfigurationPanelLayout.setPosition(horizontalStrut2,4,yPos);
			eventLevelAttributeConfigurationPanel.add(horizontalStrut2);
			
			eventLevelAttributeConfigurationPanelLayout.setPosition(attributeValueComponent,5,yPos++);
			eventLevelAttributeConfigurationPanel.add(attributeValueComponent);
		}
		
		eventLevelAttributeConfigurationPanel.setVisible(false);
	}
	
	public boolean precondition() {
		return true;
	}

	@SuppressWarnings("unchecked")
	public void readSettings() {
		System.out.println("Reading settings");
		List<JCheckBox> attributeCheckBoxList;
		List<JRadioButton> retainRadioButtonList;
		List<JRadioButton> removeRadioButtonList;
		List<Component> attributeValueSelectionComponentList;
		
		if(filterLevel == FilterLevel.Trace){
			System.out.println("Filter Level : "+FilterLevel.Trace);
			attributeCheckBoxList = traceLevelAttributeCheckBoxList;
			retainRadioButtonList = traceLevelRetainRadioButtonList;
			removeRadioButtonList = traceLevelRemoveRadioButtonList;
			attributeValueSelectionComponentList = traceLevelAttributeValueSelectionComponentList;
		}else{
			System.out.println("Filter Level : "+FilterLevel.Event);
			attributeCheckBoxList = eventLevelAttributeCheckBoxList;
			retainRadioButtonList = eventLevelRetainRadioButtonList;
			removeRadioButtonList = eventLevelRemoveRadioButtonList;
			attributeValueSelectionComponentList = eventLevelAttributeValueSelectionComponentList;
		}
		
		int noItems = attributeCheckBoxList.size();
		System.out.println("No. Items: "+noItems);
		for(int i = 0; i < noItems; i++){
			JCheckBox attributeCheckBox = attributeCheckBoxList.get(i);
			
			String attributeName = attributeCheckBox.getName();
			FilterType filterType=null;
			boolean isLiteral = false;
			boolean isContinuous = false;
			boolean isDate = false;
			Set<String> selectedValueSet=null;
			double minValue=-Double.MIN_VALUE, maxValue=Double.MAX_VALUE;
			Date startDate=null,endDate=null;
			
			if(attributeCheckBox.isSelected()){
				System.out.println(attributeName+" is selected");
				if(retainRadioButtonList.get(i).isSelected())
					filterType = FilterType.Retain;
				if(removeRadioButtonList.get(i).isSelected())
					filterType = FilterType.Remove;
			
				Component attributeValueComponent = attributeValueSelectionComponentList.get(i);
				if(attributeValueComponent instanceof CheckComboBox){
					isLiteral = true;
					selectedValueSet = (Set<String>) ((CheckComboBox) attributeValueComponent).getSelectedItems();
					System.out.println(selectedValueSet);
				}else if(attributeValueComponent instanceof JPanel){
					Component[] panelComponents = ((JPanel)attributeValueComponent).getComponents();
					for(Component component : panelComponents){
						if(component instanceof JTextField){
							isContinuous = true;
							if(component.getName().equalsIgnoreCase("MIN_VALUE")){
								minValue = new Double(((JTextField)component).getText().trim());
							}else if(component.getName().equalsIgnoreCase("MAX_VALUE")){
								maxValue = new Double(((JTextField)component).getText().trim());
							}
						}else if(component instanceof JDateChooser){
							isDate = true;
							if(((JDateChooser)component).getName().equalsIgnoreCase("StartDate")){
								startDate = ((JDateChooser)component).getDate();
							}else if(((JDateChooser)component).getName().equalsIgnoreCase("EndDate")){
								endDate = ((JDateChooser)component).getDate();
							}
						}
					}
				}
				
				if(isLiteral)
					attributeSelectedValueSetMap.put(attributeName, selectedValueSet);
				if(isContinuous){
					attributeMinValueThresholdMap.put(attributeName, minValue);
					attributeMaxValueThresholdMap.put(attributeName, maxValue);
				}
				if(isDate){
					attributeStartDateThresholdMap.put(attributeName, startDate);
					attributeEndDateThresholdMap.put(attributeName, endDate);
				}
				attributeFilterTypeMap.put(attributeName, filterType);
			}
		}
		
		for(String attribute : attributeSelectedValueSetMap.keySet()){
			System.out.println(attribute+" @ "+attributeSelectedValueSetMap.get(attribute));
		}
		for(String attribute : attributeStartDateThresholdMap.keySet())
			System.out.println(attribute+" @ "+attributeStartDateThresholdMap.get(attribute)+" @ "+attributeEndDateThresholdMap.get(attribute));
		for(String attribute : attributeMinValueThresholdMap.keySet())
			System.out.println(attribute+" @ "+attributeMinValueThresholdMap.get(attribute)+" @ "+attributeMaxValueThresholdMap.get(attribute));
		
		System.out.println("Returning from readSettings()");
		listener.setFilterLevel(filterLevel);
		listener.setCombination(combination);
		listener.setAttributeSelectedValueSetMap(attributeSelectedValueSetMap);
		listener.setAttributeFilterTypeMap(attributeFilterTypeMap);
		listener.setAttributeStartDateThresholdMap(attributeStartDateThresholdMap);
		listener.setAttributeEndDateThresholdMap(attributeEndDateThresholdMap);
		listener.setAttributeMinValueThresholdMap(attributeMinValueThresholdMap);
		listener.setAttributeMaxValueThresholdMap(attributeMaxValueThresholdMap);
	}

	public void setListener(LogFilteringSettingsListener listener) {
		this.listener = listener;
	}
	
	protected  String fixedLengthString(String string, int length) {
	    return String.format("%1$"+length+ "s", string);
	}
	
	public static String fillString(char fillChar, int count){
        // creates a string of 'x' repeating characters
        char[] chars = new char[count];
        java.util.Arrays.fill(chars, fillChar);
        return new String(chars);
    }

}
