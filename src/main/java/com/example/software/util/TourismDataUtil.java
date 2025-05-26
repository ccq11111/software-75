package com.example.software.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 旅游目的地信息工具类
 */
public class TourismDataUtil {
    private static final String JSON_PATH = "/data/tourism_destinations.json";

    public static DestinationInfo getDestinationInfo(String city) {
        Map<String, DestinationInfo> destMap = loadData();
        return destMap.get(city);
    }

    private static Map<String, DestinationInfo> loadData() {
        Map<String, DestinationInfo> destMap = new HashMap<>();
        try (InputStream is = TourismDataUtil.class.getResourceAsStream(JSON_PATH)) {
            ObjectMapper mapper = new ObjectMapper();
            List<DestinationInfo> list = mapper.readValue(is, new TypeReference<List<DestinationInfo>>() {});
            for (DestinationInfo info : list) {
                destMap.put(info.name, info);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return destMap;
    }

    public static class DestinationInfo {
        public String name;
        public int avgIncome;
        public String specialties;
        public String attractions;
        public String tips;

        public DestinationInfo() {}
        public DestinationInfo(String name, int avgIncome, String specialties, String attractions, String tips) {
            this.name = name;
            this.avgIncome = avgIncome;
            this.specialties = specialties;
            this.attractions = attractions;
            this.tips = tips;
        }
    }
} 