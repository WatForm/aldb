package commands;

import simulation.SimulationManager;

public class NotFoundCommand extends Command {
    public String getName() {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public String getHelp() {
        return null;
    }

    public void execute(String[] input, SimulationManager simulationManager) {
        System.out.printf(CommandConstants.UNDEFINED_COMMAND, input[0]);
    }
}
