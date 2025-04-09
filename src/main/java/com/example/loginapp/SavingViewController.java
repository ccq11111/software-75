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
    @FXML private Label usernameLabel; // Only needed for displaying username

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
        try {
            // Set up username (will be set by the calling controller)
            if (usernameLabel != null) {
                usernameLabel.setText("Username");
            }

            // Initialize form fields
            initializeFormFields();

            // We no longer need menu button handlers as they are handled by BaseViewController

            // Set up slider listener with smoother updates
            if (amountSlider != null && amountLabel != null) {
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
            }

            // Set up save plan button handler
            if (savePlanButton != null) {
                savePlanButton.setOnAction(event -> handleSavePlan());
            }
        } catch (Exception e) {
            System.err.println("Error initializing SavingViewController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initialize form fields with default values
     */
    private void initializeFormFields() {
        try {
            // Set current date as default for date picker
            if (startDatePicker != null) {
                startDatePicker.setValue(LocalDate.now());
            }

            // Initialize cycle combo box
            if (cycleComboBox != null) {
                ObservableList<String> cycleOptions = FXCollections.observableArrayList(
                        "Daily", "Weekly", "Monthly", "Quarterly", "Yearly"
                );
                cycleComboBox.setItems(cycleOptions);
            }

            // Initialize cycle times combo box
            if (cycleTimesComboBox != null) {
                ObservableList<Integer> cycleTimesOptions = FXCollections.observableArrayList();
                for (int i = 1; i <= 60; i++) {
                    cycleTimesOptions.add(i);
                }
                cycleTimesComboBox.setItems(cycleTimesOptions);
            }

            // Initialize currency combo box
            if (currencyComboBox != null) {
                ObservableList<String> currencyOptions = FXCollections.observableArrayList(
                        "CNY (¥)", "USD ($)", "EUR (€)", "GBP (£)", "JPY (¥)"
                );
                currencyComboBox.setItems(currencyOptions);
                currencyComboBox.setValue("CNY (¥)");
            }
        } catch (Exception e) {
            System.err.println("Error initializing form fields: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Navigation methods removed as they are now handled by BaseViewController

    /**
     * Handle saving a new plan
     */
    private void handleSavePlan() {
        try {
            // Check if UI components are available
            if (planNameField == null || cycleComboBox == null || cycleTimesComboBox == null ||
                startDatePicker == null || amountSlider == null || currencyComboBox == null) {
                System.err.println("Cannot save plan: UI components are not initialized");
                return;
            }

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
        } catch (Exception e) {
            System.err.println("Error saving plan: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "An error occurred while saving the plan");
        }
    }

    /**
     * Show an information dialog
     */
    private void showInfo(String title, String message) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Error showing info dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Store username as a field since we don't have the label in the content view
    private String username;

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
     * Show an alert dialog
     */
    private void showAlert(String title, String message) {
        try {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Error showing alert dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
