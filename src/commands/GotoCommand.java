package commands;

import simulation.DashSimulationManager;
import simulation.SimulationManager;

public class GotoCommand extends Command {
    public String getName() {
        return CommandConstants.GOTO_NAME;
    }

    public String getDescription() {
        return CommandConstants.GOTO_DESCRIPTION;
    }

    public String getHelp() {
        return CommandConstants.GOTO_HELP;
    }

    public String[] getShorthand() {
        return null;
    }

    public void execute(String[] input, SimulationManager simulationManager) {
    	if (simulationManager instanceof DashSimulationManager) {
            simulationManager = (DashSimulationManager) simulationManager; 
        } 
    	if (!simulationManager.isInitialized()) {
            System.out.println(CommandConstants.NO_MODEL_LOADED);
            return;
        }
        if (input.length == 1) { 
            System.out.println("Illegal input. Usage: goto [state name]");
            return;
        }
        else {
        	if (input.length == 2 && input[0].equals("goto")) {
        		 String stateName = input[1]; //
        	        if (stateName != null && stateName.matches("[sS]\\d+")) {
        	            int identifier = Integer.parseInt(stateName.substring(1));
        	            if (simulationManager.moveToState(identifier)) {
        	            	System.out.println(simulationManager.getCurrentStateString());
            	            return;
        	            }
        	            else {
        	            	System.out.println("Failed to goto state");
        	            }
        	        } else {
        	            System.out.println("Illegal input. Usage: goto [state name]");
        	        }
        	}
        	else {
        		System.out.println("Illegal input. Usage: goto [state name]");
        	}
        }
    }
}
