/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.upload;

import java.io.File;
import java.util.List;

/**
 * @author: Bill Branan
 * Date: 10/19/11
 */
public interface UploadFacilitator {

    public void connect(String host,
                        String username,
                        String password,
                        String spaceId);

    public void startUpload(List<File> itemsToUpload);

    public void completeUpload();

    public void exit();

}
