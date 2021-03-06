package alloy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;

public class AlloyConstants {
    public static final String ALLOY_ATOM_SEPARATOR = "\\$";
    public static final String ALWAYS_TRUE = "none = none";
    public static final String AND = "and";
    public static final String BLOCK_INITIALIZER = "{";
    public static final String BLOCK_TERMINATOR = "}";
    public static final String INT = "Int";
    public static final String NONE = "none";
    public static final String PATH_AUXILIARY_PREDICATE_FORMAT = "state_s%d";
    public static final String PATH_PREDICATE_NAME = "path";
    public static final String PLUS = "+";
    public static final String UNDERSCORE = "_";
    public static final String SET_DELIMITER = "->";
    public static final String SEQ = "seq";
    public static final String VALUE_SUFFIX = "$0";
    public static final String THIS = "this/";
    public static final String UNIV = "univ";
    public static final String CONCRETE_SIG_REGEX = "(.*)_(\\d+)";
    public static final String OR = "or";
    public static final String BREAK_PREDICATE_NAME = "break";

    public static final Set<String> BITWIDTH_SCOPED_SIGS =
        Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(INT, SEQ)));
}
