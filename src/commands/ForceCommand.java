package commands;

import simulation.AliasManager;
import simulation.ConstraintManager;
import simulation.DashSimulationManager;
import simulation.SimulationManager;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForceCommand extends Command {

    public String getName() {
        return CommandConstants.Force_NAME;
    }

    public String getDescription() {
        return CommandConstants.Force_DESCRIPTION;
    }

    public String getHelp() {
        return CommandConstants.Force_HELP;
    }

    public String[] getShorthand() {
        return CommandConstants.Force_SHORTHAND;
    }

    public void execute(String[] input, SimulationManager simulationManager) {
    	
        DashSimulationManager dashsimulationManager = (DashSimulationManager) simulationManager;
    	      
    	

        if (input.length == 1) {
            System.out.println(CommandConstants.Force_HELP);
            return;
        }
        if (input.length == 2) {
        	String transitionName = input[1];
        	if (dashsimulationManager.isTransition(transitionName).equals("")){
        		System.out.println("transition does not exist.");
        		return;
        	}
        	dashsimulationManager.forceTransition(dashsimulationManager.isTransition(transitionName), 10);
        }
        if (input.length == 3) {
        	String transitionName = input[1];
        	Integer max_steps = Integer.parseInt(input[2]);
        	if (dashsimulationManager.isTransition(transitionName).equals("")){
        		System.out.println("transition does not exist.");
        		return;
        	}
        	dashsimulationManager.forceTransition(dashsimulationManager.isTransition(transitionName), max_steps);
        }
        
    }
}
