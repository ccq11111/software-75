package com.example.software.util;

import java.time.LocalDate;
import java.util.*;

/**
 * 节日工具类，支持常见中国节日的识别和查询
 */
public class HolidayUtil {
    // 仅支持公历节日，农历节日可扩展
    private static final Map<String, String> HOLIDAY_MAP = new HashMap<>();
    static {
        HOLIDAY_MAP.put("01-01", "元旦");
        HOLIDAY_MAP.put("02-14", "情人节");
        HOLIDAY_MAP.put("03-08", "妇女节");
        HOLIDAY_MAP.put("03-12", "植树节");
        HOLIDAY_MAP.put("04-01", "愚人节");
        HOLIDAY_MAP.put("04-05", "清明节");
        HOLIDAY_MAP.put("05-01", "劳动节");
        HOLIDAY_MAP.put("05-04", "青年节");
        HOLIDAY_MAP.put("06-01", "儿童节");
        HOLIDAY_MAP.put("07-01", "建党节");
        HOLIDAY_MAP.put("08-01", "建军节");
        HOLIDAY_MAP.put("09-10", "教师节");
        HOLIDAY_MAP.put("09-18", "九一八纪念日");
        HOLIDAY_MAP.put("10-01", "国庆节");
        HOLIDAY_MAP.put("10-31", "万圣节前夜");
        HOLIDAY_MAP.put("11-11", "光棍节");
        HOLIDAY_MAP.put("12-13", "国家公祭日");
        HOLIDAY_MAP.put("12-20", "澳门回归纪念日");
        HOLIDAY_MAP.put("12-25", "圣诞节");
        // 可扩展更多节日
    }

    /**
     * 判断某天是否为节日
     */
    public static Optional<String> getHoliday(LocalDate date) {
        String key = String.format("%02d-%02d", date.getMonthValue(), date.getDayOfMonth());
        if (HOLIDAY_MAP.containsKey(key)) {
            return Optional.of(HOLIDAY_MAP.get(key));
        }
        return Optional.empty();
    }

    /**
     * 获取未来N天内的最近节日
     */
    public static Optional<HolidayInfo> getUpcomingHoliday(LocalDate from, int days) {
        for (int i = 0; i <= days; i++) {
            LocalDate d = from.plusDays(i);
            Optional<String> holiday = getHoliday(d);
            if (holiday.isPresent()) {
                return Optional.of(new HolidayInfo(holiday.get(), d));
            }
        }
        return Optional.empty();
    }

    /**
     * 节日信息
     */
    public static class HolidayInfo {
        public final String name;
        public final LocalDate date;
        public HolidayInfo(String name, LocalDate date) {
            this.name = name;
            this.date = date;
        }
    }
} 