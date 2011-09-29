/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.streaming;

/**
 * @author: Bill Branan
 * Date: 9/14/11
 */
public interface StreamingUpdateListener {

    public void successfulStreamingAddition(String mediaSpaceId,
                                            String mediaContentId);

    public void failedStreamingAddition(String mediaSpaceId,
                                        String mediaContentId,
                                        String failureMessage);

}
