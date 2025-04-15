package com.example.ccqbackend.service;
import com.example.ccqbackend.model.BillingEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class JSONService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 将数据写入JSON文件
    public void writeDataToJSON(List<BillingEntry> entries, String filename) throws IOException {
        objectMapper.writeValue(new File(filename), entries);
    }

    // 从JSON文件读取数据
    public List<BillingEntry> readDataFromJSON(String filename) throws IOException {
        return objectMapper.readValue(new File(filename), objectMapper.getTypeFactory().constructCollectionType(List.class, BillingEntry.class));
    }
}
