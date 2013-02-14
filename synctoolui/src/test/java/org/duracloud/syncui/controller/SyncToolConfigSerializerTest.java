/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.controller;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.duracloud.sync.config.SyncToolConfig;
import org.duracloud.syncui.service.SyncToolConfigSerializer;
import org.duracloud.syncui.AbstractTest;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class SyncToolConfigSerializerTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testSerializeDeserialize() throws IOException {
        SyncToolConfig c1 = new SyncToolConfig();
        String username = "test-username";
        c1.setUsername(username);
        File file = File.createTempFile("synctoolconfig", ".xml");
        file.deleteOnExit();
        SyncToolConfigSerializer.serialize(c1, file.getAbsolutePath());
        SyncToolConfig c2 = SyncToolConfigSerializer.deserialize(file.getAbsolutePath());
        Assert.assertEquals(c1.getUsername(), c2.getUsername());
    }

}
