/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.aop;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;
import org.junit.Assert;

import javax.jms.Connection;
import javax.jms.Destination;

/**
 * This class provides some basic Messaging connectivity support used by
 * sub-classing Tests.
 *
 * @author Andrew Woods
 */
public class MessagingTestSupport {

    protected ActiveMQConnectionFactory connectionFactory;

    protected String configString = "tcp://localhost:61617";

    protected final String topicName = "org.duracloud.topic.ingest";

    protected Connection createConnection() throws Exception {
        return getConnectionFactory().createConnection();
    }

    protected Destination createDestination() {
        return new ActiveMQTopic(topicName);
    }

    private ActiveMQConnectionFactory getConnectionFactory() throws Exception {
        if (connectionFactory == null) {
            connectionFactory = createConnectionFactory();
            Assert.assertTrue("Should have created a connection factory!",
                              connectionFactory != null);
        }
        return connectionFactory;
    }

    private ActiveMQConnectionFactory createConnectionFactory()
            throws Exception {
        return new ActiveMQConnectionFactory(configString);
    }

}
