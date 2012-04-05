/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and service online at
 *
 *     http://duracloud.org/license/
 */

import org.duracloud.manifest.ManifestGenerator;
import org.duracloud.client.manifest.ManifestGeneratorImpl;
import org.duracloud.common.model.Credential;
import org.duracloud.common.util.IOUtil;

import java.io.InputStream;

/**
 * Example code which connects to the DuraCloud DuraBoss manifest REST API
 * by using the ManifestClient.
 *
 * @author Bill Branan
 * Date: 4/5/12
 */
public class ExampleManifestClient {

    private static final String USERNAME = "user";     // replace as necessary
    private static final String PASSWORD = "upw";      // replace as necessary
    private static final String HOST = "localhost";    // replace as necessary
    private static final String PORT = "8080";         // replace as necessary
    private static final String CONTEXT = "duraboss";

    // A value of null for STORE_ID indicates the primary store
    private static final String STORE_ID = null;       // replace as necessary
    private static final String SPACE_ID = "my-space"; // replace as necessary

    private ManifestGeneratorImpl generator;

    public ExampleManifestClient() {
        generator = new ManifestGeneratorImpl(HOST, PORT, CONTEXT);
        generator.login(new Credential(USERNAME, PASSWORD));
    }

    public void runExample() throws Exception {
        InputStream manifest =
            generator.getManifest(STORE_ID, SPACE_ID,
                                  ManifestGenerator.FORMAT.TSV, null);

        System.out.println(IOUtil.readStringFromStream(manifest));
    }

    /**
     * This is the main method that runs the example client.
     *
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        ExampleManifestClient genClient = new ExampleManifestClient();

        try {
            genClient.runExample();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}