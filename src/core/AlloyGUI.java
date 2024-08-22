package core;
import javax.swing.*;

import org.json.JSONObject;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import simulation.SimulationManager;

public class AlloyGUI {
    private JsonDrawing drawingPanel;
    private String jsonFilePath;
    private SimulationManager simulationManager;
    
    public AlloyGUI(String filePath,SimulationManager s) {
    	simulationManager = s;
        this.jsonFilePath = filePath;
        initUI();
    }

    private void initUI() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Load JSON content
                String content = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
                JSONObject graphData = new JSONObject(content);

                // Create and set up the frame
                JFrame frame = new JFrame("JSON Viewer");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new BorderLayout());

                // Create and add the drawing panel
                drawingPanel = new JsonDrawing(graphData,simulationManager);
                frame.add(drawingPanel, BorderLayout.CENTER);

                // Set up frame size and visibility
                frame.setSize(1000, 1000);
                frame.setVisible(true);

            } catch (IOException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error loading JSON file", e);
                JOptionPane.showMessageDialog( null, "Error loading JSON file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // Method to refresh the painting
    public void refreshJson() {
        
        SwingUtilities.invokeLater(() -> {
            try {
                // Load new JSON content
                String content = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
                JSONObject newGraphData = new JSONObject(content);

                
                SwingUtilities.invokeLater(() -> {
                    drawingPanel.updateGraphData(newGraphData);
                    drawingPanel.repaint();
                });

            } catch (IOException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error loading new JSON file", e);
                JOptionPane.showMessageDialog(null, "Error loading new JSON file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    public void refreshJsonWithDelay(int delayMillis) {
    	System.out.println("refreshing...");
        Timer timer = new Timer(delayMillis, e -> {
        	try {
                // Load new JSON content
                String content = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
                JSONObject newGraphData = new JSONObject(content);

                SwingUtilities.invokeLater(() -> {
                    drawingPanel.updateGraphData(newGraphData);
                    drawingPanel.repaint();
                });

            } catch (IOException e1) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error loading new JSON file", e1);
                JOptionPane.showMessageDialog(null, "Error loading new JSON file: " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        timer.setRepeats(false); // Ensures the Timer runs only once
        timer.start(); // Start the timer
    }
}



    

