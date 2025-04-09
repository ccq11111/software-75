package com.example.loginapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.IOException;

public class SummaryViewController {
    @FXML private Label usernameLabel;
    @FXML private TextField searchField;

    // Navigation buttons
    @FXML private Button billingButton;
    @FXML private Button summaryButton;
    @FXML private Button savingButton;
    @FXML private Button footprintButton;
    @FXML private Button setButton;

    // Expenditure section
    @FXML private Button expendWeekButton;
    @FXML private Button expendMonthButton;
    @FXML private Button expendYearButton;
    @FXML private PieChart expendPieChart;

    // Income section
    @FXML private Button incomeWeekButton;
    @FXML private Button incomeMonthButton;
    @FXML private Button incomeYearButton;
    @FXML private PieChart incomePieChart;

    // Data for charts
    private ObservableList<PieChart.Data> expendWeekData;
    private ObservableList<PieChart.Data> expendMonthData;
    private ObservableList<PieChart.Data> expendYearData;

    private ObservableList<PieChart.Data> incomeWeekData;
    private ObservableList<PieChart.Data> incomeMonthData;
    private ObservableList<PieChart.Data> incomeYearData;

    @FXML
    public void initialize() {
        // Set up username (will be set by the calling controller)
        usernameLabel.setText("Username");

        // Initialize chart data
        initializeChartData();

        // Set up initial charts
        updateExpendChart("Week");
        updateIncomeChart("Week");

        // Set up button handlers for navigation
        billingButton.setOnAction(event -> navigateToBilling());
        summaryButton.setOnAction(event -> System.out.println("Already on Summary"));
        savingButton.setOnAction(event -> navigateToSaving());
        footprintButton.setOnAction(event -> System.out.println("Footprint clicked"));
        setButton.setOnAction(event -> System.out.println("Set clicked"));

        // Set up button handlers for expenditure tabs
        expendWeekButton.setOnAction(event -> {
            setActiveExpendTab("Week");
            updateExpendChart("Week");
        });

        expendMonthButton.setOnAction(event -> {
            setActiveExpendTab("Month");
            updateExpendChart("Month");
        });

        expendYearButton.setOnAction(event -> {
            setActiveExpendTab("Year");
            updateExpendChart("Year");
        });

        // Set up button handlers for income tabs
        incomeWeekButton.setOnAction(event -> {
            setActiveIncomeTab("Week");
            updateIncomeChart("Week");
        });

        incomeMonthButton.setOnAction(event -> {
            setActiveIncomeTab("Month");
            updateIncomeChart("Month");
        });

        incomeYearButton.setOnAction(event -> {
            setActiveIncomeTab("Year");
            updateIncomeChart("Year");
        });
    }

    /**
     * Initialize the chart data for all time periods
     */
    private void initializeChartData() {
        // Expenditure data
        expendWeekData = FXCollections.observableArrayList(
            new PieChart.Data("Cosmetics", 300),
            new PieChart.Data("Communication", 200),
            new PieChart.Data("Living Costs", 200)
        );

        expendMonthData = FXCollections.observableArrayList(
            new PieChart.Data("Cosmetics", 500),
            new PieChart.Data("Communication", 300),
            new PieChart.Data("Living Costs", 400)
        );

        expendYearData = FXCollections.observableArrayList(
            new PieChart.Data("Cosmetics", 3000),
            new PieChart.Data("Communication", 2400),
            new PieChart.Data("Living Costs", 5000)
        );

        // Income data
        incomeWeekData = FXCollections.observableArrayList(
            new PieChart.Data("Salary", 18800)
        );

        incomeMonthData = FXCollections.observableArrayList(
            new PieChart.Data("Salary", 18800)
        );

        incomeYearData = FXCollections.observableArrayList(
            new PieChart.Data("Salary", 225600)
        );
    }

    /**
     * Update the expenditure chart based on the selected time period
     */
    private void updateExpendChart(String period) {
        ObservableList<PieChart.Data> data;

        switch (period) {
            case "Month":
                data = expendMonthData;
                break;
            case "Year":
                data = expendYearData;
                break;
            default: // Week
                data = expendWeekData;
                break;
        }

        expendPieChart.setData(data);

        // Apply custom colors to match the design
        applyCustomColors(expendPieChart);
    }

    /**
     * Update the income chart based on the selected time period
     */
    private void updateIncomeChart(String period) {
        ObservableList<PieChart.Data> data;

        switch (period) {
            case "Month":
                data = incomeMonthData;
                break;
            case "Year":
                data = incomeYearData;
                break;
            default: // Week
                data = incomeWeekData;
                break;
        }

        incomePieChart.setData(data);

        // Apply custom colors to match the design
        for (PieChart.Data slice : incomePieChart.getData()) {
            if (slice.getName().equals("Salary")) {
                slice.getNode().setStyle("-fx-pie-color: #FF6B6B;");
            }
        }
    }

    /**
     * Apply custom colors to the expenditure chart
     */
    private void applyCustomColors(PieChart chart) {
        for (PieChart.Data slice : chart.getData()) {
            switch (slice.getName()) {
                case "Cosmetics":
                    slice.getNode().setStyle("-fx-pie-color: #FFE066;");
                    break;
                case "Communication":
                    slice.getNode().setStyle("-fx-pie-color: #70C1FF;");
                    break;
                case "Living Costs":
                    slice.getNode().setStyle("-fx-pie-color: #A9A9A9;");
                    break;
            }
        }
    }

    /**
     * Set the active tab for expenditure section
     */
    private void setActiveExpendTab(String tab) {
        expendWeekButton.getStyleClass().remove("tab-button-active");
        expendMonthButton.getStyleClass().remove("tab-button-active");
        expendYearButton.getStyleClass().remove("tab-button-active");

        expendWeekButton.getStyleClass().add("tab-button");
        expendMonthButton.getStyleClass().add("tab-button");
        expendYearButton.getStyleClass().add("tab-button");

        switch (tab) {
            case "Month":
                expendMonthButton.getStyleClass().remove("tab-button");
                expendMonthButton.getStyleClass().add("tab-button-active");
                break;
            case "Year":
                expendYearButton.getStyleClass().remove("tab-button");
                expendYearButton.getStyleClass().add("tab-button-active");
                break;
            default: // Week
                expendWeekButton.getStyleClass().remove("tab-button");
                expendWeekButton.getStyleClass().add("tab-button-active");
                break;
        }
    }

    /**
     * Set the active tab for income section
     */
    private void setActiveIncomeTab(String tab) {
        incomeWeekButton.getStyleClass().remove("tab-button-active");
        incomeMonthButton.getStyleClass().remove("tab-button-active");
        incomeYearButton.getStyleClass().remove("tab-button-active");

        incomeWeekButton.getStyleClass().add("tab-button");
        incomeMonthButton.getStyleClass().add("tab-button");
        incomeYearButton.getStyleClass().add("tab-button");

        switch (tab) {
            case "Month":
                incomeMonthButton.getStyleClass().remove("tab-button");
                incomeMonthButton.getStyleClass().add("tab-button-active");
                break;
            case "Year":
                incomeYearButton.getStyleClass().remove("tab-button");
                incomeYearButton.getStyleClass().add("tab-button-active");
                break;
            default: // Week
                incomeWeekButton.getStyleClass().remove("tab-button");
                incomeWeekButton.getStyleClass().add("tab-button-active");
                break;
        }
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
            Scene scene = new Scene(billingView, 1000, 600); // Use consistent dimensions
            Stage stage = (Stage) billingButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Navigate to the Saving view
     */
    private void navigateToSaving() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SavingView.fxml"));
            Parent savingView = loader.load();

            // Get the controller and set the username
            SavingViewController controller = loader.getController();
            controller.setUsername(usernameLabel.getText());

            // Switch to the saving view
            Scene scene = new Scene(savingView, 1000, 600); // Use consistent dimensions
            Stage stage = (Stage) savingButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading saving view: " + e.getMessage());
        }
    }

    /**
     * Set the username in the view
     */
    public void setUsername(String username) {
        usernameLabel.setText(username);
    }
}
