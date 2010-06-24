/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.util;

import org.duracloud.storage.error.InvalidIdException;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Bill Branan
 * Date: Jan 12, 2010
 */
public class IdUtilTest {

    @Test
    public void testSpaceIDs() throws Exception {
        // Test invalid space IDs
        List<String> invalidIds = new ArrayList<String>();

        invalidIds.add("Test-Space");  // Uppercase
        invalidIds.add("test-space!"); // Special character
        invalidIds.add("test..space"); // Multiple periods
        invalidIds.add("-test-space"); // Starting with a dash
        invalidIds.add("test-space-"); // Ending with a dash
        invalidIds.add("test-.space"); // Dash next to a period
        invalidIds.add("te");          // Too short
        invalidIds.add("test-space-test-space-test-space-" +
                       "test-space-test-space-test-spac)"); // Too long
        invalidIds.add("127.0.0.1");   // Formatted as an IP address

        for(String id : invalidIds) {
            checkInvalidSpaceId(id);
        }

        // Test valid space names

        String id = "test-space.test.space";
        IdUtil.validateSpaceId(id);

        id = "tes";
        IdUtil.validateSpaceId(id);

        id = "test-space-test-space-test-space-test-space-test-space-test-spa";
        IdUtil.validateSpaceId(id);
    }

    private void checkInvalidSpaceId(String id) {
         try {
            IdUtil.validateSpaceId(id);
            fail("Exception expected attempting to validate space id: " + id);
        } catch(InvalidIdException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void testContentIds() throws Exception {
        // Test invalid content IDs

        // Question mark
        String id = "test?content";
        checkInvalidContentId(id);

        // Backslash
        id = "test\\content";
        checkInvalidContentId(id);        

        // Too long
        id = "test-content";
        while(id.getBytes().length <= 1024) {
            id += "test-content";
        }
        checkInvalidContentId(id);

        // Test valid content IDs

        // Special characters
        id = "test-content-~!@#$%^&*()_+=-`;':/.,<>\"[]{}| ";
        IdUtil.validateContentId(id);
    }

    private void checkInvalidContentId(String id) {
         try {
            IdUtil.validateContentId(id);
            fail("Exception expected attempting to validate content id: " + id);
        } catch(InvalidIdException expected) {
            assertNotNull(expected);
        }
    }

}
