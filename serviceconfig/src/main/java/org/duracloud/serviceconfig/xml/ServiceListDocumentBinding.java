/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig.xml;

import org.apache.xmlbeans.XmlException;
import org.duracloud.ServiceType;
import org.duracloud.ServicesDocument;
import org.duracloud.serviceconfig.ServiceInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * This class is a helper utility for binding multiple ServiceInfo objects to a
 * multi-service xml config document.
 *
 * @author Andrew Woods
 *         Date: Nov 17, 2009
 */
public class ServiceListDocumentBinding {

    /**
     * This method binds a list of ServiceInfo objects to the content of the arg
     * multi-service xml.
     *
     * @param xml multi-service document to be bound to ServiceInfo objects
     * @return list of ServiceInfo objects
     */
    public static List<ServiceInfo> createServiceListFrom(InputStream xml) {
        ServicesDocument doc = null;
        try {
            doc = ServicesDocument.Factory.parse(xml);
        } catch (XmlException e) {
            // FIXME: add proper runtime exception
            throw new RuntimeException(e);
        } catch (IOException e) {
            // FIXME: add proper runtime exception
            throw new RuntimeException(e);
        }

        return ServiceElementReader.createServiceListFrom(doc);
    }

    /**
     * This method serializes a list of ServiceInfo objects into a
     * multi-service-config xml document.
     *
     * @param serviceList list of ServiceInfo objects to be serialized
     * @return multi-service xml document
     */
    public static ServicesDocument createDocumentFrom(List<ServiceInfo> serviceList) {
        ServicesDocument doc = ServicesDocument.Factory.newInstance();
        if (null != serviceList && serviceList.size() > 0) {

            ServiceType[] serviceTypes = new ServiceType[serviceList.size()];
            int i = 0;
            for (ServiceInfo serviceInfo : serviceList) {
                ServiceType serviceType = ServiceElementWriter.createElementFrom(
                    serviceInfo);

                serviceTypes[i++] = serviceType;
            }

            ServicesDocument.Services servicesType =
                ServicesDocument.Services.Factory.newInstance();
            servicesType.setServiceArray(serviceTypes);
            doc.setServices(servicesType);
        } else if(serviceList.size() == 0) {
            ServicesDocument.Services servicesType =
                ServicesDocument.Services.Factory.newInstance();
            servicesType.setServiceArray(new ServiceType[0]);
            doc.setServices(servicesType);
        }
        return doc;
    }

}
