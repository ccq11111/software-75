package com.example.software.service;

import com.example.software.api.BillingEntry;
import com.example.software.model.BillingEntryList;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class XMLService {

    // 将数据写入XML文件
    public void writeDataToXML(List<BillingEntry> entries, String filename) throws JAXBException {
        BillingEntryList entryList = new BillingEntryList();
        entryList.setEntries(entries);
        
        JAXBContext context = JAXBContext.newInstance(BillingEntryList.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(entryList, new File(filename));
    }
    
    // 从XML文件读取数据
    public List<BillingEntry> readDataFromXML(String filename) throws JAXBException {
        File file = new File(filename);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        
        JAXBContext context = JAXBContext.newInstance(BillingEntryList.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        BillingEntryList entryList = (BillingEntryList) unmarshaller.unmarshal(file);
        
        return entryList.getEntries();
    }
}

