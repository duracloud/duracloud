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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Bill Branan
 * Date: 5/12/11
 */
public class DurabossConfigTest {

    private String durastoreHost = "durastoreHost";
    private String durastorePort = "durastorePort";
    private String durastoreContext = "durastoreContext";
    private String duraserviceHost = "duraserviceHost";
    private String duraservicePort = "duraservicePort";
    private String duraserviceContext = "duraserviceContext";

    private String notifyType = "EMAIL";
    private String notifyUsername = "notifyUser";
    private String notifyPassword = "notifyPass";
    private String notifyOriginator = "notifyOriginator";

    private Boolean reporterEnabled = Boolean.FALSE;
    private Boolean executorEnabled = Boolean.FALSE;
    private Boolean auditorEnabled = Boolean.FALSE;

    @Test
    public void testLoad() {
        DurabossConfig config = new DurabossConfig();
        config.load(createProps());
        verifyDurabossConfig(config);
    }

    private Map<String, String> createProps() {
        Map<String, String> props = new HashMap<String, String>();

        String p = DurabossConfig.QUALIFIER + ".";

        props.put(p + DurabossConfig.duraStoreHostKey, durastoreHost);
        props.put(p + DurabossConfig.duraStorePortKey, durastorePort);
        props.put(p + DurabossConfig.duraStoreContextKey, durastoreContext);
        props.put(p + DurabossConfig.duraServiceHostKey, duraserviceHost);
        props.put(p + DurabossConfig.duraServicePortKey, duraservicePort);
        props.put(p + DurabossConfig.duraServiceContextKey, duraserviceContext);

        props.put(p + DurabossConfig.notificationKey + ".0." +
                      DurabossConfig.notificationTypeKey,
                  notifyType);
        props.put(p + DurabossConfig.notificationKey + ".0." +
                      DurabossConfig.notificationUsernameKey,
                  notifyUsername);
        props.put(p + DurabossConfig.notificationKey + ".0." +
                      DurabossConfig.notificationPasswordKey,
                  notifyPassword);
        props.put(p + DurabossConfig.notificationKey + ".0." +
                      DurabossConfig.notificationOriginatorKey,
                  notifyOriginator);

        props.put(p + DurabossConfig.reporterKey + "." +
                      DurabossConfig.enabledKey,
                  reporterEnabled.toString());
        props.put(p + DurabossConfig.executorKey + "." +
                      DurabossConfig.enabledKey,
                  executorEnabled.toString());
        props.put(p + DurabossConfig.auditorKey + "." +
                      DurabossConfig.enabledKey,
                  auditorEnabled.toString());

        return props;
    }

    private void verifyDurabossConfig(DurabossConfig config) {

        Assert.assertNotNull(config.getDurastoreHost());
        Assert.assertNotNull(config.getDurastorePort());
        Assert.assertNotNull(config.getDurastoreContext());
        Assert.assertNotNull(config.getDuraserviceHost());
        Assert.assertNotNull(config.getDuraservicePort());
        Assert.assertNotNull(config.getDuraserviceContext());

        Collection<NotificationConfig> notifyConfigs = config.getNotificationConfigs();
        Assert.assertNotNull(notifyConfigs);
        Assert.assertEquals(1, notifyConfigs.size());

        Assert.assertEquals(durastoreHost, config.getDurastoreHost());
        Assert.assertEquals(durastorePort, config.getDurastorePort());
        Assert.assertEquals(durastoreContext, config.getDurastoreContext());
        Assert.assertEquals(duraserviceHost, config.getDuraserviceHost());
        Assert.assertEquals(duraservicePort, config.getDuraservicePort());
        Assert.assertEquals(duraserviceContext, config.getDuraserviceContext());

        NotificationConfig notifyConfig = notifyConfigs.iterator().next();
        Assert.assertEquals(notifyType, notifyConfig.getType());
        Assert.assertEquals(notifyUsername, notifyConfig.getUsername());
        Assert.assertEquals(notifyPassword, notifyConfig.getPassword());
        Assert.assertEquals(notifyOriginator, notifyConfig.getOriginator());

        Assert.assertEquals(reporterEnabled, config.isReporterEnabled());
        Assert.assertEquals(executorEnabled, config.isExecutorEnabled());
        Assert.assertEquals(auditorEnabled, config.isAuditorEnabled());
    }

}
