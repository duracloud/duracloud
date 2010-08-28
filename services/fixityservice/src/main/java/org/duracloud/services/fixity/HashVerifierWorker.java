/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.client.StoreCaller;
import org.duracloud.common.error.DuraCloudCheckedException;
import org.duracloud.common.error.ManifestVerifyException;
import org.duracloud.common.util.bulk.ManifestVerifier;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.fixity.domain.ContentLocation;
import org.duracloud.services.fixity.domain.ContentLocationPair;
import org.duracloud.services.fixity.domain.FixityServiceOptions;
import org.duracloud.services.fixity.results.HashFinderResult;
import org.duracloud.services.fixity.results.HashVerifierResult;
import org.duracloud.services.fixity.results.ServiceResultListener;
import org.duracloud.services.fixity.util.FixityManifestVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * @author Andrew Woods
 *         Date: Aug 5, 2010
 */
public class HashVerifierWorker implements Runnable {

    private final Logger log = LoggerFactory.getLogger(HashVerifierWorker.class);

    private ContentStore contentStore;
    private ContentLocation workItemLocationA;
    private ContentLocation workItemLocationB;
    private File workDir;
    private ServiceResultListener resultListener;

    private FixityManifestVerifier verifier;

    public HashVerifierWorker(ContentStore contentStore,
                              ContentLocationPair workItemLocationPair,
                              File workDir,
                              ServiceResultListener resultListener) {
        this.contentStore = contentStore;
        this.workItemLocationA = workItemLocationPair.getContentLocationA();
        this.workItemLocationB = workItemLocationPair.getContentLocationB();
        this.workDir = workDir;
        this.resultListener = resultListener;
    }

    @Override
    public void run() {
        File fileA = null;
        File fileB = null;
        try {
            fileA = download(this.workItemLocationA);
            fileB = download(this.workItemLocationB);

            verifier = new FixityManifestVerifier(fileA, fileB);
            verifier.verify();

            sendResult(true, verifier.resultEntries());

        } catch (ManifestVerifyException mve) {
            sendResult(false, verifier.resultEntries());

        } catch (Exception e) {
            sendResult(false, e.getMessage());
        }
    }

    private void sendResult(boolean success, String text) {
        resultListener.processServiceResult(new HashVerifierResult(success,
                                                                   this.workItemLocationA,
                                                                   this.workItemLocationB,
                                                                   text));
    }

    private File download(ContentLocation location) throws Exception {
        Content content = getContent(location);
        if (null == content) {
            StringBuilder sb = new StringBuilder("Error downloading: ");
            sb.append(location.getSpaceId());
            sb.append("/");
            sb.append(location.getContentId());
            log.error(sb.toString());
            throw new DuraCloudCheckedException(sb.toString());
        }

        String outName = location.getSpaceId() + "-" + location.getContentId();
        File file = new File(workDir, outName);
        OutputStream output = null;
        InputStream input = null;
        try {
            output = new FileOutputStream(file);
            input = content.getStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(input));
            br.readLine(); // skip header line
            IOUtils.copy(br, output);

        } finally {
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(output);
        }

        return file;
    }

    /**
     * This method leverages the StoreCaller abstract class to loop on failed
     * contentStore calls.
     *
     * @return
     */
    private Content getContent(final ContentLocation loc) {
        StoreCaller<Content> caller = new StoreCaller<Content>() {
            protected Content doCall() throws ContentStoreException {
                return contentStore.getContent(loc.getSpaceId(),
                                               loc.getContentId());
            }

            public String getLogMessage() {
                return "Error calling contentStore.getContent() for: " +
                    loc.getSpaceId() + "/" + loc.getContentId();
            }
        };
        return caller.call();
    }
}
