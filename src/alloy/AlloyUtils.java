package alloy;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileWriter;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AlloyUtils {
    public static String getEmptyRelation(int arity) {
        StringBuilder sb = new StringBuilder();
        sb.append(AlloyConstants.NONE);
        for (int i = 1; i < arity; i++) {
            sb.append(String.format(" -> %s", AlloyConstants.NONE));
        }
        return sb.toString();
    }

    public static String getLocallyNamespacedSigName(String sigName) {
        return AlloyConstants.THIS + sigName;
    }

    /**
     * getConcreteSigsDefinition returns Alloy code to define new signatures simulating scope.
     *
     * Complex models may include scopes for Sigs (e.g. in musical chairs, a scope may be 4 Player and
     * 3 Chair). We need to keep track of these scopes throughout the transition system's execution, but
     * Alloy syntax does not allow us to specify states of specific sig instances (for example, we are unable
     * to write Alloy code to say that the fourth player is sitting on the second chair) - this means that we
     * lose information about the execution state after every step, since we cannot encode the new state in the
     * init predicate.
     *
     * To get around the aforementioned issue, we can inject new Sig definitions that are subsigs of the
     * main Sig - we define concrete Sigs that we can easily refer to when defining the state. For example,
     * if the scope of Player in musical chairs is 2, then we should define Player_1 and Player_2 sigs so
     * we can refer to them when updating the init predicate.
     *
     * @param List<String, Integer>
     * @return String
     */
    public static String getConcreteSigsDefinition(Map<String, Integer> sigScopes) {
        String concreteSigNameFormat = "%s_%d";
        String concreteSigsDefinitionFormat = "one sig %s extends %s {}\n";

        StringBuilder concreteSigsSb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : sigScopes.entrySet()) {
            if (entry.getValue() == 0) {
                continue;
            }

            String sigName = entry.getKey();
            if (AlloyConstants.BITWIDTH_SCOPED_SIGS.contains(sigName)) {
                continue;
            }

            StringBuilder sigNamesSb = new StringBuilder();
            sigNamesSb.append(String.format(concreteSigNameFormat, sigName, 0));
            for (int i = 1; i < entry.getValue(); i++) {
                sigNamesSb.append(", ");
                sigNamesSb.append(String.format(concreteSigNameFormat, sigName, i));
            }
            concreteSigsSb.append(String.format(concreteSigsDefinitionFormat, sigNamesSb.toString(), sigName));
        }
        return concreteSigsSb.toString();
    }

    public static String makeStatePredicate(String predicateName, String stateSigName, String contents) {
        String predicate = "\n"
                         + "pred %s[s: %s] {"
                         + "\n"
                         + "%s"
                         + "}"
                         + "\n";
        return String.format(predicate, predicateName, stateSigName, contents);
    }

    public static String annotatedTransitionSystem(String model, ParsingConf parsingConf, int steps) {
        return annotatedTransitionSystem(model, parsingConf, steps, "");
    }

    public static String annotatedTransitionSystemStep(String model, ParsingConf parsingConf, int steps) {
        return annotatedTransitionSystem(model, parsingConf, steps, "path[first]");
    }

    public static String annotatedTransitionSystemUntil(String model, ParsingConf parsingConf, int steps) {
        return annotatedTransitionSystem(model, parsingConf, steps, "break[last]");
    }

    /**
     * getBreakPredicate creates a predicate containing all breakpoints entered
     * by the user.
     * @param List<String>, SigData
     * @return String
     */
    public static String getBreakPredicate(List<String> rawConstraints, SigData sigData) {
        List<String> constraints = new ArrayList<String>();
        for (String rawConstraint : rawConstraints) {
            constraints.add(String.format("(%s)", getConstraint(rawConstraint, sigData.getFields())));
        }

        String constraintsString = String.join(String.format(" %s ", AlloyConstants.OR), constraints);
        String predicateBody = String.format("\t%s\n", constraintsString);

        return makeStatePredicate(
            AlloyConstants.BREAK_PREDICATE_NAME,
            sigData.getLabel(),
            predicateBody
        );
    }

    /**
     * getPathPredicate creates a predicate that conforms to a path specified by
     * the user.
     * @param List<String>, SigData
     * @return String
     */
    public static String getPathPredicate(List<String> rawConstraints, SigData sigData) {
        StringBuilder sb = new StringBuilder();
        StringBuilder stateInstanceBuilder = new StringBuilder("s");
        List<String> auxiliaryPredicates = new ArrayList<String>();

        for (int i = 0; i < rawConstraints.size(); i++) {
            String rawConstraint = rawConstraints.get(i);
            String auxiliaryPredicateName = String.format(AlloyConstants.PATH_AUXILIARY_PREDICATE_FORMAT, i);
            String constraintString = getConstraint(rawConstraint, sigData.getFields());
            String predicateBody = String.format("\t%s\n", constraintString);
            sb.append(makeStatePredicate(
                auxiliaryPredicateName,
                sigData.getLabel(),
                predicateBody
            ));

            stateInstanceBuilder.append(".next");
            auxiliaryPredicates.add(String.format("%s[%s]", auxiliaryPredicateName, stateInstanceBuilder.toString()));
        }

        String constraintsString = String.join(String.format(" %s ", AlloyConstants.AND), auxiliaryPredicates);
        String predicateBody = String.format("\t%s\n", constraintsString);

        sb.append(makeStatePredicate(
            AlloyConstants.PATH_PREDICATE_NAME,
            sigData.getLabel(),
            predicateBody
        ));
        return sb.toString();
    }

    public static void writeToFile(String contents, File file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(contents);
        writer.close();
    }

    public static String readFromFile(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }

    /**
     * getConstraint converts a raw (user-entered) constraint into a format
     * accepted by Alloy. "s." is prepended to all references to a field in the
     * raw constraint. This transformation enables constraints to be wrapped in
     * a predicate that takes a State s as a parameter.
     * @param String, Set<String>
     * @return String
     */
    private static String getConstraint(String rawConstraint, Set<String> fields) {
        if (rawConstraint.trim().isEmpty()) {
            return AlloyConstants.ALWAYS_TRUE;
        }

        StringBuilder constraint = new StringBuilder();
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < rawConstraint.length(); i++) {
            Character c = rawConstraint.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                String field = buffer.toString();
                if (fields.contains(field)) {
                    String stateField = String.format("s.%s", field);
                    constraint.setLength(constraint.length() - field.length());
                    constraint.append(stateField);
                }

                buffer.setLength(0);
            } else {
                buffer.append(c);
            }

            constraint.append(c);
        }

        if (buffer.length() > 0) {
            String field = buffer.toString();
            if (fields.contains(field)) {
                String stateField = String.format("s.%s", field);
                constraint.setLength(constraint.length() - field.length());
                constraint.append(stateField);
            }
        }

        return constraint.toString();
    }

    /**
     * annotatedTransitionSystem generates Alloy code based on the following rules:
     * 1. Use ordering module.
     * 2. Add fact to initialize state.
     * 3. Add fact to define state transitions.
     * 4. Add command to run state transitions.
     */
    private static String annotatedTransitionSystem(String model, ParsingConf parsingConf, int steps, String additionalConstraint) {
        String stateSigName = parsingConf.getStateSigName();
        String initPredicateName = parsingConf.getInitPredicateName();
        String transitionRelationName = parsingConf.getTransitionRelationName();
        Map<String, Integer> additionalSigScopes = parsingConf.getAdditionalSigScopes();
        String additionalConstraintFact = additionalConstraint.trim().isEmpty() ? "" : String.format("fact { %s }" + "\n\n", additionalConstraint);
        String transitionRelationFact = String.format(
            "fact { all s: %s, s': s.next { %s[s, s'] } }" + "\n\n", stateSigName, transitionRelationName
        );
        String sigScopes = String.format("run {  } for exactly %d %s", steps + 1, stateSigName);
        for (String sigScopeName : additionalSigScopes.keySet()) {
            String scopeFormat = (AlloyConstants.BITWIDTH_SCOPED_SIGS.contains(sigScopeName)) ?
                                     ", %d %s" :
                                     ", exactly %d %s";
            sigScopes += String.format(scopeFormat, additionalSigScopes.get(sigScopeName), sigScopeName);
        }
        return String.format(
            String.format("open util/ordering[%s]" + "\n\n", stateSigName) +
            model + "\n\n" +
            String.format("fact { %s[first] }" + "\n\n", initPredicateName) +
            additionalConstraintFact +
            transitionRelationFact +
            sigScopes,
            model
        );
    }
}
