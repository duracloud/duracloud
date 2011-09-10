/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.listener;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jms.listener.AbstractMessageListenerContainer;

import javax.jms.Destination;
import javax.jms.MapMessage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * @author Bill Branan
 * Date: 9/9/11
 */
public class BaseListenerServiceTest {

    private BaseListenerServiceImpl service;
    private ActiveMQConnectionFactory connectionFactory;
    private AbstractMessageListenerContainer jmsContainer;
    private String destTopic;
    private Destination destination;
    private MapMessage message;

    @Before
    public void setUp() throws Exception {
        jmsContainer =
            EasyMock.createMock(AbstractMessageListenerContainer.class);
        connectionFactory =
            EasyMock.createMock(ActiveMQConnectionFactory.class);
        service = new BaseListenerServiceImpl();
        message = EasyMock.createMock(MapMessage.class);

        destTopic = "destination";
        destination = new ActiveMQTopic(destTopic);
    }

    private void replayMocks() {
        EasyMock.replay(jmsContainer, connectionFactory, message);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(jmsContainer, connectionFactory, message);
    }

    @Test
    public void testInit() throws Exception {
        String brokerURL = "brokerURL";
        String messageSelector = "messageSelector";


        connectionFactory.setBrokerURL(brokerURL);
        EasyMock.expectLastCall().times(1);
        jmsContainer.setConnectionFactory(connectionFactory);
        EasyMock.expectLastCall().times(1);
        jmsContainer.setDestination(destination);
        EasyMock.expectLastCall().times(1);
        jmsContainer.setMessageSelector(messageSelector);
        EasyMock.expectLastCall().times(1);
        jmsContainer.setMessageListener(service);
        EasyMock.expectLastCall().times(1);
        jmsContainer.start();
        EasyMock.expectLastCall().times(1);
        jmsContainer.initialize();
        EasyMock.expectLastCall().times(1);

        replayMocks();

        service.setJmsContainer(jmsContainer);
        service.setConnectionFactory(connectionFactory);
        service.setBrokerURL(brokerURL);
        service.setDestination(destination);
        service.initializeMessaging(messageSelector);
    }

    @Test
    public void testOnMessage() throws Exception {
        EasyMock.expect(message.getJMSDestination())
            .andReturn(destination)
            .times(1);

        replayMocks();

        service.onMessage(message);
    }

    /**
     * private implementation of abstract BaseListenerService.
     */
    private class BaseListenerServiceImpl extends BaseListenerService {
        protected void setJmsContainer(AbstractMessageListenerContainer jmsContainer) {
            this.jmsContainer = jmsContainer;
        }

        @Override
        protected void handleMapMessage(MapMessage message, String topic) {
            assertNotNull(message);
            assertNotNull(topic);
            assertEquals(destTopic, topic);
        }
    }
}
