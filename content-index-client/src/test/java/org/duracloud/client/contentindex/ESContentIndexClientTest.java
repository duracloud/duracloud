/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.contentindex;

import org.duracloud.storage.domain.StorageProviderType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.io.FileSystemUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.duracloud.client.contentindex.ESContentIndexClient.ID_SEPARATOR;
import static org.duracloud.client.contentindex.ESContentIndexClient.SHARED_INDEX;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * @author Erik Paulsson
 *         Date: 3/12/14
 */
public class ESContentIndexClientTest {

    ContentIndexClient contentIndexClient;

    protected String account1 = "account1";
    protected String account2 = "account2";
    protected int storeId = 1;
    protected String space = "space1";

    private static Node node;
    protected static String datadir;
    private static final String TMP_PATH = "target";
    private static final String HTTP_PORT = "9205";
    private static final String HTTP_TRANSPORT_PORT = "9305";

    /**
     * Create embedded ElasticSearch server
     * @throws Exception
     */
    @BeforeClass
    public static void classSetup() throws Exception {
        final String nodeName = "junittestnode";
        datadir = TMP_PATH + "/" + nodeName;

        Map settingsMap = new HashMap();
        // create all data directories under Maven build directory
        settingsMap.put("path.conf", TMP_PATH);
        settingsMap.put("path.data", TMP_PATH);
        settingsMap.put("path.work", TMP_PATH);
        settingsMap.put("path.logs", TMP_PATH);
        // set ports used by Elastic Search to something different than default
        settingsMap.put("http.port", HTTP_PORT);
        settingsMap.put("transport.tcp.port", HTTP_TRANSPORT_PORT);
        settingsMap.put("index.number_of_shards", "1");
        settingsMap.put("index.number_of_replicas", "0");
        // disable clustering
        settingsMap.put("discovery.zen.ping.multicast.enabled", "false");
        // disable automatic index creation
        settingsMap.put("action.auto_create_index", "false");

        Settings settings = ImmutableSettings.settingsBuilder()
                                             .put(settingsMap).build();
        node = nodeBuilder().settings(settings).clusterName(nodeName)
            .client(false).node();
        node.start();
    }

    @AfterClass
    public static void classTeardown() {
        // stop ElasticSearch
        node.close();
        // delete data directory used by ElasticSearch
        File dataDir = new File(datadir);
        if (dataDir.exists()) {
            FileSystemUtils.deleteRecursively(dataDir, true);
        }
    }

    @Before
    public void setUp() {
        Client client = node.client();
        ElasticsearchOperations elasticsearchOperations =
            new ElasticsearchTemplate(client);
        contentIndexClient = new ESContentIndexClient(elasticsearchOperations,
                                                      client);
        contentIndexClient.addIndex(SHARED_INDEX, false);
        contentIndexClient.addIndex(account1, true);
        contentIndexClient.addIndex(account2, true);
    }

    @Test
    public void testSaveContentIndexItem() {
        ContentIndexItem item1 = createContentIndexItem(account1, 5);
        String item1Id = item1.getId();
        String checksum1 = item1.getProps().get("checksum");

        String returnedId = contentIndexClient.save(item1);
        assertNotNull(returnedId);
        assertEquals(item1Id, returnedId);

        ContentIndexItem retrieved = contentIndexClient.get(
            account1, storeId, space, item1.getContentId());
        assertFalse(retrieved == item1); // assert not the same object in memory
        assertNotNull(retrieved);
        assertEquals(checksum1, retrieved.getProps().get("checksum"));

        List<ContentIndexItem> items = new ArrayList();
        items.add(createContentIndexItem(account1, 4));
        items.add(createContentIndexItem(account1, 3));
        items.add(createContentIndexItem(account1, 2));
        items.add(createContentIndexItem(account1, 1));
        contentIndexClient.bulkSave(items);

        items = new ArrayList();
        items.add(createContentIndexItem(account2, 3));
        items.add(createContentIndexItem(account2, 2));
        items.add(createContentIndexItem(account2, 1));
        contentIndexClient.bulkSave(items);

        long count1 = contentIndexClient.getSpaceCount(account1, storeId, space);
        long count2 = contentIndexClient.getSpaceCount(account2, storeId, space);
        //TODO: uncomment once count bug is fixed in spring-data-elasticsearch
        // JIRA filed: https://jira.spring.io/browse/DATAES-67
        //assertEquals(5, count1);
        //assertEquals(3, count2);

        List<ContentIndexItem> itemsFull1 =
            contentIndexClient.getSpaceContents(account1, storeId, space);
        List<ContentIndexItem> itemsFull2 =
            contentIndexClient.getSpaceContents(account2, storeId, space);

        assertEquals(5, itemsFull1.size());
        int idIndex = 1;
        for(ContentIndexItem item: itemsFull1) {
            assertItemFieldsNotNull(item);
            assertEquals(account1, item.getAccount());
            //TODO: uncomment after sorting is figured out... bug in Spring ES?
//            System.out.println("#### contentId: " + item.getContentId());
//            assertTrue(item.getContentId().endsWith(idIndex+".txt"));
//            idIndex++;
        }

        assertEquals(3, itemsFull2.size());
        idIndex = 1;
        for(ContentIndexItem item: itemsFull2) {
            assertItemFieldsNotNull(item);
            assertEquals(account2, item.getAccount());
            //TODO: uncomment after sorting is figured out... bug in Spring ES?
//            assertTrue(item.getContentId().endsWith(idIndex+".txt"));
//            idIndex++;
        }

        List<ContentIndexItem> itemsId1 =
            contentIndexClient.getSpaceContentIds(account1, storeId, space);
        List<ContentIndexItem> itemsId2 =
            contentIndexClient.getSpaceContentIds(account2, storeId, space);

        assertEquals(5, itemsId1.size());
        idIndex = 1;
        for(ContentIndexItem item: itemsId1) {
            assertItemFieldsNull(item);
            //TODO: uncomment after sorting is figured out... bug in Spring ES?
//            assertTrue(item.getContentId().endsWith(idIndex+".txt"));
//            idIndex++;
        }

        assertEquals(3, itemsId2.size());
        idIndex = 1;
        for(ContentIndexItem item: itemsId2) {
            assertItemFieldsNull(item);
            //TODO: uncomment after sorting is figured out... bug in Spring ES?
//            assertTrue(item.getContentId().endsWith(idIndex+".txt"));
//            idIndex++;
        }

    }

    protected void assertItemFieldsNotNull(ContentIndexItem item) {
        assertNotNull(item.getId());
        assertNotNull(item.getAccount());
        assertNotNull(item.getStoreId());
        assertNotNull(item.getSpace());
        assertNotNull(item.getContentId());
        assertNotNull(item.getStoreType());
        assertNotNull(item.getProps().get("checksum"));
    }

    protected void assertItemFieldsNull(ContentIndexItem item) {
        assertNotNull(item.getId());
        assertNotNull(item.getContentId());
        assertNull(item.getAccount());
        assertNull(item.getStoreId());
        assertNull(item.getSpace());
        assertNull(item.getStoreType());
        assertNull(item.getProps());
    }

    protected ContentIndexItem createContentIndexItem(String account,
                                                      int identifier) {
        String contentId = "contentDir1/contentDir2/file" + identifier + ".txt";
        String checksum = "XXXchecksum" + identifier + "XXX";
        String itemId = account + ID_SEPARATOR + storeId + ID_SEPARATOR +
            space + ID_SEPARATOR + contentId;

        ContentIndexItem item = new ContentIndexItem();
        item.setId(itemId);
        item.setAccount(account);
        item.setStoreId(storeId);
        item.setStoreType(StorageProviderType.AMAZON_S3.getName());
        item.setSpace(space);
        item.setContentId(contentId);
        Map props = new HashMap();
        props.put("checksum", checksum);
        item.setProps(props);

        return item;
    }
}
