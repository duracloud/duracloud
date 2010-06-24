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
    private DuraserviceConfig.ServiceCompute serviceCompute = new DuraserviceConfig.ServiceCompute();

//    private String host = "host";
//    private String port = "port";
//    private String context = "context";

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
    private String spaceId = "spaceId";

    private String type = "type";
    private String imageId = "imageId";
    private String username = "username";
    private String password = "password";

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
        serviceStore.setSpaceId(spaceId);

        serviceCompute.setType(type);
        serviceCompute.setImageId(imageId);
        serviceCompute.setUsername(username);
        serviceCompute.setPassword(password);
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

//        props.put(prefix + DuraserviceConfig.hostKey, host);
//        props.put(prefix + DuraserviceConfig.portKey, port);
//        props.put(prefix + DuraserviceConfig.contextKey, context);

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
        props.put(p2 + DuraserviceConfig.spaceIdKey, spaceId);

        String p3 = prefix + DuraserviceConfig.serviceComputeKey + dot;
        props.put(p3 + DuraserviceConfig.typeKey, type);
        props.put(p3 + DuraserviceConfig.imageIdKey, imageId);
        props.put(p3 + DuraserviceConfig.usernameKey, username);
        props.put(p3 + DuraserviceConfig.passwordKey, password);

        return props;
    }

    private void verifyDuraserviceConfig(DuraserviceConfig config) {

//        Assert.assertNotNull(config.getHost());
//        Assert.assertNotNull(config.getPort());
//        Assert.assertNotNull(config.getContext());
//        Assert.assertEquals(config.getHost(), host);
//        Assert.assertEquals(config.getPort(), port);
//        Assert.assertEquals(config.getContext(), context);

        DuraserviceConfig.PrimaryInstance pi = config.getPrimaryInstance();
        DuraserviceConfig.UserStore us = config.getUserStore();
        DuraserviceConfig.ServiceStore ss = config.getServiceStore();
        DuraserviceConfig.ServiceCompute sc = config.getServiceCompute();

        Assert.assertNotNull(pi);
        Assert.assertNotNull(us);
        Assert.assertNotNull(ss);
        Assert.assertNotNull(sc);

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
        Assert.assertNotNull(ss.getSpaceId());
        Assert.assertEquals(serviceStoreHost, ss.getHost());
        Assert.assertEquals(serviceStorePort, ss.getPort());
        Assert.assertEquals(serviceStoreContext, ss.getContext());
        Assert.assertEquals(spaceId, ss.getSpaceId());

        Assert.assertNotNull(sc.getType());
        Assert.assertNotNull(sc.getImageId());
        Assert.assertNotNull(sc.getUsername());
        Assert.assertNotNull(sc.getPassword());
        Assert.assertEquals(type, sc.getType());
        Assert.assertEquals(imageId, sc.getImageId());
        Assert.assertEquals(username, sc.getUsername());
        Assert.assertEquals(password, sc.getPassword());
    }

}
