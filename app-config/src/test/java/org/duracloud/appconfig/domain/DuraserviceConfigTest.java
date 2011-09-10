/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.appconfig.domain;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Apr 21, 2010
 */
public class DuraserviceConfigTest {

    private DuraserviceConfig.PrimaryInstance primaryInstance = new DuraserviceConfig.PrimaryInstance();
    private DuraserviceConfig.UserStore userStore = new DuraserviceConfig.UserStore();
    private DuraserviceConfig.ServiceStore serviceStore = new DuraserviceConfig.ServiceStore();

    private String instanceHost = "instanceHost";
    private String servicesAdminPort = "servicesAdminPort";
    private String servicesAdminContext = "servicesAdminContext";

    private String userStoreHost = "userStoreHost";
    private String userStorePort = "userStorePort";
    private String userStoreContext = "userStoreContext";
    private String msgBrokerUrl = "msgBrokerUrl";

    private String serviceStoreHost = "serviceStoreHost";
    private String serviceStorePort = "serviceStorePort";
    private String serviceStoreContext = "serviceStoreContext";
    private String username = "username";
    private String password = "password";
    private String spaceId = "spaceId";
    private String serviceXmlId = "serviceXmlId";

    @Before
    public void setUp() {
        primaryInstance.setHost(instanceHost);
        primaryInstance.setServicesAdminPort(servicesAdminPort);
        primaryInstance.setServicesAdminContext(servicesAdminContext);

        userStore.setHost(userStoreHost);
        userStore.setPort(userStorePort);
        userStore.setContext(userStoreContext);
        userStore.setMsgBrokerUrl(msgBrokerUrl);

        serviceStore.setHost(serviceStoreHost);
        serviceStore.setPort(serviceStorePort);
        serviceStore.setContext(serviceStoreContext);
        serviceStore.setUsername(username);
        serviceStore.setPassword(password);
        serviceStore.setSpaceId(spaceId);
        serviceStore.setServiceXmlId(serviceXmlId);
    }


    @Test
    public void testLoad() {
        DuraserviceConfig config = new DuraserviceConfig();
        config.load(createProps());
        verifyDuraserviceConfig(config);
    }

    private Map<String, String> createProps() {
        Map<String, String> props = new HashMap<String, String>();

        String dot = ".";
        String prefix = DuraserviceConfig.QUALIFIER + dot;

        String p0 = prefix + DuraserviceConfig.primaryInstanceKey + dot;
        props.put(p0 + DuraserviceConfig.hostKey, instanceHost);
        props.put(p0 + DuraserviceConfig.servicesAdminPortKey,
                  servicesAdminPort);
        props.put(p0 + DuraserviceConfig.servicesAdminContextKey,
                  servicesAdminContext);

        String p1 = prefix + DuraserviceConfig.userStoreKey + dot;
        props.put(p1 + DuraserviceConfig.hostKey, userStoreHost);
        props.put(p1 + DuraserviceConfig.portKey, userStorePort);
        props.put(p1 + DuraserviceConfig.contextKey, userStoreContext);
        props.put(p1 + DuraserviceConfig.msgBrokerUrlKey, msgBrokerUrl);

        String p2 = prefix + DuraserviceConfig.serviceStoreKey + dot;
        props.put(p2 + DuraserviceConfig.hostKey, serviceStoreHost);
        props.put(p2 + DuraserviceConfig.portKey, serviceStorePort);
        props.put(p2 + DuraserviceConfig.contextKey, serviceStoreContext);
        props.put(p2 + DuraserviceConfig.usernameKey, username);
        props.put(p2 + DuraserviceConfig.passwordKey, password);
        props.put(p2 + DuraserviceConfig.spaceIdKey, spaceId);
        props.put(p2 + DuraserviceConfig.serviceXmlIdKey, serviceXmlId);

        return props;
    }

    private void verifyDuraserviceConfig(DuraserviceConfig config) {

        DuraserviceConfig.PrimaryInstance pi = config.getPrimaryInstance();
        DuraserviceConfig.UserStore us = config.getUserStore();
        DuraserviceConfig.ServiceStore ss = config.getServiceStore();

        Assert.assertNotNull(pi);
        Assert.assertNotNull(us);
        Assert.assertNotNull(ss);

        Assert.assertNotNull(pi.getHost());
        Assert.assertNotNull(pi.getServicesAdminPort());
        Assert.assertNotNull(pi.getServicesAdminContext());
        Assert.assertEquals(instanceHost, pi.getHost());
        Assert.assertEquals(servicesAdminPort, pi.getServicesAdminPort());
        Assert.assertEquals(servicesAdminContext, pi.getServicesAdminContext());

        Assert.assertNotNull(us.getHost());
        Assert.assertNotNull(us.getPort());
        Assert.assertNotNull(us.getContext());
        Assert.assertNotNull(us.getMsgBrokerUrl());
        Assert.assertEquals(userStoreHost, us.getHost());
        Assert.assertEquals(userStorePort, us.getPort());
        Assert.assertEquals(userStoreContext, us.getContext());
        Assert.assertEquals(msgBrokerUrl, us.getMsgBrokerUrl());

        Assert.assertNotNull(ss.getHost());
        Assert.assertNotNull(ss.getPort());
        Assert.assertNotNull(ss.getContext());
        Assert.assertNotNull(ss.getUsername());
        Assert.assertNotNull(ss.getPassword());
        Assert.assertNotNull(ss.getSpaceId());
        Assert.assertNotNull(ss.getServiceXmlId());
        Assert.assertEquals(serviceStoreHost, ss.getHost());
        Assert.assertEquals(serviceStorePort, ss.getPort());
        Assert.assertEquals(serviceStoreContext, ss.getContext());
        Assert.assertEquals(username, ss.getUsername());
        Assert.assertEquals(password, ss.getPassword());
        Assert.assertEquals(spaceId, ss.getSpaceId());
        Assert.assertEquals(serviceXmlId, ss.getServiceXmlId());
    }

}
