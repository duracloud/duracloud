/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

/**
 * @author Daniel Bernstein
 *         Date: Sep 12, 2014
 */
public  class SystemPropertiesVerifier {
    private String[] props;
    public SystemPropertiesVerifier(String[] props){
        this.props = props;
    }
    
    public void verify(){
        for(String prop : props){
            verifySystemProp(prop);
        }
    }
    /**
     * @param key
     */
    protected  void verifySystemProp(String key) {
        if (System.getProperty(key) == null){
            throw new RuntimeException("required system property " + key
                    + " has not been set.");
        }
    }

}
