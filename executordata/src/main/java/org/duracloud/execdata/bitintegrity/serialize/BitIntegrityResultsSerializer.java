/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.execdata.bitintegrity.serialize;

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.duracloud.execdata.bitintegrity.BitIntegrityResults;

import java.io.IOException;
import java.io.StringWriter;

/**
 * @author: Bill Branan
 * Date: 3/20/12
 */
public class BitIntegrityResultsSerializer {

    private ObjectMapper mapper;

    public BitIntegrityResultsSerializer() {
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

    public String serialize(BitIntegrityResults results) throws IOException {
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, results);
        return writer.toString();
    }

    public BitIntegrityResults deserialize(String json) throws IOException {
        return mapper.readValue(json, BitIntegrityResults.class);
    }

}
