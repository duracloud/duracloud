/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.aop;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

public class SpaceMessageConverter
        implements MessageConverter {

    protected final Logger log = LoggerFactory.getLogger(SpaceMessageConverter.class);

    protected static final String STORE_ID = "storeId";

    protected static final String SPACE_ID = "spaceId";

    public Object fromMessage(Message msg) throws JMSException,
            MessageConversionException {
        if (!(msg instanceof MapMessage)) {
            String err = "Arg obj is not an instance of 'MapMessage': ";
            log.error(err + msg);
            throw new MessageConversionException(err);
        }

        MapMessage mapMsg = (MapMessage)msg;
        SpaceMessage spaceMsg = new SpaceMessage();
        spaceMsg.setStoreId(mapMsg.getStringProperty(STORE_ID));
        spaceMsg.setSpaceId(mapMsg.getString(SPACE_ID));
        return spaceMsg;
    }

    public Message toMessage(Object obj, Session session) throws JMSException,
            MessageConversionException {
        if (!(obj instanceof SpaceMessage)) {
            String err = "Arg obj is not an instance of 'SpaceMessage': ";
            log.error(err + obj);
            throw new MessageConversionException(err);
        }
        SpaceMessage spaceMsg = (SpaceMessage) obj;

        MapMessage msg = session.createMapMessage();
        msg.setStringProperty(STORE_ID, spaceMsg.getStoreId());
        msg.setString(SPACE_ID, spaceMsg.getSpaceId());
        return msg;
    }

}
