package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.IOException;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;

import VerilogConstructs.VerilogAssignment;
import VerilogConstructs.VerilogModule;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;


/**
 * 
 * @author Tramy Nguyen
 *
 */
public class VerilogParserExample11_Test {

	private static VerilogModule verilog_imp;
	
	@BeforeClass
	public static void setupTest() throws ParseException, SBOLValidationException, VerilogCompilerException, XMLStreamException, IOException, BioSimException, org.apache.commons.cli.ParseException, SBOLConversionException {
		
		String[] cmd = {"-v", CompilerTestSuite.verilogCont_file};
		VerilogCompiler compiledVerilog = CompilerTestSuite.testEnv.runCompiler(cmd); 
		Map<String, VerilogModule> moduleList = compiledVerilog.getVerilogModules();
		Assert.assertEquals(1, moduleList.size());
		
		verilog_imp = moduleList.get("contAssign");
		Assert.assertNotNull(verilog_imp);
	}
	
	@Test
	public void Test_contAssign() {
		VerilogAssignment actual_assign = verilog_imp.getContinuousAssignment(0);
		Assert.assertEquals("t", actual_assign.getVariable());
		Assert.assertEquals("not(parity0)", actual_assign.getExpression());
	}
	
	
}