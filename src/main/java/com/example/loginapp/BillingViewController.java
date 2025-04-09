package com.example.loginapp;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.*;
import java.time.LocalDate;

public class BillingViewController {
    @FXML private Label usernameLabel;
    @FXML private TextField searchField;
    @FXML private Button billingButton;
    @FXML private Button summaryButton;
    @FXML private Button savingButton;
    @FXML private Button footprintButton;
    @FXML private Button setButton;
    @FXML private Button addCsvButton;

    @FXML private TextField productField;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private DatePicker timePicker;

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

    @FXML
    public void initialize() {
        // Set up the category combo box
        categoryComboBox.setItems(FXCollections.observableArrayList(
            "Living costs", "Communication", "Salary", "Cosmetics", "Food", "Transportation", "Entertainment", "Shopping", "Others"
        ));
        categoryComboBox.setValue("Living costs"); // Set default value

        // Configure table columns
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        productColumn.setCellValueFactory(cellData -> cellData.getValue().productProperty());
        timeColumn.setCellValueFactory(cellData -> cellData.getValue().formattedTimeProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        remarkColumn.setCellValueFactory(cellData -> cellData.getValue().remarkProperty());

        // Custom cell factory for price column to show positive/negative values with colors
        priceColumn.setCellFactory(column -> new TableCell<BillingEntry, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    // Format with ¥ symbol and set color based on value
                    String formattedPrice = String.format("%s¥%.2f", item >= 0 ? "+" : "-", Math.abs(item));
                    setText(formattedPrice);

                    if (item >= 0) {
                        setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                    }
                }
            }
        });

        billingTable.setItems(billingData);

        // Set current date as default for date picker
        timePicker.setValue(LocalDate.now());

        // Add sample data
        addSampleData();

        // Button handlers
        addCsvButton.setOnAction(event -> handleAddCsv());
        resetButton.setOnAction(event -> clearInputFields());
        buildButton.setOnAction(event -> handleBuild());
        inquireButton.setOnAction(event -> handleAddEntry());

        // Menu button handlers
        billingButton.setOnAction(event -> System.out.println("Billing Details clicked"));
        summaryButton.setOnAction(event -> System.out.println("Summary clicked"));
        savingButton.setOnAction(event -> System.out.println("Saving clicked"));
        footprintButton.setOnAction(event -> System.out.println("Footprint clicked"));
        setButton.setOnAction(event -> System.out.println("Set clicked"));
    }

    private void addSampleData() {
        // Add sample data to match the screenshot
        billingData.add(new BillingEntry("Living costs", "Nov.utilities bill", -200.00, LocalDate.of(2024, 12, 24), "14:29"));
        billingData.add(new BillingEntry("Communication", "Dad phone bill", -200.00, LocalDate.of(2024, 12, 23), "14:29"));
        billingData.add(new BillingEntry("Salary", "Nov.salary", 18800.00, LocalDate.of(2024, 12, 21), "14:29"));
        billingData.add(new BillingEntry("Cosmetics", "-", -300.00, LocalDate.of(2024, 12, 13), "14:29"));
    }

    private void handleAddEntry() {
        try {
            String product = productField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());
            String category = categoryComboBox.getValue();
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

    private void handleAddCsv() {
        // This would handle importing data from a CSV file
        showInfo("CSV Import", "CSV import functionality would be implemented here.");
    }

    private void handleBuild() {
        // This would handle building reports or charts
        showInfo("Build Report", "Report building functionality would be implemented here.");
    }

    private void clearInputFields() {
        productField.clear();
        priceField.clear();
        categoryComboBox.setValue("Living costs");
        timePicker.setValue(LocalDate.now());
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

    public void setUsername(String username) {
        usernameLabel.setText(username);
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
                String.format("%02d", time.getDayOfMonth()) + " " +
                timeHour
            );
            this.remark = new SimpleStringProperty(product); // Using product as remark for now
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