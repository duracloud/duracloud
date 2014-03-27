/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.contentindex.client;

import static org.duracloud.contentindex.client.ESContentIndexClient.SHARED_INDEX;

import org.elasticsearch.client.Client;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
public class ESContentIndexInitializer {

    public void initialize(){

        Client client = ESContentIndexClientUtil.createESClient();
        ElasticsearchOperations elasticsearchOperations =
            new ElasticsearchTemplate(client);
        ContentIndexClient contentIndexClient =
            new ESContentIndexClient(elasticsearchOperations, client);
        contentIndexClient.addIndex(SHARED_INDEX, false);
        elasticsearchOperations.putMapping(ContentIndexItem.class);
        elasticsearchOperations.putMapping(AccountIndexItem.class);
    }
}
