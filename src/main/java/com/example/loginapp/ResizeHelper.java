package com.example.loginapp;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.Pane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.SplitPane;

/**
 * Utility class to help with responsive layouts
 */
public class ResizeHelper {

    /**
     * Handle window resize events
     * 
     * @param scene The scene being resized
     * @param width The new width
     * @param height The new height
     */
    public static void handleWindowResize(Scene scene, double width, double height) {
        // Get the root node
        Node root = scene.getRoot();
        
        // Apply responsive adjustments
        applyResponsiveLayout(root, width, height);
    }
    
    /**
     * Apply responsive layout adjustments to a node and its children
     * 
     * @param node The node to adjust
     * @param windowWidth The current window width
     * @param windowHeight The current window height
     */
    private static void applyResponsiveLayout(Node node, double windowWidth, double windowHeight) {
        // Calculate scale factors based on default dimensions
        double widthScale = windowWidth / MainApp.DEFAULT_WIDTH;
        double heightScale = windowHeight / MainApp.DEFAULT_HEIGHT;
        
        // Apply responsive adjustments based on node type
        if (node instanceof HBox) {
            handleHBoxResize((HBox) node, widthScale, heightScale);
        } else if (node instanceof VBox) {
            handleVBoxResize((VBox) node, widthScale, heightScale);
        } else if (node instanceof StackPane) {
            handleStackPaneResize((StackPane) node, widthScale, heightScale);
        } else if (node instanceof TableView) {
            handleTableViewResize((TableView<?>) node, widthScale);
        } else if (node instanceof ScrollPane) {
            handleScrollPaneResize((ScrollPane) node, widthScale, heightScale);
        } else if (node instanceof SplitPane) {
            handleSplitPaneResize((SplitPane) node, widthScale, heightScale);
        }
        
        // Process children recursively if the node is a parent
        if (node instanceof Pane) {
            Pane pane = (Pane) node;
            for (Node child : pane.getChildren()) {
                applyResponsiveLayout(child, windowWidth, windowHeight);
            }
        }
    }
    
    /**
     * Handle HBox resizing
     */
    private static void handleHBoxResize(HBox hbox, double widthScale, double heightScale) {
        // Adjust spacing proportionally
        if (hbox.getSpacing() > 0) {
            hbox.setSpacing(hbox.getSpacing() * widthScale);
        }
        
        // Adjust preferred width if set
        if (hbox.getPrefWidth() > 0 && hbox.getPrefWidth() != Region.USE_COMPUTED_SIZE) {
            hbox.setPrefWidth(hbox.getPrefWidth() * widthScale);
        }
        
        // Adjust preferred height if set
        if (hbox.getPrefHeight() > 0 && hbox.getPrefHeight() != Region.USE_COMPUTED_SIZE) {
            hbox.setPrefHeight(hbox.getPrefHeight() * heightScale);
        }
    }
    
    /**
     * Handle VBox resizing
     */
    private static void handleVBoxResize(VBox vbox, double widthScale, double heightScale) {
        // Adjust spacing proportionally
        if (vbox.getSpacing() > 0) {
            vbox.setSpacing(vbox.getSpacing() * heightScale);
        }
        
        // Adjust preferred width if set
        if (vbox.getPrefWidth() > 0 && vbox.getPrefWidth() != Region.USE_COMPUTED_SIZE) {
            vbox.setPrefWidth(vbox.getPrefWidth() * widthScale);
        }
        
        // Adjust preferred height if set
        if (vbox.getPrefHeight() > 0 && vbox.getPrefHeight() != Region.USE_COMPUTED_SIZE) {
            vbox.setPrefHeight(vbox.getPrefHeight() * heightScale);
        }
    }
    
    /**
     * Handle StackPane resizing
     */
    private static void handleStackPaneResize(StackPane stackPane, double widthScale, double heightScale) {
        // Adjust preferred width if set
        if (stackPane.getPrefWidth() > 0 && stackPane.getPrefWidth() != Region.USE_COMPUTED_SIZE) {
            stackPane.setPrefWidth(stackPane.getPrefWidth() * widthScale);
        }
        
        // Adjust preferred height if set
        if (stackPane.getPrefHeight() > 0 && stackPane.getPrefHeight() != Region.USE_COMPUTED_SIZE) {
            stackPane.setPrefHeight(stackPane.getPrefHeight() * heightScale);
        }
    }
    
    /**
     * Handle TableView resizing
     */
    private static void handleTableViewResize(TableView<?> tableView, double widthScale) {
        // Adjust column widths proportionally
        for (TableColumn<?, ?> column : tableView.getColumns()) {
            if (column.getPrefWidth() > 0) {
                column.setPrefWidth(column.getPrefWidth() * widthScale);
            }
        }
        
        // Adjust preferred width if set
        if (tableView.getPrefWidth() > 0 && tableView.getPrefWidth() != Region.USE_COMPUTED_SIZE) {
            tableView.setPrefWidth(tableView.getPrefWidth() * widthScale);
        }
    }
    
    /**
     * Handle ScrollPane resizing
     */
    private static void handleScrollPaneResize(ScrollPane scrollPane, double widthScale, double heightScale) {
        // Adjust preferred width if set
        if (scrollPane.getPrefWidth() > 0 && scrollPane.getPrefWidth() != Region.USE_COMPUTED_SIZE) {
            scrollPane.setPrefWidth(scrollPane.getPrefWidth() * widthScale);
        }
        
        // Adjust preferred height if set
        if (scrollPane.getPrefHeight() > 0 && scrollPane.getPrefHeight() != Region.USE_COMPUTED_SIZE) {
            scrollPane.setPrefHeight(scrollPane.getPrefHeight() * heightScale);
        }
        
        // Process content
        if (scrollPane.getContent() != null) {
            applyResponsiveLayout(scrollPane.getContent(), widthScale * MainApp.DEFAULT_WIDTH, heightScale * MainApp.DEFAULT_HEIGHT);
        }
    }
    
    /**
     * Handle SplitPane resizing
     */
    private static void handleSplitPaneResize(SplitPane splitPane, double widthScale, double heightScale) {
        // Adjust preferred width if set
        if (splitPane.getPrefWidth() > 0 && splitPane.getPrefWidth() != Region.USE_COMPUTED_SIZE) {
            splitPane.setPrefWidth(splitPane.getPrefWidth() * widthScale);
        }
        
        // Adjust preferred height if set
        if (splitPane.getPrefHeight() > 0 && splitPane.getPrefHeight() != Region.USE_COMPUTED_SIZE) {
            splitPane.setPrefHeight(splitPane.getPrefHeight() * heightScale);
        }
    }
}
