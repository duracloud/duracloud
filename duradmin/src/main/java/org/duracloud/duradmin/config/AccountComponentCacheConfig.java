/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.config;

import java.util.Arrays;
import java.util.List;

import org.duracloud.common.cache.AccountComponentCache;
import org.duracloud.security.impl.UserDetailsServiceCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountComponentCacheConfig {

    @Bean 
    public String appName(){
        return "duradmin";
    }

    @Bean
    public List<AccountComponentCache>
           globalStores(UserDetailsServiceCache userDetailsStore) {
        return Arrays.asList((AccountComponentCache)userDetailsStore);
    }

}
