// Unified controller template upgrade: all controllers now implement TokenAwareController

package com.example.loginapp.controller;

import com.example.loginapp.model.SavingPlanModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

@Component
public class SavingViewController implements TokenAwareController {
    @FXML private TextField planNameField;
    @FXML private DatePicker startDatePicker;
    @FXML private ComboBox<String> cycleComboBox;
    @FXML private ComboBox<Integer> cycleTimesComboBox;
    @FXML private Slider amountSlider;
    @FXML private Label amountLabel;
    @FXML private ComboBox<String> currencyComboBox;
    @FXML private Button savePlanButton;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA);

    private String authToken;

    @Override
    public void setAuthToken(String token) {
        this.authToken = token;
    }

    @FXML
    public void initialize() {
        try {
            initializeFormFields();
            setupAmountSliderListener();

            if (savePlanButton != null) {
                savePlanButton.setOnAction(event -> handleSavePlan());
            }
        } catch (Exception e) {
            System.err.println("Error initializing SavingViewController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeFormFields() {
        if (startDatePicker != null) {
            startDatePicker.setValue(LocalDate.now());
        }

        if (cycleComboBox != null) {
            ObservableList<String> cycleOptions = FXCollections.observableArrayList(
                    "Daily", "Weekly", "Monthly", "Quarterly", "Yearly"
            );
            cycleComboBox.setItems(cycleOptions);
        }

        if (cycleTimesComboBox != null) {
            ObservableList<Integer> cycleTimesOptions = FXCollections.observableArrayList();
            for (int i = 1; i <= 60; i++) {
                cycleTimesOptions.add(i);
            }
            cycleTimesComboBox.setItems(cycleTimesOptions);
        }

        if (currencyComboBox != null) {
            ObservableList<String> currencyOptions = FXCollections.observableArrayList(
                    "CNY (¥)", "USD ($)", "EUR (€)", "GBP (£)", "JPY (¥)"
            );
            currencyComboBox.setItems(currencyOptions);
            currencyComboBox.setValue("CNY (¥)");
        }
    }

    private void setupAmountSliderListener() {
        if (amountSlider != null && amountLabel != null) {
            amountSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                String formattedValue = currencyFormat.format(newValue.doubleValue());
                amountLabel.setText(formattedValue);

                if (Math.abs(newValue.doubleValue() - oldValue.doubleValue()) > 500) {
                    amountLabel.setStyle("-fx-text-fill: #ffe100; -fx-font-weight: bold;");
                    new java.util.Timer().schedule(new java.util.TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() ->
                                    amountLabel.setStyle("-fx-text-fill: #5e3c1c; -fx-font-weight: bold;"));
                        }
                    }, 300);
                }
            });

            amountLabel.setText(currencyFormat.format(amountSlider.getValue()));
        }
    }

    private void handleSavePlan() {
        try {
            if (planNameField == null || cycleComboBox == null || cycleTimesComboBox == null ||
                    startDatePicker == null || amountSlider == null || currencyComboBox == null) {
                System.err.println("Cannot save plan: UI components are not initialized");
                return;
            }

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

            String planName = planNameField.getText().trim();
            LocalDate startDate = startDatePicker.getValue();
            String cycle = cycleComboBox.getValue();
            int cycleTimes = cycleTimesComboBox.getValue();
            double amount = amountSlider.getValue();
            String currency = currencyComboBox.getValue();

            SavingPlanModel.SavingPlan savingPlan = new SavingPlanModel.SavingPlan(
                    planName, startDate, cycle, cycleTimes, amount, currency
            );

            SavingPlanModel.getInstance().addPlan(savingPlan);

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

            planNameField.clear();
            amountSlider.setValue(0);
        } catch (Exception e) {
            System.err.println("Error saving plan: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "An error occurred while saving the plan");
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
