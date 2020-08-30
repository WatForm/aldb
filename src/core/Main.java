package core;

import commands.Command;
import commands.CommandConstants;
import commands.CommandRegistry;
import simulation.SimulationManager;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.IOException;

public class Main {
    private static SimulationManager simulationManager;
    private static SessionLog log;
    private static String prevSessionLogPath;

    private static String PROGRAM_NAME = "Alloy Debugger (ALDB)";
    private static String VERSION = "0.3.0";
    private static String PROGRAM_DESC = "A debugger for transition systems modelled in Alloy.";

    private static String RESTORE_FLAG_SHORT = "-r";
    private static String RESTORE_FLAG = "--restore";
    private static String RESTORE_DESC = "restore session from existing session log file";
    private static String VERSION_FLAG_SHORT = "-v";
    private static String VERSION_FLAG = "--version";
    private static String VERSION_DESC = "show version information and exit";

    private static String FILE_ARG_NAME = "file";
    private static String FILE_ARG_DESC = "Alloy model to load on start-up";
    private static String OPTIONAL = "?";

    private static String CREATE_LOG_ERROR = "Error. Unable to create session log. Continuing without session log.";
    private static String RESTORING_SESSION_TEXT = "Restoring session from session log \"%s\".";
    private static String SESSION_LOG_OPEN_ERROR = "Error. Session log \"%s\" could not be opened for reading. Continuing with new empty session log.";

    public static void main(String[] args) throws IOException {
        ArgumentParser parser = ArgumentParsers.newFor(PROGRAM_NAME).build()
            .defaultHelp(true)
            .description(PROGRAM_DESC)
            .version(String.format("${prog} v%s", VERSION));
        parser.addArgument(VERSION_FLAG_SHORT, VERSION_FLAG).help(VERSION_DESC).action(Arguments.version());
        parser.addArgument(RESTORE_FLAG_SHORT, RESTORE_FLAG).help(RESTORE_DESC);
        parser.addArgument(FILE_ARG_NAME).nargs(OPTIONAL).help(FILE_ARG_DESC);

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        simulationManager = new SimulationManager();
        CLI cli = new CLI();

        // Pre-load a model if its path is passed in on start-up.
        String modelPath = ns.getString(FILE_ARG_NAME);
        if (modelPath != null) {
            String[] input = { CommandConstants.LOAD_NAME, modelPath };
            Command command = CommandRegistry.commandForString(input[0]);
            command.execute(input, simulationManager);
        }

        log = new SessionLog(null);
        try {
            log.create();
        } catch (IOException e) {
            System.out.println(CREATE_LOG_ERROR);
        }

        // Attempt to restore a previous session if the restore flag is used.
        prevSessionLogPath = ns.getString(RESTORE_FLAG.substring(2));  // Drop leading `--`.
        if (prevSessionLogPath != null) {
            System.out.println(String.format(RESTORING_SESSION_TEXT, prevSessionLogPath));
            SessionLog prevLog = null;
            try {
                prevLog = new SessionLog(prevSessionLogPath);
            } catch (IOException e) {
                System.out.println(String.format(SESSION_LOG_OPEN_ERROR, prevSessionLogPath));
            }
            if (prevLog != null) {
                prevLog.restore(simulationManager, log);
            }
        }

        while (true) {
            String[] input = cli.getInput();
            Command command = CommandRegistry.commandForString(input[0]);
            command.execute(input, simulationManager);
            if (log.isInitialized()) {
                log.append(input);
            }
        }
    }
}
