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

/**
 * @author Andrew Woods
 *         Date: 3/19/12
 */
public class AllContentEventsMessageConverter implements MessageConverter {

    private final Logger log = LoggerFactory.getLogger(
        AllContentEventsMessageConverter.class);

    private ContentCopyMessageConverter copyMessageConverter;
    private ContentMessageConverter deleteAndUpdateConverter;
    private IngestMessageConverter ingestMessageConverter;

    public AllContentEventsMessageConverter() {
        this.copyMessageConverter = new ContentCopyMessageConverter();
        this.deleteAndUpdateConverter = new ContentMessageConverter();
        this.ingestMessageConverter = new IngestMessageConverter();
    }

    @Override
    public Object fromMessage(Message msg)
        throws JMSException, MessageConversionException {

        if (msg instanceof MapMessage) {
            MapMessage mapMsg = (MapMessage) msg;
            String action =
                mapMsg.getString(BaseContentMessageConverter.ACTION);
            switch (ContentMessage.ACTION.valueOf(action)) {
                case COPY:
                    return copyMessageConverter.fromMessage(msg);
                case UPDATE:
                    return deleteAndUpdateConverter.fromMessage(msg);
                case DELETE:
                    return deleteAndUpdateConverter.fromMessage(msg);
                case INGEST:
                    return ingestMessageConverter.fromMessage(msg);
            }
        }

        String err = "Arg obj is not an instance of 'MapMessage': ";
        log.error(err + msg);
        throw new MessageConversionException(err);
    }

    @Override
    public Message toMessage(Object obj, Session session)
        throws JMSException, MessageConversionException {
        
        if (obj instanceof ContentMessage) {
            ContentMessage msg = (ContentMessage) obj;
            switch (ContentMessage.ACTION.valueOf(msg.getAction())) {
                case COPY:
                    return copyMessageConverter.toMessage(obj, session);
                case UPDATE:
                    return deleteAndUpdateConverter.toMessage(obj, session);
                case DELETE:
                    return deleteAndUpdateConverter.toMessage(obj, session);
                case INGEST:
                    return ingestMessageConverter.toMessage(obj, session);
            }
        }

        String err = "Arg obj is not instance of 'ContentMessage': ";
        log.error(err + obj);
        throw new MessageConversionException(err);
    }

}
