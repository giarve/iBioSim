package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;

import VerilogConstructs.VerilogModule;
import VerilogConstructs.VerilogModuleInstance;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class VerilogParserExample3_Test {
	
	private static VerilogModule verilogModule;
	
	@BeforeClass
	public static void setupTest() throws ParseException, SBOLValidationException, VerilogCompilerException, XMLStreamException, IOException, BioSimException, org.apache.commons.cli.ParseException, SBOLConversionException {
		String[] cmd = {"-v", CompilerTestSuite.verilogReg_file};
		VerilogCompiler compiledVerilog = CompilerTestSuite.testEnv.runCompiler(cmd); 
		Map<String, VerilogModule> moduleList = compiledVerilog.getVerilogModules();
		Assert.assertEquals(1, moduleList.size());
		
		verilogModule = moduleList.get("registers");
		Assert.assertNotNull(verilogModule);
	}
	
	@Test
	public void TestVerilog_inputs() {
		List<String> actual_inputPorts = verilogModule.getInputPorts();
		Assert.assertNotNull(actual_inputPorts);
		Assert.assertTrue(actual_inputPorts.size() == 0);
	}
	
	@Test
	public void TestVerilog_outputs() {
		List<String> actual_outputPorts = verilogModule.getOutputPorts();
		Assert.assertNotNull(actual_outputPorts);
		Assert.assertTrue(actual_outputPorts.size() == 0);
	}
	
	@Test
	public void TestVerilog_registers() {
		List<String> actual_registers = verilogModule.getRegisters();
		Assert.assertNotNull(actual_registers);
		Assert.assertTrue(actual_registers.size() == 3);
		Assert.assertEquals("r1", actual_registers.get(0));
		Assert.assertEquals("r2", actual_registers.get(1));
		Assert.assertEquals("r3", actual_registers.get(2));
	}
	
	@Test
	public void TestVerilog_submodules() {
		List<VerilogModuleInstance> actual_submodules = verilogModule.getSubmodules();
		Assert.assertNotNull(actual_submodules);
		Assert.assertTrue(actual_submodules.size() == 0);
	}
	
	@Test
	public void TestVerilog_initialBlocks() {
		Assert.assertEquals(0, verilogModule.getInitialBlockList().size());
	}
	
	@Test
	public void TestVerilog_alwaysBlocks() {
		Assert.assertEquals(0, verilogModule.getAlwaysBlockList().size());
	}
	
	
	
}