// Unified controller template upgrade: all controllers now implement TokenAwareController

package com.example.loginapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class BillingViewController implements TokenAwareController {
    private String authToken;

    @FXML private Label usernameLabel;
    @FXML private Button addCsvButton;
    @FXML private TextField productField;
    @FXML private TextField priceField;
    @FXML private DatePicker timePicker;
    @FXML private ToggleGroup categoryGroup;
    @FXML private RadioButton livingCostsRadio;
    @FXML private RadioButton communicationRadio;
    @FXML private RadioButton salaryRadio;
    @FXML private RadioButton cosmeticsRadio;
    @FXML private Button resetButton;
    @FXML private Button buildButton;
    @FXML private Button inquireButton;
    @FXML private TableView<BillingEntry> billingTable;
    @FXML private TableColumn<BillingEntry, String> categoryColumn;
    @FXML private TableColumn<BillingEntry, String> productColumn;
    @FXML private TableColumn<BillingEntry, String> timeColumn;
    @FXML private TableColumn<BillingEntry, Double> priceColumn;
    @FXML private TableColumn<BillingEntry, String> remarkColumn;

    private final ObservableList<BillingEntry> billingData = FXCollections.observableArrayList();

    @Override
    public void setAuthToken(String token) {
        this.authToken = token;
    }

    @FXML
    public void initialize() {
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        productColumn.setCellValueFactory(cellData -> cellData.getValue().productProperty());
        timeColumn.setCellValueFactory(cellData -> cellData.getValue().formattedTimeProperty());
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        remarkColumn.setCellValueFactory(cellData -> cellData.getValue().remarkProperty());

        priceColumn.setCellFactory(column -> new TableCell<BillingEntry, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%s¥%.2f", item >= 0 ? "+" : "-", Math.abs(item)));
                    setStyle("-fx-text-fill: " + (item >= 0 ? "#4CAF50" : "#F44336") + "; -fx-font-weight: bold;");
                }
            }
        });

        categoryColumn.setCellFactory(column -> new TableCell<BillingEntry, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String iconPath = getCategoryIconPath(item);
                    if (iconPath != null) {
                        ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
                        imageView.setFitHeight(24);
                        imageView.setFitWidth(24);
                        setGraphic(imageView);
                    } else {
                        setText(item);
                    }
                }
            }
        });

        billingTable.setItems(billingData);
        timePicker.setValue(LocalDate.now());
        addSampleData();

        addCsvButton.setOnAction(event -> handleAddCsv());
        resetButton.setOnAction(event -> clearInputFields());
        buildButton.setOnAction(event -> handleAddRecord());
        inquireButton.setOnAction(event -> handleFuzzySearch());
    }

    private void addSampleData() {
        billingData.add(new BillingEntry("Living costs", "Nov.utilities bill", -200.00, LocalDate.of(2024, 12, 24), "14:29"));
        billingData.add(new BillingEntry("Communication", "Dad phone bill", -200.00, LocalDate.of(2024, 12, 23), "14:29"));
        billingData.add(new BillingEntry("Salary", "Nov.salary", 18800.00, LocalDate.of(2024, 12, 21), "14:29"));
        billingData.add(new BillingEntry("Cosmetics", "-", -300.00, LocalDate.of(2024, 12, 13), "14:29"));
    }

    private void handleAddRecord() {
        try {
            String product = productField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());
            String category = getSelectedCategory();
            LocalDate date = timePicker.getValue();

            if (product.isEmpty() || category == null || date == null) {
                showAlert("Error", "Please fill in all fields");
                return;
            }

            billingData.add(new BillingEntry(category, product, price, date, "14:29"));
            clearInputFields();
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid price");
        }
    }

    private void handleFuzzySearch() {
        String searchTerm = productField.getText().trim().toLowerCase();
        if (searchTerm.isEmpty()) {
            showAlert("Search Error", "Please enter a search term");
            return;
        }
        ObservableList<BillingEntry> searchResults = FXCollections.observableArrayList();
        for (BillingEntry entry : billingData) {
            if (entry.productProperty().get().toLowerCase().contains(searchTerm) ||
                    entry.remarkProperty().get().toLowerCase().contains(searchTerm)) {
                searchResults.add(entry);
            }
        }
        if (searchResults.isEmpty()) {
            showInfo("Search Results", "No matching records found");
        } else {
            billingTable.setItems(searchResults);
            showInfo("Search Results", searchResults.size() + " matching records found");
        }
    }

    private void handleAddCsv() {
        showInfo("CSV Import", "CSV import functionality would be implemented here.");
    }

    private String getSelectedCategory() {
        RadioButton selectedRadio = (RadioButton) categoryGroup.getSelectedToggle();
        return selectedRadio != null ? (String) selectedRadio.getUserData() : "Living costs";
    }

    private String getCategoryIconPath(String category) {
        switch (category) {
            case "Living costs": return "/images/living_costs.png";
            case "Communication": return "/images/communication.png";
            case "Salary": return "/images/salary.png";
            case "Cosmetics": return "/images/cosmetics.png";
            default: return null;
        }
    }

    private void clearInputFields() {
        productField.clear();
        priceField.clear();
        livingCostsRadio.setSelected(true);
        timePicker.setValue(LocalDate.now());
        billingTable.setItems(billingData);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class BillingEntry {
        private final StringProperty category;
        private final StringProperty product;
        private final DoubleProperty price;
        private final ObjectProperty<LocalDate> time;
        private final StringProperty timeHour;
        private final StringProperty formattedTime;
        private final StringProperty remark;

        public BillingEntry(String category, String product, double price, LocalDate time, String timeHour) {
            this.category = new SimpleStringProperty(category);
            this.product = new SimpleStringProperty(product);
            this.price = new SimpleDoubleProperty(price);
            this.time = new SimpleObjectProperty<>(time);
            this.timeHour = new SimpleStringProperty(timeHour);
            this.formattedTime = new SimpleStringProperty(
                    time.getYear() + "." +
                            String.format("%02d", time.getMonthValue()) + "." +
                            String.format("%02d", time.getDayOfMonth()) + " " + timeHour);
            this.remark = new SimpleStringProperty(product);
        }

        public StringProperty categoryProperty() { return category; }
        public StringProperty productProperty() { return product; }
        public DoubleProperty priceProperty() { return price; }
        public ObjectProperty<LocalDate> timeProperty() { return time; }
        public StringProperty timeHourProperty() { return timeHour; }
        public StringProperty formattedTimeProperty() { return formattedTime; }
        public StringProperty remarkProperty() { return remark; }
    }
}