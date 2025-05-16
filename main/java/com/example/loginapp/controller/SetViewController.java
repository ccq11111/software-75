// Unified controller template upgrade: all controllers now implement TokenAwareController

package com.example.loginapp.controller;

import com.example.loginapp.model.SavingPlanModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class SetViewController implements TokenAwareController {
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

    private final ObservableList<SavingPlanModel.SavingPlan> planItems =
            SavingPlanModel.getInstance().getPlans();

    private String authToken;

    @Override
    public void setAuthToken(String token) {
        this.authToken = token;
    }

    @FXML
    public void initialize() {
        try {
            initializeTable();
            initializeChart();

            if (addPlanButton != null) {
                addPlanButton.setOnAction(event -> handleNavigateToSaving());
            }

            if (backupButton != null) {
                backupButton.setOnAction(event -> showInfo("Backup", "Backup functionality goes here."));
            }

            if (logoutButton != null) {
                logoutButton.setOnAction(event -> handleLogout());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeTable() {
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

        planTable.setItems(planItems);

        if (planItems.isEmpty()) {
            SavingPlanModel.getInstance().addPlan(
                    new SavingPlanModel.SavingPlan("Travel", java.time.LocalDate.now().plusMonths(1),
                            "Daily", 20, 5000, "CNY (¥)"));
            SavingPlanModel.getInstance().addPlan(
                    new SavingPlanModel.SavingPlan("Finance", java.time.LocalDate.now(),
                            "Daily", 50, 2000, "USD ($)"));
        }
    }

    private void initializeChart() {
        double totalAssets = calculateTotalAssets();
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Assets", totalAssets)
        );
        assetsChart.setData(pieChartData);
        assetsChart.setLegendVisible(false);
        assetsChart.setStartAngle(90);

        pieChartData.get(0).getNode().setStyle("-fx-pie-color: #ff6b6b;");

        if (totalAssetsLabel != null) {
            totalAssetsLabel.setText(String.format("¥%.2f", totalAssets));
        }
    }

    private double calculateTotalAssets() {
        double total = 0.0;
        for (SavingPlanModel.SavingPlan plan : planItems) {
            if (plan.getCurrency().startsWith("CNY")) {
                total += plan.calculateTotalAmount();
            }
        }
        return Math.max(total, 8000.0);
    }

    private void handleNavigateToSaving() {
        try {
            BaseViewController controller = ViewControllerHelper.getBaseController(addPlanButton);
            if (controller != null) {
                controller.loadSavingView();
            } else {
                showError("Could not locate base view controller.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Navigation failed: " + e.getMessage());
        }
    }

    private void handleLogout() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Parent loginView = javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            stage.setScene(new Scene(loginView, stage.getWidth(), stage.getHeight()));
        } catch (Exception e) {
            e.printStackTrace();
            showError("Logout failed: " + e.getMessage());
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
