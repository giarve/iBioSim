/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.ibiosim.learn.genenet;

/**
 * A Thresholds object is used to compute the type of interaction of a connection.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Thresholds
{

	private double	Ta, Tr, Ti, Tt;

	/**
	 * Creates a Thresholds object using default values.
	 */
	public Thresholds()
	{
		Ta = 1.15;
		Tr = 0.75;
		Ti = 0.5;
		Tt = 0.025;
	}

	/**
   * Creates a Thresholds object using custom values.
   *
	 * @param Ta - activation threshold.
	 * @param Tr - repression threshold.
	 * @param Ti - no influence threshold.
	 */
	public Thresholds(double Ta, double Tr, double Ti)
	{
		this.Ta = Ta;
		this.Tr = Tr;
		this.Ti = Ti;
		this.Tt = 0.025;
	}
	
	 /**
   * Creates a Thresholds object using custom values.
   *
   * @param Ta - activation threshold.
   * @param Tr - repression threshold.
   * @param Ti - no influence threshold.
   * @param Tt - how relaxed the activation and repression thresholds are.
   */
  public Thresholds(double Ta, double Tr, double Ti, double Tt)
  {
    this.Ta = Ta;
    this.Tr = Tr;
    this.Ti = Ti;
    this.Tt = Tt;
  }

	/**
	 * Returns the threshold value of activation.
	 * 
	 * @return activation threshold value.
	 */
	public double getTa()
	{
		return Ta;
	}

	/**
	 * Returns the threshold value of repression.
	 * 
	 * @return repression threshold value.
	 */
	public double getTr()
	{
		return Tr;
	}

	/**
	 * Returns the threshold value with no influence.
	 * 
	 * @return threshold for no influence.
	 */
	public double getTi()
	{
		return Ti;
	}

	/**
	 * Set activation threshold.
	 * 
	 * @param Ta - value of threshold.
	 */
	public void setTa(double Ta)
	{
		this.Ta = Ta;
	}

	/**
	 * Set repression threshold.
	 * 
	 * @param Tr - value of threshold.
	 */
	public void setTr(double Tr)
	{
		this.Tr = Tr;
	}

	/**
	 * Set threshold for no influence.
	 * 
	 * @param Ti - value of threshold.
	 */
	public void setTi(double Ti)
	{
		this.Ti = Ti;
	}

	/**
	 * Returns relaxation threshold.
	 * 
	 * @return relaxation threshold.
	 */
	public double getTt()
	{
		return Tt;
	}
}
