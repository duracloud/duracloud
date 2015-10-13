/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.model;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class RootUserCredentialTest {

    @Test
    public void testOverrideSystemPropertyKeys() {
        String userKey = "test.username";
        String passwordKey = "test.password";
        String emailKey = "test.email";

        String username = "buzzbuzz";
        String password = "killkill";
        String email = "email@email.com";

        RootUserCredential.overrideSystemPropertyKeys(userKey, passwordKey, emailKey);
            
        System.setProperty(userKey, username);
        System.setProperty(passwordKey, password);
        System.setProperty(emailKey, email);
        
        RootUserCredential cred = new RootUserCredential();
        
        assertEquals(username, cred.getUsername());
        assertEquals(password, cred.getPassword());
        assertEquals(email, RootUserCredential.getRootEmail());
    }

}
