/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.json;

import java.io.IOException;
import java.io.StringWriter;

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

/**
 * Handles serialization and deserialization of objects JSON objects into
 * Java using JAXB. The pojos where data is stored should include JAXB
 * annotations.
 *
 * @author Bill Branan
 * Date: 7/29/13
 */
public class JaxbJsonSerializer<T> {

    private Class type;
    private ObjectMapper mapper;

    /**
     * Creates a serializer which will work with the defined class type.
     *
     * @param type
     */
    public JaxbJsonSerializer(Class<T> type) {
        this.type = type;

        // Create mapper
        mapper = new ObjectMapper();
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);

        // Use JAX-B annotations
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
        mapper.getDeserializationConfig()
              .withAnnotationIntrospector(introspector);
        mapper.getSerializationConfig()
              .withAnnotationIntrospector(introspector);
    }

    public String serialize(T object) throws IOException {
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, object);
        return writer.toString();
    }

    public T deserialize(String json) throws IOException {
        return (T) mapper.readValue(json, type);
    }

}
