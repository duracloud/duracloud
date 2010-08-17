/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.imageconversion;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * @author: Bill Branan
 * Date: Aug 16, 2010
 */
public class ImageConversionMapperTest {

    @Test
    public void testCreateScript() throws Exception {
        ImageConversionMapper mapper = new ImageConversionMapper();
        File workDir = new File("target/");

        // No colorspace
        File script = mapper.createScript(workDir, null);

        assertNotNull(script);
        assertTrue(script.exists());
        assertEquals(workDir, script.getParentFile());

        String scriptContents = FileUtils.readFileToString(script);
        assertTrue(scriptContents.contains("/bin/bash"));
        assertTrue(scriptContents.contains("sudo mogrify"));
        assertFalse(scriptContents.contains("sRGB.icm"));

        // With colorspace
        script = mapper.createScript(workDir, "sRGB");

        assertNotNull(script);
        assertTrue(script.exists());
        assertEquals(workDir, script.getParentFile());

        scriptContents = FileUtils.readFileToString(script);
        assertTrue(scriptContents.contains("/bin/bash"));
        assertTrue(scriptContents.contains("sudo mogrify"));
        assertTrue(scriptContents.contains("sRGB.icm"));        
    }

}
