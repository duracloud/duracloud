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

public class IngestMessageConverter extends BaseContentMessageConverter implements MessageConverter {

    protected final Logger log =
        LoggerFactory.getLogger(IngestMessageConverter.class);

    protected static final String MIMETYPE = "mimetype";

    protected static final String CONTENT_SIZE = "contentSize";

    protected static final String CONTENT_MD5 = "contentMd5";

    public Object fromMessage(Message msg)
        throws JMSException, MessageConversionException {
        if (!(msg instanceof MapMessage)) {
            String err = "Arg obj is not an instance of 'MapMessage': ";
            log.error(err + msg);
            throw new MessageConversionException(err);
        }

        MapMessage mapMsg = (MapMessage) msg;
        IngestMessage ingestMsg =
            (IngestMessage) super.fromMessage(new IngestMessage(), mapMsg);

        ingestMsg.setContentMimeType(mapMsg.getString(MIMETYPE));
        ingestMsg.setContentSize(mapMsg.getLong(CONTENT_SIZE));
        ingestMsg.setContentMd5(mapMsg.getString(CONTENT_MD5));
        return ingestMsg;
    }

    public Message toMessage(Object obj, Session session)
        throws JMSException, MessageConversionException {
        if (!(obj instanceof IngestMessage)) {
            String err = "Arg obj is not an instance of 'IngestMessage': ";
            log.error(err + obj);
            throw new MessageConversionException(err);
        }
        IngestMessage ingestMsg = (IngestMessage) obj;

        MapMessage msg = super.toMessage(ingestMsg, session);
        msg.setString(MIMETYPE, ingestMsg.getContentMimeType());
        msg.setLong(CONTENT_SIZE, ingestMsg.getContentSize());
        msg.setString(CONTENT_MD5, ingestMsg.getContentMd5());
        return msg;
    }

}
