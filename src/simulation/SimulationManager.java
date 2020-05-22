package simulation;

import state.StateGraph;
import state.StateNode;
import state.StatePath;

import alloy.AlloyConstants;
import alloy.AlloyInterface;
import alloy.AlloyUtils;
import alloy.ParsingConf;
import alloy.SigData;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.parser.CompModule;
import edu.mit.csail.sdg.translator.A4Solution;
import edu.mit.csail.sdg.translator.A4Tuple;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;

import org.yaml.snakeyaml.error.YAMLException;

public class SimulationManager {
    private File alloyModelFile;
    private String alloyModelString;
    private String alloyInitString;
    private SortedMap<String, List<String>> scopes;
    private ParsingConf persistentParsingConf;  // Set by setconf - used across multiple models.
    private ParsingConf embeddedParsingConf;  // Set by load - used for the current model only.
    private SigData stateSigData;
    private StatePath statePath;
    private StateGraph stateGraph;
    private Stack<A4Solution> activeSolutions;
    private AliasManager aliasManager;
    private ConstraintManager constraintManager;

    private boolean traceMode;

    public SimulationManager() {
        scopes = new TreeMap<>();
        statePath = new StatePath();
        stateGraph = new StateGraph();
        persistentParsingConf = new ParsingConf();
        embeddedParsingConf = null;
        activeSolutions = new Stack<>();
        aliasManager = new AliasManager();
        constraintManager = new ConstraintManager();

        traceMode = false;
    }

    public boolean isTrace() {
        return traceMode;
    }

    /**
     * isInitialized returns True iff a model or trace has been loaded.
     * @return boolean
     */
    public boolean isInitialized() {
        return !statePath.isEmpty();
    }

    public void setParsingConf(ParsingConf conf) {
        persistentParsingConf = conf;
    }

    /**
     * initialize is a wrapper for initializing a model or trace which cleans up internal state if
     * initialization fails.
     * @return boolean
     */
    public boolean initialize(File file, boolean isTrace) {
        ParsingConf oldEmbeddedParsingConf = embeddedParsingConf;

        boolean res = isTrace ? initializeWithTrace(file) : initializeWithModel(file);
        if (!res) {
            // The embedded conf of the new model shouldn't persist if load fails.
            embeddedParsingConf = oldEmbeddedParsingConf;
        }
        return res;
    }

    /**
     * performReverseStep goes backward by `steps` states in the current state traversal path.
     * @param steps
     */
    public void performReverseStep(int steps) {
        int initialPos = statePath.getPosition();
        StateNode targetNode = statePath.getNode(initialPos < steps ? 0 : initialPos - steps);

        if (initialPos <= steps) {
            setToInit();
        } else {
            // To set the internal state properly for an alternate path to be selected, perform
            // a step from the position one step behind the expected final position.
            statePath.decrementPosition(steps + 1, traceMode);
            performStep(1);
        }

        // If the user was on some alternate path, we need to perform `alt` until we get back
        // to the correct StateNode.
        while (!statePath.getCurNode().equals(targetNode)) {
            selectAlternatePath(false);
        }

        // Ensure the ID is set when reverse-stepping back to an alternative initial state.
        statePath.getCurNode().setIdentifier(targetNode.getIdentifier());
    }

    /**
     * performStep steps the transition system forward by `steps` state transitions.
     * @param steps
     * @return boolean
     */
    public boolean performStep(int steps) {
        return performStep(steps, new ArrayList<String>());
    }

    /**
     * performStep steps the transition system forward by `steps` state transitions.
     * The i-th constraint in `constraints` is applied to the i-th transition.
     * @param steps
     * @param constraints
     * @return boolean
     */
    public boolean performStep(int steps, List<String> constraints) {
        if (isTrace()) {
            if (statePath.atEnd()) {
                System.out.println("Cannot perform step. End of trace reached.");
                return false;
            }
            statePath.incrementPosition(steps);
            return true;
        }

        statePath.commitNodes();

        String pathPredicate = AlloyUtils.getPathPredicate(constraints, stateSigData);
        try {
            String curInitString;
            if (stateGraph.size() > 1) {
                curInitString = statePath.getCurNode().getAlloyInitString();
            } else {
                curInitString = alloyInitString;
            }
            AlloyUtils.writeToFile(
                AlloyUtils.annotatedTransitionSystemStep(alloyModelString + curInitString + pathPredicate, getParsingConf(), steps),
                alloyModelFile
            );
        } catch (IOException e) {
            System.out.println("Cannot perform step. I/O failed.");
            return false;
        }

        CompModule compModule = AlloyInterface.compile(alloyModelFile.getAbsolutePath());
        if (compModule == null) {
            System.out.println("Cannot perform step. Could not parse model.");
            return false;
        }

        A4Solution sol = AlloyInterface.run(compModule);
        if (sol == null || !sol.satisfiable()) {
            System.out.println("Cannot perform step. Transition constraint is unsatisfiable.");
            return false;
        }

        StateNode startNode = statePath.getCurNode();

        // For steps > 1, we need to generate all nodes representing the path that the series of state transitions
        // takes within the state graph.
        List<StateNode> stateNodes = getStateNodesForA4Solution(sol);

        // Filter out the initial node to avoid re-adding it to statePath.
        stateNodes.remove(0);
        statePath.setTempPath(stateNodes);

        stateGraph.addNodes(startNode, stateNodes);

        this.activeSolutions.clear();
        this.activeSolutions.push(sol);

        return true;
    }

    public boolean selectAlternatePath(boolean reverse) {
        if (activeSolutions.isEmpty()) {
            return false;
        }

        A4Solution activeSolution = null;
        if (reverse) {
            if (activeSolutions.size() == 1) {
                return false;
            }

            activeSolutions.pop();
            activeSolution = activeSolutions.peek();
        } else {
            activeSolution = activeSolutions.peek();
            if (!activeSolution.next().satisfiable()) {
                return false;
            }

            activeSolution = activeSolution.next();
            activeSolutions.push(activeSolution);
        }

        List<StateNode> stateNodes = getStateNodesForA4Solution(activeSolution);
        StateNode startNode = stateNodes.get(0);
        stateNodes.remove(0);

        statePath.clearTempPath();
        if (stateNodes.isEmpty()) {
            // This branch should only be reached when an alternate path
            // is selected for an initial state.
            statePath.setTempPath(Arrays.asList(startNode));
        } else {
            statePath.setTempPath(stateNodes);
        }

        stateGraph.addNodes(startNode, stateNodes);

        return true;
    }

    /**
     * performUntil steps the transition system up to `limit` state transitions,
     * until at least one of the constraints in the breakpoint list is satisfied.
     * @param limit
     * @return boolean
     */
    public boolean performUntil(int limit) {
        String breakPredicate = AlloyUtils.getBreakPredicate(constraintManager.getConstraints(), stateSigData);

        for (int steps = 1; steps <= limit; steps++) {
            try {
                String curInitString;
                if (stateGraph.size() > 1) {
                    curInitString = statePath.getCurNode().getAlloyInitString();
                } else {
                    curInitString = alloyInitString;
                }
                AlloyUtils.writeToFile(
                    AlloyUtils.annotatedTransitionSystemUntil(alloyModelString + curInitString + breakPredicate, getParsingConf(), steps),
                    alloyModelFile
                );
            } catch (IOException e) {
                return false;
            }

            CompModule compModule = AlloyInterface.compile(alloyModelFile.getAbsolutePath());
            if (compModule == null) {
                return false;
            }

            A4Solution sol = AlloyInterface.run(compModule);
            if (sol == null) {
                return false;
            } else if (!sol.satisfiable()) {
                // Breakpoints not hit for current step size. Try next step size.
                continue;
            }

            statePath.commitNodes();

            StateNode startNode = statePath.getCurNode();

            List<StateNode> stateNodes = getStateNodesForA4Solution(sol);
            stateNodes.remove(0);
            statePath.setTempPath(stateNodes);

            stateGraph.addNodes(startNode, stateNodes);

            this.activeSolutions.clear();
            this.activeSolutions.push(sol);

            return true;
        }

        return false;
    }

    /**
     * setToInit sets SimulationManager's internal state to point to the initial state of the
     * active model or trace.
     * @return boolean
     */
    public boolean setToInit() {
        if (traceMode) {
            statePath.decrementPosition(statePath.getPosition(), traceMode);
            return true;
        }
        try {
            AlloyUtils.writeToFile(
                AlloyUtils.annotatedTransitionSystem(
                    this.alloyModelString + this.alloyInitString,
                    getParsingConf(),
                    0
                ),
                alloyModelFile
            );
        } catch (IOException e) {
            System.out.println("error. I/O failed, cannot re-initialize model.");
            return false;
        }

        CompModule compModule = AlloyInterface.compile(alloyModelFile.getAbsolutePath());
        if (compModule == null) {
            System.out.println("error. Could not parse model.");
            return false;
        }

        A4Solution sol = AlloyInterface.run(compModule);
        List<StateNode> initialNodes = getStateNodesForA4Solution(sol);
        // We don't re-add this initial node to the StateGraph, so manually set its identifier here.
        initialNodes.get(0).setIdentifier(1);

        statePath.clearPath();
        statePath.setTempPath(initialNodes);

        activeSolutions.clear();
        activeSolutions.push(sol);

        return true;
    }

    /**
     * validateConstraint validates a user-entered constraint by transforming
     * the constraint into a predicate and verifying that the model compiles
     * after the introduction of the new predicate.
     * @param String constraint
     * @return boolean
     */
    public boolean validateConstraint(String constraint) {
        String breakPredicate = AlloyUtils.getBreakPredicate(Arrays.asList(constraint), stateSigData);

        try {
            AlloyUtils.writeToFile(
                alloyModelString + alloyInitString + breakPredicate,
                alloyModelFile
            );
        } catch (IOException e) {
            return false;
        }

        CompModule compModule = AlloyInterface.compile(alloyModelFile.getAbsolutePath());
        if (compModule == null) {
            return false;
        }

        return true;
    }

    public String getDOTString() {
        return stateGraph.getDOTString();
    }

    public String getHistory(int n) {
        return statePath.getHistory(n, traceMode);
    }

    public Map<String, List<String>> getScopes() {
        return scopes;
    }

    public List<String> getScopeForSig(String sigName) {
        return scopes.get(sigName);
    }

    public String getCurrentStateString() {
        return statePath.getCurNode().toString();
    }

    public String getCurrentStateStringForProperty(String property) {
        return statePath.getCurNode().stringForProperty(property);
    }

    public String getCurrentStateDiffString() {
        return statePath.getCurNode().getDiffString(statePath.getPrevNode());
    }

    public AliasManager getAliasManager() {
        return aliasManager;
    }

    public ConstraintManager getConstraintManager() {
        return constraintManager;
    }

    public String getWorkingDirPath() {
        return System.getProperty("user.dir");
    }

    private boolean initializeWithModel(File model) {
        if (AlloyInterface.compile(model.getPath()) == null) {
            System.out.println("error. Could not parse model.");
            return false;
        }

        String modelString;
        try {
            modelString = AlloyUtils.readFromFile(model);
        } catch (IOException e) {
            System.out.println("error. Failed to read file.");
            return false;
        }

        String configString = ParsingConf.getConfStringFromFileString(modelString).trim();
        if (configString.isEmpty()) {
            // If the new model has no embedded ParsingConf, make sure any existing embedded
            // ParsingConf for the previous model is removed.
            embeddedParsingConf = null;
        } else {
            try {
                embeddedParsingConf = ParsingConf.initializeWithYaml(configString);
            } catch (YAMLException e) {
                System.out.println("error. Invalid configuration.");
                return false;
            }
        }

        int initStartIndex = modelString.indexOf(String.format("pred %s", getParsingConf().getInitPredicateName()));
        if (initStartIndex == -1) {
            System.out.printf("error. Predicate %s not found.\n", getParsingConf().getInitPredicateName());
            return false;
        }

        // Count the number of BLOCK_INITIALIZERs and BLOCK_TERMINATORs to
        // determine the end of the init predicate.
        int blocks = 0;
        int initEndIndex = -1;
        for (int i = initStartIndex; i < modelString.length(); i++) {
            String c = String.valueOf(modelString.charAt(i));
            if (c.equals(AlloyConstants.BLOCK_INITIALIZER)) {
                blocks += 1;
            } else if (c.equals(AlloyConstants.BLOCK_TERMINATOR)) {
                blocks -= 1;
                if (blocks == 0) {
                    // When all blocks are closed, the end of the predicate has
                    // been found.
                    initEndIndex = i;
                    break;
                } else if (blocks < 0) {
                    // More BLOCK_TERMINATORs than BLOCK_INITIALIZERs is a
                    // syntax error.
                    break;
                }
            }
        }

        if (initEndIndex == -1) {
            System.out.printf("error. Issue parsing predicate %s.\n", getParsingConf().getInitPredicateName());
            return false;
        }

        this.alloyModelFile = model;
        this.alloyInitString = modelString.substring(initStartIndex, initEndIndex + 1);
        this.alloyModelString =
            modelString.substring(0, initStartIndex) +
                AlloyUtils.getConcreteSigsDefinition(getParsingConf().getAdditionalSigScopes()) +
                modelString.substring(initEndIndex + 1, modelString.length());

        try {
            AlloyUtils.writeToFile(
                AlloyUtils.annotatedTransitionSystem(
                    this.alloyModelString + this.alloyInitString,
                    getParsingConf(),
                    0
                ),
                alloyModelFile
            );
        } catch (IOException e) {
            System.out.println("error. I/O failed, cannot initialize model.");
            return false;
        }

        CompModule compModule = AlloyInterface.compile(model.getAbsolutePath());
        if (compModule == null) {
            System.out.println("error. Could not parse model.");
            return false;
        }

        A4Solution sol = AlloyInterface.run(compModule);

        evaluateScopes(sol);

        Sig stateSig = AlloyInterface.getSigFromA4Solution(sol, getParsingConf().getStateSigName());
        if (stateSig == null) {
            System.out.printf("error. Sig %s not found.\n", getParsingConf().getStateSigName());
            return false;
        }

        stateSigData = new SigData(stateSig);

        List<StateNode> initialNodes = getStateNodesForA4Solution(sol);
        statePath.clearPath();
        statePath.setTempPath(initialNodes);
        stateGraph.initWithNodes(initialNodes);

        this.traceMode = false;
        this.activeSolutions.clear();
        this.activeSolutions.push(sol);

        return true;
    }

    private boolean initializeWithTrace(File trace) {
        // Ensure any embedded ParsingConf from a previously loaded model is removed.
        embeddedParsingConf = null;

        A4Solution sol;
        try {
            sol = AlloyInterface.solutionFromXMLFile(trace);
        } catch (Exception e) {
            return false;
        }

        evaluateScopes(sol);

        Sig stateSig = AlloyInterface.getSigFromA4Solution(sol, getParsingConf().getStateSigName());
        if (stateSig == null) {
            System.out.printf("error. Sig %s not found.\n", getParsingConf().getStateSigName());
            return false;
        }

        stateSigData = new SigData(stateSig);

        List<StateNode> stateNodes = getStateNodesForA4Solution(sol);
        if (stateNodes.isEmpty()) {
            return false;
        }

        statePath.initWithPath(stateNodes);
        statePath.setPosition(0);
        stateGraph.initWithNodes(stateNodes);

        this.traceMode = true;
        this.activeSolutions.clear();

        return true;
    }

    private List<StateNode> getStateNodesForA4Solution(A4Solution sol) {
        List<StateNode> stateNodes = new ArrayList<>();

        Sig stateSig = AlloyInterface.getSigFromA4Solution(sol, getParsingConf().getStateSigName());
        if (stateSig == null) {
            return stateNodes;
        }

        if (stateSigData == null) {
            stateSigData = new SigData(stateSig);
        }

        int steps = sol.eval(stateSig).size();
        for (int i = 0; i < steps; i++) {
            stateNodes.add(new StateNode(stateSigData, getParsingConf()));
        }

        for (Sig.Field field : stateSig.getFields()) {
            for (A4Tuple tuple : sol.eval(field)) {
                String atom = tuple.atom(0);
                StateNode node = stateNodes.get(
                    Integer.parseInt(atom.split(AlloyConstants.ALLOY_ATOM_SEPARATOR)[1])
                );
                String tupleString = tuple.toString();
                node.addValueToField(
                    field.label,
                    tupleString
                        .substring(
                            tupleString.indexOf(AlloyConstants.SET_DELIMITER) + 2, tupleString.length()
                        )
                        // Sigs will only ever have $0 as a suffix since we control their scope.
                        .replace(AlloyConstants.VALUE_SUFFIX, "")
                    );
            }
        }

        return stateNodes;
    }

    /**
     * evaluateScopes gets the scope for each reachable sig for an A4Solution and stores it in StateGraph.
     * @param A4Solution sol
     */
    private void evaluateScopes(A4Solution sol) {
        for (Sig s : sol.getAllReachableSigs()) {
            String label = s.label;
            if (label.startsWith(AlloyConstants.THIS)) {
                label = label.substring(AlloyConstants.THIS.length());
            }
            // Ignore the 'univ' sig which itself contains the scope of the entire model.
            if (label.equals(AlloyConstants.UNIV)) {
                continue;
            }
            // Ignore internal concrete sigs that we've injected into the model.
            else if (label.matches(AlloyConstants.CONCRETE_SIG_REGEX)) {
                int i = label.indexOf(AlloyConstants.UNDERSCORE);
                String origSigName = label.substring(0, i);
                Map<String, Integer> sigScopes = getParsingConf().getAdditionalSigScopes();
                if (sigScopes.containsKey(origSigName) &&
                    Integer.parseInt(label.substring(i + 1)) < sigScopes.get(origSigName)) {
                    continue;
                }
            }
            List<String> tuples = new ArrayList<>();
            for (A4Tuple t : sol.eval(s)) {
                tuples.add(t.toString().replace(AlloyConstants.VALUE_SUFFIX, ""));
            }
            scopes.put(label, tuples);
        }
    }

    private ParsingConf getParsingConf() {
        return embeddedParsingConf != null ? embeddedParsingConf : persistentParsingConf;
    }
}
