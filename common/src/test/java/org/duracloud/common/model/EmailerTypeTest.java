/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.duracloud.common.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 *
 * @author Andy Foster
 */
public class EmailerTypeTest {

    @Test
    public void testEmailerTypeEnum() {

        String smtp = "SMTP";
        String ses = "SES";

        EmailerType smtpFromSmtp = EmailerType.fromString(smtp);
        EmailerType sesFromSes = EmailerType.fromString(ses);
        EmailerType sesFromOther = EmailerType.fromString("other");

        assertEquals(smtpFromSmtp, EmailerType.SMTP);
        assertEquals(sesFromSes, EmailerType.SES);
        assertEquals(sesFromOther, EmailerType.SES);

        assertEquals(smtpFromSmtp.toString(), smtp);
        assertEquals(sesFromSes.toString(), ses);
        assertEquals(sesFromOther.toString(), ses);
    }
}
