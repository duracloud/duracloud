/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.xml;

import org.duracloud.common.xml.error.XmlSerializationException;
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
 * Handles the transfer of java beans to XML and back through JAXB
 *
 * @author: Bill Branan
 * Date: 7/8/11
 */
public class XmlSerializer<T> {

    private Class clazz;
    private String schemaName;
    private String schemaVersion;

    private JAXBContext context;
    private Schema schema;

    /**
     * Creates a serializer which will be used to handle serializations
     * to and from the given top level class, using the given schema.
     *
     * @param clazz class which should be annotated as an XmlRootElement
     * @param schemaName name of the schema to use for validation
     * @param schemaVersion version of the schema expected
     */
    protected XmlSerializer(Class clazz,
                            String schemaName,
                            String schemaVersion) {
        this.clazz = clazz;
        this.schemaName = schemaName;
        this.schemaVersion = schemaVersion;
        try {
            context = JAXBContext.newInstance(clazz);
        } catch(JAXBException e) {
            throw new XmlSerializationException("Exception encountered " +
                                                "creating serializer: " +
                                                getErrorMsg(e), e);
        }

        try {
            SchemaFactory factory =
                SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Source schemaSource =
                new StreamSource(getClass().getClassLoader()
                    .getResourceAsStream(schemaName));
            schema = factory.newSchema(schemaSource);
        } catch(SAXException e) {
            throw new XmlSerializationException("Unable to load schema for " +
                                                "validation due to: " +
                                                e.getMessage());
        }
    }

    /**
     * Serializes the data stored within a java bean to XML. The bean and any
     * ancillary beans should include JAXB binding annotations.
     *
     * @param obj to serialize
     * @return XML
     */
    public String serialize(T obj) {
        try {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter writer = new StringWriter();
            marshaller.marshal(obj, writer);
            return writer.toString();
        } catch(JAXBException e) {
            throw new XmlSerializationException("Exception encountered " +
                                                "serializing report: " +
                                                getErrorMsg(e), e);
        }
    }

    /**
     * De-serializes XML into an object structure.
     *
     * @param xml to de-serialize
     * @return de-serialized object
     */
    public T deserialize(String xml) {
        if(xml == null || xml.equals("")) {
            throw new RuntimeException("XML cannot be null or empty");
        } else {
            return deserialize(new StreamSource(new StringReader(xml)));
        }
    }

    /**
     * De-serializes XML from an InputStream into an object structure.
     *
     * @param stream containing XML to de-serialize
     * @return de-serialized object
     */
    public T deserialize(InputStream stream) {
        if(stream == null) {
            throw new RuntimeException("Stream cannot be null");
        } else {
            return deserialize(new StreamSource(stream));
        }
    }

    private T deserialize(StreamSource stream) {
        try {
            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setSchema(schema); // turn on schema validation
            JAXBElement<T> report = unmarshaller.unmarshal(stream, clazz);
            return report.getValue();
        } catch(JAXBException e) {
            String error = "Exception encountered de-serializing xml " +
                           "using schema " + schemaName +" at version " +
                           schemaVersion + ": " + getErrorMsg(e);
            throw new XmlSerializationException(error, e);
        }
    }

    private String getErrorMsg(Throwable error) {
        while(null != error && null == error.getMessage()) {
            error = error.getCause();
        }
        return error.getMessage();
    }

}
