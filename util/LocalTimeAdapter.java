package com.example.software.util;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LocalTimeAdapter extends XmlAdapter<String, LocalTime> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_TIME;

    @Override
    public LocalTime unmarshal(String v) throws Exception {
        return v != null ? LocalTime.parse(v, formatter) : null;
    }

    @Override
    public String marshal(LocalTime v) throws Exception {
        return v != null ? v.format(formatter) : null;
    }
} 