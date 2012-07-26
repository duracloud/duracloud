/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication.result;

import org.duracloud.common.util.DateUtil;
import org.duracloud.services.ComputeService;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.duracloud.services.duplication.result.DuplicationEvent.TYPE.CONTENT_CREATE;
import static org.duracloud.services.duplication.result.DuplicationEvent.TYPE.CONTENT_UPDATE;
import static org.duracloud.services.duplication.result.DuplicationEvent.TYPE.SPACE_CREATE;
import static org.duracloud.services.duplication.result.DuplicationEvent.TYPE.SPACE_DELETE;
import static org.duracloud.services.duplication.result.DuplicationEvent.TYPE.SPACE_UPDATE;

/**
 * @author Andrew Woods
 *         Date: 9/18/11
 */
public class DuplicationEventTest {

    private DuplicationEvent event;

    private static final String fromStoreId = "from-store-id";
    private static final String toStoreId = "to-store-id";
    private static final String spaceId = "space-id";
    private static final String contentId = "content-id";

    @Test
    public void testGetDelay() throws Exception {
        event = new DuplicationEvent(fromStoreId, toStoreId, SPACE_UPDATE,
                                     spaceId);

        // before setting a delay, delay shows as expired
        long delay = event.getDelay(NANOSECONDS);
        Assert.assertTrue("delay: " + delay, delay < 0);

        // set delay for 500 millis out
        long delayMillis = 500;
        event.setDelay(delayMillis);
        delay = event.getDelay(NANOSECONDS);

        // verify
        long expectedDelay = NANOSECONDS.convert(delayMillis, MILLISECONDS);
        Assert.assertTrue("delay / expected: " + delay + " / " + expectedDelay,
                          delay <= expectedDelay);

        // rest a moment
        Thread.sleep(delayMillis + 1);

        // verify delay has expired
        delay = event.getDelay(NANOSECONDS);
        Assert.assertTrue("delay: " + delay, delay < 0);
    }

    @Test
    public void testCompareTo() throws Exception {
        DuplicationEvent e0 = new DuplicationEvent(fromStoreId, toStoreId,
                                                   SPACE_UPDATE, spaceId);
        DuplicationEvent e1 = new DuplicationEvent(fromStoreId, toStoreId,
                                                   SPACE_UPDATE, spaceId);
        DuplicationEvent e2 = new DuplicationEvent(fromStoreId, toStoreId,
                                                   SPACE_UPDATE, spaceId);

        Assert.assertEquals(0, e0.compareTo(e0));
        Assert.assertEquals(0, e1.compareTo(e1));
        Assert.assertEquals(0, e2.compareTo(e2));

        Assert.assertEquals(0, e0.compareTo(e1));
        Assert.assertEquals(0, e0.compareTo(e2));
        Assert.assertEquals(0, e1.compareTo(e2));

        e1.setDelay(500);
        e2.setDelay(1000);

        long delay0 = e0.getDelay(NANOSECONDS);
        long delay1 = e1.getDelay(NANOSECONDS);
        long delay2 = e2.getDelay(NANOSECONDS);

        Assert.assertTrue(delay0 + ":" + delay1, e0.compareTo(e1) < 0);
        Assert.assertTrue(delay0 + ":" + delay2, e0.compareTo(e2) < 0);
        Assert.assertTrue(delay1 + ":" + delay2, e1.compareTo(e2) < 0);

        Assert.assertTrue(delay2 + ":" + delay0, e2.compareTo(e0) > 0);
        Assert.assertTrue(delay2 + ":" + delay1, e2.compareTo(e1) > 0);
        Assert.assertTrue(delay1 + ":" + delay0, e1.compareTo(e0) > 0);
    }

    @Test
    public void testGetSpaceId() throws Exception {
        event = new DuplicationEvent(fromStoreId, toStoreId, SPACE_DELETE,
                                     spaceId);
        Assert.assertEquals(spaceId, event.getSpaceId());
    }

    @Test
    public void testGetContentId() throws Exception {
        event = new DuplicationEvent(fromStoreId, toStoreId, SPACE_DELETE,
                                     spaceId);
        Assert.assertNull(event.getContentId());

        event = new DuplicationEvent(fromStoreId, toStoreId, CONTENT_CREATE,
                                     spaceId, contentId);
        Assert.assertEquals(contentId, event.getContentId());
    }

    @Test
    public void testIsSuccess() throws Exception {
        event = new DuplicationEvent(fromStoreId, toStoreId, SPACE_DELETE,
                                     spaceId);
        Assert.assertTrue(event.isSuccess());

        event.fail("canned-message");
        Assert.assertTrue(!event.isSuccess());
    }

    @Test
    public void testFail() throws Exception {
        event = new DuplicationEvent(fromStoreId, toStoreId, SPACE_DELETE,
                                     spaceId);

        String msg = "test-event-failed";
        Assert.assertTrue(!event.getEntry().contains(msg));

        event.fail(msg);
        Assert.assertTrue(event.getEntry().contains(msg));
    }

    @Test
    public void testGetHeader() throws Exception {
        event = new DuplicationEvent(fromStoreId, toStoreId, SPACE_DELETE,
                                     spaceId);
        String header = event.getHeader();

        List<String> fields = new ArrayList<String>();
        fields.add("from-store-id");
        fields.add("to-store-id");
        fields.add("space-id");
        fields.add("content-id");
        fields.add("md5");
        fields.add("event");
        fields.add("success");
        fields.add("date-time");
        fields.add("message");

        verifyEntry(fields, header);
    }

    @Test
    public void testGetEntrySpace() throws Exception {
        DuplicationEvent.TYPE type = SPACE_CREATE;
        event = new DuplicationEvent(fromStoreId, toStoreId, type, spaceId);

        String entry = event.getEntry();

        List<String> fields = new ArrayList<String>();
        fields.add(fromStoreId);
        fields.add(toStoreId);
        fields.add(spaceId);
        fields.add("-");
        fields.add("-");
        fields.add(type.name());
        fields.add(Boolean.TRUE.toString());
        fields.add(DateUtil.convertToStringMid(System.currentTimeMillis()));
        fields.add("-");

        verifyEntry(fields, entry);
    }

    @Test
    public void testGetEntryContent() throws Exception {
        DuplicationEvent.TYPE type = CONTENT_UPDATE;
        String error = "canned-message";

        event = new DuplicationEvent(fromStoreId, toStoreId, type, spaceId, contentId);
        event.fail(error);

        String entry = event.getEntry();

        List<String> fields = new ArrayList<String>();
        fields.add(fromStoreId);
        fields.add(toStoreId);
        fields.add(spaceId);
        fields.add(contentId);
        fields.add("-");
        fields.add(type.name());
        fields.add(Boolean.FALSE.toString());
        fields.add(DateUtil.convertToStringMid(System.currentTimeMillis()));
        fields.add(error);

        verifyEntry(fields, entry);
    }

    private void verifyEntry(List<String> fields, String entry)
        throws ParseException {
        Assert.assertNotNull(entry);

        String[] cols = entry.split(Character.toString(ComputeService.DELIM));
        Assert.assertEquals(fields.size(), cols.length);

        for (int i = 0; i < fields.size(); ++i) {
            String field = fields.get(i);
            String col = cols[i];
            Assert.assertEquals(i + ": " + field + "|" + col, field, col);
        }
    }

    @Test
    public void testGetType() throws Exception {
        DuplicationEvent.TYPE type = SPACE_CREATE;
        event = new DuplicationEvent(fromStoreId, toStoreId, type, spaceId);

        Assert.assertEquals(type, event.getType());
    }

    @Test
    public void testEquals() throws Exception {
        DuplicationEvent e0 = new DuplicationEvent(fromStoreId, toStoreId,
                                                   SPACE_UPDATE, spaceId);
        DuplicationEvent e1 = new DuplicationEvent(fromStoreId, toStoreId,
                                                   SPACE_UPDATE, spaceId);
        DuplicationEvent e2 = new DuplicationEvent(fromStoreId, toStoreId,
                                                   SPACE_UPDATE, spaceId);

        e0.setDelay(500);
        e1.setDelay(5000);
        e2.setDelay(50000);

        Assert.assertEquals(e0, e0);
        Assert.assertEquals(e0, e1);
        Assert.assertEquals(e0, e2);
        Assert.assertEquals(e1, e2);

        e0.fail("error-msg");
        e1.fail("error-msg-different");

        Assert.assertEquals(e0, e0);
        Assert.assertEquals(e0, e1);
        Assert.assertEquals(e0, e2);
        Assert.assertEquals(e1, e2);
    }

    @Test
    public void testEqualsNot() throws Exception {
        DuplicationEvent e0 = new DuplicationEvent(fromStoreId, toStoreId,
                                                   SPACE_UPDATE, spaceId);
        DuplicationEvent e1 = new DuplicationEvent(fromStoreId, toStoreId,
                                                   SPACE_CREATE, spaceId);
        DuplicationEvent e2 = new DuplicationEvent(fromStoreId, toStoreId,
                                                   CONTENT_UPDATE, spaceId, contentId);

        Assert.assertTrue(!e0.equals(e1));
        Assert.assertTrue(!e0.equals(e2));
        Assert.assertTrue(!e1.equals(e2));
    }

}
