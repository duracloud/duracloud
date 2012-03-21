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
import org.springframework.jms.support.converter.MessageConverter;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

public class ContentCopyMessageConverter extends BaseContentMessageConverter
        implements MessageConverter {

    protected final Logger log =
        LoggerFactory.getLogger(ContentCopyMessageConverter.class);

    protected static final String DEST_SPACE_ID = "destSpaceId";
    protected static final String DEST_CONTENT_ID = "destContentId";
    protected static final String CONTENT_MD5= "contentMd5";

    public Object fromMessage(Message msg) throws JMSException,
            MessageConversionException {
        if (!(msg instanceof MapMessage)) {
            String err = "Arg obj is not an instance of 'MapMessage': ";
            log.error(err + msg);
            throw new MessageConversionException(err);
        }

        MapMessage mapMsg = (MapMessage) msg;
        ContentCopyMessage contentCopyMsg =
            (ContentCopyMessage) super.fromMessage(new ContentCopyMessage(), mapMsg);
        contentCopyMsg.setDestSpaceId(mapMsg.getString(DEST_SPACE_ID));
        contentCopyMsg.setDestContentId(mapMsg.getString(DEST_CONTENT_ID));
        contentCopyMsg.setContentMd5(mapMsg.getString(CONTENT_MD5));
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
        MapMessage msg = super.toMessage(contentMsg, session);

        msg.setString(DEST_SPACE_ID, contentMsg.getDestSpaceId());
        msg.setString(DEST_CONTENT_ID, contentMsg.getDestContentId());
        msg.setString(CONTENT_MD5, contentMsg.getContentMd5());
        return msg;
    }

}
