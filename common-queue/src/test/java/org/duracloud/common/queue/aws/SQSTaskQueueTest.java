/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.queue.aws;


import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.duracloud.common.queue.task.Task;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueAttributeName;

/**
 * @author Erik Paulsson
 *         Date: 10/25/13
 */
public class SQSTaskQueueTest {

    private SQSTaskQueue queue;
    private String queueName = "test-queue";

    @Before
    public void setUp() {
        AmazonSQSClient sqsClient = EasyMock.createMock(AmazonSQSClient.class);
        EasyMock.expect(sqsClient.getQueueUrl(new GetQueueUrlRequest()
                              .withQueueName(queueName)))
                .andReturn(new GetQueueUrlResult()
                               .withQueueUrl("/sqstasktest/" + queueName));
        EasyMock.expect(sqsClient.getQueueAttributes(
                    new GetQueueAttributesRequest().withQueueUrl("/sqstasktest/" + queueName)
                                                   .withAttributeNames(QueueAttributeName.VisibilityTimeout)))
                .andReturn(new GetQueueAttributesResult()
                               .addAttributesEntry(
                                   QueueAttributeName.VisibilityTimeout.name(), "300"));
        EasyMock.replay(sqsClient);

        queue = new SQSTaskQueue(sqsClient, queueName);
    }

    @Test
    public void testMarshallTask() {
        String msgId = "test-msg-id-1234";
        String receiptHandle = "test-msg-rcpt-handle-1234";
        Message msg = new Message();
        msg.setMessageId(msgId);
        msg.setReceiptHandle(receiptHandle);
        msg.setBody(Task.KEY_TYPE + "=" + Task.Type.DUP.name() + "\n" +
                        "key1=value1\nkey2=value2\nkey3=value3");
        Task task = queue.marshallTask(msg);
        assertThat(task.getProperty(SQSTaskQueue.MsgProp.MSG_ID.name()),
                   is(equalTo(msgId)));
        assertThat(task.getProperty(SQSTaskQueue.MsgProp.RECEIPT_HANDLE.name()),
                   is(equalTo(receiptHandle)));
        assertThat(task.getType(), is(equalTo(Task.Type.DUP)));
        assertThat(task.getProperty("key1"), is(equalTo("value1")));
        assertThat(task.getProperty("key2"), is(equalTo("value2")));
        assertThat(task.getProperty("key3"), is(equalTo("value3")));
    }

    @Test
    public void testUnmarshallTask() {
        Task task = new Task();
        task.setType(Task.Type.DUP);
        task.addProperty("key1", "value1");
        task.addProperty("key2", "value2");
        task.addProperty("key3", "value3");
        String msgBody = queue.unmarshallTask(task);
        
        Assert.assertTrue(msgBody.contains("key1=value1"));
        Assert.assertTrue(msgBody.contains("key2=value2"));
        Assert.assertTrue(msgBody.contains("key3=value3"));
        
    }

}
