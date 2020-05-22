package commands;

import simulation.SimulationManager;

import java.io.File;

public class TraceCommand extends Command {
    private final static String[] SHORTHAND = CommandConstants.TRACE_SHORTHAND;

    public String getName() {
        return CommandConstants.TRACE_NAME;
    }

    public String getDescription() {
        return CommandConstants.TRACE_DESCRIPTION;
    }

    public String getHelp() {
        return CommandConstants.TRACE_HELP;
    }

    public String[] getShorthand() {
        return SHORTHAND;
    }

    public boolean requiresFile() {
        return true;
    }

    public void execute(String[] input, SimulationManager simulationManager) {
        if (input.length < 2) {
            System.out.println(CommandConstants.NO_FILE_SPECIFIED);
            return;
        }

        String filename = input[1];
        File file = new File(filename);
        if (!file.exists()) {
            System.out.printf(CommandConstants.NO_SUCH_FILE, filename);
            return;
        }

        System.out.printf(CommandConstants.READING_TRACE, filename);

        if (!simulationManager.initialize(file, true)) {
            System.out.println(CommandConstants.INVALID_TRACE);
            return;
        }

        System.out.println(CommandConstants.DONE);
    }
}
