package simulation;

import java.util.HashMap;
import java.util.Map;

/**
 * AliasManager manages aliases for predicates which can be used as syntactic shortcuts.
 */
public class AliasManager {
    private Map<String, String> aliases;
    private final static String ALIAS_HEADER = "Alias";
    private final static String PREDICATE_HEADER = "Predicate";

    public AliasManager() {
        aliases = new HashMap<>();
    }

    public boolean isAlias(String alias) {
        return aliases.containsKey(alias);
    }

    public void addAlias(String alias, String predicate) {
        aliases.put(alias, predicate);
    }

    public boolean removeAlias(String alias) {
        return aliases.remove(alias) != null;
    }

    public void clearAliases() {
        aliases.clear();
    }

    public String getPredicate(String alias) {
        return aliases.get(alias);
    }

    public String getFormattedAliases() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("\n%-16s%s\n", ALIAS_HEADER, PREDICATE_HEADER));
        for (String alias : aliases.keySet()) {
            sb.append(String.format("%-16s%s\n", alias, aliases.get(alias)));
        }
        return sb.toString();
    }
}
