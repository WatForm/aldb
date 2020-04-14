package commands;

import simulation.SimulationManager;

public class CurrentCommand extends Command {
    private final static String[] SHORTHAND = CommandConstants.CURRENT_SHORTHAND;

    public String getName() {
        return CommandConstants.CURRENT_NAME;
    }

    public String getDescription() {
        return CommandConstants.CURRENT_DESCRIPTION;
    }

    public String getHelp() {
        return CommandConstants.CURRENT_HELP;
    }

    public String[] getShorthand() {
        return SHORTHAND;
    }

    public void execute(String[] input, SimulationManager simulationManager) {
        if (!simulationManager.isInitialized()) {
            System.out.println(CommandConstants.NO_MODEL_LOADED);
            return;
        }

        if (input.length == 1) {
            System.out.println(simulationManager.getCurrentStateString());
            return;
        }
        String property = input[1];
        System.out.println(simulationManager.getCurrentStateStringForProperty(property));
    }
}
