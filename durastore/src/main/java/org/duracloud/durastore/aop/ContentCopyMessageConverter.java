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
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

public class ContentCopyMessageConverter
        implements MessageConverter {

    protected final Logger log =
        LoggerFactory.getLogger(ContentCopyMessageConverter.class);

    protected static final String STORE_ID = "storeId";
    protected static final String SRC_SPACE_ID = "srcSpaceId";
    protected static final String SRC_CONTENT_ID = "srcContentId";
    protected static final String DEST_SPACE_ID = "spaceId";
    protected static final String DEST_CONTENT_ID = "contentId";
    protected static final String USERNAME = "username";

    public Object fromMessage(Message msg) throws JMSException,
            MessageConversionException {
        if (!(msg instanceof MapMessage)) {
            String err = "Arg obj is not an instance of 'MapMessage': ";
            log.error(err + msg);
            throw new MessageConversionException(err);
        }

        MapMessage mapMsg = (MapMessage)msg;
        ContentCopyMessage contentCopyMsg = new ContentCopyMessage();
        contentCopyMsg.setStoreId(mapMsg.getStringProperty(STORE_ID));
        contentCopyMsg.setSourceSpaceId(mapMsg.getString(SRC_SPACE_ID));
        contentCopyMsg.setSourceContentId(mapMsg.getString(SRC_CONTENT_ID));
        contentCopyMsg.setDestSpaceId(mapMsg.getString(DEST_SPACE_ID));
        contentCopyMsg.setDestContentId(mapMsg.getString(DEST_CONTENT_ID));
        contentCopyMsg.setUsername(mapMsg.getString(USERNAME));
        return contentCopyMsg;
    }

    public Message toMessage(Object obj, Session session) throws JMSException,
            MessageConversionException {
        if (!(obj instanceof ContentCopyMessage)) {
            String err = "Arg obj is not an instance of 'ContentCopyMessage': ";
            log.error(err + obj);
            throw new MessageConversionException(err);
        }
        ContentCopyMessage contentMsg = (ContentCopyMessage) obj;

        MapMessage msg = session.createMapMessage();
        msg.setStringProperty(STORE_ID, contentMsg.getStoreId());
        msg.setStringProperty(DEST_SPACE_ID, contentMsg.getDestSpaceId());
        msg.setString(SRC_SPACE_ID, contentMsg.getSourceSpaceId());
        msg.setString(SRC_CONTENT_ID, contentMsg.getSourceContentId());
        msg.setString(DEST_SPACE_ID, contentMsg.getDestSpaceId());
        msg.setString(DEST_CONTENT_ID, contentMsg.getDestContentId());
        msg.setString(USERNAME, contentMsg.getUsername());
        return msg;
    }

}
