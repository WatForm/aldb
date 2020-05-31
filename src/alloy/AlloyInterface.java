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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
     * solutionFromXMLFile reads an Alloy XML file and returns an A4Solution
     * using the Alloy API. The root model and all included files are obtained
     * from the XML file and saved to disk, as required by the API.
     * @param File
     * @return A4Solution
     */
    public static A4Solution solutionFromXMLFile(File file) throws Exception {
        Path tempDir = Files.createTempDirectory("aldb");

        XMLNode root = new XMLNode(file);

        // The first source file is the root module.
        String alloySourceFilename = root.getChildren(AlloyConstants.TRACE_SOURCE_TAG)
                                         .iterator()
                                         .next()
                                         .getAttribute(AlloyConstants.TRACE_FILENAME_ATTR);

        for (XMLNode node : root) {
            if (node.is(AlloyConstants.TRACE_SOURCE_TAG)) {
                String sourceFilename = node.getAttribute(AlloyConstants.TRACE_FILENAME_ATTR);
                Path sourceFilenamePath = Paths.get(sourceFilename);
                Path mainFilePath = Paths.get(alloySourceFilename).getParent();
                sourceFilenamePath = mainFilePath.relativize(sourceFilenamePath);

                // Don't include Alloy standard library files.
                if (sourceFilenamePath.startsWith("../")) {
                    continue;
                }

                File outFile = tempDir.resolve(sourceFilenamePath).toFile();
                if (outFile.getParentFile() != null) {
                    outFile.getParentFile().mkdirs();
                }

                String sourceFileContents = node.getAttribute(AlloyConstants.TRACE_CONTENT_ATTR);
                AlloyUtils.writeToFile(sourceFileContents, outFile);
            }
        }

        alloySourceFilename = tempDir.resolve(Paths.get(alloySourceFilename).getFileName()).toString();

        // Parse from a file rather than a string in order to support includes.
        CompModule module = CompUtil.parseEverything_fromFile(reporter, null, alloySourceFilename);

        return A4SolutionReader.read(module.getAllReachableSigs(), root);
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
