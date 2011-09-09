/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

import org.duracloud.chunk.manifest.ChunksManifest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.duracloud.chunk.manifest.ChunksManifest.manifestSuffix;

/**
 * @author Andrew Woods
 *         Date: 9/9/11
 */
public class ChunkFilteredIteratorTest {

    private ChunkFilteredIterator iterator;

    private List<String> space;

    private int numItems;
    private int numChunks;
    private int numManifests;

    @Before
    public void setUp() throws Exception {
        space = new ArrayList<String>();

        numItems = 0;
        numChunks = 0;
        numManifests = 0;
    }

    @Test
    public void testHasNext0() throws Exception {
        space.add(item());
        space.add(item());
        space.add(item());
        space.add(chunk());
        space.add(chunk());
        space.add(manifest());
        space.add(item());
        space.add(item());

        doTestHasNext();
    }

    @Test
    public void testHasNext1() throws Exception {
        space.add(manifest());
        space.add(item());
        space.add(item());
        space.add(chunk());
        space.add(chunk());
        space.add(item());
        space.add(item());
        space.add(manifest());

        doTestHasNext();
    }

    @Test
    public void testHasNext2() throws Exception {
        space.add(chunk());
        space.add(manifest());
        space.add(item());
        space.add(item());
        space.add(item());
        space.add(item());
        space.add(item());
        space.add(manifest());
        space.add(chunk());

        doTestHasNext();
    }

    @Test
    public void testHasNext3() throws Exception {
        space.add(chunk());
        space.add(chunk());
        space.add(chunk());
        space.add(chunk());

        doTestHasNext();
    }

    @Test
    public void testHasNext4() throws Exception {
        space.add(manifest());
        space.add(manifest());
        space.add(manifest());

        doTestHasNext();
    }

    @Test
    public void testHasNext5() throws Exception {
        doTestHasNext();
    }

    private void doTestHasNext() {
        iterator = new ChunkFilteredIterator(space.iterator());
        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }

        Assert.assertEquals(numItems + numManifests, count);
    }

    private String contentId() {
        return "content-item-";
    }

    private String item() {
        return contentId() + numItems++;
    }

    private String chunk() {
        int i = numChunks++;
        return contentId() + i + ChunksManifest.chunkSuffix + i;
    }

    private String manifest() {
        return contentId() + numManifests++ + manifestSuffix;
    }

    @Test
    public void testNext() throws Exception {
        space.add(item());
        space.add(item());
        space.add(item());
        space.add(chunk());
        space.add(chunk());
        space.add(manifest());
        space.add(item());
        space.add(item());

        iterator = new ChunkFilteredIterator(space.iterator());
        Assert.assertEquals(space.get(0), iterator.next());
        Assert.assertEquals(space.get(1), iterator.next());
        Assert.assertEquals(space.get(2), iterator.next());
        Assert.assertEquals(space.get(5), iterator.next() + manifestSuffix);
        Assert.assertEquals(space.get(6), iterator.next());
        Assert.assertEquals(space.get(7), iterator.next());
        Assert.assertNull(iterator.next());
    }

    @Test
    public void testRemove() throws Exception {
        iterator = new ChunkFilteredIterator(space.iterator());

        try {
            iterator.remove();
            Assert.fail("exception expected.");
        } catch (UnsupportedOperationException e) {
        }
    }

}
