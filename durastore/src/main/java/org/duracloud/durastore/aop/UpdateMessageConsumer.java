/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

/**
 * This is placeholder class that will be replaced when consumers are written to
 * monitor messaging topics/queues
 *
 */
public class UpdateMessageConsumer {

    protected final Logger log = LoggerFactory.getLogger(UpdateMessageConsumer.class);

    private JmsTemplate jmsTemplate;

    public void onUpdate(ContentMessage contentMsg) {
        log.info("message consumed from topic: " + contentMsg);
    }

    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

}
