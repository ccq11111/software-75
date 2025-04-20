package com.example.saving.controller;

import com.example.saving.model.SummaryResponse;
import com.example.saving.model.CategorySummary;
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
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import javafx.application.Platform;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

import java.io.IOException;
import java.time.Month;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Controller
public class SummaryViewController {
    @Value("${server.port}")
    private String serverPort;

    @FXML private VBox contentArea;
    @FXML private Label usernameLabel;

    // Expenditure controls
    @FXML private Button expendWeekButton;
    @FXML private Button expendMonthButton;
    @FXML private Button expendYearButton;
    @FXML private DatePicker expendStartDatePicker;
    @FXML private DatePicker expendEndDatePicker;
    @FXML private Label expendAmountLabel;
    @FXML private PieChart expendPieChart;

    // Income controls
    @FXML private Button incomeWeekButton;
    @FXML private Button incomeMonthButton;
    @FXML private Button incomeYearButton;
    @FXML private DatePicker incomeStartDatePicker;
    @FXML private DatePicker incomeEndDatePicker;
    @FXML private Label incomeAmountLabel;
    @FXML private PieChart incomePieChart;

    private final RestTemplate restTemplate = new RestTemplate();
    private String authToken;

    public void initialize() {
        setupEventHandlers();
        loadInitialData();
    }

    private void setupEventHandlers() {
        // Expenditure tab buttons
        expendWeekButton.setOnAction(e -> switchToWeekPeriod("expend"));
        expendMonthButton.setOnAction(e -> switchToMonthPeriod("expend"));
        expendYearButton.setOnAction(e -> switchToYearPeriod("expend"));

        // Income tab buttons
        incomeWeekButton.setOnAction(e -> switchToWeekPeriod("income"));
        incomeMonthButton.setOnAction(e -> switchToMonthPeriod("income"));
        incomeYearButton.setOnAction(e -> switchToYearPeriod("income"));

        // Date picker listeners
        expendStartDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateExpenditureData());
        expendEndDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateExpenditureData());
        incomeStartDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateIncomeData());
        incomeEndDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateIncomeData());
    }

    private void loadInitialData() {
        // Set default dates
        expendStartDatePicker.setValue(LocalDate.now().withDayOfMonth(1));
        expendEndDatePicker.setValue(LocalDate.now());
        incomeStartDatePicker.setValue(LocalDate.now().withDayOfMonth(1));
        incomeEndDatePicker.setValue(LocalDate.now());

        // Load initial data
        updateExpenditureData();
        updateIncomeData();
    }

    private void updateExpenditureData() {
        String period = getCurrentPeriod("expend");
        String startDate = formatDate(expendStartDatePicker.getValue());
        String endDate = formatDate(expendEndDatePicker.getValue());

        try {
            SummaryResponse response = fetchSummaryData("/v1/summary/expenditure", period, startDate, endDate);
            updatePieChart(expendPieChart, response.getCategories());
            expendAmountLabel.setText(String.format("¥%.2f", response.getTotal()));
        } catch (Exception e) {
            showError("Failed to load expenditure data: " + e.getMessage());
        }
    }

    private void updateIncomeData() {
        String period = getCurrentPeriod("income");
        String startDate = formatDate(incomeStartDatePicker.getValue());
        String endDate = formatDate(incomeEndDatePicker.getValue());

        try {
            SummaryResponse response = fetchSummaryData("/v1/summary/income", period, startDate, endDate);
            updatePieChart(incomePieChart, response.getCategories());
            incomeAmountLabel.setText(String.format("¥%.2f", response.getTotal()));
        } catch (Exception e) {
            showError("Failed to load income data: " + e.getMessage());
        }
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
        headers.set("Authorization", "Bearer " + authToken);

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

    private void updatePieChart(PieChart chart, List<CategorySummary> categories) {
        chart.getData().clear();
        categories.forEach(category ->
            chart.getData().add(new PieChart.Data(
                category.getCategory() + " (" + String.format("%.1f%%", category.getPercentage()) + ")",
                category.getAmount()
            ))
        );
    }

    private String getCurrentPeriod(String type) {
        if (type.equals("expend")) {
            if (expendWeekButton != null && expendWeekButton.getStyleClass().contains("tab-button-active")) {
                return "week";
            }
            if (expendMonthButton != null && expendMonthButton.getStyleClass().contains("tab-button-active")) {
                return "month";
            }
            if (expendYearButton != null && expendYearButton.getStyleClass().contains("tab-button-active")) {
                return "year";
            }
            return "month";
        } else {
            if (incomeWeekButton != null && incomeWeekButton.getStyleClass().contains("tab-button-active")) {
                return "week";
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

    private void switchToWeekPeriod(String type) {
        if (type.equals("expend")) {
            expendWeekButton.getStyleClass().add("tab-button-active");
            expendMonthButton.getStyleClass().remove("tab-button-active");
            expendYearButton.getStyleClass().remove("tab-button-active");
        } else {
            incomeWeekButton.getStyleClass().add("tab-button-active");
            incomeMonthButton.getStyleClass().remove("tab-button-active");
            incomeYearButton.getStyleClass().remove("tab-button-active");
        }
        updateData(type);
    }

    private void switchToMonthPeriod(String type) {
        if (type.equals("expend")) {
            expendMonthButton.getStyleClass().add("tab-button-active");
            expendWeekButton.getStyleClass().remove("tab-button-active");
            expendYearButton.getStyleClass().remove("tab-button-active");
            
            // Set date range to current month
            LocalDate now = LocalDate.now();
            expendStartDatePicker.setValue(now.withDayOfMonth(1));
            expendEndDatePicker.setValue(now.withDayOfMonth(now.lengthOfMonth()));
        } else {
            incomeMonthButton.getStyleClass().add("tab-button-active");
            incomeWeekButton.getStyleClass().remove("tab-button-active");
            incomeYearButton.getStyleClass().remove("tab-button-active");
            
            // Set date range to current month
            LocalDate now = LocalDate.now();
            incomeStartDatePicker.setValue(now.withDayOfMonth(1));
            incomeEndDatePicker.setValue(now.withDayOfMonth(now.lengthOfMonth()));
        }
        updateData(type);
    }

    private void switchToYearPeriod(String type) {
        if (type.equals("expend")) {
            expendYearButton.getStyleClass().add("tab-button-active");
            expendWeekButton.getStyleClass().remove("tab-button-active");
            expendMonthButton.getStyleClass().remove("tab-button-active");
        } else {
            incomeYearButton.getStyleClass().add("tab-button-active");
            incomeWeekButton.getStyleClass().remove("tab-button-active");
            incomeMonthButton.getStyleClass().remove("tab-button-active");
        }
        updateData(type);
    }

    private void updateData(String type) {
        if (type.equals("expend")) {
            updateExpenditureData();
        } else {
            updateIncomeData();
        }
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
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

    public void setAuthToken(String token) {
        this.authToken = token;
    }
}
