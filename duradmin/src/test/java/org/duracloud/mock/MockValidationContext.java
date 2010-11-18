/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.mock;

import java.security.Principal;

import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;

public class MockValidationContext
        implements ValidationContext {

    private MessageContext messageContext = new MockMessageContext();

    public MessageContext getMessageContext() {
        return this.messageContext;
    }

    public String getUserEvent() {
        // TODO Auto-generated method stub
        return null;
    }

    public Principal getUserPrincipal() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getUserValue(String property) {
        // TODO Auto-generated method stub
        return null;
    }

}