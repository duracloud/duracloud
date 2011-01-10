/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.aop;

import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;

public class SpaceMessageEquals implements IArgumentMatcher {
    private SpaceMessage spaceMessage;

    public SpaceMessageEquals(SpaceMessage spaceMessage) {
        this.spaceMessage = spaceMessage;
    }

    public boolean matches(Object actual) {
        if (!(actual instanceof SpaceMessage)) {
            return false;
        }
        SpaceMessage message = (SpaceMessage) actual;

        boolean matches = true;

        if(spaceMessage.getSpaceId() != null)
            matches = spaceMessage.getSpaceId().equals(message.getSpaceId());
        else if(message.getSpaceId() != null)
            return false;

        if(!matches)
            return false;

        if(spaceMessage.getStoreId() != null)
            matches = spaceMessage.getStoreId().equals(message.getStoreId());
        else if(message.getStoreId() != null)
            return false;

        return matches;
    }

    public void appendTo(StringBuffer buffer) {
        buffer.append("eqSpaceMessage(");
        buffer.append(spaceMessage.getClass().getName());
        buffer.append(" with store id \"");
        buffer.append(spaceMessage.getStoreId());
        buffer.append("\" and with space id \"");
        buffer.append(spaceMessage.getStoreId());
        buffer.append("\")");
    }

    public static <T extends SpaceMessage> T eqSpaceMessage(T in) {
        EasyMock.reportMatcher(new SpaceMessageEquals(in));
        return null;
    }
}
