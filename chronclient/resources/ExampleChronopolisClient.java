/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and service online at
 *
 *     http://duracloud.org/license/
 */

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.FileUtils;
import org.duracloud.client.chron.ChronopolisClient;
import org.duracloud.common.model.Credential;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import static org.duracloud.client.chron.ResponseKey.HTTP_STATUS;
import static org.duracloud.client.chron.ResponseKey.IDENTIFIER;
import static org.duracloud.client.chron.ResponseKey.RETRY_AFTER;

/**
 * Example code which connects to the Chronopolis REST server.
 *
 * @author Andrew Woods
 *         Date: 4/4/12
 */
public class ExampleChronopolisClient {

    private static final String USERNAME = "user";     // replace as necessary
    private static final String PASSWORD = "upw";      // replace as necessary
    private static final String ACCT_ID = "sdsc-acct"; // replace as necessary
    private static final String HOST = "localhost";    // replace as necessary
    private static final String PORT = "8080";         // replace as necessary
    private static final String CONTEXT = "notification-server";

    private ChronopolisClient chronopolis;

    public ExampleChronopolisClient() {
        Credential user = new Credential(USERNAME, PASSWORD);
        chronopolis = new ChronopolisClient(ACCT_ID, HOST, PORT, CONTEXT, user);
    }

    public void runExample() throws Exception {
        String spaceId = "test";     // change me
        String contentId = "cc.png"; // change me

        // Local manifest file (in BagIt format) of space to backup is required.
        File manifestFile = new File("manifest-bagit.txt");
        InputStream manifest = FileUtils.openInputStream(manifestFile);

        // Request a DuraCloud space be stored in Chronopolis.
        Map<String, String> props = chronopolis.putContentSpace(spaceId,
                                                                manifest);

        System.out.println("Put Content Space response:");
        for (String key : props.keySet()) {
            System.out.println(" " + key + " = " + props.get(key));
        }

        // Spin until the space has been stored.
        long waitSeconds = Long.parseLong(props.get(RETRY_AFTER.toString()));
        String identifier = props.get(IDENTIFIER.toString());

        boolean done = false;
        while (!done) {
            Thread.sleep(waitSeconds * 1000);

            props = chronopolis.getProcessingStatus(identifier);
            System.out.println("Get Processing Status response:");
            for (String key : props.keySet()) {
                System.out.println(" " + key + " = " + props.get(key));
            }

            int status = Integer.parseInt(props.get(HTTP_STATUS.toString()));
            if (status == HttpStatus.SC_CREATED) {
                done = true;
            }
        }

        // Check the generated manifest to verify backup.
        // Compare the receipt to the original manifest.
        InputStream receipt = chronopolis.getReceiptManifest(identifier);

        // Restore a single item.
        props = chronopolis.getContentItem(spaceId, contentId);

        // Spin until the props contains HTTP_STATUS = SC_CREATED.
        // Note, props contains RETRY_AFTER which defines the polling frequency.

        // Restore the entire space.
        props = chronopolis.getContentSpace(spaceId);

        // Spin until the props contains HTTP_STATUS = SC_CREATED.
        // Note, props contains RETRY_AFTER which defines the polling frequency.
    }

    /**
     * This is the main method that runs the example client.
     */
    public static void main(String[] args) {
        ExampleChronopolisClient chronClient = new ExampleChronopolisClient();

        try {
            chronClient.runExample();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}