package org.duracloud.contentindex.client;

import static org.duracloud.contentindex.client.ESContentIndexClient.SHARED_INDEX;

import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

public class ESContentIndexInitializer {

    public void initialize(){

        Map<String,String> settingsMap = new HashMap<>();
        // create all data directories under Maven build directory
        settingsMap.put("index.number_of_shards", "5");
        settingsMap.put("index.number_of_replicas", "0");
        // disable clustering
        settingsMap.put("discovery.zen.ping.multicast.enabled", "false");
        // disable automatic index creation
        settingsMap.put("action.auto_create_index", "false");
        // disable automatic type creation
        settingsMap.put("index.mapper.dynamic", "false");

        
        Settings settings = ImmutableSettings.settingsBuilder()
                                             .put(settingsMap).build();
        
        Client client = new TransportClient(settings)
            .addTransportAddress(new InetSocketTransportAddress("localhost", 9300));

        ElasticsearchOperations elasticsearchOperations =
            new ElasticsearchTemplate(client);
        ContentIndexClient contentIndexClient = new ESContentIndexClient(elasticsearchOperations,
                                                      client);
        contentIndexClient.addIndex(SHARED_INDEX, false);

        elasticsearchOperations.putMapping(ContentIndexItem.class);
        elasticsearchOperations.putMapping(AccountIndexItem.class);
        
    }
}
