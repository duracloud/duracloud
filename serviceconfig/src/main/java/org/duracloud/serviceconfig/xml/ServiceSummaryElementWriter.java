/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig.xml;

import java.util.Map;

import org.duracloud.ServicePropertiesType;
import org.duracloud.ServicePropertyType;
import org.duracloud.ServiceSummaryType;
import org.duracloud.ServiceType;
import org.duracloud.SingleServiceSummaryType;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.ServiceSummary;

/**
 * This class is responsible for serializing ServiceSummary objects into
 * service-summary xml documents.
 *
 * @author Andrew Woods
 *         Date: Jun 22, 2011
 */
public class ServiceSummaryElementWriter {

    /**
     * This method serializes a single ServiceSummary object into a
     * single-service-summary xml element.
     *
     * @param serviceSummary object to be serialized
     * @return xml service element with content from arg serviceSummary
     */
    public static SingleServiceSummaryType createSingleServiceSummaryElementFrom(
        ServiceSummary serviceSummary) {
        SingleServiceSummaryType singleServiceSummaryType = SingleServiceSummaryType
            .Factory
            .newInstance();
        populateElementFromObject(singleServiceSummaryType, serviceSummary);

        return singleServiceSummaryType;
    }

    /**
     * This method serializes a ServiceSummary object into a service-summary xml
     * element that will be embedded in document containing mulitple such elements.
     *
     * @param serviceSummary object to serialize
     * @return xml service element with content from arg serviceSummary
     */
    public static ServiceSummaryType createElementFrom(ServiceSummary serviceSummary) {
        ServiceSummaryType summaryType = ServiceSummaryType.Factory
            .newInstance();
        populateElementFromObject(summaryType, serviceSummary);

        return summaryType;
    }

    private static void populateElementFromObject(ServiceSummaryType summaryType,
                                                  ServiceSummary summary) {

        ServiceInfo serviceInfo = summary.getServiceInfo();
        if (null != serviceInfo) {
            ServiceType serviceType = summaryType.addNewService();
            ServiceElementWriter.populateElementFromObject(serviceType,
                                                           serviceInfo);
        }

        Map<String, String> properties = summary.getServiceProperties();
        if (null != properties && properties.size() > 0) {
            ServicePropertiesType propertiesType = summaryType.addNewProperties();

            for (String key : properties.keySet()) {
                ServicePropertyType propertyType = propertiesType.addNewProperty();
                propertyType.setName(key);
                propertyType.setStringValue(properties.get(key));
            }
        }
    }

}
