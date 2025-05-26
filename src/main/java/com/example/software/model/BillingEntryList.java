package com.example.software.model;

import com.example.software.api.BillingEntry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "billingEntries")
@XmlAccessorType(XmlAccessType.FIELD)
public class BillingEntryList {

    @XmlElement(name = "entry")
    private List<BillingEntry> entries = new ArrayList<>();

    public List<BillingEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<BillingEntry> entries) {
        this.entries = entries;
    }
} 