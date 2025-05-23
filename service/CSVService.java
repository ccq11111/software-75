package com.example.software.service;
import com.opencsv.CSVWriter;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CSVService {

    // 将数据写入CSV文件
    public void writeDataToCSV(List<String[]> data, String filename) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filename, true))) {
            writer.writeAll(data);
        }
    }

    // 从CSV文件读取数据
    public List<String[]> readDataFromCSV(String filename) throws IOException {
        // 解析 CSV 文件的逻辑
        return new ArrayList<>();
    }
}
