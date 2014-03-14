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

import org.duracloud.audit.AuditLogWriteFailedException;
import org.duracloud.storage.aop.ContentMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

/**
 * @author Daniel Bernstein Date: 3/11/2014
 */
public class DynamoDBAuditLogStoreImplT {

    private DynamoDBAuditLogStore logStore;

    private AmazonDynamoDBClient client;

    @Before
    public void setUp() throws Exception {
        String username = "test";
        String password = username;

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
                          int content) throws AuditLogWriteFailedException {

        for (int i = 0; i < accounts; i++) {
            String account = "account"+i;
                       
            for (int j = 0; j < stores; j++) {
                String storeId =  j + "";

                for (int k = 0; k < spaces; k++) {
                    String spaceId =  "spaceid"+k;
                    for (int l = 0; l < content; l++) {
                        Date timestamp = new Date();
                        String contentId = "content"+l;
                        AuditLogItem item =
                            new AuditLogItem(KeyUtil.calculateAuditLogHashKey(storeId, spaceId, contentId, timestamp + ""),
                                             KeyUtil.calculateAccountSpaceIdHash(account, spaceId),
                                             account,
                                             storeId,
                                             spaceId,
                                             contentId,
                                             contentId + "md5",
                                             "dbernstein",
                                             ContentMessage.ACTION.INGEST.name(),
                                             timestamp);
                        logStore.write(item);

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
        loadData(client, 1,1,1,1);
    }

    @Test
    public void testGetWithNullStoreId() throws Exception {
        int stores = 2;
        int content = 5;
        loadData(client, 2,stores,3,content);
        
        Iterator<AuditLogItem> it = this.logStore.getLogItems("account0", "spaceid0", null);
        
        int count = 0;
        int expectedtotal = stores*content;
        
        while(it.hasNext()){
            it.next();
            count++;
        }

        
        Assert.assertEquals(expectedtotal, count);
    }

    @Test
    public void testGetWithStoreId() throws Exception {
        int stores = 2;
        int content = 5;
        loadData(client, 2,stores,3,content);
        
        Iterator<AuditLogItem> it = this.logStore.getLogItems("account0", "spaceid0", "0");
        
        int count = 0;
        int expectedtotal = content;
        
        while(it.hasNext()){
            it.next();
            count++;
        }

        
        Assert.assertEquals(expectedtotal, count);
    }

}
