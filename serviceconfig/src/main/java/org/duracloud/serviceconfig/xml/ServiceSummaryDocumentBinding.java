/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig.xml;

import org.apache.xmlbeans.XmlException;
import org.duracloud.ServiceSummaryDocument;
import org.duracloud.SingleServiceSummaryType;
import org.duracloud.serviceconfig.ServiceSummary;

import java.io.IOException;
import java.io.InputStream;


/**
 * This class is a helper utility for binding single ServiceSummary objects to a
 * single-service xml config document.
 *
 * @author Andrew Woods
 *         Date: Jun 22, 2011
 */
public class ServiceSummaryDocumentBinding {

    /**
     * This method binds a ServiceSummary object to the content of the arg xml.
     *
     * @param xml single-service document to be bound to ServiceSummary object
     * @return ServiceSummary object
     */
    public static ServiceSummary createServiceSummaryFrom(InputStream xml) {
        try {
            ServiceSummaryDocument doc = ServiceSummaryDocument.Factory.parse(
                xml);
            return ServiceSummaryElementReader.createServiceSummaryFrom(doc);

        } catch (XmlException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method serializes the arg ServiceSummary object into an xml document.
     *
     * @param serviceSummary ServiceSummary object to be serialized
     * @return single-service xml document
     */
    public static ServiceSummaryDocument createDocumentFrom(ServiceSummary serviceSummary) {
        ServiceSummaryDocument doc = ServiceSummaryDocument.Factory
            .newInstance();
        if (serviceSummary != null) {
            SingleServiceSummaryType serviceSummaryType = ServiceSummaryElementWriter
                .createSingleServiceSummaryElementFrom(serviceSummary);

            doc.setServiceSummary(serviceSummaryType);
        }
        return doc;
    }

}
