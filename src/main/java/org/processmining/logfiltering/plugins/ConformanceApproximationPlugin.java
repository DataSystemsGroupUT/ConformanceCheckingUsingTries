package org.processmining.logfiltering.plugins;

import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logfiltering.algorithms.ProtoTypeSelectionAlgo;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.PrototypeType;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.SimilarityMeasure;
import org.processmining.logfiltering.parameters.FilterLevel;
import org.processmining.logfiltering.parameters.FilterSelection;
import org.processmining.logfiltering.parameters.MatrixFilterParameter;
import org.processmining.logfiltering.parameters.SamplingReturnType;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;



@Plugin(name = "Conformance Approximation", parameterLabels = { "Event Log","Petri net", "Parameter Object" }, returnLabels = {
		"Conf Approximation" }, returnTypes = { String.class })
public class ConformanceApproximationPlugin {

	@SuppressWarnings("unchecked")
public class MatrixFilterWizardPanel extends JPanel {
		
		
		private static final long serialVersionUID = 8572008504104999027L;
		private  SamplingReturnType[] ReturnOptions = SamplingReturnType.values();
		private PrototypeType[] PrototypeTypes = PrototypeType.values();
		public XEventClassifier[] EventAttribute;
		private FilterLevel[] chooseName = FilterLevel.values();
		private  SimilarityMeasure[] SimilarityMeasures = SimilarityMeasure.values();
		
		private final NiceDoubleSlider doubleSlider = SlickerFactory.instance()
				.createNiceDoubleSlider("Please Selecct the Threshold", 0, 1, 0.25, Orientation.HORIZONTAL);
		private NiceIntegerSlider SubsequenceLength= SlickerFactory.instance().createNiceIntegerSlider("Please Select the Percentages of Variants", 0, 100, 20, Orientation.HORIZONTAL);
		private final JComboBox<FilterLevel> comboReturnMethod = SlickerFactory.instance().createComboBox(ReturnOptions);
		private final JComboBox<SimilarityMeasure> comboSamplingMethod = SlickerFactory.instance().createComboBox(SimilarityMeasures);
		final JLabel comboFilterMethodolabel = new JLabel("Please make a selection...");
		private final JComboBox<FilterSelection> comboProtoTypeSelectionType = SlickerFactory.instance().createComboBox(PrototypeTypes);
		final JLabel comboProtoTypeSelectionTypelabel = new JLabel("Please Select How increment the alignment");
		final JLabel comboAttlabel = new JLabel("Select the Event column");
		
		JComboBox<XEventClassifier> comboAtt;

		public MatrixFilterWizardPanel(XLog log) {

			EventAttribute = log.getClassifiers().toArray(new XEventClassifier[log.getClassifiers().size()]);
			comboAtt = SlickerFactory.instance().createComboBox(EventAttribute);

			GridLayout layout = new GridLayout(0, 1);
			setLayout(layout);
			add(comboProtoTypeSelectionTypelabel);
			add(comboProtoTypeSelectionType);
			
			//add(comboReturnMethod);
			//add(comboSamplingMethod);
			//add(doubleSlider);
			add(SubsequenceLength);
			add(comboAttlabel);
			add(comboAtt);
			add(comboProtoTypeSelectionType);
			add(comboProtoTypeSelectionTypelabel);
			add(comboSamplingMethod);
		}

		public MatrixFilterParameter getParameters() {
			return new MatrixFilterParameter(SubsequenceLength.getValue(),
					(XEventClassifier) comboAtt.getSelectedItem(), (SimilarityMeasure) comboSamplingMethod.getSelectedItem(),(SamplingReturnType) comboReturnMethod.getSelectedItem(),(PrototypeType) comboProtoTypeSelectionType.getSelectedItem());
		}
		
	}

	@UITopiaVariant(affiliation = "RWTH Aachen University", author = "Mohammadreza", email = "FaniSani@pads.rwth-aachen.de")
	@PluginVariant(requiredParameterLabels = { 0 ,1})
	public String run(UIPluginContext context, XLog log, Petrinet net) {
//
	//	TransEvClassMapping mapping = constructTransEvMapping(context, log, net);
		XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
		XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;
		TransEvClassMapping mapping = constructMapping(net, log, dummyEvClass, eventClassifier);
		

		MatrixFilterWizardPanel configPanel = new MatrixFilterWizardPanel(log);
		
		if (context.showWizard("Choose Filtering Parameters", true, true, configPanel)
				.equals(InteractionResult.FINISHED)) {
			
			return run(context, log,net,mapping, configPanel.getParameters());
		} else {
			context.getFutureResult(0).cancel(true);
			return null;
		}
	}

	@UITopiaVariant(affiliation = "RWTH Aachen University", author = "Mohammadreza", email = "FaniSani@pads.rwth-aachen.de")
	@PluginVariant(requiredParameterLabels = {0,1})
	public static String run(PluginContext context, XLog log,PetrinetGraph net, MatrixFilterParameter matrixFilterParameter) {
		return run(context, log,net, new MatrixFilterParameter());
	}

	

	@PluginVariant(requiredParameterLabels = {0, 1 })
	public static String run(PluginContext context, XLog log,Petrinet net,TransEvClassMapping mapping, MatrixFilterParameter parameters) {
		XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
		XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;
		TransEvClassMapping mapping2 = constructMapping2(net, log, dummyEvClass, eventClassifier);
		
		if(parameters.getPrototypeType()!=PrototypeType.Sampling) {
		return ProtoTypeSelectionAlgo.apply(log,net, parameters,mapping2);
		}else {
			return ProtoTypeSelectionAlgo.apply2(context, log,net, parameters,mapping2);
		}
		
	}
	
	
	
	



private static TransEvClassMapping constructMapping2(Petrinet net, XLog log, XEventClass dummyEvClass,
			XEventClassifier eventClassifier) {
	TransEvClassMapping mapping = new TransEvClassMapping(eventClassifier, dummyEvClass);

	XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);

	for (Transition t : net.getTransitions()) {
		boolean mapped = false;

		for (XEventClass evClass : summary.getEventClasses().getClasses()) {
			String id = evClass.getId();
			
			String label = t.getLabel().concat("+complete");
			String label2= t.getLabel();
			
			if (label.equals(id)|| label2.equals(id)) {
				mapping.put(t, evClass);
				mapped = true;
				break;
			}
		}
	}
	System.out.println("mapping");
	System.out.println(mapping);

	return mapping;
	}

private static TransEvClassMapping constructMapping(PetrinetGraph net, XLog log, XEventClass dummyEvClass,
		XEventClassifier eventClassifier) {
	TransEvClassMapping mapping = new TransEvClassMapping(eventClassifier, dummyEvClass);

	XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);

	for (Transition t : net.getTransitions()) {
		boolean mapped = false;

		for (XEventClass evClass : summary.getEventClasses().getClasses()) {
			String id = evClass.getId();
			
			String label = t.getLabel();
											
			if (label.equals(id)) {
				mapping.put(t, evClass);
				mapped = true;
				break;
			}
		}
	}
	System.out.println("mapping");
	System.out.println(mapping);

	return mapping;
	}
}