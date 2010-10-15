/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.fixity;

import org.apache.commons.io.IOUtils;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.services.hadoop.base.ProcessFileMapper;
import org.duracloud.services.hadoop.base.ProcessResult;
import org.duracloud.services.hadoop.store.FileWithMD5;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.duracloud.common.util.ChecksumUtil.Algorithm;

/**
 * Mapper used to perform fixity service.
 *
 * @author: Andrew Woods
 * Date: Sept 21, 2010
 */
public class HashFinderMapper extends ProcessFileMapper {

    private ChecksumUtil checksumUtil = new ChecksumUtil(Algorithm.MD5);
    private static final String HASH = "hash-key";

    /**
     * Runs Fixity service on file.
     *
     * @param fileWithMD5 the file to have checksum determined
     * @param origContentId the original ID of the file
     * @return null file
     */
    @Override
    protected ProcessResult processFile(FileWithMD5 fileWithMD5, String origContentId)
        throws IOException {
        if (null == fileWithMD5 || null == fileWithMD5.getFile()) {
            super.resultInfo.put(HASH, "null-file");
            return null;
        }

        File file = fileWithMD5.getFile();

        String hash = fileWithMD5.getMd5();
        if (null == hash) {
            InputStream fileStream = new FileInputStream(file);
            hash = checksumUtil.generateChecksum(fileStream);
            closeQuietly(fileStream);
        }

        System.out.println("file: " + file.getPath() + ", hash: " + hash);
        super.resultInfo.put(HASH, hash);

        return null;
    }

    @Override
    protected String collectResult() throws IOException {
        String path = super.resultInfo.get(INPUT_PATH);

        StringBuilder sb = new StringBuilder();
        sb.append(pathUtil.getSpaceId(path));
        sb.append(",");
        sb.append(pathUtil.getContentId(path));
        sb.append(",");
        sb.append(super.resultInfo.get(HASH));
        return sb.toString();
    }

    private void closeQuietly(InputStream... streams) {
        for (InputStream stream : streams) {
            IOUtils.closeQuietly(stream);
        }
    }
}
