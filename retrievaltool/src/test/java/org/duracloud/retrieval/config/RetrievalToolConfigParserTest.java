/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.config;

import org.apache.commons.cli.ParseException;
import org.duracloud.common.util.ConsolePrompt;
import org.duracloud.retrieval.RetrievalTestBase;
import org.easymock.EasyMock;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;

/**
 * @author: Bill Branan
 * Date: Oct 13, 2010
 */
public class RetrievalToolConfigParserTest extends RetrievalTestBase {

    @Test
    public void testPasswordEnvVariable() throws Exception {
        RetrievalToolConfigParser envRetConfigParser = new RetrievalToolConfigParser() {
            protected String getPasswordEnvVariable() {
                return "envPassword";
            }
        };
        testStandardOptions(envRetConfigParser, "envPassword");
    }

	@Test
    public void testPasswordPrompt() throws Exception {
        RetrievalToolConfigParser promptRetConfigParser = new RetrievalToolConfigParser() {
            protected ConsolePrompt getConsole() {
                ConsolePrompt console = EasyMock.createMock(ConsolePrompt.class);
                char[] charPass = {'p','r','o','m','p','t','P','a','s','s','w','o','r','d'};
                EasyMock.expect(console.readPassword("DuraCloud password: ")).andReturn(charPass);
                EasyMock.replay(console);
                return console;
            }
        };
        testStandardOptions(promptRetConfigParser, "promptPassword");
    }

    public void testStandardOptions(RetrievalToolConfigParser retConfigParser, String expectedPassword) throws Exception {
    	HashMap<String, String> argsMap = getArgsMap();

        // Process configs, make sure values match
        RetrievalToolConfig retConfig =
            retConfigParser.processOptions(mapToArray(argsMap));
        checkStandardOptions(argsMap, retConfig);

        // Remove optional params
        argsMap.remove("-r");
        argsMap.remove("-i");
        argsMap.remove("-a");
        argsMap.remove("-o");
        argsMap.remove("-t");
        argsMap.remove("-d");
        argsMap.remove("-l");
        argsMap.remove("-w");

        // Process configs, make sure optional params are set to defaults
        retConfig = retConfigParser.processOptions(mapToArray(argsMap));
        assertEquals(RetrievalToolConfigParser.DEFAULT_PORT,
                     retConfig.getPort());
        assertEquals(RetrievalToolConfigParser.DEFAULT_NUM_THREADS,
                     retConfig.getNumThreads());
        assertEquals(false, retConfig.isAllSpaces());
        assertEquals(false, retConfig.isOverwrite());
        assertEquals(true, retConfig.isApplyTimestamps());
        assertEquals(false, retConfig.isListOnly());
        assertEquals(expectedPassword, retConfig.getPassword());
        assertNull(retConfig.getWorkDir());

        // Make sure error is thrown on missing required params
        for(String arg : argsMap.keySet()) {
            String failMsg = "An exception should have been thrown due to " +
                             "missing arg: " + arg;
            removeArgFailTest(retConfigParser, argsMap, arg, failMsg);
        }

        // Make sure error is thrown when numerical args are not numerical
        String failMsg = "Port arg should require a numerical value";
        addArgFailTest(retConfigParser, argsMap, "-r", "nonNum", failMsg);
        failMsg = "Threads arg should require a numerical value";
        addArgFailTest(retConfigParser, argsMap, "-t", "nonNum", failMsg);
    }

    private HashMap<String, String> getArgsMap() {
        HashMap<String, String> argsMap = new HashMap<String, String>();
        argsMap.put("-h", "localhost");
        argsMap.put("-r", "8088");
        argsMap.put("-u", "user");
        argsMap.put("-i", "0");
        argsMap.put("-s", "space1 space2 space3");
        argsMap.put("-a", "");
        argsMap.put("-c", tempDir.getAbsolutePath());
        argsMap.put("-w", tempDir.getAbsolutePath());
        argsMap.put("-o", "");
        argsMap.put("-t", "5");
        argsMap.put("-d", "");
        return argsMap;
    }

    private void checkStandardOptions(HashMap<String, String> argsMap,
                                      RetrievalToolConfig retConfig) {
        assertEquals(argsMap.get("-h"), retConfig.getHost());
        assertEquals(argsMap.get("-r"), String.valueOf(retConfig.getPort()));
        assertEquals(argsMap.get("-u"), retConfig.getUsername());
        assertEquals(argsMap.get("-i"), retConfig.getStoreId());

        String spaces = "";
        for(String space : retConfig.getSpaces()) {
            spaces += space + " ";
        }
        assertEquals(argsMap.get("-s"), spaces.trim());

        assertEquals(true, retConfig.isAllSpaces());
        assertEquals(argsMap.get("-c"),
                     retConfig.getContentDir().getAbsolutePath());
        assertEquals(argsMap.get("-w"),
                     retConfig.getWorkDir().getAbsolutePath());        
        assertEquals(true, retConfig.isOverwrite());
        assertEquals(argsMap.get("-t"),
                     String.valueOf(retConfig.getNumThreads()));
    }

    private String[] mapToArray(HashMap<String, String> map) {
        ArrayList<String> list = new ArrayList<String>();
        for(String key : map.keySet()) {
            list.add(key);
            list.add(map.get(key));
        }
        return list.toArray(new String[list.size()]);
    }

    private void addArgFailTest(RetrievalToolConfigParser retConfigParser,
                                HashMap<String, String> argsMap,
                                String arg,
                                String value,
                                String failMsg) {
        HashMap<String, String> cloneMap =
            (HashMap<String, String>)argsMap.clone();
        cloneMap.put(arg, value);
        try {
            retConfigParser.processOptions(mapToArray(cloneMap));
            fail(failMsg);
        } catch(ParseException e) {
            assertNotNull(e);
        }
    }

    private void removeArgFailTest(RetrievalToolConfigParser retConfigParser,
                                   HashMap<String, String> argsMap,
                                   String arg,
                                   String failMsg) {
        HashMap<String, String> cloneMap =
            (HashMap<String, String>)argsMap.clone();
        cloneMap.remove(arg);
        try {
            retConfigParser.processOptions(mapToArray(cloneMap));
            fail(failMsg);
        } catch(ParseException e) {
            assertNotNull(e);
        }
    }

}
