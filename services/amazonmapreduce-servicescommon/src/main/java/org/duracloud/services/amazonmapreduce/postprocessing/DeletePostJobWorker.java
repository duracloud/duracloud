/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonmapreduce.postprocessing;

import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker;
import org.duracloud.services.amazonmapreduce.BaseAmazonMapReducePostJobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DeletePostJobWorker extends BaseAmazonMapReducePostJobWorker {
    private final Logger log = LoggerFactory.getLogger(DeletePostJobWorker.class);

    private ContentStore contentStore;
    private String spaceId;
    private List<String> contentIds;

    public DeletePostJobWorker(AmazonMapReduceJobWorker predecessor,
                               ContentStore contentStore,
                               String spaceId,
                               List<String> contentIds) {
        super(predecessor);

        this.contentStore = contentStore;
        this.spaceId = spaceId;
        this.contentIds = contentIds;
    }

    @Override
    protected void doWork() {
        for(String contentId : contentIds) {
            try {
                contentStore.deleteContent(spaceId, contentId);

            } catch (ContentStoreException e) {
                StringBuilder sb = new StringBuilder("Error: ");
                sb.append("deleting content: ");
                sb.append(spaceId);
                sb.append("/");
                sb.append(contentId);
                sb.append(": ");
                sb.append(e.getMessage());
                log.error(sb.toString());
            }
        }
    }
}
