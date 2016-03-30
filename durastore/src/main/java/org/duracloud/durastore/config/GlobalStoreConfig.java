package org.duracloud.durastore.config;

import java.util.Arrays;
import java.util.List;

import org.duracloud.durastore.util.GlobalStorageProviderStore;
import org.duracloud.durastore.util.TaskProviderFactoryStore;
import org.duracloud.security.impl.GlobalStore;
import org.duracloud.security.impl.GlobalUserDetailsStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlobalStoreConfig {

    @Bean 
    public String appName(){
        return "durastore";
    }

    @Bean
    public List<GlobalStore<?>>
           globalStores(GlobalUserDetailsStore userDetailsStore,
                        GlobalStorageProviderStore storageProviderStore,
                        TaskProviderFactoryStore taskProviderFactoryStore) {
        return Arrays.asList((GlobalStore<?>) userDetailsStore,
                             (GlobalStore<?>) storageProviderStore,
                             (GlobalStore<?>) taskProviderFactoryStore);
    }

}
