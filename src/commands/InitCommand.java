package commands;

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
        if (!simulationManager.isInitialized()) {
            System.out.println(CommandConstants.NO_MODEL_LOADED);
            return;
        }

        simulationManager.resetPathToInit();
    }
}
