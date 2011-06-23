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
import org.duracloud.ServiceSummariesDocument;
import org.duracloud.ServiceSummaryDocument;
import org.duracloud.ServiceSummaryType;
import org.duracloud.SingleServiceSummaryType;
import org.duracloud.serviceconfig.ServiceSummary;
import org.duracloud.serviceconfig.ServicesConfigDocument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for binding service-config xml documents to
 * ServiceSummary objects.
 *
 * @author Andrew Woods
 *         Date: Jun 22, 2011
 */
public class ServiceSummaryElementReader {

    /**
     * This method binds a multi-service xml document to a list of
     * ServiceSummary objects
     *
     * @param doc service-summary xml document
     * @return list of ServiceSummary objects
     */
    public static List<ServiceSummary> createServiceSummaryListFrom(
        ServiceSummariesDocument doc) {
        List<ServiceSummary> summaries = new ArrayList<ServiceSummary>();

        ServiceSummariesDocument.ServiceSummaries summariesType = doc.getServiceSummaries();
        checkSchemaVersion(summariesType.getSchemaVersion());

        ServiceSummaryType[] summaryTypes = summariesType.getServiceSummaryArray();
        if (null != summaryTypes && summaryTypes.length > 0) {
            for (ServiceSummaryType summaryType : summaryTypes) {
                summaries.add(createServiceSummaryFromElement(summaryType));
            }
        }

        return summaries;
    }

    /**
     * This method binds a single-service-summary xml document to a single
     * ServiceSummary object
     *
     * @param doc service-summary xml document
     * @return single ServiceSummary object
     */
    public static ServiceSummary createServiceSummaryFrom(ServiceSummaryDocument doc) {
        SingleServiceSummaryType singleServiceSummary = doc.getServiceSummary();
        checkSchemaVersion(singleServiceSummary.getSchemaVersion());

        return createServiceSummaryFromElement(singleServiceSummary);
    }

    private static void checkSchemaVersion(String schemaVersion) {
        if (!schemaVersion.equals(ServicesConfigDocument.SCHEMA_VERSION)) {
            throw new RuntimeException(
                "Unsupported schema version: " + schemaVersion);
        }
    }

    private static ServiceSummary createServiceSummaryFromElement(
        ServiceSummaryType summaryType) {

        ServiceSummary summary = new ServiceSummary();
        if (null == summaryType) {
            return summary;
        }

        int id = summaryType.getId();
        if (id >= 0) {
            summary.setId(id);
        }

        int deploymentId = summaryType.getDeploymentId();
        if (deploymentId >= 0) {
            summary.setDeploymentId(deploymentId);
        }

        String name = summaryType.getName();
        if (!StringUtils.isBlank(name)) {
            summary.setName(name);
        }

        String version = summaryType.getVersion();
        if (!StringUtils.isBlank(version)) {
            summary.setVersion(version);
        }

        Map<String, String> configs = new HashMap<String, String>();
        populateMap(configs, summaryType.getConfigs());
        summary.setConfigs(configs);

        Map<String, String> properties = new HashMap<String, String>();
        populateMap(properties, summaryType.getProperties());
        summary.setProperties(properties);

        return summary;
    }

    private static void populateMap(Map<String, String> map,
                                    PropertiesType propertiesType) {
        if (null != propertiesType) {
            PropertyType[] propertyTypes = propertiesType.getPropertyArray();
            if (null != propertyTypes && propertyTypes.length > 0) {
                for (PropertyType propertyType : propertyTypes) {
                    map.put(propertyType.getName(),
                            propertyType.getStringValue());
                }
            }
        }
    }

}
