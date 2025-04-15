package com.example.ccqbackend.service;

import com.example.ccqbackend.model.BillingEntry;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.util.List;

@Service
public class XMLService {

    // 将数据写入XML文件
    public void writeDataToXML(List<BillingEntry> entries, String filename) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(BillingEntry.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.marshal(entries, new File(filename));
    }
}

