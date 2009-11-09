package lhpn2sbml.gui;

import lhpn2sbml.parser.*;

import gcm2sbml.gui.*;
//import gcm2sbml.parser.GCMFile;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import biomodelsim.BioSim;

public class VariablesPanel extends JPanel implements ActionListener {

	private String name = "", type = "", selList;

	private PropertyList variablesList;

	private String[] options = { "Ok", "Cancel" };

	private Boolean continuous, integer;

	private LHPNFile lhpn;

	private JPanel initPanel = null;

	private PropertyField initLow = null, initHigh = null, rateLow, rateHigh;

	private JComboBox modeBox, initBox;

	// private JComboBox typeBox, modeBox, initBox;

	private static final String[] types = new String[] { "boolean", "continuous", "discrete" };

	private static final String[] modes = new String[] { "input", "output" };

	private static final String[] booleans = new String[] { "true", "false", "unknown" };

	private HashMap<String, PropertyField> fields = null;
	
	private BioSim biosim;

	public VariablesPanel(String selected, PropertyList variablesList, Boolean boolCont,
			Boolean integer, LHPNFile lhpn, boolean atacs, BioSim biosim) {
		super(new GridLayout(6, 1));
		if (selected != null) {
			String[] array = selected.split(" ");
			this.name = array[0];
		}
		else {
			this.name = selected;
		}
		this.selList = selected;
		this.variablesList = variablesList;
		this.lhpn = lhpn;
		this.continuous = boolCont;
		this.integer = integer;
		this.biosim = biosim;

		fields = new HashMap<String, PropertyField>();

		// ID field
		PropertyField field = new PropertyField(GlobalConstants.ID, "", null, null,
				Utility.ATACSIDstring);
		fields.put(GlobalConstants.ID, field);
		add(field);

		// Type label
		JPanel tempPanel = new JPanel();
		if (continuous) {
			type = types[1];
		}
		else if (integer) {
			type = types[2];
		}
		else {
			type = types[0];
		}
		JLabel tempLabel = new JLabel("Type");
		JLabel typeLabel = new JLabel(type);
		// typeBox = new JComboBox(types);
		// typeBox.setSelectedItem(types[0]);
		// typeBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(typeLabel);
		add(tempPanel);

		// Initial field
		if (continuous || integer) {
			// JOptionPane.showMessageDialog(this, lhpn.isContinuous(selected));
			if (name != null) {
				String initVal = lhpn.getInitialVal(name);
				Pattern pattern = Pattern.compile("\\[([\\w-]+?),([\\w-]+?)\\]");
				Matcher matcher = pattern.matcher(initVal);
				if (matcher.find()) {
					initLow = new PropertyField("Initial Lower Bound", matcher.group(1), null,
							null, Utility.NAMEstring);
					initHigh = new PropertyField("Initial Upper Bound", matcher.group(2), null,
							null, Utility.NAMEstring);
				}
				else {
					initLow = new PropertyField("Initial Lower Bound", initVal, null, null,
							Utility.NAMEstring);
					initHigh = new PropertyField("Initial Upper Bound", "", null, null,
							Utility.NAMEstring);
				}
			}
			else {
				initLow = new PropertyField("Initial Lower Bound", "0", null, null,
						Utility.NAMEstring);
				initHigh = new PropertyField("Initial Upper Bound", "", null, null,
						Utility.NAMEstring);
			}
			fields.put("Initial lower", initLow);
			fields.put("Initial upper", initHigh);
			add(initLow);
			add(initHigh);
		}
		else {
			// JOptionPane.showMessageDialog(this, lhpn.isContinuous(selected));
			initPanel = new JPanel();
			JLabel initLabel = new JLabel("Initial Value");
			initBox = new JComboBox(booleans);
			initBox.setSelectedItem(booleans[1]);
			// initBox.addActionListener(this);
			initPanel.setLayout(new GridLayout(1, 2));
			initPanel.add(initLabel);
			initPanel.add(initBox);
			add(initPanel);
		}

		// Mode field
		if (!continuous && !integer) {
			tempPanel = new JPanel();
			JLabel modeLabel = new JLabel("Mode");
			modeBox = new JComboBox(modes);
			modeBox.setSelectedItem(modes[0]);
			modeBox.addActionListener(this);
			tempPanel.setLayout(new GridLayout(1, 2));
			if (atacs) {
				tempPanel.add(modeLabel);
				tempPanel.add(modeBox);
			}
			add(tempPanel);
		}

		// Initial rate field
		if (continuous) {
			if (name != null) {
				String initRate = lhpn.getInitialRate(name);
				Pattern pattern = Pattern.compile("\\[([\\w-]+?),([\\w-]+?)\\]");
				Matcher matcher = pattern.matcher(initRate);
				if (matcher.find()) {
					rateLow = new PropertyField("Rate Lower Bound", matcher.group(1), null, null,
							Utility.NAMEstring);
					rateHigh = new PropertyField("Rate Upper Bound", matcher.group(2), null, null,
							Utility.NAMEstring);
				}
				else {
					rateLow = new PropertyField("Rate Lower Bound", initRate, null, null,
							Utility.NAMEstring);
					rateHigh = new PropertyField("Rate Upper Bound", "", null, null,
							Utility.NAMEstring);
				}
			}
			else {
				rateLow = new PropertyField("Rate Lower Bound", "0", null, null, Utility.NAMEstring);
				rateHigh = new PropertyField("Rate Upper Bound", "", null, null, Utility.NAMEstring);
			}
			fields.put("Rate lower", rateLow);
			fields.put("Rate upper", rateHigh);
			add(rateLow);
			add(rateHigh);
		}

		String oldName = null;
		if (name != null) {
			oldName = name;
			// Properties prop = lhpn.getVariables().get(selected);
			fields.get(GlobalConstants.ID).setValue(name);
			// System.out.print("after " +
			// fields.get(GlobalConstants.ID).getValue());
			// if (lhpn.isContinuous(selected)) {
			// typeBox.setSelectedItem(types[1]);
			// setType(types[1]);
			// }
			// else {
			// typeBox.setSelectedItem(types[0]);
			// setType(types[0]);
			// }
			if (lhpn.isContinuous(name) || lhpn.isInteger(name)) {
				String initVal = lhpn.getInitialVal(name);
				Pattern pattern = Pattern.compile("\\[([\\w-]+),([\\w-]+)\\]");
				Matcher matcher = pattern.matcher(initVal);
				if (matcher.find()) {
					fields.get("Initial lower").setValue(matcher.group(1));
					fields.get("Initial upper").setValue(matcher.group(2));
				}
				else {
					fields.get("Initial lower").setValue(initVal);
				}
			}
			else {
				HashMap<String, String> inits;
				if (lhpn.isInput(name)) {
					inits = lhpn.getInputs();
					// JOptionPane.showMessageDialog(this,
					// modeBox.getSelectedItem());
				}
				else {
					inits = lhpn.getOutputs();
					// JOptionPane.showMessageDialog(this, inits.toString());
				}
				// JOptionPane.showMessageDialog(this, inits.toString());
				initBox.setSelectedItem(inits.get(name));
			}
			if (!continuous && !integer) {
				if (lhpn.isInput(name)) {
					modeBox.setSelectedItem(modes[0]);
				}
				else {
					modeBox.setSelectedItem(modes[1]);
				}
			}
			if (lhpn.isContinuous(name)) {
				String initRate = lhpn.getInitialRate(name);
				Pattern pattern = Pattern.compile("\\[([\\w-]+),([\\w-]+)\\]");
				Matcher matcher = pattern.matcher(initRate);
				if (matcher.find()) {
					fields.get("Rate lower").setValue(matcher.group(1));
					fields.get("Rate upper").setValue(matcher.group(2));
				}
				else {
					fields.get("Rate lower").setValue(initRate);
				}
				fields.get(GlobalConstants.ID).setValue(name);
				// System.out.print(" " +
				// fields.get(GlobalConstants.ID).getValue());
				// System.out.print(" " + fields.get("Initial
				// rate").getValue());
			}
			// loadProperties(prop);
		}

		// setType(types[0]);
		boolean display = false;
		while (!display) {
			display = openGui(oldName);
		}
	}

	private boolean checkValues() {
		for (PropertyField f : fields.values()) {
			if (!f.isValidValue()) {
				return false;
			}
		}
		return true;
	}

	private boolean openGui(String oldName) {
		int value = JOptionPane.showOptionDialog(biosim.frame(), this, "Variable Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (!checkValues()) {
				return false;
			}
			String[] allVariables = lhpn.getAllIDs();
			if (oldName == null) {
				for (int i = 0; i < allVariables.length; i++) {
					if (allVariables[i] != null) {
						if (allVariables[i].equals(fields.get(GlobalConstants.ID).getValue())) {
							Utility.createErrorMessage("Error", "Variable id already exists.");
							return false;
						}
					}
				}
			}
			else if (!oldName.equals(fields.get(GlobalConstants.ID).getValue())) {
				for (int i = 0; i < allVariables.length; i++) {
					if (allVariables[i].equals(fields.get(GlobalConstants.ID).getValue())) {
						Utility.createErrorMessage("Error", "Variable id already exists.");
						return false;
					}
				}
			}
			// System.out.print("after " +
			// fields.get(GlobalConstants.ID).getValue());
			String id = fields.get(GlobalConstants.ID).getValue();

			// Check to see if we need to add or edit
			Properties property = new Properties();
			for (PropertyField f : fields.values()) {
				if (f.getState() == null || f.getState().equals(PropertyField.states[1])) {
					property.put(f.getKey(), f.getValue());
				}
			}
			String tempVal = "";
			if (property.containsKey("Initial Lower Bound")) {
				tempVal = property.getProperty("Initial Lower Bound");
				if (property.containsKey("Initial Upper Bound")
						&& !property.get("Initial Upper Bound").equals("")) {
					tempVal = "[" + tempVal + "," + property.getProperty("Initial Upper Bound")
							+ "]";
				}
				property.setProperty("value", tempVal);
				property.remove("Initial Lower Bound");
				property.remove("Initial Upper Bound");
			}
			if (property.containsKey("Rate Lower Bound")) {
				tempVal = property.getProperty("Rate Lower Bound");
				if (property.containsKey("Rate Upper Bound")
						&& !property.get("Rate Upper Bound").equals("")) {
					tempVal = "[" + tempVal + "," + property.getProperty("Rate Upper Bound") + "]";
				}
				property.setProperty("rate", tempVal);
				property.remove("Rate Lower Bound");
				property.remove("Rate Upper Bound");
			}

			// property.put(GlobalConstants.TYPE,
			// typeBox.getSelectedItem().toString());

			if (name != null && !oldName.equals(id)) {
				lhpn.changeVariableName(oldName, id);
			}
			if (id.equals(oldName)) {
				lhpn.removeVar(id);
			}
			if (lhpn.isContinuous(id) || continuous) {
				// System.out.println("add var " + property);
				lhpn.addVar(id, property);
				tempVal = fields.get("Initial lower").getValue();
				if (fields.containsKey("Initial upper")
						&& !fields.get("Initial upper").getValue().equals("")) {
					tempVal = "[" + tempVal + "," + fields.get("Initial upper").getValue() + "]";
					//System.out.println(fields.get("Initial upper").getValue());
				}
			}
			else if (lhpn.isInteger(id) || integer) {
				// System.out.println("add var " + property);
				tempVal = fields.get("Initial lower").getValue();
				if (fields.containsKey("Initial upper")
						&& !fields.get("Initial upper").getValue().equals("")) {
					tempVal = "[" + tempVal + "," + fields.get("Initial upper").getValue() + "]";
					//System.out.println(fields.get("Initial upper").getValue());
				}
				lhpn.addInteger(id, tempVal);
			}
			else if (lhpn.isInput(id) || (!continuous && modeBox.getSelectedItem().equals("input"))) {
				// Boolean temp = false;
				// if (initBox.getSelectedItem().equals("true")) {
				// temp = true;
				// }
				lhpn.addInput(id, initBox.getSelectedItem().toString());
			}
			else if (lhpn.isOutput(id)
					|| (!continuous && modeBox.getSelectedItem().equals("output"))) {
				// Boolean temp = false;
				// if (initBox.getSelectedItem().equals("true")) {
				// temp = true;
				// }
				lhpn.addOutput(id, initBox.getSelectedItem().toString());
			}
			variablesList.removeItem(selList);
			String list;
			if (continuous || integer) {
				list = id + " - " + type + " - " + tempVal;
			}
			else {
				list = id + " - " + type + " - " + initBox.getSelectedItem().toString();
			}
			if (continuous) {
				tempVal = fields.get("Rate lower").getValue();
				if (fields.containsKey("Rate upper")
						&& !fields.get("Rate upper").getValue().equals("")) {
					tempVal = "[" + tempVal + "," + fields.get("Rate upper").getValue() + "]";
				}
				list = list + " - " + tempVal;
			}
			variablesList.removeItem(list);
			variablesList.addItem(list);
			variablesList.setSelectedValue(id, true);

		}
		else if (value == JOptionPane.NO_OPTION) {
			// System.out.println();
			return true;
		}
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")) {
			// setType(typeBox.getSelectedItem().toString());
		}
	}

}
