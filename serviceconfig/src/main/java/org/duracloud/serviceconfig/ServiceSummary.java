/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig;

import org.duracloud.common.util.DateUtil;
import org.duracloud.services.ComputeService;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

/**
 * This bean contains the complete details of a service.
 * Note: this class has a natural ordering that is inconsistent with equals.
 *
 * @author Andrew Woods
 *         Date: 6/22/11
 */
public class ServiceSummary implements Comparable<ServiceSummary> {

    /** Unique identifier for this service */
    private int id;

    /** Unique identifier for this service deployment */
    private int deploymentId;

    /** User-friendly Service name */
    private String name;

    /** Release version number of the service software */
    private String version;

    /** User configuration for this service deployment */
    private Map<String, String> configs;

    /** Properties of this service deployment */
    private Map<String, String> properties;

    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(int deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getConfigs() {
        return configs;
    }

    public void setConfigs(Map<String, String> configs) {
        this.configs = configs;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    /**
     * Compares this ServiceSummary against another using the stop time or
     * start time properties to determine which service has had the most
     * recent state update.
     *
     * Returns 1 if this object has changed state (stopped/started)
     *           more recently than the given object
     * Returns 0 if the two change state at the same time or there is not
     *           enough information to determine a difference
     * Returns -1 if the given object has changed state (stopped/started)
     *            more recently than this object
     *
     * @param ss summary to compare with this one
     * @return -1, 0, or 1
     */
    @Override
    public int compareTo(ServiceSummary ss) {
        Date compareTime = null;
        Date ssCompareTime = null;

        // Determine the best date to use for this summary
        Map<String, String> props = getProperties();
        if(null != props) {
            compareTime = toDate(props.get(ComputeService.STOPTIME_KEY));
            if(null == compareTime) {
                compareTime = toDate(props.get(ComputeService.STARTTIME_KEY));
            }
        }

        // Determine the best date to use for the provided summary
        if(null != ss) {
            Map<String, String> ssProps = ss.getProperties();
            if(null != ssProps) {
                ssCompareTime =
                    toDate(ssProps.get(ComputeService.STOPTIME_KEY));
                if(null == ssCompareTime) {
                    ssCompareTime =
                        toDate(ssProps.get(ComputeService.STARTTIME_KEY));
                }
            }
        }

        // Handle one or the other having no comparable date
        if(null == compareTime && null == ssCompareTime) {
            return 0;
        } else if(null != compareTime && null == ssCompareTime) {
            return -1;
        } else if(null == compareTime && null != ssCompareTime) {
            return 1;
        } else { // Both dates are non-null
            if(compareTime.equals(ssCompareTime)) {
                return 0;
            } else if(compareTime.before(ssCompareTime)) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    /*
     * Returns the Date encoded in this String, or null if the input is null
     * or otherwise cannot be converted.
     */
    private Date toDate(String input) {
        Date date = null;
        if(null != input) {
            try {
                date = DateUtil.convertToDate(input);
            } catch(ParseException e) {
                date = null;
            }
        }
        return date;
    }

}
