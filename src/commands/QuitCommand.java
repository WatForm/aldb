package commands;

import simulation.SimulationManager;

public class QuitCommand extends Command {
    public String getName() {
        return CommandConstants.QUIT_NAME;
    }

    public String getDescription() {
        return CommandConstants.QUIT_DESCRIPTION;
    }

    public String getHelp() {
        return CommandConstants.QUIT_HELP;
    }

    public String[] getShorthand() {
        return CommandConstants.QUIT_SHORTHAND;
    }

    public void execute(String[] input, SimulationManager simulationManager) {
        // Only require user confirmation when they are in an active simulation.
        if (!simulationManager.isInitialized()) {
            System.exit(0);
        }
        System.out.print(CommandConstants.QUIT_USER_PROMPT);
        String s = System.console().readLine();
        for (String accepted : CommandConstants.QUIT_ACCEPTED_RESPONSES) {
            if (s.equals(accepted)) {
                System.exit(0);
            }
        }
    }
}
