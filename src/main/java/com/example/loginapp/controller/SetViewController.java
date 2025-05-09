package com.example.loginapp.controller;

import com.example.loginapp.model.SavingPlanModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;

import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the Set View
 */
public class SetViewController {
    // UI Components
    @FXML private Label usernameLabel;
    @FXML private Button addPlanButton;
    @FXML private TableView<SavingPlanModel.SavingPlan> planTable;
    @FXML private TableColumn<SavingPlanModel.SavingPlan, String> nameColumn;
    @FXML private TableColumn<SavingPlanModel.SavingPlan, String> startDateColumn;
    @FXML private TableColumn<SavingPlanModel.SavingPlan, String> savedColumn;
    @FXML private TableColumn<SavingPlanModel.SavingPlan, String> cycleTimeColumn;
    @FXML private TableColumn<SavingPlanModel.SavingPlan, String> aimMoneyColumn;
    @FXML private PieChart assetsChart;
    @FXML private Label totalAssetsLabel;
    @FXML private Button backupButton;
    @FXML private Button logoutButton;

    // Get data from the shared model
    private ObservableList<SavingPlanModel.SavingPlan> planItems = SavingPlanModel.getInstance().getPlans();

    @FXML
    public void initialize() {
        try {
            // Set up username (will be set by the calling controller)
            if (usernameLabel != null) {
                usernameLabel.setText("Username");
            }

            // Initialize table
            initializeTable();

            // Initialize chart
            initializeChart();

            // Set up button handlers
            if (addPlanButton != null) {
                addPlanButton.setOnAction(event -> handleAddPlan());
            }

            if (backupButton != null) {
                backupButton.setOnAction(event -> handleBackup());
            }

            if (logoutButton != null) {
                logoutButton.setOnAction(event -> handleLogout());
            }
        } catch (Exception e) {
            System.err.println("Error initializing Set view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initialize the plan table
     */
    private void initializeTable() {
        // Configure table columns
        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));

        startDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedStartDate()));

        savedColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSavedDisplay()));

        cycleTimeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCycleTimeDisplay()));

        aimMoneyColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAimMoneyDisplay()));

        // Set table data
        planTable.setItems(planItems);

        // Add some sample data if the list is empty
        if (planItems.isEmpty()) {
            // Add sample data for demonstration
            SavingPlanModel.getInstance().addPlan(
                new SavingPlanModel.SavingPlan("Travel", java.time.LocalDate.now().plusMonths(1),
                    "Daily", 20, 5000, "CNY (¥)"));
            SavingPlanModel.getInstance().addPlan(
                new SavingPlanModel.SavingPlan("financial", java.time.LocalDate.now(),
                    "Daily", 50, 2000, "USD ($)"));
        }
    }

    /**
     * Initialize the assets chart
     */
    private void initializeChart() {
        // Calculate total assets from all plans
        double totalAssets = calculateTotalAssets();

        // Create chart data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Assets", totalAssets)
        );

        assetsChart.setData(pieChartData);
        assetsChart.setLegendVisible(false);

        // Apply custom colors to the chart
        pieChartData.get(0).getNode().setStyle("-fx-pie-color: #ff6b6b;");

        // Make the chart a donut chart
        assetsChart.setStartAngle(90);

        // Set total assets label
        if (totalAssetsLabel != null) {
            totalAssetsLabel.setText(String.format("¥%.2f", totalAssets));
        }
    }

    /**
     * Calculate total assets from all plans
     */
    private double calculateTotalAssets() {
        double total = 0.0;

        // Sum up all plan amounts
        for (SavingPlanModel.SavingPlan plan : planItems) {
            // Only count CNY plans for simplicity
            if (plan.getCurrency().startsWith("CNY")) {
                total += plan.calculateTotalAmount();
            }
        }

        // Ensure we have at least 8000 for demonstration
        return Math.max(total, 8000.0);
    }

    /**
     * Handle add plan button click - Navigate to Saving view
     */
    private void handleAddPlan() {
        try {
            // Get the parent BaseViewController to navigate to the Saving view
            Node node = addPlanButton.getScene().getRoot();
            if (node instanceof Parent) {
                Parent parent = (Parent) node;
                BaseViewController baseController = null;

                // Find the BaseViewController
                for (Node child : parent.getChildrenUnmodifiable()) {
                    if (child.getId() != null && child.getId().equals("contentArea")) {
                        baseController = (BaseViewController) parent.getUserData();
                        break;
                    }
                }

                // If we found the controller, use it to navigate
                if (baseController != null) {
                    // Call the savingButton's action event
                    baseController.navigateToSaving();
                } else {
                    // Fallback: manually load the Saving view
                    navigateToSavingManually();
                }
            } else {
                // Fallback: manually load the Saving view
                navigateToSavingManually();
            }
        } catch (Exception e) {
            System.err.println("Error handling add plan: " + e.getMessage());
            e.printStackTrace();

            // Fallback: show an alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not navigate to the Saving view. Please try again.");
            alert.showAndWait();
        }
    }

    /**
     * Fallback method to manually navigate to the Saving view
     */
    private void navigateToSavingManually() {
        try {
            // Get the current stage
            Stage stage = (Stage) addPlanButton.getScene().getWindow();

            // Load the base view again
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BaseView.fxml"));
            Parent baseView = loader.load();

            // Get the controller and set the username
            BaseViewController baseViewController = loader.getController();
            baseViewController.setUsername(username);

            // Get current window size for the new scene
            double width = stage.getWidth();
            double height = stage.getHeight();

            // Set the scene
            Scene scene = new Scene(baseView, width, height);
            stage.setScene(scene);

            // Navigate to the saving view
            baseViewController.navigateToSaving();
        } catch (IOException e) {
            System.err.println("Error navigating to Saving view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle backup button click
     */
    private void handleBackup() {
        try {
            // In a real app, this would trigger a backup process
            // For now, just show an alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Backup");
            alert.setHeaderText(null);
            alert.setContentText("Backup functionality would be implemented here.");
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Error handling backup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle logout button click - Navigate to Login view
     */
    private void handleLogout() {
        try {
            // Get the current stage
            Stage stage = (Stage) logoutButton.getScene().getWindow();

            // Navigate to the login view
            Parent loginView = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));

            // Get current window size for the new scene
            double width = stage.getWidth();
            double height = stage.getHeight();
            Scene scene = new Scene(loginView, width, height);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Error handling logout: " + e.getMessage());
            e.printStackTrace();

            // Fallback: show an alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Logout Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not log out. Please try again.");
            alert.showAndWait();
        }
    }

    // Store username as a field since we don't have the label in the content view
    private String username;

    /**
     * Set the username in the view
     */
    public void setUsername(String username) {
        this.username = username;
        // Only set the label text if the label exists
        if (usernameLabel != null) {
            usernameLabel.setText(username);
        }
    }

    public String getUsername() {
        return username;
    }


}
