/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.queue.aws;


import static org.easymock.EasyMock.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsEqual.*;
import static org.junit.Assert.*;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.duracloud.common.queue.task.Task;
import org.duracloud.common.queue.task.Task.Type;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueAttributeName;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

/**
 * @author Erik Paulsson
 *         Date: 10/25/13
 */
@RunWith(EasyMockRunner.class)
public class SQSTaskQueueTest extends EasyMockSupport{

    private SQSTaskQueue queue;
    private String queueName = "test-queue";

    @Mock
    private AmazonSQSClient sqsClient;
    
    
    @Before
    public void setup(){
    }
    
    private void setupSQSClient() {
        expect(sqsClient.getQueueUrl(new GetQueueUrlRequest()
                              .withQueueName(queueName)))
                .andReturn(new GetQueueUrlResult()
                               .withQueueUrl("/sqstasktest/" + queueName));
        expect(sqsClient.getQueueAttributes(
                    new GetQueueAttributesRequest().withQueueUrl("/sqstasktest/" + queueName)
                                                   .withAttributeNames(QueueAttributeName.VisibilityTimeout)))
                .andReturn(new GetQueueAttributesResult()
                               .addAttributesEntry(
                                   QueueAttributeName.VisibilityTimeout.name(), "300"));
    }
    
    @After
    public void tearDown(){
        verifyAll();
    }

    @Test
    public void testMarshallTask() {
        setupSQSClient();
        replayAll();
        createSubject();

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

    protected void createSubject() {
        queue = new SQSTaskQueue(sqsClient, queueName);
    }

    @Test
    public void testUnmarshallTask() {
        setupSQSClient();
        replayAll();
        createSubject();
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
    
    
    @Test
    public void testPut(){
        setupSQSClient();
        SendMessageResult result = createMock(SendMessageResult.class);
        expect(this.sqsClient.sendMessage(isA(SendMessageRequest.class))).andThrow(new AmazonServiceException("failure"));
        expect(this.sqsClient.sendMessage(isA(SendMessageRequest.class))).andReturn(result);
        replayAll();
        createSubject();
        Task task = createSampleAuditTask();
        this.queue.put(task);
    }

    protected Task createSampleAuditTask() {
        return createSampleAuditTask(System.currentTimeMillis());
    }

    protected Task createSampleAuditTask(long time) {
        Task task = new Task();
        task.setType(Type.AUDIT);
        task.getProperties().put("timestamp", time+"");
        return task;
    }
    
    @Test
    public void testPutMuliple(){
        setupSQSClient();
        SendMessageBatchResult result = createMock(SendMessageBatchResult.class);
        expect(this.sqsClient.sendMessageBatch(isA(SendMessageBatchRequest.class))).andThrow(new AmazonServiceException("failure"));
        expect(this.sqsClient.sendMessageBatch(isA(SendMessageBatchRequest.class))).andReturn(result).times(2);
        replayAll();
        createSubject();
        Set<Task> tasks = new HashSet<>();
        long time = System.currentTimeMillis();
        for(int i = 0; i < 11; i++){
            tasks.add(createSampleAuditTask(time+i));
        }
        this.queue.put(tasks);
    }

}
