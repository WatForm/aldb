package simulation;
import core.DashGUI;
import core.DashImageDisplay;
import core.ImageDisplay;
import core.JsonDrawing;
import core.AlloyGUI;
import state.StateGraph;
import state.StateNode;
import state.StatePath;

import alloy.AlloyConstants;
import alloy.AlloyInterface;
import alloy.AlloyUtils;
import alloy.ParsingConf;
import alloy.SigData;
import ca.uwaterloo.watform.core.DashUtilFcns;
import ca.uwaterloo.watform.mainfunctions.MainFunctions;
import ca.uwaterloo.watform.parser.DashModule;
import commands.CommandConstants;
import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.parser.CompModule;
import edu.mit.csail.sdg.translator.A4Solution;
import edu.mit.csail.sdg.translator.A4Tuple;

import java.io.IOException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.yaml.snakeyaml.error.YAMLException;
import javax.swing.*;
public class DashSimulationManager extends SimulationManager{
    private final static String TEMP_FILENAME_PREFIX = "_tmp_";

    private File alloyModelFile;
    private String alloyModelString;
    private String alloyInitString;
    private SortedMap<String, List<String>> scopes;
    private ParsingConf persistentParsingConf;  // Set by set conf - used across multiple models.
    private ParsingConf embeddedParsingConf;  // Set by load - used for the current model only.
    private SigData stateSigData;
    private StatePath statePath;
    private StateGraph stateGraph;
    private Stack<A4Solution> activeSolutions;
    private AliasManager aliasManager;
    private ConstraintManager constraintManager;
    private GraphPrinter gp;
    private ImageDisplay display;
    private DashImageDisplay dashdisplay;
    private boolean traceMode;
    private boolean diffMode;
    private DashModule d;// Whether differential output is enabled.
    private DashGUI dashGUI;
    
    public DashSimulationManager() {
        scopes = new TreeMap<>();
        statePath = new StatePath();
        stateGraph = new StateGraph();
        persistentParsingConf = new ParsingConf();
        embeddedParsingConf = null;
        activeSolutions = new Stack<>();
        aliasManager = new AliasManager();
        constraintManager = new ConstraintManager();
        traceMode = false;
        diffMode = true;
        
    }
    
    // checks if a given label represents a state, e.g. is s/S followed by a digit
    public boolean isState(String label) {
    	String regex = "^[sS]\\d+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(label);
        return matcher.matches();
    }
    
    //Dash-specific checks if a given label represents a transition
    public String isTransition(String label) {
    	List<String> transitions = d.getAllTransNames();
		for (String transition : transitions) {
			
			if (label.equals(formatString(transition))) {
				return formatString(transition);
			}
		}
    	return "";
    }
    
    
    //implements logic for handling clicks on transitions/states in GUI
    public void handleClick(String label) { //
    	
    	if ((isState(label))) {
    		String identifier = label.substring(1);
    		moveToState(Integer.parseInt(identifier));
    		return;
    	}
    	if (!isTransition(label).equals("")) {
    		

            int limit = 10;

            if (!forceTransition(isTransition(label),limit)) {
                System.out.println(CommandConstants.UNTIL_FAILED);
            }
    		return;
    	}
    }
    
    
    //Dash-specific, given a transitionName and step size, add alloy predicate to force the transition be taken within the specified number of steps
    public boolean forceTransition(String transitionName,int limit) {
    	String breakPredicate = AlloyUtils.getTransitionPredicate(transitionName);
    	
        for (int steps = 1; steps <= limit; steps++) {
            try {
                String curInitString;
                if (stateGraph.size() > 1) {
                    curInitString = statePath.getCurNode().getAlloyInitString();
                } else {
                    curInitString = alloyInitString;
                }
                //System.out.println( AlloyUtils.annotatedTransitionSystemForced(alloyModelString + curInitString + breakPredicate, getParsingConf(), steps));
                AlloyUtils.writeToFile(
                    AlloyUtils.annotatedTransitionSystemForced(alloyModelString + curInitString + breakPredicate, getParsingConf(), steps),
                    alloyModelFile
                );
            } catch (IOException e) {
                return false;
            }

            CompModule compModule = null;
            try {
                compModule = AlloyInterface.compile(alloyModelFile.getAbsolutePath());
            } catch (Err e) {
                return false;
            }

            A4Solution sol = null;
            try {
                sol = AlloyInterface.run(compModule);
            } catch (Err e) {
                return false;
            }

            if (!sol.satisfiable()) {
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
            
            savePath(stateNodes);
            updateHierarchy();
            printGraph();
            loadImage();
            loadDash();
            //statePath.printPath();
        	//stateGraph.printGraph();
            return true;
        }
        System.out.println("Unable to force transition within specified number of steps.");
        return false;
    }
    
    
    
    
    public void showDashGUI() { // Dash-specific, starts/refreshes the GUI displaying the interactive state tree and the control states graph
    	
    	if (dashGUI==null) {
    		dashGUI = new DashGUI("state_tree.json","control_states.json",this);
    		dashGUI.refreshJsonWithDelay(1000);
    		return;
    	}
    	dashGUI.refreshJsonWithDelay(1000);
    	
    }
    
    public void savePath() { //saves the path for the current node
    	if (statePath.getCurNode()!=null) {
	    	StateNode curr_node_from_path = statePath.getCurNode();
	    	StateNode curr_node=stateGraph.getNodeById(curr_node_from_path.getIdentifier());
	    	if (curr_node.getPath().size()==0) {
	    		curr_node.storePath(statePath.getPath());
	    		//System.out.println(curr_node.getPath());
	    	}
    	}
    }
    
    //Dash-specific, saves the path for a given list of nodes, used in forceTransitions
    public void savePath(List<StateNode> nodes) {
        for (StateNode node : nodes) {
        	List<Integer> targetPath = statePath.getPath();
        	int index = targetPath.indexOf(node.getIdentifier());
        	//System.out.println("saving: node "+node.getIdentifier()+" "+targetPath.subList(0, index+1));
            node.storePath(targetPath.subList(0, index+1));
        }
    }
    
    //builds the .dot file using the information stored in stateGraph and generates the .png image
    public void printGraph() {
    	gp = new GraphPrinter();
    	for (int i = 0; i < stateGraph.size(); i++) {
            StateNode node = stateGraph.getNode(i);
            if (node.getSteps().size()!=0) {
	            for (StateNode next_node : node.getSteps()) {
	            	if (next_node.getTransitionName().isEmpty()) {
	            		gp.addln("S"+Integer.toString(node.getIdentifier()) + " -> " + "S"+Integer.toString(next_node.getIdentifier()));
	            	}
	            	else {
	            		gp.addln("S"+Integer.toString(node.getIdentifier()) + " -> " + "S"+Integer.toString(next_node.getIdentifier())+"[label="+next_node.getTransitionName() +"]");
	            	}
	            	
	            }
            }
            else {
            	gp.addln("S"+Integer.toString(node.getIdentifier()));
            }
            if (node.hasStable()) {
	            if (node.getStable()) {
	        		gp.addln("S"+Integer.toString(node.getIdentifier()) + "[color=green]");
	        	}
	            else {
	            	gp.addln("S"+Integer.toString(node.getIdentifier()) + "[color=red]");
	            }
            }
        }
    	if (statePath.getCurNode()!=null) {
	    	StateNode curr_node = statePath.getCurNode();
	    	gp.addln("S"+Integer.toString(curr_node.getIdentifier())+ "[style=filled, fillcolor=yellow]"); //highlight curr node
	    	
	    	
    	}
	    gp.print("state_tree");
	    gp.generateJson("state_tree");
	    
    }
    
    public void loadImage() {
	    if (this.dashdisplay==null) {
	 	   	this.dashdisplay=new DashImageDisplay();
	 	}
	    dashdisplay.refresh();
	    dashdisplay.setVisible(true);
	    
    }
    
    public boolean isTrace() {
        return traceMode;
    }

    public void setDiffMode(boolean b) {
        diffMode = b;
    }

    public boolean isDiffMode() {
        return diffMode;
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
    
    // move to a specified state using the state path stored (which represents the first route taken to reach the state)
    public boolean moveToState(int identifier) {
    	if (identifier > stateGraph.size()) {
    		return false;
    	}
    	else {
    		List<StateNode> targetpath = new ArrayList<>();
    		StateNode targetnode = stateGraph.getNodeById(identifier);
    		for (int i : targetnode.getPath()) {
    			targetpath.add(stateGraph.getNodeById(i));
    		}
    		statePath.setPath(targetpath);
    		printGraph();
    		loadImage();
    		loadDash();
    		updateHierarchy();
    		return true;
    	}
    }
    /**
     * initialize is a wrapper for initializing a model or trace which cleans up internal state if
     * initialization fails.
     * @return boolean
     */
    public boolean initialize(File file, boolean isTrace) {
        ParsingConf oldEmbeddedParsingConf = embeddedParsingConf;
        
        if (file.getName().substring(file.getName().lastIndexOf('.') + 1).equals("dsh")) {
        	boolean res = initializeDashModel(file);
        	return res;
        }
        else {
        	System.out.println("Currently in dash mode, file with .dsh extension expected.");
        	return false;
        }
        
       
    }
    
    //Dash-specific, takes a .dsh file, translate it to .als and loads it in aldb
    public boolean initializeDashModel(File file) {
    	A4Reporter rep = new A4Reporter();
    	d = MainFunctions.parseAndResolveDashFile(file.getPath(),rep);
	    //buildHierarchy(d,rootName,gp,null);
	    
	    //addTransitions(gp);
	    
	    //translate to alloy
	    //initializeWithModel(file);
	    try {
	    	//d = MainFunctions.resolveDash(d, rep);
            System.out.println("Resolved Dash"); 
            CompModule c = MainFunctions.translate(d, rep);
            System.out.println("Translated Dash to Alloy"); 
            System.out.println("Method: " + "traces" +"\n");
		    String filename=file.getName();
		    String outfilename = filename.substring(0,filename.length()-4) + "-" + "traces" + ".als";
	        File out = new File(outfilename);
	        if (!out.exists()) out.createNewFile();
	        System.out.println("Creating: " + outfilename);
	        FileWriter fw = new FileWriter(out.getAbsoluteFile());
	        BufferedWriter bw = new BufferedWriter(fw);
	        bw.write(d.toStringAlloy());
	        bw.close();
	        System.out.println("Done!");
	        initializeWithModel(out);
	        
	    }
	    catch (Exception e) {
            DashUtilFcns.handleException(e);
        }
	    
	   
	    //showTransitions();
	    //gp.print("control_states");
	    printGraph();
	    savePath();
	    
	    updateHierarchy();
	    loadImage();
	    loadDash();
	    
    	return true;
    }
    
    
    //Dash-specific, helper function for getting max depth of the control state tree
    public static int maxDepth(DashModule d,String root) {
        if (root == null) {
            return 0;
        }
        if (d.getImmChildren(root).isEmpty()) {
            return 1;
        }
        int maxDepth = 0;
        for (String child : d.getImmChildren(root)) {
            maxDepth = Math.max(maxDepth, maxDepth(d,child));
        }
        return maxDepth + 1;
    }
    
  //Dash-specific, helper function for finding depth of a given node
	public static int findNodeDepth(String targetnode, DashModule d) {
		String root=d.getRootName();
        return findNodeDepthHelper(root, targetnode, 1, d);
    }

	//Dash-specific, helper function for finding depth of a given node
    private static int findNodeDepthHelper(String root, String targetnode, int depth, DashModule d) {
        if (root == null) {
            return -1; // Node not found
        }
        if (root.equals(targetnode)) {
            return depth;
        }
        for (String child : d.getImmChildren(root)) {
            int childDepth = findNodeDepthHelper(child, targetnode, depth + 1,  d);
            if (childDepth != -1) {
                return childDepth;
            }
        }
        return -1; // Node not found in this subtree
    }
	
    
  //Dash-specific, function for creating the .dot file for the control state graph
	public void buildHierarchy(DashModule d, String nodeName,GraphPrinter gp, List<String> highlightedStates) {
		
		//System.out.println("visiting node "+nodeName);
		//System.out.println(nodeName+" is leaf:"+d.isLeaf(nodeName));
		//System.out.println(nodeName+" depth:"+findNodeDepth(nodeName,d));
		//System.out.println("max depth:"+maxDepth(d,d.getRootName()));
		if (!d.isLeaf(nodeName) || findNodeDepth(nodeName,d)!=maxDepth(d,d.getRootName())) {
			if (d.isRoot(nodeName)) {
				gp.addln("subgraph cluster_"+formatString(nodeName)+" {");
				gp.addln("label="+formatString(nodeName));
				if (d.isAnd(nodeName)) {
					gp.addln("style=dashed");
				}
				
			}
			else {
				gp.addln("subgraph cluster_"+formatString(nodeName)+" {");
				if (highlightedStates!=null && highlightedStates.contains(formatString(nodeName))) {
					gp.addln("style=filled");
					gp.addln("fillcolor=yellow");
				}
				gp.addln("label="+formatString(nodeName).substring(formatString(nodeName).lastIndexOf('_')+1));
				gp.addln(formatString(nodeName)+" [style=invis,shape=point,  penwidth=0]");
				if (d.isAnd(nodeName)) {
					gp.addln("style=dashed");
				}
			}
		}
		else {
			if (highlightedStates!=null && highlightedStates.contains(formatString(nodeName))) {
				gp.addln(formatString(nodeName)+" [label="+formatString(nodeName).substring(formatString(nodeName).lastIndexOf('_') + 1)+", style=filled, fillcolor=yellow"+"]");
			}
			else {
				gp.addln(formatString(nodeName)+" [label="+formatString(nodeName).substring(formatString(nodeName).lastIndexOf('_') + 1)+"]");
			}
		}
		List<String> children = d.getImmChildren(nodeName);
		for (String child : children) {	
			buildHierarchy(d, child,gp,highlightedStates);
        }
		
		if (!d.isLeaf(nodeName)|| findNodeDepth(nodeName,d)!=maxDepth(d,d.getRootName())) {
			if (!d.isRoot(nodeName)) {
				gp.addln(formatString(nodeName)+"_other_side [style=invis,shape=point,penwidth=0]");
			}
			gp.addln("}");
		}
	}
	
	//Dash-specific, prints out all transitions
	public void showTransitions() {
		List<String> transitions = d.getAllTransNames();
		for (String transition : transitions) {
			System.out.println("transition: "+transition);
			System.out.println("source: "+d.getTransSrc(transition));
			System.out.println("destination: "+d.getTransDest(transition));
		}
	} 
	
	//Dash-specific, adds transitions (edges) to the control state graph
	public void addTransitions(GraphPrinter gp) {
		List<String> transitions = d.getAllTransNames();
		for (String transition : transitions) {
			//String[] parts = transition.split("/");
			//String lastPart = parts[parts.length - 1];
			String source = d.getTransSrc(transition).toString();
			String destination = d.getTransDest(transition).toString();
			if (findNodeDepth(source,d)==maxDepth(d,d.getRootName())){ //is node
				gp.addln(formatString(source)+"->"+formatString(destination)+" [label="+formatString(transition)+"]");
			}
			else {  //is subgraph
				gp.addln(formatString(source)+"->"+formatString(destination)+"_other_side"+" [label="+formatString(transition)+",ltail=cluster_"+formatString(source)+",lhead=cluster_"+formatString(destination)+",]");
			}
		}
	} 
	
	//Dash-specific, updates the control state .dot file
	public void updateHierarchy() {
		gp = new GraphPrinter();
		
    	
    	if (statePath.getCurNode()!=null) {
    		
	    	StateNode curr_node = statePath.getCurNode();
	    	List<String> controlstates=curr_node.getControlStateNames();
	    	buildHierarchy(d, d.getRootName(), gp,controlstates);
    	}
    	
    	addTransitions(gp);
	    gp.print("control_states");
	    gp.generateJson("control_states");
	    
	    
    }
	
	public void loadDash() {
		showDashGUI();
    	dashdisplay.refresh();
    	dashdisplay.setVisible(true);
	}
	
	//Dash-specific, updates the control state .dot file and highlights current node
	public void updateHierarchy(int id) {
		gp = new GraphPrinter();
		
    	
    	if (statePath.getNode(id-1)!=null) {
	    	StateNode curr_node = statePath.getNode(id-1);
	    	List<String> controlstates=curr_node.getControlStateNames();
	    	buildHierarchy(d, d.getRootName(), gp,controlstates);
    	}
	    gp.print("control_states");
	    gp.generateJson("control_states");
	    showDashGUI();
	    dashdisplay.refresh();
	    dashdisplay.setVisible(true);
    }
    
	// replace the '/'s in dash states and transitions with '_'
	public String formatString(String s) {
		return s.replace('/', '_');
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
        //statePath.printPath();
        updateHierarchy();
        printGraph();
        loadImage();
        loadDash();
        savePath();
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

        CompModule compModule = null;
        try {
            compModule = AlloyInterface.compile(alloyModelFile.getAbsolutePath());
        } catch (Err e) {
            System.out.println("Cannot perform step. Internal error.");
            return false;
        }

        A4Solution sol = null;
        try {
            sol = AlloyInterface.run(compModule);
        } catch (Err e) {
            System.out.println("Cannot perform step. Internal error.");
            return false;
        }

        if (!sol.satisfiable()) {
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
        //statePath.printPath();
        printGraph();
        loadImage();
        loadDash();
        savePath();
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
        //statePath.printPath();
        
        printGraph();
        updateHierarchy();
        loadImage();
        loadDash();
        savePath();
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

            CompModule compModule = null;
            try {
                compModule = AlloyInterface.compile(alloyModelFile.getAbsolutePath());
            } catch (Err e) {
                return false;
            }

            A4Solution sol = null;
            try {
                sol = AlloyInterface.run(compModule);
            } catch (Err e) {
                return false;
            }

            if (!sol.satisfiable()) {
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
            updateHierarchy();
            printGraph();
            loadImage();
            loadDash();
            savePath();
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

        CompModule compModule = null;
        try {
            compModule = AlloyInterface.compile(alloyModelFile.getAbsolutePath());
        } catch (Err e) {
            System.out.println("internal error.");
            return false;
        }

        A4Solution sol = null;
        try {
            sol = AlloyInterface.run(compModule);
        } catch (Err e) {
            System.out.println("internal error.");
            return false;
        }

        List<StateNode> initialNodes = getStateNodesForA4Solution(sol);
        // We don't re-add this initial node to the StateGraph, so manually set its identifier here.
        initialNodes.get(0).setIdentifier(1);

        statePath.clearPath();
        statePath.setTempPath(initialNodes);

        activeSolutions.clear();
        activeSolutions.push(sol);
        updateHierarchy();
        printGraph();
        loadImage();
        loadDash();
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

        try {
            AlloyInterface.compile(alloyModelFile.getAbsolutePath());
        } catch (Err e) {
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
    
    public String getStateString(int id) {
    	if (id<=0 || id-1>stateGraph.size()) {
    		return "state not found!";
    	}
        return stateGraph.getNode(id-1).toString();
    }
    
    public String getCurrentStateString() {
        return statePath.getCurNode().toString();
    }

    public String getCurrentStateStringForProperty(String property) {
        return statePath.getCurNode().stringForProperty(property);
    }

    /**
     * getCurrentStateDiffStringFromLastCommit returns the diff between the current
     * and the previous last-committed state.
     * @return String
     */
    public String getCurrentStateDiffStringFromLastCommit() {
        StateNode prev = statePath.getNode(statePath.getPosition() - statePath.getTempPathSize());
        return statePath.getCurNode().getDiffString(prev);
    }

    /**
     * getCurrentStateDiffStringByDelta returns the diff between the current state
     * and the state at the (current - delta) position in the path.
     * @param int delta
     * @return String
     */
    public String getCurrentStateDiffStringByDelta(int delta) {
        StateNode prev = statePath.getNode(statePath.getPosition() - delta);
        return statePath.getCurNode().getDiffString(prev);
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
        try {
            AlloyInterface.compile(model.getPath());
        } catch (Err e) {
            System.out.printf("error.\n\n%s\n", e.toString());
            return false;
        }

        String tempModelFilename = TEMP_FILENAME_PREFIX + model.getName();
        // Note that the temp model file must be created in the same directory as the input model
        // in order for Alloy to correctly find imported submodules.
        File tempModelFile = new File(model.getParentFile(), tempModelFilename);
        tempModelFile.deleteOnExit();

        try {
            Files.copy(model.toPath(), tempModelFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.out.println("error. I/O failed.");
            return false;
        }
        
        alloyModelFile = tempModelFile;

        String modelString;
        try {
            modelString = AlloyUtils.readFromFile(alloyModelFile);
        } catch (IOException e) {
            System.out.println("error. Failed to read file.");
            return false;
        }

        String configString = ParsingConf.getConfStringFromFileString(modelString).trim();
        if (!configString.isEmpty()) {
            try {
                embeddedParsingConf = ParsingConf.initializeWithYaml(configString);
            } catch (YAMLException e) {
                System.out.println("error. Invalid configuration.");
                return false;
            }
        }

        int transRelIndex = modelString.indexOf(String.format("pred %s", getParsingConf().getTransitionRelationName()));
        if (transRelIndex == -1) {
            System.out.printf("error. Predicate %s not found.\n", getParsingConf().getTransitionRelationName());
            return false;
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

        CompModule compModule = null;
        try {
            compModule = AlloyInterface.compile(alloyModelFile.getAbsolutePath());
        } catch (Err e) {
            System.out.println("internal error.");
            return false;
        }

        A4Solution sol = null;
        try {
            sol = AlloyInterface.run(compModule);
        } catch (Err e) {
            System.out.printf("error.\n\n%s\n", e.msg.trim());
            return false;
        }

        if (!sol.satisfiable()) {
            System.out.println("error. No instance found. Predicate may be inconsistent.");
            return false;
        }

        evaluateScopes(sol);

        Sig stateSig = AlloyInterface.getSigFromA4Solution(sol, getParsingConf().getStateSigName());
        if (stateSig == null) {
            System.out.printf("error. Sig %s not found.\n", getParsingConf().getStateSigName());
            return false;
        }
        //System.out.println(getParsingConf().getStateSigName());
        
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
        A4Solution sol;
        try {
            sol = AlloyInterface.solutionFromXMLFile(trace);
        } catch (Err e) {
            System.out.printf("error.\n\n%s\n", e.toString());
            return false;
        } catch (Exception e) {
            System.out.println("error. Could not read XML file.");
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
            System.out.println("internal error.");
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
