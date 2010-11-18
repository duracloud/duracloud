/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.mock;

import java.util.LinkedList;
import java.util.List;

import org.springframework.binding.message.Message;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.message.MessageCriteria;
import org.springframework.binding.message.MessageResolver;

public class MockMessageContext
        implements MessageContext {

    public List<Message> messages = new LinkedList<Message>();

    public boolean hasErrorMessages() {
        // TODO Auto-generated method stub
        return false;
    }

    public Message[] getMessagesBySource(Object source) {
        // TODO Auto-generated method stub
        return null;
    }

    public Message[] getMessagesByCriteria(MessageCriteria criteria) {
        // TODO Auto-generated method stub
        return null;
    }

    public Message[] getAllMessages() {
        return messages.toArray(new Message[0]);
    }

    public void clearMessages() {
        // TODO Auto-generated method stub

    }

    public void addMessage(MessageResolver messageResolver) {
        messages.add(new Message(null, messageResolver.toString(), null));
    }
}
