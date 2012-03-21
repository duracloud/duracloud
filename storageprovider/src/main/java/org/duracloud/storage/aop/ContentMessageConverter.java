/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.aop;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

public class ContentMessageConverter extends BaseContentMessageConverter
        implements MessageConverter {

    protected final Logger log = LoggerFactory.getLogger(ContentMessageConverter.class);

    public Object fromMessage(Message msg) throws JMSException,
            MessageConversionException {
        if (!(msg instanceof MapMessage)) {
            String err = "Arg obj is not an instance of 'MapMessage': ";
            log.error(err + msg);
            throw new MessageConversionException(err);
        }

        MapMessage mapMsg = (MapMessage) msg;
        return super.fromMessage(new ContentMessage(), mapMsg);
    }

    public Message toMessage(Object obj, Session session) throws JMSException,
            MessageConversionException {
        if (!(obj instanceof ContentMessage)) {
            String err = "Arg obj is not an instance of 'ContentMessage': ";
            log.error(err + obj);
            throw new MessageConversionException(err);
        }

        ContentMessage contentMsg = (ContentMessage) obj;
        return super.toMessage(contentMsg, session);
    }

}
