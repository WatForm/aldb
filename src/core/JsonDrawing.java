
package core;

import org.json.JSONArray;
import org.json.JSONObject;

import simulation.SimulationManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonDrawing extends JPanel {
    private JSONArray objects;
    private List<ShapeInfo> shapeList = new ArrayList<>();
    private Map<ShapeInfo, String> shapeLabelMap = new LinkedHashMap<>();

    private ShapeInfo hoveredShape = null;
    private Integer graphWidth;
    private JSONArray edges;

    private boolean isDrawingVisible = true;
    private double zoomFactor = 1.0;
    private boolean isDragging = false;
    private int lastX, lastY;
    private SimulationManager simulationManager;

    public JsonDrawing(JSONObject graphData,SimulationManager sm) {
    	simulationManager = sm;
    	if (graphData.has("objects")) {
	        objects = graphData.getJSONArray("objects");
	        JSONArray graphInfo = graphData.getJSONArray("_draw_");
	
	        JSONObject s = graphInfo.getJSONObject(2);
	        JSONArray points = s.getJSONArray("points");
	        JSONArray secondPoint = points.getJSONArray(1);
	        graphWidth = (int) secondPoint.getDouble(1);
	        if (graphData.has("edges")) {
	            edges = graphData.getJSONArray("edges");
        }
    	}
        // Add mouse listener for click and hover events
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    isDragging = true;
                    isDrawingVisible = false;
                    lastX = e.getX();
                    lastY = e.getY();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    isDragging = false;
                    isDrawingVisible = true;
                    setCursor(Cursor.getDefaultCursor());
                    repaint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
            	Point2D.Double adjustedClickPoint = new Point2D.Double(
                        e.getX() / zoomFactor,
                        e.getY() / zoomFactor
                    );
                if (SwingUtilities.isLeftMouseButton(e) && !isDragging) {
                    //printShapeLabelMap();
                    
                    // Iterate over shapeList in reverse order to find the topmost shape
                    for (int i = shapeList.size() - 1; i >= 0; i--) {
                        ShapeInfo shapeInfo = shapeList.get(i);
                        if (shapeInfo.shape.contains(adjustedClickPoint)) {
                            String label = shapeLabelMap.get(shapeInfo);
                            JOptionPane.showMessageDialog(JsonDrawing.this, "Shape clicked: " + shapeInfo + ", Label: " + label);
                            simulationManager.handleClick(label);
                            break; // Stop after finding the topmost shape
                        }
                    }
                }
            }


        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean repaintNeeded = false;
                for (ShapeInfo shapeInfo : shapeList) {
                    if (shapeInfo.shape.contains(e.getPoint())) {
                        if (hoveredShape != shapeInfo) {
                            hoveredShape = shapeInfo;
                            repaintNeeded = true;
                        }
                    } else if (hoveredShape == shapeInfo) {
                        hoveredShape = null;
                        repaintNeeded = true;
                    }
                }
                if (repaintNeeded) {
                    repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    int dx = e.getX() - lastX;
                    int dy = e.getY() - lastY;
                    lastX = e.getX();
                    lastY = e.getY();
                    setLocation(getX() + dx, getY() + dy);
                    repaint();  // Continuously repaint as the mouse is dragging
                }
            }
        });

        // Add mouse wheel listener for zooming
        addMouseWheelListener(e -> {
            if (e.getWheelRotation() < 0) {
                zoomIn();
            } else {
                zoomOut();
            }
        });
    }

    private void zoomIn() {
        zoomFactor += 0.1;
        repaint();
    }

    private void zoomOut() {
        zoomFactor -= 0.1;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (isDrawingVisible) {
            shapeList.clear();
            shapeLabelMap.clear();  // Clear the map before adding new entries
            g2d.scale(zoomFactor, zoomFactor);

            for (int i = 0; i < objects.length(); i++) {
                JSONObject s = objects.getJSONObject(i);
                String fillcolor = "";
                if (s.has("fillcolor")) {
                    fillcolor = s.getString("fillcolor");
                }
                if (s.has("_draw_")) {
                    JSONArray shapeInfoArray = s.getJSONArray("_draw_");
                    drawShapes(g2d, shapeInfoArray, fillcolor);
                }
                if (s.has("_ldraw_")) {
                    JSONArray labelInfoArray = s.getJSONArray("_ldraw_");
                    drawLabels(g2d, labelInfoArray);
                }
            }
            if (edges != null) {
                for (int i = 0; i < edges.length(); i++) {
                    JSONObject s = edges.getJSONObject(i);
                    String fillcolor = "";
                    if (s.has("fillcolor")) {
                        fillcolor = s.getString("fillcolor");
                    }
                    if (s.has("_draw_")) {
                        JSONArray shapeInfoArray = s.getJSONArray("_draw_");
                        drawShapes(g2d, shapeInfoArray, fillcolor);
                    }
                    if (s.has("_hdraw_")) {
                        JSONArray headInfoArray = s.getJSONArray("_hdraw_");
                        drawShapes(g2d, headInfoArray, fillcolor);
                    }
                    if (s.has("_ldraw_")) {
                        JSONArray labelInfoArray = s.getJSONArray("_ldraw_");
                        drawLabels(g2d, labelInfoArray);
                    }
                }
            }
        }
    }

    private void drawShapes(Graphics2D g2d, JSONArray shapeInfoArray, String fillcolor) {
        for (int i = 0; i < shapeInfoArray.length(); i++) {
            JSONObject jsonObject = shapeInfoArray.getJSONObject(i);
            String op = jsonObject.getString("op");

            switch (op) {
                case "C":
                    break;
                case "c":
                    // Handle color or other properties
                    if (jsonObject.has("color")) {
                        Color color = Color.decode(jsonObject.getString("color"));
                        g2d.setColor(color);
                    }
                    break;
                case "e":
                    // Draw ellipse
                    if (jsonObject.has("rect")) {
                        JSONArray rectArray = jsonObject.getJSONArray("rect");
                        int x = rectArray.getInt(0);
                        int y = graphWidth - rectArray.getInt(1);
                        int width = rectArray.getInt(2);
                        int height = rectArray.getInt(3);

                        // Convert centroid to upper-left corner
                        int upperLeftX = x - (width / 2);
                        int upperLeftY = y - (height / 2);
                        Ellipse2D ellipse = new Ellipse2D.Double(upperLeftX, upperLeftY, width, height);
                        ShapeInfo shapeInfo = new ShapeInfo(ellipse, g2d.getColor());
                        shapeList.add(shapeInfo);
                        shapeLabelMap.put(shapeInfo, null); // Assume null label for now

                        // Add shading effect if hovered
                        if (hoveredShape != null && hoveredShape.shape.equals(ellipse)) {
                            g2d.setColor(hoveredShape.color.darker());
                        }
                        g2d.draw(ellipse);
                        if (fillcolor.equals("yellow")) {
                            g2d.setColor(Color.YELLOW);
                            g2d.fill(ellipse);
                        }
                        g2d.setColor(hoveredShape != null && hoveredShape.shape.equals(ellipse) ? hoveredShape.color : g2d.getColor());
                    }
                    break;
                case "p":
                    // Draw polygon
                    if (jsonObject.has("points")) {
                        JSONArray pointArray = jsonObject.getJSONArray("points");
                        Polygon polygon = new Polygon();
                        for (int j = 0; j < pointArray.length(); j++) {
                            JSONArray point = pointArray.getJSONArray(j);
                            int x = point.getInt(0);
                            int y = graphWidth - point.getInt(1);
                            polygon.addPoint(x, y);
                        }
                        ShapeInfo shapeInfo = new ShapeInfo(polygon, g2d.getColor());
                        shapeList.add(shapeInfo);
                        shapeLabelMap.put(shapeInfo, null); // Assume null label for now

                        if (hoveredShape != null && hoveredShape.shape.equals(polygon)) {
                            g2d.setColor(Color.GREEN);
                        }
                        g2d.draw(polygon);
                        if (fillcolor.equals("yellow")) {
                            g2d.setColor(Color.YELLOW);
                            g2d.fill(polygon);
                        }
                    }
                    break;
                case "b":
                    // Draw path
                    if (jsonObject.has("points")) {
                        JSONArray pointArray = jsonObject.getJSONArray("points");
                        Path2D path = new Path2D.Double();
                        boolean firstPoint = true;
                        for (int j = 0; j < pointArray.length(); j++) {
                            JSONArray point = pointArray.getJSONArray(j);
                            double x = point.getDouble(0);
                            double y = graphWidth - point.getDouble(1);
                            if (firstPoint) {
                                path.moveTo(x, y);
                                firstPoint = false;
                            } else {
                                path.lineTo(x, y);
                            }
                        }
                        ShapeInfo shapeInfo = new ShapeInfo(path, g2d.getColor());
                        shapeList.add(shapeInfo);
                        shapeLabelMap.put(shapeInfo, null); // Assume null label for now
                        g2d.draw(path);
                    }
                    break;
                case "P":
                    // Draw polygon
                    if (jsonObject.has("points")) {
                        JSONArray pointArray = jsonObject.getJSONArray("points");
                        if (pointArray.length() == 4) { // rectangle
                            Polygon rectangle = new Polygon();
                            int x1 = pointArray.getJSONArray(0).getInt(0);
                            int x2 = pointArray.getJSONArray(1).getInt(0);
                            int x3 = pointArray.getJSONArray(2).getInt(0);
                            int x4 = pointArray.getJSONArray(3).getInt(0);
                            int y1 = graphWidth - pointArray.getJSONArray(0).getInt(1);
                            int y2 = graphWidth - pointArray.getJSONArray(1).getInt(1);
                            int y3 = graphWidth - pointArray.getJSONArray(2).getInt(1);
                            int y4 = graphWidth - pointArray.getJSONArray(3).getInt(1);
                            rectangle.addPoint(x1, y1);
                            rectangle.addPoint(x2, y2);
                            rectangle.addPoint(x3, y3);
                            rectangle.addPoint(x4, y4);

                            ShapeInfo shapeInfo = new ShapeInfo(rectangle, g2d.getColor());
                            shapeList.add(shapeInfo);
                            shapeLabelMap.put(shapeInfo, null); // Assume null label for now
                            
                            g2d.draw(rectangle);
                            if (fillcolor.equals("yellow")) {
                                g2d.setColor(Color.YELLOW);
                                g2d.fill(rectangle);
                            }
                        }

                        if (pointArray.length() == 3) { // triangle, head of an edge
                            Polygon triangle = new Polygon();
                            int x1 = pointArray.getJSONArray(0).getInt(0);
                            int x2 = pointArray.getJSONArray(1).getInt(0);
                            int x3 = pointArray.getJSONArray(2).getInt(0);
                            int y1 = graphWidth - pointArray.getJSONArray(0).getInt(1);
                            int y2 = graphWidth - pointArray.getJSONArray(1).getInt(1);
                            int y3 = graphWidth - pointArray.getJSONArray(2).getInt(1);
                            triangle.addPoint(x1, y1);
                            triangle.addPoint(x2, y2);
                            triangle.addPoint(x3, y3);
                            g2d.fill(triangle);
                        }
                    }
                    break;
                case "S":
                    break;
                case "E":
                    if (jsonObject.has("rect")) {
                        JSONArray rectArray = jsonObject.getJSONArray("rect");
                        int x = rectArray.getInt(0);
                        int y = graphWidth - rectArray.getInt(1);
                        int width = rectArray.getInt(2);
                        int height = rectArray.getInt(3);

                        // Convert centroid to upper-left corner
                        int upperLeftX = x - (width / 2);
                        int upperLeftY = y - (height / 2);
                        Ellipse2D ellipse = new Ellipse2D.Double(upperLeftX, upperLeftY, width, height);
                        ShapeInfo shapeInfo = new ShapeInfo(ellipse, g2d.getColor());
                        shapeList.add(shapeInfo);
                        shapeLabelMap.put(shapeInfo, null); // Assume null label for now

                        // Add shading effect if hovered
                        if (hoveredShape != null && hoveredShape.shape.equals(ellipse)) {
                            g2d.setColor(hoveredShape.color.darker());
                        }
                        g2d.draw(ellipse);
                        if (fillcolor.equals("yellow")) {
                            g2d.setColor(Color.YELLOW);
                            g2d.fill(ellipse);
                        }
                        g2d.setColor(hoveredShape != null && hoveredShape.shape.equals(ellipse) ? hoveredShape.color : g2d.getColor());
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported operation: " + op);
            }
        }
    }

    private void drawLabels(Graphics2D g2d, JSONArray labelInfoArray) {
        for (int i = 0; i < labelInfoArray.length(); i++) {
            JSONObject jsonObject = labelInfoArray.getJSONObject(i);
            String op = jsonObject.getString("op");

            switch (op) {
                case "F":
                    // Set font properties
                    if (jsonObject.has("face") && jsonObject.has("size")) {
                        String face = jsonObject.getString("face");
                        int size = jsonObject.getInt("size");
                        g2d.setFont(new Font(face, Font.PLAIN, size));
                    }
                    break;
                case "c":
                    // Handle color or other properties
                    if (jsonObject.has("color")) {
                        Color color = Color.decode(jsonObject.getString("color"));
                        g2d.setColor(color);
                    }
                    break;
                case "T":
                    // Draw text
                    if (jsonObject.has("pt") && jsonObject.has("text")) {
                        JSONArray ptArray = jsonObject.getJSONArray("pt");
                        int x = ptArray.getInt(0);
                        int y = ptArray.getInt(1);
                        String text = jsonObject.getString("text");
                        FontMetrics metrics = g2d.getFontMetrics(g2d.getFont());
                        int textWidth = metrics.stringWidth(text);
                        int textHeight = metrics.getHeight();
                        int textAscent = metrics.getAscent();

                        // Calculate the new x and y coordinates
                        int adjustedX = x - textWidth / 2;
                        int adjustedY = y + textAscent / 2 - textHeight / 2;
                        
                        
                        // Draw the string at the adjusted coordinates
                        g2d.drawString(text, adjustedX, graphWidth - adjustedY);
                        
                        // Update the label for the corresponding shape
                        updateLastValue(shapeLabelMap,text);
                        
                        Rectangle textBox = new Rectangle(adjustedX, graphWidth- adjustedY - textAscent, textWidth, textHeight);
                        ShapeInfo shapeInfo = new ShapeInfo(textBox, g2d.getColor());
                        //g2d.draw(textBox);
                        shapeLabelMap.put(shapeInfo,text);
                        shapeList.add(shapeInfo);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported operation: " + op);
            }
        }
    }

    
    private ShapeInfo getLastKey(Map<ShapeInfo, String> map) {
        // Convert the key set to a list
        List<ShapeInfo> keys = new ArrayList<>(map.keySet());
        
        // Return the last key in the list
        if (!keys.isEmpty()) {
            return keys.get(keys.size() - 1);
        }
        return null; // Return null if the map is empty
    }

    private void updateLastValue(Map<ShapeInfo, String> map, String newValue) {
        ShapeInfo lastKey = getLastKey(map);
        if (lastKey != null) {
            map.put(lastKey, newValue);
        } else {
            System.out.println("Map is empty, no update performed.");
        }
    }

    public void updateGraphData(JSONObject newGraphData) {
    	if (!newGraphData.has("objects")) {
    		return;
    	}
        objects = newGraphData.getJSONArray("objects");
        JSONArray graphInfo = newGraphData.getJSONArray("_draw_");

        JSONObject s = graphInfo.getJSONObject(2);
        JSONArray points = s.getJSONArray("points");
        JSONArray secondPoint = points.getJSONArray(1);
        graphWidth = (int) secondPoint.getDouble(1);

        if (newGraphData.has("edges")) {
            edges = newGraphData.getJSONArray("edges");
        } else {
            edges = null;
        }

        // Rebuild shape list and label map based on new data
        shapeList.clear();
        shapeLabelMap.clear();
        repaint(); // Trigger a repaint to reflect the new data
    }

    // Inner class to hold shape information
    private static class ShapeInfo {
        Shape shape;
        Color color;

        ShapeInfo(Shape shape, Color color) {
            this.shape = shape;
            this.color = color;
        }

        @Override
        public String toString() {
            return "ShapeInfo{" +
                    "shape=" + shape +
                    ", color=" + color +
                    '}';
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ShapeInfo shapeInfo = (ShapeInfo) obj;
            return shape.equals(shapeInfo.shape);
        }

        @Override
        public int hashCode() {
            return shape.hashCode();
        }
    }

    private void printShapeLabelMap() {
        System.out.println("Shape Label Map:");
        for (Map.Entry<ShapeInfo, String> entry : shapeLabelMap.entrySet()) {
            ShapeInfo shapeInfo = entry.getKey();
            String label = entry.getValue();
            System.out.println("Shape: " + shapeInfo + ", Label: " + (label != null ? label : "No Label"));
        }
    }
}
