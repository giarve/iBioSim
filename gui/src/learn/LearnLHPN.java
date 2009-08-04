package learn;

//import gcm2sbml.parser.GCMFile;
import lhpn2sbml.parser.LHPNFile;
import parser.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.prefs.Preferences;

import javax.swing.*;

//import org.sbml.libsbml.*;
import biomodelsim.*;

/**
 * This class creates a GUI for the Learn program. It implements the
 * ActionListener class. This allows the GUI to perform actions when menu items
 * and buttons are selected.
 * 
 * @author Curtis Madsen
 */
public class LearnLHPN extends JPanel implements ActionListener, Runnable, ItemListener { // added ItemListener SB

	private static final long serialVersionUID = -5806315070287184299L;

	private JButton save, run, viewLhpn, saveLhpn, viewLog; // the run button

	private JComboBox debug; // debug combo box

	private JTextField iteration, backgroundField;

	// private JTextField windowRising, windowSize;

	private JComboBox numBins;

	private JCheckBox basicFBP;

	private ArrayList<ArrayList<Component>> variables;

	private JPanel variablesPanel; //, basicOpt, advancedOpt;

	private JRadioButton user, auto, range, points;

	private JButton suggest;

	private String directory, lrnFile;

	private JLabel numBinsLabel;

	private Log log;

	private String separator;

	private BioSim biosim;

	private String learnFile, binFile, newBinFile, lhpnFile;

	private boolean change, fail;

	private ArrayList<String> variablesList;

	private boolean firstRead, generate, execute;

	// SB

	private ArrayList<Variable> reqdVarsL;

	private ArrayList<Integer> reqdVarIndices;

	private ArrayList<ArrayList<Double>> data;

	private ArrayList<String> varNames;

	private int[][] bins;

	private ArrayList<ArrayList<Double>> divisionsL;

	private Double[][] rates;

	private Double[] duration;

	private int dmvcCnt = 0;

	private int pathLength ; //= 7 ;// intFixed 25 pd 7 integrator 15;

	private int rateSampling ; //= -1 ; //intFixed 250; 20; //-1;

	private boolean placeRates = true;

	private LHPNFile g;

	private Integer numPlaces = 0;

	private Integer numTransitions = 0;

	HashMap<String, Properties> placeInfo;

	HashMap<String, Properties> transitionInfo;

	/*
	 * public enum PType { RATE, DMVC, PROP, ASGN, TRACE }
	 */
	private Double minDelayVal = 10.0;

	private Double minRateVal = 10.0;

	private Double minDivisionVal = 10.0;

	// private String decPercent;

	// private boolean limitExists;

	private Double delayScaleFactor = 1.0;

	private Double varScaleFactor = 1.0;

	BufferedWriter out;

	File logFile;

	// Threshold parameters
	private double epsilon ;//= 0.1; // What is the +/- epsilon where signals are considered to be equivalent

	private int runLength ; //= 15; // the number of time points that a value must persist to be considered constant

	private double runTime ; // = 5e-12; // 10e-6 for intFixed; 5e-6 for integrator. 5e-12 for pd;// the amount of time that must pass to be considered constant when using absoluteTime

	private boolean absoluteTime ; // = true; // true for intfixed //false; true for pd; false for integrator// when False time points are used to determine DMVC and when true absolutime time is used to determine DMVC

	private double percent ; // = 0.8; // a decimal value representing the percent of the total trace that must be constant to qualify to become a DMVC var

	private JTextField epsilonG;
	
	private JTextField percentG;
	
	private JCheckBox absTimeG;
	
	private JTextField pathLengthG;
	
	private JTextField rateSamplingG;
	
	private JTextField runTimeG;
	
	private JTextField runLengthG;
	
	//private ArrayList<Boolean> tempPorts;
	
	// private int[] numValuesL;// the number of constant values for each variable...-1 indicates that the variable isn't considered a DMVC variable

	// private double vaRateUpdateInterval = 1e-6;// how often the rate is added
	// to the continuous variable in the Verilog-A model output

	// Pattern lParenR = Pattern.compile("\\(+"); //SB

	// Pattern absoluteTimeR = Pattern.compile(".absoluteTime"); //SB

	// Pattern falseR = Pattern.compile("false",Pattern.CASE_INSENSITIVE); //pass the I flag to be case insensitive

	/**
	 * This is the constructor for the Learn class. It initializes all the input
	 * fields, puts them on panels, adds the panels to the frame, and then
	 * displays the frame.
	 */
	public LearnLHPN(String directory, Log log, BioSim biosim) {
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		} else {
			separator = File.separator;
		}

		this.biosim = biosim;
		this.log = log;
		this.directory = directory;
		String[] getFilename = directory.split(separator);
		lrnFile = getFilename[getFilename.length - 1] + ".lrn";
//		binFile = getFilename[getFilename.length - 1] + ".bins";
//		newBinFile = getFilename[getFilename.length - 1] + "_NEW" + ".bins";
		lhpnFile = getFilename[getFilename.length - 1] + ".lpn";
		Preferences biosimrc = Preferences.userRoot();

		// Sets up the encodings area
		JPanel radioPanel = new JPanel(new BorderLayout());
		JPanel selection1 = new JPanel();
		JPanel selection2 = new JPanel();
		JPanel selection = new JPanel(new BorderLayout());

		/*
		 * spacing = new JRadioButton("Equal Spacing Of Bins"); data = new
		 * JRadioButton("Equal Data Per Bins");
		 */

		range = new JRadioButton("Minimize Range of Rates");
		points = new JRadioButton("Equalize Points Per Bin");
		user = new JRadioButton("Use User Generated Levels");
		auto = new JRadioButton("Use Auto Generated Levels");
		suggest = new JButton("Suggest Levels");
		ButtonGroup select = new ButtonGroup();
		select.add(auto);
		select.add(user);
		ButtonGroup select2 = new ButtonGroup();
		select2.add(range);
		select2.add(points);
		// if (biosimrc.get("biosim.learn.autolevels", "").equals("Auto")) {
		// auto.setSelected(true);
		// }
		// else {
		// user.setSelected(true);
		// }
		user.addActionListener(this);
		range.addActionListener(this);
		auto.addActionListener(this);
		suggest.addActionListener(this);
		// if (biosimrc.get("biosim.learn.equaldata", "").equals("Equal Data Per
		// Bins")) {
		// data.setSelected(true);
		// }
		// else {
		range.setSelected(true);
		// }
		points.addActionListener(this);
		selection1.add(points);
		selection1.add(range);
		selection2.add(auto);
		selection2.add(user);
		selection2.add(suggest);
		auto.setSelected(true);
		selection.add(selection1, "North");
		selection.add(selection2, "Center");
		suggest.setEnabled(false);
		JPanel encodingPanel = new JPanel(new BorderLayout());
		variablesPanel = new JPanel();
		JPanel sP = new JPanel();
		((FlowLayout) sP.getLayout()).setAlignment(FlowLayout.LEFT);
		sP.add(variablesPanel);
		JLabel encodingsLabel = new JLabel("Variable Levels:");
		JScrollPane scroll2 = new JScrollPane();
		scroll2.setMinimumSize(new Dimension(260, 200));
		scroll2.setPreferredSize(new Dimension(276, 132));
		scroll2.setViewportView(sP);
		radioPanel.add(selection, "North");
		radioPanel.add(encodingPanel, "Center");
		encodingPanel.add(encodingsLabel, "North");
		encodingPanel.add(scroll2, "Center");

		// Sets up initial network and experiments text fields
		// JPanel initNet = new JPanel();
		// JLabel initNetLabel = new JLabel("Background Knowledge Network:");
		// browseInit = new JButton("Browse");
		// browseInit.addActionListener(this);
		// initNetwork = new JTextField(39);
		// initNet.add(initNetLabel);
		// initNet.add(initNetwork);
		// initNet.add(browseInit);

		// Sets up the thresholds area
		JPanel thresholdPanel2 = new JPanel(new GridLayout(8, 2));
		JPanel thresholdPanel1 = new JPanel(new GridLayout(4, 2));
		
		JPanel binsFilePanel1 = new JPanel(new GridLayout(5, 2)); // SB
		JPanel binsFilePanel2 = new JPanel(new GridLayout(5, 2)); // SB
		JPanel binsFilePanelTop = new JPanel(new GridLayout(0, 1));
		JPanel binsFilePanelBottom = new JPanel(new GridLayout(0, 1));
		JPanel binsFilePanel = new JPanel();
		JPanel binsFileHoldPanel = new JPanel();
		JLabel backgroundLabel = new JLabel("Model File:");
		backgroundField = new JTextField(lhpnFile);
		backgroundField.setEditable(false);
		thresholdPanel1.add(backgroundLabel);
		thresholdPanel1.add(backgroundField);
		JLabel iterationLabel = new JLabel("Iterations of Optimization Algorithm");
		iteration = new JTextField("10");
		thresholdPanel1.add(iterationLabel);
		thresholdPanel1.add(iteration);
		
		// SB
		
		JLabel rateLabel = new JLabel("Parameters for Rate calculation");
		binsFilePanelTop.add(rateLabel, "North");
		JLabel epsilonLabel = new JLabel("Epsilon");
		epsilonG = new JTextField("0.1");
		binsFilePanel1.add(epsilonLabel);
		binsFilePanel1.add(epsilonG);
		JLabel pathLengthLabel = new JLabel("Pathlength");
		pathLengthG = new JTextField("15");
		binsFilePanel1.add(pathLengthLabel);
		binsFilePanel1.add(pathLengthG);
		JLabel rateSamplingLabel = new JLabel("Rate Sampling");
		rateSamplingG = new JTextField("-1");
		binsFilePanel1.add(rateSamplingLabel);
		binsFilePanel1.add(rateSamplingG);
		binsFilePanelTop.add(binsFilePanel1,"Center");
		JLabel dmvcLabel = new JLabel("Parameters for DMVC determination");
		binsFilePanelBottom.add(dmvcLabel,"North");
		JLabel absTimeLabel = new JLabel("Absolute Time");
		absTimeG = new JCheckBox();
		binsFilePanel2.add(absTimeLabel);
		binsFilePanel2.add(absTimeG);
		absTimeG.setSelected(true);
		absTimeG.addItemListener(this); 
		JLabel percentLabel = new JLabel("Percent");
		percentG = new JTextField("0.8");
		binsFilePanel2.add(percentLabel);
		binsFilePanel2.add(percentG);
		JLabel runTimeLabel = new JLabel("Dmvc Run Time");
		runTimeG = new JTextField("5e-6");
		binsFilePanel2.add(runTimeLabel);
		binsFilePanel2.add(runTimeG);
		JLabel runLengthLabel = new JLabel("DMVC Run Length");
		runLengthG = new JTextField("15");
		binsFilePanel2.add(runLengthLabel);
		binsFilePanel2.add(runLengthG);
		runLengthG.setEnabled(false);
		binsFilePanelBottom.add(binsFilePanel2,"Center");
	//	((FlowLayout) binsFilePanel.getLayout()).setAlignment(FlowLayout.LEFT);
		binsFilePanel.add(binsFilePanelTop);
	//	((FlowLayout) binsFilePanel.getLayout()).setAlignment(FlowLayout.RIGHT);
		binsFilePanel.add(binsFilePanelBottom);
		binsFileHoldPanel.add(binsFilePanel);
		
		epsilonG.addActionListener(this); //SB
		pathLengthG.addActionListener(this); //SB
		rateSamplingG.addActionListener(this); //SB
		percentG.addActionListener(this); //SB
		runTimeG.addActionListener(this); //SB
		runLengthG.addActionListener(this); //SB
		
		divisionsL = new ArrayList<ArrayList<Double>>(); // SB
		reqdVarsL = new ArrayList<Variable>();
		/*
		 * JLabel activationLabel = new JLabel("Ratio For Activation (Ta):");
		 * thresholdPanel2.add(activationLabel); activation = new
		 * JTextField(biosimrc.get("biosim.learn.ta", "")); //
		 * activation.addActionListener(this); thresholdPanel2.add(activation);
		 * JLabel repressionLabel = new JLabel("Ratio For Repression (Tr):");
		 * thresholdPanel2.add(repressionLabel); repression = new
		 * JTextField(biosimrc.get("biosim.learn.tr", "")); //
		 * repression.addActionListener(this); thresholdPanel2.add(repression);
		 * JLabel influenceLevelLabel = new JLabel("Merge Influence Vectors
		 * Delta (Tm):"); thresholdPanel2.add(influenceLevelLabel);
		 * influenceLevel = new JTextField(biosimrc.get("biosim.learn.tm", "")); //
		 * influenceLevel.addActionListener(this);
		 * thresholdPanel2.add(influenceLevel); JLabel letNThroughLabel = new
		 * JLabel("Minimum Number Of Initial Vectors (Tn): ");
		 * thresholdPanel1.add(letNThroughLabel); letNThrough = new
		 * JTextField(biosimrc.get("biosim.learn.tn", "")); //
		 * letNThrough.addActionListener(this);
		 * thresholdPanel1.add(letNThrough); JLabel maxVectorSizeLabel = new
		 * JLabel("Maximum Influence Vector Size (Tj):");
		 * thresholdPanel1.add(maxVectorSizeLabel); maxVectorSize = new
		 * JTextField(biosimrc.get("biosim.learn.tj", "")); //
		 * maxVectorSize.addActionListener(this);
		 * thresholdPanel1.add(maxVectorSize); JLabel parentLabel = new
		 * JLabel("Score For Empty Influence Vector (Ti):");
		 * thresholdPanel1.add(parentLabel); parent = new
		 * JTextField(biosimrc.get("biosim.learn.ti", ""));
		 * parent.addActionListener(this); thresholdPanel1.add(parent); JLabel
		 * relaxIPDeltaLabel = new JLabel("Relax Thresholds Delta (Tt):");
		 * thresholdPanel2.add(relaxIPDeltaLabel); relaxIPDelta = new
		 * JTextField(biosimrc.get("biosim.learn.tt", "")); //
		 * relaxIPDelta.addActionListener(this);
		 * thresholdPanel2.add(relaxIPDelta);
		 */

		numBinsLabel = new JLabel("Number of Bins:");
		String[] bins = { "2", "3", "4", "5", "6", "7", "8", "9" };
		numBins = new JComboBox(bins);
		numBins.setSelectedItem(biosimrc.get("biosim.learn.bins", ""));
		numBins.addActionListener(this);
		numBins.setActionCommand("text");
		thresholdPanel1.add(numBinsLabel);
		thresholdPanel1.add(numBins);
		JPanel thresholdPanelHold1 = new JPanel();
		thresholdPanelHold1.add(thresholdPanel1);
		JLabel debugLabel = new JLabel("Debug Level:");
		String[] options = new String[4];
		options[0] = "0";
		options[1] = "1";
		options[2] = "2";
		options[3] = "3";
		debug = new JComboBox(options);
		// debug.setSelectedItem(biosimrc.get("biosim.learn.debug", ""));
		debug.addActionListener(this);
		thresholdPanel2.add(debugLabel);
		thresholdPanel2.add(debug);
		// succ = new JRadioButton("Successors");
		// pred = new JRadioButton("Predecessors");
		// both = new JRadioButton("Both");
		// if (biosimrc.get("biosim.learn.succpred", "").equals("Successors")) {
		// succ.setSelected(true);
		// }
		// else if (biosimrc.get("biosim.learn.succpred",
		// "").equals("Predecessors")) {
		// pred.setSelected(true);
		// }
		// else {
		// both.setSelected(true);
		// }
		// succ.addActionListener(this);
		// pred.addActionListener(this);
		// both.addActionListener(this);
		basicFBP = new JCheckBox("Basic FindBaseProb");
		// if (biosimrc.get("biosim.learn.findbaseprob", "").equals("True")) {
		// basicFBP.setSelected(true);
		// }
		// else {
		basicFBP.setSelected(false);
		// }
		basicFBP.addActionListener(this);
		// ButtonGroup succOrPred = new ButtonGroup();
		// succOrPred.add(succ);
		// succOrPred.add(pred);
		// succOrPred.add(both);
		JPanel three = new JPanel();
		// three.add(succ);
		// three.add(pred);
		// three.add(both);
		((FlowLayout) three.getLayout()).setAlignment(FlowLayout.LEFT);
		thresholdPanel2.add(three);
		thresholdPanel2.add(new JPanel());
		thresholdPanel2.add(basicFBP);
		thresholdPanel2.add(new JPanel());
		// JPanel thresholdPanelHold2 = new JPanel();
		// thresholdPanelHold2.add(thresholdPanel2);

		/*
		 * JLabel windowRisingLabel = new JLabel("Window Rising Amount:");
		 * windowRising = new JTextField("1");
		 * thresholdPanel2.add(windowRisingLabel);
		 * thresholdPanel2.add(windowRising); JLabel windowSizeLabel = new
		 * JLabel("Window Size:"); windowSize = new JTextField("1");
		 * thresholdPanel2.add(windowSizeLabel);
		 * thresholdPanel2.add(windowSize); harshenBoundsOnTie = new
		 * JCheckBox("Harshen Bounds On Tie");
		 * harshenBoundsOnTie.setSelected(true); donotInvertSortOrder = new
		 * JCheckBox("Do Not Invert Sort Order");
		 * donotInvertSortOrder.setSelected(true); seedParents = new
		 * JCheckBox("Parents Should Be Ranked By Score");
		 * seedParents.setSelected(true); mustNotWinMajority = new
		 * JCheckBox("Must Not Win Majority");
		 * mustNotWinMajority.setSelected(true); donotTossSingleRatioParents =
		 * new JCheckBox("Single Ratio Parents Should Be Kept");
		 * donotTossChangedInfluenceSingleParents = new JCheckBox( "Parents That
		 * Change Influence Should Not Be Tossed");
		 * thresholdPanel2.add(harshenBoundsOnTie);
		 * thresholdPanel2.add(donotInvertSortOrder);
		 * thresholdPanel2.add(seedParents);
		 * thresholdPanel2.add(mustNotWinMajority);
		 * thresholdPanel2.add(donotTossSingleRatioParents);
		 * thresholdPanel2.add(donotTossChangedInfluenceSingleParents);
		 */

		// load parameters
		// reading lrnFile twice. On the first read, only learnFile (the initial lpn) is processed
		// in the gap b/w these reads, reqdVarsL is created based on the learnFile
		Properties load = new Properties();
		learnFile = "";
		
		try {
			FileInputStream in = new FileInputStream(new File(directory
					+ separator + lrnFile));
			load.load(in);
			in.close();
			if (load.containsKey("learn.file")) {
				String[] getProp = load.getProperty("learn.file").split(
						separator);
				learnFile = directory.substring(0, directory.length()
						- getFilename[getFilename.length - 1].length())
						+ separator + getProp[getProp.length - 1];
				backgroundField.setText(getProp[getProp.length - 1]);
				
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(biosim.frame(),
					"Unable to load properties file!",
					"Error Loading Properties", JOptionPane.ERROR_MESSAGE);
		}
		// SB
		variablesList = new ArrayList<String>();
		LHPNFile lhpn = new LHPNFile(log);
		// log.addText(learnFile);
		lhpn.load(learnFile);
		HashMap<String, Properties> variablesMap = lhpn.getVariables();
		for (String s : variablesMap.keySet()) {
			variablesList.add(s);
			reqdVarsL.add(new Variable(s));
			divisionsL.add(new ArrayList<Double>());
		}
		// System.out.println(variablesList);
		try {
			FileInputStream in = new FileInputStream(new File(directory
					+ separator + lrnFile));
			load.load(in);
			in.close();
			if (load.containsKey("learn.iter")) {
				iteration.setText(load.getProperty("learn.iter"));
			}
			if (load.containsKey("learn.bins")) {
				numBins.setSelectedItem(load.getProperty("learn.bins"));
			}
			if (load.containsKey("learn.equal")) {
				if (load.getProperty("learn.equal").equals("range")) {
					range.setSelected(true);
				} else {
					points.setSelected(true);
				}
			}
			if (load.containsKey("learn.use")) {
				if (load.getProperty("learn.use").equals("auto")) {
					auto.setSelected(true);
				} else if (load.getProperty("learn.use").equals("user")) {
					user.setSelected(true);
				}
			}
			if (load.containsKey("learn.epsilon")){
				epsilonG.setText(load.getProperty("learn.epsilon"));
			}
			if (load.containsKey("learn.rateSampling")){
				rateSamplingG.setText(load.getProperty("learn.rateSampling"));
			}
			if (load.containsKey("learn.pathLength")){
				pathLengthG.setText(load.getProperty("learn.pathLength"));
			}
			if (load.containsKey("learn.percent")){
				percentG.setText(load.getProperty("learn.percent"));
			}
			if (load.containsKey("learn.absTime")){
				absTimeG.setSelected(Boolean.parseBoolean(load.getProperty("learn.absTime")));
			}
			if (load.containsKey("learn.runTime")){
				runTimeG.setText(load.getProperty("learn.runTime"));
			}
			if (load.containsKey("learn.runLength")){
				runLengthG.setText(load.getProperty("learn.runLength"));
			}
			
			int j = 0;
			//levels();
			while (load.containsKey("learn.bins"+j)){
				String s = load.getProperty("learn.bins" + j);
				String[] savedBins = s.split("\\s");
				//divisionsL.add(new ArrayList<Double>());
				//variablesList.add(savedBins[0]);
			//	((JComboBox)(((JPanel)variablesPanel.getComponent(j+1)).getComponent(2))).setSelectedItem(savedBins[1]);
				for (int i = 2; i < savedBins.length ; i++){
			//		((JTextField)(((JPanel)variablesPanel.getComponent(j+1)).getComponent(i+1))).setText(savedBins[i]);
					divisionsL.get(j).add(Double.parseDouble(savedBins[i]));
				}
				j++;
			}
			if (load.containsKey("learn.inputs")){
				String s = load.getProperty("learn.inputs");
				String[] savedInputs = s.split("\\s");
				for (String st1 : savedInputs){
					reqdVarsL.get(Integer.parseInt(st1)).setInput(true);
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(biosim.frame(),
					"Unable to load properties file!",
					"Error Loading Properties", JOptionPane.ERROR_MESSAGE);
		}
		try {
			FileWriter write = new FileWriter(new File(directory + separator + "background.g"));
			BufferedReader input = new BufferedReader(new FileReader(new File(learnFile)));
			String line = null;
			while ((line = input.readLine()) != null) {
				write.write(line + "\n");
			}
			write.close();
			input.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(biosim.frame(),
					"Unable to create background file!",
					"Error Writing Background", JOptionPane.ERROR_MESSAGE);
		}

		// sortSpecies();
		JPanel runHolder = new JPanel();
		autogen(false);
		if (auto.isSelected()) {
			auto.doClick();
		} else {
			user.doClick();
		}

		// Creates the run button
		run = new JButton("Save and Learn");
		runHolder.add(run);
		run.addActionListener(this);
		run.setMnemonic(KeyEvent.VK_L);

		// Creates the run button
		save = new JButton("Save Parameters");
		runHolder.add(save);
		save.addActionListener(this);
		save.setMnemonic(KeyEvent.VK_S);

		// Creates the view circuit button
		viewLhpn = new JButton("View Circuit");
		runHolder.add(viewLhpn);
		viewLhpn.addActionListener(this);
		viewLhpn.setMnemonic(KeyEvent.VK_V);

		// Creates the save circuit button
		saveLhpn = new JButton("Save Circuit");
		runHolder.add(saveLhpn);
		saveLhpn.addActionListener(this);
		saveLhpn.setMnemonic(KeyEvent.VK_C);

		// Creates the view circuit button
		viewLog = new JButton("View Run Log");
		runHolder.add(viewLog);
		viewLog.addActionListener(this);
		viewLog.setMnemonic(KeyEvent.VK_R);
		if (!(new File(directory + separator + lhpnFile).exists())) {
			viewLhpn.setEnabled(false);
			saveLhpn.setEnabled(false);
		}
		if (!(new File(directory + separator + "run.log").exists())) {
			viewLog.setEnabled(false);
		}

		// Creates the main panel
		this.setLayout(new BorderLayout());
		JPanel middlePanel = new JPanel(new BorderLayout());
		JPanel firstTab = new JPanel(new BorderLayout());
		JPanel firstTab1 = new JPanel(new BorderLayout());
		JPanel secondTab = new JPanel(new BorderLayout()); // SB uncommented
		middlePanel.add(radioPanel, "Center");
		// firstTab1.add(initNet, "North");
		firstTab1.add(thresholdPanelHold1, "Center");
		firstTab.add(firstTab1, "North");
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				middlePanel, null);
		splitPane.setDividerSize(0);
		// secondTab.add(thresholdPanelHold2, "North");
	//	JPanel binsFileHoldPanel = new JPanel();
	//	binsFileHoldPanel.add(binsFilePanel);
		//binsFileHoldPanel.setMinimumSize(new Dimension(10000,16000));
		//binsFileHoldPanel.setPreferredSize(getPreferredSize());
		secondTab.add(binsFileHoldPanel, "Center");
		firstTab.add(splitPane, "Center");
		JTabbedPane tab = new JTabbedPane();
		tab.addTab("Basic Options", firstTab);
		tab.addTab("Advanced Options", secondTab);
		this.add(tab, "Center");
		//this.addTab("Basic", (JComponent)firstTab);
		//this.addTab("Advanced", (JComponent)firstTab);
		// this.add(runHolder, "South");
		firstRead = true;
		// if (user.isSelected()) {
		// auto.doClick();
		// user.doClick();
		// }
		// else {
		// user.doClick();
		// auto.doClick();
		// }
		firstRead = false;
		change = false;
	}

	/**
	 * This method performs different functions depending on what menu items or
	 * buttons are selected.
	 */
	public void actionPerformed(ActionEvent e) {
		/*
		 * if (e.getActionCommand().contains("box")) { int num =
		 * Integer.parseInt(e.getActionCommand().substring(3)) - 1; if
		 * (!((JCheckBox) this.species.get(num).get(0)).isSelected()) {
		 * ((JComboBox) this.species.get(num).get(2)).setSelectedItem("0");
		 * editText(num); speciesPanel.revalidate(); speciesPanel.repaint(); for
		 * (int i = 1; i < this.species.get(num).size(); i++) {
		 * this.species.get(num).get(i).setEnabled(false); } } else {
		 * this.species.get(num).get(1).setEnabled(true); if (user.isSelected()) {
		 * for (int i = 2; i < this.species.get(num).size(); i++) {
		 * this.species.get(num).get(i).setEnabled(true); } } } } else
		 */
		change = true;
		if (e.getActionCommand().contains("text")) {
			// int num = Integer.parseInt(e.getActionCommand().substring(4)) -
			// 1;
			if (variables != null && user.isSelected()) {
				for (int i = 0; i < variables.size(); i++) {
					editText(i);
				}
			}
			variablesPanel.revalidate();
			variablesPanel.repaint();
			int j = 0;
			for (Component c : variablesPanel.getComponents()) {
				if (j == 0){   // recheck .. SB
					j++;		// SB
					continue;	// SB
				}				//SB
				if (reqdVarsL.get(j-1).isInput()){ //tempPorts.get(j-1)){
					((JCheckBox)((JPanel) c).getComponent(1)).setSelected(true); // SB
				}	
				j++;
			}
			biosim.setGlassPane(true);
		} else if (e.getSource() == numBins || e.getSource() == debug) {
			biosim.setGlassPane(true);
		} else if (e.getActionCommand().contains("dmv")) {
			int num = Integer.parseInt(e.getActionCommand().substring(3)) - 1;
			editText(num);
		} else if (e.getActionCommand().contains("input")) {
			int num = Integer.parseInt(e.getActionCommand().substring(5));  // -1; ??
			reqdVarsL.get(num).setInput(!reqdVarsL.get(num).isInput());
			//tempPorts.set(num, !tempPorts.get(num).booleanValue());
			//editText(num);
		}
		else if (e.getSource() == user) {
			if (!firstRead) {
				try {
//					FileWriter write = new FileWriter(new File(directory + separator + binFile));
					// write.write("time 0\n");
					for (int i = 0; i < variables.size(); i++) {
//						if (((JTextField) variables.get(i).get(0)).getText().trim().equals("")) {
//							write.write("?");
//						} else {
//							write.write(((JTextField) variables.get(i).get(0)).getText().trim());
//						}
						// write.write(" " + ((JComboBox) variables.get(i).get(1)).getSelectedItem());
						for (int j = 3; j < variables.get(i).size(); j++) { // changed 2 to 3 SB
							if (((JTextField) variables.get(i).get(j)).getText().trim().equals("")) {
//								write.write(" ?");
								divisionsL.get(i).set(j-3,null);
							} else {
//								write.write(" " + ((JTextField) variables.get(i).get(j)).getText().trim());
								divisionsL.get(i).set(j-3,Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
							}
						}
//						write.write("\n");
					}
//					write.close();
				} catch (Exception e1) {
				}
			}
			numBinsLabel.setEnabled(false);
			numBins.setEnabled(false);
			suggest.setEnabled(true);
			// levelsBin();
			variablesPanel.revalidate();
			variablesPanel.repaint();
			levels();
		} else if (e.getSource() == auto) {
			numBinsLabel.setEnabled(true);
			numBins.setEnabled(true);
			suggest.setEnabled(false);
			//int j = 0;  // recheck .. SB
			for (Component c : variablesPanel.getComponents()) {
				for (int i = 2; i < ((JPanel) c).getComponentCount(); i++) { // changed 1 to 2 SB
					((JPanel) c).getComponent(i).setEnabled(false);
				}
			/*	if (j == 0){   // recheck .. SB
					j++;		// SB
					continue;	// SB
				}				//SB
				((JCheckBox)((JPanel) c).getComponent(1)).addActionListener(this); // SB
				((JCheckBox)((JPanel) c).getComponent(1)).setActionCommand("input" + j); // SB */
			}
		} else if (e.getSource() == suggest) {
			autogen(false);
			variablesPanel.revalidate();
			variablesPanel.repaint();
			int j = 0;
			for (Component c : variablesPanel.getComponents()) {
				if (j == 0){   // recheck .. SB
					j++;		// SB
					continue;	// SB
				}				//SB
				if (reqdVarsL.get(j-1).isInput()){ //tempPorts.get(j-1)){
					((JCheckBox)((JPanel) c).getComponent(1)).setSelected(true); // SB
				}
				//if (tempPorts.get(j-1)){
				//	((JCheckBox)((JPanel) c).getComponent(1)).setSelected(true); // SB
				//}	
				j++;
			}
		} 
		// if the browse initial network button is clicked
		// else if (e.getSource() == browseInit) {
		// Buttons.browse(this, new File(initNetwork.getText().trim()),
		// initNetwork,
		// JFileChooser.FILES_ONLY, "Open");
		// }
		// if the run button is selected
		else if (e.getSource() == run) {
			save();
			learn();
		} else if (e.getSource() == save) {
			save();
		} else if (e.getSource() == viewLhpn) {
			viewLhpn();
		} else if (e.getSource() == viewLog) {
			viewLog();
		} else if (e.getSource() == saveLhpn) {
			saveLhpn();
		}
	}

	public void itemStateChanged(ItemEvent e) {
	    Object source = e.getItemSelectable();
	    if (source == absTimeG) {
	        absoluteTime = !absoluteTime; 
	        if (e.getStateChange() == ItemEvent.DESELECTED){
		    	absTimeG.setSelected(false);
		    	runTimeG.setEnabled(false);
		    	runLengthG.setEnabled(true);
		    }
		    else{
		    	absTimeG.setSelected(true);
		    	runTimeG.setEnabled(true);
		    	runLengthG.setEnabled(false);
		    }
	    } 
	}

	private void autogen(boolean readfile) {
		try {
			if (!readfile) {
//				FileWriter write = new FileWriter(new File(directory + separator + binFile));
//				FileWriter writeNew = new FileWriter(new File(directory	+ separator + newBinFile));
				// write.write("time 0\n");
				// boolean flag = false;
				// for (int i = 0; i < variables.size(); i++) {
				// if (((JCheckBox) variables.get(i).get(1)).isSelected()) {
				// if (!flag) {
				// write.write(".dmvc ");
				// writeNew.write(".dmvc ");
				// flag = true;
				// }
				// write.write(((JTextField)
				// variables.get(i).get(0)).getText().trim() + " ");
				// writeNew.write(((JTextField)
				// variables.get(i).get(0)).getText().trim()
				// + " ");
				// }
				// }
				// if (flag) {
				// write.write("\n");
				// writeNew.write("\n");
				// }
				for (int i = 0; i < variables.size(); i++) {
					// if (!((JCheckBox) variables.get(i).get(1)).isSelected())
					// {
//						if (((JTextField) variables.get(i).get(0)).getText().trim().equals("")) {
//						write.write("?");
//						writeNew.write("?");
//					} else {
//						write.write(((JTextField) variables.get(i).get(0)).getText().trim());
//						writeNew.write(((JTextField) variables.get(i).get(0)).getText().trim());
//					}
					// write.write(" " + ((JComboBox)
					// variables.get(i).get(1)).getSelectedItem());
					for (int j = 3; j < variables.get(i).size(); j++) {  // changed 2 to 3 SB
						if (((JTextField) variables.get(i).get(j)).getText().trim().equals("")) {
//							write.write(" ?");
//							writeNew.write(" ?");
							divisionsL.get(i).set(j-3,null);
						} else {
//							write.write(" "	+ ((JTextField) variables.get(i).get(j)).getText().trim());
//							writeNew.write(" " + ((JTextField) variables.get(i).get(j)).getText().trim());
							divisionsL.get(i).set(j-3,Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
						}
					}
//					write.write("\n");
//					writeNew.write("\n");
					// }
				}
//				write.close();
//				writeNew.close();
				// Integer numThresh =
				// Integer.parseInt(numBins.getSelectedItem().toString()) - 1;
				// Thread myThread = new Thread(this);
				generate = true;
				execute = false;
				new Thread(this).start();
			}
		} catch (Exception e1) {
			// e1.printStackTrace();
			levels();
		}
	}

	private void levels() {  // based on the current data, create/update the variablesPanel???
	/*	ArrayList<String> str = null;
		try {
			Scanner f = new Scanner(new File(directory + separator + binFile));
			// log.addText(directory + separator + binFile);
			str = new ArrayList<String>();
			str.add(f.nextLine());
			while (f.hasNextLine()) {
				str.add(f.nextLine());
			}
			f.close();
			// System.out.println("here " + str.toString());
		} catch (Exception e1) {
		} */
		if (!directory.equals("")) {
			if (true) {
				// System.out.println(str.toString());
				variablesPanel.removeAll();
				this.variables = new ArrayList<ArrayList<Component>>();
				variablesPanel.setLayout(new GridLayout(
						variablesList.size() + 1, 1));
				int max = 0;
			/*	if (str != null) {
					for (String st : str) {
						String[] getString = st.split("\\s");
						max = Math.max(max, getString.length + 1);
					}
				} */
				if (!divisionsL.isEmpty()){
					for (int i = 0; i < divisionsL.size(); i++){
						max = Math.max(max, divisionsL.get(i).size()+2);
					}
				}
				JPanel label = new JPanel(new GridLayout());
				// label.add(new JLabel("Use"));
				label.add(new JLabel("Variables"));
				// label.add(new JLabel("DMV"));
				label.add(new JLabel("Input")); //SB
				label.add(new JLabel("Number Of Bins"));
				for (int i = 0; i < max - 2; i++) {
					label.add(new JLabel("Level " + (i + 1)));
				}
				variablesPanel.add(label);
				int j = 0;
				for (String s : variablesList) {
					// System.out.println(s + str.toString());
					j++;
					JPanel sp = new JPanel(new GridLayout());
					ArrayList<Component> specs = new ArrayList<Component>();
					// JCheckBox check = new JCheckBox();
					// check.setSelected(true);
					// specs.add(check);
					specs.add(new JTextField(s));
					String[] options = { "2", "3", "4", "5", "6", "7", "8", "9" };
					// String[] options = { "3", "4", "5", "6", "7", "8", "9" };
					JComboBox combo = new JComboBox(options);
					// String[] dmvOptions = { "", "Yes", "No" };
					// JComboBox dmv = new JComboBox(dmvOptions);
					JCheckBox dmv = new JCheckBox();
					JCheckBox input = new JCheckBox(); //SB
					// dmv.setSelectedIndex(0);
					dmv.addActionListener(this);
					input.addActionListener(this); //SB
					dmv.setActionCommand("dmv" + j);
					input.setActionCommand("input" + (j-1)); // SB  j-1 or j ??????
					combo.setSelectedItem(numBins.getSelectedItem());
					// specs.add(dmv);
					specs.add(input); //SB
					specs.add(combo);
					((JTextField) specs.get(0)).setEditable(false);
					// sp.add(specs.get(0));
					// ((JCheckBox) specs.get(0)).addActionListener(this);
					// ((JCheckBox) specs.get(0)).setActionCommand("box" + j);
					sp.add(specs.get(0));
					sp.add(specs.get(1));
					sp.add(specs.get(2));  // Uncommented SB
					if (reqdVarsL.get(j-1).isInput()){
						((JCheckBox) specs.get(1)).setSelected(true);
					}
					else{
						((JCheckBox) specs.get(1)).setSelected(false);
					}
					((JComboBox) specs.get(2)).addActionListener(this); // changed 1 to 2 SB
					((JComboBox) specs.get(2)).setActionCommand("text" + j);// changed 1 to 2 SB
					this.variables.add(specs);
					if (!divisionsL.isEmpty()) {
						boolean found = false;
						ArrayList<Double> div =  divisionsL.get(j-1);
							// log.addText(s + " here " + st);
							// String[] getString = st.split(" ");
							// log.addText(getString[0] + s);
							/*if (getString[0].trim().equals(".dmvc")) {
								for (int i = 1; i < getString.length; i++) {
									if (getString[i].equals(s)) {
										// log.addText(s);
										// ((JCheckBox)
										// specs.get(1)).setSelected(true);
									}
								}
							} else if (getString[0].trim().equals(s)) {*/
								found = true;
							//	if (getString.length >= 1) {
								if (div.size() >= 0){
									//((JComboBox) specs.get(2)).setSelectedItem("div.size()+1");// Treats div.size() as string & doesn't work.. changed 1 to 2 SB
									((JComboBox) specs.get(2)).setSelectedItem(String.valueOf(div.size()+1));// changed 1 to 2 SB
									for (int i = 0; i < (Integer.parseInt((String) ((JComboBox) specs.get(2)).getSelectedItem()) - 1); i++) {// changed 1 to 2 SB
										if (div.isEmpty() || div.size() < i) {
											specs.add(new JTextField(""));
										} else {
											// log.addText(getString[i+1]);
											specs.add(new JTextField(div.get(i).toString()));
										}
										// if (((JCheckBox)
										// specs.get(1)).isSelected()) {
										// log.addText("here");
										// ((JTextField) specs.get(i +
										// 2)).setEditable(false);
										// }
										sp.add(specs.get(i + 3)); // changed 2 to 3 SB
									}
									for (int i = Integer.parseInt((String) ((JComboBox) specs.get(2)).getSelectedItem()) - 1; i < max - 2; i++) {// changed 1 to 2 SB
										sp.add(new JLabel());
									}
								}
						//	}
						//}
						if (!found) {
							for (int i = 0; i < Integer.parseInt((String) ((JComboBox) specs.get(2)).getSelectedItem()) - 1; i++) {// changed 1 to 2 SB
								specs.add(new JTextField(""));
								sp.add(specs.get(i + 2));// changed 1 to 2 SB
							}
							for (int i = Integer.parseInt((String) ((JComboBox) specs.get(2)).getSelectedItem()) - 1; i < max - 2; i++) {// changed 1 to 2 SB
								sp.add(new JLabel());
							}
						}
					} else {
						for (int i = 0; i < Integer.parseInt((String) ((JComboBox) specs.get(2)).getSelectedItem()) - 1; i++) {// changed 1 to 2 SB
							specs.add(new JTextField(""));
							sp.add(specs.get(i + 2));// changed 1 to 2 SB
						}
					}
					variablesPanel.add(sp);
				}
			}
		}
		editText(0);
	}

	private void editText(int num) {
		try {
			ArrayList<Component> specs = variables.get(num);
			Component[] panels = variablesPanel.getComponents();
			int boxes = Integer.parseInt((String) ((JComboBox) specs.get(2)).getSelectedItem()); //changed 1 to 2 SB
			if ((specs.size() - 3) < boxes) { // changed 2 to 3 SB
				for (int i = 0; i < boxes - 1; i++) {
					try {
						specs.get(i + 3); // changed 2 to 3 SB
					} catch (Exception e1) {
						JTextField temp = new JTextField("");
						((JPanel) panels[num + 1]).add(temp);
						specs.add(temp);
					}
				}
			} else {
				try {
					if (boxes > 0) {
						while (true) {
							specs.remove(boxes + 2); // changed 1 to 2 SB
							((JPanel) panels[num + 1]).remove(boxes + 2); // changed 1 to 2 SB
						}
					} else if (boxes == 0) {
						while (true) {
							specs.remove(3); // changed 2 to 3 SB
							((JPanel) panels[num + 1]).remove(3); // changed 2 to 3 SB
						}
					}
				} catch (Exception e1) {
				}
			}
			int max = 0;
			for (int i = 0; i < this.variables.size(); i++) {
				max = Math.max(max, variables.get(i).size());
			}
			if (((JPanel) panels[0]).getComponentCount() < max) {
				for (int i = 0; i < max - 3; i++) { //changed 2 to 3 SB
					try {
						((JPanel) panels[0]).getComponent(i + 3); //changed 2 to 3 SB
					} catch (Exception e) {
						((JPanel) panels[0]).add(new JLabel("Level " + (i + 1)));
					}
				}
			} else {
				try {
					while (true) {
						((JPanel) panels[0]).remove(max);
					}
				} catch (Exception e) {
				}
			}
			for (int i = 1; i < panels.length; i++) {
				JPanel sp = (JPanel) panels[i];
				for (int j = sp.getComponentCount() - 1; j >= 3; j--) {//changed 2 to 3 SB
					if (sp.getComponent(j) instanceof JLabel) {
						sp.remove(j);
					}
				}
				if (max > sp.getComponentCount()) {
					for (int j = sp.getComponentCount(); j < max; j++) {
						sp.add(new JLabel());
					}
				} else {
					for (int j = sp.getComponentCount() - 2; j >= max; j--) {//changed 2 to 3 SB .. not sure??
						sp.remove(j);
					}
				}
			}
		} catch (Exception e) {
		}
	}

	public void saveLhpn() {
		try {
			if (true) {// (new File(directory + separator +
				// "method.gcm").exists()) {
				String copy = JOptionPane.showInputDialog(biosim.frame(),
						"Enter Circuit Name:", "Save Circuit",
						JOptionPane.PLAIN_MESSAGE);
				if (copy != null) {
					copy = copy.trim();
				} else {
					return;
				}
				if (!copy.equals("")) {
					if (copy.length() > 1) {
						if (!copy.substring(copy.length() - 2).equals(".lpn")) {
							copy += ".lpn";
						}
					} else {
						copy += ".lpn";
					}
				}
				biosim.saveLhpn(copy, directory + separator + lhpnFile);
			} else {
				JOptionPane.showMessageDialog(biosim.frame(),
						"No circuit has been generated yet.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(biosim.frame(),
					"Unable to save circuit.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void viewLhpn() {
		try {
			File work = new File(directory);
			if (new File(directory + separator + lhpnFile).exists()) {
				String dotFile = lhpnFile.replace(".lpn", ".dot");
				File dot = new File(directory + separator + dotFile);
				dot.delete();
				log.addText("Executing:\n" + "atacs -cPllodpl " + lhpnFile);
				Runtime exec = Runtime.getRuntime();
				Process load = exec.exec("atacs -cPllodpl " + lhpnFile, null,
						work);
				load.waitFor();
				if (dot.exists()) {
					viewLhpn.setEnabled(true);
					String command = "";
					if (System.getProperty("os.name").contentEquals("Linux")) {
						command = "gnome-open " + dotFile;
						log.addText("gnome-open " + directory + separator
								+ dotFile + "\n");
					} else {
						command = "open " + dotFile;
						log.addText("open " + directory + separator + dotFile
								+ "\n");
					}
					exec.exec(command, null, work);
				} else {
					File log = new File(directory + separator + "atacs.log");
					BufferedReader input = new BufferedReader(new FileReader(
							log));
					String line = null;
					JTextArea messageArea = new JTextArea();
					while ((line = input.readLine()) != null) {
						messageArea.append(line);
						messageArea
								.append(System.getProperty("line.separator"));
					}
					input.close();
					messageArea.setLineWrap(true);
					messageArea.setWrapStyleWord(true);
					messageArea.setEditable(false);
					JScrollPane scrolls = new JScrollPane();
					scrolls.setMinimumSize(new Dimension(500, 500));
					scrolls.setPreferredSize(new Dimension(500, 500));
					scrolls.setViewportView(messageArea);
					JOptionPane.showMessageDialog(biosim.frame(), scrolls,
							"Log", JOptionPane.INFORMATION_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(biosim.frame(),
						"No circuit has been generated yet.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(biosim.frame(),
					"Unable to view circuit.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void viewLog() {
		try {
			if (new File(directory + separator + "run.log").exists()) {
				File log = new File(directory + separator + "run.log");
				BufferedReader input = new BufferedReader(new FileReader(log));
				String line = null;
				JTextArea messageArea = new JTextArea();
				while ((line = input.readLine()) != null) {
					messageArea.append(line);
					messageArea.append(System.getProperty("line.separator"));
				}
				input.close();
				messageArea.setLineWrap(true);
				messageArea.setWrapStyleWord(true);
				messageArea.setEditable(false);
				JScrollPane scrolls = new JScrollPane();
				scrolls.setMinimumSize(new Dimension(500, 500));
				scrolls.setPreferredSize(new Dimension(500, 500));
				scrolls.setViewportView(messageArea);
				JOptionPane.showMessageDialog(biosim.frame(), scrolls,
						"Run Log", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(biosim.frame(),
						"No run log exists.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(biosim.frame(),
					"Unable to view run log.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void save() {
		try {
			Properties prop = new Properties();
			FileInputStream in = new FileInputStream(new File(directory
					+ separator + lrnFile));
			prop.load(in);
			in.close();
			prop.setProperty("learn.file", learnFile);
			prop.setProperty("learn.iter", this.iteration.getText().trim());
			prop.setProperty("learn.bins", (String) this.numBins.getSelectedItem());
			if (range.isSelected()) {
				prop.setProperty("learn.equal", "range");
			} else {
				prop.setProperty("learn.equal", "points");
			}
			if (auto.isSelected()) {
				prop.setProperty("learn.use", "auto");
			} else {
				prop.setProperty("learn.use", "user");
			}
			prop.setProperty("learn.epsilon", this.epsilonG.getText().trim());
			prop.setProperty("learn.pathLength", this.pathLengthG.getText().trim());
			prop.setProperty("learn.rateSampling", this.rateSamplingG.getText().trim());
			prop.setProperty("learn.percent", this.percentG.getText().trim());
			prop.setProperty("learn.absTime",String.valueOf(this.absTimeG.isSelected()));
			prop.setProperty("learn.runTime",this.runTimeG.getText().trim());
			prop.setProperty("learn.runLength",this.runLengthG.getText().trim());
			int k = 0;
			int inputCount = 0;
			String ip = null;
			for (Component c : variablesPanel.getComponents()) {
				if (k == 0){
					k++;
					continue;
				}
				String s =  ((JTextField)((JPanel)c).getComponent(0)).getText().trim() + " " + (String)((JComboBox)((JPanel)c).getComponent(2)).getSelectedItem();
				int numOfBins = Integer.parseInt((String)((JComboBox)((JPanel)c).getComponent(2)).getSelectedItem())-1;
				for (int i = 0; i < numOfBins; i++){
					s += " ";
					s += ((JTextField)(((JPanel)c).getComponent(i+3))).getText().trim();
				}
				prop.setProperty("learn.bins"+ (k-1), s);
				if (((JCheckBox)((JPanel)c).getComponent(1)).isSelected()){
					if (inputCount == 0){
						inputCount++;
						ip = String.valueOf(k-1);//((JTextField)((JPanel)c).getComponent(0)).getText().trim();
					}
					else{
						ip = ip + " " + String.valueOf(k-1);
					}
				}
				k++;
			}
			if (inputCount != 0){
				prop.setProperty("learn.inputs", ip);
			}
			log.addText("Saving learn parameters to file:\n" + directory
					+ separator + lrnFile + "\n");
			FileOutputStream out = new FileOutputStream(new File(directory
					+ separator + lrnFile));
			prop.store(out, learnFile);
			out.close();
//			String[] tempBin = lrnFile.split("\\.");
//			String binFile = tempBin[0] + ".bins";
//			FileWriter write = new FileWriter(new File(directory + separator + binFile));
			// boolean flag = false;
			// for (int i = 0; i < variables.size(); i++) {
			// if (((JCheckBox) variables.get(i).get(1)).isSelected()) {
			// if (!flag) {
			// write.write(".dmvc ");
			// flag = true;
			// }
			// write.write(((JTextField)
			// variables.get(i).get(0)).getText().trim() + " ");
			// }
			// }
			// if (flag) {
			// write.write("\n");
			// }
			for (int i = 0; i < variables.size(); i++) {
				if (((JTextField) variables.get(i).get(0)).getText().trim().equals("")) {
//					write.write("?");
				} else {
//					write.write(((JTextField) variables.get(i).get(0)).getText().trim());
				}
				// write.write(", " + ((JComboBox)
				// variables.get(i).get(1)).getSelectedItem());
				for (int j = 3; j < variables.get(i).size(); j++) { // changed 2 to 3 SB
					if (((JTextField) variables.get(i).get(j)).getText().trim().equals("")) {
//						write.write(" ?");
					} else {
//						write.write(" " + ((JTextField) variables.get(i).get(j)).getText().trim());
					}
				}
//				write.write("\n");
			}
//			write.close();
			// log.addText("Creating levels file:\n" + directory + separator +
			// binFile + "\n");
			// String command = "autogenT.py -b" + binFile + " -t"
			// + numBins.getSelectedItem().toString() + " -i" +
			// iteration.getText();
			// if (range.isSelected()) {
			// command = command + " -cr";
			// }
			// File work = new File(directory);
			// Runtime.getRuntime().exec(command, null, work);
			change = false;
		} catch (Exception e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"Unable to save parameter file!", "Error Saving File",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void reload(String newname) {
		// try {
		// Properties prop = new Properties();
		// FileInputStream in = new FileInputStream(new File(directory +
		// separator + lrnFile));
		// prop.load(in);
		// in.close();
		// prop.setProperty("learn.file", newname);
		// prop.setProperty("learn.iter", this.iteration.getText().trim());
		// prop.setProperty("learn.bins", (String)
		// this.numBins.getSelectedItem());
		// if (range.isSelected()) {
		// prop.setProperty("learn.equal", "range");
		// }
		// else {
		// prop.setProperty("learn.equal", "points");
		// }
		// if (auto.isSelected()) {
		// prop.setProperty("learn.use", "auto");
		// }
		// else {
		// prop.setProperty("learn.use", "user");
		// }
		// log.addText("Saving learn parameters to file:\n" + directory +
		// separator + lrnFile
		// + "\n");
		// FileOutputStream out = new FileOutputStream(new File(directory +
		// separator + lrnFile));
		// prop.store(out, learnFile);
		// out.close();
		// }
		// catch (Exception e1) {
		// //e1.printStackTrace();
		// JOptionPane.showMessageDialog(biosim.frame(), "Unable to save
		// parameter file!",
		// "Error Saving File", JOptionPane.ERROR_MESSAGE);
		// }
		backgroundField.setText(newname);
	}

	public void learn() {
		try {
			if (auto.isSelected()) {
//				FileWriter write = new FileWriter(new File(directory + separator + binFile));
//				FileWriter writeNew = new FileWriter(new File(directory	+ separator + newBinFile));
				// write.write("time 0\n");
				// boolean flag = false;
				// for (int i = 0; i < variables.size(); i++) {
				// if (((JCheckBox) variables.get(i).get(1)).isSelected()) {
				// if (!flag) {
				// write.write(".dmvc ");
				// writeNew.write(".dmvc ");
				// flag = true;
				// }
				// write.write(((JTextField)
				// variables.get(i).get(0)).getText().trim() + " ");
				// writeNew.write(((JTextField)
				// variables.get(i).get(0)).getText().trim()
				// + " ");
				// }
				// }
				// if (flag) {
				// write.write("\n");
				// writeNew.write("\n");
				// }
				for (int i = 0; i < variables.size(); i++) {
					// if (!((JCheckBox) variables.get(i).get(1)).isSelected())
					// {
					if (((JTextField) variables.get(i).get(0)).getText().trim()
							.equals("")) {
//						write.write("?");
//						writeNew.write("?");
					} else {
//						write.write(((JTextField) variables.get(i).get(0)).getText().trim());
//						writeNew.write(((JTextField) variables.get(i).get(0)).getText().trim());
					}
					// write.write(" " + ((JComboBox)
					// variables.get(i).get(1)).getSelectedItem());
					for (int j = 3; j < variables.get(i).size(); j++) {
						if (((JTextField) variables.get(i).get(j)).getText()
								.trim().equals("")) {
//							write.write(" ?");
//							writeNew.write(" ?");
							divisionsL.get(i).set(j-3,null);
						} else {
//							write.write(" " + ((JTextField) variables.get(i).get(j)).getText().trim());
//							writeNew.write(" " + ((JTextField) variables.get(i).get(j)).getText().trim());
							divisionsL.get(i).set(j-3,Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
						}
					}
//					write.write("\n");
//					writeNew.write("\n");
					// }
				}
//				write.close();
//				writeNew.close();
				generate = true;
			} else {
//				FileWriter write = new FileWriter(new File(directory + separator + binFile));
				// boolean flag = false;
				// for (int i = 0; i < variables.size(); i++) {
				// if (((JCheckBox) variables.get(i).get(1)).isSelected()) {
				// if (!flag) {
				// write.write(".dmvc ");
				// flag = true;
				// }
				// write.write(((JTextField)
				// variables.get(i).get(0)).getText().trim() + " ");
				// }
				// }
				// if (flag) {
				// write.write("\n");
				// }
				for (int i = 0; i < variables.size(); i++) {
					if (((JTextField) variables.get(i).get(0)).getText().trim()
							.equals("")) {
//						write.write("?");
					} else {
//						write.write(((JTextField) variables.get(i).get(0)).getText().trim());
					}
					for (int j = 3; j < variables.get(i).size(); j++) {  // changed 2 to 3 SB
						if (((JTextField) variables.get(i).get(j)).getText().trim().equals("")) {
//							write.write(" ?");
							divisionsL.get(i).set(j-3,null);
						} else {
//							write.write(" " + ((JTextField) variables.get(i).get(j)).getText().trim());
							divisionsL.get(i).set(j-3,Double.parseDouble(((JTextField) variables.get(i).get(j)).getText().trim()));
						}
					}
//					write.write("\n");
				}
//				write.close();
				generate = false;
			}
			execute = true;
			new Thread(this).start();
		} catch (Exception e) {
		}
	}

	public void run() {
		new File(directory + separator + lhpnFile).delete();
		fail = false;
		try {
			File work = new File(directory);
			final JFrame running = new JFrame("Progress");
			final JButton cancel = new JButton("Cancel");
			WindowListener w = new WindowListener() {
				public void windowClosing(WindowEvent arg0) {
					cancel.doClick();
					running.dispose();
				}

				public void windowOpened(WindowEvent arg0) {
				}

				public void windowClosed(WindowEvent arg0) {
				}

				public void windowIconified(WindowEvent arg0) {
				}

				public void windowDeiconified(WindowEvent arg0) {
				}

				public void windowActivated(WindowEvent arg0) {
				}

				public void windowDeactivated(WindowEvent arg0) {
				}
			};
			running.addWindowListener(w);
			JPanel text = new JPanel();
			JPanel progBar = new JPanel();
			JPanel button = new JPanel();
			JPanel all = new JPanel(new BorderLayout());
			JLabel label = new JLabel("Running...");
			JProgressBar progress = new JProgressBar();
			progress.setIndeterminate(true);
			// progress.setStringPainted(true);
			// progress.setString("");
			progress.setValue(0);
			text.add(label);
			progBar.add(progress);
			button.add(cancel);
			all.add(text, "North");
			all.add(progBar, "Center");
			all.add(button, "South");
			running.setContentPane(all);
			running.pack();
			Dimension screenSize;
			try {
				Toolkit tk = Toolkit.getDefaultToolkit();
				screenSize = tk.getScreenSize();
			} catch (AWTError awe) {
				screenSize = new Dimension(640, 480);
			}
			Dimension frameSize = running.getSize();

			if (frameSize.height > screenSize.height) {
				frameSize.height = screenSize.height;
			}
			if (frameSize.width > screenSize.width) {
				frameSize.width = screenSize.width;
			}
			int x = screenSize.width / 2 - frameSize.width / 2;
			int y = screenSize.height / 2 - frameSize.height / 2;
			running.setLocation(x, y);
			running.setVisible(true);
			running.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			FileWriter out = new FileWriter(new File(directory + separator
					+ "run.log"));
			if (generate) {
				String makeBin = "autogenT.py -b" + newBinFile + " -i"
						+ iteration.getText();
				if (range.isSelected()) {
					makeBin = makeBin + " -cr";
				}
				log.addText(makeBin);
				// log.addText("Creating levels file:\n" + directory + separator
				// + binFile + "\n");
				final Process bins = Runtime.getRuntime().exec(makeBin, null,
						work);
				cancel.setActionCommand("Cancel");
				cancel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						bins.destroy();
						running.setCursor(null);
						running.dispose();
					}
				});
				biosim.getExitButton().setActionCommand("Exit program");
				biosim.getExitButton().addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						bins.destroy();
						running.setCursor(null);
						running.dispose();
					}
				});
				try {
					String output = "";
					InputStream reb = bins.getInputStream();
					InputStreamReader isr = new InputStreamReader(reb);
					BufferedReader br = new BufferedReader(isr);
					int count = 0;
					while ((output = br.readLine()) != null) {
						if (output.matches("\\d+/\\d+")) {
							// log.addText(output);
							count += 500;
							progress.setValue(count);
						} else if (output.contains("ERROR")) {
							fail = true;
						}
						out.write(output);
						out.write("\n");
					}
					br.close();
					isr.close();
					reb.close();
					if (!execute || fail) {
						out.close();
					}
					viewLog.setEnabled(true);
				} catch (Exception e) {
				}
				int exitValue = bins.waitFor();
				if (exitValue == 143) {
					JOptionPane.showMessageDialog(biosim.frame(),
							"Learning was" + " canceled by the user.",
							"Canceled Learning", JOptionPane.ERROR_MESSAGE);
				}
				FileOutputStream outBins = new FileOutputStream(new File(directory + separator + binFile));
				FileInputStream inBins = new FileInputStream(new File(directory	+ separator + newBinFile));
				int read = inBins.read();
				while (read != -1) {
					outBins.write(read);
					read = inBins.read();
				}
				inBins.close();
				outBins.close();
				if (!execute) {
					levels();
				}
			}
			if (execute && !fail) {
				File lhpn = new File(lhpnFile);
				lhpn.delete();
				// String command = "data2lhpn.py -b" + binFile + " -l" +
				// lhpnFile;
				String command = "dataToLHPN()";
				log.addText("Running:\n" + command  + "\n");
				dataToLHPN();
				// log.addText("Executing:\n" + command + " " + directory +
				// "\n");
				// File work = new File(directory);
				//				
				// final Process run = Runtime.getRuntime().exec(command,
				// null,work);
				// cancel.setActionCommand("Cancel");
				// cancel.addActionListener(new ActionListener() {
				// public void actionPerformed(ActionEvent e) {
				// run.destroy();
				// running.setCursor(null);
				// running.dispose();
				// }
				// }
				// );
				// biosim.getExitButton().setActionCommand("Exit program");
				// biosim.getExitButton().addActionListener(new ActionListener()
				// {
				// public void actionPerformed(ActionEvent e) {
				// run.destroy();
				// running.setCursor(null);
				// running.dispose();
				// }
				// }
				// );
				// try {
				// String output = "";
				// InputStream reb = run.getInputStream();
				// InputStreamReader isr = new InputStreamReader(reb);
				// BufferedReader br = new BufferedReader(isr);
				// while ((output = br.readLine()) != null) {
				// if (output.contains("ERROR")) {
				// fail = true;
				// }
				// out.write(output);
				// out.write("\n");
				// }
				// br.close();
				// isr.close();
				// reb.close();
				// out.close();
				// viewLog.setEnabled(true);
				// }
				// catch (Exception e) {
				//					  
				// }
				// int exitValue = run.waitFor();
				// if (exitValue == 143) {
				// JOptionPane.showMessageDialog(biosim.frame(), "Learning was"
				// + "canceled by the user.", "Canceled Learning",
				// JOptionPane.ERROR_MESSAGE);
				// }
				viewLog.setEnabled(true);
				if (new File(directory + separator + lhpnFile).exists()) {
					viewLhpn();
				} else {
					fail = true;
				}

			}
			running.setCursor(null);
			running.dispose();
			if (fail) {
				viewLog();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public boolean hasChanged() {
		return change;
	}

	public boolean isComboSelected() {
		if (debug.isFocusOwner() || numBins.isFocusOwner()) {
			return true;
		}
		if (variables == null) {
			return false;
		}
		for (int i = 0; i < variables.size(); i++) {
			if (((JComboBox) variables.get(i).get(2)).isFocusOwner()) {  // changed 1 to 2 SB
				return true;
			}
		}
		return false;
	}

	public boolean getViewLhpnEnabled() {
		return viewLhpn.isEnabled();
	}

	public boolean getSaveLhpnEnabled() {
		return saveLhpn.isEnabled();
	}

	public boolean getViewLogEnabled() {
		return viewLog.isEnabled();
	}

	public void updateSpecies(String newLearnFile) {
		learnFile = newLearnFile;
		variablesList = new ArrayList<String>();
		/*
		 * if ((learnFile.contains(".vhd")) || (learnFile.contains(".lpn"))) {
		 * LHPNFile lhpn = new LHPNFile(); lhpn.load(directory + separator +
		 * learnFile); Set<String> ids = lhpn.getVariables().keySet(); /*try {
		 * FileWriter write = new FileWriter( new File(directory + separator +
		 * "background.g")); write.write("digraph G {\n"); for (String s : ids) {
		 * variablesList.add(s); write.write("s" + s + "
		 * [shape=ellipse,color=black,label=\"" + (s) + "\"" + "];\n"); }
		 * write.write("}\n"); write.close(); } catch (Exception e) {
		 * JOptionPane.showMessageDialog(biosim.frame(), "Unable to create
		 * background file!", "Error Writing Background",
		 * JOptionPane.ERROR_MESSAGE); } } else {
		 */
		LHPNFile lhpn = new LHPNFile();
		lhpn.load(learnFile);
		HashMap<String, Properties> speciesMap = lhpn.getVariables();
		for (String s : speciesMap.keySet()) {
			variablesList.add(s);
		}
		/*
		 * try { FileWriter write = new FileWriter( new File(directory +
		 * separator + "background.gcm")); BufferedReader input = new
		 * BufferedReader(new FileReader(new File(learnFile))); String line =
		 * null; while ((line = input.readLine()) != null) { write.write(line +
		 * "\n"); } write.close(); input.close(); } catch (Exception e) {
		 * JOptionPane.showMessageDialog(biosim.frame(), "Unable to create
		 * background file!", "Error Writing Background",
		 * JOptionPane.ERROR_MESSAGE); }
		 */
		sortVariables();
		if (user.isSelected()) {
			auto.doClick();
			user.doClick();
		} else {
			user.doClick();
			auto.doClick();
		}
		levels();
	}

	private void sortVariables() {
		int i, j;
		String index;
		for (i = 1; i < variablesList.size(); i++) {
			index = variablesList.get(i);
			j = i;
			while ((j > 0)
					&& variablesList.get(j - 1).compareToIgnoreCase(index) > 0) {
				variablesList.set(j, variablesList.get(j - 1));
				j = j - 1;
			}
			variablesList.set(j, index);
		}
	}

	public void setDirectory(String directory) {
		this.directory = directory;
		String[] getFilename = directory.split(separator);
		lrnFile = getFilename[getFilename.length - 1] + ".lrn";
	}

	/**
	 * This method generates an LHPN model from the simulation traces provided
	 * in the learn view. The generated LHPN is stored in an object of type
	 * lhpn2sbml.parser.LHPNfile . It is then saved in *.lpn file using the
	 * save() method of the above class.
	 * 
	 * Rev. 1 - Scott Little (data2lhpn.py) 
	 * Rev. 2 - Satish Batchu ( dataToLHPN() )
	 */

	public void dataToLHPN() {
		try {
			logFile = new File(directory + separator + "tmp.log");
			logFile.createNewFile();
			out = new BufferedWriter(new FileWriter(logFile));
			TSDParser tsd = new TSDParser(directory + separator + "run-1.tsd",
					biosim, false);
			varNames = tsd.getSpecies();
			// Check whether all the tsd files are following the same variables
			// & order
			// vars = varNames.toArray(new String[varNames.size()]);
			int i = 1;
			// String failProp = ""; // ?????????
		//	divisionsL = parseBinFile();
		//	for (int j = 0; j < tempPorts.size(); j++){
		//		reqdVarsL.get(j).setInput(tempPorts.get(j));
		//	}
		//    reqdVarsL.get(1).setInput(true); // temporary. should probably be provided in binsfile??
		//	reqdVarsL.get(4).setInput(true); // for pd
		//	reqdVarsL.get(5).setInput(true); // for pd
			String failProp = "";
			String enFailAnd = "";
			String enFail = "";
			// Graph g = new Graph(reqdVarsL.toArray(new
			// Variable[reqdVarsL.size()]),failProp);
			// Add logic to deal with failprop and related places/transitions
			g = new LHPNFile(); // The generated lhpn is stored in this object
			placeInfo = new HashMap<String, Properties>();
			transitionInfo = new HashMap<String, Properties>();
			if (new File(directory + separator + "learn" + ".prop").exists()){
				BufferedReader prop = new BufferedReader(new FileReader(directory + separator + "learn" + ".prop"));
				failProp = prop.readLine();
				failProp = failProp.replace("\n", "");
				Properties p0 = new Properties();
				placeInfo.put("failProp", p0);
				p0.setProperty("placeNum", numPlaces.toString());
				p0.setProperty("type", "PROP");
				p0.setProperty("initiallyMarked", "true");
				g.addPlace("p" + numPlaces, true);
				numPlaces++;
				Properties p1 = new Properties();
				transitionInfo.put("failProp", p1);
				p1.setProperty("transitionNum", numTransitions
						.toString());
				g.addTransition("t" + numTransitions); // prevTranKey+key);
				g.addControlFlow("p"
						+ placeInfo.get("failProp").getProperty(
								"placeNum"), "t"
						+ transitionInfo.get("failProp")
								.getProperty("transitionNum")); 
				numTransitions++;
				enFailAnd = "&~fail";
				enFail = "~fail";
			}
			epsilon = Double.parseDouble(epsilonG.getText().trim());
			rateSampling = Integer.parseInt(rateSamplingG.getText().trim());
			pathLength = Integer.parseInt(pathLengthG.getText().trim());
			percent = Double.parseDouble(percentG.getText().trim());
			runLength = Integer.parseInt(runLengthG.getText().trim());
			runTime = Double.parseDouble(runTimeG.getText().trim());
			while (new File(directory + separator + "run-" + i + ".tsd").exists()) {
				genBinsRates("run-" + i + ".tsd", divisionsL);
				detectDMV();
				updateGraph(bins, rates);
				i++;
			}
			for (String st1 : g.getTransitionList()) {
				out.write("\nTransition is " + st1);
				String binEncoding = getPlaceInfoIndex(g.getPreset(st1)[0]);
				out.write(" Incoming place " + g.getPreset(st1)[0]
						+ " Bin encoding is " + binEncoding);
				if (g.getPostset(st1).length != 0){
					binEncoding = getPlaceInfoIndex(g.getPostset(st1)[0]);
					out.write(" Outgoing place " + g.getPostset(st1)[0]
						+ " Bin encoding is " + binEncoding);
				}
			}
			out.write("\nTotal no of transitions : " + numTransitions);
			out.write("\nTotal no of places : " + numPlaces);
			out.write("\nPlaces are : ");
			for (String st3 : g.getPlaceList()) {
				out.write(st3 + " ");
			}
			out.write("\n");
			for (String t : g.getTransitionList()) {
				for (String p : g.getPreset(t)) {
					out.write(p + " " + t + "\n");
				}
				for (String p : g.getPostset(t)) {
					out.write(t + " " + p + "\n");
				}
			}

			Properties initCond = new Properties();
			for (Variable v : reqdVarsL) {
				if (v.isDmvc()) {
					g.addInteger(v.getName(), v.getInitValue());
				} else {
					initCond.put("value", v.getInitValue());
					initCond.put("rate", v.getInitValue());
					g.addVar(v.getName(), initCond);
				}
			}
			g.addOutput("fail", "false");
			normalize();
			for (String t : g.getTransitionList()) {
				// Transition t = g.get_valT(sortedTrans.get(j));
				if ((g.getPreset(t) != null)
						&& (g.getPostset(t) != null)
						&& (placeInfo.get(getPlaceInfoIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))
						&& (placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))) {
					ArrayList<Integer> diffL = diff(getPlaceInfoIndex(g.getPreset(t)[0]), getPlaceInfoIndex(g.getPostset(t)[0]));
					String condStr = "";
					String[] binIncoming = getPlaceInfoIndex(g.getPreset(t)[0]).split("");
					String[] binOutgoing = getPlaceInfoIndex(g.getPostset(t)[0]).split("");
					for (int k : diffL) {
						if (Integer.parseInt(binIncoming[k + 1]) < Integer.parseInt(binOutgoing[k + 1])) {
							double val = divisionsL.get(k).get(Integer.parseInt(binIncoming[k + 1])).doubleValue();
							condStr += "(" + reqdVarsL.get(k).getName() + ">=" + (int) Math.floor(val) + ")";
						} else {
							double val = divisionsL.get(k).get(Integer.parseInt(binOutgoing[k + 1])).doubleValue();
							condStr += "~(" + reqdVarsL.get(k).getName() + ">="	+ (int) Math.ceil(val) + ")";
						}
						if (diffL.get(diffL.size() - 1) != k) {
							condStr += "&";
						}
						// Enablings Till above.. Below one is dmvc
						// delay,assignment. Whenever a transition's incoming
						// and outgoing places differ in dmvc vars, then the
						// transition before this transition gets the assignment
						// of this dmvc value with delay equal to that of the in
						// b/w place's duration range. This has to be changed
						// after taking the causal relation input
						if ((reqdVarsL.get(k).isDmvc())
								&& (!reqdVarsL.get(k).isInput())) { // require
							// few more changes here.should
							// check for those variables that are constant over
							// these regions and make them as causal????? thesis
							String pPrev = g.getPreset(t)[0];
							for (String tPrev : g.getPreset(pPrev)) {
								out.write("\n<" + tPrev + "= " + "{" + placeInfo.get(getPlaceInfoIndex(pPrev)).getProperty("dMin") + "," + placeInfo.get(getPlaceInfoIndex(pPrev)).getProperty("dMax") + "}" + "[" + reqdVarsL.get(k).getName() + ":=[" + placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty(reqdVarsL.get(k).getName() + "_vMin") + "," + placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty(reqdVarsL.get(k).getName() + "_vMax") + "]>");
								g.changeDelay(tPrev, "[" + (int) Double.parseDouble(placeInfo.get(getPlaceInfoIndex(pPrev)).getProperty("dMin")) + "," + (int) Double.parseDouble(placeInfo.get(getPlaceInfoIndex(pPrev)).getProperty("dMax")) + "]");
								g.addIntAssign(tPrev,reqdVarsL.get(k).getName(),"[" + (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty(reqdVarsL.get(k).getName() + "_vMin"))) + ","+ (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty(reqdVarsL.get(k).getName() + "_vMax"))) + "]");
							}
						}
					}
					condStr += enFailAnd;
					g.addEnabling(t, condStr);
				} else if ((g.getPreset(t) != null)
						&& (g.getPostset(t) != null)
						&& (placeInfo.get(getPlaceInfoIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("DMVC"))
						&& (placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("type").equalsIgnoreCase("DMVC"))) {
					// Dealing with graphs obtained from DMVC INPUT variables
					// NO ENABLINGS for these transitions
					String nextPlace = g.getPostset(t)[0];
					g.changeDelay(t, "[" + (int) Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty("dMin")) + "," + (int) Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty("dMax")) + "]");
					g.addIntAssign(t, placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("DMVCVariable"), "[" + (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("DMVCValue")))	+ "," + (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("DMVCValue"))) + "]");
					g.addEnabling(t, enFail);
				} else if ((g.getPreset(t) != null) && (placeInfo.get(getPlaceInfoIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("PROP"))){
					g.addEnabling(t, failProp);
					g.addBoolAssign(t, "fail", "true"); // fail would be the variable name
					//g.addProperty(failProp);
				}
				// if ((t.getIncomingP() != null) &&
				// (t.getIncomingP().isPropP())){
				// t.setEnabling(t.getIncomingP().getProperty());
				// }
				// if ((t.getIncomingP() != null) && (t.getOutgoingP() != null)
				// && (t.getIncomingP().isDmvcP()) &&
				// (t.getOutgoingP().isDmvcP())){
				// out.write("\n<t"+t.getTransitionNum() + "= " + "{" +
				// t.getMinDelay() + "," + t.getMaxDelay() + "}" + "["+
				// reqdVarsL.get(t.getOutgoingP().getDmvcVar()).getName()+ ":= "
				// + t.getOutgoingP().getDmvcVal() + "]>");
				// }
			}
			out.write("\n#@.rate_assignments {");
			if (placeRates) {
				for (String st1 : g.getPlaceList()) {
					String p = getPlaceInfoIndex(st1);
					if (placeInfo.get(p).getProperty("type").equalsIgnoreCase(
							"RATE")) {
						for (String t : g.getPreset(st1)) {
							for (int k = 0; k < reqdVarsL.size(); k++) {
								if (!reqdVarsL.get(k).isDmvc()) {
									out.write("<" + t	+ "=["	+ reqdVarsL.get(k).getName() + ":=["	+ getMinRate(p, reqdVarsL.get(k).getName())	+ "," + getMaxRate(p, reqdVarsL.get(k).getName()) + "]]>");
									g.addRateAssign(t, reqdVarsL.get(k).getName(), "["	+ getMinRate(p, reqdVarsL.get(k).getName())	+ "," + getMaxRate(p, reqdVarsL.get(k).getName()) + "]");
								}
							}
						}
						out.write("\n");
					}
				}
			}
			/*
			 * ADD TRANSITION BASED RATE GENERATION LOGIC HERE else{
			 * //Transition based rate generation for (String st: sortedTrans){
			 * Transition t = g.get_valT(st); if ((t.getOutgoingP() != null) &&
			 * (t.getIncomingP() != null)){ ArrayList<Integer> diffL =
			 * t.getOutgoingP().diff(t.getIncomingP().getBinEncoding()); for
			 * (int k:diffL){ if (t.getIncomingP().isRateP()){ out.write("<t" +
			 * t.getTransitionNum() + "=[" + reqdVarsL.get(k).getName() + ":=[" +
			 * t.minRate(k) + "," + t.maxRate(k) + "]]>\n"); } } } } }
			 */
			/*
			 * if (g.isFailProp()){ out.write("#@.boolean_assignments {\n"); for
			 * (String st:sortedPlaces){ Place p = g.get_valP(st); if
			 * (p.isPropP()){ for (Transition t:p.getOutgoing()){ out.write("<t" +
			 * t.getTransitionNum() + "=[fail:=TRUE]>"); } } } out.write("\n"); }
			 */out.close();
			g.save(directory + separator + lhpnFile);

		} catch (IOException e) {
			System.out.println("LPN file couldn't be created/written ");
		}

	}

	public ArrayList<ArrayList<Double>> parseBinFile() {
		reqdVarsL = new ArrayList<Variable>();
		ArrayList<String> linesBinFileL = null;
		int h = 0;
		//ArrayList<ArrayList<Double>> divisionsL = new ArrayList<ArrayList<Double>>();
		
		try {
			Scanner f1 = new Scanner(new File(directory + separator + binFile));
			// log.addText(directory + separator + binFile);
			linesBinFileL = new ArrayList<String>();
			linesBinFileL.add(f1.nextLine());
			while (f1.hasNextLine()) {
				linesBinFileL.add(f1.nextLine());
			}
			out.write("Required variables and their levels are :");
			for (String st : linesBinFileL) {
				divisionsL.add(new ArrayList<Double>());
				String[] wordsBinFileL = st.split("\\s");
				for (int i = 0; i < wordsBinFileL.length; i++) {
					if (i == 0) {
						reqdVarsL.add(new Variable(wordsBinFileL[i]));
						out.write("\n"	+ reqdVarsL.get(reqdVarsL.size() - 1).getName());
					} else {
						divisionsL.get(h).add(Double.parseDouble(wordsBinFileL[i]));

					}
				}
				out.write(" " + divisionsL.get(h));
				h++;
				// max = Math.max(max, wordsBinFileL.length + 1);
			}
			f1.close();

		} catch (Exception e1) {
		}
		return divisionsL;
	}

	public void genBinsRates(String datFile,ArrayList<ArrayList<Double>> divisionsL) { // genBins
		TSDParser tsd = new TSDParser(directory + separator + datFile, biosim,false);
		// genBins
		data = tsd.getData();
		reqdVarIndices = new ArrayList<Integer>();
		bins = new int[reqdVarsL.size()][data.get(0).size()];
		for (int i = 0; i < reqdVarsL.size(); i++) {
			// System.out.println("Divisions " + divisionsL.get(i));
			for (int j = 1; j < varNames.size(); j++) {
				if (reqdVarsL.get(i).getName().equalsIgnoreCase(varNames.get(j))) {
					// System.out.println(reqdVarsL.get(i) + " matched "+
					// varNames.get(j) + " i = " + i + " j = " + j);
					reqdVarIndices.add(j);
					for (int k = 0; k < data.get(j).size(); k++) {
						// System.out.print(data.get(j).get(k) + " ");
						for (int l = 0; l < divisionsL.get(i).size(); l++) {
							if (data.get(j).get(k) <= divisionsL.get(i).get(l)) {
								bins[i][k] = l;
								break;
							} else {
								bins[i][k] = l + 1; // indices of bins not same
								// as that of the variable.
								// i here. not j; if j
								// wanted, then size of bins
								// array should be varNames
								// not reqdVars
							}
						}
					}
					// System.out.print(" ");
				}
			}
		}
		/*
		 * System.out.println("array bins is :"); for (int i = 0; i <
		 * reqdVarsL.size(); i++) { System.out.print(reqdVarsL.get(i).getName() + "
		 * "); for (int k = 0; k < data.get(0).size(); k++) {
		 * System.out.print(bins[i][k] + " "); } System.out.print("\n"); }
		 */
		// genRates
		rates = new Double[reqdVarsL.size()][data.get(0).size()];
		duration = new Double[data.get(0).size()];
		int mark, k; // indices of rates not same as that of the variable. if
		// wanted, then size of rates array should be varNames
		// not reqdVars
		if (placeRates) {
			if (rateSampling == -1) { // replacing inf with -1 since int
				mark = 0;
				for (int i = 0; i < data.get(0).size(); i++) {
					if (i < mark) {
						continue;
					}
					while ((mark < data.get(0).size()) && (compareBins(i, mark))) {
						mark++;
					}
					if ((data.get(0).get(mark - 1) != data.get(0).get(i)) && ((mark - i) >=  pathLength)) { 
						for (int j = 0; j < reqdVarsL.size(); j++) {
							k = reqdVarIndices.get(j);
							rates[j][i] = ((data.get(k).get(mark - 1) - data.get(k).get(i)) / (data.get(0).get(mark - 1) - data.get(0).get(i)));
						}
						duration[i] = data.get(0).get(mark - 1)	- data.get(0).get(i);
					}
				}
			} else {
				boolean calcRate;
				boolean prevFail = true;
				int binStartPoint = 0, binEndPoint = 0;
				for (int i = 0; i < (data.get(0).size() - rateSampling); i++) {
					calcRate = true;
					for (int l = 0; l < rateSampling; l++) {
						if (!compareBins(i, i + l)) {
							if (!prevFail){
								binEndPoint = i -2 + rateSampling;
								duration[binStartPoint] = data.get(0).get(binEndPoint)	- data.get(0).get(binStartPoint);
							}
							calcRate = false;
							prevFail = true;
							break;
						}
					}
					if (calcRate && (data.get(0).get(i + rateSampling) != data.get(0).get(i))) {
						for (int j = 0; j < reqdVarsL.size(); j++) {
							k = reqdVarIndices.get(j);
							rates[j][i] = ((data.get(k).get(i + rateSampling) - data.get(k).get(i)) / (data.get(0).get(i + rateSampling) - data.get(0).get(i)));
						}
						if (prevFail){
							binStartPoint = i;
						}
						prevFail = false;
					}
				}
				if (!prevFail){ // for the last genuine rate-calculating region of the trace; this may not be required if the trace is incomplete.trace data may not necessarily end at a region endpt
					duration[binStartPoint] = data.get(0).get(data.get(0).size()-1)	- data.get(0).get(binStartPoint);
				}
			}
		} 
		/*
		 * ADD LATER: duration[i] SHOULD BE ADDED TO THE NEXT 2 IF/ELSE
		 * BRANCHES(Transition based rate calc) ALSO
		 */
		else { // Transition based rate calculation
			if (rateSampling == -1) { // replacing inf with -1 since int
				for (int j = 0; j < reqdVarsL.size(); j++) {
					mark = 0;
					k = reqdVarIndices.get(j);
					for (int i = 0; i < data.get(0).size(); i++) {
						if (i < mark) {
							continue;
						}
						while ((mark < data.get(0).size())
								&& (bins[k][i] == bins[k][mark])) {
							mark++;
						}
						if ((data.get(0).get(mark - 1) != data.get(0).get(i))) {
							rates[j][i] = ((data.get(k).get(mark - 1) - data.get(k).get(i)) / (data.get(0).get(mark - 1) - data.get(0).get(i)));
						}
					}
				}
			} else {
				boolean calcRate;
				for (int i = 0; i < (data.get(0).size() - rateSampling); i++) {
					for (int j = 0; j < reqdVarsL.size(); j++) {
						calcRate = true;
						k = reqdVarIndices.get(j);
						for (int l = 0; l < rateSampling; l++) {
							if (bins[k][i] != bins[k][i + l]) {
								calcRate = false;
								break;
							}
						}
						if (calcRate && (data.get(0).get(i + rateSampling) != data.get(0).get(i))) {
							rates[j][i] = ((data.get(k).get(i + rateSampling) - data.get(k).get(i)) / (data.get(0).get(i + rateSampling) - data.get(0).get(i)));
						}
					}
				}
			}
		}
		try {
			for (int i = 0; i < (data.get(0).size()); i++) {
				for (int j = 0; j < reqdVarsL.size(); j++) {
					k = reqdVarIndices.get(j);
					out.write(data.get(k).get(i) + " ");// + bins[j][i] + " " +
					// rates[j][i] + " ");
				}
				for (int j = 0; j < reqdVarsL.size(); j++) {
					out.write(bins[j][i] + " ");
				}
				for (int j = 0; j < reqdVarsL.size(); j++) {
					out.write(rates[j][i] + " ");
				}
				out.write(duration[i] + " ");
				out.write("\n");
			}
		} catch (IOException e) {
			System.out
					.println("Log file couldn't be opened for writing rates and bins ");
		}
	}

	public boolean compareBins(int j, int mark) {
		for (int i = 0; i < reqdVarsL.size(); i++) {
			if (bins[i][j] != bins[i][mark]) {
				return false;
			} else {
				continue;
			}
		}
		return true;
	}

	public void updateRateInfo(int[][] bins, Double[][] rates) {
		String prevPlaceKey = ""; // "" or " " ; rechk
		String key = "";
		// boolean addNewPlace;
		// ArrayList<String> ratePlaces = new ArrayList<String>(); // ratePlaces
		// can include non-input dmv places.
		// boolean newRate = false;
		Properties p0, p1 = null;
		for (int i = 0; i < (data.get(0).size() - 1); i++) {
			if (rates[0][i] != null) { // check if indices are ok. 0???? or
				// 1???
				prevPlaceKey = key;
				key = "";
				for (int j = 0; j < reqdVarsL.size(); j++) {
					key += bins[j][i];
				}
				if (placeInfo.containsKey(key)) {
					p0 = placeInfo.get(key);
				} else {
					p0 = new Properties();
					placeInfo.put(key, p0);
					p0.setProperty("placeNum", numPlaces.toString());
					p0.setProperty("type", "RATE");
					p0.setProperty("initiallyMarked", "false");
					g.addPlace("p" + numPlaces, false);
					numPlaces++;
				}
				if (duration[i] != null){
					addDuration(p0, duration[i]);
				}
				for (int j = 0; j < reqdVarsL.size(); j++) {
					// rechk if (reqdVarsL.get(j).isDmvc() &&
					// reqdVarsL.get(j).isInput()) {
					// continue;
					// }
					if (reqdVarsL.get(j).isDmvc()) { // &&
						// !reqdVarsL.get(j).isInput()){
						for (int k = 0; k < reqdVarsL.get(j).getRuns()
								.getAvgVals().length; k++) {
							if ((reqdVarsL.get(j).getRuns().getStartPoint(k) <= i)
									&& (reqdVarsL.get(j).getRuns().getEndPoint(
											k) >= i)) {
								addValue(
										p0,
										reqdVarsL.get(j).getName(),
										reqdVarsL.get(j).getRuns().getAvgVals()[k]);// data.get(reqdVarIndices.get(j)).get(i));
								break;
							}
							if (reqdVarsL.get(j).getRuns().getStartPoint(k) >= i) {
								addValue(
										p0,
										reqdVarsL.get(j).getName(),
										reqdVarsL.get(j).getRuns().getAvgVals()[k]);// data.get(reqdVarIndices.get(j)).get(i));
								break;
							}
							// WRONG addValue(p0, reqdVarsL.get(j).getName(),
							// data.get(reqdVarIndices.get(j)).get(i));
						}
						continue;
					}
					addRate(p0, reqdVarsL.get(j).getName(), rates[j][i]);
					// newR, oldR, dmvc etc. left
				}
				if (!prevPlaceKey.equalsIgnoreCase(key)) {
					if (transitionInfo.containsKey(prevPlaceKey + key)) { // instead
						// of
						// tuple
						p1 = transitionInfo.get(prevPlaceKey + key);
					} else if (prevPlaceKey != "") {
						// transition = new
						// Transition(reqdVarsL.size(),place,prevPlace);
						p1 = new Properties();
						transitionInfo.put(prevPlaceKey + key, p1);
						p1.setProperty("transitionNum", numTransitions
								.toString());
						g.addTransition("t" + numTransitions); // prevTranKey+key);
						g.addControlFlow("p"
								+ placeInfo.get(prevPlaceKey).getProperty(
										"placeNum"), "t"
								+ transitionInfo.get(prevPlaceKey + key)
										.getProperty("transitionNum")); 
						g.addControlFlow("t"
								+ transitionInfo.get(prevPlaceKey + key)
										.getProperty("transitionNum"), "p"
								+ placeInfo.get(key).getProperty("placeNum"));
						numTransitions++;
						// transition.setCore(true);
					}
				}
				if (p1 != null) {
					for (int j = 0; j < reqdVarsL.size(); j++) {
						if (reqdVarsL.get(j).isDmvc()
								&& reqdVarsL.get(j).isInput()) {
							continue;
						}
						if (reqdVarsL.get(j).isDmvc()) {
							continue;
						}
						addRate(p1, reqdVarsL.get(j).getName(), rates[j][i]);
					}
				}
			}
		}
	}

	public void updateTimeInfo(int[][] bins) {
		String prevPlace = null;
		String currPlace = null;
		Properties p3 = null;
		ArrayList<String> dmvcPlaceL = new ArrayList<String>(); // only dmvc
		// inputs
		boolean exists;
		// int dmvcCnt = 0; making this global .. rechk
		String[] places;
		try {
			for (int i = 0; i < reqdVarsL.size(); i++) {
				if (reqdVarsL.get(i).isDmvc() && reqdVarsL.get(i).isInput()) {
					out.write(reqdVarsL.get(i).getName() + " is a dmvc input variable \n");
					// dmvcCnt = 0; in case of multiple tsd files, this may be a problem. may create a new distinct place with an existing key.??
					prevPlace = null;
					currPlace = null;
					p3 = null;
					Properties p2 = null;
					String k;
					DMVCrun runs = reqdVarsL.get(i).getRuns();
					Double[] avgVals = runs.getAvgVals();
					out.write("variable " + reqdVarsL.get(i).getName() + " Number of runs = " + avgVals.length + "Avg Values are : " + avgVals + "\n");
					for (int j = 0; j < avgVals.length; j++) { // this gives number of runs/startpoints/endpoints
						exists = false;
						places = g.getPlaceList();
						if (places.length > 1) {
							for (String st : places) {
								k = getPlaceInfoIndex(st);
								if (placeInfo.get(k).getProperty("type").equalsIgnoreCase("DMVC")) {
									if ((Math.abs(Double.parseDouble(placeInfo.get(k).getProperty("DMVCValue")) - avgVals[j]) < epsilon)
											&& (placeInfo.get(k).getProperty("DMVCVariable").equalsIgnoreCase(reqdVarsL.get(i).getName()))) {
										out.write("Place with key " + k + "already exists. so adding dmvcTime to it\n");
										addDmvcTime(placeInfo.get(k), reqdVarsL.get(i).getName(), calcDelay(runs.getStartPoint(j), runs.getEndPoint(j)));
										exists = true;
										prevPlace = currPlace;
										currPlace = getPlaceInfoIndex(st);// k;
										p2 = placeInfo.get(currPlace);
		//next few lines commented to remove multiple dmv input places of same variable from being marked.
										/*	if (j == 0) { // adding the place corresponding to the first dmv run to initial marking. 
											// SHOULD ensure that multiple dmv places belonging to same var are not marked initially
											placeInfo.get(k).setProperty("initiallyMarked", "true");
											g.changeInitialMarking("p"	+ placeInfo.get(k).getProperty("placeNum"),true);
										} */
										// break ; here?
									}
								}
							}
						}
						if (!exists) {
							prevPlace = currPlace;
							currPlace = "d_" + i + "_" + dmvcCnt;
							p2 = new Properties();
							p2.setProperty("placeNum", numPlaces.toString());
							p2.setProperty("type", "DMVC");
							p2.setProperty("DMVCVariable", reqdVarsL.get(i)
									.getName());
							p2.setProperty("DMVCValue", avgVals[j].toString());
							p2.setProperty("initiallyMarked", "false");
							addDmvcTime(p2, reqdVarsL.get(i).getName(),
									calcDelay(runs.getStartPoint(j), runs
											.getEndPoint(j)));
							placeInfo.put("d_" + i + "_" + dmvcCnt, p2);
							g.addPlace("p" + numPlaces, false);
							if (j == 0) { // adding the place corresponding to
								// the first dmv run to initial
								// marking
								p2.setProperty("initiallyMarked", "true");
								g.changeInitialMarking("p"
										+ p2.getProperty("placeNum"), true);
							}
							numPlaces++;
							out.write("Created new place with key " + "d_" + i
									+ "_" + dmvcCnt + "\n");
							dmvcCnt++;
							dmvcPlaceL.add(currPlace);
						}
						Double d = calcDelay(runs.getStartPoint(j), runs.getEndPoint(j));// data.get(0).get(runs.getEndPoint(j))
						// -
						// data.get(0).get(runs.getStartPoint(j));//
						// data.get(0).get(reqdVarsL.get(prevPlace.getDmvcVar()).getRuns().getEndPoint(j-1));
						addDuration(p2, d); // addDelay
						out.write("Delay in place p"+ p2.getProperty("placeNum")+ " after updating " + d + " is ["+ p2.getProperty("dMin") + ","+ p2.getProperty("dMax") + "]\n");
						if (prevPlace != null) {
							if (transitionInfo.containsKey(prevPlace
									+ currPlace)) {
								p3 = transitionInfo.get(prevPlace + currPlace);
							} else {
								p3 = new Properties();
								transitionInfo.put(prevPlace + currPlace, p3);
								p3.setProperty("transitionNum", numTransitions.toString());
								g.addTransition("t" + numTransitions); // prevTranKey+key);
								g.addControlFlow("p"+ placeInfo.get(prevPlace).getProperty("placeNum"), "t"+ transitionInfo.get(prevPlace + currPlace).getProperty("transitionNum")); 
								g.addControlFlow("t"+ transitionInfo.get(prevPlace+ currPlace).getProperty("transitionNum"),"p"+ placeInfo.get(currPlace).getProperty("placeNum"));
								numTransitions++;
							}
						}
					}
				} else if (reqdVarsL.get(i).isDmvc()) { // non-input dmvc

				}
			}
		} catch (IOException e) {
			System.out
					.println("Log file couldn't be opened for writing rates and bins ");
		}
	}

	public void updateGraph(int[][] bins, Double[][] rates) {
		updateRateInfo(bins, rates);
		updateTimeInfo(bins);
		int initMark = 0;
		int k;
		String key;
		for (int i = 0; i < reqdVarsL.size(); i++) {
			for (int j = 0; j < data.get(0).size(); j++) {
				if (rates[i][j] != null) {
					k = reqdVarIndices.get(i);
					// addInitValues(data.get(k).get(j), i); // k or i think ??
					// addInitRates(rates[i][j], i);// k or i think??
					reqdVarsL.get(i).addInitValues(data.get(k).get(j));
					reqdVarsL.get(i).addInitRates(rates[i][j]);
					initMark = j;
					break;
				}
			}
			key = "";
			for (int l = 0; l < reqdVarsL.size(); l++) {
				key = key + bins[l][initMark];
			}
			if (placeInfo.get(key).getProperty("initiallyMarked").equalsIgnoreCase("false")) {
				placeInfo.get(key).setProperty("initiallyMarked", "true");
				g.changeInitialMarking("p" + placeInfo.get(key).getProperty("placeNum"), true);
			}
		}

	}

	public void detectDMV() {
		int startPoint, endPoint, mark, numPoints;
		double absTime;
		for (int i = 0; i < reqdVarsL.size(); i++) {
			absTime = 0;
			mark = 0;
			DMVCrun runs = reqdVarsL.get(i).getRuns();
			runs.clearAll(); // flush all the runs from previous dat file.
			for (int j = 0; j < data.get(0).size(); j++) {
				if (j < mark) // not reqd??
					continue;
				if (Math.abs(data.get(reqdVarIndices.get(i)).get(j)
						- data.get(reqdVarIndices.get(i)).get(j + 1)) <= epsilon) {
					startPoint = j;
					runs.addValue(data.get(reqdVarIndices.get(i)).get(j)); // chk
					// carefully
					// reqdVarIndices.get(i)
					while (((j + 1) < data.get(0).size())
							&& (Math.abs(data.get(reqdVarIndices.get(i)).get(
									startPoint)
									- data.get(reqdVarIndices.get(i))
											.get(j + 1)) <= epsilon)) {
						runs.addValue(data.get(reqdVarIndices.get(i))
								.get(j + 1)); // chk carefully
						// reqdVarIndices.get(i)
						j++;
					}
					endPoint = j;
					if (!absoluteTime) {
						if (((endPoint - startPoint) + 1) >= runLength) {
							runs.addStartPoint(startPoint);
							runs.addEndPoint(endPoint);
						} else {
							runs.removeValue();
						}
					} else {
						if (calcDelay(startPoint, endPoint) >= runTime) {
							runs.addStartPoint(startPoint);
							runs.addEndPoint(endPoint);
							absTime += calcDelay(startPoint, endPoint);
						} else {
							runs.removeValue();
						}
					}
					mark = endPoint;
				}
			}
			try {
				if (!absoluteTime) {
					numPoints = runs.getNumPoints();
					if ((numPoints / (double) data.get(0).size()) < percent) {
						runs.clearAll();
						reqdVarsL.get(i).setDmvc(false);
						out.write(reqdVarsL.get(i).getName()
								+ " is not a dmvc \n");
					} else {
						reqdVarsL.get(i).setDmvc(true);
						out
								.write(reqdVarsL.get(i).getName()
										+ " is  a dmvc \n");
					}
				} else {
					if ((absTime / (data.get(0).get(data.get(0).size() - 1) - data
							.get(0).get(0))) < percent) {
						runs.clearAll();
						reqdVarsL.get(i).setDmvc(false);
						out.write(reqdVarsL.get(i).getName()
								+ " is not a dmvc \n");
					} else {
						reqdVarsL.get(i).setDmvc(true);
						out
								.write(reqdVarsL.get(i).getName()
										+ " is  a dmvc \n");
					}
				}
			} catch (IOException e) {
				System.out
						.println("Log file couldn't be opened for writing rates and bins ");
			}
		}
	}

	public double calcDelay(int i, int j) {
		return (data.get(0).get(j) - data.get(0).get(i));
		// should add some next run logic later..?
	}

	public void addValue(Properties p, String name, Double v) { // latest
		// change..
		// above one
		// working fine
		// if this
		// doesn't
		Double vMin;
		Double vMax;
		if ((p.getProperty(name + "_vMin") == null)
				&& (p.getProperty(name + "_vMax") == null)) {
			p.setProperty(name + "_vMin", v.toString());
			p.setProperty(name + "_vMax", v.toString());
			return;
		} else {
			vMin = Double.parseDouble(p.getProperty(name + "_vMin"));
			vMax = Double.parseDouble(p.getProperty(name + "_vMax"));
			if (v < vMin) {
				vMin = v;
			} else if (v > vMax) {
				vMax = v;
			}
		}
		p.setProperty(name + "_vMin", vMin.toString());
		p.setProperty(name + "_vMax", vMax.toString());
	}

	public void addRate(Properties p, String name, Double r) { // latest
		// change..
		// above one
		// working fine
		// if this
		// doesn't
		Double rMin;
		Double rMax;
		if ((p.getProperty(name + "_rMin") == null)
				&& (p.getProperty(name + "_rMax") == null)) {
			p.setProperty(name + "_rMin", r.toString());
			p.setProperty(name + "_rMax", r.toString());
			return;
		} else {
			rMin = Double.parseDouble(p.getProperty(name + "_rMin"));
			rMax = Double.parseDouble(p.getProperty(name + "_rMax"));
			if (r < rMin) {
				rMin = r;
			} else if (r > rMax) {
				rMax = r;
			}
		}
		p.setProperty(name + "_rMin", rMin.toString());
		p.setProperty(name + "_rMax", rMax.toString());
	}

	public void addDmvcTime(Properties p, String name, Double t) {
		if (p.getProperty("dmvcTime_" + name) == null) {
			p.setProperty("dmvcTime_" + name, t.toString());
		} else {
			// Double d = Double.parseDouble(p.getProperty("dmvcTime_" + name));
			// d = d + t;
			// p.setProperty("dmvcTime_" + name, d.toString());
			p.setProperty("dmvcTime_" + name, p.getProperty("dmvcTime_" + name)
					+ " " + t.toString());
		}
	}

	public void normalize() {
		Double minDelay = getMinDelay();
		Double maxDelay = getMaxDelay();
		Double scaleFactor = 1.0;
		try {
			out.write("minimum delay is " + minDelay
					+ " before scaling time.\n");
			if ((minDelay != null) && (minDelay != 0)) {
				for (int i = 0; i < 18; i++) {
					if (scaleFactor > (minDelayVal / minDelay)) {
						break;
					}
					scaleFactor *= 10.0;
				}
				if ((maxDelay != null) && ((int) (maxDelay * scaleFactor) > Integer.MAX_VALUE)) {
					System.out.println("Delay Scaling has caused an overflow");
				}
				out.write("minimum delay value is " + scaleFactor * minDelay
						+ "after scaling by " + scaleFactor + "\n");
				delayScaleFactor = scaleFactor;
				scaleDelay();
			}
			scaleFactor = 1.0;
			Double minRate = getMinRate(); // minRate should return minimum by
			// magnitude alone?? or even by sign..
			Double maxRate = getMaxRate();
			out.write("minimum rate is " + minRate + " before scaling the variable.\n");
			if ((minRate != null) && (minRate != 0)) {
				for (int i = 0; i < 14; i++) {
					if (scaleFactor > Math.abs(minRateVal / minRate)) {
						break;
					}
					scaleFactor *= 10.0;
				}
				for (int i = 0; i < 14; i++) {
					if ((maxRate != null) && (Math.abs((int) (maxRate * scaleFactor)) < Integer.MAX_VALUE)) {
						break;
					}
					scaleFactor /= 10.0;
				}
				if ((maxRate != null) && (Math.abs((int) (maxRate * scaleFactor)) > Integer.MAX_VALUE)) {
					System.out.println("Rate Scaling has caused an overflow");
				}
				out.write("minimum rate is " + minRate * scaleFactor + " after scaling by " + scaleFactor + "\n");
				varScaleFactor = scaleFactor;
				scaleVariable();
			}
			Double minDivision = getMinDiv();
			Double maxDivision = getMaxDiv();
			out.write("minimum division is " + minDivision + " before scaling for division.\n");
			scaleFactor = 1.0;
			if ((minDivision != null) && (minDivision != 0)) {
				for (int i = 0; i < 14; i++) {
					if (Math.abs(scaleFactor * minDivision) > minDivisionVal) {
						break;
					}
					scaleFactor *= 10;
				}
				if ((maxDivision != null)
						&& (Math.abs((int) (maxDivision * scaleFactor)) > Integer.MAX_VALUE)) {
					System.out.println("Division Scaling has caused an overflow");
				}
				out.write("minimum division is " + minDivision * scaleFactor
						+ " after scaling by " + scaleFactor + "\n");
				varScaleFactor *= scaleFactor;
				scaleVariable();
			}
		} catch (IOException e) {
			System.out.println("LPN file couldn't be created/written ");
		}
		return;
	}

	public void scaleVariable() {
		for (String place : placeInfo.keySet()) {
			if (place != "failProp"){
				Properties p = placeInfo.get(place);
				if (p.getProperty("type").equals("DMVC")) {
					p.setProperty("DMVCValue", Double.toString(Double.parseDouble(p.getProperty("DMVCValue"))* varScaleFactor));
				} else {
					for (Variable v : reqdVarsL) {
						if (!v.isDmvc()) {
							// p.setProperty(v.getName() +
							// "_rMin",Integer.toString((int)(Double.parseDouble(p.getProperty(v.getName()
							// + "_rMin"))/delayScaleFactor)));
							// p.setProperty(v.getName() +
							// "_rMax",Integer.toString((int)(Double.parseDouble(p.getProperty(v.getName()
							// + "_rMax"))/delayScaleFactor)));
							p.setProperty(v.getName() + "_rMin", Double
									.toString(Double.parseDouble(p.getProperty(v.getName()
											+ "_rMin"))* varScaleFactor));
							p.setProperty(v.getName() + "_rMax", Double
									.toString(Double.parseDouble(p.getProperty(v.getName()
											+ "_rMax"))* varScaleFactor));
						} else {
							// p.setProperty(v.getName() +
							// "_rMin",Integer.toString((int)(Double.parseDouble(p.getProperty(v.getName()
							// + "_rMin"))/delayScaleFactor)));
							// p.setProperty(v.getName() +
							// "_rMax",Integer.toString((int)(Double.parseDouble(p.getProperty(v.getName()
							// + "_rMax"))/delayScaleFactor)));
							if (!v.isInput()) {
								p.setProperty(v.getName() + "_vMin", Double
										.toString(Double.parseDouble(p.getProperty(v.getName()
												+ "_vMin"))* varScaleFactor));
								p.setProperty(v.getName() + "_vMax", Double
										.toString(Double.parseDouble(p.getProperty(v.getName()
												+ "_vMax")) * varScaleFactor));
							}

						}
					}
				}
			}
		}
		for (Variable v : reqdVarsL) {
			v.scaleInitByVar(varScaleFactor);
			for (int i = 0; i < divisionsL.size(); i++) { // or reqdVarsL.size()
				for (int j = 0; j < divisionsL.get(i).size(); j++) {
					divisionsL.get(i).set(j,
							divisionsL.get(i).get(j) * varScaleFactor);
				}
			}
		}
	}

	public void scaleDelay() {
		for (String place : placeInfo.keySet()) {
			if (place != "failProp"){
				Properties p = placeInfo.get(place);
				if (p.getProperty("type").equals("DMVC")) {
					String[] times = null;
					String name = p.getProperty("DMVCVar");
					String s = p.getProperty("dmvcTime_" + name);
					String newS = null;
					if (s != null) {
						times = s.split(" ");
						for (int i = 0; i < times.length; i++) {
							if (newS == null) {
								// newS = Integer.toString((int)(Double.parseDouble(times[i])*delayScaleFactor));
								newS = Double.toString(Double.parseDouble(times[i])
										* delayScaleFactor);
							} else {
								// newS = newS + Integer.toString((int)(Double.parseDouble(times[i])*delayScaleFactor));
								newS = newS + Double.toString(Double
										.parseDouble(times[i]) * delayScaleFactor);
							}
						}
						p.setProperty("dmvcTime_" + name, newS);
					}
					p.setProperty("dMin", Double.toString(Double.parseDouble(p
							.getProperty("dMin")) * delayScaleFactor));
					p.setProperty("dMax", Double.toString(Double.parseDouble(p
							.getProperty("dMax")) * delayScaleFactor));
				} else{
					// p.setProperty("dMin",Integer.toString((int)(Double.parseDouble(p.getProperty("dMin"))*delayScaleFactor)));
					// p.setProperty("dMax",Integer.toString((int)(Double.parseDouble(p.getProperty("dMax"))*delayScaleFactor)));
					p.setProperty("dMin", Double.toString(Double.parseDouble(p
							.getProperty("dMin")) * delayScaleFactor));
					p.setProperty("dMax", Double.toString(Double.parseDouble(p
							.getProperty("dMax")) * delayScaleFactor));
					for (Variable v : reqdVarsL) {
						if (!v.isDmvc()) {
							// p.setProperty(v.getName() +
							// "_rMin",Integer.toString((int)(Double.parseDouble(p.getProperty(v.getName()
							// + "_rMin"))/delayScaleFactor)));
							// p.setProperty(v.getName() +
							// "_rMax",Integer.toString((int)(Double.parseDouble(p.getProperty(v.getName()
							// + "_rMax"))/delayScaleFactor)));
							p.setProperty(v.getName() + "_rMin", Double
									.toString(Double.parseDouble(p.getProperty(v
											.getName() + "_rMin"))	/ delayScaleFactor));
							p.setProperty(v.getName() + "_rMax", Double
									.toString(Double.parseDouble(p.getProperty(v
											.getName() + "_rMax"))	/ delayScaleFactor));
						}
					}
				}
			}
		}
		for (Variable v : reqdVarsL) {
			// if (!v.isDmvc()){ this if maynot be required.. rates do exist for dmvc ones as well.. since calculated before detectDMV
			v.scaleInitByDelay(delayScaleFactor);
			// }
		}
		// SEE IF RATES IN TRANSITIONS HAVE TO BE ADJUSTED HERE

	}

	public Double getMinDiv() {
		Double minDiv = divisionsL.get(0).get(0);
		for (int i = 0; i < divisionsL.size(); i++) {
			for (int j = 0; j < divisionsL.get(i).size(); j++) {
				if (divisionsL.get(i).get(j) < minDiv) {
					minDiv = divisionsL.get(i).get(j);
				}
			}
		}
		return minDiv;
	}

	public Double getMaxDiv() {
		Double maxDiv = divisionsL.get(0).get(0);
		for (int i = 0; i < divisionsL.size(); i++) {
			for (int j = 0; j < divisionsL.get(i).size(); j++) {
				if (divisionsL.get(i).get(j) > maxDiv) {
					maxDiv = divisionsL.get(i).get(j);
				}
			}
		}
		return maxDiv;
	}

	public Double getMinRate() { // minimum of entire lpn
		Double minRate = null;
		for (String place : placeInfo.keySet()) {
			Properties p = placeInfo.get(place);
			if (p.getProperty("type").equals("RATE")) {
				for (Variable v : reqdVarsL) {
					if ((minRate == null)
							&& (p.getProperty(v.getName() + "_rMin") != null)
							&& (Double.parseDouble(p.getProperty(v.getName()
									+ "_rMin")) != 0.0)) {
						minRate = Double.parseDouble(p.getProperty(v.getName()
								+ "_rMin"));
					} else if ((p.getProperty(v.getName() + "_rMin") != null)
							&& (Double.parseDouble(p.getProperty(v.getName()
									+ "_rMin")) < minRate)
							&& (Double.parseDouble(p.getProperty(v.getName()
									+ "_rMin")) != 0.0)) {
						minRate = Double.parseDouble(p.getProperty(v.getName()
								+ "_rMin"));
					}
				}
			}
		}
		return minRate;
	}

	public Double getMaxRate() {
		Double maxRate = null;
		for (String place : placeInfo.keySet()) {
			Properties p = placeInfo.get(place);
			if (p.getProperty("type").equals("RATE")) {
				for (Variable v : reqdVarsL) {
					if ((maxRate == null)
							&& (p.getProperty(v.getName() + "_rMax") != null)
							&& (Double.parseDouble(p.getProperty(v.getName()
									+ "_rMax")) != 0.0)) {
						maxRate = Double.parseDouble(p.getProperty(v.getName()
								+ "_rMax"));
					} else if ((p.getProperty(v.getName() + "_rMax") != null)
							&& (Double.parseDouble(p.getProperty(v.getName()
									+ "_rMax")) > maxRate)
							&& (Double.parseDouble(p.getProperty(v.getName()
									+ "_rMax")) != 0.0)) {
						maxRate = Double.parseDouble(p.getProperty(v.getName()
								+ "_rMax"));
					}
				}
			}
		}
		return maxRate;
	}

	public Double getMinDelay() {
		Double minDelay = null;
		for (String place : placeInfo.keySet()) {
			Properties p = placeInfo.get(place);
			if (p.getProperty("type").equals("DMVC")) {
				if ((minDelay == null) && (getMinDmvcTime(p) != null)
						&& (getMinDmvcTime(p) != 0)) {
					minDelay = getMinDmvcTime(p);
				} else if ((getMinDmvcTime(p) != null)
						&& (getMinDmvcTime(p) != 0)
						&& (getMinDmvcTime(p) < minDelay)) {
					minDelay = getMinDmvcTime(p);
				}
			} else {
				if ((minDelay == null) && (p.getProperty("dMin") != null)
						&& (Double.parseDouble(p.getProperty("dMin")) != 0)) {
					minDelay = Double.parseDouble(p.getProperty("dMin"));
				} else if ((p.getProperty("dMin") != null)
						&& (Double.parseDouble(p.getProperty("dMin")) != 0)
						&& (Double.parseDouble(p.getProperty("dMin")) < minDelay)) {
					minDelay = Double.parseDouble(p.getProperty("dMin"));
				}

			}
		}
		return minDelay;
	}

	public Double getMaxDelay() {
		Double maxDelay = null;
		for (String place : placeInfo.keySet()) {
			Properties p = placeInfo.get(place);
			if (p.getProperty("type").equals("DMVC")) {
				if ((maxDelay == null) && (getMaxDmvcTime(p) != null)
						&& (getMaxDmvcTime(p) != 0)) {
					maxDelay = getMaxDmvcTime(p);
				} else if ((getMaxDmvcTime(p) != null)
						&& (getMaxDmvcTime(p) != 0)
						&& (getMaxDmvcTime(p) > maxDelay)) {
					maxDelay = getMaxDmvcTime(p);
				}
			} else {
				if ((maxDelay == null) && (p.getProperty("dMax") != null)
						&& (Double.parseDouble(p.getProperty("dMax")) != 0)) {
					maxDelay = Double.parseDouble(p.getProperty("dMax"));
				} else if ((p.getProperty("dMax") != null)
						&& (Double.parseDouble(p.getProperty("dMax")) != 0)
						&& (Double.parseDouble(p.getProperty("dMax")) > maxDelay)) {
					maxDelay = Double.parseDouble(p.getProperty("dMax"));
				}

			}
		}
		return maxDelay;
	}

	public Double getMinDmvcTime(Properties p) {
		String[] times = null;
		String name = p.getProperty("DMVCVar");
		String s = p.getProperty("dmvcTime_" + name);
		if (s != null) {
			times = s.split(" ");
			Double min = Double.parseDouble(times[0]);
			for (int i = 0; i < times.length; i++) {
				if (Double.parseDouble(times[i]) < min) {
					min = Double.parseDouble(times[i]);
				}
			}
			return min;
		} else {
			return null;
		}
	}

	public Double getMaxDmvcTime(Properties p) {
		String[] times = null;
		String name = p.getProperty("DMVCVar");
		String s = p.getProperty("dmvcTime_" + name);
		if (s != null) {
			times = s.split(" ");
			Double max = Double.parseDouble(times[0]);
			for (int i = 0; i < times.length; i++) {
				if (Double.parseDouble(times[i]) > max) {
					max = Double.parseDouble(times[i]);
				}
			}
			return max;
		} else {
			return null;
		}
	}

	public void addDuration(Properties p, Double d) {
		Double dMin;
		Double dMax;
		// d = d*(10^6);
		if ((p.getProperty("dMin") == null) && (p.getProperty("dMax") == null)) {
			// p.setProperty("dMin", Integer.toString((int)(Math.floor(d))));
			// p.setProperty("dMax", Integer.toString((int)(Math.floor(d))));
			p.setProperty("dMin", d.toString());
			p.setProperty("dMax", d.toString());
			return;
		} else {
			dMin = Double.parseDouble(p.getProperty("dMin"));
			dMax = Double.parseDouble(p.getProperty("dMax"));
			if (d < dMin) {
				dMin = d;
			} else if (d > dMax) {
				dMax = d;
			}
		}
		p.setProperty("dMin", dMin.toString());
		p.setProperty("dMax", dMax.toString());
		// p.setProperty("dMin", Integer.toString((int)(Math.floor(dMin))));
		// p.setProperty("dMax", Integer.toString((int)(Math.ceil(dMax))));
	}

	public String getPlaceInfoIndex(String s) {
		String index = null;
		for (String st2 : placeInfo.keySet()) {
			if (("p" + placeInfo.get(st2).getProperty("placeNum"))
					.equalsIgnoreCase(s)) {
				index = st2;
				break;
			}
		}
		return index;
	}

	public ArrayList<Integer> diff(String pre_bin, String post_bin) {
		ArrayList<Integer> diffL = new ArrayList<Integer>();
		// String p_bin[] = p.getBinEncoding();
		String[] preset_encoding = pre_bin.split("");
		String[] postset_encoding = post_bin.split("");
		for (int j = 1; j < preset_encoding.length; j++) { // to account for ""
			// being created in the array
			if (Integer.parseInt(preset_encoding[j]) != Integer
					.parseInt(postset_encoding[j])) {
				diffL.add(j - 1);// to account for "" being created in the
				// array
			}
		}
		return (diffL);
	}

	public int getMinRate(String place, String name) {
		Properties p = placeInfo.get(place);
		return ((int) Math.floor(Double.parseDouble(p.getProperty(name
				+ "_rMin"))));
		// return(rMin[i]);
	}

	public int getMaxRate(String place, String name) {
		Properties p = placeInfo.get(place);
		return ((int) Math.floor(Double.parseDouble(p.getProperty(name
				+ "_rMax"))));
		// return(rMin[i]);
	}

}
/*
 * public String cleanRow (String row){ String rowNS,rowTS = null; try{ rowNS =
 * lParenR.matcher(row).replaceAll(""); rowTS =
 * rParenR.matcher(rowNS).replaceAll(""); return rowTS; }
 * catch(PatternSyntaxException pse){ System.out.format("There is a problem with
 * the regular expression!%n"); System.out.format("The pattern in question is:
 * %s%n",pse.getPattern()); System.out.format("The description is:
 * %s%n",pse.getDescription()); System.out.format("The message is:
 * %s%n",pse.getMessage()); System.out.format("The index is:
 * %s%n",pse.getIndex()); System.exit(0); return rowTS; } }
 */

