/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.image;

/**
 * @author: Bill Branan
 * Date: Apr 22, 2010
 */
public interface ConversionResultListener {

    public void  processConversionResult(ConversionResult result);
    
}
