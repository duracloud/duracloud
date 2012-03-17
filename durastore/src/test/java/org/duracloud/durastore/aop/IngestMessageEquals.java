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

public class IngestMessageEquals implements IArgumentMatcher {
    private IngestMessage ingestMessage;

    public IngestMessageEquals(IngestMessage ingestMessage) {
        this.ingestMessage = ingestMessage;
    }

    public boolean matches(Object actual) {
        if (!(actual instanceof IngestMessage)) {
            return false;
        }
        IngestMessage message = (IngestMessage) actual;

        boolean matches = true;

        if (ingestMessage.getSpaceId() != null) {
            matches = ingestMessage.getSpaceId().equals(message.getSpaceId());
        } else if (message.getSpaceId() != null) {
            return false;
        }

        if (!matches) {
            return false;
        }

        if (ingestMessage.getStoreId() != null) {
            matches = ingestMessage.getStoreId().equals(message.getStoreId());
        } else if (message.getStoreId() != null) {
            return false;
        }

        if (!matches) {
            return false;
        }

        if (ingestMessage.getContentId() != null) {
            matches =
                ingestMessage.getContentId().equals(message.getContentId());
        } else if (message.getContentId() != null) {
            return false;
        }

        if (!matches) {
            return false;
        }

        if (ingestMessage.getContentMimeType() != null) {
            matches = ingestMessage.getContentMimeType()
                                   .equals(message.getContentMimeType());
        } else if (message.getContentMimeType() != null) {
            return false;
        }

        if (!matches) {
            return false;
        }

        if (ingestMessage.getContentMd5() != null) {
            matches =
                ingestMessage.getContentMd5().equals(message.getContentMd5());
        } else if (message.getContentMd5() != null) {
            return false;
        }

        if (!matches) {
            return false;
        }

        matches = ingestMessage.getContentSize() == message.getContentSize();

        return matches;
    }

    public void appendTo(StringBuffer buffer) {
        buffer.append("eqIngestMessage(");
        buffer.append(ingestMessage.getClass().getName());
        buffer.append(" with store id \"");
        buffer.append(ingestMessage.getStoreId());
        buffer.append("\" and with space id \"");
        buffer.append(ingestMessage.getStoreId());
        buffer.append("\" and with content id \"");
        buffer.append(ingestMessage.getContentId());
        buffer.append("\" and with mime type \"");
        buffer.append(ingestMessage.getContentMimeType());
        buffer.append("\" and with content size\"");
        buffer.append(ingestMessage.getContentSize());
        buffer.append("\" and with content MD5 \"");
        buffer.append(ingestMessage.getContentMd5());
        buffer.append("\")");
    }

    public static <T extends IngestMessage> T eqIngestMessage(T in) {
        EasyMock.reportMatcher(new IngestMessageEquals(in));
        return null;
    }
}
