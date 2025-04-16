package com.example.loginapp;

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
import javafx.scene.layout.GridPane;

import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

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
        
        // Add row click event handler
        planTable.setRowFactory(tv -> {
            TableRow<SavingPlanModel.SavingPlan> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    SavingPlanModel.SavingPlan clickedPlan = row.getItem();
                    showAddSavingDialog(clickedPlan);
                }
            });
            return row;
        });
    }

    /**
     * Initialize the assets chart
     */
    private void initializeChart() {
        // Calculate total assets from all plans
        double totalAssets = calculateTotalAssets();
        
        // Create chart data with individual plan contributions
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        // Add data for each plan that has savings
        for (SavingPlanModel.SavingPlan plan : planItems) {
            double amount = plan.getSavedAmount();
            
            // Skip plans with zero savings
            if (amount <= 0) {
                continue;
            }
            
            // Convert to CNY for consistent display
            if (plan.getCurrency().startsWith("USD")) {
                amount *= 7.2;
            } else if (plan.getCurrency().startsWith("EUR")) {
                amount *= 7.9;
            } else if (plan.getCurrency().startsWith("GBP")) {
                amount *= 9.3;
            } else if (plan.getCurrency().startsWith("JPY")) {
                amount *= 0.048;
            }
            
            // Add the plan to chart data
            pieChartData.add(new PieChart.Data(plan.getName(), amount));
        }
        
        // If no plans have savings, add a placeholder
        if (pieChartData.isEmpty()) {
            pieChartData.add(new PieChart.Data("No Savings", 100));
        }

        // Apply the data to the chart
        assetsChart.setData(pieChartData);
        
        // Show the legend to identify different plans
        assetsChart.setLegendVisible(true);
        
        // Apply custom colors to each slice
        String[] colorPalette = {
            "#ff6b6b", "#4d96ff", "#5cb85c", "#f0ad4e", "#6f42c1",
            "#20c997", "#6610f2", "#fd7e14", "#e83e8c", "#17a2b8"
        };
        
        int colorIndex = 0;
        for (PieChart.Data data : pieChartData) {
            String color = colorPalette[colorIndex % colorPalette.length];
            data.getNode().setStyle("-fx-pie-color: " + color + ";");
            colorIndex++;
        }

        // Set total assets label with CNY symbol
        if (totalAssetsLabel != null) {
            totalAssetsLabel.setText(String.format("¥%.2f", totalAssets));
        }
    }

    /**
     * Calculate total assets from all plans
     */
    private double calculateTotalAssets() {
        double total = 0.0;

        // Sum up all plan saved amounts
        for (SavingPlanModel.SavingPlan plan : planItems) {
            // Convert all currencies to CNY
            double amount = plan.getSavedAmount();
            
            // Apply exchange rates for different currencies
            if (plan.getCurrency().startsWith("USD")) {
                // USD to CNY (approximate exchange rate: 1 USD = 7.2 CNY)
                amount *= 7.2;
            } else if (plan.getCurrency().startsWith("EUR")) {
                // EUR to CNY (approximate exchange rate: 1 EUR = 7.9 CNY)
                amount *= 7.9;
            } else if (plan.getCurrency().startsWith("GBP")) {
                // GBP to CNY (approximate exchange rate: 1 GBP = 9.3 CNY)
                amount *= 9.3;
            } else if (plan.getCurrency().startsWith("JPY")) {
                // JPY to CNY (approximate exchange rate: 1 JPY = 0.048 CNY)
                amount *= 0.048;
            }
            
            // Add to total (CNY plans remain unchanged)
            total += amount;
        }

        return total;
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
            backupData();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Backup Successful");
            alert.setHeaderText(null);
            alert.setContentText("Data has been successfully backed up to local file.");
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Error during backup: " + e.getMessage());
            e.printStackTrace();
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Backup Failed");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred during backup. Please try again.");
            alert.showAndWait();
        }
    }

    /**
     * Backup plan data to local file
     */
    private void backupData() throws IOException {
        // Create backup directory if it doesn't exist
        File backupDir = new File("backups");
        if (!backupDir.exists()) {
            backupDir.mkdir();
        }

        // Create backup filename with timestamp
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File backupFile = new File(backupDir, "saving_plans_backup_" + timestamp + ".json");

        // Convert data to JSON format
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.writeValue(backupFile, planItems);
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

    /**
     * Show dialog to add saving amount to a plan
     */
    private void showAddSavingDialog(SavingPlanModel.SavingPlan plan) {
        try {
            // Create the custom dialog
            Dialog<Double> dialog = new Dialog<>();
            dialog.setTitle("Add Saving");
            dialog.setHeaderText("Add amount to " + plan.getName() + " plan");
            
            // Set the button types
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            // Create the amount field and label
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
            
            TextField amountField = new TextField();
            amountField.setPromptText("Amount");
            
            // Show current saved amount
            Label currentSavedLabel = new Label("Current saved: " + plan.getSavedDisplay());
            Label currencyLabel = new Label(plan.getCurrency());
            
            grid.add(new Label("Amount to add:"), 0, 0);
            grid.add(amountField, 1, 0);
            grid.add(currencyLabel, 2, 0);
            grid.add(currentSavedLabel, 0, 1, 3, 1);
            
            dialog.getDialogPane().setContent(grid);
            
            // Request focus on the amount field by default
            javafx.application.Platform.runLater(() -> amountField.requestFocus());
            
            // Convert the result to a number when the save button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    try {
                        return Double.parseDouble(amountField.getText());
                    } catch (NumberFormatException e) {
                        // Show error for invalid input
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Invalid Input");
                        alert.setHeaderText(null);
                        alert.setContentText("Please enter a valid number.");
                        alert.showAndWait();
                        return null;
                    }
                }
                return null;
            });
            
            // Show the dialog and wait for the user's response
            Optional<Double> result = dialog.showAndWait();
            
            // Process the result
            result.ifPresent(amount -> {
                // Update the saved amount
                double currentSaved = plan.getSavedAmount();
                plan.setSavedAmount(currentSaved + amount);
                
                // Refresh the table and chart
                planTable.refresh();
                initializeChart(); // Update the chart with new data
                
                // Show confirmation
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Amount Added");
                alert.setHeaderText(null);
                alert.setContentText(String.format("Successfully added %.2f to %s plan.", 
                        amount, plan.getName()));
                alert.showAndWait();
            });
            
        } catch (Exception e) {
            System.err.println("Error showing add saving dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
