/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.vote;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Test;
import org.springframework.security.core.Authentication;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class SpaceAccessVoterTest {
    @Test
    public void testExtractSpaceIdFromPathInof(){
        String spaceId = "space-id";
        String[] spaceIds = { "/manifest/" + spaceId,
                              "/bit-integrity/" + spaceId,
                              "/report/space/" + spaceId,
                              "/" + spaceId + "/arbitrary-path/content-id"
                            };
        SpaceAccessVoter voter = new SpaceAccessVoter(null,null) {
            
            @Override
            public int vote(Authentication authentication,
                            Object object,
                            Collection attributes) {
                return 0;
            }
        };
        
        for(String spid : spaceIds){
            assertEquals(spaceId,voter.extractSpaceId(spid));
        }
    }
}
