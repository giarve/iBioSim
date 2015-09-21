package analysis.dynamicsim.hierarchical.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;

import analysis.dynamicsim.hierarchical.simulator.HierarchicalObjects.ModelState;
import analysis.dynamicsim.hierarchical.states.DocumentState;
import analysis.dynamicsim.hierarchical.util.arrays.ArraysObject;
import analysis.dynamicsim.hierarchical.util.arrays.ArraysPair;
import analysis.dynamicsim.hierarchical.util.arrays.DimensionObject;
import analysis.dynamicsim.hierarchical.util.arrays.IndexObject;
import analysis.dynamicsim.hierarchical.util.comp.HierarchicalEventToFire;
import analysis.dynamicsim.hierarchical.util.comp.HierarchicalStringDoublePair;
import biomodel.network.GeneticNetwork;
import biomodel.parser.BioModel;
import biomodel.parser.GCMParser;

public class HierarchicalUtilities
{

	public static int flattenedSize(ModelState model, String variable, Map<String, Double> replacements)
	{
		int size = 1;
		List<ArraysPair> pairs = model.getArrays().get(variable);

		if (pairs.size() > 1)
		{
			System.out.println("Something weird is happening");
		}
		DimensionObject dim = pairs.get(0).getDim();

		if (dim != null)
		{
			List<ArraysObject> objects = dim.getDimensions();
			for (int i = 0; i < objects.size(); i++)
			{
				String param = objects.get(i).getSize();
				int value = (int) model.getVariableToValue(replacements, param);
				size = size * value;
			}
		}
		return size;
	}

	public static int flattenedIndex(ModelState model, String variable, int[] indices, Map<String, Double> replacements)
	{

		int index = 0;

		List<ArraysPair> pairs = model.getArrays().get(variable);

		DimensionObject dim = pairs.get(0).getDim();

		if (dim != null)
		{
			List<ArraysObject> objects = dim.getDimensions();

			for (int i = 0; i < objects.size(); i++)
			{
				String param = objects.get(i).getSize();
				int value = (int) model.getVariableToValue(replacements, param);
				int arrayDim = objects.get(i).getArrayDim();

				for (int j = arrayDim + 1; j < indices.length; j++)
				{
					indices[j] = indices[j] * value;
				}
			}

			for (int j = 0; j < indices.length; j++)
			{
				index += indices[j];
			}
		}

		return index;
	}

	public static String getVariableFromArray(String variable)
	{
		if (variable.contains("["))
		{
			int index = variable.indexOf("[");
			variable = variable.substring(0, index);
		}
		return variable;
	}

	public static int[] getIndicesFromVariable(String variable)
	{
		int[] indices = null;
		if (variable.contains("["))
		{
			String[] split = variable.split("\\[");
			indices = new int[split.length - 1];
			int n = indices.length;
			for (int i = 0; i < indices.length; i++)
			{
				indices[n - 1 - i] = Integer.valueOf(split[i + 1].replace("]", ""));
			}
		}
		return indices;
	}

	public static void alterLocalParameter(ASTNode node, String oldString, String newString)
	{
		// String reactionID = reaction.getId();
		if (node.isName() && node.getName().equals(oldString))
		{
			node.setVariable(null);
			node.setName(newString);
		}
		else
		{
			ASTNode childNode;
			for (int i = 0; i < node.getChildCount(); i++)
			{
				childNode = node.getChild(i);
				alterLocalParameter(childNode, oldString, newString);
			}
		}
	}

	public static void updatePropensity(ModelState model, String affectedReactionID, Set<HierarchicalStringDoublePair> reactantStoichiometrySet,
			Set<String> affectedSpecies, double currentTime, Map<String, Double> replacements)
	{

		Set<HierarchicalStringDoublePair> reactionToSpecies = model.getReactionToSpeciesAndStoichiometrySetMap().get(affectedReactionID);

		if (model.getReactionToFormulaMap().get(affectedReactionID) == null)
		{
			model.getReactionToPropensityMap().put(affectedReactionID, 0.0);
			return;
		}

		boolean notEnoughMoleculesFlag = false;

		for (HierarchicalStringDoublePair speciesAndStoichiometry : reactantStoichiometrySet)
		{
			String speciesID = speciesAndStoichiometry.string;

			double stoichiometry = speciesAndStoichiometry.doub;

			if (model.getVariableToValue(replacements, speciesID) < stoichiometry)
			{
				notEnoughMoleculesFlag = true;
				break;
			}
		}

		for (HierarchicalStringDoublePair speciesPair : reactionToSpecies)
		{
			affectedSpecies.add(speciesPair.string);
		}

		double newPropensity = 0.0;
		if (notEnoughMoleculesFlag == false)
		{
			newPropensity = Evaluator.evaluateExpressionRecursive(model, model.getReactionToFormulaMap().get(affectedReactionID), false, currentTime,
					null, null, replacements);
		}

		double oldPropensity = model.getReactionToPropensityMap().get(affectedReactionID);
		model.setPropensity(model.getPropensity() + newPropensity - oldPropensity);
		model.updateReactionToPropensityMap(affectedReactionID, newPropensity);

	}

	public static void updatePropensity(ModelState modelstate, String species, double currentTime, Map<String, Double> replacements)
	{
		// ModelState model = getModelState(modelstate);
		Set<String> reactions = modelstate.getSpeciesToAffectedReactionSetMap().get(species);
		updatePropensities(reactions, modelstate, currentTime, replacements);

	}

	public static Set<String> updatePropensities(Set<String> affectedReactionSet, ModelState modelstate, double currentTime,
			Map<String, Double> replacements)
	{

		Set<String> affectedSpecies = new HashSet<String>();

		for (String affectedReactionID : affectedReactionSet)
		{

			if (modelstate.isDeletedByMetaID(affectedReactionID))
			{
				continue;
			}

			int[] dims = HierarchicalUtilities.getIndicesFromVariable(affectedReactionID);

			if (modelstate.isArrayedObject(affectedReactionID))
			{
				continue;
			}
			else if (dims != null)
			{
				String reaction = HierarchicalUtilities.getVariableFromArray(affectedReactionID);

				updatePropensity(modelstate, reaction, currentTime, dims, replacements);

				dims = null;
			}
			else
			{
				Set<HierarchicalStringDoublePair> reactantStoichiometrySet = modelstate.getReactionToReactantStoichiometrySetMap().get(
						affectedReactionID);
				updatePropensity(modelstate, affectedReactionID, reactantStoichiometrySet, affectedSpecies, currentTime, replacements);
			}
		}

		return affectedSpecies;
	}

	public static List<String> getIndexedSpeciesReference(ModelState modelstate, String reaction, String type, String species, int[] reactionIndices,
			double time, Map<String, Double> replacements)
	{

		List<String> listOfReferences = new ArrayList<String>();

		List<ArraysPair> pairs = modelstate.getArrays().get(reaction + "__" + type + "__" + species);

		if (pairs == null)
		{
			listOfReferences.add(species);
			return listOfReferences;
		}

		for (ArraysPair pair : pairs)
		{

			IndexObject index = pair.getIndex();

			String id = species;
			Map<Integer, ASTNode> speciesAttribute = index.getAttributes().get("species");

			Map<String, Integer> dimensionIdMap = new HashMap<String, Integer>();

			for (int i = 0; i < reactionIndices.length; i++)
			{
				dimensionIdMap.put("d" + i, reactionIndices[i]);
			}

			for (int i = speciesAttribute.size() - 1; i >= 0; i--)
			{
				int ind = (int) Evaluator.evaluateExpressionRecursive(modelstate, speciesAttribute.get(i), false, time, null, dimensionIdMap,
						replacements);
				id = id + "[" + ind + "]";

			}

			listOfReferences.add(id);
		}
		return listOfReferences;

	}

	public static void updatePropensity(ModelState modelstate, String id, double time, int[] indices, Map<String, Double> replacements)
	{
		String arrayedId = HierarchicalUtilities.getArrayedID(modelstate, id, indices);

		Set<HierarchicalStringDoublePair> reactantStoichiometrySet = modelstate.getReactionToReactantStoichiometrySetMap().get(id);

		modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(id);

		if (modelstate.getReactionToFormulaMap().get(id) == null)
		{
			return;
		}

		boolean notEnoughMoleculesFlag = false;
		String type = "reactant";
		for (HierarchicalStringDoublePair speciesAndStoichiometry : reactantStoichiometrySet)
		{

			List<String> speciesIDs = getIndexedSpeciesReference(modelstate, id, type, speciesAndStoichiometry.string, indices, time, replacements);

			for (String speciesID : speciesIDs)
			{
				double stoichiometry = speciesAndStoichiometry.doub;

				if (modelstate.getVariableToValue(replacements, speciesID) < stoichiometry)
				{
					notEnoughMoleculesFlag = true;
					break;
				}
			}
		}

		double newPropensity = 0.0;

		double oldPropensity = modelstate.getPropensity(arrayedId);

		if (notEnoughMoleculesFlag == false)
		{
			Map<String, Integer> dimensionIdMap = new HashMap<String, Integer>();

			for (int i = 0; i < indices.length; i++)
			{
				dimensionIdMap.put("d" + i, indices[i]);
			}
			newPropensity = Evaluator.evaluateExpressionRecursive(modelstate, modelstate.getReactionToFormulaMap().get(id), false, time, null,
					dimensionIdMap, replacements);
		}
		modelstate.setPropensity(modelstate.getPropensity() + newPropensity - oldPropensity);

		if (newPropensity > 0)
		{
			modelstate.updateReactionToPropensityMap(arrayedId, newPropensity);
		}
		else
		{
			modelstate.getReactionToPropensityMap().remove(arrayedId);
		}
	}

	public static boolean checkGrid(Model model)
	{
		if (model.getCompartment("Grid") != null)
		{
			return true;
		}
		return false;
	}

	/**
	 * fires events
	 * 
	 * @param noAssignmentRulesFlag
	 * @param noConstraintsFlag
	 * @param currentTime
	 *            TODO
	 * @param replacements
	 *            TODO
	 */
	public static HashSet<String> fireEvents(ModelState modelstate, Selector selector, final boolean noAssignmentRulesFlag,
			final boolean noConstraintsFlag, double currentTime, Map<String, Double> replacements)
	{

		HashSet<String> untriggeredEvents = new HashSet<String>();
		HashSet<String> variableInFiredEvents = new HashSet<String>();
		HashSet<String> affectedReactionSet = new HashSet<String>();
		HashSet<String> affectedAssignmentRuleSet = new HashSet<String>();
		HashSet<ASTNode> affectedConstraintSet = new HashSet<ASTNode>();

		checkTriggeredEvents(modelstate, untriggeredEvents, currentTime, replacements);
		updatePreviousTriggerValue(modelstate, currentTime, replacements);

		while (modelstate.getTriggeredEventQueue().size() > 0 && modelstate.getTriggeredEventQueue().peek().getFireTime() <= currentTime)
		{
			fireSingleEvent(modelstate, affectedAssignmentRuleSet, affectedConstraintSet, affectedReactionSet, variableInFiredEvents,
					untriggeredEvents, noAssignmentRulesFlag, noConstraintsFlag, currentTime, replacements);
			variableInFiredEvents.addAll(performAssignmentRules(modelstate, affectedAssignmentRuleSet, replacements, currentTime));
			handleEvents(modelstate, currentTime, replacements);
		}

		if (selector == Selector.VARIABLE)
		{
			return variableInFiredEvents;
		}

		return affectedReactionSet;
	}

	public static void getAllASTNodeChildren(ASTNode node, ArrayList<ASTNode> nodeChildrenList)
	{

		ASTNode child;
		long size = node.getChildCount();

		for (int i = 0; i < size; i++)
		{
			child = node.getChild(i);
			if (child.getChildCount() == 0)
			{
				nodeChildrenList.add(child);
			}
			else
			{
				nodeChildrenList.add(child);
				getAllASTNodeChildren(child, nodeChildrenList);
			}
		}
	}

	public static String getArrayedID(ModelState modelstate, String id, int[] indices)
	{
		for (int i = indices.length - 1; i >= 0; i--)
		{
			id = id + "[" + indices[i] + "]";
		}
		return id;
	}

	public static boolean getBooleanFromDouble(double value)
	{

		if (value == 0.0)
		{
			return false;
		}
		return true;
	}

	public static double getDoubleFromBoolean(boolean value)
	{

		if (value == true)
		{
			return 1.0;
		}
		return 0.0;
	}

	public static SBMLDocument getFlattenedRegulations(String path, String filename)
	{
		BioModel biomodel = new BioModel(path);
		biomodel.load(filename);
		GCMParser parser = new GCMParser(biomodel);
		GeneticNetwork network = parser.buildNetwork(biomodel.getSBMLDocument());
		SBMLDocument sbml = network.getSBML();
		return network.mergeSBML(filename, sbml);
	}

	public static int getPercentage(int totalRuns, int currentRun, double currentTime, double timeLimit)
	{

		if (totalRuns == 1)
		{
			double timePerc = currentTime / timeLimit;
			return (int) (timePerc * 100);
		}
		else
		{
			double runPerc = 1.0 * currentRun / totalRuns;
			return (int) (runPerc * 100);
		}
	}

	/**
	 * performs every rate rule using the current time step
	 * 
	 * @param delta_t
	 * @return
	 */
	public static void performEulerRateRules(ModelState modelstate, double currentTime, double delta_t, Map<String, Double> replacements)
	{
		String referencedVariable;
		for (String variable : modelstate.getRateRulesList().keySet())
		{
			ASTNode rateRule = modelstate.getRateRulesList().get(variable);
			referencedVariable = HierarchicalUtilities.getReferencedVariable(variable);

			if (!modelstate.isConstant(referencedVariable))
			{

				if (modelstate.getSpeciesToHasOnlySubstanceUnitsMap().containsKey(referencedVariable)
						&& modelstate.getSpeciesToHasOnlySubstanceUnitsMap().get(referencedVariable) == false)
				{
					double currVal = modelstate.getVariableToValue(replacements, variable);
					double incr = delta_t
							* (Evaluator.evaluateExpressionRecursive(modelstate, rateRule, false, currentTime, null, null, replacements) * modelstate
									.getVariableToValue(replacements, modelstate.getSpeciesToCompartmentNameMap().get(variable)));
					modelstate.setVariableToValue(replacements, variable, currVal + incr);
				}
				else
				{
					double currVal = modelstate.getVariableToValue(replacements, variable);
					double incr = delta_t * Evaluator.evaluateExpressionRecursive(modelstate, rateRule, false, currentTime, null, null, replacements);

					modelstate.setVariableToValue(replacements, variable, currVal + incr);
				}
			}
		}
	}

	public static List<String> getIndexedObject(ModelState modelstate, String id, String variable, String prefix, String attribute, int[] indices,
			Map<String, Double> replacements)
	{

		Map<String, Integer> dimensionIdMap = new HashMap<String, Integer>();

		List<ArraysPair> listOfPairs = modelstate.getArrays().get(id);

		if (listOfPairs == null)
		{
			return null;
		}

		List<String> listOfObjects = new ArrayList<String>();

		for (ArraysPair pair : listOfPairs)
		{
			IndexObject index = pair.getIndex();

			if (index == null)
			{
				continue;
			}

			Map<Integer, ASTNode> indexMap = index.getAttributes().get(attribute);

			int[] newIndices = new int[indices.length];

			for (int i = 0; i < indices.length; i++)
			{
				dimensionIdMap.put(prefix + i, indices[i]);
			}

			for (int i = 0; i < newIndices.length; i++)
			{
				newIndices[i] = (int) Evaluator
						.evaluateExpressionRecursive(modelstate, indexMap.get(i), false, 0, null, dimensionIdMap, replacements);
			}

			listOfObjects.add(getArrayedID(modelstate, variable, newIndices));

			newIndices = null;
		}

		return listOfObjects;
	}

	public static double handleEvents(double currentTime, Map<String, Double> replacements, ModelState topmodel, Map<String, ModelState> submodels)
	{
		double nextEventTime = Double.POSITIVE_INFINITY;
		if (!topmodel.isNoEventsFlag())
		{
			handleEvents(topmodel, currentTime, replacements);
			if (!topmodel.getTriggeredEventQueue().isEmpty() && topmodel.getTriggeredEventQueue().peek().getFireTime() <= nextEventTime)
			{
				if (topmodel.getTriggeredEventQueue().peek().getFireTime() < nextEventTime)
				{
					nextEventTime = topmodel.getTriggeredEventQueue().peek().getFireTime();
				}
			}
		}

		for (ModelState models : submodels.values())
		{
			if (!models.isNoEventsFlag())
			{
				handleEvents(models, currentTime, replacements);
				if (!models.getTriggeredEventQueue().isEmpty() && models.getTriggeredEventQueue().peek().getFireTime() <= nextEventTime)
				{
					if (models.getTriggeredEventQueue().peek().getFireTime() < nextEventTime)
					{
						nextEventTime = models.getTriggeredEventQueue().peek().getFireTime();
					}
				}
			}
		}
		return nextEventTime;
	}

	/**
	 * inlines a formula with function definitions
	 * 
	 * @param formula
	 * @return
	 */
	public static ASTNode inlineFormula(DocumentState state, ASTNode formula, Map<String, Model> models, Set<String> ibiosimFunctionDefinitions)
	{
		// TODO: Avoid calling this method
		if (formula.isFunction() == false || formula.isLeaf() == false)
		{

			for (int i = 0; i < formula.getChildCount(); ++i)
			{
				formula.replaceChild(i, inlineFormula(state, formula.getChild(i), models, ibiosimFunctionDefinitions));// .clone()));
			}
		}

		if (formula.isFunction() && models.get(state.getModel()).getFunctionDefinition(formula.getName()) != null)
		{

			if (ibiosimFunctionDefinitions != null && ibiosimFunctionDefinitions.contains(formula.getName()))
			{
				return formula;
			}

			ASTNode inlinedFormula = models.get(state.getModel()).getFunctionDefinition(formula.getName()).getBody().clone();

			ASTNode oldFormula = formula.clone();

			ArrayList<ASTNode> inlinedChildren = new ArrayList<ASTNode>();
			HierarchicalUtilities.getAllASTNodeChildren(inlinedFormula, inlinedChildren);

			if (inlinedChildren.size() == 0)
			{
				inlinedChildren.add(inlinedFormula);
			}

			Map<String, Integer> inlinedChildToOldIndexMap = new HashMap<String, Integer>();

			for (int i = 0; i < models.get(state.getModel()).getFunctionDefinition(formula.getName()).getArgumentCount(); ++i)
			{
				inlinedChildToOldIndexMap.put(models.get(state.getModel()).getFunctionDefinition(formula.getName()).getArgument(i).getName(), i);
			}

			for (int i = 0; i < inlinedChildren.size(); ++i)
			{

				ASTNode child = inlinedChildren.get(i);
				if ((child.getChildCount() == 0) && child.isName())
				{

					int index = inlinedChildToOldIndexMap.get(child.getName());
					HierarchicalUtilities.replaceArgument(inlinedFormula, child.toFormula(), oldFormula.getChild(index));

					if (inlinedFormula.getChildCount() == 0)
					{
						inlinedFormula = oldFormula.getChild(index);
					}
				}
			}

			return inlinedFormula;
		}
		return formula;
	}

	public static HashSet<String> performAssignmentRules(ModelState modelstate, Set<String> affectedAssignmentRuleSet,
			Map<String, Double> replacements, double currentTime)
	{

		HashSet<String> affectedVariables = new HashSet<String>();

		for (String variable : affectedAssignmentRuleSet)
		{

			if (!modelstate.isConstant(variable))
			{

				updateVariableValue(modelstate, variable, modelstate.getAssignmentRulesList().get(variable), replacements, currentTime);

				affectedVariables.add(variable);
			}
		}

		return affectedVariables;
	}

	public static void performAssignmentRules(ModelState modelstate, Map<String, Double> replacements, double currentTime)
	{
		boolean changed = true;
		while (changed)
		{
			changed = false;

			for (String variable : modelstate.getAssignmentRulesList().keySet())
			{
				ASTNode assignmentRule = modelstate.getAssignmentRulesList().get(variable);
				if (!modelstate.isConstant(variable))
				{
					changed |= updateVariableValue(modelstate, variable, assignmentRule, replacements, currentTime);
				}
			}
		}
	}

	public static void replaceArgument(ASTNode formula, String bvar, ASTNode arg)
	{
		for (int i = 0; i < formula.getChildCount(); i++)
		{
			ASTNode child = formula.getChild(i);
			if (child.isString() && child.getName().equals(bvar))
			{
				formula.replaceChild(i, arg.clone());
			}
			else if (child.getChildCount() > 0)
			{
				replaceArgument(child, bvar, arg);
			}
		}
	}

	public static void replaceArgument(ASTNode formula, String bvar, int arg)
	{
		if (formula.isString() && formula.getName().equals(bvar))
		{
			formula.setValue(arg);
		}
		for (int i = 0; i < formula.getChildCount(); i++)
		{
			ASTNode child = formula.getChild(i);

			replaceArgument(child, bvar, arg);

		}
	}

	public static void replaceDimensionIds(ASTNode math, String prefix, int[] indices)
	{
		for (int i = 0; i < indices.length; i++)
		{
			HierarchicalUtilities.replaceArgument(math, prefix + i, indices[i]);
		}
	}

	public static String getReferencedVariable(String variable)
	{
		if (variable.contains("["))
		{
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < variable.length(); i++)
			{
				if (variable.charAt(i) == '[')
				{
					break;
				}
				builder.append(variable.charAt(i));
			}
			return builder.toString();
		}
		return variable;
	}

	public static void replaceSelector(ModelState modelstate, Map<String, Double> replacements, ASTNode formula)
	{
		if (formula.getType() == ASTNode.Type.FUNCTION_SELECTOR)
		{
			if (formula.getChild(0).isName())
			{
				int[] indices = new int[formula.getChildCount() - 1];
				String id = formula.getChild(0).getName();

				for (int i = 1; i < formula.getChildCount() - 1; i++)
				{
					indices[i - 1] = (int) Evaluator.evaluateExpressionRecursive(modelstate, formula.getChild(i), false, 0, null, null, replacements);
				}

				String newId = getArrayedID(modelstate, id, indices);

				formula.setType(ASTNode.Type.NAME);
				formula.setName(newId);
				indices = null;
			}
		}
		else
		{
			for (int i = 0; i < formula.getChildCount(); i++)
			{
				replaceSelector(modelstate, replacements, formula.getChild(i));
			}
		}
	}

	public static boolean testConstraints(ModelState modelstate, HashSet<ASTNode> affectedConstraintSet, double currentTime,
			Map<String, Double> replacements)
	{
		for (ASTNode constraint : affectedConstraintSet)
		{
			if (HierarchicalUtilities.getBooleanFromDouble(Evaluator.evaluateExpressionRecursive(modelstate, constraint, false, currentTime, null,
					null, replacements)))
			{
				return true;
			}
		}

		return false;
	}

	public static boolean updateVariableValue(ModelState modelstate, String variable, ASTNode math, Map<String, Double> replacements,
			double currentTime)
	{

		boolean changed = false;
		String referencedVariable = HierarchicalUtilities.getReferencedVariable(variable);

		if (modelstate.getSpeciesToHasOnlySubstanceUnitsMap().containsKey(referencedVariable)
				&& !modelstate.getSpeciesToHasOnlySubstanceUnitsMap().get(referencedVariable))
		{
			double compartment = modelstate.getVariableToValue(replacements, modelstate.getSpeciesToCompartmentNameMap().get(variable));

			double oldValue = modelstate.getVariableToValue(replacements, variable);
			double newValue = Evaluator.evaluateExpressionRecursive(modelstate, math, false, currentTime, null, null, replacements);

			// TODO: is this correct?
			if (oldValue != newValue)
			{
				changed = true;
				modelstate.setVariableToValue(replacements, variable, newValue * compartment);
			}
		}
		else
		{
			double oldValue = modelstate.getVariableToValue(replacements, variable);
			double newValue = Evaluator.evaluateExpressionRecursive(modelstate, math, false, currentTime, null, null, replacements);

			if (oldValue != newValue)
			{
				changed = true;
				modelstate.setVariableToValue(replacements, variable,
						Evaluator.evaluateExpressionRecursive(modelstate, math, false, currentTime, null, null, replacements));
			}
		}

		return changed;
	}

	private static void checkTriggeredEvents(ModelState modelstate, Set<String> untriggeredEvents, double currentTime,
			Map<String, Double> replacements)
	{
		for (HierarchicalEventToFire triggeredEvent : modelstate.getTriggeredEventQueue())
		{
			String triggeredEventID = triggeredEvent.getEventID();

			if (!modelstate.getEventToTriggerPersistenceMap().get(triggeredEventID)
					&& !HierarchicalUtilities.getBooleanFromDouble(Evaluator.evaluateExpressionRecursive(modelstate, modelstate
							.getEventToTriggerMap().get(triggeredEventID), false, currentTime, null, null, replacements)))
			{

				untriggeredEvents.add(triggeredEventID);
				modelstate.getEventToPreviousTriggerValueMap().put(triggeredEventID, false);
			}

			if (modelstate.getEventToTriggerPersistenceMap().get(triggeredEventID)
					&& !HierarchicalUtilities.getBooleanFromDouble(Evaluator.evaluateExpressionRecursive(modelstate, modelstate
							.getEventToTriggerMap().get(triggeredEventID), false, currentTime, null, null, replacements)))
			{
				modelstate.getUntriggeredEventSet().add(triggeredEventID);
			}
		}
	}

	public static boolean isEventTriggered(ModelState modelstate, String event, double t, double[] y, boolean state,
			Map<String, Integer> variableToIndexMap, Map<String, Double> replacements)
	{
		double givenState = Evaluator.evaluateExpressionRecursive(modelstate, modelstate.getEventToTriggerMap().get(event), state, t, y,
				variableToIndexMap, replacements);

		if (givenState > 0)
		{
			return true;
		}

		return false;
	}

	private static HierarchicalEventToFire selectRandomEvent(ModelState modelstate, HierarchicalEventToFire eventToFire)
	{

		HierarchicalEventToFire nextToFire = modelstate.getTriggeredEventQueue().peek();

		List<HierarchicalEventToFire> queue = new ArrayList<HierarchicalEventToFire>();

		queue.add(eventToFire);

		while (nextToFire != null)
		{
			if (nextToFire.getFireTime() == eventToFire.getFireTime() && nextToFire.getPriority() == eventToFire.getPriority())
			{
				nextToFire = modelstate.getTriggeredEventQueue().poll();
				queue.add(nextToFire);
				nextToFire = modelstate.getTriggeredEventQueue().peek();
			}
			else
			{
				nextToFire = null;
			}
		}
		double rand = Math.random();
		int select = (int) (rand * queue.size());

		for (int i = 0; i < queue.size(); i++)
		{
			if (select == i)
			{
				nextToFire = queue.get(i);
			}
			else
			{
				modelstate.getTriggeredEventQueue().add(queue.get(i));
			}
		}

		return nextToFire;

	}

	private static void fireSingleEvent(ModelState modelstate, Set<String> affectedAssignmentRuleSet, Set<ASTNode> affectedConstraintSet,
			Set<String> affectedReactionSet, Set<String> variableInFiredEvents, Set<String> untriggeredEvents, final boolean noAssignmentRulesFlag,
			final boolean noConstraintsFlag, double currentTime, Map<String, Double> replacements)
	{

		HierarchicalEventToFire eventToFire = modelstate.getTriggeredEventQueue().poll();

		eventToFire = selectRandomEvent(modelstate, eventToFire);

		String eventToFireID = eventToFire.getEventID();

		modelstate.getUntriggeredEventSet().add(eventToFireID);

		modelstate.addEventToPreviousTriggerValueMap(
				eventToFireID,
				HierarchicalUtilities.getBooleanFromDouble(Evaluator.evaluateExpressionRecursive(modelstate,
						modelstate.getEventToTriggerMap().get(eventToFireID), false, currentTime, null, null, replacements)));

		if (!modelstate.getEventToTriggerPersistenceMap().get(eventToFireID))
		{
			boolean state = isEventTriggered(modelstate, eventToFireID, currentTime, null, false, null, replacements);

			if (!state)
			{
				return;
			}

		}

		Map<String, Double> assignments = new HashMap<String, Double>();

		if (modelstate.getEventToAffectedReactionSetMap().get(eventToFireID) != null)
		{
			affectedReactionSet.addAll(modelstate.getEventToAffectedReactionSetMap().get(eventToFireID));
		}

		for (String variable : modelstate.getEventToAssignmentSetMap().get(eventToFireID).keySet())
		{
			double assignmentValue;

			if (modelstate.getEventToUseValuesFromTriggerTimeMap().get(eventToFireID))
			{
				assignmentValue = eventToFire.getEventAssignmentSet().get(variable);
			}
			else
			{
				ASTNode math = modelstate.getEventToAssignmentSetMap().get(eventToFireID).get(variable);
				assignmentValue = Evaluator.evaluateExpressionRecursive(modelstate, math, false, currentTime, null, null, replacements);
			}

			variableInFiredEvents.add(variable);

			if (modelstate.isConstant(variable) == false)
			{
				assignments.put(variable, assignmentValue);
			}

			if (noAssignmentRulesFlag == false && modelstate.getVariableToIsInAssignmentRuleMap().get(variable) == true)
			{
				affectedAssignmentRuleSet.addAll(modelstate.getVariableToAffectedAssignmentRuleSetMap().get(variable));
			}
			if (noConstraintsFlag == false && modelstate.getVariableToIsInConstraintMap().get(variable) == true)
			{
				affectedConstraintSet.addAll(modelstate.getVariableToAffectedConstraintSetMap().get(variable));
			}

		}

		for (String id : assignments.keySet())
		{
			modelstate.setVariableToValue(replacements, id, assignments.get(id));
		}

		for (HierarchicalEventToFire triggeredEvent : modelstate.getTriggeredEventQueue())
		{

			String triggeredEventID = triggeredEvent.getEventID();

			if (!modelstate.getEventToTriggerPersistenceMap().get(triggeredEventID)
					&& !HierarchicalUtilities.getBooleanFromDouble(Evaluator.evaluateExpressionRecursive(modelstate, modelstate
							.getEventToTriggerMap().get(triggeredEventID), false, currentTime, null, null, replacements)))
			{

				untriggeredEvents.add(triggeredEventID);
				modelstate.getEventToPreviousTriggerValueMap().put(triggeredEventID, false);
			}

			if (modelstate.getEventToTriggerPersistenceMap().get(triggeredEventID)
					&& !HierarchicalUtilities.getBooleanFromDouble(Evaluator.evaluateExpressionRecursive(modelstate, modelstate
							.getEventToTriggerMap().get(triggeredEventID), false, currentTime, null, null, replacements)))
			{
				modelstate.getUntriggeredEventSet().add(triggeredEventID);
			}
		}
	}

	private static void handleEvents(ModelState modelstate, double currentTime, Map<String, Double> replacements)
	{
		for (String untriggeredEventID : modelstate.getUntriggeredEventSet())
		{

			boolean eventTrigger = HierarchicalUtilities.getBooleanFromDouble(Evaluator.evaluateExpressionRecursive(modelstate, modelstate
					.getEventToTriggerMap().get(untriggeredEventID), false, currentTime, null, null, replacements));

			if (eventTrigger)
			{

				if (currentTime == 0.0 && modelstate.getEventToTriggerInitiallyTrueMap().get(untriggeredEventID))
				{
					continue;
				}

				if (modelstate.getEventToPreviousTriggerValueMap().get(untriggeredEventID))
				{
					continue;
				}

				if (modelstate.getEventToUseValuesFromTriggerTimeMap().get(untriggeredEventID))
				{
					handleEventsValueAtTrigger(modelstate, untriggeredEventID, currentTime, replacements);
				}
				else
				{
					handleEventsValueAtFire(modelstate, untriggeredEventID, currentTime, replacements);
				}
			}
			modelstate.getEventToPreviousTriggerValueMap().put(untriggeredEventID, eventTrigger);
		}

	}

	private static void handleEventsValueAtFire(ModelState modelstate, String untriggeredEventID, double currentTime, Map<String, Double> replacements)
	{
		double fireTime = currentTime;

		if (modelstate.hasDelay(untriggeredEventID))
		{
			fireTime += Evaluator.evaluateExpressionRecursive(modelstate, modelstate.getEventToDelayMap().get(untriggeredEventID), false,
					currentTime, null, null, replacements);
		}

		double priority;

		if (modelstate.getEventToPriorityMap().containsKey(untriggeredEventID))
		{
			priority = Evaluator.evaluateExpressionRecursive(modelstate, modelstate.getEventToPriorityMap().get(untriggeredEventID), false,
					currentTime, null, null, replacements);
		}
		else
		{
			priority = Double.NEGATIVE_INFINITY;
		}

		modelstate.getTriggeredEventQueue().add(new HierarchicalEventToFire(modelstate.getID(), untriggeredEventID, null, fireTime, priority));
	}

	private static void handleEventsValueAtTrigger(ModelState modelstate, String untriggeredEventID, double currentTime,
			Map<String, Double> replacements)
	{
		Map<String, Double> evaluatedAssignments = new HashMap<String, Double>();

		for (String variable : modelstate.getEventToAssignmentSetMap().get(untriggeredEventID).keySet())
		{

			ASTNode math = modelstate.getEventToAssignmentSetMap().get(untriggeredEventID).get(variable);
			double value = Evaluator.evaluateExpressionRecursive(modelstate, math, false, currentTime, null, null, replacements);
			evaluatedAssignments.put(variable, value);
		}

		double fireTime = currentTime;

		if (modelstate.hasDelay(untriggeredEventID))
		{
			fireTime += Evaluator.evaluateExpressionRecursive(modelstate, modelstate.getEventToDelayMap().get(untriggeredEventID), false,
					currentTime, null, null, replacements);
		}

		double priority;

		if (modelstate.getEventToPriorityMap().containsKey(untriggeredEventID))
		{
			priority = Evaluator.evaluateExpressionRecursive(modelstate, modelstate.getEventToPriorityMap().get(untriggeredEventID), false,
					currentTime, null, null, replacements);
		}
		else
		{
			priority = Double.NEGATIVE_INFINITY;
		}

		modelstate.getTriggeredEventQueue().add(
				new HierarchicalEventToFire(modelstate.getID(), untriggeredEventID, evaluatedAssignments, fireTime, priority));
	}

	private static void updatePreviousTriggerValue(ModelState modelstate, double currentTime, Map<String, Double> replacements)
	{
		for (String untriggeredEventID : modelstate.getUntriggeredEventSet())
		{

			if (modelstate.getEventToTriggerPersistenceMap().get(untriggeredEventID) == false
					&& HierarchicalUtilities.getBooleanFromDouble(Evaluator.evaluateExpressionRecursive(modelstate, modelstate.getEventToTriggerMap()
							.get(untriggeredEventID), false, currentTime, null, null, replacements)) == false)
			{
				modelstate.getEventToPreviousTriggerValueMap().put(untriggeredEventID, false);
			}
		}
	}

	public static enum Selector
	{
		REACTION, VARIABLE;
	}

}
