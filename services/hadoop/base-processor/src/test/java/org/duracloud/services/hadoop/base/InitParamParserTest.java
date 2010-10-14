/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.base;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.duracloud.storage.domain.HadoopTypes.*;


/**
 * @author: Bill Branan
 * Date: Aug 11, 2010
 */
public class InitParamParserTest {

    private InitParamParser paramParser;

    private static final String inputVal = "/input/path";
    private static final String outputVal = "/output/path";
    private static final String usernameVal = "username";
    private static final String passwordVal = "password";
    private static final String hostVal = "host";

    @Before
    public void setUp() {
        paramParser = new InitParamParser();
    }

    @Test
    public void testNull() throws Exception {
        try {
            paramParser.parseInitParams(null);
            Assert.fail("Job Runner should fail when no arguments are provided");
        } catch (Exception expected) {
            Assert.assertNotNull(expected);
        }
    }

    @Test
    public void testNoOutPath() throws Exception {

        String[] argsInput = {TASK_PARAMS.INPUT_PATH.getCliForm(),
                              inputVal};
        try {
            paramParser.parseInitParams(argsInput);
            Assert.fail("Job Runner should fail when output argument is missing");
        } catch (Exception expected) {
            Assert.assertNotNull(expected);
        }
    }

    @Test
    public void testNoInPath() throws Exception {
        String[] argsOutput = {TASK_PARAMS.OUTPUT_PATH.getCliForm(),
                               outputVal};
        try {
            paramParser.parseInitParams(argsOutput);
            Assert.fail("Job Runner should fail when input arguments is missing");
        } catch (Exception expected) {
            Assert.assertNotNull(expected);
        }
    }

    @Test
    public void testParseLongArgs() throws Exception {
        String[] args = {TASK_PARAMS.INPUT_PATH.getCliForm(),
                         inputVal,
                         TASK_PARAMS.OUTPUT_PATH.getCliForm(),
                         outputVal,
                         TASK_PARAMS.DC_HOST.getCliForm(),
                         hostVal,
                         TASK_PARAMS.DC_USERNAME.getCliForm(),
                         usernameVal,
                         TASK_PARAMS.DC_PASSWORD.getCliForm(),
                         passwordVal};
        Map<String, String> params = paramParser.parseInitParams(args);
        verifyParams(params);
    }

    @Test
    public void testParseShortArgs() throws Exception {
        String[] argsShort = {"-i",
                              inputVal,
                              "-o",
                              outputVal,
                              "-h",
                              hostVal,
                              "-u",
                              usernameVal,
                              "-p",
                              passwordVal};
        Map<String, String> params = paramParser.parseInitParams(argsShort);
        verifyParams(params);
    }

    private void verifyParams(Map<String, String> params) {
        Assert.assertNotNull(params);
        String inputPath = params.get(TASK_PARAMS.INPUT_PATH.getLongForm());
        Assert.assertNotNull(inputPath);
        Assert.assertEquals(inputPath, inputVal);

        String outputPath = params.get(TASK_PARAMS.OUTPUT_PATH.getLongForm());
        Assert.assertNotNull(outputPath);
        Assert.assertEquals(outputPath, outputVal);

        String host = params.get(TASK_PARAMS.DC_HOST.getLongForm());
        Assert.assertNotNull(host);
        Assert.assertEquals(host, hostVal);

        String username = params.get(TASK_PARAMS.DC_USERNAME.getLongForm());
        Assert.assertNotNull(username);
        Assert.assertEquals(username, usernameVal);

        String password = params.get(TASK_PARAMS.DC_PASSWORD.getLongForm());
        Assert.assertNotNull(password);
        Assert.assertEquals(password, passwordVal);
    }

}
