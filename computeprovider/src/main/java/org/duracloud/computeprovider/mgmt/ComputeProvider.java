/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.computeprovider.mgmt;

import java.net.URL;

import org.duracloud.common.model.Credential;

/**
 * <pre>
 * This interface exposes basic management capabilities of ComputeProviders:
 * start, stop, describe...
 *
 * It provides an abstraction layer from implementations such as:
 * Amazon-EC2 MicroSoft-Azure ...
 * </pre>
 *
 * @author Andrew Woods
 */
public interface ComputeProvider {

    /**
     * This method starts an instance-image with provided credential and
     * Compute-provider properties.
     *
     * @param credential
     * @param props
     *        Provider-specific properties
     * @return ID of running instance.
     * @throws Exception
     */
    public String start(Credential credential, String xmlProps)
            throws Exception;

    /**
     * This method stops the running instance with provided id.
     *
     * @param credential
     *        Compute-provider credentials
     * @param instanceId
     * @param props
     *        Provider-specific properties
     * @throws Exception
     */
    public void stop(Credential credential, String instanceId, String xmlProps)
            throws Exception;

    /**
     * This method returns true if the instance is successfully running.
     *
     * @param credential
     *        Compute-provider credentials
     * @param instanceId
     * @param props
     *        Provider-specific properties
     * @return
     */
    public boolean isInstanceRunning(Credential credential,
                                     String instanceId,
                                     String xmlProps) throws Exception;

    /**
     * This method returns true if the webapp of provided instance is
     * successfully running.
     *
     * @param credential
     *        Compute-provider credentials
     * @param instanceId
     * @param props
     *        Provider-specific properties
     * @return
     */
    public boolean isWebappRunning(Credential credential,
                                   String instanceId,
                                   String xmlProps) throws Exception;

    /**
     * This method returns true if the webapp of provided instance is currently
     * booting.
     *
     * @param credential
     *        Compute-provider credentials
     * @param instanceId
     * @param props
     *        Provider-specific properties
     * @return
     */
    public boolean isInstanceBooting(Credential credential,
                                     String instanceId,
                                     String xmlProps) throws Exception;

    /**
     * This method returns the URL of the instancewebapp on the instance with
     * the provided id.
     *
     * @param credential
     *        Compute-provider credentials
     * @param instanceId
     * @param props
     *        Provider-specific properties
     * @return
     * @throws Exception
     */
    public URL getWebappURL(Credential credential,
                            String instanceId,
                            String xmlProps) throws Exception;

    /**
     * This method retrieves description of initiated instance.
     *
     * @param credential
     *        Compute-provider credentials
     * @param instanceId
     * @param props
     *        Provider-specific properties
     * @return
     */
    public InstanceDescription describeRunningInstance(Credential credential,
                                                       String instanceId,
                                                       String xmlProps);

}
