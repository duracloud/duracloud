/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reportdata.storage.serialize;

import org.duracloud.reportdata.storage.StorageReport;
import org.duracloud.reportdata.storage.error.SerializationException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
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

    protected StorageReportSerializerBase(Class clazz) {
        this.clazz = clazz;
        try {
            context = JAXBContext.newInstance(clazz);
        } catch(JAXBException e) {
            throw new SerializationException("Exception encountered " +
                                             "creating serializer: " +
                                             getErrorMsg(e), e);
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
            JAXBElement<StorageReport> report =
                unmarshaller.unmarshal(stream, clazz);
            return (T)report.getValue();
        } catch(JAXBException e) {
            throw new SerializationException("Exception encountered " +
                                             "de-serializing report: " +
                                             getErrorMsg(e), e);
        }
    }

    private String getErrorMsg(Throwable error) {
        while(null != error && null == error.getMessage()) {
            error = error.getCause();
        }
        return error.getMessage();
    }

}
