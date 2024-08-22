package core;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class ImageDisplay extends JFrame {
    private BufferedImage image1;
    private BufferedImage image2;
    private JLabel imageLabel1;
    private JLabel imageLabel2;

    public ImageDisplay() {
        setTitle("Image Display");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Load the images
        loadImages();

        // Create labels to display the images
        imageLabel1 = new JLabel(new ImageIcon(image1));
        

        // Create a panel with a GridLayout to hold the image labels
        JPanel panel = new JPanel(new GridLayout(1, 1));
        panel.add(imageLabel1);
        

        // Add the panel to the JFrame
        add(panel, BorderLayout.CENTER);
    }

    private void loadImages() {
        waitForFinalFiles("state_tree.png", "control_states.png");

        try {
            // Load the final image files
            File file1 = new File("state_tree.png");
            

            if (file1.exists() && file1.isFile()) {
                image1 = ImageIO.read(file1);
            }

            
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1); // Exit if loading fails
        }
    }

    private void waitForFinalFiles(String... filePaths) {
        for (String filePath : filePaths) {
            File finalFile = new File(filePath);
            long startTime = System.currentTimeMillis();
            long timeout = 10000; // 10 seconds

            while (System.currentTimeMillis() - startTime < timeout) {
                if (finalFile.exists() && finalFile.isFile()) {
                    break; // File exists, proceed
                }

                try {
                    Thread.sleep(100); // Check every 100 milliseconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (!finalFile.exists()) {
                System.out.println("Timeout reached: " + filePath + " not found within 10 seconds.");
            }
        }
    }

    public void refresh() {
        new Thread(() -> {
            waitForFinalFiles("state_tree.png", "control_states.png");

            SwingUtilities.invokeLater(() -> {
                loadImages();
                imageLabel1.setIcon(new ImageIcon(image1));
                
            });
        }).start();
    }

    public static void writeImage(BufferedImage image, String path) {
        File tempFile = new File(path + "_temp.png");
        File finalFile = new File(path);

        try {
            // Write to the temporary file first
            ImageIO.write(image, "png", tempFile);

            // Rename to the final file after writing is complete
            if (tempFile.renameTo(finalFile)) {
                System.out.println("File written and renamed successfully.");
            } else {
                System.out.println("Error renaming the file.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
