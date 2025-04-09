package com.example.loginapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

/**
 * Controller for the Saving View
 */
public class SavingViewController {
    @FXML private Label usernameLabel;
    @FXML private TextField searchField;

    // Navigation buttons
    @FXML private Button billingButton;
    @FXML private Button summaryButton;
    @FXML private Button savingButton;
    @FXML private Button footprintButton;
    @FXML private Button setButton;

    // Form fields
    @FXML private TextField planNameField;
    @FXML private DatePicker startDatePicker;
    @FXML private ComboBox<String> cycleComboBox;
    @FXML private ComboBox<Integer> cycleTimesComboBox;
    @FXML private Slider amountSlider;
    @FXML private Label amountLabel;
    @FXML private ComboBox<String> currencyComboBox;
    @FXML private Button savePlanButton;

    // Currency formatter
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA);

    @FXML
    public void initialize() {
        // Set up username (will be set by the calling controller)
        usernameLabel.setText("Username");

        // Initialize form fields
        initializeFormFields();

        // Set up button handlers for navigation
        billingButton.setOnAction(event -> navigateToBilling());
        summaryButton.setOnAction(event -> navigateToSummary());
        savingButton.setOnAction(event -> System.out.println("Already on Saving"));
        footprintButton.setOnAction(event -> System.out.println("Footprint clicked"));
        setButton.setOnAction(event -> System.out.println("Set clicked"));

        // Set up slider listener with smoother updates
        amountSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            // Format the value as currency
            String formattedValue = currencyFormat.format(newValue.doubleValue());
            // Update the UI with the formatted value
            amountLabel.setText(formattedValue);

            // Add visual feedback by changing the label color when the value changes significantly
            if (Math.abs(newValue.doubleValue() - oldValue.doubleValue()) > 500) {
                amountLabel.setStyle("-fx-text-fill: #ffe100; -fx-font-weight: bold;");
                // Reset the style after a short delay
                new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            javafx.application.Platform.runLater(() ->
                                amountLabel.setStyle("-fx-text-fill: #5e3c1c; -fx-font-weight: bold;"));
                        }
                    }, 300
                );
            }
        });

        // Initialize slider value
        String initialValue = currencyFormat.format(amountSlider.getValue());
        amountLabel.setText(initialValue);

        // Set up save plan button handler
        savePlanButton.setOnAction(event -> handleSavePlan());
    }

    /**
     * Initialize form fields with default values
     */
    private void initializeFormFields() {
        // Set current date as default for date picker
        startDatePicker.setValue(LocalDate.now());

        // Initialize cycle combo box
        ObservableList<String> cycleOptions = FXCollections.observableArrayList(
                "Daily", "Weekly", "Monthly", "Quarterly", "Yearly"
        );
        cycleComboBox.setItems(cycleOptions);

        // Initialize cycle times combo box
        ObservableList<Integer> cycleTimesOptions = FXCollections.observableArrayList();
        for (int i = 1; i <= 60; i++) {
            cycleTimesOptions.add(i);
        }
        cycleTimesComboBox.setItems(cycleTimesOptions);

        // Initialize currency combo box
        ObservableList<String> currencyOptions = FXCollections.observableArrayList(
                "CNY (¥)", "USD ($)", "EUR (€)", "GBP (£)", "JPY (¥)"
        );
        currencyComboBox.setItems(currencyOptions);
        currencyComboBox.setValue("CNY (¥)");
    }

    /**
     * Navigate to the Billing view
     */
    private void navigateToBilling() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BillingView.fxml"));
            Parent billingView = loader.load();

            // Get the controller and set the username
            BillingViewController controller = loader.getController();
            controller.setUsername(usernameLabel.getText());

            // Switch to the billing view
            Scene scene = new Scene(billingView, 1000, 600);
            Stage stage = (Stage) billingButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Error loading billing view");
        }
    }

    /**
     * Navigate to the Summary view
     */
    private void navigateToSummary() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SummaryView.fxml"));
            Parent summaryView = loader.load();

            // Get the controller and set the username
            SummaryViewController controller = loader.getController();
            controller.setUsername(usernameLabel.getText());

            // Switch to the summary view
            Scene scene = new Scene(summaryView, 1000, 600);
            Stage stage = (Stage) summaryButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Error loading summary view");
        }
    }

    /**
     * Handle saving a new plan
     */
    private void handleSavePlan() {
        // Validate inputs
        if (planNameField.getText().trim().isEmpty()) {
            showAlert("Error", "Please enter a plan name");
            return;
        }

        if (cycleComboBox.getValue() == null) {
            showAlert("Error", "Please select a cycle");
            return;
        }

        if (cycleTimesComboBox.getValue() == null) {
            showAlert("Error", "Please select cycle times");
            return;
        }

        if (currencyComboBox.getValue() == null) {
            showAlert("Error", "Please select a currency");
            return;
        }

        // Get values from form
        String planName = planNameField.getText().trim();
        LocalDate startDate = startDatePicker.getValue();
        String cycle = cycleComboBox.getValue();
        int cycleTimes = cycleTimesComboBox.getValue();
        double amount = amountSlider.getValue();
        String currency = currencyComboBox.getValue();

        // Create a new saving plan
        SavingPlan savingPlan = new SavingPlan(planName, startDate, cycle, cycleTimes, amount, currency);

        // In a real app, this would be saved to a database
        // For now, just show a success message with calculated values
        String message = String.format(
                "Plan '%s' created successfully!\n\n" +
                "Start Date: %s\n" +
                "End Date: %s\n" +
                "Cycle: %s\n" +
                "Cycle Times: %d\n" +
                "Amount per cycle: %s\n" +
                "Total amount: %s\n" +
                "Currency: %s",
                savingPlan.getName(),
                savingPlan.getStartDate(),
                savingPlan.calculateEndDate(),
                savingPlan.getCycle(),
                savingPlan.getCycleTimes(),
                currencyFormat.format(savingPlan.getAmount()),
                currencyFormat.format(savingPlan.calculateTotalAmount()),
                savingPlan.getCurrency());

        showInfo("Plan Created", message);

        // Clear form for next entry
        planNameField.clear();
        amountSlider.setValue(0);
    }

    /**
     * Show an information dialog
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setUsername(String username) {
        usernameLabel.setText(username);
    }

    /**
     * Show an alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
