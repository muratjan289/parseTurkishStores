package com.turkish.turkishstores.service.xmlMethods;

import com.turkish.turkishstores.service.general.Item;
import com.turkish.turkishstores.service.general.items;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

public class CreateXml {



    public static String convertListToXML(List<Item> list) {
        try {
            JAXBContext context = JAXBContext.newInstance(items.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            items products = new items();
            products.setProductList(list);

            StringWriter writer = new StringWriter();
            marshaller.marshal(products, writer);

            System.out.println(writer);
            return writer.toString();
        } catch (PropertyException e) {
            throw new RuntimeException(e);
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void convertListToXMLAndSaveToFile(List<Item> list, String filePath) {
        String xmlContent = convertListToXML(list);
        if (xmlContent != null) {
            try (FileWriter fileWriter = new FileWriter(filePath)) {
                fileWriter.write(xmlContent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
