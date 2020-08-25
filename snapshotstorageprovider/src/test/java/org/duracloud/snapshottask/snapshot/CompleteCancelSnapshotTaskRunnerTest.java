/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expectLastCall;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.duracloud.common.constant.Constants;
import org.duracloud.common.model.AclType;
import org.duracloud.snapshot.SnapshotConstants;
import org.duracloud.snapshotstorage.SnapshotStorageProvider;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Daniel Bernstein
 * Date: 9/22/2015
 */
@RunWith(EasyMockRunner.class)
public class CompleteCancelSnapshotTaskRunnerTest extends EasyMockSupport {

    @Mock
    private StorageProvider snapshotProvider;

    @Mock
    private SnapshotStorageProvider unwrappedSnapshotProvider;

    private CompleteCancelSnapshotTaskRunner taskRunner;

    private String dcSnapshotUser = "snapshot-user";
    private String bridgeHost = "bridge-host";
    private String bridgePort = "bridge-port";
    private String bridgeUser = "bridge-user";
    private String bridgePass = "bridge-pass";

    @Before
    public void setup() {
    }

    protected void setupSubject() {
        taskRunner =
            new CompleteCancelSnapshotTaskRunner(snapshotProvider,
                                                 unwrappedSnapshotProvider,
                                                 dcSnapshotUser,
                                                 bridgeHost,
                                                 bridgePort,
                                                 bridgeUser,
                                                 bridgePass);

    }

    @After
    public void tearDown() throws IOException {
        verifyAll();
    }

    @Test
    public void testGetName() {
        replayAll();
        setupSubject();
        assertEquals(SnapshotConstants.COMPLETE_SNAPSHOT_CANCEL_TASK_NAME, taskRunner.getName());
    }

    @Test
    public void testPerform() {
        String spaceId = "space-id";
        String snapshotId = "snapshot-id";

        //setup delete space properties
        snapshotProvider.deleteContent(eq(spaceId),
                                       eq(Constants.SNAPSHOT_PROPS_FILENAME));
        expectLastCall().once();

        //setup remove snapshot space property
        Map<String, String> spaceProps = new HashMap<>();
        spaceProps.put(Constants.SNAPSHOT_ID_PROP, snapshotId);
        EasyMock.expect(snapshotProvider.getSpaceProperties(spaceId))
                .andReturn(spaceProps);

        Capture<Map<String, String>> propsCapture = Capture.newInstance(CaptureType.FIRST);
        unwrappedSnapshotProvider.setNewSpaceProperties(EasyMock.eq(spaceId),
                                                        EasyMock.capture(propsCapture));
        expectLastCall().once();

        //setup remove read only acl
        String aclUserName = "acl-" + dcSnapshotUser;
        AclType aclValue = AclType.READ;

        Map<String, AclType> spaceACLs = new HashMap<>();
        spaceACLs.put(aclUserName, aclValue);
        EasyMock.expect(snapshotProvider.getSpaceACLs(spaceId))
                .andReturn(spaceACLs);

        Capture<Map<String, AclType>> spaceACLsCapture =
            Capture.newInstance(CaptureType.FIRST);
        snapshotProvider.setSpaceACLs(EasyMock.eq(spaceId),
                                      EasyMock.capture(spaceACLsCapture));

        replayAll();
        setupSubject();
        taskRunner.performTask("{\"spaceId\":\"" + spaceId + "\"}");

        spaceProps = propsCapture.getValue();

        assertFalse(spaceProps.containsKey(Constants.SNAPSHOT_ID_PROP));

        spaceACLs = spaceACLsCapture.getValue();

        assertFalse(spaceProps.containsKey(aclUserName));

    }

}
