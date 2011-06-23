/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig.xml;

import org.apache.xmlbeans.XmlException;
import org.duracloud.ServiceSummariesDocument;
import org.duracloud.ServiceSummaryType;
import org.duracloud.serviceconfig.ServiceSummary;
import org.duracloud.serviceconfig.ServicesConfigDocument;

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
public class ServiceSummaryListDocumentBinding {

    /**
     * This method binds a list of ServiceInfo objects to the content of the arg
     * multi-service xml.
     *
     * @param xml multi-service document to be bound to ServiceInfo objects
     * @return list of ServiceInfo objects
     */
    public static List<ServiceSummary> createServiceSummaryListFrom(InputStream xml) {
        ServiceSummariesDocument doc = null;
        try {
            doc = ServiceSummariesDocument.Factory.parse(xml);

        } catch (XmlException e) {
            // FIXME: add proper runtime exception
            throw new RuntimeException(e);
        } catch (IOException e) {
            // FIXME: add proper runtime exception
            throw new RuntimeException(e);
        }

        return ServiceSummaryElementReader.createServiceSummaryListFrom(doc);
    }

    /**
     * This method serializes a list of ServiceInfo objects into a
     * multi-service-config xml document.
     *
     * @param summaryList list of ServiceInfo objects to be serialized
     * @return multi-service xml document
     */
    public static ServiceSummariesDocument createDocumentFrom(List<ServiceSummary> summaryList) {
        ServiceSummariesDocument doc = ServiceSummariesDocument.Factory
            .newInstance();
        ServiceSummariesDocument.ServiceSummaries summariesType = getSummariesType();

        if (null != summaryList && summaryList.size() > 0) {
            ServiceSummaryType[] summaryTypes = new ServiceSummaryType[summaryList
                .size()];
            int i = 0;
            for (ServiceSummary summary : summaryList) {
                ServiceSummaryType summaryType = ServiceSummaryElementWriter.createElementFrom(
                    summary);
                summaryTypes[i++] = summaryType;
            }
            summariesType.setServiceSummaryArray(summaryTypes);

        } else if (summaryList.size() == 0) {
            summariesType.setServiceSummaryArray(new ServiceSummaryType[0]);
        }

        doc.setServiceSummaries(summariesType);
        return doc;
    }

    private static ServiceSummariesDocument.ServiceSummaries getSummariesType() {
        ServiceSummariesDocument.ServiceSummaries summariesType = ServiceSummariesDocument
            .ServiceSummaries
            .Factory
            .newInstance();
        summariesType.setSchemaVersion(ServicesConfigDocument.SCHEMA_VERSION);
        return summariesType;
    }

}
