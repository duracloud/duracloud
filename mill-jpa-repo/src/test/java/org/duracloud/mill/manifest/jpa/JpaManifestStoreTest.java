/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.mill.manifest.jpa;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.Iterator;

import org.duracloud.mill.db.model.ManifestItem;
import org.duracloud.mill.db.repo.JpaManifestItemRepo;
import org.duracloud.mill.manifest.ManifestItemWriteException;
import org.duracloud.mill.test.jpa.JpaTestBase;
import org.easymock.Capture;
import org.easymock.Mock;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author Daniel Bernstein Date: Sep 2, 2014
 */
public class JpaManifestStoreTest extends JpaTestBase<ManifestItem> {

    @Mock
    private JpaManifestItemRepo repo;

    private JpaManifestStore store;
    private String account = "account";
    private String storeId = "store-id";
    private String spaceId = "space-id";
    private String contentId = "content-id";
    private String contentChecksum = "content-checksum";
    private String contentSize = "content-size";
    private String contentMimetype = "content-mimetype";

    /**
     * Test method for
     * {@link org.duracloud.mill.manifest.jpa.JpaManifestStore#add(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}
     * .
     * 
     * @throws ManifestItemWriteException
     */
    @Test
    public void testAdd() throws ManifestItemWriteException {
        createTestSubject();
        Capture<ManifestItem> capture = new Capture<>();
        expect(this.repo.findByAccountAndStoreIdAndSpaceIdAndContentId(account,
                                                                       storeId,
                                                                       spaceId,
                                                                       contentId)).andReturn(null);
        ManifestItem returnItem = createMock(ManifestItem.class);
        expect(repo.saveAndFlush(capture(capture))).andReturn(returnItem);
        replayAll();
        Date timestamp = new Date();
        store.addUpdate(account,
                        storeId,
                        spaceId,
                        contentId,
                        contentChecksum,
                        contentMimetype,
                        contentSize,
                        timestamp);
        ManifestItem item = capture.getValue();
        assertEquals(account, item.getAccount());
        assertEquals(storeId, item.getStoreId());
        assertEquals(spaceId, item.getSpaceId());
        assertEquals(contentId, item.getContentId());
        assertEquals(contentChecksum, item.getContentChecksum());
        assertEquals(timestamp, item.getModified());
        assertEquals(contentMimetype, item.getContentMimetype());
        assertEquals(contentSize, item.getContentSize());

    }

    @Test
    public void testUpdate() throws ManifestItemWriteException {
        createTestSubject();
        Date timestamp = new Date();
        ManifestItem returnItem = createMock(ManifestItem.class);
        expect(returnItem.getModified()).andReturn(new Date(System.currentTimeMillis() - 1000));
        
        expect(returnItem.getContentChecksum()).andReturn("old checksum");
        returnItem.setContentChecksum(contentChecksum);
        expectLastCall();
        expect(returnItem.getContentMimetype()).andReturn("old mimetype");
        returnItem.setContentMimetype(contentMimetype);
        expectLastCall();

        expect(returnItem.getContentSize()).andReturn("old size");
        returnItem.setContentSize(contentSize);
        expectLastCall();

        returnItem.setModified(timestamp);
        expectLastCall();
        expect(returnItem.isDeleted()).andReturn(true);
        returnItem.setDeleted(false);
        expectLastCall();

        expect(this.repo.findByAccountAndStoreIdAndSpaceIdAndContentId(account,
                                                                       storeId,
                                                                       spaceId,
                                                                       contentId)).andReturn(returnItem);

        expect(repo.saveAndFlush(returnItem)).andReturn(returnItem);
        replayAll();
        store.addUpdate(account,
                        storeId,
                        spaceId,
                        contentId,
                        contentChecksum,
                        contentMimetype,
                        contentSize,
                        timestamp);
    }
    
    @Test
    public void testUpdateIgnoredDueToUnchangedItem() throws ManifestItemWriteException {
        createTestSubject();
        Date timestamp = new Date();
        ManifestItem returnItem = createMock(ManifestItem.class);
        expect(returnItem.getModified()).andReturn(new Date(System.currentTimeMillis() - 1000));
        
        expect(returnItem.getContentChecksum()).andReturn(contentChecksum);
        expect(returnItem.getContentMimetype()).andReturn(contentMimetype);
        expect(returnItem.getContentSize()).andReturn(contentSize);

        expect(returnItem.isDeleted()).andReturn(false);

        expect(this.repo.findByAccountAndStoreIdAndSpaceIdAndContentId(account,
                                                                       storeId,
                                                                       spaceId,
                                                                       contentId)).andReturn(returnItem);
        replayAll();
        store.addUpdate(account,
                        storeId,
                        spaceId,
                        contentId,
                        contentChecksum,
                        contentMimetype,
                        contentSize,
                        timestamp);
    }

    @Test
    public void testIgnoreUpdateDueToOutOfOrderMessage() throws ManifestItemWriteException {
        createTestSubject();
        Date timestamp = new Date();
        ManifestItem returnItem = createMock(ManifestItem.class);
        expect(returnItem.getModified()).andReturn(new Date(System.currentTimeMillis() + 1000));

        expect(this.repo.findByAccountAndStoreIdAndSpaceIdAndContentId(account,
                                                                       storeId,
                                                                       spaceId,
                                                                       contentId)).andReturn(returnItem);

        replayAll();
        store.addUpdate(account,
                        storeId,
                        spaceId,
                        contentId,
                        contentChecksum,
                        contentMimetype,
                        contentSize,
                        timestamp);
    }

    @Test
    public void testFlagAsDeleted() throws ManifestItemWriteException {
        createTestSubject();
        Date timestamp = new Date();
        ManifestItem returnItem = createMock(ManifestItem.class);
        expect(returnItem.isDeleted()).andReturn(false);
        expect(returnItem.getModified()).andReturn(new Date(System.currentTimeMillis() - 1000));
        returnItem.setModified(timestamp);
        expectLastCall();
        returnItem.setDeleted(true);
        expectLastCall();
        expect(this.repo.findByAccountAndStoreIdAndSpaceIdAndContentId(account,
                                                                       storeId,
                                                                       spaceId,
                                                                       contentId)).andReturn(returnItem);

        expect(repo.saveAndFlush(returnItem)).andReturn(returnItem);
        replayAll();
        store.flagAsDeleted(account, storeId, spaceId, contentId, timestamp);
    }

    @Test
    public void testFlagAsNotFound() throws ManifestItemWriteException {
        createTestSubject();
        Date timestamp = new Date();
        expect(this.repo.findByAccountAndStoreIdAndSpaceIdAndContentId(account,
                                                                       storeId,
                                                                       spaceId,
                                                                       contentId)).andReturn(null);
        Capture<ManifestItem>  itemCapture = new Capture<ManifestItem>();
        
        expect(repo.saveAndFlush(capture(itemCapture))).andReturn(new ManifestItem());
        
        replayAll();
        store.flagAsDeleted(account, storeId, spaceId, contentId, timestamp);
        
        ManifestItem item = itemCapture.getValue();
        
        assertNotNull(item.getContentChecksum());
        assertNotNull(item.getContentMimetype());
        assertNotNull(item.getContentSize());
        assertEquals(account, item.getAccount());
        assertEquals(storeId, item.getStoreId());
        assertEquals(spaceId, item.getSpaceId());
        assertEquals(contentId, item.getContentId());
        assertEquals(timestamp, item.getModified());

    }

    private void createTestSubject() {
        store = new JpaManifestStore(repo);
    }

    
    @Test
    public void updateMissingFromStorageProviderFlag() throws ManifestItemWriteException {
        createTestSubject();
        ManifestItem returnItem = createMock(ManifestItem.class);
        returnItem.setMissingFromStorageProvider(true);
        expectLastCall();
        expect(this.repo.findByAccountAndStoreIdAndSpaceIdAndContentId(account,
                                                                       storeId,
                                                                       spaceId,
                                                                       contentId)).andReturn(returnItem);
        expect(repo.saveAndFlush(returnItem)).andReturn(returnItem);
        replayAll();
        store.updateMissingFromStorageProviderFlag(account,
                                                   storeId,
                                                   spaceId,
                                                   contentId,
                                                   true);
    }

    @Test
    public void updateMissingFromStorageProviderFlagNotFound()  {
        createTestSubject();
        
        expect(this.repo.findByAccountAndStoreIdAndSpaceIdAndContentId(account,
                                                                       storeId,
                                                                       spaceId,
                                                                       contentId)).andReturn(null);

        replayAll();
        try {
            store.updateMissingFromStorageProviderFlag(account,
                                                       storeId,
                                                       spaceId,
                                                       contentId,
                                                       true);
            Assert.fail("expected failure");
        } catch (ManifestItemWriteException e) {
        }
    }

    /**
     * Test method for
     * {@link org.duracloud.mill.manifest.jpa.JpaManifestStore#getItems(java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testGetItems() {
        createTestSubject();

        Capture<Pageable> capture = new Capture<>();
        int count = 10;

        Page<ManifestItem> page = setupPage(count);
        expect(this.repo.findByAccountAndStoreIdAndSpaceIdAndDeletedFalseOrderByContentIdAsc(eq(account),
                                                                                   eq(storeId),
                                                                                   eq(spaceId),
                                                                                   capture(capture))).andReturn(page);
        replayAll();

        Iterator<ManifestItem> it = this.store.getItems(account,storeId, spaceId);
        verifyIterator(count, it);
        verifyPageable(capture);
    }

    /**
     * Test method for
     * {@link org.duracloud.mill.manifest.jpa.JpaManifestStore#getItem(java.lang.String, java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testGetItem() throws Exception {
        createTestSubject();
        ManifestItem returnItem = createMock(ManifestItem.class);
        expect(repo.findByAccountAndStoreIdAndSpaceIdAndContentId(account,
                                                                  storeId,
                                                                  spaceId,
                                                                  contentId)).andReturn(returnItem);
        replayAll();
        assertNotNull(store.getItem(account, storeId, spaceId, contentId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.duracloud.mill.test.jpa.JpaTestBase#create()
     */
    @Override
    protected ManifestItem create() {
        return createMock(ManifestItem.class);
    }

}
