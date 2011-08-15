/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.provider;

import org.duracloud.storage.error.NotFoundException;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StorageProviderBaseTest {
    private StorageProviderTest provider;
    private StorageProviderBase providerTest;
    private String spaceId = "testspace";

    @Before
    public void setUp() throws Exception {
        providerTest = EasyMock.createMock("StorageProviderBase",
                                           StorageProviderBase.class);
        provider = new StorageProviderTest();
        provider.setTest(providerTest);
    }

    @After
    public void tearDown() {
        EasyMock.verify(providerTest);

        provider = null;
        providerTest = null;
    }

    @Test
    public void testDeleteSpace() {
        providerTest.throwIfSpaceNotExist(spaceId);
        EasyMock.expectLastCall();

        EasyMock.expect(providerTest.getSpaceProperties(spaceId))
            .andReturn(new HashMap<String, String>())
            .once();

        providerTest.setSpaceProperties(EasyMock.<String>anyObject(),
                                      EasyMock.<Map<String, String>>anyObject());
        EasyMock.expectLastCall();

        EasyMock.replay(providerTest);

        provider.deleteSpace(spaceId);
    }

    @Test
    public void testEmptyDeleteWorker(){
        EasyMock.expect(providerTest.getSpaceContents(EasyMock.eq(spaceId),
                                                      EasyMock.<String>isNull()))
            .andReturn(new ArrayList<String>().iterator())
            .once();

        providerTest.removeSpace(spaceId);
        EasyMock.expectLastCall();
        
        EasyMock.replay(providerTest);


        StorageProviderBase.SpaceDeleteWorker worker =
            provider.getSpaceDeleteWorker(spaceId);
        worker.run();
    }

    @Test
    public void testOnceDeleteWorker(){
        String contentId = "content-id";
        List<String> contents = new ArrayList<String>();
        contents.add(contentId);

        EasyMock.expect(providerTest.getSpaceContents(EasyMock.eq(spaceId),
                                                      EasyMock.<String>isNull()))
            .andReturn(contents.iterator())
            .once();

        providerTest.deleteContent(spaceId, contentId);
        EasyMock.expectLastCall();

        EasyMock.expect(providerTest.getSpaceContents(EasyMock.eq(spaceId),
                                                      EasyMock.<String>isNull()))
            .andReturn(new ArrayList<String>().iterator())
            .once();

        providerTest.removeSpace(spaceId);
        EasyMock.expectLastCall();

        EasyMock.replay(providerTest);


        StorageProviderBase.SpaceDeleteWorker worker =
            provider.getSpaceDeleteWorker(spaceId);
        worker.run();
    }

    @Test
    public void testOnceMultipleDeleteWorker(){
        String contentId = "content-id";
        List<String> contents = new ArrayList<String>();
        contents.add(contentId);
        contents.add(contentId);

        EasyMock.expect(providerTest.getSpaceContents(EasyMock.eq(spaceId),
                                                      EasyMock.<String>isNull()))
            .andReturn(contents.iterator())
            .once();

        providerTest.deleteContent(spaceId, contentId);
        EasyMock.expectLastCall().times(2);

        EasyMock.expect(providerTest.getSpaceContents(EasyMock.eq(spaceId),
                                                      EasyMock.<String>isNull()))
            .andReturn(new ArrayList<String>().iterator())
            .once();

        providerTest.removeSpace(spaceId);
        EasyMock.expectLastCall();

        EasyMock.replay(providerTest);


        StorageProviderBase.SpaceDeleteWorker worker =
            provider.getSpaceDeleteWorker(spaceId);
        worker.run();
    }

    @Test
    public void testRetriesDeleteWorker(){
        String contentId = "content-id";
        List<String> contents = new ArrayList<String>();
        contents.add(contentId);

        EasyMock.expect(providerTest.getSpaceContents(spaceId, null))
            .andReturn(contents.iterator());

        // 5 tries, 5 failures
        for (int i = 0; i < 5; ++i) {
            EasyMock.expect(providerTest.getSpaceContents(spaceId, null))
                .andReturn(contents.iterator());

            providerTest.deleteContent(spaceId, contentId);
            EasyMock.expectLastCall().andThrow(new NotFoundException(""));
        }

        EasyMock.expect(providerTest.getSpaceProperties(spaceId))
            .andReturn(new HashMap<String, String>());

        providerTest.setSpaceProperties(EasyMock.<String>anyObject(),
                                        EasyMock.<Map<String, String>>anyObject());
        EasyMock.expectLastCall();

        EasyMock.replay(providerTest);


        StorageProviderBase.SpaceDeleteWorker worker =
            provider.getSpaceDeleteWorker(spaceId);
        worker.run();
    }

    public class StorageProviderTest extends StorageProviderBase {
        private StorageProviderBase test;
        protected void removeSpace(String spaceId){
            test.removeSpace(spaceId);
        }
        public Iterator<String> getSpaces(){
            return test.getSpaces();
        }
        protected void throwIfSpaceNotExist(String spaceId){
            test.throwIfSpaceNotExist(spaceId);
        }
        public InputStream getContent(String spaceId,
                                      String contentId){
            return test.getContent(spaceId,contentId);
        }
        public Iterator<String> getSpaceContents(String spaceId,
                                                 String prefix){
            return test.getSpaceContents(spaceId,prefix);
        }
        public List<String> getSpaceContentsChunked(String spaceId,
                                                    String prefix,
                                                    long maxResults,
                                                    String marker){
            return test.getSpaceContentsChunked(spaceId,
                                                prefix,
                                                maxResults,
                                                marker);
        }
        public void createSpace(String spaceId){test.createSpace(spaceId);}
        public Map<String, String> getSpaceProperties(String spaceId){
            return test.getSpaceProperties(spaceId);
        }
        public void setSpaceProperties(String spaceId,
                                       Map<String, String> spaceProperties){
            test.setSpaceProperties(spaceId,spaceProperties);
        }
        public AccessType getSpaceAccess(String spaceId){
            return test.getSpaceAccess(spaceId);
        }
        public void setSpaceAccess(String spaceId,
                                   AccessType access){
            test.setSpaceAccess(spaceId,access);
        }
        public String addContent(String spaceId,
                                 String contentId,
                                 String contentMimeType,
                                 long contentSize,
                                 String contentChecksum,
                                 InputStream content){
            return test.addContent(spaceId,
                                   contentId,
                                   contentMimeType,
                                   contentSize,
                                   contentChecksum,
                                   content);
        }

        @Override
        public String copyContent(String sourceSpaceId,
                                  String sourceContentId,
                                  String destSpaceId,
                                  String destContentId) {
            return test.copyContent(sourceSpaceId,
                                    sourceContentId,
                                    destSpaceId,
                                    destContentId);
        }

        public void deleteContent(String spaceId,
                                  String contentId){
            test.deleteContent(spaceId,contentId);
        }
        public void setContentProperties(String spaceId,
                                         String contentId,
                                         Map<String, String> contentProperties){
            test.setContentProperties(spaceId,contentId,contentProperties);
        }
        public Map<String, String> getContentProperties(String spaceId,
                                                        String contentId){
            return test.getContentProperties(spaceId,contentId);
        }

        public void setTest(StorageProviderBase test) {
            this.test = test;
        }
    }
}
