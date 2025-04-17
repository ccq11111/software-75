package com.example.ccqbackend.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Service
public class FileService {

    // 将数据写入文本文件（TXT）
    public void writeDataToTXT(List<String> data, String filename) throws IOException {
        Path path = Paths.get(filename);
        Files.write(path, data, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    // 从文本文件读取数据
    public List<String> readDataFromTXT(String filename) throws IOException {
        return Files.readAllLines(Paths.get(filename));
    }
}
