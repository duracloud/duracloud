/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig;

import org.apache.xmlbeans.XmlObject;
import org.duracloud.ServiceDocument;
import org.duracloud.ServicesDocument;
import org.duracloud.serviceconfig.xml.ServiceDocumentBinding;
import org.duracloud.serviceconfig.xml.ServiceListDocumentBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * ServicesConfigDocument is the top-level abstraction for the entire set of
 * sevice-config settings for all applicable services.
 * It provides the ability to serialize and deserialize these settings.
 *
 * @author Andrew Woods
 *         Date: Nov 6, 2009
 */
public class ServicesConfigDocument {

    public static final String SCHEMA_VERSION = "0.2";

    /**
     * This method binds the arg xml to a list of ServiceInfo objects.
     *
     * @param xml multi-service xml document to be bound to ServiceInfo objects
     * @return list of ServiceInfo objects
     */
    public static List<ServiceInfo> getServiceList(InputStream xml) {
        return ServiceListDocumentBinding.createServiceListFrom(xml);
    }

    /**
     * This method binds the arg xml to a single ServiceInfo object.
     *
     * @param xml single-service xml document to be bound to ServiceInfo object
     * @return ServiceInfo object
     */
    public static ServiceInfo getService(InputStream xml) {
        return ServiceDocumentBinding.createServiceFrom(xml);
    }

    /**
     * This method serializes the arg list of ServiceInfo objects into a multi-
     * service xml config document.
     *
     * @param serviceList ServiceInfo objects to be serialized
     * @return multi-service xml config document
     */
    public static String getServiceListAsXML(List<ServiceInfo> serviceList) {
        ServicesDocument doc = ServiceListDocumentBinding.createDocumentFrom(
            serviceList);
        return docToString(doc);
    }

    /**
     * This method serializes the arg ServiceInfo object into a single-service
     * xml config document.
     *
     * @param service ServiceInfo object to be serialized
     * @return single-service xml config document
     */
    public static String getServiceAsXML(ServiceInfo service) {
        ServiceDocument doc = ServiceDocumentBinding.createDocumentFrom(service);
        return docToString(doc);
    }

    private static String docToString(XmlObject doc) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            doc.save(outputStream);
        } catch (IOException e) {
            // FIXME: add proper runtime exception
            throw new RuntimeException(e);
        } finally {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                // FIXME: add proper runtime exception
                throw new RuntimeException(e);
            }
        }
        return outputStream.toString();
    }

}
