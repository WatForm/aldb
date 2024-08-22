package commands;

import simulation.DashSimulationManager;
import simulation.SimulationManager;

public class InitCommand extends Command {
    public String getName() {
        return CommandConstants.INIT_NAME;
    }

    public String getDescription() {
        return CommandConstants.INIT_DESCRIPTION;
    }

    public String getHelp() {
        return CommandConstants.INIT_HELP;
    }

    public String[] getShorthand() {
        return CommandConstants.INIT_SHORTHAND;
    }

    public void execute(String[] input, SimulationManager simulationManager) {
    	if (simulationManager instanceof DashSimulationManager) {
            simulationManager = (DashSimulationManager) simulationManager; 
        } 
        if (!simulationManager.isInitialized()) {
            System.out.println(CommandConstants.NO_MODEL_LOADED);
            return;
        }
        if (input.length == 1) { //if no arguments are specified, print current node
        	if (simulationManager.setToInit()) {
                System.out.println(simulationManager.getCurrentStateString());
            };
            return;
        }
        else {
        	if (input.length == 3 && input[1].equals("goto")) {
        		 String stateName = input[2]; //
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
        	            System.out.println("Illegal input");
        	        }
        	}
        	else {
        		//System.out.println(input.length);
        		//System.out.println(input[1]=="goto");
        		System.out.println("Invalid command");
        	}
        }
    }
}
