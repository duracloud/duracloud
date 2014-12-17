/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.appconfig.xml;

import org.duracloud.appconfig.domain.DurabossConfig;
import org.duracloud.appconfig.domain.NotificationConfig;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author: Bill Branan
 * Date: 12/6/11
 */
public class DurabossInitDocumentBindingTest {

    private String durastoreHost = "durastoreHost";
    private String durastorePort = "durastorePort";
    private String durastoreContext = "durastoreContext";

    private String notifyType = "EMAIL";
    private String notifyUsername = "notifyUser";
    private String notifyPassword = "notifyPass";
    private String notifyOriginator = "notifyOriginator";
    private String notifyAdmin1 = "admin1";
    private String notifyAdmin2 = "admin2";

    @Test
    public void testXmlRoundTrip() throws Exception {
        // Create config
        DurabossConfig config = new DurabossConfig();
        config.setReporterEnabled(false);
        config.setDurastoreHost(durastoreHost);
        config.setDurastorePort(durastorePort);
        config.setDurastoreContext(durastoreContext);
        // Add notification config
        NotificationConfig notifyConfig = new NotificationConfig();
        notifyConfig.setType(notifyType);
        notifyConfig.setUsername(notifyUsername);
        notifyConfig.setPassword(notifyPassword);
        notifyConfig.setOriginator(notifyOriginator);
        List<String> admins = new ArrayList<String>();
        admins.add(notifyAdmin1);
        admins.add(notifyAdmin2);
        notifyConfig.setAdmins(admins);
        Map<String, NotificationConfig> notifyConfigMap =
            new HashMap<String, NotificationConfig>();
        notifyConfigMap.put("0", notifyConfig);
        config.setNotificationConfigs(notifyConfigMap);

        // Run round trip
        String xml = DurabossInitDocumentBinding.createDocumentFrom(config);
        assertNotNull(xml);

        InputStream xmlStream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        DurabossConfig trippedConfig =
            DurabossInitDocumentBinding.createDurabossConfigFrom(xmlStream);

        // Verify results
        assertFalse(config.isReporterEnabled());
        assertEquals(config.getDurastoreHost(),
                     trippedConfig.getDurastoreHost());
        assertEquals(config.getDurastorePort(),
                     trippedConfig.getDurastorePort());
        assertEquals(config.getDurastoreContext(),
                     trippedConfig.getDurastoreContext());

        Collection<NotificationConfig> trippedNotifyConfigs =
            trippedConfig.getNotificationConfigs();
        assertEquals(1, trippedNotifyConfigs.size());
        NotificationConfig trippedNotifyConfig =
            trippedNotifyConfigs.iterator().next();

        assertEquals(notifyConfig.getType(),
                     trippedNotifyConfig.getType());
        assertEquals(notifyConfig.getUsername(),
                     trippedNotifyConfig.getUsername());
        assertEquals(notifyConfig.getPassword(),
                     trippedNotifyConfig.getPassword());
        assertEquals(notifyConfig.getOriginator(),
                     trippedNotifyConfig.getOriginator());
        List<String> trippedAdmins = trippedNotifyConfig.getAdmins();
        assertEquals(2, trippedAdmins.size());
        assertTrue(trippedAdmins.contains(notifyAdmin1));
        assertTrue(trippedAdmins.contains(notifyAdmin2));
    }

}
