package com.example.loginapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.IOException;
import java.time.Month;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SummaryViewController {
    @FXML private Label usernameLabel; // Only needed for displaying username

    // Expenditure section
    @FXML private Button expendCustomButton; // Replaced the original Week button
    @FXML private Button expendMonthButton;
    @FXML private Button expendYearButton;
    @FXML private PieChart expendPieChart;
    @FXML private ComboBox<String> expendMonthSelector;
    @FXML private DatePicker expendStartDatePicker; // Start date picker
    @FXML private DatePicker expendEndDatePicker; // End date picker

    // Income section
    @FXML private Button incomeCustomButton; // Replaced the original Week button
    @FXML private Button incomeMonthButton;
    @FXML private Button incomeYearButton;
    @FXML private PieChart incomePieChart;
    @FXML private ComboBox<String> incomeMonthSelector;
    @FXML private DatePicker incomeStartDatePicker; // Start date picker
    @FXML private DatePicker incomeEndDatePicker; // End date picker

    // Data for charts
    private ObservableList<PieChart.Data> expendCustomData; // Replaced the original Week data
    private ObservableList<PieChart.Data> expendMonthData;
    private ObservableList<PieChart.Data> expendYearData;
    private ObservableList<PieChart.Data>[] expendMonthlyData; // Data stored by month

    private ObservableList<PieChart.Data> incomeCustomData; // Replaced the original Week data
    private ObservableList<PieChart.Data> incomeMonthData;
    private ObservableList<PieChart.Data> incomeYearData;
    private ObservableList<PieChart.Data>[] incomeMonthlyData; // Data stored by month

    private String currentExpendPeriod = "Custom";
    private String currentIncomePeriod = "Custom";
    private int currentExpendMonth = 0; // Default to January
    private int currentIncomeMonth = 0; // Default to January
    
    // Default date range
    private LocalDate expendStartDate = LocalDate.now().minusDays(7);
    private LocalDate expendEndDate = LocalDate.now();
    private LocalDate incomeStartDate = LocalDate.now().minusDays(7);
    private LocalDate incomeEndDate = LocalDate.now();

    @FXML
    public void initialize() {
        try {
            // Set up username (will be set by the calling controller)
            if (usernameLabel != null) {
                usernameLabel.setText("Username");
            }

            // Initialize month selectors
            initializeMonthSelectors();
            
            // Initialize date pickers
            initializeDatePickers();

            // Initialize chart data
            initializeChartData();

            // Set up initial charts
            if (expendPieChart != null && incomePieChart != null) {
                updateExpendChart("Custom");
                updateIncomeChart("Custom");
            }

            // We no longer need menu button handlers as they are handled by BaseViewController

            // Set up button handlers for expenditure tabs
            if (expendCustomButton != null) {
                expendCustomButton.setOnAction(event -> {
                    setActiveExpendTab("Custom");
                    updateExpendChart("Custom");
                    if (expendMonthSelector != null) {
                        expendMonthSelector.setVisible(false);
                    }
                    if (expendStartDatePicker != null && expendEndDatePicker != null) {
                        expendStartDatePicker.setVisible(true);
                        expendEndDatePicker.setVisible(true);
                    }
                });
            }

            if (expendMonthButton != null) {
                expendMonthButton.setOnAction(event -> {
                    setActiveExpendTab("Month");
                    updateExpendChart("Month");
                    if (expendMonthSelector != null) {
                        expendMonthSelector.setVisible(true);
                    }
                    if (expendStartDatePicker != null && expendEndDatePicker != null) {
                        expendStartDatePicker.setVisible(false);
                        expendEndDatePicker.setVisible(false);
                    }
                });
            }

            if (expendYearButton != null) {
                expendYearButton.setOnAction(event -> {
                    setActiveExpendTab("Year");
                    updateExpendChart("Year");
                    if (expendMonthSelector != null) {
                        expendMonthSelector.setVisible(false);
                    }
                    if (expendStartDatePicker != null && expendEndDatePicker != null) {
                        expendStartDatePicker.setVisible(false);
                        expendEndDatePicker.setVisible(false);
                    }
                });
            }

            // Set up button handlers for income tabs
            if (incomeCustomButton != null) {
                incomeCustomButton.setOnAction(event -> {
                    setActiveIncomeTab("Custom");
                    updateIncomeChart("Custom");
                    if (incomeMonthSelector != null) {
                        incomeMonthSelector.setVisible(false);
                    }
                    if (incomeStartDatePicker != null && incomeEndDatePicker != null) {
                        incomeStartDatePicker.setVisible(true);
                        incomeEndDatePicker.setVisible(true);
                    }
                });
            }

            if (incomeMonthButton != null) {
                incomeMonthButton.setOnAction(event -> {
                    setActiveIncomeTab("Month");
                    updateIncomeChart("Month");
                    if (incomeMonthSelector != null) {
                        incomeMonthSelector.setVisible(true);
                    }
                    if (incomeStartDatePicker != null && incomeEndDatePicker != null) {
                        incomeStartDatePicker.setVisible(false);
                        incomeEndDatePicker.setVisible(false);
                    }
                });
            }

            if (incomeYearButton != null) {
                incomeYearButton.setOnAction(event -> {
                    setActiveIncomeTab("Year");
                    updateIncomeChart("Year");
                    if (incomeMonthSelector != null) {
                        incomeMonthSelector.setVisible(false);
                    }
                    if (incomeStartDatePicker != null && incomeEndDatePicker != null) {
                        incomeStartDatePicker.setVisible(false);
                        incomeEndDatePicker.setVisible(false);
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Error initializing SummaryViewController: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Initialize date pickers
     */
    private void initializeDatePickers() {
        // Set up expenditure date pickers
        if (expendStartDatePicker != null && expendEndDatePicker != null) {
            // Set default values
            expendStartDatePicker.setValue(expendStartDate);
            expendEndDatePicker.setValue(expendEndDate);
            
            // Default visibility
            expendStartDatePicker.setVisible(true);
            expendEndDatePicker.setVisible(true);
            
            // Add date change listeners
            expendStartDatePicker.setOnAction(event -> {
                expendStartDate = expendStartDatePicker.getValue();
                updateExpendChart("Custom");
            });
            
            expendEndDatePicker.setOnAction(event -> {
                expendEndDate = expendEndDatePicker.getValue();
                updateExpendChart("Custom");
            });
        }
        
        // Set up income date pickers
        if (incomeStartDatePicker != null && incomeEndDatePicker != null) {
            // Set default values
            incomeStartDatePicker.setValue(incomeStartDate);
            incomeEndDatePicker.setValue(incomeEndDate);
            
            // Default visibility
            incomeStartDatePicker.setVisible(true);
            incomeEndDatePicker.setVisible(true);
            
            // Add date change listeners
            incomeStartDatePicker.setOnAction(event -> {
                incomeStartDate = incomeStartDatePicker.getValue();
                updateIncomeChart("Custom");
            });
            
            incomeEndDatePicker.setOnAction(event -> {
                incomeEndDate = incomeEndDatePicker.getValue();
                updateIncomeChart("Custom");
            });
        }
    }

    /**
     * Initialize month selector dropdowns
     */
    private void initializeMonthSelectors() {
        // Create month list
        ObservableList<String> months = FXCollections.observableArrayList();
        for (int i = 1; i <= 12; i++) {
            months.add(Month.of(i).toString());
        }
        
        // Set up expenditure month selector
        if (expendMonthSelector != null) {
            expendMonthSelector.setItems(months);
            expendMonthSelector.getSelectionModel().select(0); // Default to January
            expendMonthSelector.setVisible(false); // Initially hidden
            
            expendMonthSelector.setOnAction(event -> {
                currentExpendMonth = expendMonthSelector.getSelectionModel().getSelectedIndex();
                updateExpendChart("Month");
            });
        }
        
        // Set up income month selector
        if (incomeMonthSelector != null) {
            incomeMonthSelector.setItems(months);
            incomeMonthSelector.getSelectionModel().select(0); // Default to January
            incomeMonthSelector.setVisible(false); // Initially hidden
            
            incomeMonthSelector.setOnAction(event -> {
                currentIncomeMonth = incomeMonthSelector.getSelectionModel().getSelectedIndex();
                updateIncomeChart("Month");
            });
        }
    }

    /**
     * Initialize the chart data for all time periods
     */
    private void initializeChartData() {
        // Expenditure data
        expendCustomData = FXCollections.observableArrayList(
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

        // Initialize monthly expenditure data
        initializeMonthlyExpendData();

        // Income data
        incomeCustomData = FXCollections.observableArrayList(
            new PieChart.Data("Salary", 18800)
        );

        incomeMonthData = FXCollections.observableArrayList(
            new PieChart.Data("Salary", 18800)
        );

        incomeYearData = FXCollections.observableArrayList(
            new PieChart.Data("Salary", 225600)
        );

        // Initialize monthly income data
        initializeMonthlyIncomeData();
    }

    /**
     * Initialize monthly expenditure data
     */
    @SuppressWarnings("unchecked")
    private void initializeMonthlyExpendData() {
        expendMonthlyData = new ObservableList[12];
        
        // Create different sample data for each month
        for (int i = 0; i < 12; i++) {
            // Set different data based on month
            double cosmeticsAmount = 400 + (i * 50) % 300;
            double communicationAmount = 250 + (i * 30) % 200;
            double livingCostsAmount = 350 + (i * 80) % 400;
            
            expendMonthlyData[i] = FXCollections.observableArrayList(
                new PieChart.Data("Cosmetics", cosmeticsAmount),
                new PieChart.Data("Communication", communicationAmount),
                new PieChart.Data("Living Costs", livingCostsAmount)
            );
        }
    }

    /**
     * Initialize monthly income data
     */
    @SuppressWarnings("unchecked")
    private void initializeMonthlyIncomeData() {
        incomeMonthlyData = new ObservableList[12];
        
        // Create different sample data for each month
        for (int i = 0; i < 12; i++) {
            // Set different income based on month
            double salary = 18000 + (i * 500) % 3000;
            
            incomeMonthlyData[i] = FXCollections.observableArrayList(
                new PieChart.Data("Salary", salary)
            );
        }
    }
    
    /**
     * Generate expenditure data for date range
     */
    private ObservableList<PieChart.Data> generateExpendDataForDateRange(LocalDate start, LocalDate end) {
        // This is just a sample. In a real application, data should be retrieved from a database or other data source
        // Here we simply adjust data based on the length of the date range
        long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        
        double factor = days / 7.0; // Factor compared to weekly data
        
        return FXCollections.observableArrayList(
            new PieChart.Data("Cosmetics", 300 * factor),
            new PieChart.Data("Communication", 200 * factor),
            new PieChart.Data("Living Costs", 200 * factor)
        );
    }
    
    /**
     * Generate income data for date range
     */
    private ObservableList<PieChart.Data> generateIncomeDataForDateRange(LocalDate start, LocalDate end) {
        // This is just a sample. In a real application, data should be retrieved from a database or other data source
        // Here we simply adjust data based on the length of the date range
        long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        
        double factor = days / 7.0; // Factor compared to weekly data
        
        return FXCollections.observableArrayList(
            new PieChart.Data("Salary", 18800 * factor)
        );
    }

    /**
     * Update the expenditure chart based on the selected time period
     */
    private void updateExpendChart(String period) {
        ObservableList<PieChart.Data> data;

        currentExpendPeriod = period;
        
        switch (period) {
            case "Month":
                if (expendMonthlyData != null && currentExpendMonth >= 0 && currentExpendMonth < 12) {
                    data = expendMonthlyData[currentExpendMonth];
                } else {
                    data = expendMonthData;
                }
                break;
            case "Year":
                data = expendYearData;
                break;
            case "Custom":
                // Use data for custom date range
                data = generateExpendDataForDateRange(expendStartDate, expendEndDate);
                break;
            default: // Default to custom date
                data = expendCustomData;
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

        currentIncomePeriod = period;
        
        switch (period) {
            case "Month":
                if (incomeMonthlyData != null && currentIncomeMonth >= 0 && currentIncomeMonth < 12) {
                    data = incomeMonthlyData[currentIncomeMonth];
                } else {
                    data = incomeMonthData;
                }
                break;
            case "Year":
                data = incomeYearData;
                break;
            case "Custom":
                // Use data for custom date range
                data = generateIncomeDataForDateRange(incomeStartDate, incomeEndDate);
                break;
            default: // Default to custom date
                data = incomeCustomData;
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
        expendCustomButton.getStyleClass().remove("tab-button-active");
        expendMonthButton.getStyleClass().remove("tab-button-active");
        expendYearButton.getStyleClass().remove("tab-button-active");

        expendCustomButton.getStyleClass().add("tab-button");
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
            default: // Custom
                expendCustomButton.getStyleClass().remove("tab-button");
                expendCustomButton.getStyleClass().add("tab-button-active");
                break;
        }
    }

    /**
     * Set the active tab for income section
     */
    private void setActiveIncomeTab(String tab) {
        incomeCustomButton.getStyleClass().remove("tab-button-active");
        incomeMonthButton.getStyleClass().remove("tab-button-active");
        incomeYearButton.getStyleClass().remove("tab-button-active");

        incomeCustomButton.getStyleClass().add("tab-button");
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
            default: // Custom
                incomeCustomButton.getStyleClass().remove("tab-button");
                incomeCustomButton.getStyleClass().add("tab-button-active");
                break;
        }
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
