/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and service online at
 *
 *     http://duracloud.org/license/
 */

import org.duracloud.client.exec.ExecutorImpl;
import org.duracloud.common.model.Credential;

import java.util.Map;
import java.util.Set;

/**
 * Example code which connects to the DuraCloud DuraBoss executor REST API
 * by using the ExecutorClient.
 *
 * @author Bill Branan
 * Date: 4/4/12
 */
public class ExampleExecutorClient {

    private static final String USERNAME = "user";  // replace as necessary
    private static final String PASSWORD = "upw";   // replace as necessary
    private static final String HOST = "localhost"; // replace as necessary
    private static final String PORT = "8080";      // replace as necessary
    private static final String CONTEXT = "duraboss";

    private ExecutorImpl executor;

    public ExampleExecutorClient() {
        executor = new ExecutorImpl(HOST, PORT, CONTEXT);
        executor.login(new Credential(USERNAME, PASSWORD));
    }

    public void runExample() throws Exception {
        Set<String> actions = executor.getSupportedActions();

        System.out.println("Supported Executor Actions:");
        for(String action : actions) {
            System.out.println("  " + action);
        }

        Map<String, String> status = executor.getStatus();

        System.out.println("\n\nCurrent Executor Status");
        for(String statusKey : status.keySet()) {
            System.out.println("  " + statusKey + ": " + status.get(statusKey));
        }
    }

    /**
     * This is the main method that runs the example client.
     *
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        ExampleExecutorClient execClient = new ExampleExecutorClient();

        try {
            execClient.runExample();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}