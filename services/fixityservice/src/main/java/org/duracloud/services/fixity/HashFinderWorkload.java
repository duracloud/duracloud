/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity;

import org.apache.commons.io.input.AutoCloseInputStream;
import org.duracloud.client.ContentStore;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.fixity.domain.ContentLocation;
import org.duracloud.services.fixity.domain.FixityServiceOptions;
import org.duracloud.services.fixity.util.CountListener;
import org.duracloud.services.fixity.util.IteratorCounterThread;
import org.duracloud.services.fixity.util.StoreCaller;
import org.duracloud.services.fixity.worker.ServiceWorkload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import static org.duracloud.services.fixity.domain.FixityServiceOptions.Mode;

/**
 * @author Andrew Woods
 *         Date: Aug 5, 2010
 */
public class HashFinderWorkload implements ServiceWorkload<ContentLocation>, CountListener {

    private final Logger log = LoggerFactory.getLogger(HashFinderWorkload.class);

    private FixityServiceOptions serviceOptions;
    private ContentStore contentStore;

    private Iterator<ContentLocation> workload;
    private List<CountListener> countListeners;
    private long count;

    public HashFinderWorkload(FixityServiceOptions serviceOptions,
                              ContentStore contentStore) {
        this.serviceOptions = serviceOptions;
        this.contentStore = contentStore;

        this.workload = createWorkload();
        this.countListeners = new ArrayList<CountListener>();
        findWorkloadSize();
    }

    private Iterator<ContentLocation> createWorkload() {
        Iterator<ContentLocation> itr;

        FixityServiceOptions.Mode mode = serviceOptions.getMode();
        if (findFromSpace(mode)) {
            Iterator<String> contents = getSpaceContents();
            if (null == contents) {
                itr = noWorkload(
                    "Error: no content items found for " + getSpaceId());
            }
            itr = new SpaceIterator(getSpaceId(), contents);

        } else if (findFromListing(mode)) {
            itr = getContentsFromListing();
            if (null == itr) {
                itr = noWorkload(
                    "Error: no content items found in listing " + getSpaceId() +
                        "/" + getContentId());
            }

        } else {
            itr = noWorkload("Error: Unexpected mode: '" + mode.getKey() + "'");
        }

        return itr;
    }

    private Iterator<String> getSpaceContents() {
        StoreCaller<Iterator<String>> caller = new StoreCaller<Iterator<String>>() {
            protected Iterator<String> doCall() throws ContentStoreException {
                return contentStore.getSpaceContents(getSpaceId());
            }

            public String getLogMessage() {
                return "Error calling contentStore.getSpaceContents() for: " +
                    getSpaceId();
            }
        };
        return caller.call();
    }

    private Iterator<ContentLocation> getContentsFromListing() {
        Content listing = getContent();
        if (null == listing) {
            return noWorkload(
                "Error: no content listing found for: " + getSpaceId() + "/" +
                    getContentId());
        }

        InputStream listingStream = listing.getStream();
        if (null == listingStream) {
            return noWorkload(
                "Error: listing stream is null for: " + getSpaceId() + "/" +
                    getContentId());
        }

        return new ListingIterator(new AutoCloseInputStream(listingStream));
    }

    private Content getContent() {
        StoreCaller<Content> caller = new StoreCaller<Content>() {
            protected Content doCall() throws ContentStoreException {
                return contentStore.getContent(getSpaceId(), getContentId());
            }

            public String getLogMessage() {
                return "Error calling contentStore.getContent() for: " +
                    getSpaceId() + "/" + getContentId();
            }
        };
        return caller.call();
    }

    private String getContentId() {
        String contentId;

        Mode mode = serviceOptions.getMode();
        if (findFromListing(mode)) {
            contentId = serviceOptions.getProvidedListingContentIdA();

        } else {
            log.error("Invalid mode for seeking contentId: " + mode.getKey());
            contentId = "invalid-mode-for-content-id";
        }
        return contentId;
    }

    private boolean findFromListing(Mode mode) {
        return mode.equals(Mode.ALL_IN_ONE_LIST) ||
            mode.equals(Mode.GENERATE_LIST);
    }

    private boolean findFromSpace(Mode mode) {
        return mode.equals(Mode.ALL_IN_ONE_SPACE) ||
            mode.equals(Mode.GENERATE_SPACE);
    }

    private Iterator<ContentLocation> noWorkload(String msg) {
        log.error(msg);

        List<ContentLocation> tmp = new ArrayList<ContentLocation>();
        return tmp.iterator();
    }

    private String getSpaceId() {
        String spaceId;

        Mode mode = serviceOptions.getMode();
        if (findFromSpace(mode)) {
            spaceId = serviceOptions.getTargetSpaceId();

        } else if (findFromListing(mode)) {
            spaceId = serviceOptions.getProvidedListingSpaceIdA();

        } else {
            log.error("Error: Invalid mode: " + mode.getKey());
            spaceId = "error-retrieving-space-id";
        }

        return spaceId;
    }

    private void findWorkloadSize() {
        Iterator itr = null;
        FixityServiceOptions.Mode mode = serviceOptions.getMode();
        if (findFromSpace(mode)) {
            itr = getSpaceContents();

        } else if (findFromListing(mode)) {
            itr = getContentsFromListing();
        }

        if (itr != null) {
            IteratorCounterThread p = new IteratorCounterThread(itr, this);
            new Thread(p).start();

        } else {
            log.warn("Unable to determine workload size, itr is null.");
        }
    }

    @Override
    public boolean hasNext() {
        return workload.hasNext();
    }

    @Override
    public ContentLocation next() {
        return workload.next();
    }

    @Override
    public void registerCountListener(CountListener listener) {
        countListeners.add(listener);
    }

    @Override
    public void setCount(long count) {
        this.count = count;
        for (CountListener listener : countListeners) {
            listener.setCount(count);
        }
    }


    private class SpaceIterator implements Iterator<ContentLocation> {
        String spaceId;
        private Iterator<String> contents;

        public SpaceIterator(String spaceId, Iterator<String> contents) {
            this.spaceId = spaceId;
            this.contents = contents;
        }

        @Override
        public boolean hasNext() {
            return contents.hasNext();
        }

        @Override
        public ContentLocation next() {
            return new ContentLocation(spaceId, contents.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove() not supported");
        }
    }


    private class ListingIterator implements Iterator<ContentLocation> {
        private BufferedReader reader;
        private String currentLine;

        public ListingIterator(InputStream inputStream) {
            reader = new BufferedReader(new InputStreamReader(inputStream));

            // skip first header line
            currentLine = readLine();

            // buffer first real line
            currentLine = readLine();
        }

        private String readLine() {
            try {
                return reader.readLine();

            } catch (IOException e) {
                log.error("Error reading next line from ListingIterator.");
                return null;
            }
        }

        @Override
        public boolean hasNext() {
            return currentLine != null;
        }

        @Override
        public ContentLocation next() {
            StringTokenizer tokenizer = new StringTokenizer(currentLine, ",");

            String spaceId = null;
            if (tokenizer.hasMoreTokens()) {
                spaceId = tokenizer.nextToken().trim();
            }

            String contentId = null;
            if (tokenizer.hasMoreTokens()) {
                contentId = tokenizer.nextToken().trim();
            }

            if (null == spaceId || null == contentId) {
                log.error("Invalid listing format: " + currentLine);
                spaceId = "error-unknown-space-id";
                contentId = "error-unknown-content-id";
            }

            currentLine = readLine();
            return new ContentLocation(spaceId, contentId);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove() not supported");
        }
    }
}
