package commands;

import alloy.AlloyUtils;
import simulation.SimulationManager;

import java.io.IOException;
import java.io.File;
import java.nio.file.Files;

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

        System.out.printf(CommandConstants.READING_MODEL, filename);

        String inputFileContents;
        try {
            inputFileContents = AlloyUtils.readFromFile(file);
        } catch (IOException e) {
            System.out.println(CommandConstants.FAILED_TO_READ_FILE);
            return;
        }

        File tmpModelFile;
        try {
            tmpModelFile = AlloyUtils.createTmpFile(inputFileContents, file);
        } catch (IOException e) {
            System.out.println(CommandConstants.TMP_FILE_ERROR);
            return;
        }

        if (simulationManager.initializeWithModel(tmpModelFile)) {
            System.out.println(CommandConstants.DONE);
        }
    }
}
