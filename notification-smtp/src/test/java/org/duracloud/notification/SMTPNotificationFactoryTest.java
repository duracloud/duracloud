/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.notification;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Shibo Liu
 * Date: Feb 29, 2020
 */
public class SMTPNotificationFactoryTest {

    private SMTPNotificationFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = new SMTPNotificationFactory("test-host", 100);
        factory.initialize("accessKey", "secretAccessKey");
    }

    @Test
    public void testCreateEmailer() throws Exception {
        SMTPNotificationFactory emailerFactory = new SMTPNotificationFactory("test-host", 100);

        String from = "a@g.com";

        boolean threw = false;
        try {
            emailerFactory.getEmailer(from);
            Assert.fail("Exception expected.");
        } catch (Exception e) {
            threw = true;
        }
        Assert.assertTrue(threw);

        emailerFactory.initialize("accessKey", "secretAccessKey");
        Emailer emailer = emailerFactory.getEmailer("a+b@g.h.com");
        Assert.assertNotNull(emailer);
    }

    @Test
    public void testCreateMultipleEmailers() throws Exception {
        Emailer emailer0 = factory.getEmailer("a@g.com");
        Assert.assertNotNull(emailer0);

        Emailer emailer1 = factory.getEmailer("x@y.org");
        Assert.assertNotNull(emailer1);
        Assert.assertNotSame(emailer0, emailer1);

        Emailer emailer2 = factory.getEmailer("a@g.com");
        Assert.assertNotNull(emailer2);
        Assert.assertEquals(emailer0, emailer2);
    }

    @Test
    public void testCreateEmailerInvalid() throws Exception {
        testInvalid("from");
        testInvalid(null);
    }

    private void testInvalid(String from) {
        boolean threw = false;
        try {
            factory.getEmailer(from);
        } catch (Exception e) {
            threw = true;
        }
        Assert.assertTrue(threw);
    }
}
