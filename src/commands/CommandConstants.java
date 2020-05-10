package commands;

public class CommandConstants {
    public final static String DONE = "done.";
    public final static String READING_MODEL = "Reading model from %s...";
    public final static String READING_TRACE = "Reading trace from %s...";
    public final static String WRITING_DOT_GRAPH = "Writing DOT graph to %s...";
    public final static String NO_SUCH_FILE = "%s: No such file.\n";
    public final static String NO_FILE_SPECIFIED = "Error. No file specified.";
    public final static String IO_FAILED = "error. I/O failed.";
    public final static String FAILED_TO_READ_FILE = "error. Failed to read file.";
    public final static String FAILED_TO_READ_CONF = "error. Invalid configuration.";
    public final static String INVALID_TRACE = "error. Invalid trace.";
    public final static String TMP_FILE_ERROR = "internal error. Failed to create temporary Alloy file.";
    public final static String SETTING_PARSING_OPTIONS = "Setting default parsing options...";
    public final static String SETTING_PARSING_OPTIONS_FROM = "Setting parsing options from %s...";
    public final static String UNDEFINED_COMMAND = "Undefined command: \"%s\". Try \"help\".\n";
    public final static String AVAILABLE_COMMANDS_STR = "Available commands:\n\n";
    public final static String COMMAND_HELP_DELIMITER = "%-15s-- %s\n";
    public final static String INTEGER_ERROR = "Error. Input must be an integer.";
    public final static String GR_ONE_ERROR = "Error. Input must be >= 1.";
    public final static String SIG_NOT_FOUND = "Signature not found.";
    public final static String ALT_UNAVAILABLE = "No alternate execution paths.";
    public final static String UNTIL_FAILED = "Unable to find satisfying solution.";
    public final static String NO_MODEL_LOADED = "No model file specified.\nUse the \"load\" command.";

    public final static String ALIAS_NAME = "alias";
    public final static String ALIAS_DESCRIPTION = "Control the set of aliases used";
    public final static String ALIAS_HELP = "Control the set of predicate aliases used.\n\n" +
            "Usage:\n\n" +
            "alias <alias> <predicate>  -- Add an alias\n" +
            "alias -rm <alias>          -- Remove an alias\n" +
            "alias -l                   -- List all aliases\n" +
            "alias -c                   -- Clear aliases";
    public final static String ALIAS_DNE = "Alias \"%s\" does not exist.";
    public final static String[] ALIAS_SHORTHAND = {};

    public final static String CURRENT_NAME = "current";
    public final static String CURRENT_DESCRIPTION = "Display the current state";
    public final static String CURRENT_HELP = "Display the current state.\n\n" +
        "Usage: current [property]\n\n" +
        "By default, all properties are printed.";
    public final static String[] CURRENT_SHORTHAND = {"c", "curr"};

    public final static String HELP_NAME = "help";
    public final static String HELP_DESCRIPTION = "Display the list of available commands";
    public final static String[] HELP_SHORTHAND = {"h"};
    public final static String HELP_COMMAND_END_STR = "\nType \"help\" followed by a command name for full documentation.";

    public final static String LOAD_NAME = "load";
    public final static String LOAD_DESCRIPTION = "Load an Alloy model";
    public final static String LOAD_HELP = "Load an Alloy model.\n\n" +
        "Usage: load <filename>\n\n" +
        "The specified file must be an Alloy (.als) file.\n\n" +
        "You can also specify custom parsing options for this Alloy model as a comment.\n" +
        "The comment block needs to have a header as: BEGIN_ALDB_CONF and a footer as: END_ALDB_CONF.\n" +
        "The configuration format follows the YAML format for the setconf command.";
    public final static String[] LOAD_SHORTHAND = {"l", "ld"};

    public final static String QUIT_NAME = "quit";
    public final static String QUIT_DESCRIPTION = "Exit ALDB";
    public final static String QUIT_HELP = "Exit ALDB\n\n" +
        "Confirmation is required when attempting to quit during an active debugging session.";
    public final static String[] QUIT_SHORTHAND = {"q", "exit"};
    public final static String QUIT_USER_PROMPT = "A debugging session is active.\nQuit anyway? (y or n) ";
    public final static String[] QUIT_ACCEPTED_RESPONSES = {"y", "Y", "yes", "Yes"};

    public final static String[] REVERSE_STEP_SHORTHAND = {"rs", "r"};
    public final static String REVERSE_STEP_NAME = "reverse-step";
    public final static String REVERSE_STEP_DESCRIPTION = "Go back n steps in the current state traversal path";
    public final static String REVERSE_STEP_HELP = "Go back n steps in the current state traversal path.\n\n" +
        "Usage: reverse-step [n]\n\n" +
        "n is an integer >= 1. By default, n = 1.";

    public final static String SETCONF_NAME = "setconf";
    public final static String SETCONF_DESCRIPTION = "Set parsing options for the current session";
    public final static String SETCONF_HELP = "Set custom parsing options for the current session.\n\n" +
        "Usage: setconf [filename]\n\n" +
        "The specified file must be in YAML. The following (customizable) properties are set by default:\n\n" +
        "# Name of the sig representing the main state in the Alloy model.\n" +
        "stateSigName: State\n" +
        "# Name of the predicate which defines the initial state in the Alloy model.\n" +
        "initPredicateName: init\n" +
        "# Name of the transition relation in the Alloy model.\n" +
        "transitionRelationName: next\n" +
        "# Additional Alloy sig scopes to specify.\n" +
        "additionalSigScopes: {}\n\n" +
        "Running setconf with no filename will set the above default options.";

    public final static String STEP_NAME = "step";
    public final static String STEP_DESCRIPTION = "Perform a state transition of n steps";
    public final static String STEP_HELP = "Perform a state transition of n steps.\n\n" +
        "Usage: step [n | constraints]\n\n" +
        "n must be an integer >= 1. By default, n = 1.\n\n" +
        "A list of constraints may also be specified, as a comma-separated list enclosed by square brackets.\n" +
        "n is equal to the number of items in the list.\n" +
        "The i-th constraint is applied when performing the i-th transition.";
    public final static String[] STEP_SHORTHAND = {"s", "st"};

    public final static String TRACE_NAME = "trace";
    public final static String TRACE_DESCRIPTION = "Load a saved Alloy XML instance";
    public final static String TRACE_HELP = "Load a saved Alloy XML instance.\n\n" +
        "Usage: trace <filename>\n\n" +
        "The specified file should be in the Alloy XML format.";
    public final static String[] TRACE_SHORTHAND = {"t"};

    public final static String HISTORY_NAME = "history";
    public final static String HISTORY_DESCRIPTION = "Display past states";
    public final static String HISTORY_HELP = "Display the past n consecutive states of the active execution path.\n\n" +
        "Usage: history [n]\n\n" +
        "n must be an integer >= 1. By default, n = 3.";

    public final static String SCOPE_NAME = "scope";
    public final static String SCOPE_DESCRIPTION = "Display scope set";
    public final static String SCOPE_HELP = "Display the scope set of the active model.\n\n" +
        "Usage: scope [sig-name]\n\n" +
        "By default, scope sets are displayed for all signatures in the model.";

    public final static String ALT_NAME = "alt";
    public final static String ALT_DESCRIPTION = "Select an alternate execution path";
    public final static String ALT_HELP = "Select an alternate execution path.\n\n" +
        "Usage: alt [-r]\n\n" +
        "If \"-r\" is specified, the previous execution path is selected.";
    public final static String[] ALT_SHORTHAND = {"a"};

    public final static String BREAK_NAME = "break";
    public final static String BREAK_DESCRIPTION = "Control the set of constraints used";
    public final static String BREAK_HELP = "Control the set of constraints used.\n\n" +
        "Usage:\n\n" +
        "break <constraint>  -- Add a constraint\n" +
        "break -rm <num>     -- Remove a constraint\n" +
        "break -l            -- List all constraints\n" +
        "break -c            -- Clear constraints";
    public final static String CONSTRAINT_REGEX = "([^\"]\\S*|\".+?\")\\s*";
    public final static String INVALID_CONSTRAINT_ID = "No constraint number %d.";
    public final static String INVALID_CONSTRAINT = "Invalid constraint: \"%s\".";
    public final static String[] BREAK_SHORTHAND = {"b"};

    public final static String UNTIL_NAME = "until";
    public final static String UNTIL_DESCRIPTION = "Run until constraints are met";
    public final static String UNTIL_HELP = "Run until constraints are met.\n\n" +
        "Usage: until [limit]\n\n" +
        "limit must be an integer >= 1. By default, limit = 10.";
    public final static String[] UNTIL_SHORTHAND = {"u"};

    public final static String DOT_NAME = "dot";
    public final static String DOT_DESCRIPTION = "Dump DOT graph to disk";
    public final static String DOT_HELP = "Dump DOT graph to the current working directory.\n\nUsage: dot";
    public final static String[] DOT_SHORTHAND = {"d"};
}
