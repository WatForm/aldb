package core;
import javax.swing.*;

import org.json.JSONObject;

import simulation.SimulationManager;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashGUI {
    private JsonDrawing drawingPanel1;
    private JsonDrawing drawingPanel2;
    private String jsonFilePath1;
    private String jsonFilePath2;
    private SimulationManager simulationManager;

    public DashGUI(String filePath1, String filePath2, SimulationManager s) {
        this.jsonFilePath1 = filePath1;
        this.jsonFilePath2 = filePath2;
        simulationManager = s;
        initUI();
    }

    private void initUI() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Load JSON content
                String content1 = new String(Files.readAllBytes(Paths.get(jsonFilePath1)));
                JSONObject graphData1 = new JSONObject(content1);

                String content2 = new String(Files.readAllBytes(Paths.get(jsonFilePath2)));
                JSONObject graphData2 = new JSONObject(content2);

                // Create and set up the frame
                JFrame frame = new JFrame("Dash Model Viewer");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new GridLayout(1, 2)); // Two panels side by side

                // Create and add the drawing panels
                drawingPanel1 = new JsonDrawing(graphData1,simulationManager);
                drawingPanel2 = new JsonDrawing(graphData2,simulationManager);
                frame.add(drawingPanel1);
                frame.add(drawingPanel2);

                // Set up frame size and visibility
                frame.setSize(1000, 500); // Adjusted to accommodate two panels
                frame.setVisible(true);

            } catch (IOException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error loading JSON file", e);
                JOptionPane.showMessageDialog(null, "Error loading JSON file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // Method to refresh the painting
    public void refreshJson() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Load new JSON content
                String content1 = new String(Files.readAllBytes(Paths.get(jsonFilePath1)));
                JSONObject newGraphData1 = new JSONObject(content1);

                String content2 = new String(Files.readAllBytes(Paths.get(jsonFilePath2)));
                JSONObject newGraphData2 = new JSONObject(content2);

                SwingUtilities.invokeLater(() -> {
                    drawingPanel1.updateGraphData(newGraphData1);
                    drawingPanel1.repaint();

                    drawingPanel2.updateGraphData(newGraphData2);
                    drawingPanel2.repaint();
                });

            } catch (IOException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error loading new JSON file", e);
                JOptionPane.showMessageDialog(null, "Error loading new JSON file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public void refreshJsonWithDelay(int delayMillis) {
        Timer timer = new Timer(delayMillis, e -> {
            try {
                // Load new JSON content
                String content1 = new String(Files.readAllBytes(Paths.get(jsonFilePath1)));
                JSONObject newGraphData1 = new JSONObject(content1);

                String content2 = new String(Files.readAllBytes(Paths.get(jsonFilePath2)));
                JSONObject newGraphData2 = new JSONObject(content2);

                SwingUtilities.invokeLater(() -> {
                    drawingPanel1.updateGraphData(newGraphData1);
                    drawingPanel1.repaint();

                    drawingPanel2.updateGraphData(newGraphData2);
                    drawingPanel2.repaint();
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
