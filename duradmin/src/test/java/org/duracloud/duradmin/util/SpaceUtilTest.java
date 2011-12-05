/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.duradmin.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.duracloud.client.ContentStore;
import org.duracloud.common.model.AclType;
import org.duracloud.security.impl.DuracloudUserDetails;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
/**
 * 
 * @author Daniel Bernstein
 *         Date: 11/29/2011
 *
 */
public class SpaceUtilTest {
    private String spaceId = "testSpace";
    private String group = "group-testgroup";
    private String username = "user";
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

    @Test
    public void testAdmin() throws Exception{
        EasyMock.expect(authentication.getAuthorities())
                .andReturn(new GrantedAuthority[] { new GrantedAuthorityImpl("ROLE_ADMIN") });

        replay();
        
        AclType result = SpaceUtil.resolveCallerAcl(contentStore, spaceId, authentication);
        Assert.assertEquals(AclType.WRITE, result);
        
        
    }

    @Test
    public void testReadOnlyNonAdmin() throws Exception{
        
        EasyMock.expect(authentication.getAuthorities())
                .andReturn(new GrantedAuthority[0]);

        Map<String,AclType> acls = new HashMap<String,AclType>();
        acls.put(username, AclType.READ);
        EasyMock.expect(contentStore.getSpaceACLs(spaceId)).andReturn(acls);
        
        replay();
        
        AclType result = SpaceUtil.resolveCallerAcl(contentStore, spaceId, authentication);
        Assert.assertEquals(AclType.READ, result);
    }

    private void replay() {
        EasyMock.replay(contentStore, authentication, userDetails);
    }

    @Test
    public void testReadWriteNonAdmin() throws Exception{
        
        Map<String,AclType> acls = new HashMap<String,AclType>();
        acls.put(username, AclType.WRITE);
        EasyMock.expect(contentStore.getSpaceACLs(spaceId)).andReturn(acls);

        EasyMock.expect(authentication.getAuthorities())
                .andReturn(new GrantedAuthority[0]);

        replay();
        
        AclType result = SpaceUtil.resolveCallerAcl(contentStore, spaceId, authentication);
        Assert.assertEquals(AclType.WRITE, result);
    }

    @Test
    public void testReadWriteGroupNonAdmin() throws Exception{
        
        Map<String,AclType> acls = new HashMap<String,AclType>();
        acls.put(group, AclType.WRITE);
        EasyMock.expect(contentStore.getSpaceACLs(spaceId)).andReturn(acls);

        EasyMock.expect(authentication.getAuthorities())
                .andReturn(new GrantedAuthority[0]);

        replay();
        
        AclType result = SpaceUtil.resolveCallerAcl(contentStore, spaceId, authentication);
        Assert.assertEquals(AclType.WRITE, result);
    }

    
    @Test
    public void testUnauthorized() throws Exception{
        EasyMock.expect(authentication.getAuthorities())
                .andReturn(new GrantedAuthority[0]);
        
        EasyMock.expect(contentStore.getSpaceACLs(spaceId)).andReturn(new HashMap<String,AclType>());
        replay();
        AclType result = SpaceUtil.resolveCallerAcl(contentStore, spaceId, authentication);
        Assert.assertNull(result);
    }

}
