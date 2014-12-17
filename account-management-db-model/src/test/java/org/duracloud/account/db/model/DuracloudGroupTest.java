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
