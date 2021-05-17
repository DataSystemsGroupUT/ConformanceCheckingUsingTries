package org.processmining.logfiltering.plugins;

import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logfiltering.algorithms.SelectActivities;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.SamplingType;
import org.processmining.logfiltering.parameters.FilterLevel;
import org.processmining.logfiltering.parameters.FilterSelection;
import org.processmining.logfiltering.parameters.MatrixFilterParameter;
import org.processmining.logfiltering.parameters.SamplingReturnType;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

@Plugin(name = "Activity Selection", parameterLabels = { "Event Log", "Parameter Object" }, returnLabels = {
		"Subset Event Log" }, returnTypes = { XLog.class })
public class ActivitySelectionPlugin {

	@SuppressWarnings("unchecked")
	public class MatrixFilterWizardPanel extends JPanel {
		
		private static final long serialVersionUID = 8572008504104999027L;
		public XEventClassifier[] EventAttribute;
		private FilterLevel[] chooseName = FilterLevel.values();
		private  SamplingType[] SamplingOptions = SamplingType.values();
		private  SamplingReturnType[] ReturnOptions = SamplingReturnType.values();
		private FilterSelection[] KeepOrRemove = FilterSelection.values();
		private final NiceDoubleSlider doubleSlider = SlickerFactory.instance()
				.createNiceDoubleSlider("Please Selecct the Threshold", 0, 1, 0.25, Orientation.HORIZONTAL);
		private NiceIntegerSlider SubsequenceLength= SlickerFactory.instance().createNiceIntegerSlider("Please Select the Percentages of Activities", 0, 100, 75, Orientation.HORIZONTAL);
		private final JComboBox<FilterLevel> comboReturnMethod = SlickerFactory.instance().createComboBox(ReturnOptions);
		private final JComboBox<FilterLevel> comboSamplingMethod = SlickerFactory.instance().createComboBox(SamplingOptions);
		final JLabel comboFilterMethodolabel = new JLabel("Please make a selection...");
		private final JComboBox<FilterSelection> comboFilterSelectionType = SlickerFactory.instance().createComboBox(KeepOrRemove);
		final JLabel comboFilterSelectionTypelabel = new JLabel("Keep/Remove Outliers");
		final JLabel comboAttlabel = new JLabel("Select the Event column");
		final JLabel Policylabel = new JLabel("Select the Sampling Policy (Variant/Trace)");
		final JLabel Selectionlabel = new JLabel("Select the InstanceSelection Strategy");
		JComboBox<XEventClassifier> comboAtt;

		public MatrixFilterWizardPanel(XLog log) {

			EventAttribute = log.getClassifiers().toArray(new XEventClassifier[log.getClassifiers().size()]);
			comboAtt = SlickerFactory.instance().createComboBox(EventAttribute);

			GridLayout layout = new GridLayout(0, 1);
			setLayout(layout);
			add(Policylabel);
			add(comboReturnMethod);
			add(Selectionlabel);
			add(comboSamplingMethod);
			//add(doubleSlider);
			add(SubsequenceLength);
			add(comboAttlabel);
			add(comboAtt);
			//add(comboFilterSelectionTypelabel);
			//add(comboFilterSelectionType);
		}

		public MatrixFilterParameter getParameters() {
			return new MatrixFilterParameter(SubsequenceLength.getValue(),
					(XEventClassifier) comboAtt.getSelectedItem(), (SamplingType) comboSamplingMethod.getSelectedItem(),(SamplingReturnType) comboReturnMethod.getSelectedItem());
		}
	}

	@UITopiaVariant(affiliation = "RWTH Aachen University", author = "Mohammadreza", email = "FaniSani@pads.rwth-aachen.de")
	@PluginVariant(requiredParameterLabels = { 0 })
	public XLog run(UIPluginContext context, XLog log) {


		int temp = 0;

		MatrixFilterWizardPanel configPanel = new MatrixFilterWizardPanel(log);
		if (context.showWizard("Choose Filtering Parameters", true, true, configPanel)
				.equals(InteractionResult.FINISHED)) {
			return run(context, log, configPanel.getParameters());
		} else {
			context.getFutureResult(0).cancel(true);
			return null;
		}
	}

	@UITopiaVariant(affiliation = "RWTH Aachen University", author = "Mohammadreza", email = "FaniSani@pads.rwth-aachen.de")
	@PluginVariant(requiredParameterLabels = {0})
	public static XLog run(PluginContext context, XLog log) {
		return run(context, log, new MatrixFilterParameter());
	}

	@PluginVariant(requiredParameterLabels = {0, 1 })
	public static XLog run(PluginContext context, XLog log, MatrixFilterParameter parameters) {
		return SelectActivities.apply(log, parameters);
	}

}
