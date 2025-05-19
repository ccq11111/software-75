package com.example.software.view;

import com.example.software.api.ApiServiceFactory;
import com.example.software.model.CategorySummary;
import com.example.software.model.SummaryResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SummaryViewController {
//    @Value("${server.port}")
    private String serverPort = "8080";
    @FXML private Label usernameLabel; // Only needed for displaying username
    private final RestTemplate restTemplate = new RestTemplate();
    private final ApiServiceFactory apiServiceFactory = ApiServiceFactory.getInstance();// 获取API服务工厂实例
    // Expenditure section
//    @FXML private Button expendWeekButton;
    @FXML private Button expendCustomButton;
    @FXML private Button expendMonthButton;
    @FXML private Button expendYearButton;
    @FXML private PieChart expendPieChart;
    @FXML private ComboBox<String> expendYearSelector;
    @FXML private ComboBox<String> expendMonthSelector;
    @FXML private DatePicker expendStartDatePicker; // Start date picker
    @FXML private DatePicker expendEndDatePicker; // End date picker
    @FXML private HBox expendDatePicker;
    @FXML private Label expendAmountLabel;

    // Income section
//    @FXML private Button incomeWeekButton;
    @FXML private Button incomeMonthButton;
    @FXML private Button incomeYearButton;
    @FXML private Button incomeCustomButton; // Replaced the original Week button

    @FXML private PieChart incomePieChart;
    @FXML private ComboBox<String> incomeMonthSelector;
    @FXML private ComboBox<String> incomeYearSelector;
    @FXML private DatePicker incomeStartDatePicker; // Start date picker
    @FXML private DatePicker incomeEndDatePicker; // End date picker
    @FXML private Label incomeAmountLabel;
    @FXML private HBox incomeDatePicker;
    // Data for charts
//    private ObservableList<PieChart.Data> expendWeekData;
    private ObservableList<PieChart.Data> expendMonthData;
    private ObservableList<PieChart.Data> expendYearData;
    private ObservableList<PieChart.Data> expendCustomData; // Replaced the original Week data
    private ObservableList<PieChart.Data>[] expendMonthlyData; // Data stored by month
    private ObservableList<PieChart.Data> incomeWeekData;
    private ObservableList<PieChart.Data> incomeMonthData;
    private ObservableList<PieChart.Data> incomeYearData;
    private ObservableList<PieChart.Data> incomeCustomData; // Replaced the original Week data
    private ObservableList<PieChart.Data>[] incomeMonthlyData; // Data stored by month

    private String currentExpendPeriod = "Custom";
    private String currentIncomePeriod = "Custom";
    private int currentExpendMonth = 0; // Default to January
    private int currentIncomeMonth = 0; // Default to January

    private String currentExpendYear = "2023";
    private String currentIncomeYear = "2023";

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

            // Initialize chart data
            initializeChartData();

            // Initialize date pickers
            initializeDatePickers();

            // Initialize month selectors
            initializeMonthSelectors();

            // Set up initial charts
//            if (expendPieChart != null && incomePieChart != null) {
//                updateExpendChart("Custom");
//                updateIncomeChart("Custom");
//            }

            // We no longer need menu button handlers as they are handled by BaseViewController

            // Set up button handlers for expenditure tabs
            if (expendCustomButton != null) {
                expendCustomButton.setOnAction(event -> {
                    setActiveExpendTab("Custom");
                    updateExpenditureData();
//                    updateExpendChart("Custom");
                    if (expendMonthSelector != null) {
                        expendMonthSelector.setVisible(false);
                    }
                    if (expendYearSelector != null) {
                        expendYearSelector.setVisible(false);
                    }
                    if (expendDatePicker != null) {
                        expendDatePicker.setVisible(true);
                        expendDatePicker.setManaged(true);
                    }
                });
            }
            // Set up button handlers for expenditure tabs
//            if (expendWeekButton != null) {
//                expendWeekButton.setOnAction(event -> {
//                    System.out.println("Expend Week button clicked");
//                    updateExpendChart("Week");
//                });
//            }

            if (expendMonthButton != null) {
                expendMonthButton.setOnAction(event -> {
                    setActiveExpendTab("Month");
                    updateExpenditureData();
//                    updateExpendChart("Month");
                    if (expendMonthSelector != null) {
                        expendMonthSelector.setVisible(true);
                    }
                    if (expendYearSelector != null) {
                        expendYearSelector.setVisible(true);
                    }
                    if (expendDatePicker != null) {
                        expendDatePicker.setVisible(false);
                        expendDatePicker.setManaged(false);
                    }
                });
            }

            if (expendYearButton != null) {
                expendYearButton.setOnAction(event -> {
                    setActiveExpendTab("Year");
//                    updateExpendChart("Year");
                    updateExpenditureData();
                    if (expendMonthSelector != null) {
                        expendMonthSelector.setVisible(false);
                    }
                    if (expendYearSelector != null) {
                        expendYearSelector.setVisible(true);
                    }
                    if (expendDatePicker != null) {
                        expendDatePicker.setVisible(false);
                        expendDatePicker.setManaged(false);
                    }
                });
            }

            // Set up button handlers for income tabs
            if (incomeCustomButton != null) {
                incomeCustomButton.setOnAction(event -> {
                    setActiveIncomeTab("Custom");
//                    updateIncomeChart("Custom");
                    updateIncomeData();
                    if (incomeMonthSelector != null) {
                        incomeMonthSelector.setVisible(false);
                    }
                    if (incomeYearSelector != null) {
                        incomeYearSelector.setVisible(false);
                    }
                    if (incomeDatePicker != null) {
                        incomeDatePicker.setVisible(true);
                        incomeDatePicker.setManaged(true);
                    }
                });
            }

//            if (incomeWeekButton != null) {
//                incomeWeekButton.setOnAction(event -> {
//                    System.out.println("Income Week button clicked");
//                    updateIncomeChart("Week");
//                });
//            }

            if (incomeMonthButton != null) {
                incomeMonthButton.setOnAction(event -> {
                    setActiveIncomeTab("Month");
//                    updateIncomeChart("Month");
                    updateIncomeData();
                    if (incomeMonthSelector != null) {
                        incomeMonthSelector.setVisible(true);
                    }
                    if (incomeYearSelector != null) {
                        incomeYearSelector.setVisible(true);
                    }
                    if (incomeDatePicker != null) {
                        incomeDatePicker.setVisible(false);
                        incomeDatePicker.setManaged(false);
                    }
                });
            }

            if (incomeYearButton != null) {
                incomeYearButton.setOnAction(event -> {
                    setActiveIncomeTab("Year");
//                    updateIncomeChart("Year");
                    updateIncomeData();
                    if (incomeMonthSelector != null) {
                        incomeMonthSelector.setVisible(false);
                    }
                    if (incomeYearSelector != null) {
                        incomeYearSelector.setVisible(true);
                    }
                    if (incomeDatePicker != null) {
                        incomeDatePicker.setVisible(false);
                        incomeDatePicker.setManaged(false);
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Error initializing SummaryViewController: " + e.getMessage());
            e.printStackTrace();
        }
        // Initialize monthly expenditure data
        initializeMonthlyExpendData();
        // Initialize monthly income data
        initializeMonthlyIncomeData();

        updateExpenditureData();
        updateIncomeData();
    }
    private void updateExpenditureData() {
        String period = getCurrentPeriod("expend");
        String startDate = "";
        String endDate = "";
        if(period.equals("custom")){
            startDate = formatDate(expendStartDatePicker.getValue());
            endDate = formatDate(expendEndDatePicker.getValue());
        } else if(period.equals("month")){
            startDate = formatDate(LocalDate.of(Integer.parseInt(currentExpendYear), currentExpendMonth + 1, 1));
            endDate = formatDate(LocalDate.of(Integer.parseInt(currentExpendYear), currentExpendMonth + 1, 31));
        } else if(period.equals("year")){
            startDate = formatDate(LocalDate.of(Integer.parseInt(currentExpendYear), 1, 1));
            endDate = formatDate(LocalDate.of(Integer.parseInt(currentExpendYear), 12, 31));
        }
        try {
            SummaryResponse response = fetchSummaryData("/api/purseai/v1/summary/expenditure", period, startDate, endDate);
            updatePieChart(expendPieChart, response.getCategories());
            expendAmountLabel.setText(String.format("¥%.2f", response.getTotal()));
        } catch (Exception e) {
            showError("Failed to load expenditure data: " + e.getMessage());
        }
    }

    private void updateIncomeData() {
        String period = getCurrentPeriod("income");
        String startDate = "";
        String endDate = "";
        if(period.equals("custom")){
            startDate = formatDate(incomeStartDatePicker.getValue());
            endDate = formatDate(incomeEndDatePicker.getValue());
        } else if(period.equals("month")){
            startDate = formatDate(LocalDate.of(Integer.parseInt(currentIncomeYear), currentIncomeMonth + 1, 1));
            endDate = formatDate(LocalDate.of(Integer.parseInt(currentIncomeYear), currentIncomeMonth + 1, 31));
        } else if(period.equals("year")){
            startDate = formatDate(LocalDate.of(Integer.parseInt(currentIncomeYear), 1, 1));
            endDate = formatDate(LocalDate.of(Integer.parseInt(currentIncomeYear), 12, 31));
        }

        try {
            SummaryResponse response = fetchSummaryData("/api/purseai/v1/summary/income", period, startDate, endDate);
            updatePieChart(incomePieChart, response.getCategories());
            incomeAmountLabel.setText(String.format("¥%.2f", response.getTotal()));
        } catch (Exception e) {
            showError("Failed to load income data: " + e.getMessage());
        }
    }
    private String getCurrentPeriod(String type) {
        if (type.equals("expend")) {
            if (expendCustomButton != null && expendCustomButton.getStyleClass().contains("tab-button-active")) {
                return "custom";
            }
            if (expendMonthButton != null && expendMonthButton.getStyleClass().contains("tab-button-active")) {
                return "month";
            }
            if (expendYearButton != null && expendYearButton.getStyleClass().contains("tab-button-active")) {
                return "year";
            }
            return "month";
        } else {
            if (incomeCustomButton != null && incomeCustomButton.getStyleClass().contains("tab-button-active")) {
                return "custom";
            }
            if (incomeMonthButton != null && incomeMonthButton.getStyleClass().contains("tab-button-active")) {
                return "month";
            }
            if (incomeYearButton != null && incomeYearButton.getStyleClass().contains("tab-button-active")) {
                return "year";
            }
            return "month";
        }
    }
    private String formatDate(LocalDate date) {
        return date != null ? date.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
    }
    private SummaryResponse fetchSummaryData(String endpoint, String period, String startDate, String endDate) {
        String url = String.format("http://localhost:%s%s?period=%s", serverPort, endpoint, period.toLowerCase());
        if (startDate != null && !startDate.isEmpty()) {
            url += "&startDate=" + startDate;
        }
        if (endDate != null && !endDate.isEmpty()) {
            url += "&endDate=" + endDate;
        }

        System.out.println("Fetching data from URL: " + url);
        System.out.println("Period: " + period);
        System.out.println("Start Date: " + startDate);
        System.out.println("End Date: " + endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiServiceFactory.getToken());

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                System.out.println("Response body: " + responseBody);

                // 手动解析 JSON
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                SummaryResponse summaryResponse = mapper.readValue(responseBody, SummaryResponse.class);
                return summaryResponse;
            } else {
                throw new RuntimeException("Failed to fetch data: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("Error fetching data: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch data: " + e.getMessage());
        }
    }
    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
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
            expendDatePicker.setVisible(true);
            expendDatePicker.setManaged(true);

            // Add date change listeners
            expendStartDatePicker.setOnAction(event -> {
                expendStartDate = expendStartDatePicker.getValue();
//                updateExpendChart("Custom");
                updateExpenditureData();
            });

            expendEndDatePicker.setOnAction(event -> {
                expendEndDate = expendEndDatePicker.getValue();
//                updateExpendChart("Custom");
                updateExpenditureData();
            });
        }

        // Set up income date pickers
        if (incomeStartDatePicker != null && incomeEndDatePicker != null) {
            // Set default values
            incomeStartDatePicker.setValue(incomeStartDate);
            incomeEndDatePicker.setValue(incomeEndDate);

            // Default visibility
            incomeDatePicker.setVisible(true);
            incomeDatePicker.setManaged(true);
            // Add date change listeners
            incomeStartDatePicker.setOnAction(event -> {
                incomeStartDate = incomeStartDatePicker.getValue();
//                updateIncomeChart("Custom");
                updateIncomeData();
            });

            incomeEndDatePicker.setOnAction(event -> {
                incomeEndDate = incomeEndDatePicker.getValue();
//                updateIncomeChart("Custom");
                updateIncomeData();
            });
        }
    }
    private void updatePieChart(PieChart chart, List<CategorySummary> categories) {
        chart.getData().clear();
        categories.forEach(category ->
                chart.getData().add(new PieChart.Data(
                        category.getCategory() + " (" + String.format("%.1f%%", category.getPercentage()) + ")",
                        category.getAmount()
                ))
        );
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
//                updateExpendChart("Month");
                updateExpenditureData();
            });
        }
        ObservableList<String> years = FXCollections.observableArrayList();
        years.add("2023");
        years.add("2024");
        years.add("2025");
        if (expendYearSelector != null) {
            expendYearSelector.setItems(years);
            expendYearSelector.getSelectionModel().select(0); // Default to January
            expendYearSelector.setVisible(false); // Initially hidden

            expendYearSelector.setOnAction(event -> {
                currentExpendYear = expendYearSelector.getSelectionModel().getSelectedItem();
//                updateExpendChart("Month");
                updateExpenditureData();
            });
        }

        // Set up income month selector
        if (incomeMonthSelector != null) {
            incomeMonthSelector.setItems(months);
            incomeMonthSelector.getSelectionModel().select(0); // Default to January
            incomeMonthSelector.setVisible(false); // Initially hidden

            incomeMonthSelector.setOnAction(event -> {
                currentIncomeMonth = incomeMonthSelector.getSelectionModel().getSelectedIndex();
//                updateIncomeChart("Month");
                updateIncomeData();
            });
        }

        if (incomeYearSelector != null) {
            incomeYearSelector.setItems(years);
            incomeYearSelector.getSelectionModel().select(0); // Default to January
            incomeYearSelector.setVisible(false); // Initially hidden

            incomeYearSelector.setOnAction(event -> {
                currentIncomeYear = incomeYearSelector.getSelectionModel().getSelectedItem();
//                updateIncomeChart("Month");
                updateIncomeData();
            });
        }
    }

    /**
     * Initialize the chart data for all time periods
     */
    private void initializeChartData() {
        // Expenditure data
//        expendWeekData = FXCollections.observableArrayList(
//            new PieChart.Data("Cosmetics", 300),
//            new PieChart.Data("Communication", 200),
//            new PieChart.Data("Living Costs", 200)
//        );

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
//        incomeWeekData = FXCollections.observableArrayList(
//            new PieChart.Data("Salary", 0) // Zero income for testing the placeholder
//        );
        incomeCustomData = FXCollections.observableArrayList(
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
        // First, make sure the correct tab is visually selected
        setActiveExpendTab(period);

        ObservableList<PieChart.Data> data;

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
            default: // Week
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
        // First, make sure the correct tab is visually selected
        setActiveIncomeTab(period);

        ObservableList<PieChart.Data> data;

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
            default: // Week
                data = incomeCustomData;
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
//        expendWeekButton.getStyleClass().remove("tab-button-active");
        expendMonthButton.getStyleClass().remove("tab-button-active");
        expendYearButton.getStyleClass().remove("tab-button-active");
        expendCustomButton.getStyleClass().remove("tab-button-active");

//        expendWeekButton.getStyleClass().add("tab-button");
        expendMonthButton.getStyleClass().add("tab-button");
        expendYearButton.getStyleClass().add("tab-button");
        expendCustomButton.getStyleClass().add("tab-button");

        switch (tab) {
            case "Month":
                expendMonthButton.getStyleClass().remove("tab-button");
                expendMonthButton.getStyleClass().add("tab-button-active");
                break;
            case "Year":
                expendYearButton.getStyleClass().remove("tab-button");
                expendYearButton.getStyleClass().add("tab-button-active");
                break;
            case "Custom":
                expendCustomButton.getStyleClass().remove("tab-button");
                expendCustomButton.getStyleClass().add("tab-button-active");
                break;
            default: // Week
                expendCustomButton.getStyleClass().remove("tab-button");
                expendCustomButton.getStyleClass().add("tab-button-active");
                break;
        }
    }

    /**
     * Set the active tab for income section
     */
    private void setActiveIncomeTab(String tab) {
        System.out.println("Setting active income tab: " + tab);

        // Debug current style classes
//        System.out.println("Before change - Week button classes: " + incomeWeekButton.getStyleClass());
        System.out.println("Before change - Month button classes: " + incomeMonthButton.getStyleClass());
        System.out.println("Before change - Year button classes: " + incomeYearButton.getStyleClass());

        // First remove active class from all buttons
//        incomeWeekButton.getStyleClass().remove("tab-button-active");
        incomeMonthButton.getStyleClass().remove("tab-button-active");
        incomeYearButton.getStyleClass().remove("tab-button-active");
        incomeCustomButton.getStyleClass().remove("tab-button-active");

        // Then ensure all buttons have the base tab-button class
//        incomeWeekButton.getStyleClass().add("tab-button");
        incomeMonthButton.getStyleClass().add("tab-button");
        incomeYearButton.getStyleClass().add("tab-button");
        incomeCustomButton.getStyleClass().add("tab-button");
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
            case "Custom":
                incomeCustomButton.getStyleClass().remove("tab-button");
                incomeCustomButton.getStyleClass().add("tab-button-active");
                break;
            default: // Week
                incomeCustomButton.getStyleClass().remove("tab-button");
                incomeCustomButton.getStyleClass().add("tab-button-active");
                break;
        }

        // Debug updated style classes
//        System.out.println("After change - Week button classes: " + incomeWeekButton.getStyleClass());
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
