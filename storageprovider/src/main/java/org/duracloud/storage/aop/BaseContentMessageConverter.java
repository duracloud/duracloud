/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.support.converter.MessageConversionException;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Session;

/**
 * @author Andrew Woods
 *         Date: 3/19/12
 */
public abstract class BaseContentMessageConverter {

    protected final Logger log = LoggerFactory.getLogger(
        BaseContentMessageConverter.class);

    protected static final String STORE_ID = "storeId";
    protected static final String SPACE_ID = "spaceId";
    protected static final String CONTENT_ID = "contentId";
    protected static final String USERNAME = "username";
    protected static final String ACTION = "action";

    public ContentMessage fromMessage(ContentMessage msg, MapMessage mapMsg)
        throws JMSException, MessageConversionException {

        msg.setStoreId(mapMsg.getStringProperty(STORE_ID));
        msg.setSpaceId(mapMsg.getString(SPACE_ID));
        msg.setContentId(mapMsg.getString(CONTENT_ID));
        msg.setUsername(mapMsg.getString(USERNAME));
        msg.setAction(mapMsg.getString(ACTION));
        return msg;
    }

    public MapMessage toMessage(ContentMessage contentMsg, Session session)
        throws JMSException, MessageConversionException {
        MapMessage msg = session.createMapMessage();
        msg.setStringProperty(STORE_ID, contentMsg.getStoreId());
        msg.setStringProperty(SPACE_ID, contentMsg.getSpaceId());
        msg.setString(SPACE_ID, contentMsg.getSpaceId());
        msg.setString(CONTENT_ID, contentMsg.getContentId());
        msg.setString(USERNAME, contentMsg.getUsername());
        msg.setString(ACTION, contentMsg.getAction());
        return msg;
    }


}
