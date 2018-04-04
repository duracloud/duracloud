/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.xml;

import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

/**
 * Schema generator which can be used to generate a schema for pojo classes
 * which have been marked up with JAXB annotations.
 *
 * @author: Bill Branan
 * Date: 7/13/11
 */
public class SchemaGenerator extends SchemaOutputResolver {

    private File baseDir = new File("src/main/resources");
    private String fileName = null;

    /**
     * Creates a SchemaGenerator indicating the preferred name of the
     * generated schema file.
     *
     * @param fileName of the schema
     */
    public SchemaGenerator(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Creates a SchemaGenerator indicating the preferred name of the
     * generated schema file and the directory in which is should be stored.
     *
     * @param fileName  of the schema
     * @param schemaDir directory in which to create the schema
     */
    public SchemaGenerator(String fileName, File schemaDir) {
        this(fileName);
        if (null != schemaDir) {
            this.baseDir = schemaDir;
        }
    }

    /**
     * Generates an XML Schema which includes the given classes
     *
     * @param classes to include in the schema definition
     * @throws JAXBException
     * @throws IOException
     */
    public void generateSchema(Class... classes)
        throws JAXBException, IOException {
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }

        JAXBContext context = JAXBContext.newInstance(classes);
        context.generateSchema(this);
    }

    /**
     * Called by the schema generation process. There is no need to call
     * this method directly.
     */
    @Override
    public Result createOutput(String namespaceUri, String defaultFileName)
        throws IOException {
        if (null == fileName) {
            fileName = defaultFileName;
        }
        return new StreamResult(new File(baseDir, fileName));
    }
}