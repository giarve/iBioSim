package biomodel.gui.textualeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import main.Gui;
import main.util.MutableBoolean;
import main.util.Utility;

import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.FunctionDefinition;
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.sbml.libsbml.Unit;
import org.sbml.libsbml.UnitDefinition;
import org.sbml.libsbml.libsbml;


/**
 * This is a class for creating SBML parameters
 * 
 * @author Chris Myers
 * 
 */
public class Reactions extends JPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private JComboBox stoiciLabel;

	private JComboBox reactionComp; // compartment combo box

	private JList reactions; // JList of reactions

	private String[] reacts; // array of reactions

	/*
	 * reactions buttons
	 */
	private JButton addReac, removeReac, editReac;

	private JList reacParameters; // JList of reaction parameters

	private String[] reacParams; // array of reaction parameters

	/*
	 * reaction parameters buttons
	 */
	private JButton reacAddParam, reacRemoveParam, reacEditParam;

	private ArrayList<Parameter> changedParameters; // ArrayList of parameters

	/*
	 * reaction parameters text fields
	 */
	private JTextField reacParamID, reacParamValue, reacParamName;

	private JComboBox reacParamUnits;

	private JTextField reacID, reacName; // reaction name and id text

	// fields

	private JComboBox reacReverse, reacFast; // reaction reversible, fast combo

	// boxes

	/*
	 * reactant buttons
	 */
	private JButton addReactant, removeReactant, editReactant;

	private JList reactants; // JList for reactants

	private String[] reacta; // array for reactants

	private JComboBox reactantConstant;
	/*
	 * ArrayList of reactants
	 */
	private ArrayList<SpeciesReference> changedReactants;

	/*
	 * product buttons
	 */
	private JButton addProduct, removeProduct, editProduct;

	private JList products; // JList for products

	private String[] proda; // array for products

	private JComboBox productConstant;
	/*
	 * ArrayList of products
	 */
	private ArrayList<SpeciesReference> changedProducts;

	/*
	 * modifier buttons
	 */
	private JButton addModifier, removeModifier, editModifier;

	private JList modifiers; // JList for modifiers

	private String[] modifier; // array for modifiers

	/*
	 * ArrayList of modifiers
	 */
	private ArrayList<ModifierSpeciesReference> changedModifiers;

	private JComboBox productSpecies; // ComboBox for product editing

	private JComboBox modifierSpecies; // ComboBox for modifier editing

	private JTextField productId;

	private JTextField productName;

	private JTextField productStoiciometry; // text field for editing products

	private JComboBox reactantSpecies; // ComboBox for reactant editing

	/*
	 * text field for editing reactants
	 */
	private JTextField reactantId;

	private JTextField reactantName;

	private JTextField reactantStoiciometry;

	private JTextArea kineticLaw; // text area for editing kinetic law

	private ArrayList<String> thisReactionParams;

	private JButton useMassAction, clearKineticLaw;

	private SBMLDocument document;

	private ArrayList<String> usedIDs;

	private MutableBoolean dirty;

	private Boolean paramsOnly;

	private String file;

	private ArrayList<String> parameterChanges;

	private Gui biosim;

	private InitialAssignments initialsPanel;

	private Rules rulesPanel;
	
	private String selectedReaction;

	public Reactions(Gui biosim, SBMLDocument document, ArrayList<String> usedIDs, MutableBoolean dirty, Boolean paramsOnly,
			ArrayList<String> getParams, String file, ArrayList<String> parameterChanges) {
		super(new BorderLayout());
		this.document = document;
		this.usedIDs = usedIDs;
		this.biosim = biosim;
		this.dirty = dirty;
		this.paramsOnly = paramsOnly;
		this.file = file;
		this.parameterChanges = parameterChanges;
		Model model = document.getModel();
		JPanel addReacs = new JPanel();
		addReac = new JButton("Add Reaction");
		removeReac = new JButton("Remove Reaction");
		editReac = new JButton("Edit Reaction");
		addReacs.add(addReac);
		addReacs.add(removeReac);
		addReacs.add(editReac);
		addReac.addActionListener(this);
		removeReac.addActionListener(this);
		editReac.addActionListener(this);
		if (paramsOnly) {
			addReac.setEnabled(false);
			removeReac.setEnabled(false);
		}
		JLabel reactionsLabel = new JLabel("List of Reactions:");
		reactions = new JList();
		reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll2 = new JScrollPane();
		scroll2.setViewportView(reactions);
		ListOf listOfReactions = model.getListOfReactions();
		reacts = new String[(int) model.getNumReactions()];
		for (int i = 0; i < model.getNumReactions(); i++) {
			Reaction reaction = (Reaction) listOfReactions.get(i);
			reacts[i] = reaction.getId();
			if (paramsOnly) {
				ListOf params = reaction.getKineticLaw().getListOfParameters();
				for (int j = 0; j < reaction.getKineticLaw().getNumParameters(); j++) {
					Parameter paramet = ((Parameter) (params.get(j)));
					for (int k = 0; k < getParams.size(); k++) {
						if (getParams.get(k).split(" ")[0].equals(reaction.getId() + "/" + paramet.getId())) {
							parameterChanges.add(getParams.get(k));
							String[] splits = getParams.get(k).split(" ");
							if (splits[splits.length - 2].equals("Modified") || splits[splits.length - 2].equals("Custom")) {
								String value = splits[splits.length - 1];
								paramet.setValue(Double.parseDouble(value));
							}
							else if (splits[splits.length - 2].equals("Sweep")) {
								String value = splits[splits.length - 1];
								paramet.setValue(Double.parseDouble(value.split(",")[0].substring(1).trim()));
							}
							if (!reacts[i].contains("Modified")) {
								reacts[i] += " Modified";
							}
						}
					}
				}
			}
		}
		Utility.sort(reacts);
		reactions.setListData(reacts);
		reactions.setSelectedIndex(0);
		reactions.addMouseListener(this);
		this.add(reactionsLabel, "North");
		this.add(scroll2, "Center");
		this.add(addReacs, "South");
	}

	/**
	 * Creates a frame used to edit reactions or create new ones.
	 */
	public void reactionsEditor(SBMLDocument document, String option, String reactionId, boolean inSchematic) {
		/*
		 * if (option.equals("OK") && reactions.getSelectedIndex() == -1) {
		 * JOptionPane.showMessageDialog(Gui.frame, "No reaction selected.",
		 * "Must Select A Reaction", JOptionPane.ERROR_MESSAGE); return; }
		 */
		selectedReaction = reactionId;
		JLabel id = new JLabel("ID:");
		reacID = new JTextField(15);
		JLabel name = new JLabel("Name:");
		if (document.getLevel() < 3) {
			reacName = new JTextField(50);
		}
		else {
			reacName = new JTextField(30);
		}
		JLabel reactionCompLabel = new JLabel("Compartment:");
		ListOf listOfCompartments = document.getModel().getListOfCompartments();
		String[] addC = new String[(int) document.getModel().getNumCompartments()];
		for (int i = 0; i < document.getModel().getNumCompartments(); i++) {
			addC[i] = ((Compartment) listOfCompartments.get(i)).getId();
		}
		reactionComp = new JComboBox(addC);
		JLabel reverse = new JLabel("Reversible:");
		String[] options = { "true", "false" };
		reacReverse = new JComboBox(options);
		reacReverse.setSelectedItem("false");
		JLabel fast = new JLabel("Fast:");
		reacFast = new JComboBox(options);
		reacFast.setSelectedItem("false");
		String selectedID = "";
		Reaction copyReact = null;
		if (option.equals("OK")) {
			Reaction reac = document.getModel().getReaction(reactionId);
			copyReact = (Reaction) reac.cloneObject();
			reacID.setText(reac.getId());
			selectedID = reac.getId();
			reacName.setText(reac.getName());
			if (reac.getReversible()) {
				reacReverse.setSelectedItem("true");
			}
			else {
				reacReverse.setSelectedItem("false");
			}
			if (reac.getFast()) {
				reacFast.setSelectedItem("true");
			}
			else {
				reacFast.setSelectedItem("false");
			}
			if (document.getLevel() > 2) {
				reactionComp.setSelectedItem(reac.getCompartment());
			}
		}
		else {
			String NEWreactionId = "r0";
			int i = 0;
			while (usedIDs.contains(NEWreactionId)) {
				i++;
				NEWreactionId = "r" + i;
			}
			reacID.setText(NEWreactionId);
		}
		JPanel param = new JPanel(new BorderLayout());
		JPanel addParams = new JPanel();
		reacAddParam = new JButton("Add Parameter");
		reacRemoveParam = new JButton("Remove Parameter");
		reacEditParam = new JButton("Edit Parameter");
		addParams.add(reacAddParam);
		addParams.add(reacRemoveParam);
		addParams.add(reacEditParam);
		reacAddParam.addActionListener(this);
		reacRemoveParam.addActionListener(this);
		reacEditParam.addActionListener(this);
		JLabel parametersLabel = new JLabel("List Of Local Parameters:");
		reacParameters = new JList();
		reacParameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 220));
		scroll.setPreferredSize(new Dimension(276, 152));
		scroll.setViewportView(reacParameters);
		reacParams = new String[0];
		changedParameters = new ArrayList<Parameter>();
		thisReactionParams = new ArrayList<String>();
		if (option.equals("OK")) {
			Reaction reac = document.getModel().getReaction(reactionId);
			ListOf listOfParameters = reac.getKineticLaw().getListOfParameters();
			reacParams = new String[(int) reac.getKineticLaw().getNumParameters()];
			for (int i = 0; i < reac.getKineticLaw().getNumParameters(); i++) {
				/*
				 * This code is a hack to get around a local parameter
				 * conversion bug in libsbml
				 */
				Parameter pp = (Parameter) listOfParameters.get(i);
				Parameter parameter = new Parameter(document.getLevel(), document.getVersion());
				parameter.setId(pp.getId());
				parameter.setName(pp.getName());
				parameter.setValue(pp.getValue());
				parameter.setUnits(pp.getUnits());

				changedParameters.add(parameter);
				thisReactionParams.add(parameter.getId());
				String p;
				if (parameter.isSetUnits()) {
					p = parameter.getId() + " " + parameter.getValue() + " " + parameter.getUnits();
				}
				else {
					p = parameter.getId() + " " + parameter.getValue();
				}
				if (paramsOnly) {
					for (int j = 0; j < parameterChanges.size(); j++) {
						if (parameterChanges.get(j).split(" ")[0].equals(selectedReaction + "/"	+ parameter.getId())) {
							p = parameterChanges.get(j).split("/")[1];
						}
					}
				}
				reacParams[i] = p;
			}
		}
		else {
			// Parameter p = new Parameter(BioSim.SBML_LEVEL,
			// BioSim.SBML_VERSION);
			Parameter p = new Parameter(document.getLevel(), document.getVersion());
			p.setId("kf");
			p.setValue(0.1);
			changedParameters.add(p);
			// p = new Parameter(BioSim.SBML_LEVEL, BioSim.SBML_VERSION);
			p = new Parameter(document.getLevel(), document.getVersion());
			p.setId("kr");
			p.setValue(1.0);
			changedParameters.add(p);
			reacParams = new String[2];
			reacParams[0] = "kf 0.1";
			reacParams[1] = "kr 1.0";
			thisReactionParams.add("kf");
			thisReactionParams.add("kr");
		}
		Utility.sort(reacParams);
		reacParameters.setListData(reacParams);
		reacParameters.setSelectedIndex(0);
		reacParameters.addMouseListener(this);
		param.add(parametersLabel, "North");
		param.add(scroll, "Center");
		param.add(addParams, "South");

		JPanel reactantsPanel = new JPanel(new BorderLayout());
		JPanel addReactants = new JPanel();
		addReactant = new JButton("Add Reactant");
		removeReactant = new JButton("Remove Reactant");
		editReactant = new JButton("Edit Reactant");
		addReactants.add(addReactant);
		addReactants.add(removeReactant);
		addReactants.add(editReactant);
		addReactant.addActionListener(this);
		removeReactant.addActionListener(this);
		editReactant.addActionListener(this);
		JLabel reactantsLabel = new JLabel("List Of Reactants:");
		reactants = new JList();
		reactants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll2 = new JScrollPane();
		scroll2.setMinimumSize(new Dimension(260, 220));
		scroll2.setPreferredSize(new Dimension(276, 152));
		scroll2.setViewportView(reactants);
		reacta = new String[0];
		changedReactants = new ArrayList<SpeciesReference>();
		if (option.equals("OK")) {
			Reaction reac = document.getModel().getReaction(reactionId);
			ListOf listOfReactants = reac.getListOfReactants();
			reacta = new String[(int) reac.getNumReactants()];
			for (int i = 0; i < reac.getNumReactants(); i++) {
				SpeciesReference reactant = (SpeciesReference) listOfReactants.get(i);
				changedReactants.add(reactant);
				if (reactant.isSetStoichiometryMath()) {
					reacta[i] = reactant.getSpecies() + " " + SBMLutilities.myFormulaToString(reactant.getStoichiometryMath().getMath());
				}
				else {
					reacta[i] = reactant.getSpecies() + " " + reactant.getStoichiometry();
				}
			}
		}
		Utility.sort(reacta);
		reactants.setListData(reacta);
		reactants.setSelectedIndex(0);
		reactants.addMouseListener(this);
		reactantsPanel.add(reactantsLabel, "North");
		reactantsPanel.add(scroll2, "Center");
		reactantsPanel.add(addReactants, "South");

		JPanel productsPanel = new JPanel(new BorderLayout());
		JPanel addProducts = new JPanel();
		addProduct = new JButton("Add Product");
		removeProduct = new JButton("Remove Product");
		editProduct = new JButton("Edit Product");
		addProducts.add(addProduct);
		addProducts.add(removeProduct);
		addProducts.add(editProduct);
		addProduct.addActionListener(this);
		removeProduct.addActionListener(this);
		editProduct.addActionListener(this);
		JLabel productsLabel = new JLabel("List Of Products:");
		products = new JList();
		products.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll3 = new JScrollPane();
		scroll3.setMinimumSize(new Dimension(260, 220));
		scroll3.setPreferredSize(new Dimension(276, 152));
		scroll3.setViewportView(products);
		proda = new String[0];
		changedProducts = new ArrayList<SpeciesReference>();
		if (option.equals("OK")) {
			Reaction reac = document.getModel().getReaction(reactionId);
			ListOf listOfProducts = reac.getListOfProducts();
			proda = new String[(int) reac.getNumProducts()];
			for (int i = 0; i < reac.getNumProducts(); i++) {
				SpeciesReference product = (SpeciesReference) listOfProducts.get(i);
				changedProducts.add(product);
				if (product.isSetStoichiometryMath()) {
					this.proda[i] = product.getSpecies() + " " + SBMLutilities.myFormulaToString(product.getStoichiometryMath().getMath());
				}
				else {
					this.proda[i] = product.getSpecies() + " " + product.getStoichiometry();
				}
			}
		}
		Utility.sort(proda);
		products.setListData(proda);
		products.setSelectedIndex(0);
		products.addMouseListener(this);
		productsPanel.add(productsLabel, "North");
		productsPanel.add(scroll3, "Center");
		productsPanel.add(addProducts, "South");

		JPanel modifierPanel = new JPanel(new BorderLayout());
		JPanel addModifiers = new JPanel();
		addModifier = new JButton("Add Modifier");
		removeModifier = new JButton("Remove Modifier");
		editModifier = new JButton("Edit Modifier");
		addModifiers.add(addModifier);
		addModifiers.add(removeModifier);
		addModifiers.add(editModifier);
		addModifier.addActionListener(this);
		removeModifier.addActionListener(this);
		editModifier.addActionListener(this);
		JLabel modifiersLabel = new JLabel("List Of Modifiers:");
		modifiers = new JList();
		modifiers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll5 = new JScrollPane();
		scroll5.setMinimumSize(new Dimension(260, 220));
		scroll5.setPreferredSize(new Dimension(276, 152));
		scroll5.setViewportView(modifiers);
		modifier = new String[0];
		changedModifiers = new ArrayList<ModifierSpeciesReference>();
		if (option.equals("OK")) {
			Reaction reac = document.getModel().getReaction(reactionId);
			ListOf listOfModifiers = reac.getListOfModifiers();
			modifier = new String[(int) reac.getNumModifiers()];
			for (int i = 0; i < reac.getNumModifiers(); i++) {
				ModifierSpeciesReference modifier = (ModifierSpeciesReference) listOfModifiers.get(i);
				changedModifiers.add(modifier);
				this.modifier[i] = modifier.getSpecies();
			}
		}
		Utility.sort(modifier);
		modifiers.setListData(modifier);
		modifiers.setSelectedIndex(0);
		modifiers.addMouseListener(this);
		modifierPanel.add(modifiersLabel, "North");
		modifierPanel.add(scroll5, "Center");
		modifierPanel.add(addModifiers, "South");

		JLabel kineticLabel = new JLabel("Kinetic Law:");
		kineticLaw = new JTextArea();
		kineticLaw.setLineWrap(true);
		kineticLaw.setWrapStyleWord(true);
		useMassAction = new JButton("Use Mass Action");
		clearKineticLaw = new JButton("Clear");
		useMassAction.addActionListener(this);
		clearKineticLaw.addActionListener(this);
		JPanel kineticButtons = new JPanel();
		kineticButtons.add(useMassAction);
		kineticButtons.add(clearKineticLaw);
		JScrollPane scroll4 = new JScrollPane();
		scroll4.setMinimumSize(new Dimension(100, 100));
		scroll4.setPreferredSize(new Dimension(100, 100));
		scroll4.setViewportView(kineticLaw);
		if (option.equals("OK")) {
			kineticLaw.setText(SBMLutilities.myFormulaToString(document.getModel().getReaction(reactionId).getKineticLaw().getMath()));
		}
		JPanel kineticPanel = new JPanel(new BorderLayout());
		kineticPanel.add(kineticLabel, "North");
		kineticPanel.add(scroll4, "Center");
		kineticPanel.add(kineticButtons, "South");
		JPanel reactionPanel = new JPanel(new BorderLayout());
		if (inSchematic) {
			JPanel reactionPanelNorth = new JPanel(new GridLayout(2, 1));
			JPanel reactionPanelNorth1 = new JPanel();
			JPanel reactionPanelNorth1b = new JPanel();
			reactionPanelNorth1.add(id);
			reactionPanelNorth1.add(reacID);
			reactionPanelNorth1.add(name);
			reactionPanelNorth1.add(reacName);
			if (document.getLevel() > 2) {
				reactionPanelNorth1b.add(reactionCompLabel);
				reactionPanelNorth1b.add(reactionComp);
			}
			reactionPanelNorth1b.add(reverse);
			reactionPanelNorth1b.add(reacReverse);
			reactionPanelNorth1b.add(fast);
			reactionPanelNorth1b.add(reacFast);
			reactionPanelNorth.add(reactionPanelNorth1);
			reactionPanelNorth.add(reactionPanelNorth1b);
			reactionPanel.add(reactionPanelNorth, "North");
			reactionPanel.add(param, "Center");
			reactionPanel.add(kineticPanel, "South");
		}
		else {
			JPanel reactionPanelNorth = new JPanel();
			JPanel reactionPanelNorth1 = new JPanel();
			JPanel reactionPanelNorth1b = new JPanel();
			JPanel reactionPanelNorth2 = new JPanel();
			JPanel reactionPanelCentral = new JPanel(new GridLayout(1, 3));
			JPanel reactionPanelSouth = new JPanel(new GridLayout(1, 2));
			reactionPanelNorth1.add(id);
			reactionPanelNorth1.add(reacID);
			reactionPanelNorth1.add(name);
			reactionPanelNorth1.add(reacName);
			if (document.getLevel() > 2) {
				reactionPanelNorth1.add(reactionCompLabel);
				reactionPanelNorth1.add(reactionComp);
			}
			reactionPanelNorth1b.add(reverse);
			reactionPanelNorth1b.add(reacReverse);
			reactionPanelNorth1b.add(fast);
			reactionPanelNorth1b.add(reacFast);
			reactionPanelNorth2.add(reactionPanelNorth1);
			reactionPanelNorth2.add(reactionPanelNorth1b);
			reactionPanelNorth.add(reactionPanelNorth2);
			reactionPanelCentral.add(reactantsPanel);
			reactionPanelCentral.add(productsPanel);
			reactionPanelCentral.add(modifierPanel);
			reactionPanelSouth.add(param);
			reactionPanelSouth.add(kineticPanel);
			reactionPanel.add(reactionPanelNorth, "North");
			reactionPanel.add(reactionPanelCentral, "Center");
			reactionPanel.add(reactionPanelSouth, "South");
		}
		if (paramsOnly) {
			reacID.setEditable(false);
			reacName.setEditable(false);
			reacReverse.setEnabled(false);
			reacFast.setEnabled(false);
			reacAddParam.setEnabled(false);
			reacRemoveParam.setEnabled(false);
			addReactant.setEnabled(false);
			removeReactant.setEnabled(false);
			editReactant.setEnabled(false);
			addProduct.setEnabled(false);
			removeProduct.setEnabled(false);
			editProduct.setEnabled(false);
			addModifier.setEnabled(false);
			removeModifier.setEnabled(false);
			editModifier.setEnabled(false);
			kineticLaw.setEditable(false);
			useMassAction.setEnabled(false);
			clearKineticLaw.setEnabled(false);
			reactionComp.setEnabled(false);
		}
		Object[] options1 = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, reactionPanel, "Reaction Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, options1, options1[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			String reac = reacID.getText().trim();
			error = SBMLutilities.checkID(document, usedIDs, reac, selectedID, false);
			if (!error) {
				if (kineticLaw.getText().trim().equals("")) {
					JOptionPane.showMessageDialog(Gui.frame, "A reaction must have a kinetic law.", "Enter A Kinetic Law", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else if ((changedReactants.size() == 0) && (changedProducts.size() == 0)) {
					JOptionPane.showMessageDialog(Gui.frame, "A reaction must have at least one reactant or product.", "No Reactants or Products",
							JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else if (SBMLutilities.myParseFormula(kineticLaw.getText().trim()) == null) {
					JOptionPane.showMessageDialog(Gui.frame, "Unable to parse kinetic law.", "Kinetic Law Error", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else {
					ArrayList<String> invalidKineticVars = getInvalidVariablesInReaction(kineticLaw.getText().trim(), true, "", false);
					if (invalidKineticVars.size() > 0) {
						String invalid = "";
						for (int i = 0; i < invalidKineticVars.size(); i++) {
							if (i == invalidKineticVars.size() - 1) {
								invalid += invalidKineticVars.get(i);
							}
							else {
								invalid += invalidKineticVars.get(i) + "\n";
							}
						}
						String message;
						message = "Kinetic law contains unknown variables.\n\n" + "Unknown variables:\n" + invalid;
						JTextArea messageArea = new JTextArea(message);
						messageArea.setLineWrap(true);
						messageArea.setWrapStyleWord(true);
						messageArea.setEditable(false);
						JScrollPane scrolls = new JScrollPane();
						scrolls.setMinimumSize(new Dimension(300, 300));
						scrolls.setPreferredSize(new Dimension(300, 300));
						scrolls.setViewportView(messageArea);
						JOptionPane.showMessageDialog(Gui.frame, scrolls, "Kinetic Law Error", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					if (!error) {
						error = SBMLutilities.checkNumFunctionArguments(document, SBMLutilities.myParseFormula(kineticLaw.getText().trim()));
					}
				}
			}
			if (!error) {
				if (SBMLutilities.myParseFormula(kineticLaw.getText().trim()).isBoolean()) {
					JOptionPane.showMessageDialog(Gui.frame, "Kinetic law must evaluate to a number.", "Number Expected", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
			}
			if (!error) {
				if (option.equals("OK")) {
					int index = reactions.getSelectedIndex();
					String val = reactionId;
					Reaction react = document.getModel().getReaction(val);
					ListOf remove;
					long size;
					remove = react.getKineticLaw().getListOfParameters();
					size = react.getKineticLaw().getNumParameters();
					for (int i = 0; i < size; i++) {
						remove.remove(0);
					}
					for (int i = 0; i < changedParameters.size(); i++) {
						react.getKineticLaw().addParameter(changedParameters.get(i));
					}
					remove = react.getListOfProducts();
					size = react.getNumProducts();
					for (int i = 0; i < size; i++) {
						remove.remove(0);
					}
					for (int i = 0; i < changedProducts.size(); i++) {
						react.addProduct(changedProducts.get(i));
					}
					remove = react.getListOfModifiers();
					size = react.getNumModifiers();
					for (int i = 0; i < size; i++) {
						remove.remove(0);
					}
					for (int i = 0; i < changedModifiers.size(); i++) {
						react.addModifier(changedModifiers.get(i));
					}
					remove = react.getListOfReactants();
					size = react.getNumReactants();
					for (int i = 0; i < size; i++) {
						remove.remove(0);
					}
					for (int i = 0; i < changedReactants.size(); i++) {
						react.addReactant(changedReactants.get(i));
					}
					if (reacReverse.getSelectedItem().equals("true")) {
						react.setReversible(true);
					}
					else {
						react.setReversible(false);
					}
					if (document.getLevel() > 2) {
						react.setCompartment((String) reactionComp.getSelectedItem());
					}
					if (reacFast.getSelectedItem().equals("true")) {
						react.setFast(true);
					}
					else {
						react.setFast(false);
					}
					react.setId(reacID.getText().trim());
					react.setName(reacName.getText().trim());
					react.getKineticLaw().setMath(SBMLutilities.myParseFormula(kineticLaw.getText().trim()));
					error = checkKineticLawUnits(react.getKineticLaw());
					if (!error) {
						error = SBMLutilities.checkCycles(document);
						if (error) {
							JOptionPane.showMessageDialog(Gui.frame, "Cycle detected within initial assignments, assignment rules, and rate laws.",
									"Cycle Detected", JOptionPane.ERROR_MESSAGE);
						}
					}
					if (!error) {
						for (int i = 0; i < usedIDs.size(); i++) {
							if (usedIDs.get(i).equals(val)) {
								usedIDs.set(i, reacID.getText().trim());
							}
						}
						if (index >= 0) {
							if (!paramsOnly) {
								reacts[index] = reac;
							}
							reactions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
							reacts = Utility.getList(reacts, reactions);
							reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							Utility.sort(reacts);
							reactions.setListData(reacts);
							reactions.setSelectedIndex(index);
						}
					}
					else {
						changedParameters = new ArrayList<Parameter>();
						ListOf listOfParameters = react.getKineticLaw().getListOfParameters();
						for (int i = 0; i < react.getKineticLaw().getNumParameters(); i++) {
							Parameter parameter = (Parameter) listOfParameters.get(i);
							changedParameters.add(parameter);
						}
						changedProducts = new ArrayList<SpeciesReference>();
						ListOf listOfProducts = react.getListOfProducts();
						for (int i = 0; i < react.getNumProducts(); i++) {
							SpeciesReference product = (SpeciesReference) listOfProducts.get(i);
							changedProducts.add(product);
						}
						changedReactants = new ArrayList<SpeciesReference>();
						ListOf listOfReactants = react.getListOfReactants();
						for (int i = 0; i < react.getNumReactants(); i++) {
							SpeciesReference reactant = (SpeciesReference) listOfReactants.get(i);
							changedReactants.add(reactant);
						}
						changedModifiers = new ArrayList<ModifierSpeciesReference>();
						ListOf listOfModifiers = react.getListOfModifiers();
						for (int i = 0; i < react.getNumModifiers(); i++) {
							ModifierSpeciesReference modifier = (ModifierSpeciesReference) listOfModifiers.get(i);
							changedModifiers.add(modifier);
						}
					}
				}
				else {
					Reaction react = document.getModel().createReaction();
					react.createKineticLaw();
					int index = reactions.getSelectedIndex();
					for (int i = 0; i < changedParameters.size(); i++) {
						react.getKineticLaw().addParameter(changedParameters.get(i));
					}
					for (int i = 0; i < changedProducts.size(); i++) {
						react.addProduct(changedProducts.get(i));
					}
					for (int i = 0; i < changedModifiers.size(); i++) {
						react.addModifier(changedModifiers.get(i));
					}
					for (int i = 0; i < changedReactants.size(); i++) {
						react.addReactant(changedReactants.get(i));
					}
					if (reacReverse.getSelectedItem().equals("true")) {
						react.setReversible(true);
					}
					else {
						react.setReversible(false);
					}
					if (reacFast.getSelectedItem().equals("true")) {
						react.setFast(true);
					}
					else {
						react.setFast(false);
					}
					if (document.getLevel() > 2) {
						react.setCompartment((String) reactionComp.getSelectedItem());
					}
					react.setId(reacID.getText().trim());
					react.setName(reacName.getText().trim());
					react.getKineticLaw().setMath(SBMLutilities.myParseFormula(kineticLaw.getText().trim()));
					error = checkKineticLawUnits(react.getKineticLaw());
					if (!error) {
						error = SBMLutilities.checkCycles(document);
						if (error) {
							JOptionPane.showMessageDialog(Gui.frame, "Cycle detected within initial assignments, assignment rules, and rate laws.",
									"Cycle Detected", JOptionPane.ERROR_MESSAGE);
						}
					}
					if (!error) {
						usedIDs.add(reacID.getText().trim());
						JList add = new JList();
						Object[] adding = { reac };
						add.setListData(adding);
						add.setSelectedIndex(0);
						reactions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						adding = Utility.add(reacts, reactions, add, false, null, null, null, null, null, null, Gui.frame);
						reacts = new String[adding.length];
						for (int i = 0; i < adding.length; i++) {
							reacts[i] = (String) adding[i];
						}
						Utility.sort(reacts);
						reactions.setListData(reacts);
						reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						if (document.getModel().getNumReactions() == 1) {
							reactions.setSelectedIndex(0);
						}
						else {
							reactions.setSelectedIndex(index);
						}
					}
					else {
						removeTheReaction(document, reac);
					}
				}
				dirty.setValue(true);
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, reactionPanel, "Reaction Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options1, options1[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			if (option.equals("OK")) {
				String reac = reactionId;
				removeTheReaction(document, reac);
				document.getModel().addReaction(copyReact);
			}
			return;
		}
	}

	/**
	 * Find invalid reaction variables in a formula
	 */
	private ArrayList<String> getInvalidVariablesInReaction(String formula, boolean isReaction, String arguments, boolean isFunction) {
		ArrayList<String> validVars = new ArrayList<String>();
		ArrayList<String> invalidVars = new ArrayList<String>();
		ListOf sbml = document.getModel().getListOfFunctionDefinitions();
		for (int i = 0; i < document.getModel().getNumFunctionDefinitions(); i++) {
			validVars.add(((FunctionDefinition) sbml.get(i)).getId());
		}
		if (isReaction) {
			for (int i = 0; i < changedParameters.size(); i++) {
				validVars.add(((Parameter) changedParameters.get(i)).getId());
			}
			for (int i = 0; i < changedReactants.size(); i++) {
				validVars.add(((SpeciesReference) changedReactants.get(i)).getSpecies());
				validVars.add(((SpeciesReference) changedReactants.get(i)).getId());
			}
			for (int i = 0; i < changedProducts.size(); i++) {
				validVars.add(((SpeciesReference) changedProducts.get(i)).getSpecies());
				validVars.add(((SpeciesReference) changedProducts.get(i)).getId());
			}
			for (int i = 0; i < changedModifiers.size(); i++) {
				validVars.add(((ModifierSpeciesReference) changedModifiers.get(i)).getSpecies());
			}
		}
		else if (!isFunction) {
			sbml = document.getModel().getListOfSpecies();
			for (int i = 0; i < document.getModel().getNumSpecies(); i++) {
				validVars.add(((Species) sbml.get(i)).getId());
			}
		}
		if (isFunction) {
			String[] args = arguments.split(" |\\,");
			for (int i = 0; i < args.length; i++) {
				validVars.add(args[i]);
			}
		}
		else {
			sbml = document.getModel().getListOfCompartments();
			for (int i = 0; i < document.getModel().getNumCompartments(); i++) {
				if (document.getLevel() > 2 || ((Compartment) sbml.get(i)).getSpatialDimensions() != 0) {
					validVars.add(((Compartment) sbml.get(i)).getId());
				}
			}
			sbml = document.getModel().getListOfParameters();
			for (int i = 0; i < document.getModel().getNumParameters(); i++) {
				validVars.add(((Parameter) sbml.get(i)).getId());
			}
			sbml = document.getModel().getListOfReactions();
			for (int i = 0; i < document.getModel().getNumReactions(); i++) {
				Reaction reaction = (Reaction) sbml.get(i);
				validVars.add(reaction.getId());
				ListOf sbml2 = reaction.getListOfReactants();
				for (int j = 0; j < reaction.getNumReactants(); j++) {
					SpeciesReference reactant = (SpeciesReference) sbml2.get(j);
					if ((reactant.isSetId()) && (!reactant.getId().equals(""))) {
						validVars.add(reactant.getId());
					}
				}
				sbml2 = reaction.getListOfProducts();
				for (int j = 0; j < reaction.getNumProducts(); j++) {
					SpeciesReference product = (SpeciesReference) sbml2.get(j);
					if ((product.isSetId()) && (!product.getId().equals(""))) {
						validVars.add(product.getId());
					}
				}
			}
		}
		String[] splitLaw = formula.split(" |\\(|\\)|\\,|\\*|\\+|\\/|\\-");
		for (int i = 0; i < splitLaw.length; i++) {
			if (splitLaw[i].equals("abs") || splitLaw[i].equals("arccos") || splitLaw[i].equals("arccosh") || splitLaw[i].equals("arcsin")
					|| splitLaw[i].equals("arcsinh") || splitLaw[i].equals("arctan") || splitLaw[i].equals("arctanh") || splitLaw[i].equals("arccot")
					|| splitLaw[i].equals("arccoth") || splitLaw[i].equals("arccsc") || splitLaw[i].equals("arccsch") || splitLaw[i].equals("arcsec")
					|| splitLaw[i].equals("arcsech") || splitLaw[i].equals("acos") || splitLaw[i].equals("acosh") || splitLaw[i].equals("asin")
					|| splitLaw[i].equals("asinh") || splitLaw[i].equals("atan") || splitLaw[i].equals("atanh") || splitLaw[i].equals("acot")
					|| splitLaw[i].equals("acoth") || splitLaw[i].equals("acsc") || splitLaw[i].equals("acsch") || splitLaw[i].equals("asec")
					|| splitLaw[i].equals("asech") || splitLaw[i].equals("cos") || splitLaw[i].equals("cosh") || splitLaw[i].equals("cot")
					|| splitLaw[i].equals("coth") || splitLaw[i].equals("csc") || splitLaw[i].equals("csch") || splitLaw[i].equals("ceil")
					|| splitLaw[i].equals("factorial") || splitLaw[i].equals("exp") || splitLaw[i].equals("floor") || splitLaw[i].equals("ln")
					|| splitLaw[i].equals("log") || splitLaw[i].equals("sqr") || splitLaw[i].equals("log10") || splitLaw[i].equals("pow")
					|| splitLaw[i].equals("sqrt") || splitLaw[i].equals("root") || splitLaw[i].equals("piecewise") || splitLaw[i].equals("sec")
					|| splitLaw[i].equals("sech") || splitLaw[i].equals("sin") || splitLaw[i].equals("sinh") || splitLaw[i].equals("tan")
					|| splitLaw[i].equals("tanh") || splitLaw[i].equals("") || splitLaw[i].equals("and") || splitLaw[i].equals("or")
					|| splitLaw[i].equals("xor") || splitLaw[i].equals("not") || splitLaw[i].equals("eq") || splitLaw[i].equals("geq")
					|| splitLaw[i].equals("leq") || splitLaw[i].equals("gt") || splitLaw[i].equals("neq") || splitLaw[i].equals("lt")
					|| splitLaw[i].equals("delay") || splitLaw[i].equals("t") || splitLaw[i].equals("time") || splitLaw[i].equals("true")
					|| splitLaw[i].equals("false") || splitLaw[i].equals("pi") || splitLaw[i].equals("exponentiale")
					|| ((document.getLevel() > 2) && (splitLaw[i].equals("avogadro")))) {
			}
			else {
				String temp = splitLaw[i];
				if (splitLaw[i].substring(splitLaw[i].length() - 1, splitLaw[i].length()).equals("e")) {
					temp = splitLaw[i].substring(0, splitLaw[i].length() - 1);
				}
				try {
					Double.parseDouble(temp);
				}
				catch (Exception e1) {
					if (!validVars.contains(splitLaw[i])) {
						invalidVars.add(splitLaw[i]);
					}
				}
			}
		}
		return invalidVars;
	}

	/**
	 * Creates a frame used to edit reactions parameters or create new ones.
	 */
	private void reacParametersEditor(String option) {
		if (option.equals("OK") && reacParameters.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(Gui.frame, "No parameter selected.", "Must Select A Parameter", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JPanel parametersPanel;
		if (paramsOnly) {
			parametersPanel = new JPanel(new GridLayout(6, 2));
		}
		else {
			parametersPanel = new JPanel(new GridLayout(4, 2));
		}
		JLabel idLabel = new JLabel("ID:");
		JLabel nameLabel = new JLabel("Name:");
		JLabel valueLabel = new JLabel("Value:");
		JLabel unitsLabel = new JLabel("Units:");
		reacParamID = new JTextField();
		reacParamName = new JTextField();
		reacParamValue = new JTextField();
		reacParamUnits = new JComboBox();
		reacParamUnits.addItem("( none )");
		Model model = document.getModel();
		ListOf listOfUnits = model.getListOfUnitDefinitions();
		String[] units = new String[(int) model.getNumUnitDefinitions()];
		for (int i = 0; i < model.getNumUnitDefinitions(); i++) {
			UnitDefinition unit = (UnitDefinition) listOfUnits.get(i);
			units[i] = unit.getId();
			// GET OTHER THINGS
		}
		for (int i = 0; i < units.length; i++) {
			if (document.getLevel() > 2
					|| (!units[i].equals("substance") && !units[i].equals("volume") && !units[i].equals("area") && !units[i].equals("length") && !units[i]
							.equals("time"))) {
				reacParamUnits.addItem(units[i]);
			}
		}
		String[] unitIdsL2V4 = { "substance", "volume", "area", "length", "time", "ampere", "becquerel", "candela", "celsius", "coulomb",
				"dimensionless", "farad", "gram", "gray", "henry", "hertz", "item", "joule", "katal", "kelvin", "kilogram", "litre", "lumen", "lux",
				"metre", "mole", "newton", "ohm", "pascal", "radian", "second", "siemens", "sievert", "steradian", "tesla", "volt", "watt", "weber" };
		String[] unitIdsL3V1 = { "ampere", "avogadro", "becquerel", "candela", "celsius", "coulomb", "dimensionless", "farad", "gram", "gray",
				"henry", "hertz", "item", "joule", "katal", "kelvin", "kilogram", "litre", "lumen", "lux", "metre", "mole", "newton", "ohm",
				"pascal", "radian", "second", "siemens", "sievert", "steradian", "tesla", "volt", "watt", "weber" };
		String[] unitIds;
		if (document.getLevel() < 3) {
			unitIds = unitIdsL2V4;
		}
		else {
			unitIds = unitIdsL3V1;
		}
		for (int i = 0; i < unitIds.length; i++) {
			reacParamUnits.addItem(unitIds[i]);
		}
		String[] list = { "Original", "Modified" };
		String[] list1 = { "1", "2" };
		final JComboBox type = new JComboBox(list);
		final JTextField start = new JTextField();
		final JTextField stop = new JTextField();
		final JTextField step = new JTextField();
		final JComboBox level = new JComboBox(list1);
		final JButton sweep = new JButton("Sweep");
		sweep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] options = { "Ok", "Close" };
				JPanel p = new JPanel(new GridLayout(4, 2));
				JLabel startLabel = new JLabel("Start:");
				JLabel stopLabel = new JLabel("Stop:");
				JLabel stepLabel = new JLabel("Step:");
				JLabel levelLabel = new JLabel("Level:");
				p.add(startLabel);
				p.add(start);
				p.add(stopLabel);
				p.add(stop);
				p.add(stepLabel);
				p.add(step);
				p.add(levelLabel);
				p.add(level);
				int i = JOptionPane.showOptionDialog(Gui.frame, p, "Sweep", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
						options[0]);
				if (i == JOptionPane.YES_OPTION) {
					double startVal = 0.0;
					double stopVal = 0.0;
					double stepVal = 0.0;
					try {
						startVal = Double.parseDouble(start.getText().trim());
						stopVal = Double.parseDouble(stop.getText().trim());
						stepVal = Double.parseDouble(step.getText().trim());
					}
					catch (Exception e1) {
					}
					reacParamValue.setText("(" + startVal + "," + stopVal + "," + stepVal + "," + level.getSelectedItem() + ")");
				}
			}
		});
		if (paramsOnly) {
			reacParamID.setEditable(false);
			reacParamName.setEditable(false);
			reacParamValue.setEnabled(false);
			reacParamUnits.setEnabled(false);
			sweep.setEnabled(false);
		}
		String selectedID = "";
		if (option.equals("OK")) {
			String v = ((String) reacParameters.getSelectedValue()).split(" ")[0];
			Parameter paramet = null;
			for (Parameter p : changedParameters) {
				if (p.getId().equals(v)) {
					paramet = p;
				}
			}
			reacParamID.setText(paramet.getId());
			selectedID = paramet.getId();
			reacParamName.setText(paramet.getName());
			reacParamValue.setText("" + paramet.getValue());
			if (paramet.isSetUnits()) {
				reacParamUnits.setSelectedItem(paramet.getUnits());
			}
			if (paramsOnly && (((String) reacParameters.getSelectedValue()).contains("Modified"))
					|| (((String) reacParameters.getSelectedValue()).contains("Custom"))
					|| (((String) reacParameters.getSelectedValue()).contains("Sweep"))) {
				type.setSelectedItem("Modified");
				sweep.setEnabled(true);
				reacParamValue.setText(((String) reacParameters.getSelectedValue()).split(" ")[((String) reacParameters.getSelectedValue())
						.split(" ").length - 1]);
				reacParamValue.setEnabled(true);
				reacParamUnits.setEnabled(false);
				if (reacParamValue.getText().trim().startsWith("(")) {
					try {
						start.setText((reacParamValue.getText().trim()).split(",")[0].substring(1).trim());
						stop.setText((reacParamValue.getText().trim()).split(",")[1].trim());
						step.setText((reacParamValue.getText().trim()).split(",")[2].trim());
						int lev = Integer.parseInt((reacParamValue.getText().trim()).split(",")[3].replace(")", "").trim());
						if (lev == 1) {
							level.setSelectedIndex(0);
						}
						else {
							level.setSelectedIndex(1);
						}
					}
					catch (Exception e1) {
					}
				}
			}
		}
		parametersPanel.add(idLabel);
		parametersPanel.add(reacParamID);
		parametersPanel.add(nameLabel);
		parametersPanel.add(reacParamName);
		if (paramsOnly) {
			JLabel typeLabel = new JLabel("Type:");
			type.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (!((String) type.getSelectedItem()).equals("Original")) {
						sweep.setEnabled(true);
						reacParamValue.setEnabled(true);
						reacParamUnits.setEnabled(false);
					}
					else {
						sweep.setEnabled(false);
						reacParamValue.setEnabled(false);
						reacParamUnits.setEnabled(false);
						SBMLDocument d = Gui.readSBML(file);
						KineticLaw KL = d.getModel().getReaction(selectedReaction).getKineticLaw();
						ListOf list = KL.getListOfParameters();
						int number = -1;
						for (int i = 0; i < KL.getNumParameters(); i++) {
							if (((Parameter) list.get(i)).getId().equals(((String) reacParameters.getSelectedValue()).split(" ")[0])) {
								number = i;
							}
						}
						reacParamValue.setText(d.getModel().getReaction(selectedReaction).getKineticLaw()
								.getParameter(number).getValue()
								+ "");
						if (d.getModel().getReaction(selectedReaction).getKineticLaw().getParameter(number)
								.isSetUnits()) {
							reacParamUnits.setSelectedItem(d.getModel().getReaction(selectedReaction)
									.getKineticLaw().getParameter(number).getUnits());
						}
						reacParamValue.setText(d.getModel().getReaction(selectedReaction).getKineticLaw().getParameter(number).getValue()	+ "");
					}
				}
			});
			parametersPanel.add(typeLabel);
			parametersPanel.add(type);
		}
		parametersPanel.add(valueLabel);
		parametersPanel.add(reacParamValue);
		if (paramsOnly) {
			parametersPanel.add(new JLabel());
			parametersPanel.add(sweep);
		}
		parametersPanel.add(unitsLabel);
		parametersPanel.add(reacParamUnits);
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, parametersPanel, "Parameter Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = SBMLutilities.checkID(document, usedIDs, reacParamID.getText().trim(), selectedID, true);
			if (!error) {
				if (thisReactionParams.contains(reacParamID.getText().trim()) && (!reacParamID.getText().trim().equals(selectedID))) {
					JOptionPane.showMessageDialog(Gui.frame, "ID is not unique.", "ID Not Unique", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
			}
			if (!error) {
				double val = 0;
				if (reacParamValue.getText().trim().startsWith("(") && reacParamValue.getText().trim().endsWith(")")) {
					try {
						Double.parseDouble((reacParamValue.getText().trim()).split(",")[0].substring(1).trim());
						Double.parseDouble((reacParamValue.getText().trim()).split(",")[1].trim());
						Double.parseDouble((reacParamValue.getText().trim()).split(",")[2].trim());
						int lev = Integer.parseInt((reacParamValue.getText().trim()).split(",")[3].replace(")", "").trim());
						if (lev != 1 && lev != 2) {
							error = true;
							JOptionPane.showMessageDialog(Gui.frame, "The level can only be 1 or 2.", "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
					catch (Exception e1) {
						error = true;
						JOptionPane.showMessageDialog(Gui.frame, "Invalid sweeping parameters.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				else {
					try {
						val = Double.parseDouble(reacParamValue.getText().trim());
					}
					catch (Exception e1) {
						JOptionPane
								.showMessageDialog(Gui.frame, "The value must be a real number.", "Enter A Valid Value", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
				}
				if (!error) {
					String unit = (String) reacParamUnits.getSelectedItem();
					String param = "";
					if (paramsOnly && !((String) type.getSelectedItem()).equals("Original")) {
						int index = reacParameters.getSelectedIndex();
						String[] splits = reacParams[index].split(" ");
						for (int i = 0; i < splits.length - 2; i++) {
							param += splits[i] + " ";
						}
						if (!splits[splits.length - 2].equals("Modified") && !splits[splits.length - 2].equals("Custom")
								&& !splits[splits.length - 2].equals("Sweep")) {
							param += splits[splits.length - 2] + " " + splits[splits.length - 1] + " ";
						}
						if (reacParamValue.getText().trim().startsWith("(") && reacParamValue.getText().trim().endsWith(")")) {
							double startVal = Double.parseDouble((reacParamValue.getText().trim()).split(",")[0].substring(1).trim());
							double stopVal = Double.parseDouble((reacParamValue.getText().trim()).split(",")[1].trim());
							double stepVal = Double.parseDouble((reacParamValue.getText().trim()).split(",")[2].trim());
							int lev = Integer.parseInt((reacParamValue.getText().trim()).split(",")[3].replace(")", "").trim());
							param += "Sweep (" + startVal + "," + stopVal + "," + stepVal + "," + lev + ")";
						}
						else {
							param += "Modified " + val;
						}
					}
					else {
						if (unit.equals("( none )")) {
							param = reacParamID.getText().trim() + " " + val;
						}
						else {
							param = reacParamID.getText().trim() + " " + val + " " + unit;
						}
					}
					if (option.equals("OK")) {
						int index = reacParameters.getSelectedIndex();
						String v = ((String) reacParameters.getSelectedValue()).split(" ")[0];
						Parameter paramet = null;
						for (Parameter p : changedParameters) {
							if (p.getId().equals(v)) {
								paramet = p;
							}
						}
						reacParameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						reacParams = Utility.getList(reacParams, reacParameters);
						reacParameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						paramet.setId(reacParamID.getText().trim());
						paramet.setName(reacParamName.getText().trim());
						for (int i = 0; i < thisReactionParams.size(); i++) {
							if (thisReactionParams.get(i).equals(v)) {
								thisReactionParams.set(i, reacParamID.getText().trim());
							}
						}
						paramet.setValue(val);
						if (unit.equals("( none )")) {
							paramet.unsetUnits();
						}
						else {
							paramet.setUnits(unit);
						}
						reacParams[index] = param;
						Utility.sort(reacParams);
						reacParameters.setListData(reacParams);
						reacParameters.setSelectedIndex(index);
						if (paramsOnly) {
							int remove = -1;
							for (int i = 0; i < parameterChanges.size(); i++) {
								if (parameterChanges.get(i).split(" ")[0].equals(selectedReaction + "/"	+ reacParamID.getText().trim())) {
									remove = i;
								}
							}
							String reacValue = selectedReaction;
							int index1 = reactions.getSelectedIndex();
							if (remove != -1) {
								parameterChanges.remove(remove);
								reactions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								reacts = Utility.getList(reacts, reactions);
								reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								reacts[index1] = reacValue.split(" ")[0];
								Utility.sort(reacts);
								reactions.setListData(reacts);
								reactions.setSelectedIndex(index1);
							}
							if (!((String) type.getSelectedItem()).equals("Original")) {
								parameterChanges.add(reacValue + "/" + param);
								reactions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								reacts = Utility.getList(reacts, reactions);
								reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								reacts[index1] = reacValue + " Modified";
								Utility.sort(reacts);
								reactions.setListData(reacts);
								reactions.setSelectedIndex(index1);
							}
						}
						else {
							kineticLaw.setText(SBMLutilities.updateFormulaVar(kineticLaw.getText().trim(), v, reacParamID.getText().trim()));
						}
					}
					else {
						int index = reacParameters.getSelectedIndex();
						// Parameter paramet = new Parameter(BioSim.SBML_LEVEL,
						// BioSim.SBML_VERSION);
						Parameter paramet = new Parameter(document.getLevel(), document.getVersion());
						changedParameters.add(paramet);
						paramet.setId(reacParamID.getText().trim());
						paramet.setName(reacParamName.getText().trim());
						thisReactionParams.add(reacParamID.getText().trim());
						paramet.setValue(val);
						if (!unit.equals("( none )")) {
							paramet.setUnits(unit);
						}
						JList add = new JList();
						Object[] adding = { param };
						add.setListData(adding);
						add.setSelectedIndex(0);
						reacParameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						adding = Utility.add(reacParams, reacParameters, add, false, null, null, null, null, null, null, Gui.frame);
						reacParams = new String[adding.length];
						for (int i = 0; i < adding.length; i++) {
							reacParams[i] = (String) adding[i];
						}
						Utility.sort(reacParams);
						reacParameters.setListData(reacParams);
						reacParameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						try {
							if (document.getModel().getReaction(selectedReaction).getKineticLaw()
									.getNumParameters() == 1) {
								reacParameters.setSelectedIndex(0);
							}
							else {
								reacParameters.setSelectedIndex(index);
							}
						}
						catch (Exception e2) {
							reacParameters.setSelectedIndex(0);
						}
					}
					dirty.setValue(true);
				}
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, parametersPanel, "Parameter Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	/**
	 * Creates a frame used to edit products or create new ones.
	 */
	public void productsEditor(SBMLDocument document, String option, String selectedProductId, SpeciesReference product) {
		JPanel productsPanel;
		if (document.getLevel() < 3) {
			productsPanel = new JPanel(new GridLayout(4, 2));
		}
		else {
			productsPanel = new JPanel(new GridLayout(5, 2));
		}
		JLabel productIdLabel = new JLabel("Id:");
		JLabel productNameLabel = new JLabel("Name:");
		JLabel speciesLabel = new JLabel("Species:");
		Object[] stoiciOptions = { "Stoichiometry", "Stoichiometry Math" };
		stoiciLabel = new JComboBox(stoiciOptions);
		JLabel stoichiometryLabel = new JLabel("Stoichiometry");
		JLabel constantLabel = new JLabel("Constant");
		Object[] productConstantOptions = { "true", "false" };
		productConstant = new JComboBox(productConstantOptions);
		ListOf listOfSpecies = document.getModel().getListOfSpecies();
		String[] speciesList = new String[(int) document.getModel().getNumSpecies()];
		for (int i = 0; i < document.getModel().getNumSpecies(); i++) {
			speciesList[i] = ((Species) listOfSpecies.get(i)).getId();
		}
		Utility.sort(speciesList);
		productSpecies = new JComboBox();
		productSpecies.setEnabled(false);
		for (int i = 0; i < speciesList.length; i++) {
			Species species = document.getModel().getSpecies(speciesList[i]);
			if (species.getBoundaryCondition() || (!species.getConstant() && Rules.keepVarRateRule(document, "", speciesList[i]))) {
				productSpecies.addItem(speciesList[i]);
			}
		}
		productId = new JTextField("");
		/*
		 * int j = 0; while (usedIDs.contains("product"+j)) { j++; }
		 * productId.setText("product"+j);
		 */
		productName = new JTextField("");
		productStoiciometry = new JTextField("1");
		String selectedID = "";
		if (option.equals("OK")) {
			String v = selectedProductId;
			if (product == null) {
				for (SpeciesReference p : changedProducts) {
					if (p.getSpecies().equals(v)) {
						product = p;
					}
				}
			}
			if (product.isSetId()) {
				selectedID = product.getId();
				productId.setText(product.getId());
			}
			if (product.isSetName()) {
				productName.setText(product.getName());
			}
			productSpecies.setSelectedItem(product.getSpecies());
			if ((document.getLevel() < 3) && (product.isSetStoichiometryMath())) {
				stoiciLabel.setSelectedItem("Stoichiometry Math");
				productStoiciometry.setText("" + SBMLutilities.myFormulaToString(product.getStoichiometryMath().getMath()));
			}
			else {
				productStoiciometry.setText("" + product.getStoichiometry());
			}
			if (!product.getConstant()) {
				productConstant.setSelectedItem("false");
			}
		}
		productsPanel.add(productIdLabel);
		productsPanel.add(productId);
		productsPanel.add(productNameLabel);
		productsPanel.add(productName);
		productsPanel.add(speciesLabel);
		productsPanel.add(productSpecies);
		if (document.getLevel() < 3) {
			productsPanel.add(stoiciLabel);
		}
		else {
			productsPanel.add(stoichiometryLabel);
		}
		productsPanel.add(productStoiciometry);
		if (document.getLevel() > 2) {
			productsPanel.add(constantLabel);
			productsPanel.add(productConstant);
		}
		if (speciesList.length == 0) {
			JOptionPane.showMessageDialog(Gui.frame, "There are no species availiable to be products." + "\nAdd species to this sbml file first.",
					"No Species", JOptionPane.ERROR_MESSAGE);
			return;
		}
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, productsPanel, "Products Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = false;
			String prod = "";
			double val = 1.0;
			if (productId.getText().trim().equals("")) {
				error = SBMLutilities.variableInUse(document, selectedID, false, true);
			}
			else {
				error = SBMLutilities.checkID(document, usedIDs, productId.getText().trim(), selectedID, false);
			}
			if (!error) {
				if (stoiciLabel.getSelectedItem().equals("Stoichiometry")) {
					try {
						val = Double.parseDouble(productStoiciometry.getText().trim());
					}
					catch (Exception e1) {
						JOptionPane.showMessageDialog(Gui.frame, "The stoichiometry must be a real number.", "Enter A Valid Value",
								JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					if (val <= 0) {
						JOptionPane.showMessageDialog(Gui.frame, "The stoichiometry value must be greater than 0.", "Enter A Valid Value",
								JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					prod = productSpecies.getSelectedItem() + " " + val;
				}
				else {
					prod = productSpecies.getSelectedItem() + " " + productStoiciometry.getText().trim();
				}
			}
			int index = -1;
			if (!error) {
				if (product == null) {
					if (option.equals("OK")) {
						index = products.getSelectedIndex();
					}
					products.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					proda = Utility.getList(proda, products);
					products.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					if (index >= 0) {
						products.setSelectedIndex(index);
					}
					for (int i = 0; i < proda.length; i++) {
						if (i != index) {
							if (proda[i].split(" ")[0].equals(productSpecies.getSelectedItem())) {
								error = true;
								JOptionPane.showMessageDialog(Gui.frame, "Unable to add species as a product.\n"
										+ "Each species can only be used as a product once.", "Species Can Only Be Used Once",
										JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			}
			if (!error) {
				if (stoiciLabel.getSelectedItem().equals("Stoichiometry Math")) {
					if (productStoiciometry.getText().trim().equals("")) {
						JOptionPane.showMessageDialog(Gui.frame, "Stoichiometry math must have formula.", "Enter Stoichiometry Formula",
								JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					else if (SBMLutilities.myParseFormula(productStoiciometry.getText().trim()) == null) {
						JOptionPane.showMessageDialog(Gui.frame, "Stoichiometry formula is not valid.", "Enter Valid Formula",
								JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					else {
						ArrayList<String> invalidVars = getInvalidVariablesInReaction(productStoiciometry.getText().trim(), true, "", false);
						if (invalidVars.size() > 0) {
							String invalid = "";
							for (int i = 0; i < invalidVars.size(); i++) {
								if (i == invalidVars.size() - 1) {
									invalid += invalidVars.get(i);
								}
								else {
									invalid += invalidVars.get(i) + "\n";
								}
							}
							String message;
							message = "Stoiciometry math contains unknown variables.\n\n" + "Unknown variables:\n" + invalid;
							JTextArea messageArea = new JTextArea(message);
							messageArea.setLineWrap(true);
							messageArea.setWrapStyleWord(true);
							messageArea.setEditable(false);
							JScrollPane scrolls = new JScrollPane();
							scrolls.setMinimumSize(new Dimension(300, 300));
							scrolls.setPreferredSize(new Dimension(300, 300));
							scrolls.setViewportView(messageArea);
							JOptionPane.showMessageDialog(Gui.frame, scrolls, "Stoiciometry Math Error", JOptionPane.ERROR_MESSAGE);
							error = true;
						}
						if (!error) {
							error = SBMLutilities.checkNumFunctionArguments(document,
									SBMLutilities.myParseFormula(productStoiciometry.getText().trim()));
						}
						if (!error) {
							if (SBMLutilities.myParseFormula(productStoiciometry.getText().trim()).isBoolean()) {
								JOptionPane.showMessageDialog(Gui.frame, "Stoichiometry math must evaluate to a number.", "Number Expected",
										JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						}
					}
				}
			}
			if (!error && option.equals("OK") && productConstant.getSelectedItem().equals("true")) {
				String id = selectedID;
				error = SBMLutilities.checkConstant(document, "Product stoiciometry", id);
			}
			if (!error) {
				if (option.equals("OK")) {
					String v = selectedProductId;
					SpeciesReference produ = product;
					if (produ == null) {
						for (SpeciesReference p : changedProducts) {
							if (p.getSpecies().equals(v)) {
								produ = p;
							}
						}
						products.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						proda = Utility.getList(proda, products);
						products.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					}
					produ.setId(productId.getText().trim());
					for (int i = 0; i < usedIDs.size(); i++) {
						if (usedIDs.get(i).equals(selectedID)) {
							usedIDs.set(i, produ.getId());
						}
					}
					produ.setName(productName.getText().trim());
					produ.setSpecies((String) productSpecies.getSelectedItem());
					if (stoiciLabel.getSelectedItem().equals("Stoichiometry")) {
						produ.setStoichiometry(val);
						produ.unsetStoichiometryMath();
					}
					else {
						produ.createStoichiometryMath();
						produ.getStoichiometryMath().setMath(SBMLutilities.myParseFormula(productStoiciometry.getText().trim()));
						produ.setStoichiometry(1);
					}
					if (productConstant.getSelectedItem().equals("true")) {
						produ.setConstant(true);
					}
					else {
						produ.setConstant(false);
					}
					if (product == null) {
						proda[index] = prod;
						Utility.sort(proda);
						products.setListData(proda);
						products.setSelectedIndex(index);
					}
					SBMLutilities.updateVarId(document, false, selectedID, productId.getText().trim());
					if (product == null) {
						kineticLaw.setText(SBMLutilities.updateFormulaVar(kineticLaw.getText().trim(), selectedID, productId.getText().trim()));
					}
				}
				else {
					// SpeciesReference produ = new
					// SpeciesReference(BioSim.SBML_LEVEL, BioSim.SBML_VERSION);
					SpeciesReference produ = new SpeciesReference(document.getLevel(), document.getVersion());
					produ.setId(productId.getText().trim());
					usedIDs.add(produ.getId());
					produ.setName(productName.getText().trim());
					changedProducts.add(produ);
					produ.setSpecies((String) productSpecies.getSelectedItem());
					if (stoiciLabel.getSelectedItem().equals("Stoichiometry")) {
						produ.setStoichiometry(val);
					}
					else {
						produ.createStoichiometryMath();
						produ.getStoichiometryMath().setMath(SBMLutilities.myParseFormula(productStoiciometry.getText().trim()));
					}
					if (productConstant.getSelectedItem().equals("true")) {
						produ.setConstant(true);
					}
					else {
						produ.setConstant(false);
					}
					JList add = new JList();
					Object[] adding = { prod };
					add.setListData(adding);
					add.setSelectedIndex(0);
					products.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					adding = Utility.add(proda, products, add, false, null, null, null, null, null, null, Gui.frame);
					proda = new String[adding.length];
					for (int i = 0; i < adding.length; i++) {
						proda[i] = (String) adding[i];
					}
					Utility.sort(proda);
					products.setListData(proda);
					products.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					products.setSelectedIndex(0);
				}
				dirty.setValue(true);
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, productsPanel, "Products Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	/**
	 * Creates a frame used to edit modifiers or create new ones.
	 */
	public void modifiersEditor(String option) {
		if (option.equals("OK") && modifiers.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(Gui.frame, "No modifier selected.", "Must Select A Modifier", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JPanel modifiersPanel = new JPanel(new GridLayout(1, 2));
		JLabel speciesLabel = new JLabel("Species:");
		ListOf listOfSpecies = document.getModel().getListOfSpecies();
		String[] speciesList = new String[(int) document.getModel().getNumSpecies()];
		for (int i = 0; i < document.getModel().getNumSpecies(); i++) {
			speciesList[i] = ((Species) listOfSpecies.get(i)).getId();
		}
		Utility.sort(speciesList);
		Object[] choices = speciesList;
		modifierSpecies = new JComboBox(choices);
		if (option.equals("OK")) {
			String v = ((String) modifiers.getSelectedValue()).split(" ")[0];
			ModifierSpeciesReference modifier = null;
			for (ModifierSpeciesReference p : changedModifiers) {
				if (p.getSpecies().equals(v)) {
					modifier = p;
				}
			}
			modifierSpecies.setSelectedItem(modifier.getSpecies());
		}
		modifiersPanel.add(speciesLabel);
		modifiersPanel.add(modifierSpecies);
		if (choices.length == 0) {
			JOptionPane.showMessageDialog(Gui.frame, "There are no species availiable to be modifiers." + "\nAdd species to this sbml file first.",
					"No Species", JOptionPane.ERROR_MESSAGE);
			return;
		}
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, modifiersPanel, "Modifiers Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = false;
			String mod = (String) modifierSpecies.getSelectedItem();
			if (option.equals("OK")) {
				int index = modifiers.getSelectedIndex();
				modifiers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				modifier = Utility.getList(modifier, modifiers);
				modifiers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				modifiers.setSelectedIndex(index);
				for (int i = 0; i < modifier.length; i++) {
					if (i != index) {
						if (modifier[i].equals(modifierSpecies.getSelectedItem())) {
							error = true;
							JOptionPane
									.showMessageDialog(Gui.frame, "Unable to add species as a modifier.\n"
											+ "Each species can only be used as a modifier once.", "Species Can Only Be Used Once",
											JOptionPane.ERROR_MESSAGE);
						}
					}
				}
				if (!error) {
					String v = ((String) modifiers.getSelectedValue());
					ModifierSpeciesReference modi = null;
					for (ModifierSpeciesReference p : changedModifiers) {
						if (p.getSpecies().equals(v)) {
							modi = p;
						}
					}
					modifiers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					modifier = Utility.getList(modifier, modifiers);
					modifiers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					modi.setSpecies((String) modifierSpecies.getSelectedItem());
					modifier[index] = mod;
					Utility.sort(modifier);
					modifiers.setListData(modifier);
					modifiers.setSelectedIndex(index);
				}
			}
			else {
				int index = modifiers.getSelectedIndex();
				modifiers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				modifier = Utility.getList(modifier, modifiers);
				modifiers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				modifiers.setSelectedIndex(index);
				for (int i = 0; i < modifier.length; i++) {
					if (modifier[i].equals(modifierSpecies.getSelectedItem())) {
						error = true;
						JOptionPane.showMessageDialog(Gui.frame, "Unable to add species as a modifier.\n"
								+ "Each species can only be used as a modifier once.", "Species Can Only Be Used Once", JOptionPane.ERROR_MESSAGE);
					}
				}
				if (!error) {
					// ModifierSpeciesReference modi = new
					// ModifierSpeciesReference(BioSim.SBML_LEVEL,
					// BioSim.SBML_VERSION);
					ModifierSpeciesReference modi = new ModifierSpeciesReference(document.getLevel(), document.getVersion());
					changedModifiers.add(modi);
					modi.setSpecies((String) modifierSpecies.getSelectedItem());
					JList add = new JList();
					Object[] adding = { mod };
					add.setListData(adding);
					add.setSelectedIndex(0);
					modifiers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					adding = Utility.add(modifier, modifiers, add, false, null, null, null, null, null, null, Gui.frame);
					modifier = new String[adding.length];
					for (int i = 0; i < adding.length; i++) {
						modifier[i] = (String) adding[i];
					}
					Utility.sort(modifier);
					modifiers.setListData(modifier);
					modifiers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					try {
						if (document.getModel().getReaction(selectedReaction).getNumModifiers() == 1) {
							modifiers.setSelectedIndex(0);
						}
						else {
							modifiers.setSelectedIndex(index);
						}
					}
					catch (Exception e2) {
						modifiers.setSelectedIndex(0);
					}
				}
			}
			dirty.setValue(true);
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, modifiersPanel, "Modifiers Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	/**
	 * Creates a frame used to edit reactants or create new ones.
	 */
	public void reactantsEditor(SBMLDocument document, String option, String selectedReactantId, SpeciesReference reactant) {
		JPanel reactantsPanel;
		if (document.getLevel() < 3) {
			reactantsPanel = new JPanel(new GridLayout(4, 2));
		}
		else {
			reactantsPanel = new JPanel(new GridLayout(5, 2));
		}
		JLabel reactantIdLabel = new JLabel("Id:");
		JLabel reactantNameLabel = new JLabel("Name:");
		JLabel speciesLabel = new JLabel("Species:");
		Object[] stoiciOptions = { "Stoichiometry", "Stoichiometry Math" };
		stoiciLabel = new JComboBox(stoiciOptions);
		JLabel stoichiometryLabel = new JLabel("Stoichiometry");
		JLabel constantLabel = new JLabel("Constant");
		Object[] reactantConstantOptions = { "true", "false" };
		reactantConstant = new JComboBox(reactantConstantOptions);
		ListOf listOfSpecies = document.getModel().getListOfSpecies();
		String[] speciesList = new String[(int) document.getModel().getNumSpecies()];
		for (int i = 0; i < document.getModel().getNumSpecies(); i++) {
			speciesList[i] = ((Species) listOfSpecies.get(i)).getId();
		}
		Utility.sort(speciesList);
		reactantSpecies = new JComboBox();
		reactantSpecies.setEnabled(false);
		for (int i = 0; i < speciesList.length; i++) {
			Species species = document.getModel().getSpecies(speciesList[i]);
			if (species.getBoundaryCondition() || (!species.getConstant() && Rules.keepVarRateRule(document, "", speciesList[i]))) {
				reactantSpecies.addItem(speciesList[i]);
			}
		}
		reactantId = new JTextField("");
		reactantName = new JTextField("");
		reactantStoiciometry = new JTextField("1");
		String selectedID = "";
		if (option.equals("OK")) {
			String v = selectedReactantId;
			if (reactant == null) {
				for (SpeciesReference r : changedReactants) {
					if (r.getSpecies().equals(v)) {
						reactant = r;
					}
				}
			}
			reactantSpecies.setSelectedItem(reactant.getSpecies());
			if (reactant.isSetId()) {
				selectedID = reactant.getId();
				reactantId.setText(reactant.getId());
			}
			if (reactant.isSetName()) {
				reactantName.setText(reactant.getName());
			}
			if ((document.getLevel() < 3) && (reactant.isSetStoichiometryMath())) {
				stoiciLabel.setSelectedItem("Stoichiometry Math");
				reactantStoiciometry.setText("" + SBMLutilities.myFormulaToString(reactant.getStoichiometryMath().getMath()));
			}
			else {
				reactantStoiciometry.setText("" + reactant.getStoichiometry());
			}
			if (!reactant.getConstant()) {
				reactantConstant.setSelectedItem("false");
			}
		}
		reactantsPanel.add(reactantIdLabel);
		reactantsPanel.add(reactantId);
		reactantsPanel.add(reactantNameLabel);
		reactantsPanel.add(reactantName);
		reactantsPanel.add(speciesLabel);
		reactantsPanel.add(reactantSpecies);
		if (document.getLevel() < 3) {
			reactantsPanel.add(stoiciLabel);
		}
		else {
			reactantsPanel.add(stoichiometryLabel);
		}
		reactantsPanel.add(reactantStoiciometry);
		if (document.getLevel() > 2) {
			reactantsPanel.add(constantLabel);
			reactantsPanel.add(reactantConstant);
		}
		if (speciesList.length == 0) {
			JOptionPane.showMessageDialog(Gui.frame, "There are no species availiable to be reactants." + "\nAdd species to this sbml file first.",
					"No Species", JOptionPane.ERROR_MESSAGE);
			return;
		}
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, reactantsPanel, "Reactants Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = false;
			String react = "";
			double val = 1.0;
			if (reactantId.getText().trim().equals("")) {
				error = SBMLutilities.variableInUse(document, selectedID, false, true);
			}
			else {
				error = SBMLutilities.checkID(document, usedIDs, reactantId.getText().trim(), selectedID, false);
			}
			if (!error) {
				if (stoiciLabel.getSelectedItem().equals("Stoichiometry")) {
					try {
						val = Double.parseDouble(reactantStoiciometry.getText().trim());
					}
					catch (Exception e1) {
						JOptionPane.showMessageDialog(Gui.frame, "The stoichiometry must be a real number.", "Enter A Valid Value",
								JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					if (val <= 0) {
						JOptionPane.showMessageDialog(Gui.frame, "The stoichiometry value must be greater than 0.", "Enter A Valid Value",
								JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					react = reactantSpecies.getSelectedItem() + " " + val;
				}
				else {
					react = reactantSpecies.getSelectedItem() + " " + reactantStoiciometry.getText().trim();
				}
			}
			int index = -1;
			if (!error) {
				if (reactant == null) {
					if (option.equals("OK")) {
						index = reactants.getSelectedIndex();
					}
					reactants.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					reacta = Utility.getList(reacta, reactants);
					reactants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					if (index >= 0) {
						reactants.setSelectedIndex(index);
					}
					for (int i = 0; i < reacta.length; i++) {
						if (i != index) {
							if (reacta[i].split(" ")[0].equals(reactantSpecies.getSelectedItem())) {
								error = true;
								JOptionPane.showMessageDialog(Gui.frame, "Unable to add species as a reactant.\n"
										+ "Each species can only be used as a reactant once.", "Species Can Only Be Used Once",
										JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			}
			if (!error) {
				if (stoiciLabel.getSelectedItem().equals("Stoichiometry Math")) {
					if (reactantStoiciometry.getText().trim().equals("")) {
						JOptionPane.showMessageDialog(Gui.frame, "Stoichiometry math must have formula.", "Enter Stoichiometry Formula",
								JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					else if (SBMLutilities.myParseFormula(reactantStoiciometry.getText().trim()) == null) {
						JOptionPane.showMessageDialog(Gui.frame, "Stoichiometry formula is not valid.", "Enter Valid Formula",
								JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					else {
						ArrayList<String> invalidVars = getInvalidVariablesInReaction(reactantStoiciometry.getText().trim(), true, "", false);
						if (invalidVars.size() > 0) {
							String invalid = "";
							for (int i = 0; i < invalidVars.size(); i++) {
								if (i == invalidVars.size() - 1) {
									invalid += invalidVars.get(i);
								}
								else {
									invalid += invalidVars.get(i) + "\n";
								}
							}
							String message;
							message = "Stoiciometry math contains unknown variables.\n\n" + "Unknown variables:\n" + invalid;
							JTextArea messageArea = new JTextArea(message);
							messageArea.setLineWrap(true);
							messageArea.setWrapStyleWord(true);
							messageArea.setEditable(false);
							JScrollPane scrolls = new JScrollPane();
							scrolls.setMinimumSize(new Dimension(300, 300));
							scrolls.setPreferredSize(new Dimension(300, 300));
							scrolls.setViewportView(messageArea);
							JOptionPane.showMessageDialog(Gui.frame, scrolls, "Stoiciometry Math Error", JOptionPane.ERROR_MESSAGE);
							error = true;
						}
						if (!error) {
							error = SBMLutilities.checkNumFunctionArguments(document,
									SBMLutilities.myParseFormula(reactantStoiciometry.getText().trim()));
						}
						if (!error) {
							if (SBMLutilities.myParseFormula(reactantStoiciometry.getText().trim()).isBoolean()) {
								JOptionPane.showMessageDialog(Gui.frame, "Stoichiometry math must evaluate to a number.", "Number Expected",
										JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						}
					}
				}
			}
			if (!error && option.equals("OK") && reactantConstant.getSelectedItem().equals("true")) {
				String id = selectedID;
				error = SBMLutilities.checkConstant(document, "Reactant stoiciometry", id);
			}
			if (!error) {
				if (option.equals("OK")) {
					String v = selectedReactantId;
					SpeciesReference reactan = reactant;
					if (reactant == null) {
						for (SpeciesReference r : changedReactants) {
							if (r.getSpecies().equals(v)) {
								reactan = r;
							}
						}
						reactants.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						reacta = Utility.getList(reacta, reactants);
						reactants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					}
					reactan.setId(reactantId.getText().trim());
					for (int i = 0; i < usedIDs.size(); i++) {
						if (usedIDs.get(i).equals(selectedID)) {
							usedIDs.set(i, reactan.getId());
						}
					}
					reactan.setName(reactantName.getText().trim());
					reactan.setSpecies((String) reactantSpecies.getSelectedItem());
					if (stoiciLabel.getSelectedItem().equals("Stoichiometry")) {
						reactan.setStoichiometry(val);
						reactan.unsetStoichiometryMath();
					}
					else {
						reactan.createStoichiometryMath();
						reactan.getStoichiometryMath().setMath(SBMLutilities.myParseFormula(reactantStoiciometry.getText().trim()));
						reactan.setStoichiometry(1);
					}
					if (reactantConstant.getSelectedItem().equals("true")) {
						reactan.setConstant(true);
					}
					else {
						reactan.setConstant(false);
					}
					if (reactant == null) {
						reacta[index] = react;
						Utility.sort(reacta);
						reactants.setListData(reacta);
						reactants.setSelectedIndex(index);
					}
					SBMLutilities.updateVarId(document, false, selectedID, reactantId.getText().trim());
					if (reactant == null) {
						kineticLaw.setText(SBMLutilities.updateFormulaVar(kineticLaw.getText().trim(), selectedID, reactantId.getText().trim()));
					}
				}
				else {
					// SpeciesReference reactan = new
					// SpeciesReference(BioSim.SBML_LEVEL, BioSim.SBML_VERSION);
					SpeciesReference reactan = new SpeciesReference(document.getLevel(), document.getVersion());
					reactan.setId(reactantId.getText().trim());
					usedIDs.add(reactan.getId());
					reactan.setName(reactantName.getText().trim());
					reactan.setConstant(true);
					changedReactants.add(reactan);
					reactan.setSpecies((String) reactantSpecies.getSelectedItem());
					if (stoiciLabel.getSelectedItem().equals("Stoichiometry")) {
						reactan.setStoichiometry(val);
					}
					else {
						reactan.createStoichiometryMath();
						reactan.getStoichiometryMath().setMath(SBMLutilities.myParseFormula(reactantStoiciometry.getText().trim()));
					}
					if (reactantConstant.getSelectedItem().equals("true")) {
						reactan.setConstant(true);
					}
					else {
						reactan.setConstant(false);
					}
					JList add = new JList();
					Object[] adding = { react };
					add.setListData(adding);
					add.setSelectedIndex(0);
					reactants.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					adding = Utility.add(reacta, reactants, add, false, null, null, null, null, null, null, Gui.frame);
					reacta = new String[adding.length];
					for (int i = 0; i < adding.length; i++) {
						reacta[i] = (String) adding[i];
					}
					Utility.sort(reacta);
					reactants.setListData(reacta);
					reactants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					reactants.setSelectedIndex(0);
				}
				dirty.setValue(true);
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, reactantsPanel, "Reactants Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	/**
	 * Remove a reaction
	 */
	private void removeReaction() {
		int index = reactions.getSelectedIndex();
		if (index != -1) {
			String selected = ((String) reactions.getSelectedValue()).split(" ")[0];
			removeTheReaction(document, selected);
			usedIDs.remove(selected);
			reactions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			reacts = (String[]) Utility.remove(reactions, reacts);
			reactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			if (index < reactions.getModel().getSize()) {
				reactions.setSelectedIndex(index);
			}
			else {
				reactions.setSelectedIndex(index - 1);
			}
			dirty.setValue(true);
		}
	}

	/**
	 * Remove the reaction
	 */
	public void removeTheReaction(SBMLDocument document, String selected) {
		Reaction tempReaction = document.getModel().getReaction(selected);
		ListOf r = document.getModel().getListOfReactions();
		for (int i = 0; i < document.getModel().getNumReactions(); i++) {
			if (((Reaction) r.get(i)).getId().equals(tempReaction.getId())) {
				r.remove(i);
			}
		}
		usedIDs.remove(selected);
	}

	/**
	 * Remove a reactant from a reaction
	 */
	private void removeReactant() {
		int index = reactants.getSelectedIndex();
		if (index != -1) {
			String v = ((String) reactants.getSelectedValue()).split(" ")[0];
			for (int i = 0; i < changedReactants.size(); i++) {
				if (changedReactants.get(i).getSpecies().equals(v) && !SBMLutilities.variableInUse(document, changedReactants.get(i).getId(), false, true)) {
					changedReactants.remove(i);
					reactants.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					reacta = (String[]) Utility.remove(reactants, reacta);
					reactants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					if (index < reactants.getModel().getSize()) {
						reactants.setSelectedIndex(index);
					}
					else {
						reactants.setSelectedIndex(index - 1);
					}
					dirty.setValue(true);
				}
			}
		}
	}

	/**
	 * Remove a product from a reaction
	 */
	private void removeProduct() {
		int index = products.getSelectedIndex();
		if (index != -1) {
			String v = ((String) products.getSelectedValue()).split(" ")[0];
			for (int i = 0; i < changedProducts.size(); i++) {
				if (changedProducts.get(i).getSpecies().equals(v) && !SBMLutilities.variableInUse(document, changedProducts.get(i).getId(), false, true)) {
					changedProducts.remove(i);
					products.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					proda = (String[]) Utility.remove(products, proda);
					products.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					if (index < products.getModel().getSize()) {
						products.setSelectedIndex(index);
					}
					else {
						products.setSelectedIndex(index - 1);
					}
					dirty.setValue(true);
				}
			}
		}
	}

	/**
	 * Remove a modifier from a reaction
	 */
	private void removeModifier() {
		int index = modifiers.getSelectedIndex();
		if (index != -1) {
			String v = ((String) modifiers.getSelectedValue()).split(" ")[0];
			for (int i = 0; i < changedModifiers.size(); i++) {
				if (changedModifiers.get(i).getSpecies().equals(v)) {
					changedModifiers.remove(i);
				}
			}
			modifiers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			modifier = (String[]) Utility.remove(modifiers, modifier);
			modifiers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			if (index < modifiers.getModel().getSize()) {
				modifiers.setSelectedIndex(index);
			}
			else {
				modifiers.setSelectedIndex(index - 1);
			}
			dirty.setValue(true);
		}
	}

	/**
	 * Remove a function if not in use
	 */
	private void useMassAction() {
		String kf;
		String kr;
		if (changedParameters.size() == 0) {
			kf = "kf";
			kr = "kr";
		}
		else if (changedParameters.size() == 1) {
			kf = changedParameters.get(0).getId();
			kr = changedParameters.get(0).getId();
		}
		else {
			kf = changedParameters.get(0).getId();
			kr = changedParameters.get(1).getId();
		}
		String kinetic = kf;
		if (document.getLevel() > 2) {
			boolean addEquil = false;
			String equilExpr = "";
			for (SpeciesReference s : changedReactants) {
				if (s.isSetId()) {
					addEquil = true;
					equilExpr += s.getId();
				}
				else {
					equilExpr += s.getStoichiometry();
				}
			}
			if (addEquil) {
				kinetic += " * pow(" + kf + "/" + kr + "," + equilExpr + "-2)";
			}
		}
		for (SpeciesReference s : changedReactants) {
			if ((document.getLevel() < 3) && (s.isSetStoichiometryMath())) {
				kinetic += " * pow(" + s.getSpecies() + ", " + SBMLutilities.myFormulaToString(s.getStoichiometryMath().getMath()) + ")";
			}
			else if ((document.getLevel() > 2) && (s.isSetId())) {
				kinetic += " * pow(" + s.getSpecies() + ", " + s.getId() + ")";
			}
			else {
				if (s.getStoichiometry() == 1) {
					kinetic += " * " + s.getSpecies();
				}
				else {
					kinetic += " * pow(" + s.getSpecies() + ", " + s.getStoichiometry() + ")";
				}
			}
		}
		for (ModifierSpeciesReference s : changedModifiers) {
			kinetic += " * " + s.getSpecies();
		}
		if (reacReverse.getSelectedItem().equals("true")) {
			kinetic += " - " + kr;
			if (document.getLevel() > 2) {
				boolean addEquil = false;
				String equilExpr = "";
				for (SpeciesReference s : changedProducts) {
					if (s.isSetId()) {
						addEquil = true;
						equilExpr += s.getId();
					}
					else {
						equilExpr += s.getStoichiometry();
					}
				}
				if (addEquil) {
					kinetic += " * pow(" + kf + "/" + kr + "," + equilExpr + "-1)";
				}
			}
			for (SpeciesReference s : changedProducts) {
				if ((document.getLevel() < 3) && (s.isSetStoichiometryMath())) {
					kinetic += " * pow(" + s.getSpecies() + ", " + SBMLutilities.myFormulaToString(s.getStoichiometryMath().getMath()) + ")";
				}
				else if ((document.getLevel() > 2) && (s.isSetId())) {
					kinetic += " * pow(" + s.getSpecies() + ", " + s.getId() + ")";
				}
				else {
					if (s.getStoichiometry() == 1) {
						kinetic += " * " + s.getSpecies();
					}
					else {
						kinetic += " * pow(" + s.getSpecies() + ", " + s.getStoichiometry() + ")";
					}
				}
			}
			for (ModifierSpeciesReference s : changedModifiers) {
				kinetic += " * " + s.getSpecies();
			}
		}
		kineticLaw.setText(kinetic);
		dirty.setValue(true);
	}

	/**
	 * Remove a reaction parameter, if allowed
	 */
	private void reacRemoveParam() {
		int index = reacParameters.getSelectedIndex();
		if (index != -1) {
			String v = ((String) reacParameters.getSelectedValue()).split(" ")[0];
			if (reactions.getSelectedIndex() != -1) {
				String kinetic = kineticLaw.getText().trim();
				String[] vars = new String[0];
				if (!kinetic.equals("")) {
					vars = SBMLutilities.myFormulaToString(SBMLutilities.myParseFormula(kineticLaw.getText().trim())).split(" |\\(|\\)|\\,");
				}
				for (int j = 0; j < vars.length; j++) {
					if (vars[j].equals(v)) {
						JOptionPane.showMessageDialog(Gui.frame, "Cannot remove reaction parameter because it is used in the kinetic law.",
								"Cannot Remove Parameter", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				for (SpeciesReference p : changedProducts) {
					if (p.isSetSpecies()) {
						String specRef = p.getSpecies();
						if (p.isSetStoichiometryMath()) {
							vars = SBMLutilities.myFormulaToString(p.getStoichiometryMath().getMath()).split(" |\\(|\\)|\\,");
							for (int k = 0; k < vars.length; k++) {
								if (vars[k].equals(v)) {
									JOptionPane.showMessageDialog(Gui.frame,
											"Cannot remove reaction parameter because it is used in the stoichiometry math for product " + specRef
													+ ".", "Cannot Remove Parameter", JOptionPane.ERROR_MESSAGE);
									return;
								}
							}
						}
					}
				}
				for (SpeciesReference r : changedReactants) {
					if (r.isSetSpecies()) {
						String specRef = r.getSpecies();
						if (r.isSetStoichiometryMath()) {
							vars = SBMLutilities.myFormulaToString(r.getStoichiometryMath().getMath()).split(" |\\(|\\)|\\,");
							for (int k = 0; k < vars.length; k++) {
								if (vars[k].equals(v)) {
									JOptionPane.showMessageDialog(Gui.frame,
											"Cannot remove reaction parameter because it is used in the stoichiometry math for reactant " + specRef
													+ ".", "Cannot Remove Parameter", JOptionPane.ERROR_MESSAGE);
									return;
								}
							}
						}
					}
				}
			}
			for (int i = 0; i < changedParameters.size(); i++) {
				if (changedParameters.get(i).getId().equals(v)) {
					changedParameters.remove(i);
				}
			}
			thisReactionParams.remove(v);
			reacParameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			reacParams = (String[]) Utility.remove(reacParameters, reacParams);
			reacParameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			if (index < reacParameters.getModel().getSize()) {
				reacParameters.setSelectedIndex(index);
			}
			else {
				reacParameters.setSelectedIndex(index - 1);
			}
			dirty.setValue(true);
		}
	}

	/**
	 * Check the units of a kinetic law
	 */
	public boolean checkKineticLawUnits(KineticLaw law) {
		document.getModel().populateListFormulaUnitsData();
		if (law.containsUndeclaredUnits()) {
			if (biosim.checkUndeclared) {
				JOptionPane.showMessageDialog(Gui.frame, "Kinetic law contains literals numbers or parameters with undeclared units.\n"
						+ "Therefore, it is not possible to completely verify the consistency of the units.", "Contains Undeclared Units",
						JOptionPane.WARNING_MESSAGE);
			}
			return false;
		}
		else if (biosim.checkUnits) {
			UnitDefinition unitDef = law.getDerivedUnitDefinition();
			// UnitDefinition unitDefLaw = new UnitDefinition(BioSim.SBML_LEVEL,
			// BioSim.SBML_VERSION);
			UnitDefinition unitDefLaw = new UnitDefinition(document.getLevel(), document.getVersion());
			if (document.getModel().getUnitDefinition("substance") != null) {
				UnitDefinition subUnitDef = document.getModel().getUnitDefinition("substance");
				for (int i = 0; i < subUnitDef.getNumUnits(); i++) {
					Unit subUnit = subUnitDef.getUnit(i);
					unitDefLaw.addUnit(subUnit);
				}
			}
			else {
				Unit unit = unitDefLaw.createUnit();
				unit.setKind(libsbml.UnitKind_forName("mole"));
				unit.setExponent(1);
				unit.setScale(0);
				unit.setMultiplier(1.0);
			}
			if (document.getModel().getUnitDefinition("time") != null) {
				UnitDefinition timeUnitDef = document.getModel().getUnitDefinition("time");
				for (int i = 0; i < timeUnitDef.getNumUnits(); i++) {
					Unit timeUnit = timeUnitDef.getUnit(i);
					Unit recTimeUnit = unitDefLaw.createUnit();
					recTimeUnit.setKind(timeUnit.getKind());
					if (document.getLevel() < 3) {
						recTimeUnit.setExponent(timeUnit.getExponent() * (-1));
					}
					else {
						recTimeUnit.setExponent(timeUnit.getExponentAsDouble() * (-1));
					}
					recTimeUnit.setScale(timeUnit.getScale());
					recTimeUnit.setMultiplier(timeUnit.getMultiplier());
				}
			}
			else {
				Unit unit = unitDefLaw.createUnit();
				unit.setKind(libsbml.UnitKind_forName("second"));
				unit.setExponent(-1);
				unit.setScale(0);
				unit.setMultiplier(1.0);
			}
			if (!UnitDefinition.areEquivalent(unitDef, unitDefLaw)) {
				JOptionPane.showMessageDialog(Gui.frame, "Kinetic law units should be substance / time.", "Units Do Not Match",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
		}
		return false;
	}

	public void setPanels(InitialAssignments initialsPanel, Rules rulesPanel) {
		this.initialsPanel = initialsPanel;
		this.rulesPanel = rulesPanel;
	}

	public void actionPerformed(ActionEvent e) {
		// if the add compartment type button is clicked
		// if the add species type button is clicked
		// if the add compartment button is clicked
		// if the add parameters button is clicked
		// if the add reactions button is clicked
		if (e.getSource() == addReac) {
			reactionsEditor(document, "Add", "", false);
		}
		// if the edit reactions button is clicked
		else if (e.getSource() == editReac) {
			if (reactions.getSelectedIndex() == -1) {
				JOptionPane.showMessageDialog(Gui.frame, "No reaction selected.", "Must Select A Reaction", JOptionPane.ERROR_MESSAGE);
				return;
			}
			reactionsEditor(document, "OK", ((String) reactions.getSelectedValue()).split(" ")[0], false);
			initialsPanel.refreshInitialAssignmentPanel(document);
			rulesPanel.refreshRulesPanel(document);
		}
		// if the remove reactions button is clicked
		else if (e.getSource() == removeReac) {
			removeReaction();
		}
		// if the add reactions parameters button is clicked
		else if (e.getSource() == reacAddParam) {
			reacParametersEditor("Add");
		}
		// if the edit reactions parameters button is clicked
		else if (e.getSource() == reacEditParam) {
			reacParametersEditor("OK");
		}
		// if the remove reactions parameters button is clicked
		else if (e.getSource() == reacRemoveParam) {
			reacRemoveParam();
		}
		// if the add reactants button is clicked
		else if (e.getSource() == addReactant) {
			reactantsEditor(document, "Add", "", null);
		}
		// if the edit reactants button is clicked
		else if (e.getSource() == editReactant) {
			if (reactants.getSelectedIndex() == -1) {
				JOptionPane.showMessageDialog(Gui.frame, "No reactant selected.", "Must Select A Reactant", JOptionPane.ERROR_MESSAGE);
				return;
			}
			reactantsEditor(document, "OK", ((String) reactants.getSelectedValue()).split(" ")[0], null);
			initialsPanel.refreshInitialAssignmentPanel(document);
			rulesPanel.refreshRulesPanel(document);
		}
		// if the remove reactants button is clicked
		else if (e.getSource() == removeReactant) {
			removeReactant();
		}
		// if the add products button is clicked
		else if (e.getSource() == addProduct) {
			productsEditor(document, "Add", "", null);
		}
		// if the edit products button is clicked
		else if (e.getSource() == editProduct) {
			if (products.getSelectedIndex() == -1) {
				JOptionPane.showMessageDialog(Gui.frame, "No product selected.", "Must Select A Product", JOptionPane.ERROR_MESSAGE);
				return;
			}
			productsEditor(document, "OK", ((String) products.getSelectedValue()).split(" ")[0], null);
			initialsPanel.refreshInitialAssignmentPanel(document);
			rulesPanel.refreshRulesPanel(document);
		}
		// if the remove products button is clicked
		else if (e.getSource() == removeProduct) {
			removeProduct();
		}
		// if the add modifiers button is clicked
		else if (e.getSource() == addModifier) {
			modifiersEditor("Add");
		}
		// if the edit modifiers button is clicked
		else if (e.getSource() == editModifier) {
			modifiersEditor("OK");
		}
		// if the remove modifiers button is clicked
		else if (e.getSource() == removeModifier) {
			removeModifier();
		}
		// if the clear button is clicked
		else if (e.getSource() == clearKineticLaw) {
			kineticLaw.setText("");
			dirty.setValue(true);
		}
		// if the use mass action button is clicked
		else if (e.getSource() == useMassAction) {
			useMassAction();
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == reactions) {
				if (reactions.getSelectedIndex() == -1) {
					JOptionPane.showMessageDialog(Gui.frame, "No reaction selected.", "Must Select A Reaction", JOptionPane.ERROR_MESSAGE);
					return;
				}
				reactionsEditor(document, "OK", ((String) reactions.getSelectedValue()).split(" ")[0], false);
				initialsPanel.refreshInitialAssignmentPanel(document);
				rulesPanel.refreshRulesPanel(document);
			}
			else if (e.getSource() == reacParameters) {
				reacParametersEditor("OK");
			}
			else if (e.getSource() == reactants) {
				if (!paramsOnly) {
					if (reactants.getSelectedIndex() == -1) {
						JOptionPane.showMessageDialog(Gui.frame, "No reactant selected.", "Must Select A Reactant", JOptionPane.ERROR_MESSAGE);
						return;
					}
					reactantsEditor(document, "OK", ((String) reactants.getSelectedValue()).split(" ")[0], null);
					initialsPanel.refreshInitialAssignmentPanel(document);
					rulesPanel.refreshRulesPanel(document);
				}
			}
			else if (e.getSource() == products) {
				if (!paramsOnly) {
					if (products.getSelectedIndex() == -1) {
						JOptionPane.showMessageDialog(Gui.frame, "No product selected.", "Must Select A Product", JOptionPane.ERROR_MESSAGE);
						return;
					}
					productsEditor(document, "OK", ((String) products.getSelectedValue()).split(" ")[0], null);
					initialsPanel.refreshInitialAssignmentPanel(document);
					rulesPanel.refreshRulesPanel(document);
				}
			}
			else if (e.getSource() == modifiers) {
				if (!paramsOnly) {
					modifiersEditor("OK");
				}
			}
		}
	}
	
	/**
	 * Refresh reaction panel
	 */
	public void refreshReactionPanel(SBMLDocument document) {
		String selectedReactionId = "";
		if (!reactions.isSelectionEmpty()) {
			selectedReactionId = ((String) reactions.getSelectedValue()).split(" ")[0];
		}
		this.document = document;
		Model model = document.getModel();
		ListOf listOfReactions = model.getListOfReactions();
		reacts = new String[(int) model.getNumReactions()];
		for (int i = 0; i < model.getNumReactions(); i++) {
			Reaction reaction = (Reaction) listOfReactions.get(i);
			reacts[i] = reaction.getId();
			ListOf params = reaction.getKineticLaw().getListOfParameters();
			for (int j = 0; j < reaction.getKineticLaw().getNumParameters(); j++) {
				Parameter paramet = ((Parameter) (params.get(j)));
				for (int k = 0; k < parameterChanges.size(); k++) {
					if (parameterChanges.get(k).split(" ")[0].equals(reaction.getId() + "/" + paramet.getId())) {
						String[] splits = parameterChanges.get(k).split(" ");
						if (splits[splits.length - 2].equals("Modified") || splits[splits.length - 2].equals("Custom")) {
							String value = splits[splits.length - 1];
							paramet.setValue(Double.parseDouble(value));
						}
						else if (splits[splits.length - 2].equals("Sweep")) {
							String value = splits[splits.length - 1];
							paramet.setValue(Double.parseDouble(value.split(",")[0].substring(1).trim()));
						}
						if (!reacts[i].contains("Modified")) {
							reacts[i] += " Modified";
						}
					}
				}
			}
		}
		Utility.sort(reacts);
		int selected = 0;
		for (int i = 0; i < reacts.length; i++) {
			if (reacts[i].split(" ")[0].equals(selectedReactionId)) {
				selected = i;
			}
		}
		reactions.setListData(reacts);
		reactions.setSelectedIndex(selected);
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseReleased(MouseEvent e) {
	}

}
