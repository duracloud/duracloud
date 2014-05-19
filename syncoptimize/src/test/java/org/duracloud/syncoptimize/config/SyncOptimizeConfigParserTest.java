/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncoptimize.config;

import org.apache.commons.cli.ParseException;
import org.duracloud.common.util.ConsolePrompt;
import org.easymock.EasyMock;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

/**
 * @author Bill Branan
 *         Date: 5/16/14
 */
public class SyncOptimizeConfigParserTest {
    
    @Test
    public void testPasswordEnvVariable() throws Exception {
        SyncOptimizeConfigParser envRetConfigParser =
            new SyncOptimizeConfigParser() {
            protected String getPasswordEnvVariable() {
                return "envPassword";
            }
        };
        testStandardOptions(envRetConfigParser, "envPassword");
    }

	@Test
    public void testPasswordPrompt() throws Exception {
        SyncOptimizeConfigParser promptRetConfigParser =
            new SyncOptimizeConfigParser() {
            protected ConsolePrompt getConsole() {
                ConsolePrompt console = EasyMock.createMock(ConsolePrompt.class);
                char[] charPass = {'P','a','s','s','w','o','r','d'};
                EasyMock.expect(console.readPassword("DuraCloud password: "))
                        .andReturn(charPass);
                EasyMock.replay(console);
                return console;
            }
        };
        testStandardOptions(promptRetConfigParser, "Password");
    }

    public void testStandardOptions(SyncOptimizeConfigParser retConfigParser,
                                    String expectedPassword) throws Exception {
    	HashMap<String, String> argsMap = getArgsMap();

        // Process configs, make sure values match
        SyncOptimizeConfig syncOptConfig =
            retConfigParser.processOptions(mapToArray(argsMap));
        checkStandardOptions(argsMap, syncOptConfig);

        // Remove optional params
        argsMap.remove("-r");
        argsMap.remove("-n");
        argsMap.remove("-m");

        // Process configs, make sure optional params are set to defaults
        syncOptConfig = retConfigParser.processOptions(mapToArray(argsMap));
        assertEquals(SyncOptimizeConfigParser.DEFAULT_PORT,
                     syncOptConfig.getPort());
        assertEquals(SyncOptimizeConfigParser.DEFAULT_NUM_FILES,
                     syncOptConfig.getNumFiles());
        assertEquals(SyncOptimizeConfigParser.DEFAULT_SIZE_FILES,
                     syncOptConfig.getSizeFiles());
        assertEquals(expectedPassword, syncOptConfig.getPassword());

        // Make sure error is thrown on missing required params
        for(String arg : argsMap.keySet()) {
            String failMsg = "An exception should have been thrown due to " +
                             "missing arg: " + arg;
            removeArgFailTest(retConfigParser, argsMap, arg, failMsg);
        }

        // Make sure error is thrown when numerical args are not numerical
        String failMsg = "Port arg should require a numerical value";
        addArgFailTest(retConfigParser, argsMap, "-r", "nonNum", failMsg);
    }

    private HashMap<String, String> getArgsMap() {
        HashMap<String, String> argsMap = new HashMap<>();
        argsMap.put("-h", "localhost");
        argsMap.put("-r", "8088");
        argsMap.put("-u", "user");
        argsMap.put("-s", "space1");
        argsMap.put("-n", "500");
        argsMap.put("-m", "1000");
        return argsMap;
    }

    private void checkStandardOptions(HashMap<String, String> argsMap,
                                      SyncOptimizeConfig retConfig) {
        assertEquals(argsMap.get("-h"), retConfig.getHost());
        assertEquals(argsMap.get("-r"), String.valueOf(retConfig.getPort()));
        assertEquals(argsMap.get("-u"), retConfig.getUsername());
        assertEquals(argsMap.get("-s"), retConfig.getSpaceId());
    }

    private String[] mapToArray(HashMap<String, String> map) {
        ArrayList<String> list = new ArrayList<>();
        for(String key : map.keySet()) {
            list.add(key);
            list.add(map.get(key));
        }
        return list.toArray(new String[list.size()]);
    }

    private void addArgFailTest(SyncOptimizeConfigParser retConfigParser,
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

    private void removeArgFailTest(SyncOptimizeConfigParser retConfigParser,
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