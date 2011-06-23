/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig.xml;

import org.apache.commons.lang.StringUtils;
import org.duracloud.PropertiesType;
import org.duracloud.PropertyType;
import org.duracloud.ServiceSummaryType;
import org.duracloud.SingleServiceSummaryType;
import org.duracloud.serviceconfig.ServiceSummary;

import java.util.Map;

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
        int id = summary.getId();
        if (id >= 0) {
            summaryType.setId(id);
        }

        int deploymentId = summary.getDeploymentId();
        if (deploymentId >= 0) {
            summaryType.setDeploymentId(deploymentId);
        }

        String name = summary.getName();
        if (!StringUtils.isBlank(name)) {
            summaryType.setName(name);
        }

        String version = summary.getVersion();
        if (!StringUtils.isBlank(version)) {
            summaryType.setVersion(version);
        }

        Map<String, String> configs = summary.getConfigs();
        if (null != configs && !configs.isEmpty()) {
            populatePropertiesType(configs, summaryType.addNewConfigs());
        }

        Map<String, String> properties = summary.getProperties();
        if (null != properties && !properties.isEmpty()) {
            populatePropertiesType(properties, summaryType.addNewProperties());
        }
    }

    private static void populatePropertiesType(Map<String, String> map,
                                               PropertiesType propertiesType) {
        for (String key : map.keySet()) {
            PropertyType propertyType = propertiesType.addNewProperty();
            propertyType.setName(key);
            propertyType.setStringValue(map.get(key));
        }
    }

}
