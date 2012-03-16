/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.fixityproperties;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.error.ContentStoreException;
import org.duracloud.storage.domain.HadoopTypes;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.duracloud.services.hadoop.base.Constants.DELIM;

/**
 * @author Andrew Woods
 *         Date: Feb 9, 2011
 */
public class HashPropertiesFinderMapperTest {

    private HashPropertiesFinderMapper mapper;

    private String hash;

    private JobConf jobConf;
    private Text key;
    private Text value;
    private OutputCollector output;
    private Reporter reporter;
    private ContentStoreManager storeMgr;
    private ContentStore contentStore;

    @Before
    public void setUp() throws Exception {
        mapper = new HashPropertiesFinderMapper();
        hash = "5d41402abc4b2a76b9719d911017c592";
    }

    @After
    public void tearDown() {
        if (null != jobConf) {
            EasyMock.verify(jobConf);
        }
        if (null != key) {
            EasyMock.verify(key);
        }
        if (null != value) {
            EasyMock.verify(value);
        }
        if (null != output) {
            EasyMock.verify(output);
        }
        if (null != reporter) {
            EasyMock.verify(reporter);
        }
        if (null != storeMgr) {
            EasyMock.verify(storeMgr);
        }
        if (null != contentStore) {
            EasyMock.verify(contentStore);
        }
    }

    private void replayMocks() {
        EasyMock.replay(jobConf,
                        key,
                        value,
                        output,
                        reporter,
                        storeMgr,
                        contentStore);
    }

    @Test
    public void testMapNoContentStore() throws IOException {
        createMocks();
        createMockExpectations();
        replayMocks();

        mapper.configure(jobConf);
        mapper.map(key, value, output, reporter);

        String result = mapper.collectResult();
        Assert.assertNotNull(result);
        Assert.assertTrue(result, result.contains("malformed"));
    }

    @Test
    public void testMap() throws IOException, ContentStoreException {
        createMocks();
        createMockExpectations();

        Map<String, String> properties = new HashMap<String, String>();
        properties.put(StorageProvider.PROPERTIES_CONTENT_MD5, hash);
        EasyMock.expect(
            contentStore.getContentProperties(EasyMock.isA(String.class),
                                              EasyMock.isA(String.class)))
            .andReturn(properties);
        EasyMock.expect(storeMgr.getContentStore(EasyMock.isA(String.class)))
            .andReturn(contentStore);

        replayMocks();

        mapper.configure(jobConf);
        mapper.setStoreManager(storeMgr);
        mapper.map(key, value, output, reporter);

        String result = mapper.collectResult();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains(hash));
    }

    private void createMocks() {
        jobConf = EasyMock.createMock("JobConf", JobConf.class);

        String dcHost = "host";
        EasyMock.expect(jobConf.get(HadoopTypes.TASK_PARAMS
                                        .DC_HOST
                                        .getLongForm())).andReturn(dcHost);
        String dcPort = "443";
        EasyMock.expect(jobConf.get(HadoopTypes.TASK_PARAMS
                                        .DC_PORT
                                        .getLongForm())).andReturn(dcPort);
        String dcCtxt = "context";
        EasyMock.expect(jobConf.get(HadoopTypes.TASK_PARAMS
                                        .DC_CONTEXT
                                        .getLongForm())).andReturn(dcCtxt);
        String dcUser = "user";
        EasyMock.expect(jobConf.get(HadoopTypes.TASK_PARAMS
                                        .DC_USERNAME
                                        .getLongForm())).andReturn(dcUser);
        String dcPass = "password";
        EasyMock.expect(jobConf.get(HadoopTypes.TASK_PARAMS
                                        .DC_PASSWORD
                                        .getLongForm())).andReturn(dcPass);

        String dcStoreId = "1";
        EasyMock.expect(jobConf.get(HadoopTypes.TASK_PARAMS
                                        .DC_STORE_ID
                                        .getLongForm())).andReturn(dcStoreId);

        key = EasyMock.createMock("Text", Text.class);
        value = EasyMock.createMock("Text", Text.class);
        output = EasyMock.createMock("OutputCollector", OutputCollector.class);
        reporter = EasyMock.createMock("Reporter", Reporter.class);
        storeMgr = EasyMock.createMock("ContentStoreManager",
                                       ContentStoreManager.class);
        contentStore = EasyMock.createMock("ContentStore", ContentStore.class);
    }

    private void createMockExpectations() throws IOException {
        output.collect(EasyMock.isA(Text.class), EasyMock.isA(Text.class));
        EasyMock.expectLastCall();

        reporter.setStatus(EasyMock.isA(String.class));
        EasyMock.expectLastCall().atLeastOnce();
    }

    @Test
    public void testCollectResult() throws IOException {
        String result = mapper.collectResult();
        Assert.assertNotNull(result);
        Assert.assertEquals(
            "null-space" + DELIM + "null-content-id" + DELIM + "null", result);
    }

}
