/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reportdata.storage.serialize;

import org.duracloud.reportdata.storage.StorageReport;
import org.duracloud.reportdata.storage.StorageReportBase;
import org.duracloud.reportdata.storage.error.SerializationException;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author: Bill Branan
 * Date: 7/8/11
 */
public class StorageReportSerializerBase<T> {

    private Class clazz;
    private JAXBContext context;
    private Schema schema;



    protected StorageReportSerializerBase(Class clazz) {
        this.clazz = clazz;
        try {
            context = JAXBContext.newInstance(clazz);
        } catch(JAXBException e) {
            throw new SerializationException("Exception encountered " +
                                             "creating serializer: " +
                                             getErrorMsg(e), e);
        }

        try {
            SchemaFactory factory =
                SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Source schemaSource =
                new StreamSource(getClass().getClassLoader()
                    .getResourceAsStream(StorageReportBase.SCHEMA_NAME));
            schema = factory.newSchema(schemaSource);
        } catch(SAXException e) {
            throw new SerializationException("Unable to load schema for " +
                                             "storage report validation " +
                                             "due to: " + e.getMessage());
        }
    }

    public String serialize(Object report) {
        try {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            StringWriter writer = new StringWriter();
            marshaller.marshal(report, writer);
            return writer.toString();
        } catch(JAXBException e) {
            throw new SerializationException("Exception encountered " +
                                             "serializing report: " +
                                             getErrorMsg(e), e);
        }
    }

    public T deserialize(String xml) {
        if(xml == null || xml.equals("")) {
            throw new RuntimeException("Report XML cannot be null or empty");
        } else {
            return deserialize(new StreamSource(new StringReader(xml)));
        }
    }

    public T deserialize(InputStream stream) {
        if(stream == null) {
            throw new RuntimeException("Report stream cannot be null");
        } else {
            return deserialize(new StreamSource(stream));
        }
    }

    private T deserialize(StreamSource stream) {
        try {
            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setSchema(schema); // turn on schema validation
            JAXBElement<StorageReport> report =
                unmarshaller.unmarshal(stream, clazz);
            return (T)report.getValue();
        } catch(JAXBException e) {
            String error = "Exception encountered de-serializing report " +
                            "against storage report schema version " +
                            StorageReportBase.SCHEMA_VERSION + ": " +
                            getErrorMsg(e);
            throw new SerializationException(error, e);
        }
    }

    private String getErrorMsg(Throwable error) {
        while(null != error && null == error.getMessage()) {
            error = error.getCause();
        }
        return error.getMessage();
    }

}
