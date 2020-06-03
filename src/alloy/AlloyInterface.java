package alloy;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.XMLNode;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Sig.Field;
import edu.mit.csail.sdg.parser.CompUtil;
import edu.mit.csail.sdg.parser.CompModule;
import edu.mit.csail.sdg.translator.A4Options;
import edu.mit.csail.sdg.translator.A4Solution;
import edu.mit.csail.sdg.translator.A4SolutionReader;
import edu.mit.csail.sdg.translator.A4TupleSet;
import edu.mit.csail.sdg.translator.TranslateAlloyToKodkod;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AlloyInterface {
    private static final A4Reporter reporter = new A4Reporter();
    private static final A4Options options = new A4Options();

    public static CompModule compile(String modelPath) throws Err {
        return CompUtil.parseEverything_fromFile(reporter, null, modelPath);
    }

    public static A4Solution run(CompModule module) throws Err {
        List<Command> commands = module.getAllCommands();

        // Use the command injected by us at the end of the input
        // model. This ensures any extraneous commands in the input model are
        // not run.
        Command command = commands.get(commands.size() - 1);
        return TranslateAlloyToKodkod.execute_command(
          reporter, module.getAllReachableSigs(), command, options);
    }

    /**
     * solutionFromXMLFile reads an Alloy XML file and returns an A4Solution.
     * @param File
     * @return A4Solution
     * @throws Exception if reading the file or converting it to an A4Solution failed.
     */
    public static A4Solution solutionFromXMLFile(File file) throws Exception {
        return A4SolutionReader.read(new ArrayList<Sig>(), new XMLNode(file));
    }

    public static Sig getSigFromA4Solution(A4Solution sol, String sigName) {
        for (Sig sig : sol.getAllReachableSigs()) {
            if (sig.toString().equals(AlloyUtils.getLocallyNamespacedSigName(sigName))) {
                return sig;
            }
        }

        return null;
    }
}
