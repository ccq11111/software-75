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
    @FXML private Label usernameLabel; // Only needed for displaying username

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
        try {
            // Set up username (will be set by the calling controller)
            if (usernameLabel != null) {
                usernameLabel.setText("Username");
            }

            // Initialize chart data
            initializeChartData();

            // Set up initial charts
            if (expendPieChart != null && incomePieChart != null) {
                updateExpendChart("Week");
                updateIncomeChart("Week");
            }

            // We no longer need menu button handlers as they are handled by BaseViewController

            // Set up button handlers for expenditure tabs
            if (expendWeekButton != null) {
                expendWeekButton.setOnAction(event -> {
                    System.out.println("Expend Week button clicked");
                    updateExpendChart("Week");
                });
            }

            if (expendMonthButton != null) {
                expendMonthButton.setOnAction(event -> {
                    System.out.println("Expend Month button clicked");
                    updateExpendChart("Month");
                });
            }

            if (expendYearButton != null) {
                expendYearButton.setOnAction(event -> {
                    System.out.println("Expend Year button clicked");
                    updateExpendChart("Year");
                });
            }

            // Set up button handlers for income tabs
            if (incomeWeekButton != null) {
                incomeWeekButton.setOnAction(event -> {
                    System.out.println("Income Week button clicked");
                    updateIncomeChart("Week");
                });
            }

            if (incomeMonthButton != null) {
                incomeMonthButton.setOnAction(event -> {
                    System.out.println("Income Month button clicked");
                    updateIncomeChart("Month");
                });
            }

            if (incomeYearButton != null) {
                incomeYearButton.setOnAction(event -> {
                    System.out.println("Income Year button clicked");
                    updateIncomeChart("Year");
                });
            }
        } catch (Exception e) {
            System.err.println("Error initializing SummaryViewController: " + e.getMessage());
            e.printStackTrace();
        }
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
        // For Week, we'll set up a test case with zero income to demonstrate the placeholder
        incomeWeekData = FXCollections.observableArrayList(
            new PieChart.Data("Salary", 0) // Zero income for testing the placeholder
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
        // First, make sure the correct tab is visually selected
        setActiveExpendTab(period);

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
        // First, make sure the correct tab is visually selected
        setActiveIncomeTab(period);

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

        // Check if there's any income data
        boolean hasIncome = false;
        double totalIncome = 0;

        for (PieChart.Data slice : data) {
            totalIncome += slice.getPieValue();
            if (slice.getPieValue() > 0) {
                hasIncome = true;
            }
        }

        // If there's no income, create a white placeholder pie chart
        if (!hasIncome || totalIncome == 0) {
            // Create a placeholder with a white pie chart
            ObservableList<PieChart.Data> placeholderData = FXCollections.observableArrayList(
                new PieChart.Data("No Income", 1)
            );
            incomePieChart.setData(placeholderData);

            // Set the placeholder slice to white
            for (PieChart.Data slice : incomePieChart.getData()) {
                slice.getNode().setStyle("-fx-pie-color: white;");
            }
        } else {
            // Normal case with income data
            incomePieChart.setData(data);

            // Apply custom colors to match the design
            for (PieChart.Data slice : incomePieChart.getData()) {
                if (slice.getName().equals("Salary")) {
                    slice.getNode().setStyle("-fx-pie-color: #FF6B6B;");
                }
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
        System.out.println("Setting active income tab: " + tab);

        // Debug current style classes
        System.out.println("Before change - Week button classes: " + incomeWeekButton.getStyleClass());
        System.out.println("Before change - Month button classes: " + incomeMonthButton.getStyleClass());
        System.out.println("Before change - Year button classes: " + incomeYearButton.getStyleClass());

        // First remove active class from all buttons
        incomeWeekButton.getStyleClass().remove("tab-button-active");
        incomeMonthButton.getStyleClass().remove("tab-button-active");
        incomeYearButton.getStyleClass().remove("tab-button-active");

        // Then ensure all buttons have the base tab-button class
        incomeWeekButton.getStyleClass().add("tab-button");
        incomeMonthButton.getStyleClass().add("tab-button");
        incomeYearButton.getStyleClass().add("tab-button");

        // Finally, set the active class on the selected button
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

        // Debug updated style classes
        System.out.println("After change - Week button classes: " + incomeWeekButton.getStyleClass());
        System.out.println("After change - Month button classes: " + incomeMonthButton.getStyleClass());
        System.out.println("After change - Year button classes: " + incomeYearButton.getStyleClass());
    }

    // Navigation methods removed as they are now handled by BaseViewController

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
