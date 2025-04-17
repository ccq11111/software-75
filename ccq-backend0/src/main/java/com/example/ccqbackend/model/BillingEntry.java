package com.example.ccqbackend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonFormat;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import com.example.ccqbackend.util.LocalDateAdapter;
import com.example.ccqbackend.util.LocalDateTimeAdapter;
import com.example.ccqbackend.util.LocalTimeAdapter;

@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement(name = "billingEntry")
@XmlAccessorType(XmlAccessType.FIELD)
public class BillingEntry {

    @XmlElement
    private String category;
    
    @XmlElement
    private String product;
    
    @XmlElement
    private BigDecimal price;
    
    @XmlElement
    private String remark;
    
    @XmlElement
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime entryDateTime;
    
    @XmlElement
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    
    @XmlElement
    @XmlJavaTypeAdapter(LocalTimeAdapter.class)
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime time;
    
    @XmlElement
    private String formattedTime;

    // 构造函数
    public BillingEntry() {
        // 默认构造函数
    }
    
    public BillingEntry(String category, String product, BigDecimal price, String remark) {
        this.category = category;
        this.product = product;
        this.price = price;
        this.remark = remark;
        this.entryDateTime = LocalDateTime.now();
        this.date = entryDateTime.toLocalDate();
        this.time = entryDateTime.toLocalTime();
        this.formattedTime = String.format("%02d:%02d", time.getHour(), time.getMinute());
    }

    // Getters and Setters
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getEntryDateTime() {
        return entryDateTime;
    }

    public void setEntryDateTime(LocalDateTime entryDateTime) {
        this.entryDateTime = entryDateTime;
        if (entryDateTime != null) {
            this.date = entryDateTime.toLocalDate();
            this.time = entryDateTime.toLocalTime();
            this.formattedTime = String.format("%02d:%02d", time.getHour(), time.getMinute());
        }
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
        updateEntryDateTime();
    }
    
    public LocalTime getTime() {
        return time;
    }
    
    public void setTime(LocalTime time) {
        this.time = time;
        if (time != null) {
            this.formattedTime = String.format("%02d:%02d", time.getHour(), time.getMinute());
        }
        updateEntryDateTime();
    }
    
    public String getFormattedTime() {
        return formattedTime;
    }
    
    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }
    
    private void updateEntryDateTime() {
        if (date != null && time != null) {
            this.entryDateTime = LocalDateTime.of(date, time);
        }
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s", 
            category, product, price != null ? price.toString() : "0", remark, 
            entryDateTime != null ? entryDateTime.toString() : "");
    }
}
