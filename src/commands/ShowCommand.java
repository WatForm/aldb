package commands;

import simulation.DashSimulationManager;
import simulation.SimulationManager;

public class ShowCommand extends Command {


    public String getName() {
        return CommandConstants.SHOW_NAME;
    }

    public String getDescription() {
        return CommandConstants.SHOW_DESCRIPTION;
    }

    public String getHelp() {
        return CommandConstants.SHOW_HELP;
    }



    public void execute(String[] input, SimulationManager simulationManager) {
        if (!simulationManager.isInitialized()) {
            System.out.println(CommandConstants.NO_MODEL_LOADED);
            return;
        }

        if (input.length == 1) { //if no arguments are specified, print current node
            System.out.println(simulationManager.getCurrentStateString());
            return;
        }
        String stateName = input[1]; //
        if (stateName != null && stateName.matches("[sS]\\d+")) {
            int identifier = Integer.parseInt(stateName.substring(1));
            //update control states graph here
            if (simulationManager instanceof DashSimulationManager) {
                DashSimulationManager dashSimManager = (DashSimulationManager) simulationManager;
            }
            
            System.out.println(simulationManager.getStateString(identifier));
            return;
        } else {
            System.out.println("Illegal input");
        }
    }
}
