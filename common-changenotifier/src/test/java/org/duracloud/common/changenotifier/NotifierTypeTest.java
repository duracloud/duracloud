/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.duracloud.common.changenotifier;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 *
 * @author Andy Foster
 */
public class NotifierTypeTest {

    @Test
    public void testNotifierTypeEnum() {

        String rmq = "RabbitMQ";
        String sns = "SNS";

        NotifierType rmqFromRmq = NotifierType.fromString(rmq);
        NotifierType snsFromSns = NotifierType.fromString(sns);
        NotifierType snsFromOther = NotifierType.fromString("other");

        assertEquals(rmqFromRmq, NotifierType.RABBITMQ);
        assertEquals(snsFromSns, NotifierType.SNS);
        assertEquals(snsFromOther, NotifierType.SNS);

        assertEquals(rmqFromRmq.toString(), rmq);
        assertEquals(snsFromSns.toString(), sns);
        assertEquals(snsFromOther.toString(), sns);
    }
}
