/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.queue.rabbitmq;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.impl.AMQImpl;
import org.duracloud.common.queue.task.Task;
import org.duracloud.common.queue.task.Task.Type;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Andy Foster
 * Date: 2020.02.12
 */
@RunWith(EasyMockRunner.class)
public class RabbitMQTaskQueueTest extends EasyMockSupport {

    private Connection connection;
    private RabbitMQTaskQueue queue;
    private Channel channel;
    private String queueName = "test-queue";
    private String exchange = "test-exchange";

    @Before
    public void setup() {
    }

    private void setupRabbitMQClient() throws IOException {
        connection = createMock("Connection", Connection.class);
        channel = createMock("Channel", Channel.class);
        InetAddress address = InetAddress.getByName("127.0.0.1");

        expect(connection.createChannel()).andReturn(channel);
        expect(channel.queueBind(queueName, exchange, queueName)).andReturn(new AMQImpl.Queue.BindOk());
        expect(connection.getAddress()).andReturn(address);
        channel.basicPublish(anyObject(String.class), anyObject(String.class), anyObject(null), anyObject(byte[].class));
        expectLastCall().anyTimes();
    }

    @After
    public void tearDown() {
        verifyAll();
    }

    private void createSubject() {
        queue = new RabbitMQTaskQueue(connection, exchange, queueName);
    }

    @Test
    public void testMarshallTask() throws IOException {
        setupRabbitMQClient();
        replayAll();
        createSubject();

        String message = Task.KEY_TYPE + "=" + Task.Type.DUP.name() + "\n" +
                         "key1=value1\nkey2=value2\nkey3=value3";
        byte[] msgBody = message.getBytes();

        Task task = queue.marshallTask(msgBody, 0, queueName, exchange);

        assertThat(task.getProperty(RabbitMQTaskQueue.MsgProp.DELIVERY_TAG.name()),
                   is(equalTo("0")));
        assertThat(task.getProperty(RabbitMQTaskQueue.MsgProp.EXCHANGE.name()),
                   is(equalTo("test-exchange")));
        assertThat(task.getProperty(RabbitMQTaskQueue.MsgProp.ROUTING_KEY.name()),
                   is(equalTo("test-queue")));
        assertThat(task.getType(), is(equalTo(Task.Type.DUP)));
        assertThat(task.getProperty("key1"), is(equalTo("value1")));
        assertThat(task.getProperty("key2"), is(equalTo("value2")));
        assertThat(task.getProperty("key3"), is(equalTo("value3")));
    }

    @Test
    public void testUnmarshallTask() throws IOException {
        setupRabbitMQClient();
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
    public void testPut() throws IOException {
        setupRabbitMQClient();
        replayAll();
        createSubject();

        Task task = createSampleAuditTask(System.currentTimeMillis());
        this.queue.put(task);
    }

    protected Task createSampleAuditTask(long time) {
        Task task = new Task();
        task.setType(Type.AUDIT);
        task.getProperties().put("timestamp", time + "");
        return task;
    }

    @Test
    public void testPutMuliple() throws IOException {
        setupRabbitMQClient();
        replayAll();
        createSubject();
        Set<Task> tasks = new HashSet<>();
        long time = System.currentTimeMillis();
        for (int i = 0; i < 11; i++) {
            tasks.add(createSampleAuditTask(time + i));
        }
        this.queue.put(tasks);
    }
}