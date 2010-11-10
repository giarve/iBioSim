package stategraph;

import java.util.ArrayList;

public class PerfromMarkovAnalysisThread extends Thread {

	private StateGraph sg;
	
	private double tolerance;
	
	private ArrayList<String> conditions;

	public PerfromMarkovAnalysisThread(StateGraph sg) {
		super(sg);
		this.sg = sg;
	}

	public void start(double tolerance, ArrayList<String> conditions) {
		this.tolerance = tolerance;
		this.conditions = conditions;
		super.start();
	}

	@Override
	public void run() {
		sg.performSteadyStateMarkovianAnalysis(tolerance, conditions);
	}
}
