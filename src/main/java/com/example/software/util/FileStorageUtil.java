package com.example.software.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class FileStorageUtil {
    private static final String DATA_DIR = "data";
    private final ObjectMapper objectMapper;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public FileStorageUtil() {
        this.objectMapper = new ObjectMapper();
        createDataDirectory();
    }

    private void createDataDirectory() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Could not create data directory", e);
        }
    }

    public <T> void saveToFile(String filename, T data) {
        lock.writeLock().lock();
        try {
            Path filePath = Paths.get(DATA_DIR, filename);
            objectMapper.writeValue(filePath.toFile(), data);
        } catch (IOException e) {
            throw new RuntimeException("Error saving to file: " + filename, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public <T> void saveListToFile(String filename, List<T> dataList) {
        lock.writeLock().lock();
        try {
            Path filePath = Paths.get(DATA_DIR, filename);
            objectMapper.writeValue(filePath.toFile(), dataList);
        } catch (IOException e) {
            throw new RuntimeException("Error saving list to file: " + filename, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public <T> T loadFromFile(String filename, Class<T> type) {
        lock.readLock().lock();
        try {
            Path filePath = Paths.get(DATA_DIR, filename);
            if (!Files.exists(filePath)) {
                return null;
            }
            return objectMapper.readValue(filePath.toFile(), type);
        } catch (IOException e) {
            throw new RuntimeException("Error loading from file: " + filename, e);
        } finally {
            lock.readLock().unlock();
        }
    }

    public <T> List<T> loadListFromFile(String filename, Class<T> type) {
        lock.readLock().lock();
        try {
            Path filePath = Paths.get(DATA_DIR, filename);
            if (!Files.exists(filePath)) {
                return new ArrayList<>();
            }
            CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, type);
            return objectMapper.readValue(filePath.toFile(), listType);
        } catch (IOException e) {
            throw new RuntimeException("Error loading list from file: " + filename, e);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean deleteFile(String filename) {
        lock.writeLock().lock();
        try {
            Path filePath = Paths.get(DATA_DIR, filename);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error deleting file: " + filename, e);
        } finally {
            lock.writeLock().unlock();
        }
    }
} 