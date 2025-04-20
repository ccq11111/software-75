package com.example.loginapp;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.io.IOException;

public class BillingViewController {
    @FXML private Label usernameLabel; // Only needed for displaying username
    @FXML private Button addCsvButton;

    @FXML private TextField productField;
    @FXML private TextField priceField;
    @FXML private DatePicker timePicker;

    // Category radio buttons
    @FXML private ToggleGroup categoryGroup;
    @FXML private RadioButton livingCostsRadio;
    @FXML private RadioButton communicationRadio;
    @FXML private RadioButton salaryRadio;
    @FXML private RadioButton cosmeticsRadio;

    @FXML private Button resetButton;
    @FXML private Button buildButton;
    @FXML private Button inquireButton;

    // Removed ComboBox as we're now using radio buttons

    @FXML private TableView<BillingEntry> billingTable;
    @FXML private TableColumn<BillingEntry, String> categoryColumn;
    @FXML private TableColumn<BillingEntry, String> productColumn;
    @FXML private TableColumn<BillingEntry, String> timeColumn;
    @FXML private TableColumn<BillingEntry, Double> priceColumn;
    @FXML private TableColumn<BillingEntry, String> remarkColumn;

    private final ObservableList<BillingEntry> billingData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
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

        // Custom cell factory for category column to show icons
        categoryColumn.setCellFactory(column -> new TableCell<BillingEntry, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Create an ImageView with the appropriate icon based on category
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

        // Set current date as default for date picker
        timePicker.setValue(LocalDate.now());

        // Add sample data
        addSampleData();

        // Button handlers
        addCsvButton.setOnAction(event -> handleAddCsv());
        resetButton.setOnAction(event -> clearInputFields());
        buildButton.setOnAction(event -> handleAddRecord()); // Build button adds a record
        inquireButton.setOnAction(event -> handleFuzzySearch()); // Inquire button performs fuzzy search

        // We no longer need menu button handlers as they are handled by BaseViewController
    }

    private void addSampleData() {
        // Add sample data to match the screenshot
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

        // Create a filtered list for the search results
        ObservableList<BillingEntry> searchResults = FXCollections.observableArrayList();

        // Perform fuzzy search on product and remark fields
        for (BillingEntry entry : billingData) {
            if (entry.productProperty().get().toLowerCase().contains(searchTerm) ||
                entry.remarkProperty().get().toLowerCase().contains(searchTerm)) {
                searchResults.add(entry);
            }
        }

        // Update the table with search results
        if (searchResults.isEmpty()) {
            showInfo("Search Results", "No matching records found");
        } else {
            billingTable.setItems(searchResults);
            showInfo("Search Results", searchResults.size() + " matching records found");
        }
    }

    private void handleAddCsv() {
        // This would handle importing data from a CSV file
        showInfo("CSV Import", "CSV import functionality would be implemented here.");
    }

    // Helper method to get the selected category from radio buttons
    private String getSelectedCategory() {
        RadioButton selectedRadio = (RadioButton) categoryGroup.getSelectedToggle();
        if (selectedRadio != null) {
            return (String) selectedRadio.getUserData();
        }
        return "Living costs"; // Default
    }

    // Helper method to get the icon path for a category
    private String getCategoryIconPath(String category) {
        switch (category) {
            case "Living costs":
                return "/images/living_costs.png";
            case "Communication":
                return "/images/communication.png";
            case "Salary":
                return "/images/salary.png";
            case "Cosmetics":
                return "/images/cosmetics.png";
            default:
                return null;
        }
    }

    private void clearInputFields() {
        productField.clear();
        priceField.clear();
        livingCostsRadio.setSelected(true); // Reset to default category
        timePicker.setValue(LocalDate.now());

        // Reset table to show all data after a search
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

    // Navigation methods removed as they are now handled by BaseViewController

    // Store username as a field since we don't have the label in the content view
    private String username;

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