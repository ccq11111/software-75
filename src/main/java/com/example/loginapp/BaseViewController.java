package com.example.loginapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * Controller for the base view that contains the global sidebar
 * and dynamically loads content into the right side content area
 */
public class BaseViewController {
    @FXML private Label usernameLabel;
    @FXML private TextField searchField;

    // Navigation buttons
    @FXML private Button billingButton;
    @FXML private Button summaryButton;
    @FXML private Button savingButton;
    @FXML private Button footprintButton;
    @FXML private Button setButton;
    @FXML private Button aiButton;

    // Menu item containers for styling active state
    @FXML private HBox billingMenuItem;
    @FXML private HBox summaryMenuItem;
    @FXML private HBox savingMenuItem;
    @FXML private HBox footprintMenuItem;
    @FXML private HBox setMenuItem;

    // Content area where different views will be loaded
    @FXML private StackPane contentArea;

    // Current active view
    private String currentView = "";

    @FXML
    public void initialize() {
        // Set up button handlers for navigation
        billingButton.setOnAction(event -> loadBillingView());
        summaryButton.setOnAction(event -> loadSummaryView());
        savingButton.setOnAction(event -> loadSavingView());
        footprintButton.setOnAction(event -> System.out.println("Footprint clicked"));
        setButton.setOnAction(event -> loadSetView());

        // Set up AI button handler
        aiButton.setOnAction(event -> openAIDialog());

        // Load the billing view by default
        loadBillingView();
    }

    /**
     * Load the billing view into the content area
     */
    private void loadBillingView() {
        if ("billing".equals(currentView)) return;

        try {
            // Load the billing view content
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BillingViewContent.fxml"));
            Node billingContent = loader.load();

            // Get the controller and set the username
            BillingViewController controller = loader.getController();
            controller.setUsername(usernameLabel.getText());

            // Update the content area
            contentArea.getChildren().clear();
            contentArea.getChildren().add(billingContent);

            // Update active menu item styling
            setActiveMenuItem("billing");

            currentView = "billing";
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading billing view: " + e.getMessage());
        }
    }

    /**
     * Load the summary view into the content area
     */
    private void loadSummaryView() {
        if ("summary".equals(currentView)) return;

        try {
            // Load the summary view content
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SummaryViewContent.fxml"));
            Node summaryContent = loader.load();

            // Get the controller and set the username
            SummaryViewController controller = loader.getController();
            controller.setUsername(usernameLabel.getText());

            // Update the content area
            contentArea.getChildren().clear();
            contentArea.getChildren().add(summaryContent);

            // Update active menu item styling
            setActiveMenuItem("summary");

            currentView = "summary";
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading summary view: " + e.getMessage());
        }
    }

    /**
     * Load the saving view into the content area
     */
    private void loadSavingView() {
        navigateToSaving();
    }

    /**
     * Public method to navigate to the saving view
     * This can be called from other controllers
     */
    public void navigateToSaving() {
        if ("saving".equals(currentView)) return;

        try {
            // Load the saving view content
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SavingViewContent.fxml"));
            Node savingContent = loader.load();

            // Get the controller and set the username
            SavingViewController controller = loader.getController();
            controller.setUsername(usernameLabel.getText());

            // Update the content area
            contentArea.getChildren().clear();
            contentArea.getChildren().add(savingContent);

            // Update active menu item styling
            setActiveMenuItem("saving");

            currentView = "saving";
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading saving view: " + e.getMessage());
        }
    }

    /**
     * Set the active menu item styling
     */
    private void setActiveMenuItem(String activeItem) {
        // Reset all menu items to inactive
        billingMenuItem.getStyleClass().remove("menu-item-active");
        billingMenuItem.getStyleClass().add("menu-item");
        billingButton.getStyleClass().remove("menu-button-active");
        billingButton.getStyleClass().add("menu-button");

        summaryMenuItem.getStyleClass().remove("menu-item-active");
        summaryMenuItem.getStyleClass().add("menu-item");
        summaryButton.getStyleClass().remove("menu-button-active");
        summaryButton.getStyleClass().add("menu-button");

        savingMenuItem.getStyleClass().remove("menu-item-active");
        savingMenuItem.getStyleClass().add("menu-item");
        savingButton.getStyleClass().remove("menu-button-active");
        savingButton.getStyleClass().add("menu-button");

        footprintMenuItem.getStyleClass().remove("menu-item-active");
        footprintMenuItem.getStyleClass().add("menu-item");
        footprintButton.getStyleClass().remove("menu-button-active");
        footprintButton.getStyleClass().add("menu-button");

        setMenuItem.getStyleClass().remove("menu-item-active");
        setMenuItem.getStyleClass().add("menu-item");
        setButton.getStyleClass().remove("menu-button-active");
        setButton.getStyleClass().add("menu-button");

        // Set the active menu item
        switch (activeItem) {
            case "billing":
                billingMenuItem.getStyleClass().remove("menu-item");
                billingMenuItem.getStyleClass().add("menu-item-active");
                billingButton.getStyleClass().remove("menu-button");
                billingButton.getStyleClass().add("menu-button-active");
                break;
            case "summary":
                summaryMenuItem.getStyleClass().remove("menu-item");
                summaryMenuItem.getStyleClass().add("menu-item-active");
                summaryButton.getStyleClass().remove("menu-button");
                summaryButton.getStyleClass().add("menu-button-active");
                break;
            case "saving":
                savingMenuItem.getStyleClass().remove("menu-item");
                savingMenuItem.getStyleClass().add("menu-item-active");
                savingButton.getStyleClass().remove("menu-button");
                savingButton.getStyleClass().add("menu-button-active");
                break;
            case "footprint":
                footprintMenuItem.getStyleClass().remove("menu-item");
                footprintMenuItem.getStyleClass().add("menu-item-active");
                footprintButton.getStyleClass().remove("menu-button");
                footprintButton.getStyleClass().add("menu-button-active");
                break;
            case "set":
                setMenuItem.getStyleClass().remove("menu-item");
                setMenuItem.getStyleClass().add("menu-item-active");
                setButton.getStyleClass().remove("menu-button");
                setButton.getStyleClass().add("menu-button-active");
                break;
        }
    }

    /**
     * Load the set view into the content area
     */
    private void loadSetView() {
        if ("set".equals(currentView)) return;

        try {
            // Load the set view content
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SetViewContent.fxml"));
            Node setContent = loader.load();

            // Get the controller and set the username
            SetViewController controller = loader.getController();
            controller.setUsername(usernameLabel.getText());

            // Update the content area
            contentArea.getChildren().clear();
            contentArea.getChildren().add(setContent);

            // Update active menu item styling
            setActiveMenuItem("set");

            currentView = "set";
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading set view: " + e.getMessage());
        }
    }

    /**
     * Set the username in the view
     */
    public void setUsername(String username) {
        usernameLabel.setText(username);
    }

    /**
     * Open the AI dialog
     */
    private void openAIDialog() {
        try {
            // Load the AI view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AIView.fxml"));
            Parent aiView = loader.load();

            // Create a new stage for the AI dialog
            Stage aiStage = new Stage();
            aiStage.initModality(Modality.NONE); // Non-modal dialog
            aiStage.initStyle(StageStyle.UNDECORATED); // No window decorations
            aiStage.setTitle("AI Assistant");

            // Set the scene
            Scene scene = new Scene(aiView);
            aiStage.setScene(scene);

            // Position the dialog relative to the main window
            Stage mainStage = (Stage) aiButton.getScene().getWindow();
            aiStage.setX(mainStage.getX() + (mainStage.getWidth() - 800) / 2);
            aiStage.setY(mainStage.getY() + (mainStage.getHeight() - 600) / 2);

            // Show the dialog
            aiStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading AI view: " + e.getMessage());
        }
    }
}
