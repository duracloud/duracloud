/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit.dynamodb;

import java.util.Date;
import java.util.Iterator;

import junit.framework.Assert;

import org.duracloud.audit.AuditLogItem;
import org.duracloud.audit.AuditLogWriteFailedException;
import org.duracloud.error.NotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

/**
 * This "integration" test has been included in the auditor module 
 * rather than in the integration-test module because it is more a 
 * unit test than an integration test, but nevertheless because it depends
 * on the dynamodb local maven plugin to be setup and torn down in the 
 * pre/post-integration-test phase it must be run in the integration-test phase.
 * In order to distinguish this kind of intra-module integration test, we use the
 * "IT" suffix on the test class which is recognized by the maven-failsafe plugin.
 * 
 * @author Daniel Bernstein 
 *         Date: 3/11/2014
 */
public class DynamoDBAuditLogStoreImplIT {

    private DynamoDBAuditLogStore logStore;

    private AmazonDynamoDBClient client;

    @Before
    public void setUp() throws Exception {
        String username = "test";
        String password = "password";

        client =
            new AmazonDynamoDBClient(new BasicAWSCredentials(username, password));
        client.setRegion(Region.getRegion(Regions.DEFAULT_REGION));
        client.setEndpoint("http://localhost:"
            + System.getProperty("dynamodblocal.port", "8000"));
        DatabaseUtil.create(client);

        logStore = new DynamoDBAuditLogStore();
        logStore.initialize(client);

    }

    private void loadData(AmazonDynamoDBClient client,
                          int accounts,
                          int stores,
                          int spaces,
                          int content,
                          int dates) throws AuditLogWriteFailedException {

        for (int i = 0; i < accounts; i++) {
            String account = "account"+i;
                       
            for (int j = 0; j < stores; j++) {
                String storeId =  "store"+j;

                for (int k = 0; k < spaces; k++) {
                    String spaceId =  "space"+k;
                    for (int l = 0; l < content; l++) {
                        for(int m = 0; m < dates; m++){
                            //add dates one day apart moving into the past.
                            Date timestamp =
                                new Date(System.currentTimeMillis()
                                    - (24 * 60 * 60 * 1000 * m));
                            String contentId = "content"+l;
                            logStore.write(account,
                                           storeId,
                                           spaceId,
                                           contentId,
                                           contentId + "MD5",
                                           "application/pdf",
                                           "2000",
                                           "user",
                                           "action",
                                           "props",
                                           "acls",
                                           "sourcespace",
                                           "sourcecontent",
                                           timestamp);
                        }
                    }
                }
            }
        }
    }

    @After
    public void tearDown() throws Exception {
        DatabaseUtil.drop(client);
    }

    @Test
    public void testWrite() throws Exception {
        loadData(client, 1,1,1,1,1);
    }

    @Test
    public void testGetLogItemsForContentId() throws Exception {
        int stores = 2;
        int content = 5;
        int dates = 2;
        loadData(client, 2,stores,3,content,dates);
        
        Iterator<AuditLogItem> it = this.logStore.getLogItems("account0", "store0","space0","content0");
        
        int count = 0;
        int expectedtotal = dates;
        
        while(it.hasNext()){
            verifyItem(it.next());
            count++;
        }

        Assert.assertEquals(expectedtotal, count);
    }
    
    @Test
    public void testGetLogItemForContentId() throws Exception {
        int stores = 2;
        int content = 5;
        int dates = 2;
        loadData(client, 2,stores,3,content,dates);
        AuditLogItem it = this.logStore.getLatestLogItem("account0", "store0","space0","content0");
        verifyItem(it);
    }

    @Test
    public void testGetLogItemForContentIdNotFound() throws Exception{
        int stores = 2;
        int content = 5;
        int dates = 2;
        loadData(client, 2,stores,3,content,dates);
        try {
            this.logStore.getLatestLogItem("account0", "store0","space0","contentxxx");
            Assert.fail();
        }catch(NotFoundException ex){
            Assert.assertTrue(true);
            
        }
        
    }

    @Test
    public void testGetAllSpaceItems() throws Exception {
        int stores = 2;
        int content = 5;
        int dates = 2;
        loadData(client, 2,stores,3,content,dates);
        
        Iterator<AuditLogItem> it = this.logStore.getLogItems("account0","space0");
        
        int count = 0;
        int expectedtotal = stores*content*dates;
        
        while(it.hasNext()){
            verifyItem(it.next());
            count++;
        }

        Assert.assertEquals(expectedtotal, count);
    }

    private void verifyItem(AuditLogItem item) {
        Assert.assertNotNull(item.getAccount());
        Assert.assertNotNull(item.getStoreId());
        Assert.assertNotNull(item.getSpaceId());
        Assert.assertNotNull(item.getContentId());
        Assert.assertNotNull(item.getContentMd5());
        Assert.assertNotNull(item.getMimetype());
        Assert.assertNotNull(item.getContentSize());
        
    }

}
