package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.Participation;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SequenceOntology;
import org.sbolstandard.core2.SystemsBiologyOntology;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModule;


/**
 * Test !a decomposed to a flat SBOL data model.
 *  
 * @author Tramy Nguyen
 */
public class SBOLExample1_Test {

	private static SBOLDocument sbolDoc;
	private static ModuleDefinition sbolDesign;

	@BeforeClass
	public static void setupTest() throws ParseException, SBOLValidationException, VerilogCompilerException, XMLStreamException, IOException, BioSimException, org.apache.commons.cli.ParseException, SBOLConversionException {
		VerilogParser verilogParser = new VerilogParser();
		VerilogModule verilogModule = verilogParser.parseVerilogFile(new File(TestingFiles.verilogNotDecomp_File));
		VerilogToSBOL sbolConverter = new VerilogToSBOL(true);
		WrappedSBOL sbolWrapper = sbolConverter.convertVerilog2SBOL(verilogModule);
		Assert.assertNotNull(sbolWrapper);
		sbolDoc = sbolWrapper.getSBOLDocument();
		Assert.assertEquals(1, sbolDoc.getModuleDefinitions().size());
		sbolDesign = sbolDoc.getModuleDefinition("MD0" + verilogModule.getModuleId(), "1.0");
	}
	
	@Test
	public void Test_cdSize() {
		Assert.assertEquals(6, sbolDoc.getComponentDefinitions().size());
	}

	@Test
	public void Test_fcSize() {
		Assert.assertEquals(3, sbolDesign.getFunctionalComponents().size());
	}

	@Test
	public void Test_proteinSize() {
		int actualSize = 0; 
		for(ComponentDefinition cd : sbolDoc.getComponentDefinitions()) {
			if(cd.getTypes().iterator().next().equals(ComponentDefinition.PROTEIN)) {
				actualSize++;
			}
		}
		Assert.assertEquals(2, actualSize);
	}

	@Test
	public void Test_dnaSize() {
		int actualSize = 0; 
		for(ComponentDefinition cd : sbolDoc.getComponentDefinitions()) {
			if(cd.getTypes().iterator().next().equals(ComponentDefinition.DNA_REGION)) {
				actualSize++;
			}
		}
		Assert.assertEquals(4, actualSize);
	}

	@Test
	public void Test_interactionSize() {
		Assert.assertEquals(2, sbolDesign.getInteractions().size());
	}

	@Test
	public void Test_inhibitionSize() {
		int actualSize = 0; 
		for(Interaction inter : sbolDesign.getInteractions()) {
			if(inter.getTypes().iterator().next().equals(SystemsBiologyOntology.INHIBITION)) {
				actualSize++;
			}
		}
		Assert.assertEquals(1, actualSize);
	}

	@Test
	public void Test_productionSize() {
		int actualSize = 0; 
		for(Interaction inter : sbolDesign.getInteractions()) {
			if(inter.getTypes().iterator().next().equals(SystemsBiologyOntology.GENETIC_PRODUCTION)) {
				actualSize++;
			}
		}
		Assert.assertEquals(1, actualSize);
	}

	@Test
	public void Test_NOT1() {
		FunctionalComponent gate = sbolDesign.getFunctionalComponent("FC2_notTU");
		Assert.assertNotNull(gate);
		
		FunctionalComponent input = sbolDesign.getFunctionalComponent("FC0_a");
		Assert.assertNotNull(input);
		
		FunctionalComponent output = sbolDesign.getFunctionalComponent("FC1_y");
		Assert.assertNotNull(output);
		
		Interaction inhibition = sbolDesign.getInteraction("I0_Inhib");
		Assert.assertNotNull(inhibition);
		Assert.assertEquals(SystemsBiologyOntology.INHIBITION, inhibition.getTypes().iterator().next());
		Assert.assertEquals(2, inhibition.getParticipations().size());

		for(Participation p : inhibition.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SystemsBiologyOntology.INHIBITOR)) {
				Assert.assertEquals(input, p.getParticipant());
			}
			else if(role.equals(SystemsBiologyOntology.INHIBITED)){
				Assert.assertEquals(gate, p.getParticipant());
			}
		}
	
		Interaction production = sbolDesign.getInteraction("I1_Prod");
		Assert.assertNotNull(production);
		Assert.assertEquals(SystemsBiologyOntology.GENETIC_PRODUCTION, production.getTypes().iterator().next());
		Assert.assertEquals(2, production.getParticipations().size());

		for(Participation p : production.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SequenceOntology.ENGINEERED_REGION)) {
				Assert.assertEquals(gate, p.getParticipant());
			}
			else if(role.equals(SystemsBiologyOntology.PRODUCT)){
				Assert.assertEquals(output, p.getParticipant());
			}
		}
	}


}