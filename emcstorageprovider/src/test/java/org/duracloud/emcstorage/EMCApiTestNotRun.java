/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.emcstorage;

import com.emc.esu.api.*;
import com.emc.esu.api.Grantee.GRANT_TYPE;
import com.emc.esu.api.rest.EsuRestApi;
import org.duracloud.common.model.Credential;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.unittestdb.UnitTestDatabaseUtil;
import org.duracloud.unittestdb.domain.ResourceType;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class EMCApiTestNotRun {

    private String username;

    private String token;

    private String subtenant;

    private final String PERMISSION_FULL = "FULL_CONTROL";
    private final String PERMISSION_NONE = "NONE";
    private final String PERMISSION_CUSTOM = "CUSTOM";

    private EsuApi emcApi;

    final String ESU_HOST = "accesspoint.emccis.com";

    final int ESU_PORT = 80;

    @Before
    public void setUp() throws Exception {
        Credential emcCredential = getCredential();
        Assert.assertNotNull(emcCredential);

        username = emcCredential.getUsername();
        String password = emcCredential.getPassword();
        Assert.assertNotNull(username);
        Assert.assertNotNull(password);

        subtenant = username.substring(0, username.indexOf('/'));
        token = username.substring(username.indexOf('/') + 1);

        emcApi = new EsuRestApi(ESU_HOST, ESU_PORT, username, password);
    }

    @After
    public void tearDown() throws Exception {
        emcApi = null;
    }

    private Credential getCredential() throws Exception {
        UnitTestDatabaseUtil dbUtil = new UnitTestDatabaseUtil();
        return dbUtil.findCredentialForResource(ResourceType.fromStorageProviderType(
                                                StorageProviderType.EMC));
    }

    @Test
    public void testCreateObject() {
        doCreateObjectWithArgs(null, null, null, null);
    }

    @Test
    public void testCreateObjectAcl0() {
        Acl acl0 = new Acl();
        doCreateObject(acl0);
    }

    @Test
    public void testCreateObjectAcl1() {
        // Seems like this one should work... ?
        Acl acl2 = new Acl();
        acl2.addGrant(this.createUserGrant(token));
        doCreateObject(acl2);
    }

    @Test
    public void testCreateObjectAcl2() {
        Acl acl2 = new Acl();
        acl2.addGrant(this.createUserGrant(subtenant));
        doCreateObject(acl2);
    }

    @Test
    public void testCreateObjectAcl3() {
        Acl acl2 = new Acl();
        acl2.addGrant(this.createUserGrant(username));
        doCreateObject(acl2);
    }

    @Test
    public void testCreateObjectAcl4() {
        // User is not recognized.
        Acl acl1 = new Acl();
        acl1.addGrant(this.createUserGrant("smith"));
        doCreateObject(acl1);
    }

    @Test
    public void testCreateObjectMetadataList() {
        MetadataList metadataList0 = new MetadataList();
        doCreateObject(metadataList0);

        MetadataList metadataList1 = new MetadataList();
        Metadata metadata1 = new Metadata("color", "green", true);
        metadataList1.addMetadata(metadata1);
        doCreateObject(metadataList1);

        MetadataList metadataList2 = new MetadataList();
        Metadata metadata2a = new Metadata("color", "green", true);
        Metadata metadata2b = new Metadata("color", "green", true);
        metadataList2.addMetadata(metadata2a);
        metadataList2.addMetadata(metadata2b);
        doCreateObject(metadataList2);
    }

    @Test
    public void testCreateObjectData() {
        byte[] data0 = "".getBytes();
        doCreateObject(data0);

        byte[] data1 = "~!@#$%^&*()_+".getBytes();
        doCreateObject(data1);
    }

    private void doCreateObject(Acl argAcl) {
        doCreateObjectWithArgs(argAcl, null, null, null);
    }

    private void doCreateObject(MetadataList argMetadataList) {
        doCreateObjectWithArgs(null, argMetadataList, null, null);
    }

    private void doCreateObject(byte[] argData) {
        doCreateObjectWithArgs(null, null, argData, null);
    }

    private void doCreateObjectWithArgs(Acl argAcl,
                                        MetadataList argMetadataList,
                                        byte[] argData,
                                        String argMimeType) {
        ObjectId id = emcApi.createObject(argAcl,
                                          argMetadataList,
                                          argData,
                                          argMimeType);
        assertNotNull(id);

        verifyAcl(argAcl, emcApi.getAcl(id));
        verifyMetadataList(argMetadataList, emcApi.getUserMetadata(id, null));
        verifyData(argData, emcApi.readObject(id, null, null));

        // cleanup.
        emcApi.deleteObject(id);
    }

    @Test
    public void testDeleteObjectById() {
        ObjectId id = emcApi.createObject(null, null, null, null);
        Assert.assertNotNull(id);

        emcApi.deleteObject(id);

        try {
            emcApi.getSystemMetadata(id, null);
            fail("Exception expected.");
        } catch (Exception e) {
        }
    }

    @Test
    public void testDeleteFileObjectByPath0() {
        doDeleteObjectByPath("/test.path");
    }

    @Test
    public void testDeleteFileObjectByPath1() {
        doDeleteObjectByPath("/test");
    }

    @Test
    public void testDeleteFileObjectByPath2() {
        doDeleteObjectByPath("/best");
    }

    @Test
    public void testDeleteFileObjectByPath3() {
        doDeleteObjectByPath("/file.txt");
    }

    @Test
    public void testDeleteFileObjectByPath4() {
        doDeleteObjectByPath("/path/file.txt");
    }

    @Test
    public void testDeleteFileObjectByPath5() {
        doDeleteObjectByPath("/path/file");
    }

    @Test
    public void testDeleteDirObjectByPath0() {
        doDeleteObjectByPath("/test.zero/");
    }

    @Test
    public void testDeleteDirObjectByPath1() {
        doDeleteObjectByPath("/test.another/");
    }

    @Test
    public void testDeleteDirObjectByPath2() {
        doDeleteObjectByPath("/test/");
    }

    @Test
    public void testDeleteDirObjectByPath3() {
        doDeleteObjectByPath("/testAnother/");
    }

    @Test
    public void testDeleteDirObjectByPath4() {
        doDeleteObjectByPath("/test.another/dir/");
    }

    @Test
    public void testDeleteDirObjectByPath5() {
        doDeleteObjectByPath("/test.another/dir-name/");
    }

    private void doDeleteObjectByPath(String argPath) {
        ObjectPath path = new ObjectPath(argPath);
        Identifier id = null;
        if (!exists(path)) {
            id = emcApi.createObjectOnPath(path, null, null, null, null);
            Assert.assertNotNull(id);
        }

        emcApi.deleteObject(path);

        assertFalse(exists(path));
    }

    private boolean exists(ObjectPath path) {
        boolean found = true;
        try {
            emcApi.getSystemMetadata(path, null);
        } catch (Exception e) {
            found = false;
        }
        return found;
    }

    @Test
    public void testObjectPath() {
        new ObjectPath("/");
        new ObjectPath("/path/");
        new ObjectPath("/path");

        new ObjectPath("/Path");
        new ObjectPath("/pa.th");
        new ObjectPath("/pa-th");

        new ObjectPath("/path/file");
        new ObjectPath("/paTh/File");
        new ObjectPath("/path/file/");

        try {
            new ObjectPath("path/");
            fail("Exception expected.");
        } catch (Exception e) {
        }

        try {
            new ObjectPath("path");
            fail("Exception expected.");
        } catch (Exception e) {
        }

        try {
            new ObjectPath("");
            fail("Exception expected.");
        } catch (Exception e) {
        }

        try {
            new ObjectPath(null);
            fail("Exception expected.");
        } catch (Exception e) {
        }
    }

    @Test
    public void testCreateObjectOnPath0() {
        doCreateObjectOnPath("/path/");
    }

    @Test
    public void testCreateObjectOnPath1() {
        doCreateObjectOnPath("/dir0/DIR1");
    }

    @Test
    public void testCreateObjectOnPath2() {
        doCreateObjectOnPath("/path");
    }

    @Test
    public void testCreateObjectOnPath3() {
        doCreateObjectOnPath("/PATH");
    }

    @Test
    public void testCreateObjectOnPath4() {
        String path = "/path";
        Acl acl = new Acl();
        byte[] data = "".getBytes();
        MetadataList metadataList = new MetadataList();
        String mimeType = "application/octet-stream";

        doCreateObjectOnPathWithArgs(path, acl, metadataList, data, mimeType);
    }

    @Test
    public void testCreateObjectOnPath5() {
        try {
            doCreateObjectOnPath("probed-emc-uid.duracloud-test-space.17339/");
            fail("Exception expected. Path should start with '/'");
        } catch (Exception e) {
            // do nothing
        }
    }

    @Test
    public void testCreateObjectOnPath6() {
        doCreateObjectOnPath("/probed-emc-uid.duracloud-test-space.17339/");
    }

    @Test
    public void testCreateObjectOnPath7() {
        try {
            doCreateObjectOnPath("/");
            fail("Exception expected.");
        } catch (Exception e) {
            // do nothing
        }
    }

    @Test
    public void testCreateObjectOnPathWithAcl0() {
        Acl acl = new Acl();
        doCreateObjectOnPathWithAcl(acl);
    }

    @Test
    public void testCreateObjectOnPathWithAcl1() {
        Acl acl = null;
        doCreateObjectOnPathWithAcl(acl);
    }

    @Test
    public void testCreateObjectOnPathWithAcl2() {
        Acl acl = new Acl();
        acl.addGrant(createUserGrant("woods"));
        doCreateObjectOnPathWithAcl(acl);
    }

    public void doCreateObjectOnPathWithAcl(Acl acl) {
        String path = "/newpath";
        byte[] data = "".getBytes();
        MetadataList metadataList = new MetadataList();
        String mimeType = "application/octet-stream";

        doCreateObjectOnPathWithArgs(path, acl, metadataList, data, mimeType);
    }

    private Grant createUserGrant(String name) {
        return new Grant(new Grantee(name, GRANT_TYPE.USER), PERMISSION_FULL);
    }

    private void doCreateObjectOnPath(String argPath) {
        doCreateObjectOnPathWithArgs(argPath, null, null, null, null);
    }

    private void doCreateObjectOnPathWithArgs(String argPath,
                                              Acl argAcl,
                                              MetadataList argMetadataList,
                                              byte[] argData,
                                              String argMimeType) {
        ObjectPath path = new ObjectPath(argPath);
        deleteIfExists(path);

        ObjectId id = emcApi.createObjectOnPath(path,
                                                argAcl,
                                                argMetadataList,
                                                argData,
                                                argMimeType);

        assertNotNull(id);

        verifyAcl(argAcl, emcApi.getAcl(id));
        verifyMetadataList(argMetadataList, emcApi.getUserMetadata(id, null));
        verifyData(argData, emcApi.readObject(id, null, null));

        // cleanup.
        emcApi.deleteObject(id);
    }

    private void deleteIfExists(ObjectPath path) {
        if (exists(path)) {
            this.doDeleteObjectByPath(path.toString());
        }
    }

    private void verifyAcl(Acl argAcl, Acl acl) {
        if (argAcl == null) {
            return;
        }

        assertNotNull(acl);
        List<Grant> argGrants = new ArrayList<Grant>();
        List<Grant> grants = new ArrayList<Grant>();

        final int NUM_DEFAULT_GRANTEES = 2; // other, token
        int defaultGranteesFound = 0;
        for (Grant g : argAcl) {
            argGrants.add(g);
            if (isDefaultGrantee(g)) {
                defaultGranteesFound++;
            }
        }

        for (Grant g : acl) {
            grants.add(g);
        }

        // There are two default Grantee's in the ACL.
        int expectedSize =
            argGrants.size() + NUM_DEFAULT_GRANTEES - defaultGranteesFound;
        assertEquals(expectedSize, grants.size());
        for (Grant g : argAcl) {
            assertTrue(grants.contains(g));
        }

    }

    private boolean isDefaultGrantee(Grant g) {
        String name = g.getGrantee().getName();
        return ("other".equals(name) || token.equals(name));
    }

    private void verifyMetadataList(MetadataList argMetadataList,
                                    MetadataList userMetadata) {
        if (argMetadataList == null) {
            return;
        }

        assertNotNull(userMetadata);
        List<Metadata> argMetadatas = new ArrayList<Metadata>();
        List<Metadata> metadatas = new ArrayList<Metadata>();

        for (Metadata m : argMetadataList) {
            argMetadatas.add(m);
        }

        for (Metadata m : userMetadata) {
            metadatas.add(m);
        }

        assertEquals(argMetadatas.size(), metadatas.size());
        for (Metadata argM : argMetadatas) {
            String name = argM.getName();
            String value = argM.getValue();

            boolean found = false;
            for (Metadata m : metadatas) {
                if (name.equals(m.getName()) && value.equals(m.getValue())) {
                    found = true;
                }
            }
            assertTrue(found);
        }
    }

    private void verifyData(byte[] argData, byte[] data) {
        if (argData == null) {
            return;
        }

        assertNotNull(data);
        assertEquals(new String(argData), new String(data));
    }

    @Test
    public void testListableTags0() {
        String userMdName = "color";
        Metadata metadata = new Metadata(userMdName, "green", true);
        assertTrue(metadata.isListable());

        MetadataList metadataList = new MetadataList();
        metadataList.addMetadata(metadata);

        ObjectId id = null;
        try {
            id = emcApi.createObject(null, metadataList, null, null);
        } catch (EsuException e) {
            fail(e.getMessage());
        }
        assertNotNull(id);

        boolean found = false;
        MetadataList userMetadata = emcApi.getUserMetadata(id, null);
        for (Metadata md : userMetadata) {
            String name = md.getName();
            if (userMdName.equals(name)) {
                found = true;
            }
        }
        assertTrue(found);

        MetadataTags tags = emcApi.listUserMetadataTags(id);
        assertNotNull(tags);

        MetadataTag listableTag = null;
        for (MetadataTag md : tags) {
            String name = md.getName();

            if (userMdName.equals(name)) {
                listableTag = md;
            }
        }
        assertNotNull(listableTag);

        List<Identifier> ids = null;
        try {
            ids = emcApi.listObjects(listableTag);
        } catch (Exception e) {
        }
        assertNotNull(ids);

        found = false;
        for (Identifier each : ids) {
            if (id.equals(each)) {
                found = true;
            }
        }
        assertTrue(found);

    }

    @Test
    public void testListableTags1() {
        final int NUM_VISIBLE_SPACES = 2;
        String mdNameSpace = "test:space";
        String mdNameContent = "test:content";

        // Clean any previously existing objects.
        deleteObjectsWithTags(mdNameSpace, mdNameContent);

        final boolean isListable = true;

        Metadata metadataSpaceA = new Metadata(mdNameSpace,
                                               "spaceId:0",
                                               isListable);
        Metadata metadataSpaceB = new Metadata(mdNameSpace,
                                               "spaceId:1",
                                               !isListable);
        Metadata metadataSpaceC = new Metadata(mdNameSpace,
                                               "spaceId:2",
                                               isListable);
        Metadata metadataContentA = new Metadata(mdNameContent,
                                                 "content:0",
                                                 !isListable);
        Metadata metadataContentB = new Metadata(mdNameContent,
                                                 "content:1",
                                                 !isListable);
        Metadata metadataContentC = new Metadata(mdNameContent,
                                                 "content:2",
                                                 !isListable);

        MetadataList metadataListA = new MetadataList();
        metadataListA.addMetadata(metadataSpaceA);
        metadataListA.addMetadata(metadataContentA);

        MetadataList metadataListB = new MetadataList();
        metadataListB.addMetadata(metadataSpaceB);
        metadataListB.addMetadata(metadataContentB);

        MetadataList metadataListC = new MetadataList();
        metadataListC.addMetadata(metadataSpaceC);
        metadataListC.addMetadata(metadataContentC);

        ObjectId idA = null;
        ObjectId idB = null;
        ObjectId idC = null;
        try {
            idA = emcApi.createObject(null, metadataListA, null, null);
            idB = emcApi.createObject(null, metadataListB, null, null);
            idC = emcApi.createObject(null, metadataListC, null, null);
        } catch (EsuException e) {
            fail(e.getMessage());
        }
        assertNotNull(idA);
        assertNotNull(idB);
        assertNotNull(idC);

        // List Spaces.
        List<Identifier> idsSpace = null;
        try {
            idsSpace = emcApi.listObjects(mdNameSpace);
        } catch (Exception e) {
        }
        assertNotNull(idsSpace);
        assertEquals(NUM_VISIBLE_SPACES, idsSpace.size());

        boolean foundA = false;
        boolean foundB = false;
        boolean foundC = false;
        for (Identifier each : idsSpace) {
            if (idA.equals(each)) {
                foundA = true;
            }
            if (idB.equals(each)) {
                foundB = true;
            }
            if (idC.equals(each)) {
                foundC = true;
            }
        }
        assertTrue(foundA);
        assertTrue(!foundB);
        assertTrue(foundC);

        // List Content.
        List<Identifier> idsContent = null;
        try {
            idsContent = emcApi.listObjects(mdNameContent);
        } catch (Exception e) {
        }
        assertEquals(null, idsContent);

        // Cleanup.
        deleteObjectsWithTags(mdNameSpace, mdNameContent);
    }

    private void deleteObjectsWithTags(String... tagNames) {
        for (String tagName : tagNames) {
            try {
                for (Identifier id : emcApi.listObjects(tagName)) {
                    emcApi.deleteObject(id);
                }
            } catch (Exception e) {
            }
        }
    }

    @Test
    public void testUpdateObjectWithMetadata() {
        String userMdName = "color";
        Metadata metadata = new Metadata(userMdName, "green", true);
        assertTrue(metadata.isListable());

        MetadataList metadataList = new MetadataList();
        metadataList.addMetadata(metadata);

        ObjectId id = emcApi.createObject(null, null, null, null);
        assertNotNull(id);

        try {
            emcApi.updateObject(id, null, metadataList, null, null, null);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        boolean found = false;
        MetadataList userMetadata = emcApi.getUserMetadata(id, null);
        for (Metadata md : userMetadata) {
            String name = md.getName();
            if (userMdName.equals(name)) {
                found = true;
            }
        }
        assertTrue(found);

    }

    @Test
    public void testUpdateObjectWithContent() {
        ObjectId id = emcApi.createObject(null, null, null, null);
        assertNotNull(id);

        byte[] data = "data".getBytes();
        emcApi.updateObject(id, null, null, null, data, null);

        byte[] out = emcApi.readObject(id, null, null);
        assertNotNull(out);
        assertEquals(new String(data), new String(out));

    }

    @Test
    public void testQueryObjects() {
        String xquery = "/path";
        List<Identifier> ids = emcApi.queryObjects(xquery);
        assertNotNull(ids);

        for (Identifier id : ids) {
            System.out.println("id: '" + id + "'");
        }
    }
}
