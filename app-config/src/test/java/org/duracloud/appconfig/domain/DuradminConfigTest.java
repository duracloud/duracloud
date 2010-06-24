/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.appconfig.domain;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Apr 21, 2010
 */
public class DuradminConfigTest {

    private String host = "host";
    private String port = "port";
    private String context = "context";
    private String durastoreHost = "durastoreHost";
    private String durastorePort = "durastorePort";
    private String durastoreContext = "durastoreContext";
    private String duraserviceHost = "duraserviceHost";
    private String duraservicePort = "duraservicePort";
    private String duraserviceContext = "duraserviceContext";

    @Test
    public void testLoad() {
        DuradminConfig config = new DuradminConfig();
        config.load(createProps());
        verifyDuradminConfig(config);
    }

    private Map<String, String> createProps() {
        Map<String, String> props = new HashMap<String, String>();

        String p = DuradminConfig.QUALIFIER+".";

//        props.put(p + DuradminConfig.hostKey, host);
//        props.put(p + DuradminConfig.portKey, port);
//        props.put(p + DuradminConfig.contextKey, context);

        props.put(p + DuradminConfig.duraStoreHostKey, durastoreHost);
        props.put(p + DuradminConfig.duraStorePortKey, durastorePort);
        props.put(p + DuradminConfig.duraStoreContextKey, durastoreContext);
        props.put(p + DuradminConfig.duraServiceHostKey, duraserviceHost);
        props.put(p + DuradminConfig.duraServicePortKey, duraservicePort);
        props.put(p + DuradminConfig.duraServiceContextKey, duraserviceContext);

        return props;
    }

    private void verifyDuradminConfig(DuradminConfig config) {

        Assert.assertNotNull(config.getDurastoreHost());
        Assert.assertNotNull(config.getDurastorePort());
        Assert.assertNotNull(config.getDurastoreContext());
        Assert.assertNotNull(config.getDuraserviceHost());
        Assert.assertNotNull(config.getDuraservicePort());
        Assert.assertNotNull(config.getDuraserviceContext());

        Assert.assertEquals(durastoreHost, config.getDurastoreHost());
        Assert.assertEquals(durastorePort, config.getDurastorePort());
        Assert.assertEquals(durastoreContext, config.getDurastoreContext());
        Assert.assertEquals(duraserviceHost, config.getDuraserviceHost());
        Assert.assertEquals(duraservicePort, config.getDuraservicePort());
        Assert.assertEquals(duraserviceContext, config.getDuraserviceContext());
    }
    
}
