/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.contentindex.client;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.duracloud.contentindex.client.ESContentIndexClient.SHARED_INDEX;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.provider.StorageProvider;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.io.FileSystemUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

/**
 * @author Erik Paulsson
 *         Date: 3/12/14
 */
public class ESContentIndexClientTest {

    private static final String CHECKSUM_KEY = StorageProvider.PROPERTIES_CONTENT_CHECKSUM;

    private static ContentIndexClient contentIndexClient;

    protected static String account1 = "account1";
    protected static String account2 = "account2";
    protected String storeId = "1";
    protected String spacePrefix = "space";
    protected String space = spacePrefix + "1";
    protected String key1 = "key1", key2 = "key2",
        value1 = "value1", value2 = "value2";

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

        Map<String,String> settingsMap = new HashMap<>();
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
        // disable automatic type creation
        settingsMap.put("index.mapper.dynamic", "false");

        Settings settings = ImmutableSettings.settingsBuilder()
                                             .put(settingsMap).build();
        node = nodeBuilder().settings(settings).clusterName(nodeName)
            .client(false).node();
        node.start();

        Client client = node.client();
        ElasticsearchOperations elasticsearchOperations =
            new ElasticsearchTemplate(client);
        contentIndexClient = new ESContentIndexClient(elasticsearchOperations,
                                                      client);
        contentIndexClient.addIndex(SHARED_INDEX, false);
        contentIndexClient.addIndex(account1, true);
        contentIndexClient.addIndex(account2, true);

        elasticsearchOperations.putMapping(ContentIndexItem.class);
        elasticsearchOperations.putMapping(AccountIndexItem.class);
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

    @Test
    public void testAccountItems() {
        Map<String, Integer> storeSpaceCreate = new HashMap<>();
        storeSpaceCreate.put("0", 50);
        storeSpaceCreate.put("1", 100);
        AccountIndexItem accountIndexItem =
            createAccountItem("account1", storeSpaceCreate);
        contentIndexClient.save(accountIndexItem);

        Collection<String> spaces = contentIndexClient.getSpaces("account1", "0");
        assertEquals(50, spaces.size());
        checkSpacesOrder(spaces, 0);
    }

    /**
     * Checks that spaces are returned in alphanumeric order
     * @param spaces
     * @param storeId
     */
    protected void checkSpacesOrder(Collection<String> spaces, int storeId) {
        int i = 1;
        for(String space: spaces) {
            assertTrue(space.equals(
                spacePrefix + storeId + "-" + String.format("%03d", i)));
            i++;
        }
    }

    protected AccountIndexItem createAccountItem(
            String account,
            Map<String, Integer> storeSpaces) {
        AccountIndexItem item = new AccountIndexItem();
        item.setId(account);
        Set<String> spaces = new HashSet<>();
        for(String storeId: storeSpaces.keySet()) {
            spaces.clear();
            int numSpaces = storeSpaces.get(storeId);
            for(int i = numSpaces; i >= 1; i--) {
                spaces.add(spacePrefix + storeId + "-" + String.format("%03d", i));
            }
            item.addSpaces(storeId, spaces);
        }
        return item;
    }

    @Test
    public void testSaveContentIndexItem() throws ContentIndexClientValidationException {
        ContentIndexItem item5 = createContentIndexItem(account1, 5);
        item5.addTag(value1);
        String item5Id = item5.getId();
        String checksum1 = item5.getProps().get(CHECKSUM_KEY);

        String returnedId = contentIndexClient.save(item5);
        assertNotNull(returnedId);
        assertEquals(item5Id, returnedId);

        ContentIndexItem retrieved = contentIndexClient.get(
            account1, storeId, space, item5.getContentId());
        assertFalse(retrieved == item5); // assert not the same object in memory
        assertNotNull(retrieved);
        assertEquals(checksum1, retrieved.getProps().get(CHECKSUM_KEY));

        List<ContentIndexItem> items = new ArrayList<>();
        items.add(createContentIndexItem(account1, 4));
        items.add(createContentIndexItem(account1, 3).addProp(key1, value1));
        items.add(createContentIndexItem(account1, 2));
        items.add(createContentIndexItem(account1, 1));
        contentIndexClient.bulkSave(items);

        items = new ArrayList<>();
        items.add(createContentIndexItem(account2, 3)
                      .addProp(key1, value1).addProp(key2, value2));
        items.add(createContentIndexItem(account2, 2)
                      .addProp(key1, value1).addTag(value2));
        items.add(createContentIndexItem(account2, 1).addTag(value1));
        contentIndexClient.bulkSave(items);

        long count1 = contentIndexClient.getSpaceCount(account1, storeId, space);
        long count2 = contentIndexClient.getSpaceCount(account2, storeId, space);
        assertEquals(5, count1);
        assertEquals(3, count2);

        Iterator<ContentIndexItem> itemsFull1 =
            contentIndexClient.getSpaceContents(account1, storeId, space);
        Iterator<ContentIndexItem> itemsFull2 =
            contentIndexClient.getSpaceContents(account2, storeId, space);

        int idIndex = 0;
        while(itemsFull1.hasNext()) {
            idIndex++;
            ContentIndexItem item = itemsFull1.next();
            assertItemFieldsNotNull(item);
            assertEquals(account1, item.getAccount());
            assertTrue(item.getContentId().endsWith(idIndex+".txt"));
        }
        assertEquals(5, idIndex);

        idIndex = 0;
        while(itemsFull2.hasNext()) {
            idIndex++;
            ContentIndexItem item = itemsFull2.next();
            assertItemFieldsNotNull(item);
            assertEquals(account2, item.getAccount());
            assertTrue(item.getContentId().endsWith(idIndex+".txt"));
        }
        assertEquals(3, idIndex);

        Iterator<String> itemsId1 = contentIndexClient.getSpaceContentIds(
            account1, storeId, space);
        Iterator<String> itemsId2 = contentIndexClient.getSpaceContentIds(
            account2, storeId, space);

        idIndex = 0;
        while(itemsId1.hasNext()) {
            idIndex++;
            assertTrue(itemsId1.next().endsWith(idIndex+".txt"));
        }
        assertEquals(5, idIndex);

        idIndex = 0;
        while(itemsId2.hasNext()) {
            idIndex++;
            assertTrue(itemsId2.next().endsWith(idIndex+".txt"));
        }
        assertEquals(3, idIndex);

        // both accounts
        items = contentIndexClient.getItemWithValue(value1, null, null, null);
        assertEquals(5, items.size());
        items = contentIndexClient.getItemWithValue(value2, null, null, null);
        assertEquals(2, items.size());

        // account1
        items = contentIndexClient.getItemWithValue(value1, account1, null, null);
        assertEquals(2, items.size());
        items = contentIndexClient.getItemWithValue(value2, account1, null, null);
        assertEquals(0, items.size());

        // account2
        items = contentIndexClient.getItemWithValue(value1, account2, null, null);
        assertEquals(3, items.size());

    }
    
    @Test
    public void testSaveContentIndexItemWithMissingCheckSum()  {
        ContentIndexItem item5 = createContentIndexItem(account1, 5);
        item5.getProps().remove(CHECKSUM_KEY);

        trySave(item5);
    }

    protected void trySave(ContentIndexItem item5) {
        try {
            contentIndexClient.save(item5);
            Assert.fail("should have failed validation");
        } catch (ContentIndexClientValidationException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testSaveContentIndexItemWithMissingVersion()  {
        ContentIndexItem item5 = createContentIndexItem(account1, 5);
        item5.setVersion(null);
        trySave(item5);
    }

    @Test
    public void testSaveContentIndexItemWithMissingStoreId()  {
        ContentIndexItem item5 = createContentIndexItem(account1, 5);
        item5.setStoreId(null);
        trySave(item5);
    }

    @Test
    public void testSaveContentIndexItemWithMissingStoreType()  {
        ContentIndexItem item5 = createContentIndexItem(account1, 5);
        item5.setStoreType(null);
        trySave(item5);
    }

    @Test
    public void testSaveContentIndexItemWithMissingSpace()  {
        ContentIndexItem item5 = createContentIndexItem(account1, 5);
        item5.setSpace(null);
        trySave(item5);
    }

    @Test
    public void testSaveContentIndexItemWithMissingAcocunt()  {
        ContentIndexItem item5 = createContentIndexItem(account1, 5);
        item5.setAccount(null);
        trySave(item5);
    }

    @Test
    public void testVersionedDeletes() throws ContentIndexClientValidationException {
        ContentIndexItem item = createContentIndexItem(account1, 1000);
        long version = item.getVersion();
        long oldVersion = version - 1;
        long newVersion = version + 1;
        
        String account = item.getAccount();
        String storeId = item.getStoreId();
        String space = item.getSpace();
        String content = item.getContentId();

        contentIndexClient.save(item);

        //delete older and confirm that newer was not deleted.
        item = contentIndexClient.get(account, storeId, space, content);
        Assert.assertEquals(version, item.getVersion().longValue());
        item.setVersion(oldVersion);
        contentIndexClient.delete(item);
        item = contentIndexClient.get(account, storeId, space, content);
        
        assertNotNull(item);
        assertEquals(version, item.getVersion().longValue());

        //delete newer version
        item.setVersion(newVersion);
        
        contentIndexClient.delete(item);
        
        item = contentIndexClient.get(account, storeId, space, content);
        assertNull(item);
        
        
    }

    
    protected void assertItemFieldsNotNull(ContentIndexItem item) {
        assertNotNull(item.getId());
        assertNotNull(item.getAccount());
        assertNotNull(item.getStoreId());
        assertNotNull(item.getSpace());
        assertNotNull(item.getContentId());
        assertNotNull(item.getStoreType());
        assertNotNull(item.getProps().get(CHECKSUM_KEY));
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

        ContentIndexItem item =
            new ContentIndexItem(account, storeId, space, contentId);
        item.setStoreType(StorageProviderType.AMAZON_S3.getName());
        Map<String,String> props = new HashMap<>();
        props.put(CHECKSUM_KEY, checksum);
        item.setProps(props);
        item.setVersion(System.currentTimeMillis());
        return item;
    }
}
