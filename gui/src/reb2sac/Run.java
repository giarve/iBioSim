package reb2sac;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import biomodelsim.*;
import graph.*;
import buttons.*;
import sbmleditor.*;

/**
 * This class creates the properties file that is given to the reb2sac program.
 * It also executes the reb2sac program.
 * 
 * @author Curtis Madsen
 */
public class Run implements ActionListener {

	private Process reb2sac;

	private String separator;
				
        private Reb2Sac r2s;

	public Run(Reb2Sac reb2sac) {
	        r2s = reb2sac;
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}
	}

	/**
	 * This method is given which buttons are selected and creates the properties
	 * file from all the other information given.
	 */
	public void createProperties(double timeLimit, double printInterval, double timeStep,
			double absError, String outDir, long rndSeed, int run, String[] termCond,
			String[] intSpecies, String printer_id, String printer_track_quantity, String[] getFilename,
			String selectedButtons, Component component, String filename, double rap1, double rap2,
			double qss, int con, JCheckBox usingSSA, String ssaFile, JCheckBox usingSad, File sadFile) {
		Properties abs = new Properties();
		if (selectedButtons.contains("abs") || selectedButtons.contains("nary")) {
			abs.setProperty("reb2sac.abstraction.method.0.1", "enzyme-kinetic-qssa-1");
			abs.setProperty("reb2sac.abstraction.method.0.2", "reversible-to-irreversible-transformer");
			abs.setProperty("reb2sac.abstraction.method.0.3", "multiple-products-reaction-eliminator");
			abs.setProperty("reb2sac.abstraction.method.0.4", "multiple-reactants-reaction-eliminator");
			abs.setProperty("reb2sac.abstraction.method.0.5",
					"single-reactant-product-reaction-eliminator");
			abs.setProperty("reb2sac.abstraction.method.0.6", "dimer-to-monomer-substitutor");
			abs.setProperty("reb2sac.abstraction.method.0.7", "inducer-structure-transformer");
			abs.setProperty("reb2sac.abstraction.method.1.1", "modifier-structure-transformer");
			abs.setProperty("reb2sac.abstraction.method.1.2", "modifier-constant-propagation");
			abs.setProperty("reb2sac.abstraction.method.2.1", "operator-site-forward-binding-remover");
			abs.setProperty("reb2sac.abstraction.method.2.3", "enzyme-kinetic-rapid-equilibrium-1");
			abs.setProperty("reb2sac.abstraction.method.2.4", "irrelevant-species-remover");
			abs.setProperty("reb2sac.abstraction.method.2.5", "inducer-structure-transformer");
			abs.setProperty("reb2sac.abstraction.method.2.6", "modifier-constant-propagation");
			abs.setProperty("reb2sac.abstraction.method.2.7", "similar-reaction-combiner");
			abs.setProperty("reb2sac.abstraction.method.2.8", "modifier-constant-propagation");
		}
		if (selectedButtons.contains("abs")) {
			abs.setProperty("reb2sac.abstraction.method.2.2", "dimerization-reduction");
		}
		else if (selectedButtons.contains("nary")) {
			abs.setProperty("reb2sac.abstraction.method.2.2", "dimerization-reduction-level-assignment");
		}
		abs.setProperty("simulation.printer", printer_id);
		abs.setProperty("simulation.printer.tracking.quantity", printer_track_quantity);
		if (selectedButtons.equals("none_monteCarlo")) {
			abs.setProperty("reb2sac.abstraction.method.3.1", "reversible-to-irreversible-transformer");
			abs.setProperty("reb2sac.abstraction.method.3.2", "kinetic-law-constants-simplifier");
		}
		else if (selectedButtons.contains("none")) {
			abs.setProperty("reb2sac.abstraction.method.3.1", "kinetic-law-constants-simplifier");
		}
		for (int i = 0; i < intSpecies.length; i++) {
			if (intSpecies[i] != "") {
				abs.setProperty("reb2sac.interesting.species." + (i + 1), "" + intSpecies[i]);
			}
		}
		abs.setProperty("reb2sac.rapid.equilibrium.condition.1", "" + rap1);
		abs.setProperty("reb2sac.rapid.equilibrium.condition.2", "" + rap2);
		abs.setProperty("reb2sac.qssa.condition.1", "" + qss);
		abs.setProperty("reb2sac.operator.max.concentration.threshold", "" + con);
		if (selectedButtons.contains("none")) {
			abs.setProperty("reb2sac.abstraction.method", "none");
		}
		if (selectedButtons.contains("abs")) {
			abs.setProperty("reb2sac.abstraction.method", "abs");
		}
		else if (selectedButtons.contains("nary")) {
			abs.setProperty("reb2sac.abstraction.method", "nary");
		}
		if (selectedButtons.contains("ODE")) {
			abs.setProperty("reb2sac.simulation.method", "ODE");
		}
		else if (selectedButtons.contains("monteCarlo")) {
			abs.setProperty("reb2sac.simulation.method", "monteCarlo");
		}
		else if (selectedButtons.contains("markov")) {
			abs.setProperty("reb2sac.simulation.method", "markov");
		}
		else if (selectedButtons.contains("sbml")) {
			abs.setProperty("reb2sac.simulation.method", "SBML");
		}
		else if (selectedButtons.contains("dot")) {
			abs.setProperty("reb2sac.simulation.method", "Network");
		}
		else if (selectedButtons.contains("xhtml")) {
			abs.setProperty("reb2sac.simulation.method", "Browser");
		}
		if (!selectedButtons.contains("monteCarlo")) {
			// if (selectedButtons.equals("none_ODE") ||
			// selectedButtons.equals("abs_ODE")) {
			abs.setProperty("ode.simulation.time.limit", "" + timeLimit);
			abs.setProperty("ode.simulation.print.interval", "" + printInterval);
			if (timeStep == Double.MAX_VALUE) {
			  abs.setProperty("ode.simulation.time.step", "inf");
			} else {
			  abs.setProperty("ode.simulation.time.step", "" + timeStep);
			}
			abs.setProperty("ode.simulation.absolute.error", "" + absError);
			abs.setProperty("ode.simulation.out.dir", outDir);
		}
		if (!selectedButtons.contains("ODE")) {
			// if (selectedButtons.equals("none_monteCarlo") ||
			// selectedButtons.equals("abs_monteCarlo")) {
			abs.setProperty("monte.carlo.simulation.time.limit", "" + timeLimit);
			abs.setProperty("monte.carlo.simulation.print.interval", "" + printInterval);
			if (timeStep == Double.MAX_VALUE) {
			  abs.setProperty("monte.carlo.simulation.time.step", "inf");
			} else {
			  abs.setProperty("monte.carlo.simulation.time.step", "" + timeStep);
			}
			abs.setProperty("monte.carlo.simulation.random.seed", "" + rndSeed);
			abs.setProperty("monte.carlo.simulation.runs", "" + run);
			abs.setProperty("monte.carlo.simulation.out.dir", outDir);
			if (usingSad.isSelected()) {
				abs.setProperty("simulation.run.termination.decider", "sad");
				abs.setProperty("computation.analysis.sad.path", sadFile.getName());
			}
		}
		if (!usingSad.isSelected()) {
			abs.setProperty("simulation.run.termination.decider", "constraint");
		}
		if (usingSSA.isSelected() && selectedButtons.contains("monteCarlo")) {
			abs.setProperty("simulation.time.series.species.level.file", ssaFile);
		}
		for (int i = 0; i < termCond.length; i++) {
			if (termCond[i] != "") {
				abs.setProperty("simulation.run.termination.condition." + (i + 1), "" + termCond[i]);
			}
		}
		try {
			if (!getFilename[getFilename.length - 1].contains(".")) {
				getFilename[getFilename.length - 1] += ".";
				filename += ".";
			}
			int cut = 0;
			for (int i = 0; i < getFilename[getFilename.length - 1].length(); i++) {
				if (getFilename[getFilename.length - 1].charAt(i) == '.') {
					cut = i;
				}
			}
			FileOutputStream store = new FileOutputStream(new File((filename.substring(0, filename
					.length()
					- getFilename[getFilename.length - 1].length()))
					+ getFilename[getFilename.length - 1].substring(0, cut) + ".properties"));
			abs.store(store, getFilename[getFilename.length - 1].substring(0, cut) + " Properties");
			store.close();
		}
		catch (Exception except) {
			JOptionPane.showMessageDialog(component, "Unable To Save Properties File!"
					+ "\nMake sure you select a model for abstraction.", "Unable To Save File",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * This method is given what data is entered into the nary frame and creates
	 * the nary properties file from that information.
	 */
	public void createNaryProperties(double timeLimit, double printInterval, double timeStep, String outDir,
			long rndSeed, int run, String printer_id, String printer_track_quantity,
			String[] getFilename, Component component, String filename, JRadioButton monteCarlo,
			String stopE, double stopR, String[] finalS, ArrayList<JTextField> inhib,
			ArrayList<JList> consLevel, ArrayList<String> getSpeciesProps, ArrayList<Object[]> conLevel,
			String[] termCond, String[] intSpecies, double rap1, double rap2, double qss, int con,
			ArrayList<Integer> counts, JCheckBox usingSSA, String ssaFile) {
		Properties nary = new Properties();
		try {
			FileInputStream load = new FileInputStream(
					new File(outDir + separator + "species.properties"));
			nary.load(load);
			load.close();
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(component, "Species Properties File Not Found!",
					"File Not Found", JOptionPane.ERROR_MESSAGE);
		}
		nary.setProperty("reb2sac.abstraction.method.0.1", "enzyme-kinetic-qssa-1");
		nary.setProperty("reb2sac.abstraction.method.0.2", "reversible-to-irreversible-transformer");
		nary.setProperty("reb2sac.abstraction.method.0.3", "multiple-products-reaction-eliminator");
		nary.setProperty("reb2sac.abstraction.method.0.4", "multiple-reactants-reaction-eliminator");
		nary.setProperty("reb2sac.abstraction.method.0.5",
				"single-reactant-product-reaction-eliminator");
		nary.setProperty("reb2sac.abstraction.method.0.6", "dimer-to-monomer-substitutor");
		nary.setProperty("reb2sac.abstraction.method.0.7", "inducer-structure-transformer");
		nary.setProperty("reb2sac.abstraction.method.1.1", "modifier-structure-transformer");
		nary.setProperty("reb2sac.abstraction.method.1.2", "modifier-constant-propagation");
		nary.setProperty("reb2sac.abstraction.method.2.1", "operator-site-forward-binding-remover");
		nary.setProperty("reb2sac.abstraction.method.2.3", "enzyme-kinetic-rapid-equilibrium-1");
		nary.setProperty("reb2sac.abstraction.method.2.4", "irrelevant-species-remover");
		nary.setProperty("reb2sac.abstraction.method.2.5", "inducer-structure-transformer");
		nary.setProperty("reb2sac.abstraction.method.2.6", "modifier-constant-propagation");
		nary.setProperty("reb2sac.abstraction.method.2.7", "similar-reaction-combiner");
		nary.setProperty("reb2sac.abstraction.method.2.8", "modifier-constant-propagation");
		nary.setProperty("reb2sac.abstraction.method.2.2", "dimerization-reduction");
		nary.setProperty("reb2sac.abstraction.method.3.1", "nary-order-unary-transformer");
		nary.setProperty("reb2sac.abstraction.method.3.2", "modifier-constant-propagation");
		nary.setProperty("reb2sac.abstraction.method.3.3", "absolute-inhibition-generator");
		nary.setProperty("reb2sac.abstraction.method.3.4", "final-state-generator");
		nary.setProperty("reb2sac.abstraction.method.3.5", "stop-flag-generator");
		nary.setProperty("reb2sac.nary.order.decider", "distinct");
		nary.setProperty("simulation.printer", printer_id);
		nary.setProperty("simulation.printer.tracking.quantity", printer_track_quantity);
		nary.setProperty("reb2sac.analysis.stop.enabled", stopE);
		nary.setProperty("reb2sac.analysis.stop.rate", "" + stopR);
		for (int i = 0; i < getSpeciesProps.size(); i++) {
			if (!(inhib.get(i).getText().trim() != "<<none>>")) {
				nary.setProperty("reb2sac.absolute.inhibition.threshold." + getSpeciesProps.get(i), inhib
						.get(i).getText().trim());
			}
			String[] consLevels = Buttons.getList(conLevel.get(i), consLevel.get(i));
			for (int j = 0; j < counts.get(i); j++) {
				nary.remove("reb2sac.concentration.level." + getSpeciesProps.get(i) + "." + (j + 1));
			}
			for (int j = 0; j < consLevels.length; j++) {
				nary.setProperty("reb2sac.concentration.level." + getSpeciesProps.get(i) + "." + (j + 1),
						consLevels[j]);
			}
		}
		if (monteCarlo.isSelected()) {
			nary.setProperty("monte.carlo.simulation.time.limit", "" + timeLimit);
			nary.setProperty("monte.carlo.simulation.print.interval", "" + printInterval);
			if (timeStep == Double.MAX_VALUE) {
			  nary.setProperty("monte.carlo.simulation.time.step", "inf");
			} else {
			  nary.setProperty("monte.carlo.simulation.time.step", "" + timeStep);
			}
			nary.setProperty("monte.carlo.simulation.random.seed", "" + rndSeed);
			nary.setProperty("monte.carlo.simulation.runs", "" + run);
			nary.setProperty("monte.carlo.simulation.out.dir", ".");
		}
		for (int i = 0; i < finalS.length; i++) {
			if (finalS[i].trim() != "<<unknown>>") {
				nary.setProperty("reb2sac.final.state." + (i + 1), "" + finalS[i]);
			}
		}
		if (usingSSA.isSelected() && monteCarlo.isSelected()) {
			nary.setProperty("simulation.time.series.species.level.file", ssaFile);
		}
		for (int i = 0; i < intSpecies.length; i++) {
			if (intSpecies[i] != "") {
				nary.setProperty("reb2sac.interesting.species." + (i + 1), "" + intSpecies[i]);
			}
		}
		nary.setProperty("reb2sac.rapid.equilibrium.condition.1", "" + rap1);
		nary.setProperty("reb2sac.rapid.equilibrium.condition.2", "" + rap2);
		nary.setProperty("reb2sac.qssa.condition.1", "" + qss);
		nary.setProperty("reb2sac.operator.max.concentration.threshold", "" + con);
		for (int i = 0; i < termCond.length; i++) {
			if (termCond[i] != "") {
				nary.setProperty("simulation.run.termination.condition." + (i + 1), "" + termCond[i]);
			}
		}
		try {
			FileOutputStream store = new FileOutputStream(new File((filename.substring(0, filename
					.length()
					- getFilename[getFilename.length - 1].length()))
					+ getFilename[getFilename.length - 1].substring(0, getFilename[getFilename.length - 1]
							.length() - 5) + ".properties"));
			nary.store(store, getFilename[getFilename.length - 1].substring(0,
					getFilename[getFilename.length - 1].length() - 5)
					+ " Properties");
			store.close();
		}
		catch (Exception except) {
			JOptionPane.showMessageDialog(component, "Unable To Save Properties File!"
					+ "\nMake sure you select a model for simulation.", "Unable To Save File",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Executes the reb2sac program. If ODE, monte carlo, or markov is selected,
	 * this method creates a Graph object.
	 */
	public int execute(String filename, JRadioButton sbml, JRadioButton dot, JRadioButton xhtml,
			Component component, JRadioButton ode, JRadioButton monteCarlo, String sim,
			String printer_id, String printer_track_quantity, String outDir, JRadioButton nary,
			int naryRun, String[] intSpecies, Log log, JCheckBox usingSSA, String ssaFile,
			BioSim biomodelsim, JTabbedPane simTab, String root, JProgressBar progress, int steps,
			String simName) {
		Runtime exec = Runtime.getRuntime();
		int exitValue = 255;
		while (outDir.split(separator)[outDir.split(separator).length - 1].equals(".")) {
			outDir = outDir.substring(0, outDir.length() - 1
					- outDir.split(separator)[outDir.split(separator).length - 1].length());
		}
		try {
			long time1;
			String directory = "";
			String theFile = "";
			String sbmlName = "";
			if (filename.lastIndexOf('/') >= 0) {
				directory = filename.substring(0, filename.lastIndexOf('/') + 1);
				theFile = filename.substring(filename.lastIndexOf('/') + 1);
			}
			if (filename.lastIndexOf('\\') >= 0) {
				directory = filename.substring(0, filename.lastIndexOf('\\') + 1);
				theFile = filename.substring(filename.lastIndexOf('\\') + 1);
			}
			File work = new File(directory);
			String out = theFile;
			if (out.length() > 4 && out.substring(out.length() - 5, out.length()).equals(".sbml")) {
				out = out.substring(0, out.length() - 5);
			}
			else if (out.length() > 3 && out.substring(out.length() - 4, out.length()).equals(".xml")) {
				out = out.substring(0, out.length() - 4);
			}
			if (nary.isSelected() && naryRun == 1) {
				log.addText("Executing:\nreb2sac --target.encoding=nary-level " + filename + "\n");
				time1 = System.nanoTime();
				reb2sac = exec.exec("reb2sac --target.encoding=nary-level " + theFile, null, work);
			}
			else if (sbml.isSelected()) {
				sbmlName = JOptionPane.showInputDialog(component, "Enter SBML Model ID:", "Model ID",
						JOptionPane.PLAIN_MESSAGE);
				if (sbmlName != null && !sbmlName.trim().equals("")) {
					sbmlName = sbmlName.trim();
					if (sbmlName.length() > 4) {
						if (!sbmlName.substring(sbmlName.length() - 4).equals(".sbml")) {
							sbmlName += ".sbml";
						}
					}
					else {
						sbmlName += ".sbml";
					}
					File f = new File(root + separator + sbmlName);
					if (f.exists()) {
						Object[] options = { "Overwrite", "Cancel" };
						int value = JOptionPane.showOptionDialog(component, "File already exists."
								+ "\nDo you want to overwrite?", "Overwrite", JOptionPane.YES_NO_OPTION,
								JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
						if (value == JOptionPane.YES_OPTION) {
							File dir = new File(root + separator + sbmlName);
							if (dir.isDirectory()) {
								biomodelsim.deleteDir(dir);
							}
							else {
								System.gc();
								dir.delete();
							}
						}
						else {
							return 0;
						}
					}
					log.addText("Executing:\nreb2sac --target.encoding=sbml --out=" + ".." + separator
							+ sbmlName + " " + filename + "\n");
					time1 = System.nanoTime();
					reb2sac = exec.exec("reb2sac --target.encoding=sbml --out=" + ".." + separator + sbmlName
							+ " " + theFile, null, work);
				}
				else {
					time1 = System.nanoTime();
				}
			}
			else if (dot.isSelected()) {
				log.addText("Executing:\nreb2sac --target.encoding=dot --out=" + out + ".dot " + filename
						+ "\n");
				time1 = System.nanoTime();
				reb2sac = exec.exec("reb2sac --target.encoding=dot --out=" + out + ".dot " + theFile, null,
						work);
			}
			else if (xhtml.isSelected()) {
				log.addText("Executing:\nreb2sac --target.encoding=xhtml --out=" + out + ".xhtml "
						+ filename + "\n");
				time1 = System.nanoTime();
				reb2sac = exec.exec("reb2sac --target.encoding=xhtml --out=" + out + ".xhtml " + theFile,
						null, work);
			}
			else if (usingSSA.isSelected()) {
				log
						.addText("Executing:\nreb2sac --target.encoding=ssa-with-user-update " + filename
								+ "\n");
				time1 = System.nanoTime();
				reb2sac = exec
						.exec("reb2sac --target.encoding=ssa-with-user-update " + theFile, null, work);
			}
			else {
				if (sim.equals("atacs")) {
					log.addText("Executing:\nreb2sac --target.encoding=hse2 " + filename + "\n");
					time1 = System.nanoTime();
					reb2sac = exec.exec("reb2sac --target.encoding=hse2 " + theFile, null, work);
				}
				else {
					log.addText("Executing:\nreb2sac --target.encoding=" + sim + " " + filename + "\n");
					time1 = System.nanoTime();
					reb2sac = exec.exec("reb2sac --target.encoding=" + sim + " " + theFile, null, work);
				}
			}
			String error = "";
			try {
				InputStream reb = reb2sac.getInputStream();
				InputStreamReader isr = new InputStreamReader(reb);
				BufferedReader br = new BufferedReader(isr);
				int count = 0;
				while (br.readLine() != null) {
					if (steps > 0) {
						count++;
						progress.setValue(count);
					}
					// log.addText(output);
				}
				InputStream reb2 = reb2sac.getErrorStream();
				int read = reb2.read();
				while (read != -1) {
					error += (char) read;
					read = reb2.read();
				}
				br.close();
				isr.close();
				reb.close();
				reb2.close();
			}
			catch (Exception e) {
			}
			exitValue = reb2sac.waitFor();
			long time2 = System.nanoTime();
			long minutes;
			long hours;
			long days;
			double secs = ((time2 - time1) / 1000000000.0);
			long seconds = ((time2 - time1) / 1000000000);
			secs = secs - seconds;
			minutes = seconds / 60;
			secs = seconds % 60 + secs;
			hours = minutes / 60;
			minutes = minutes % 60;
			days = hours / 24;
			hours = hours % 60;
			String time;
			String dayLabel;
			String hourLabel;
			String minuteLabel;
			String secondLabel;
			if (days == 1) {
				dayLabel = " day ";
			}
			else {
				dayLabel = " days ";
			}
			if (hours == 1) {
				hourLabel = " hour ";
			}
			else {
				hourLabel = " hours ";
			}
			if (minutes == 1) {
				minuteLabel = " minute ";
			}
			else {
				minuteLabel = " minutes ";
			}
			if (seconds == 1) {
				secondLabel = " second";
			}
			else {
				secondLabel = " seconds";
			}
			if (days != 0) {
				time = days + dayLabel + hours + hourLabel + minutes + minuteLabel + secs + secondLabel;
			}
			else if (hours != 0) {
				time = hours + hourLabel + minutes + minuteLabel + secs + secondLabel;
			}
			else if (minutes != 0) {
				time = minutes + minuteLabel + secs + secondLabel;
			}
			else {
				time = secs + secondLabel;
			}
			if (!error.equals("")) {
				log.addText("Errors:\n" + error + "\n");
			}
			log.addText("Total Simulation Time: " + time + " for " + simName + "\n\n");
			if (exitValue != 0) {
				if (exitValue == 143) {
					JOptionPane.showMessageDialog(biomodelsim.frame(), "The simulation was"
							+ " canceled by the user.", "Canceled Simulation", JOptionPane.ERROR_MESSAGE);
				}
				else if (exitValue == 139) {
					JOptionPane.showMessageDialog(biomodelsim.frame(),
							"The selected model is not a valid sbml file." + "\nYou must select an sbml file.",
							"Not An SBML File", JOptionPane.ERROR_MESSAGE);
				}
				else {
					JOptionPane.showMessageDialog(biomodelsim.frame(), "Error In Execution!\n"
							+ "Bad Return Value!\n" + "The reb2sac program returned " + exitValue
							+ " as an exit value.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			else {
				if (nary.isSelected() && naryRun == 1) {
				}
				else if (sbml.isSelected()) {
					biomodelsim.addTab(sbmlName, new SBML_Editor(root + separator + sbmlName, null, log,
							biomodelsim, null, null), "SBML Editor");
					biomodelsim.refreshTree();
				}
				else if (dot.isSelected()) {
					log.addText("Executing:\ndotty " + directory + out + ".dot" + "\n");
					exec.exec("dotty " + out + ".dot", null, work);
				}
				else if (xhtml.isSelected()) {
					if (System.getProperty("os.name").contentEquals("Linux")) {
						log.addText("Executing:\ngnome-open " + directory + out + ".xhtml" + "\n");
						exec.exec("gnome-open " + out + ".xhtml", null, work);
					}
					else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
						log.addText("Executing:\nopen " + directory + out + ".xhtml" + "\n");
						exec.exec("open " + out + ".xhtml", null, work);
					}
					else {
						log.addText("Executing:\ncmd /c start " + directory + out + ".xhtml" + "\n");
						exec.exec("cmd /c start " + out + ".xhtml", null, work);
					}
				}
				else if (usingSSA.isSelected()) {
					if (!printer_id.equals("null.printer")) {
						for (int i = 0; i < simTab.getComponentCount(); i++) {
							if (simTab.getComponentAt(i).getName().equals("Graph")) {
								if (simTab.getComponentAt(i) instanceof Graph) {
									((Graph) simTab.getComponentAt(i)).refresh();
								}
								else {
								  simTab.setComponentAt(i, new Graph(r2s, printer_track_quantity, outDir
											.split(separator)[outDir.split(separator).length - 1]
											+ " simulation results", printer_id, outDir, "time", biomodelsim, null, log,
											null, true));
									simTab.getComponentAt(i).setName("Graph");
								}
							}
							if (simTab.getComponentAt(i).getName().equals("ProbGraph")) {
								if (simTab.getComponentAt(i) instanceof Graph) {
									((Graph) simTab.getComponentAt(i)).refresh();
								}
								else {
									if (new File(filename.substring(0, filename.length()
											- filename.split(separator)[filename.split(separator).length - 1].length())
											+ "sim-rep.txt").exists()) {
									  simTab.setComponentAt(i, new Graph(r2s, printer_track_quantity, outDir
												.split(separator)[outDir.split(separator).length - 1]
												+ " simulation results", printer_id, outDir, "time", biomodelsim, null,
												log, null, false));
										simTab.getComponentAt(i).setName("ProbGraph");
									}
								}
							}
						}
					}
				}
				else if (sim.equals("atacs")) {
					log.addText("Executing:\natacs -T0.000001 -oqoflhsgllvA "
							+ filename.substring(0, filename.length()
									- filename.split(separator)[filename.split(separator).length - 1].length())
							+ "out.hse\n");
					exec.exec("atacs -T0.000001 -oqoflhsgllvA out.hse", null, work);
					for (int i = 0; i < simTab.getComponentCount(); i++) {
						if (simTab.getComponentAt(i).getName().equals("ProbGraph")) {
							if (simTab.getComponentAt(i) instanceof Graph) {
								((Graph) simTab.getComponentAt(i)).refresh();
							}
							else {
							  simTab.setComponentAt(i, new Graph(r2s, printer_track_quantity,
										outDir.split(separator)[outDir.split(separator).length - 1]
												+ " simulation results", printer_id, outDir, "time", biomodelsim, null,
										log, null, false));
								simTab.getComponentAt(i).setName("ProbGraph");
							}
						}
					}
					// simTab.add("Probability Graph", new Graph(printer_track_quantity,
					// outDir.split(separator)[outDir.split(separator).length - 1] + "
					// simulation results",
					// printer_id, outDir, "time", biomodelsim, null, log, null, false));
					// simTab.getComponentAt(simTab.getComponentCount() -
					// 1).setName("ProbGraph");
				}
				else {
					if (!printer_id.equals("null.printer")) {
						if (ode.isSelected()) {
							for (int i = 0; i < simTab.getComponentCount(); i++) {
								if (simTab.getComponentAt(i).getName().equals("Graph")) {
									if (simTab.getComponentAt(i) instanceof Graph) {
										((Graph) simTab.getComponentAt(i)).refresh();
									}
									else {
									  simTab.setComponentAt(i, new Graph(r2s, printer_track_quantity, outDir
												.split(separator)[outDir.split(separator).length - 1]
												+ " simulation results", printer_id, outDir, "time", biomodelsim, null,
												log, null, true));
										simTab.getComponentAt(i).setName("Graph");
									}
								}
								if (simTab.getComponentAt(i).getName().equals("ProbGraph")) {
									if (simTab.getComponentAt(i) instanceof Graph) {
										((Graph) simTab.getComponentAt(i)).refresh();
									}
									else {
										if (new File(filename.substring(0, filename.length()
												- filename.split(separator)[filename.split(separator).length - 1].length())
												+ "sim-rep.txt").exists()) {
										  simTab.setComponentAt(i, new Graph(r2s, printer_track_quantity, outDir
													.split(separator)[outDir.split(separator).length - 1]
													+ " simulation results", printer_id, outDir, "time", biomodelsim, null,
													log, null, false));
											simTab.getComponentAt(i).setName("ProbGraph");
										}
									}
								}
							}
						}
						else if (monteCarlo.isSelected()) {
							for (int i = 0; i < simTab.getComponentCount(); i++) {
								if (simTab.getComponentAt(i).getName().equals("Graph")) {
									if (simTab.getComponentAt(i) instanceof Graph) {
										((Graph) simTab.getComponentAt(i)).refresh();
									}
									else {
									  simTab.setComponentAt(i, new Graph(r2s, printer_track_quantity, outDir
												.split(separator)[outDir.split(separator).length - 1]
												+ " simulation results", printer_id, outDir, "time", biomodelsim, null,
												log, null, true));
										simTab.getComponentAt(i).setName("Graph");
									}
								}
								if (simTab.getComponentAt(i).getName().equals("ProbGraph")) {
									if (simTab.getComponentAt(i) instanceof Graph) {
										((Graph) simTab.getComponentAt(i)).refresh();
									}
									else {
										if (new File(filename.substring(0, filename.length()
												- filename.split(separator)[filename.split(separator).length - 1].length())
												+ "sim-rep.txt").exists()) {
										  simTab.setComponentAt(i, new Graph(r2s, printer_track_quantity, outDir
													.split(separator)[outDir.split(separator).length - 1]
													+ " simulation results", printer_id, outDir, "time", biomodelsim, null,
													log, null, false));
											simTab.getComponentAt(i).setName("ProbGraph");
										}
									}
								}
							}
						}
					}
				}
			}
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(biomodelsim.frame(), "Error In Execution!",
					"Error In Execution", JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		}
		return exitValue;
	}

	/**
	 * This method is called if a button that cancels the simulation is pressed.
	 */
	public void actionPerformed(ActionEvent e) {
		reb2sac.destroy();
	}
}
