/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ec2typicacomputeprovider.mgmt;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.Arrays;
import java.util.List;

import com.xerox.amazonws.ec2.Jec2;
import com.xerox.amazonws.ec2.LaunchConfiguration;
import com.xerox.amazonws.ec2.ReservationDescription;

import org.duracloud.common.model.Credential;
import org.duracloud.common.util.ExceptionUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.duracloud.computeprovider.mgmt.ComputeProvider;
import org.duracloud.computeprovider.mgmt.InstanceDescription;
import org.duracloud.computeprovider.mgmt.InstanceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EC2ComputeProvider
        implements ComputeProvider {

    protected final Logger log = LoggerFactory.getLogger(EC2ComputeProvider.class);

    private Jec2 ec2;

    private Credential credential;

    private EC2ComputeProviderProperties props;

    /**
     * {@inheritDoc}
     */
    public String start(Credential credential, String xmlProps)
            throws Exception {
        log.info("start(" + credential + ", " + xmlProps + ")");
        initialize(credential, xmlProps);

        ReservationDescription response =
                getEC2().runInstances(createStartRequest());

        return getInstanceId(response);
    }

    private LaunchConfiguration createStartRequest() throws Exception {
        LaunchConfiguration request =
                new LaunchConfiguration(getProps().getImageId());
        request.setKeyName(getProps().getKeyname());
        request.setMinCount(getProps().getMinInstanceCount());
        request.setMaxCount(getProps().getMaxInstanceCount());

        return request;
    }

    private String getInstanceId(ReservationDescription response) {
        return EC2Helper.getFirstRunningInstance(response).getInstanceId();
    }

    /**
     * {@inheritDoc}
     */
    public void stop(Credential credential, String instanceId, String xmlProps)
            throws Exception {
        initialize(credential, xmlProps);

        getEC2().terminateInstances(Arrays.asList(instanceId));
    }

    /**
     * {@inheritDoc}
     *
     * @throws Exception
     */
    public InstanceDescription describeRunningInstance(Credential credential,
                                                       String instanceId,
                                                       String xmlProps) {
        initialize(credential, xmlProps);

        return getDescription(instanceId);
    }

    private InstanceDescription getDescription(String instanceId) {
        InstanceDescription description = null;
        try {
            List<ReservationDescription> response =
                    requestRunningInstanceDescription(instanceId);
            description = new EC2InstanceDescription(response, getProps());

        } catch (Exception e) {
            log.error("Error getting instance description for: " + instanceId, e);
            description = new EC2InstanceDescription(e);
        }

        return description;
    }

    private List<ReservationDescription> requestRunningInstanceDescription(String instanceId)
            throws Exception {
        return getEC2().describeInstances(Arrays.asList(instanceId));
    }

    /**
     * {@inheritDoc}
     */
    public boolean isInstanceRunning(Credential credential,
                                     String instanceId,
                                     String xmlProps) throws Exception {
        initialize(credential, xmlProps);
        InstanceDescription description = getDescription(instanceId);
        return isInstanceRunning(description);
    }

    private boolean isInstanceRunning(InstanceDescription description) {
        return (description != null && InstanceState.RUNNING.equals(description
                .getState()));
    }

    /**
     * {@inheritDoc}
     */
    public boolean isWebappRunning(Credential credential,
                                   String instanceId,
                                   String xmlProps) throws Exception {
        initialize(credential, xmlProps);
        InstanceDescription description = getDescription(instanceId);
        return isWebappRunning(description);
    }

    private boolean isWebappRunning(InstanceDescription description) {
        return (isInstanceRunning(description) && pingURL(description.getURL()) == HttpURLConnection.HTTP_OK);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isInstanceBooting(Credential credential,
                                     String instanceId,
                                     String xmlProps) throws Exception {
        initialize(credential, xmlProps);
        InstanceDescription description = getDescription(instanceId);
        return isInstanceBooting(description);
    }

    private boolean isInstanceBooting(InstanceDescription description) {
        if (description == null) return false;

        return (InstanceState.PENDING.equals(description.getState()) || (!isWebappRunning(description) && isInstanceRunning(description)));
    }

    private int pingURL(URL url) {
        int statusCode = HttpURLConnection.HTTP_NOT_FOUND;
        try {
            RestHttpHelper helper = new RestHttpHelper();
            HttpResponse resp = helper.get(url.toString());
            statusCode = resp.getStatusCode();
        } catch (Exception e) {
            log.warn("Error pinging: "+ url.toString(), e);
        }
        log.info("ping url: " + url + ", status: " + statusCode);

        return statusCode;
    }

    /**
     * {@inheritDoc}
     */
    public URL getWebappURL(Credential credential,
                            String instanceId,
                            String xmlProps) throws Exception {
        initialize(credential, xmlProps);

        InstanceDescription description = getDescription(instanceId);

        if (description == null) {
            throw new Exception("No URL for instance: '" + instanceId + "'");
        }
        return description.getURL();
    }

    // TODO: awoods: remove?
//    private AmazonEC2Config convertConfigurationFrom(EC2ComputeProviderProperties props) {
//        AmazonEC2Config config = new AmazonEC2Config();
//        config.setSignatureMethod(props.getSignatureMethod());
//        config.setMaxAsyncThreads(props.getMaxAsyncThreads());
//
//        return config;
//    }

    private void setProps(String xmlProps) {
        this.props = new EC2ComputeProviderProperties();
        try {
            this.props.loadFromXml(xmlProps);
        } catch (Exception e) {
            log.error("Unable to load properties: " + xmlProps);
            log.error(e.getMessage());
            log.error(ExceptionUtil.getStackTraceAsString(e));
        }
    }

    public EC2ComputeProviderProperties getProps() throws Exception {
        return props;
    }

    public Jec2 getEC2() throws Exception {
        if (ec2 == null) {
            log.info("initializing ec2 from props.");
            //            AmazonEC2Config config = convertConfigurationFrom(getProps());
            try {
                ec2 =
                        new Jec2(credential.getUsername(), credential
                                .getPassword());
            } catch (Exception e) {
                String msg =
                        "Failed initializing EC2: " + credential + "|" + props;
                log.error(msg, e);
                throw new Exception(msg, e);
            }
        }
        return ec2;
    }

    public void setEC2(Jec2 ec2) {
        this.ec2 = ec2;
    }

    private void initialize(Credential cred, String xmlProps) {
        setCredential(cred);
        setProps(xmlProps);
    }

    private void setCredential(Credential credential) {
        this.credential = credential;
    }

}
