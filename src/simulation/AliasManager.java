package simulation;

import java.util.HashMap;
import java.util.Map;

/**
 * AliasManager manages aliases for formulas which can be used as syntactic shortcuts.
 */
public class AliasManager {
    private Map<String, String> aliases;
    private final static String ALIAS_HEADER = "Alias";
    private final static String FORMULA_HEADER = "Formula";
    private final static String BACKTICK = "`";

    public AliasManager() {
        aliases = new HashMap<>();
    }

    public boolean isAlias(String alias) {
        return aliases.containsKey(alias);
    }

    public boolean addAlias(String alias, String formula) {
        String resolvedFormula = resolveFormula(formula);
        if (resolvedFormula == null || resolvedFormula.isEmpty()) {
            return false;
        }
        aliases.put(alias, resolvedFormula);
        return true;
    }

    public boolean removeAlias(String alias) {
        return aliases.remove(alias) != null;
    }

    public void clearAliases() {
        aliases.clear();
    }

    public String getFormula(String alias) {
        return aliases.get(alias);
    }

    public String getFormattedAliases() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("\n%-16s%s\n", ALIAS_HEADER, FORMULA_HEADER));
        for (String alias : aliases.keySet()) {
            sb.append(String.format("%-16s%s\n", alias, aliases.get(alias)));
        }
        return sb.toString();
    }

    private String resolveFormula(String formula) {
        int l = -1;
        StringBuilder resolved = new StringBuilder();
        for (int i = 0; i < formula.length(); i++) {
            String c = String.valueOf(formula.charAt(i));
            if (c.equals(BACKTICK)) {
                if (l == -1) {
                    l = i;
                } else {
                    String nestedAlias = formula.substring(l + 1, i).trim();
                    l = -1;
                    if (nestedAlias.isEmpty()) {
                        continue;
                    }
                    if (!isAlias(nestedAlias)) {
                        return null;
                    }
                    resolved.append(getFormula(nestedAlias));
                }
            } else if (l == -1) {
                resolved.append(c);
            }
        }
        if (l != -1) {
            return null;
        }
        return resolved.toString();
    }
}
