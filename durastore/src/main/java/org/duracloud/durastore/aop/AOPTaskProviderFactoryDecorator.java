/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.aop;

import java.util.HashMap;
import java.util.Map;

import org.duracloud.storage.error.TaskException;
import org.duracloud.storage.provider.TaskProvider;
import org.duracloud.storage.provider.TaskProviderFactory;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.framework.ProxyFactoryBean;

/**
 * An AOP decorate to wrap the task provider factory.
 * This class is necessary to ensure that pointcuts will be triggered when
 * calling TaskProviders which are not spring managed.
 * @author Daniel Bernstein
 *         11/16/2015
 *         
 *
 */
public class AOPTaskProviderFactoryDecorator implements TaskProviderFactory {
    private TaskProviderFactory factory;
    private PointcutAdvisor advisor;
    private Map<String, TaskProvider> providerMap;
    
    public AOPTaskProviderFactoryDecorator(TaskProviderFactory factory, PointcutAdvisor advisor) {
        this.factory = factory;
        this.advisor = advisor;
        this.providerMap = new HashMap<>();
    }
    
    @Override
    public TaskProvider getTaskProvider() {
        return getTaskProvider(null);
    }

    @Override
    public TaskProvider getTaskProvider(String storageAccountId)
        throws TaskException {
        
        TaskProvider provider = providerMap.get(storageAccountId);

        if(provider == null){
            provider = this.factory.getTaskProvider(storageAccountId);
            ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
            proxyFactoryBean.setTargetClass(TaskProvider.class);
            proxyFactoryBean.setTarget(provider);
            proxyFactoryBean.addAdvisor(advisor);
            provider = (TaskProvider)proxyFactoryBean.getObject();
            providerMap.put(storageAccountId, provider);
        }

        return provider;
    }

}
