package commands;

import alloy.AlloyUtils;
import simulation.SimulationManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class LoadCommand extends Command {
    private final static String TEMP_FILENAME_PREFIX = "_tmp_";

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

        String tempModelFilename = TEMP_FILENAME_PREFIX + file.getName();
        // Note that the temp model file must be created in the same directory as the input model
        // in order for Alloy to correctly find imported submodules.
        File tempModelFile = new File(file.getParentFile(), tempModelFilename);
        tempModelFile.deleteOnExit();

        try {
            Files.copy(file.toPath(), tempModelFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.out.println(CommandConstants.TMP_FILE_ERROR);
            return;
        }

        if (simulationManager.initialize(tempModelFile, false)) {
            System.out.println(CommandConstants.DONE);
        }
    }
}
