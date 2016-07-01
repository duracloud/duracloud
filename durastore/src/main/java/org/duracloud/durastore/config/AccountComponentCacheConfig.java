package org.duracloud.durastore.config;

import java.util.Arrays;
import java.util.List;

import org.duracloud.common.cache.AccountComponentCache;
import org.duracloud.durastore.util.StorageProviderFactoryCache;
import org.duracloud.durastore.util.TaskProviderFactoryCache;
import org.duracloud.security.impl.UserDetailsServiceCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountComponentCacheConfig {

    @Bean 
    public String appName(){
        return "durastore";
    }

    @Bean
    public List<AccountComponentCache<?>>
           globalStores(UserDetailsServiceCache userDetailsServiceCache,
                        StorageProviderFactoryCache storageProviderFactoryCache,
                        TaskProviderFactoryCache taskProviderFactoryCache) {
        return Arrays.asList((AccountComponentCache<?>) userDetailsServiceCache,
                             (AccountComponentCache<?>) storageProviderFactoryCache,
                             (AccountComponentCache<?>) taskProviderFactoryCache);
    }

}
