package commands;

import alloy.AlloyUtils;
import simulation.DashSimulationManager;
import simulation.SimulationManager;

import java.io.File;

public class LoadCommand extends Command {
    public String getName() {
        return CommandConstants.LOAD_NAME;
    }

    public String getDescription() {
        return CommandConstants.LOAD_DESCRIPTION;
    }

    public String getHelp() {
        return CommandConstants.LOAD_HELP;
    }

    public String[] getShorthand() {
        return CommandConstants.LOAD_SHORTHAND;
    }

    public boolean requiresFile() {
        return true;
    }

    public void execute(String[] input, SimulationManager simulationManager) {
    	if (simulationManager instanceof DashSimulationManager) {
            simulationManager = (DashSimulationManager) simulationManager; 
        } 
        if (input.length < 2) {
            System.out.println(CommandConstants.NO_FILE_SPECIFIED);
            return;
        }
        else {
        	if (input[1].equals("dash")||input[1].equals("-d")) { //loading dash module
        		if (input.length < 3) {
                    System.out.println(CommandConstants.NO_FILE_SPECIFIED);
                    return;
                }
        		String filename = input[2];
		        File file = new File(filename);
		        if (!file.exists()) {
		            System.out.printf(CommandConstants.NO_SUCH_FILE, filename);
		            return;
		        }
		        System.out.printf(CommandConstants.READING_DASH_MODEL, filename);
		        if (simulationManager.initialize(file, false)) {
		            System.out.println(CommandConstants.DONE);
		            
		        }
        	}
        	else {
		        String filename = input[1];
		        File file = new File(filename);
		        if (!file.exists()) {
		            System.out.printf(CommandConstants.NO_SUCH_FILE, filename);
		            return;
		        }
		
		        System.out.printf(CommandConstants.READING_MODEL, filename);
		
		        if (simulationManager.initialize(file, false)) {
		            System.out.println(CommandConstants.DONE);
		        }
        	}
        }
    }
}
