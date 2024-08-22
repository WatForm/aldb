package simulation;
import java.io.FileOutputStream;
import java.io.IOException;
 
public class GraphPrinter {
 
    private StringBuilder graphBuilder = new StringBuilder();
    private String filePrefix;
 
    public GraphPrinter() {}
 
    /**
     * @param filePrefix Only put filename prefix E.g. File1, File2, MyNewFile, myfile etc. It will use the filePrefix to create output dot file and png file with same name.
     */
    public GraphPrinter(String filePrefix) {
        this.filePrefix = filePrefix;
    }
 
    /**
     * @param line  line contains a valid dot file graphviz text
     */
    public void addln(String line) {
        graphBuilder.append(line).append("\n");
    }
 
    public void add(String text) {
        graphBuilder.append(text);
    }
 
    public void addnewln() {
        graphBuilder.append("\n");
    }
 
    /**
     * Creates an output dot file and uses that to create graphviz png output using following command
     * dot -Tpng <filePrefix>.dot -o <filePrefix>.png
     * If you want to change the certain format, change below.
     */
    public void print(String prefix) {
        try {
        	filePrefix = prefix;
        	if (prefix.equals("control_states")) {
        		graphBuilder.insert(0, "compound=true \n");
            }
            graphBuilder.insert(0, "digraph G {").append("\n");
           
            graphBuilder.append("}").append("\n");
            writeTextToFile(filePrefix + ".dot", graphBuilder.toString());
 
            StringBuilder command = new StringBuilder();
            command.append("dot -Tpng ").    // output type
                    append(filePrefix).append(".dot ").   // input dot file
                    append("-o ").append(filePrefix).append(".png");  // output image
 
            executeCommand(command.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    public void generateJson(String prefix) {
    	//System.out.println("generating Json...");
        try {
        	filePrefix = prefix;
            
            StringBuilder command = new StringBuilder();
            command.append("dot -Tjson ").    // output type
                    append(filePrefix).append(".dot ").   // input dot file
                    append("-o ").append(filePrefix).append(".json");  // output json
            //System.out.println(command.toString());
            executeCommand(command.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    private void executeCommand(String command) throws Exception {
        Process process = Runtime.getRuntime().exec(command);
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Command execution failed with exit code: " + exitCode);
        }
    }

 
    private void writeTextToFile(String fileName, String text) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(fileName);
        outputStream.write(text.getBytes());
        outputStream.close();
    }
}