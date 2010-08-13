/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.base;

import org.junit.Test;

import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

/**
 * @author: Bill Branan
 * Date: Aug 11, 2010
 */
public class InitParamParserTest {

    @Test
    public void testParseInitParams() throws Exception {
        InitParamParser paramParser = new InitParamParser();
        try {
            paramParser.parseInitParams(null);
            fail("Job Runner should fail when no arguments are provided");
        } catch(Exception expected) {
            assertNotNull(expected);
        }

        String inputVal = "/input/path";
        String outputVal = "/output/path";

        String[] argsInput = {"-inputPath", inputVal};
        try {
            paramParser.parseInitParams(argsInput);
            fail("Job Runner should fail when output argument is missing");
        } catch(Exception expected) {
            assertNotNull(expected);
        }

        String[] argsOutput = {"-outputPath", outputVal};
        try {
            paramParser.parseInitParams(argsOutput);
            fail("Job Runner should fail when input arguments is missing");
        } catch(Exception expected) {
            assertNotNull(expected);
        }

        String[] args = {"-inputPath", inputVal, "-outputPath", outputVal};
        paramParser.parseInitParams(args);

        String[] argsShort = {"-i", inputVal, "-o", outputVal};
        paramParser.parseInitParams(argsShort);

        Map<String, String> params = paramParser.parseInitParams(args);
        assertNotNull(params);

        String inputPath = params.get("inputPath");
        assertNotNull(inputPath);
        assertEquals(inputPath, inputVal);

        String outputPath = params.get("outputPath");
        assertNotNull(outputPath);
        assertEquals(outputPath, outputVal);
    }

}
