/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.contentstore;

import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.rest.DuraCloudRequestContextUtil;
import org.duracloud.common.util.AccountStoreConfig;
import org.duracloud.duradmin.config.DuradminConfig;

public class ContentStoreManagerFactoryImpl
        implements ContentStoreManagerFactory {

    private DuraCloudRequestContextUtil requestUtil;

    public ContentStoreManagerFactoryImpl(DuraCloudRequestContextUtil requestUtil){
        this.requestUtil = requestUtil;
    }
    public ContentStoreManager create() throws Exception {
        String durastoreHost = null;
        String durastorePort = null;
        if(AccountStoreConfig.accountStoreIsLocal()){
            durastoreHost = DuradminConfig.getDuraStoreHost();
            durastoreHost = DuradminConfig.getDuraStorePort();
        }else{
            durastoreHost = requestUtil.getHost();
            durastorePort = requestUtil.getPort()+"";
        }
        
        return new ContentStoreManagerImpl(durastoreHost,
                                           durastorePort);
    }
}
