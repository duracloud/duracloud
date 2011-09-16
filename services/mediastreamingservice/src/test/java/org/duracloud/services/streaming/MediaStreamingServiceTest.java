/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.streaming;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author: Bill Branan
 * Date: 9/16/11
 */
public class MediaStreamingServiceTest {

    @Test
    public void testCreateMessageSelector() {
        MediaStreamingService service = new MediaStreamingService();
        String spaceId1 = "video-space";
        String spaceId2 = "audio-space";
        String[] spaceIds = {spaceId1, spaceId2};

        String messageSelector = service.createMessageSelector(spaceIds);
        assertNotNull(messageSelector);
        String expected =
            "(spaceId = '" + spaceId1 + "') OR (spaceId = '" + spaceId2 +"')";
        assertEquals(expected, messageSelector);
    }
    
}
