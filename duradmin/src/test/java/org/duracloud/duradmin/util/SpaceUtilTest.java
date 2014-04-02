/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.duradmin.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.duracloud.client.ContentStore;
import org.duracloud.common.model.AclType;
import org.duracloud.duradmin.domain.Acl;
import org.duracloud.security.impl.DuracloudUserDetails;
import org.duracloud.storage.domain.StorageProviderType;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
/**
 * 
 * @author Daniel Bernstein
 *         Date: 11/29/2011
 *
 */
public class SpaceUtilTest {
    private String group = "group-testgroup";
    private String username = "user";
    private String spaceId = "spaceId";
    private ContentStore contentStore;
    private Authentication authentication;
    private DuracloudUserDetails userDetails;
    
    @Before
    public void setUp() throws Exception {
        contentStore = EasyMock.createMock(ContentStore.class);
        authentication = EasyMock.createMock(Authentication.class);
        userDetails = EasyMock.createMock(DuracloudUserDetails.class);
        userDetails = EasyMock.createMock(DuracloudUserDetails.class);
        EasyMock.expect(userDetails.getGroups()).andReturn(Arrays.asList(new String[]{group})).anyTimes();
        EasyMock.expect(userDetails.getUsername()).andReturn(username).anyTimes();

        EasyMock.expect(authentication.getPrincipal()).andReturn(userDetails).anyTimes();
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(contentStore);
        EasyMock.verify(authentication);
        EasyMock.verify(userDetails);
    }

    private void expectedAmazonS3StorageProviderType(){
        expectGetStorageProviderType(StorageProviderType.AMAZON_S3);
    }

    private void expectGetStorageProviderType(StorageProviderType spType){
        EasyMock.expect(contentStore.getStorageProviderType()).andReturn(spType.getName());
    }
    
    @Test
    public void testAdmin() throws Exception{
        expectAdminAuthority();
        expectedAmazonS3StorageProviderType();
        replay();
        
        AclType result = SpaceUtil.resolveCallerAcl(spaceId, contentStore, new HashMap<String,AclType>(), authentication);
        Assert.assertEquals(AclType.WRITE, result);
        
        
    }

    protected void expectAdminAuthority() {
        expectAuthority("ROLE_ADMIN", 2);
    }
    
    protected void expectAuthority(String role, int times){
        Collection auths = Arrays.asList(new GrantedAuthority[] { 
            new GrantedAuthorityImpl(role) });
        EasyMock.expect(authentication.getAuthorities())
        .andReturn(auths)
        .times(times);
    }

    @Test
    public void testReadOnlyNonAdmin() throws Exception{
        
        
        expectSubAdminAuthority();
        expectedAmazonS3StorageProviderType();

        Map<String,AclType> acls = new HashMap<String,AclType>();
        acls.put(username, AclType.READ);
        
        replay();
        
        AclType result = SpaceUtil.resolveCallerAcl(spaceId, contentStore, acls, authentication);
        Assert.assertEquals(AclType.READ, result);
    }

    protected void expectSubAdminAuthority() {
        Collection auths = new ArrayList<GrantedAuthority>();
        EasyMock.expect(authentication.getAuthorities())
                .andReturn(auths).times(2);
    }

    private void replay() {
        EasyMock.replay(contentStore, authentication, userDetails);
    }

    @Test
    public void testReadWriteNonAdmin() throws Exception{
        expectedAmazonS3StorageProviderType();
        
        Map<String,AclType> acls = new HashMap<String,AclType>();
        acls.put(username, AclType.WRITE);

        expectSubAdminAuthority();

        replay();
        
        AclType result = SpaceUtil.resolveCallerAcl(spaceId, contentStore, acls, authentication);
        Assert.assertEquals(AclType.WRITE, result);
    }

    @Test
    public void testReadWriteGroupNonAdmin() throws Exception{
        expectedAmazonS3StorageProviderType();
        
        Map<String,AclType> acls = new HashMap<String,AclType>();
        acls.put(group, AclType.WRITE);

        expectSubAdminAuthority();

        replay();
        
        AclType result = SpaceUtil.resolveCallerAcl(spaceId, contentStore, acls, authentication);
        Assert.assertEquals(AclType.WRITE, result);
    }

    @Test
    public void testChronopolisSnashotInProgress() throws Exception{
        expectGetStorageProviderType(StorageProviderType.CHRON_STAGE);
        EasyMock.expect(this.contentStore.getContentProperties(
                                               EasyMock.isA(String.class),
                                               EasyMock.isA(String.class)))
                                          .andReturn(
                                               new HashMap<String, String>());
        expectAuthority("ROLE_ADMIN", 1);
        

        replay();
        
        AclType result = SpaceUtil.resolveCallerAcl(spaceId, contentStore, null, authentication);
        
        Assert.assertEquals(AclType.READ, result);
    }

    
    
    @Test
    public void testUnauthorized() throws Exception{
        expectedAmazonS3StorageProviderType();

        expectSubAdminAuthority();
        
        replay();
        AclType result = SpaceUtil.resolveCallerAcl(spaceId, contentStore, new HashMap<String,AclType>(), authentication);
        Assert.assertNull(result);
    }
    
    @Test
    public void testAclSort() {
        replay(); //necessary since mocks are defined in setup and teardown.
        List<Acl> acls = new LinkedList<Acl>();
        acls.add(new Acl("user-z"));
        acls.add(new Acl("group-a"));
        acls.add(new Acl("user-a"));
        acls.add(new Acl("group-public"));
        acls.add(new Acl("group-z"));

        SpaceUtil.sortAcls(acls);
        
        assertPosition(acls, 0, "group-public");
        assertPosition(acls, 1, "group-a");
        assertPosition(acls, 2, "group-z");
        assertPosition(acls, 3, "user-a");
        assertPosition(acls, 4, "user-z");
        
    }

    private void assertPosition(List<Acl> acls, int index, String name) {
        Assert.assertEquals(name, acls.get(index).getName());
        
    }

}
