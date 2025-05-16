// Unified controller template upgrade: all controllers now implement TokenAwareController

package com.example.loginapp.controller;

import com.example.loginapp.model.SummaryResponse;
import com.example.loginapp.model.CategorySummary;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.application.Platform;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class SummaryViewController implements TokenAwareController {
    @FXML private VBox contentArea;

    // Expenditure section
    @FXML private Button expendWeekButton;
    @FXML private Button expendMonthButton;
    @FXML private Button expendYearButton;
    @FXML private DatePicker expendStartDatePicker;
    @FXML private DatePicker expendEndDatePicker;
    @FXML private Label expendAmountLabel;
    @FXML private PieChart expendPieChart;

    // Income section
    @FXML private Button incomeWeekButton;
    @FXML private Button incomeMonthButton;
    @FXML private Button incomeYearButton;
    @FXML private DatePicker incomeStartDatePicker;
    @FXML private DatePicker incomeEndDatePicker;
    @FXML private Label incomeAmountLabel;
    @FXML private PieChart incomePieChart;

    // Floating AI Button
    @FXML private Button aiFloatingButton;

    private final RestTemplate restTemplate = new RestTemplate();
    private String authToken;

    @Override
    public void setAuthToken(String token) {
        this.authToken = token;
    }

    @FXML
    public void initialize() {
        setupEventHandlers();
        loadInitialData();
        setupAIButtonHandler();
    }

    private void setupEventHandlers() {
        expendWeekButton.setOnAction(e -> switchToWeekPeriod("expend"));
        expendMonthButton.setOnAction(e -> switchToMonthPeriod("expend"));
        expendYearButton.setOnAction(e -> switchToYearPeriod("expend"));

        incomeWeekButton.setOnAction(e -> switchToWeekPeriod("income"));
        incomeMonthButton.setOnAction(e -> switchToMonthPeriod("income"));
        incomeYearButton.setOnAction(e -> switchToYearPeriod("income"));

        expendStartDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateExpenditureData());
        expendEndDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateExpenditureData());
        incomeStartDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateIncomeData());
        incomeEndDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateIncomeData());
    }

    private void setupAIButtonHandler() {
        if (aiFloatingButton != null) {
            aiFloatingButton.setOnAction(event -> openAIDialog());
        }
    }

    private void openAIDialog() {
        try {
            BaseViewController baseController = ViewControllerHelper.getBaseController(aiFloatingButton);
            if (baseController != null) {
                baseController.openAIDialog();
            } else {
                showError("Could not locate base view controller.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to open AI Assistant: " + e.getMessage());
        }
    }

    private void loadInitialData() {
        expendStartDatePicker.setValue(LocalDate.now().withDayOfMonth(1));
        expendEndDatePicker.setValue(LocalDate.now());
        incomeStartDatePicker.setValue(LocalDate.now().withDayOfMonth(1));
        incomeEndDatePicker.setValue(LocalDate.now());

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
        String url = String.format("http://localhost:8089%s?period=%s", endpoint, period.toLowerCase());
        if (startDate != null && !startDate.isEmpty()) url += "&startDate=" + startDate;
        if (endDate != null && !endDate.isEmpty()) url += "&endDate=" + endDate;

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
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                return mapper.readValue(response.getBody(), SummaryResponse.class);
            } else {
                throw new RuntimeException("Failed to fetch data: " + response.getStatusCode());
            }
        } catch (Exception e) {
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
            if (expendWeekButton.getStyleClass().contains("tab-button-active")) return "week";
            if (expendMonthButton.getStyleClass().contains("tab-button-active")) return "month";
            if (expendYearButton.getStyleClass().contains("tab-button-active")) return "year";
            return "month";
        } else {
            if (incomeWeekButton.getStyleClass().contains("tab-button-active")) return "week";
            if (incomeMonthButton.getStyleClass().contains("tab-button-active")) return "month";
            if (incomeYearButton.getStyleClass().contains("tab-button-active")) return "year";
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
        LocalDate now = LocalDate.now();
        if (type.equals("expend")) {
            expendMonthButton.getStyleClass().add("tab-button-active");
            expendWeekButton.getStyleClass().remove("tab-button-active");
            expendYearButton.getStyleClass().remove("tab-button-active");
            expendStartDatePicker.setValue(now.withDayOfMonth(1));
            expendEndDatePicker.setValue(now.withDayOfMonth(now.lengthOfMonth()));
        } else {
            incomeMonthButton.getStyleClass().add("tab-button-active");
            incomeWeekButton.getStyleClass().remove("tab-button-active");
            incomeYearButton.getStyleClass().remove("tab-button-active");
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
}
