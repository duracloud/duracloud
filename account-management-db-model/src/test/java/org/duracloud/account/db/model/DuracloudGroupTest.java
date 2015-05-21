/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.model;

import org.junit.Assert;
import org.junit.Test;

public class DuracloudGroupTest {

    @Test
    public void testPrettyName() {
        DuracloudGroup group = new DuracloudGroup();
        group.setName(DuracloudGroup.PREFIX + "test");
        Assert.assertEquals("test", group.getPrettyName());
    }

}
