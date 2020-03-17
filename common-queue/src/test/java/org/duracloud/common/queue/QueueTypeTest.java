/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.duracloud.common.queue;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 *
 * @author Andy Foster
 */
public class QueueTypeTest {

    @Test
    public void testQueueTypeEnum() {

        String rmq = "RabbitMQ";
        String sqs = "SQS";

        QueueType rmqFromRmq = QueueType.fromString(rmq);
        QueueType sqsFromSqs = QueueType.fromString(sqs);
        QueueType sqsFromOther = QueueType.fromString("other");

        assertEquals(rmqFromRmq, QueueType.RABBITMQ);
        assertEquals(sqsFromSqs, QueueType.SQS);
        assertEquals(sqsFromOther, QueueType.SQS);

        assertEquals(rmqFromRmq.toString(), rmq);
        assertEquals(sqsFromSqs.toString(), sqs);
        assertEquals(sqsFromOther.toString(), sqs);
    }
}
