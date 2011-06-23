/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig;

import org.apache.xmlbeans.XmlObject;
import org.duracloud.ServiceSummaryDocument;
import org.duracloud.serviceconfig.xml.ServiceSummaryDocumentBinding;
import org.duracloud.serviceconfig.xml.ServiceSummaryListDocumentBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * ServiceSummariesDocument is the top-level abstraction for the entire set of
 * service-summary details for all applicable services.
 * It provides the ability to serialize and deserialize these details.
 *
 * @author Andrew Woods
 *         Date: Jun 23, 2011
 */
public class ServiceSummariesDocument {

    /**
     * This method binds the arg xml to a list of ServiceSummary objects.
     *
     * @param xml multi-service xml document to bind to ServiceSummary objects
     * @return list of ServiceSummary objects
     */
    public static List<ServiceSummary> getServiceSummaryList(InputStream xml) {
        return ServiceSummaryListDocumentBinding.createServiceSummaryListFrom(
            xml);
    }

    /**
     * This method binds the arg xml to a single ServiceSummary object.
     *
     * @param xml single-service xml document to bind to ServiceSummary object
     * @return ServiceSummary object
     */
    public static ServiceSummary getServiceSummary(InputStream xml) {
        return ServiceSummaryDocumentBinding.createServiceSummaryFrom(xml);
    }

    /**
     * This method serializes the arg list of ServiceSummary objects into a
     * multi-service xml config document.
     *
     * @param summaryList ServiceSummary objects to be serialized
     * @return multi-service xml config document
     */
    public static String getServiceSummaryListAsXML(List<ServiceSummary> summaryList) {
        org.duracloud.ServiceSummariesDocument doc = ServiceSummaryListDocumentBinding
            .createDocumentFrom(summaryList);
        return docToString(doc);
    }

    /**
     * This method serializes the arg ServiceSummary object into a
     * single-service xml config document.
     *
     * @param summary ServiceSummary object to be serialized
     * @return single-service xml config document
     */
    public static String getServiceSummaryAsXML(ServiceSummary summary) {
        ServiceSummaryDocument doc = ServiceSummaryDocumentBinding.createDocumentFrom(
            summary);
        return docToString(doc);
    }

    private static String docToString(XmlObject doc) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            doc.save(outputStream);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return outputStream.toString();
    }

}
