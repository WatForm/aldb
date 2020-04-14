package commands;

import alloy.AlloyUtils;
import simulation.SimulationManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DotCommand extends Command {
    private final String FILE_PREFIX = "aldb.";
    private final String DATE_FORMAT = "yyyy.MM.dd.HH.mm.ss";
    private final String EXT = ".gv";

    public String getName() {
        return CommandConstants.DOT_NAME;
    }

    public String getDescription() {
        return CommandConstants.DOT_DESCRIPTION;
    }

    public String[] getShorthand() {
        return CommandConstants.DOT_SHORTHAND;
    }

    public String getHelp() {
        return CommandConstants.DOT_HELP;
    }

    public void execute(String[] input, SimulationManager simulationManager) {
        if (!simulationManager.isInitialized()) {
            System.out.println(CommandConstants.NO_MODEL_LOADED);
            return;
        }

        String filename = FILE_PREFIX + new SimpleDateFormat(DATE_FORMAT).format(new Date()) + EXT;
        System.out.printf(CommandConstants.WRITING_DOT_GRAPH, filename);

        File f = new File(simulationManager.getWorkingDirPath(), filename);
        try {
            f.createNewFile();
            AlloyUtils.writeToFile(simulationManager.getDOTString(), f);
            System.out.println(CommandConstants.DONE);
        } catch (IOException e) {
            System.out.println(CommandConstants.IO_FAILED);
        }
    }
}
