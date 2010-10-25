/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.common.model;

import org.duracloud.services.common.error.ServiceException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Andrew Woods
 *         Date: Jan 27, 2010
 */
public class NamedFilterListTest {

    private NamedFilterList list;

    private String file0 = "viewer.html";
    private String file1 = "index.html";
    private String file2 = "WEB-INF/classes/djatoka.properties";

    private String target0a = "$DURA_HOST$";
    private String target0b = "$DURA_PORT$";
    private String target1 = "$DURA_TITLE$";
    private String target2 = "$DURA_PROP$";

    private String value0a = "test.duracloud.org";
    private String value0b = "12345";
    private String value1 = "DuraCloud Title";
    private String value2 = "prop=true";

    @Before
    public void setUp() {
        Map<String, String> filters0 = new HashMap<String, String>();
        Map<String, String> filters1 = new HashMap<String, String>();
        Map<String, String> filters2 = new HashMap<String, String>();

        filters0.put(target0a, value0a);
        filters0.put(target0b, value0b);
        filters1.put(target1, value1);
        filters2.put(target2, value2);

        NamedFilterList.NamedFilter namedFilter0 = new NamedFilterList.NamedFilter(
            file0,
            filters0);
        NamedFilterList.NamedFilter namedFilter1 = new NamedFilterList.NamedFilter(
            file1,
            filters1);
        NamedFilterList.NamedFilter namedFilter2 = new NamedFilterList.NamedFilter(
            file2,
            filters2);

        List<NamedFilterList.NamedFilter> namedFilters = new ArrayList<NamedFilterList.NamedFilter>();
        namedFilters.add(namedFilter0);
        namedFilters.add(namedFilter1);
        namedFilters.add(namedFilter2);
        list = new NamedFilterList(namedFilters);
    }

    @After
    public void tearDown() {
        list = null;
    }

    @Test
    public void test() throws ServiceException {
        List<String> names = list.getNames();
        Assert.assertEquals(3, names.size());
        Assert.assertTrue(names.contains(file0));
        Assert.assertTrue(names.contains(file1));
        Assert.assertTrue(names.contains(file2));

        // file0
        NamedFilterList.NamedFilter filter = list.getFilter(file0);
        Assert.assertNotNull(filter);

        Assert.assertEquals(file0, filter.getName());

        Set<String> targets = filter.getFilterTargets();
        Assert.assertNotNull(targets);
        Assert.assertEquals(2, targets.size());
        Assert.assertTrue(target0a, targets.contains(target0a));
        Assert.assertTrue(target0b, targets.contains(target0b));

        Assert.assertEquals(value0a, filter.getFilterValue(target0a));
        Assert.assertEquals(value0b, filter.getFilterValue(target0b));

        // file1
        filter = list.getFilter(file1);
        Assert.assertNotNull(filter);

        Assert.assertEquals(file1, filter.getName());

        targets = filter.getFilterTargets();
        Assert.assertNotNull(targets);
        Assert.assertEquals(1, targets.size());
        Assert.assertTrue(target1, targets.contains(target1));

        Assert.assertEquals(value1, filter.getFilterValue(target1));

        // file2
        filter = list.getFilter(file2);
        Assert.assertNotNull(filter);

        Assert.assertEquals(file2, filter.getName());

        targets = filter.getFilterTargets();
        Assert.assertNotNull(targets);
        Assert.assertEquals(1, targets.size());
        Assert.assertTrue(target2, targets.contains(target2));

        Assert.assertEquals(value2, filter.getFilterValue(target2));

    }

}
