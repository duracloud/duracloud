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

}
