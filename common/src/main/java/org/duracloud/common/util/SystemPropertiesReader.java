package org.duracloud.common.util;

import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
/**
 * Reads one or more and overlays them on the system properties.
 * 
 * @author Daniel Bernstein
 *
 */
public class SystemPropertiesReader{
    private Logger log = LoggerFactory.getLogger(SystemPropertiesReader.class);
    
    public SystemPropertiesReader(final Collection<Resource> resources){
        Properties systemProperties = System.getProperties();
        for(Resource resource : resources){
            try(InputStream inputStream = resource.getInputStream()){
                systemProperties.load(inputStream);
                log.info("loaded {}", resource.getFile().getAbsolutePath());
            }catch(Exception ex){
                try{
                    log.warn("unable to load resource: {}", resource.getFile().getAbsolutePath());
                }catch(Exception e){}
            }
        }
    }
}