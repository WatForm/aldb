package commands;

import simulation.SimulationManager;

public class AltCommand extends Command {
    private final static String R_FLAG = "-r";

    public String getName() {
        return CommandConstants.ALT_NAME;
    }

    public String[] getShorthand() {
        return CommandConstants.ALT_SHORTHAND;
    }

    public String getDescription() {
        return CommandConstants.ALT_DESCRIPTION;
    }

    public String getHelp() {
        return CommandConstants.ALT_HELP;
    }

    public void execute(String[] input, SimulationManager simulationManager) {
        if (!simulationManager.isInitialized()) {
            System.out.println(CommandConstants.NO_MODEL_LOADED);
            return;
        }

        boolean reverse = false;
        if (input.length != 1) {
            if (input[1].equals(R_FLAG)) {
                reverse = true;
            } else {
                System.out.println(CommandConstants.ALT_HELP);
                return;
            }
        }

        if (!simulationManager.selectAlternatePath(reverse)) {
            System.out.println(CommandConstants.ALT_UNAVAILABLE);
            return;
        }

        if (simulationManager.isDiffMode()) {
            System.out.println(simulationManager.getCurrentStateDiffStringFromLastCommit());
        } else {
            System.out.println(simulationManager.getCurrentStateString());
        }
    }
}
