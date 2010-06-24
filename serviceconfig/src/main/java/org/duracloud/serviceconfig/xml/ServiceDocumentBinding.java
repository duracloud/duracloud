/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig.xml;

import org.apache.xmlbeans.XmlException;
import org.duracloud.ServiceDocument;
import org.duracloud.SingleServiceType;
import org.duracloud.serviceconfig.ServiceInfo;

import java.io.IOException;
import java.io.InputStream;


/**
 * This class is a helper utility for binding single ServiceInfo objects to a
 * single-service xml config document.
 *
 * @author Andrew Woods
 *         Date: Nov 17, 2009
 */
public class ServiceDocumentBinding {

    /**
     * This method binds a ServiceInfo object to the content of the arg xml.
     *
     * @param xml single-service document to be bound to ServiceInfo object
     * @return ServiceInfo object
     */
    public static ServiceInfo createServiceFrom(InputStream xml) {
        try {
            ServiceDocument doc = ServiceDocument.Factory.parse(xml);
            return ServiceElementReader.createServiceFrom(doc);
        } catch (XmlException e) {
            // FIXME: add proper runtime exception
            throw new RuntimeException(e);
        } catch (IOException e) {
            // FIXME: add proper runtime exception
            throw new RuntimeException(e);
        }

    }

    /**
     * This method serializes the arg ServiceInfo object into an xml document.
     * 
     * @param serviceInfo ServiceInfo object to be serialized
     * @return single-service xml document
     */
    public static ServiceDocument createDocumentFrom(ServiceInfo serviceInfo) {
        ServiceDocument doc = ServiceDocument.Factory.newInstance();
        if (serviceInfo != null) {
            SingleServiceType serviceType = ServiceElementWriter.createSingleServiceElementFrom(
                serviceInfo);

            doc.setService(serviceType);
        }
        return doc;
    }

}
