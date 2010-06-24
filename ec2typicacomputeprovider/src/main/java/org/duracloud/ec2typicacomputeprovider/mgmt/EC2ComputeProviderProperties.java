/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ec2typicacomputeprovider.mgmt;

import java.io.InputStream;

import java.util.Properties;

import org.duracloud.common.util.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * POJO container for EC2-specific configuration information. -load/store is
 * implemented in base class.
 *
 * @author Andrew Woods
 */
public class EC2ComputeProviderProperties
        extends ApplicationConfig {

    protected static final Logger log =
            LoggerFactory.getLogger(EC2ComputeProviderProperties.class);

    private Properties props;

    private final String providerKey = "provider";

    private final String signatureMethodKey = "signatureMethod";

    private final String keynameKey = "keyname";

    private final String imageIdKey = "imageId";

    private final String minInstanceCountKey = "minInstanceCount";

    private final String maxInstanceCountKey = "maxInstanceCount";

    private final String maxAsyncThreadsKey = "maxAsyncThreads";

    private final String webappProtocolKey = "webappProtocol";

    private final String webappPortKey = "webappPort";

    private final String webappNameKey = "webappName";

    public EC2ComputeProviderProperties() {
        props = new Properties();
    }

    public void loadFromXml(String xml) throws Exception {
        props = ApplicationConfig.getPropsFromXml(xml);
    }

    public void loadFromXmlStream(InputStream propsXmlStream) throws Exception {
        props = ApplicationConfig.getPropsFromXmlStream(propsXmlStream);
    }

    public String getAsXml() throws Exception {
        return ApplicationConfig.getXmlFromProps(props);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("EC2Props[");
        sb.append("\n\tprovider:" + getProvider());
        sb.append("\n\tsignatureMethod:" + getSignatureMethod());
        sb.append("\n\tkeyname:" + getKeyname());
        sb.append("\n\timageId:" + getImageId());
        sb.append("\n\tminInstanceCount:" + getMinInstanceCount());
        sb.append("\n\tmaxInstanceCount:" + getMaxInstanceCount());
        sb.append("\n\tmaxAsyncThreads:" + getMaxAsyncThreads());
        sb.append("\n\twebappProtocol:" + getWebappProtocol());
        sb.append("\n\twebappPort:" + getWebappPort());
        sb.append("\n\twebappName:" + getWebappName());
        sb.append("]");
        return sb.toString();
    }

    public String getProvider() {
        return props.getProperty(providerKey);
    }

    public String getSignatureMethod() {
        return props.getProperty(signatureMethodKey);
    }

    public String getKeyname() {
        return props.getProperty(keynameKey);
    }

    public String getImageId() {
        return props.getProperty(imageIdKey);
    }

    public int getMinInstanceCount() {
        int val = -1;
        try {
            val = Integer.parseInt(props.getProperty(minInstanceCountKey));
        } catch (NumberFormatException e) {
            log.error("Prop should be numeric: " + minInstanceCountKey, e);
        }
        return val;
    }

    public int getMaxInstanceCount() {
        int val = -1;
        try {
            val = Integer.parseInt(props.getProperty(maxInstanceCountKey));
        } catch (NumberFormatException e) {
            log.error("Prop should be numeric: " + maxInstanceCountKey, e);
        }
        return val;
    }

    public int getMaxAsyncThreads() {
        int val = -1;
        try {
            val = Integer.parseInt(props.getProperty(maxAsyncThreadsKey));
        } catch (NumberFormatException e) {
            log.error("Prop should be numeric: " + maxAsyncThreadsKey, e);
        }
        return val;
    }

    public String getWebappProtocol() {
        return props.getProperty(webappProtocolKey);
    }

    public int getWebappPort() {
        int val = -1;
        try {
            val = Integer.parseInt(props.getProperty(webappPortKey));
        } catch (NumberFormatException e) {
            log.error("Prop should be numeric: " + webappPortKey , e);
        }
        return val;
    }

    public String getWebappName() {
        return props.getProperty(webappNameKey);
    }

}
