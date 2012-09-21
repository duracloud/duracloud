/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author: Bill Branan
 * Date: 7/26/12
 */
public class DuplicationServiceTest {

    Duplicator duper;
    DuplicationService dupService;

    private static final String SPACE_CREATE_TOPIC = "space-create-topic";
    private static final String SPACE_UPDATE_TOPIC = "space-update-topic";
    private static final String SPACE_UPDATE_ACL_TOPIC = "space-update-acl-topic";
    private static final String SPACE_DELETE_TOPIC = "space-delete-topic";
    private static final String CONTENT_CREATE_TOPIC = "content-create-topic";
    private static final String CONTENT_COPY_TOPIC = "content-copy-topic";
    private static final String CONTENT_UPDATE_TOPIC = "content-update-topic";
    private static final String CONTENT_DELETE_TOPIC = "content-delete-topic";

    private static final String spaceId = "space-id";
    private static final String contentId = "content-id";

    @Before
    public void setup() {
        duper = EasyMock.createMock("Duplicator", Duplicator.class);
        dupService = new DuplicationService();
        dupService.setSpaceCreateTopic(SPACE_CREATE_TOPIC);
        dupService.setSpaceUpdateTopic(SPACE_UPDATE_TOPIC);
        dupService.setSpaceUpdateAclTopic(SPACE_UPDATE_ACL_TOPIC);
        dupService.setSpaceDeleteTopic(SPACE_DELETE_TOPIC);
        dupService.setContentCreateTopic(CONTENT_CREATE_TOPIC);
        dupService.setContentCopyTopic(CONTENT_COPY_TOPIC);
        dupService.setContentUpdateTopic(CONTENT_UPDATE_TOPIC);
        dupService.setContentDeleteTopic(CONTENT_DELETE_TOPIC);
    }

    @After
    public void teardown() {
        EasyMock.verify(duper);
    }

    @Test
    public void testDupSpaceCreate() throws Exception {
        duper.createSpace(spaceId);
        EasyMock.expectLastCall();

        runTest(SPACE_CREATE_TOPIC);
    }

    @Test
    public void testDupSpaceUpdate() throws Exception {
        duper.updateSpace(spaceId);
        EasyMock.expectLastCall();

        runTest(SPACE_UPDATE_TOPIC);
    }

    @Test
    public void testDupSpaceUpdateAcl() throws Exception {
        duper.updateSpaceAcl(spaceId);
        EasyMock.expectLastCall();

        runTest(SPACE_UPDATE_ACL_TOPIC);
    }

    @Test
    public void testDupSpaceDelete() throws Exception {
        duper.deleteSpace(spaceId);
        EasyMock.expectLastCall();

        runTest(SPACE_DELETE_TOPIC);
    }

    @Test
    public void testDupContentCreate() throws Exception {
        EasyMock.expect(duper.createContent(spaceId, contentId))
                .andReturn("");

        runTest(CONTENT_CREATE_TOPIC);
    }

    @Test
    public void testDupContentCopy() throws Exception {
        EasyMock.expect(duper.createContent(spaceId, contentId))
                .andReturn("");

        runTest(CONTENT_COPY_TOPIC);
    }

    @Test
    public void testDupContentUpdate() throws Exception {
        duper.updateContent(spaceId, contentId);
        EasyMock.expectLastCall();

        runTest(CONTENT_UPDATE_TOPIC);
    }

    @Test
    public void testDupContentDelete() throws Exception {
        duper.deleteContent(spaceId, contentId);
        EasyMock.expectLastCall();

        runTest(CONTENT_DELETE_TOPIC);
    }

    private void runTest(String topic) {
        EasyMock.replay(duper);
        dupService.processDuplication(topic, spaceId, contentId, duper);
    }

    @Test
    public void testSpaceSettings() throws Exception {
        assertNull(dupService.spacesToWatch);

        String space1 = "space-1";
        String space2 = "my-favorite-space";
        String space1StoreList = "1,2,3";
        String space2StoreList = "1";

        Map<String, String> props = new HashMap<String, String>();
        props.put(dupService.SPACE_PREFIX + space1, space1StoreList);
        props.put(dupService.SPACE_PREFIX + space2, space2StoreList);
        dupService.updateConfig(props);

        assertEquals(2, dupService.spacesToWatch.keySet().size());
        assertTrue(dupService.spacesToWatch.containsKey(space1));
        assertTrue(dupService.spacesToWatch.containsKey(space2));
        assertEquals(space1StoreList, dupService.spacesToWatch.get(space1));
        assertEquals(space2StoreList, dupService.spacesToWatch.get(space2));

        EasyMock.replay(duper);
    }

    /*
     * Verifies that message duplication is carried out correctly based on
     * the space duplication configuration. The flow of this test:
     * - Configure to include only space1, to be duplicated to store1 and store2
     * - Configure a default for new spaces to be duplicated to store1
     * - Add content to space1, see that it is duplicated to 2 stores
     * - Add content to space2, see that it is ignored as expected
     * - Create space3, see that it is duplicated to store1, as expected based
     *     on the default setting
     * - Add content to space3, see that it is duplicated to store1, as
     *     expected based on the default setting
     */
    @Test
    public void testHandleDuplicationMessage() throws Exception {
        String store1 = "1";
        String store2 = "2";

        // Add space1 to the watch list with dup to stores 1 and 2
        // Include a default dup to store 1
        String space1 = "space1";
        String space1StoreList = store1 + "," + store2;
        Map<String, String> props = new HashMap<String, String>();
        props.put(dupService.SPACE_PREFIX + space1, space1StoreList);
        String defaultStoreList = store1;
        props.put(dupService.NEW_SPACE_DEFAULT, defaultStoreList);
        dupService.updateConfig(props);

        String space2 = "space2";
        String space3 = "space3";
        String contentId = "new-content.txt";

        // Set duplicator list to include store1 and store2
        Map<String, Duplicator> duplicators = new HashMap<String, Duplicator>();
        duplicators.put(store1, duper);
        duplicators.put(store2, duper);
        dupService.duplicators = duplicators;

        // Result of adding content to space1 - duplicated to 2 stores
        EasyMock.expect(duper.createContent(space1, contentId))
                .andReturn("")
                .times(2);

        // Result of adding new space, space 3 - duplicated to 1 store (default)
        duper.createSpace(space3);
        EasyMock.expectLastCall().times(1);

        // Result of adding content to space3 - duplicated to 1 store (default)
        EasyMock.expect(duper.createContent(space3, contentId))
                .andReturn("")
                .times(1);

        EasyMock.replay(duper);

        // Add content to space1 - should be duplicatd to 2 stores
        dupService.handleDuplicationMessage(
            dupService.getContentCreateTopic(), space1, contentId);

        // Add content to space2 - should be ignored
        dupService.handleDuplicationMessage(
            dupService.getContentCreateTopic(), space2, contentId);

        // Add space 3 - should be duplicated to 1 store
        dupService.handleDuplicationMessage(
            dupService.getSpaceCreateTopic(), space3, null);

        // Add content to space3 - should be duplicated to 1 store
        dupService.handleDuplicationMessage(
            dupService.getContentCreateTopic(), space3, contentId);
    }

}
