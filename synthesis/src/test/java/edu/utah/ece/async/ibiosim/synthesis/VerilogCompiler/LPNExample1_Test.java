package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModule;
import edu.utah.ece.async.lema.verification.lpn.LPN;

/**
 * Test lpn output for even zero design.  
 * 
 * @author Tramy Nguyen
 */
public class LPNExample1_Test {
	
	private static LPN lpn;
	
	@BeforeClass
	public static void setupTest() throws XMLStreamException, IOException, BioSimException, VerilogCompilerException, SBMLException, ParseException, SBOLValidationException { 
		VerilogToLPNCompiler compiler = new VerilogToLPNCompiler();
		VerilogModule spec = compiler.parseVerilogFile(new File(TestingFiles.verilogEvenZero_impFile));
		VerilogModule tb = compiler.parseVerilogFile(new File(TestingFiles.verilogEvenZero_tbFile));
		lpn = compiler.compileToLPN(spec, tb, TestingFiles.writeOutputDir);
	}
	
	@Test
	public void Test_inputSize(){
		Assert.assertEquals(2, lpn.getAllInputs().size());
	}
	
	@Test
	public void Test_inputs(){
		Map<String, String> expected_in = new HashMap<String, String>();
		expected_in.put("bit0", "false");
		expected_in.put("bit1", "false");

		Assert.assertTrue(expected_in.keySet().equals(lpn.getAllInputs().keySet()));
		Assert.assertTrue(expected_in.equals(lpn.getAllInputs()));
	}
	
	@Test
	public void Test_outputSize(){
		Assert.assertEquals(2, lpn.getAllOutputs().size());
	}
	
	
	@Test
	public void Test_outputs(){
		Map<String, String> expected_out = new HashMap<String, String>();
		expected_out.put("parity0", "false");
		expected_out.put("parity1", "false");
	
		Assert.assertTrue(expected_out.keySet().equals(lpn.getAllOutputs().keySet()));
		Assert.assertTrue(expected_out.equals(lpn.getAllOutputs()));
	}
	
	@Test
	public void Test_booleanSize(){
		Assert.assertEquals(5, lpn.getBooleans().size());
		
	}
	
	@Test
	public void Test_booleans(){
		Map<String, String> expected_bool = new HashMap<String, String>();
		expected_bool.put("bit0", "false");
		expected_bool.put("bit1", "false");
		expected_bool.put("parity0", "false");
		expected_bool.put("parity1", "false");
		expected_bool.put("ez_instance__state", "false");
		
		Assert.assertTrue(expected_bool.keySet().equals(lpn.getBooleans().keySet()));
		Assert.assertTrue(expected_bool.equals(lpn.getBooleans()));
	}

	@Test
	public void Test_TransitionSize() {
		Assert.assertEquals(52, lpn.getAllTransitions().length);
	}

}
