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
    @FXML private Button setButton;
    @FXML private Button addCsvButton;
    
    @FXML private TextField productField;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private DatePicker timePicker;
    
    @FXML private TableView<BillingEntry> billingTable;
    @FXML private TableColumn<BillingEntry, String> productColumn;
    @FXML private TableColumn<BillingEntry, Double> priceColumn;
    @FXML private TableColumn<BillingEntry, String> categoryColumn;
    @FXML private TableColumn<BillingEntry, LocalDate> timeColumn;

    private final ObservableList<BillingEntry> billingData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Set up the category combo box
        categoryComboBox.setItems(FXCollections.observableArrayList(
            "Food", "Transportation", "Entertainment", "Shopping", "Others"
        ));
        categoryComboBox.setValue("Food"); // Set default value

        // Configure table columns
        productColumn.setCellValueFactory(cellData -> cellData.getValue().productProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        timeColumn.setCellValueFactory(cellData -> cellData.getValue().timeProperty());

        billingTable.setItems(billingData);

        // Set current date as default for date picker
        timePicker.setValue(LocalDate.now());

        // Add button click handler
        addCsvButton.setOnAction(event -> handleAddEntry());

        // Menu button handlers
        billingButton.setOnAction(event -> System.out.println("Billing Details clicked"));
        summaryButton.setOnAction(event -> System.out.println("Summary clicked"));
        savingButton.setOnAction(event -> System.out.println("Saving clicked"));
        setButton.setOnAction(event -> System.out.println("Set clicked"));
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

            billingData.add(new BillingEntry(product, price, category, date));
            clearInputFields();
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid price");
        }
    }

    private void clearInputFields() {
        productField.clear();
        priceField.clear();
        categoryComboBox.setValue("Food");
        timePicker.setValue(LocalDate.now());
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setUsername(String username) {
        usernameLabel.setText(username);
    }

    public static class BillingEntry {
        private final StringProperty product;
        private final DoubleProperty price;
        private final StringProperty category;
        private final ObjectProperty<LocalDate> time;

        public BillingEntry(String product, double price, String category, LocalDate time) {
            this.product = new SimpleStringProperty(product);
            this.price = new SimpleDoubleProperty(price);
            this.category = new SimpleStringProperty(category);
            this.time = new SimpleObjectProperty<>(time);
        }

        public StringProperty productProperty() { return product; }
        public DoubleProperty priceProperty() { return price; }
        public StringProperty categoryProperty() { return category; }
        public ObjectProperty<LocalDate> timeProperty() { return time; }
    }
}