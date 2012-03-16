/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.aop;

import org.easymock.IArgumentMatcher;
import org.easymock.EasyMock;

public class ContentMessageEquals implements IArgumentMatcher {
    private ContentMessage contentMessage;

    public ContentMessageEquals(ContentMessage contentMessage) {
        this.contentMessage = contentMessage;
    }

    public boolean matches(Object actual) {
        if (!(actual instanceof ContentMessage)) {
            return false;
        }
        ContentMessage message = (ContentMessage) actual;

        boolean matches = true;

        if(contentMessage.getSpaceId() != null)
            matches = contentMessage.getSpaceId().equals(message.getSpaceId());
        else if(message.getSpaceId() != null)
            return false;

        if(!matches)
            return false;

        if(contentMessage.getStoreId() != null)
            matches = contentMessage.getStoreId().equals(message.getStoreId());
        else if(message.getStoreId() != null)
            return false;

        if(!matches)
            return false;

        if(contentMessage.getContentId() != null)
            matches = contentMessage.getContentId().equals(message.getContentId());
        else if(message.getContentId() != null)
            return false;

        return matches;
    }

    public void appendTo(StringBuffer buffer) {
        buffer.append("eqContentMessage(");
        buffer.append(contentMessage.getClass().getName());
        buffer.append(" with store id \"");
        buffer.append(contentMessage.getStoreId());
        buffer.append("\" and with space id \"");
        buffer.append(contentMessage.getStoreId());
        buffer.append("\" and with content id \"");
        buffer.append(contentMessage.getContentId());
        buffer.append("\")");
    }

    public static <T extends ContentMessage> T eqContentMessage(T in) {
        EasyMock.reportMatcher(new ContentMessageEquals(in));
        return null;
    }
}
