package edu.utah.ece.async.ibiosim.analysis.properties;

import java.util.ArrayList;
import java.util.List;

public class SimulationProperties {


  private int         numSteps, run;
  private double        initialTime, outputStartTime, minTimeStep, maxTimeStep, printInterval, timeLimit, timeStep, absError, relError;
  
  private String        printer_id, printer_track_quantity,genStats;
 
  private long        rndSeed;
  
  private List<String>      intSpecies;
  
 
  public SimulationProperties()
  {
    run = 1;
    initialTime = 0;
    outputStartTime = 0;
    minTimeStep = 0;
    printInterval = 1;
    timeStep = 1;
    absError = 1e-9;
    relError = 1e-9;
    rndSeed = 314159;
    printer_track_quantity = "amount";
    genStats = "false";
    timeLimit = 100;
  }
  /**
   * @return the absError
   */
  public double getAbsError() {
    return absError;
  }
  
  /**
   * @return the initialTime
   */
  public double getInitialTime() {
    return initialTime;
  }
  /**
   * @return the minTimeStep
   */
  public double getMinTimeStep() {
    return minTimeStep;
  }
  /**
   * @return the maxTimeStep
   */
  public double getMaxTimeStep() {
    return maxTimeStep;
  }
  
  /**
   * @return the outputStartTime
   */
  public double getOutputStartTime() {
    return outputStartTime;
  }
  
  /**
   * @return the printer_id
   */
  public String getPrinter_id() {
    return printer_id;
  }
  /**
   * @return the printer_track_quantity
   */
  public String getPrinter_track_quantity() {
    return printer_track_quantity;
  }
  
  /**
   * @return the printInterval
   */
  public double getPrintInterval() {
    return printInterval;
  }
  

  /**
   * @return the relError
   */
  public double getRelError() {
    return relError;
  }
  /**
   * @return the rndSeed
   */
  public long getRndSeed() {
    return rndSeed;
  }
  

  /**
   * @return the timeLimit
   */
  public double getTimeLimit() {
    return timeLimit;
  }
  /**
   * @return the timeStep
   */
  public double getTimeStep() {
    return timeStep;
  }
  
  /**
   * @return the run
   */
  public int getRun() {
    return run;
  }
  
  /**
   * @param absError the absError to set
   */
  public void setAbsError(double absError) {
    this.absError = absError;
  }
  
  /**
   * @param initialTime the initialTime to set
   */
  public void setInitialTime(double initialTime) {
    this.initialTime = initialTime;
  }
  
  /**
   * @param minTimeStep the minTimeStep to set
   */
  public void setMinTimeStep(double minTimeStep) {
    this.minTimeStep = minTimeStep;
  }
  
  /**
   * @param maxTimeStep the minTimeStep to set
   */
  public void setMaxTimeStep(double maxTimeStep) {
    this.maxTimeStep = maxTimeStep;
  }
  
  /**
   * @param outputStartTime the outputStartTime to set
   */
  public void setOutputStartTime(double outputStartTime) {
    this.outputStartTime = outputStartTime;
  }
  

  /**
   * @param printer_id the printer_id to set
   */
  public void setPrinter_id(String printer_id) {
    this.printer_id = printer_id;
  }
  /**
   * @param printer_track_quantity the printer_track_quantity to set
   */
  public void setPrinter_track_quantity(String printer_track_quantity) {
    this.printer_track_quantity = printer_track_quantity;
  }
  
  /**
   * @param printInterval the printInterval to set
   */
  public void setPrintInterval(double printInterval) {
    this.printInterval = printInterval;
  }
  
  /**
   * @param relError the relError to set
   */
  public void setRelError(double relError) {
    this.relError = relError;
  }
  /**
   * @param rndSeed the rndSeed to set
   */
  public void setRndSeed(long rndSeed) {
    this.rndSeed = rndSeed;
  }
  /**
   * @param run the run to set
   */
  public void setRun(int run) {
    this.run = run;
  }
  
  /**
   * @param timeLimit the timeLimit to set
   */
  public void setTimeLimit(double timeLimit) {
    this.timeLimit = timeLimit;
  }
  /**
   * @param timeStep the timeStep to set
   */
  public void setTimeStep(double timeStep) {
    this.timeStep = timeStep;
  }
  
  
  public int getNumSteps() {
    return numSteps;
  }

  
  public void setNumSteps(int numSteps) {
    this.numSteps = numSteps;
  }
  
  /**
   * 
   */
  public void addIntSpecies(String species) {
    if(intSpecies==null)
    {
      intSpecies = new ArrayList<String>();
    }
    intSpecies.add(species);
  }
  
  /**
   * @return the intSpecies
   */
  public List<String> getIntSpecies() {
    return intSpecies;
  }
  
  /**
   * @param intSpecies the intSpecies to set
   */
  public void setIntSpecies(List<String> intSpecies) {
    this.intSpecies = intSpecies;
  }
  
  /**
   * @return the genStats
   */
  public String getGenStats() {
    return genStats;
  }
  
  /**
   * @param genStats the genStats to set
   */
  public void setGenStats(String genStats) {
    this.genStats = genStats;
  }


}
