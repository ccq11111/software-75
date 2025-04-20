package com.example.loginapp.api;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mock implementation of the billing service for the test account
 * This implementation works completely offline without any network connections
 */
public class MockBillingService implements BillingService {

    private static final Map<String, BillingEntry> billingEntries = new HashMap<>();

    static {
        // Add some sample billing entries
        addSampleBillingEntries();
    }

    private static void addSampleBillingEntries() {
        // Sample entry 1: Groceries
        String entryId1 = "entry-" + UUID.randomUUID().toString();
        BillingEntry entry1 = new BillingEntry(
            entryId1,
            "Groceries",
            "Weekly shopping",
            new BigDecimal("120.50"),
            LocalDate.now().minusDays(2),
            LocalTime.of(14, 30),
            "Bought fruits, vegetables, and meat"
        );
        billingEntries.put(entryId1, entry1);

        // Sample entry 2: Dining
        String entryId2 = "entry-" + UUID.randomUUID().toString();
        BillingEntry entry2 = new BillingEntry(
            entryId2,
            "Dining",
            "Restaurant dinner",
            new BigDecimal("85.75"),
            LocalDate.now().minusDays(1),
            LocalTime.of(19, 45),
            "Dinner with friends"
        );
        billingEntries.put(entryId2, entry2);

        // Sample entry 3: Transportation
        String entryId3 = "entry-" + UUID.randomUUID().toString();
        BillingEntry entry3 = new BillingEntry(
            entryId3,
            "Transportation",
            "Taxi fare",
            new BigDecimal("25.00"),
            LocalDate.now().minusDays(3),
            LocalTime.of(9, 15),
            "Taxi to work"
        );
        billingEntries.put(entryId3, entry3);

        // Sample entry 4: Entertainment
        String entryId4 = "entry-" + UUID.randomUUID().toString();
        BillingEntry entry4 = new BillingEntry(
            entryId4,
            "Entertainment",
            "Movie tickets",
            new BigDecimal("45.00"),
            LocalDate.now().minusDays(5),
            LocalTime.of(20, 0),
            "Movie night with family"
        );
        billingEntries.put(entryId4, entry4);

        // Sample entry 5: Shopping
        String entryId5 = "entry-" + UUID.randomUUID().toString();
        BillingEntry entry5 = new BillingEntry(
            entryId5,
            "Shopping",
            "Clothing",
            new BigDecimal("150.25"),
            LocalDate.now().minusDays(7),
            LocalTime.of(15, 20),
            "New clothes for summer"
        );
        billingEntries.put(entryId5, entry5);
    }

    @Override
    public BillingEntryResponse createEntry(String category, String product, BigDecimal price,
                                           LocalDate date, LocalTime time, String remark) throws ApiException {
        // Create a new billing entry
        String entryId = "entry-" + UUID.randomUUID().toString();
        BillingEntry entry = new BillingEntry(
            entryId,
            category,
            product,
            price,
            date,
            time,
            remark
        );

        // Save the entry
        billingEntries.put(entryId, entry);

        // Return the response
        return new BillingEntryResponse(entry);
    }

    @Override
    public List<BillingEntry> getEntries(LocalDate startDate, LocalDate endDate,
                                        String category, String searchTerm) throws ApiException {
        // Filter entries based on criteria
        List<BillingEntry> filteredEntries = new ArrayList<>(billingEntries.values());

        // Apply start date filter
        if (startDate != null) {
            filteredEntries = filteredEntries.stream()
                .filter(entry -> entry.getDate().isEqual(startDate) || entry.getDate().isAfter(startDate))
                .collect(Collectors.toList());
        }

        // Apply end date filter
        if (endDate != null) {
            filteredEntries = filteredEntries.stream()
                .filter(entry -> entry.getDate().isEqual(endDate) || entry.getDate().isBefore(endDate))
                .collect(Collectors.toList());
        }

        // Apply category filter
        if (category != null && !category.isEmpty()) {
            filteredEntries = filteredEntries.stream()
                .filter(entry -> entry.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
        }

        // Apply search term filter
        if (searchTerm != null && !searchTerm.isEmpty()) {
            filteredEntries = filteredEntries.stream()
                .filter(entry ->
                    entry.getProduct().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    entry.getRemark().toLowerCase().contains(searchTerm.toLowerCase()))
                .collect(Collectors.toList());
        }

        return filteredEntries;
    }

    @Override
    public BillingEntryResponse updateEntry(String entryId, String category, String product,
                                          BigDecimal price, LocalDate date, LocalTime time,
                                          String remark) throws ApiException {
        // Check if the entry exists
        if (!billingEntries.containsKey(entryId)) {
            throw new ApiException(
                new ApiError("NOT_FOUND", "Billing entry not found"),
                404
            );
        }

        // Get the existing entry
        BillingEntry existingEntry = billingEntries.get(entryId);

        // Update the entry
        existingEntry.setCategory(category);
        existingEntry.setProduct(product);
        existingEntry.setPrice(price);
        existingEntry.setDate(date);
        existingEntry.setTime(time);
        existingEntry.setRemark(remark);

        // Return the response
        return new BillingEntryResponse(existingEntry);
    }

    @Override
    public ApiResponse deleteEntry(String entryId) throws ApiException {
        // Check if the entry exists
        if (!billingEntries.containsKey(entryId)) {
            throw new ApiException(
                new ApiError("NOT_FOUND", "Billing entry not found"),
                404
            );
        }

        // Delete the entry
        billingEntries.remove(entryId);

        // Return the response
        return new ApiResponse(true, "Entry deleted successfully");
    }

    @Override
    public ImportResponse importFromCsv(File file) throws ApiException {
        // For the mock implementation, just pretend to import some entries
        int entriesImported = 5;
        int entriesSkipped = 2;

        // Return the response
        return new ImportResponse(entriesImported, entriesSkipped);
    }
}
