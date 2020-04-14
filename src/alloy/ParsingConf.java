package alloy;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.util.HashMap;
import java.util.Map;

public class ParsingConf {
    private static final String STATE_SIG_NAME_DEFAULT = "State";
    private static final String INIT_PREDICATE_NAME_DEFAULT = "init";
    private static final String TRANSITION_RELATION_NAME_DEFAULT = "next";

    // Important keywords that are parsed/observed in the comments of an Alloy model file for configuration.
    public final static String BEGIN_ALDB_CONF = "BEGIN_ALDB_CONF";
    public final static String END_ALDB_CONF = "END_ALDB_CONF";
    private final static String ESCAPED_CHARACTERS = "[/*]";

    // Name of the sig representing the main state in the Alloy model.
    private String stateSigName;
    // Name of the predicate which defines the initial state in the Alloy model.
    private String initPredicateName;
    // Name of the transition relation in the Alloy model.
    private String transitionRelationName;
    // Additional Alloy sig scopes to specify.
    private Map<String, Integer> additionalSigScopes;

    public ParsingConf() {
        stateSigName = STATE_SIG_NAME_DEFAULT;
        initPredicateName = INIT_PREDICATE_NAME_DEFAULT;
        transitionRelationName = TRANSITION_RELATION_NAME_DEFAULT;
        additionalSigScopes = new HashMap<>();
    }

    public static ParsingConf initializeWithYaml(String file) {
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        Yaml yaml = new Yaml(new Constructor(ParsingConf.class), representer);
        return yaml.loadAs(file, ParsingConf.class);
    }

    public void setStateSigName(String stateSigName) {
        this.stateSigName = stateSigName;
    }

    public String getStateSigName() {
        return stateSigName;
    }

    public void setInitPredicateName(String initPredicateName) {
        this.initPredicateName = initPredicateName;
    }

    public String getInitPredicateName() {
        return initPredicateName;
    }

    public void setTransitionRelationName(String transitionRelationName) {
        this.transitionRelationName = transitionRelationName;
    }

    public String getTransitionRelationName() {
        return transitionRelationName;
    }

    public void setAdditionalSigScopes(Map<String, Integer> additionalSigScopes) {
        this.additionalSigScopes = additionalSigScopes;
    }

    public Map<String, Integer> getAdditionalSigScopes() {
        return additionalSigScopes;
    }

    public static String getConfStringFromFileString(String file) {
        String config = "";
        int fileStartIdx = file.indexOf(BEGIN_ALDB_CONF);
        int fileEndIdx = file.indexOf(END_ALDB_CONF);

        if (fileStartIdx == -1 || fileEndIdx == -1) return config;

        config = file.substring(fileStartIdx + BEGIN_ALDB_CONF.length(), fileEndIdx-1).replaceAll(ESCAPED_CHARACTERS, "");
        return config;
    }
}
