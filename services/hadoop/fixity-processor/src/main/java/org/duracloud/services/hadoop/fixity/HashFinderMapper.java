/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.fixity;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.services.hadoop.base.ProcessFileMapper;

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
     * @param file the file to have checksum determined
     * @return null file
     */
    @Override
    protected File processFile(File file) throws IOException {
        File nullFile = null;
        if (null == file) {
            super.resultInfo.put(HASH, "null-file");
            return nullFile;
        }

        InputStream fileStream = new FileInputStream(file);
        String hash = checksumUtil.generateChecksum(fileStream);
        closeQuietly(fileStream);

        System.out.println("file: " + file.getPath() + ", hash: " + hash);
        super.resultInfo.put(HASH, hash);

        return nullFile;
    }

    @Override
    protected String collectResult() throws IOException {
        String path = super.resultInfo.get(INPUT_PATH);

        StringBuilder sb = new StringBuilder();
        sb.append(getSpaceId(path));
        sb.append(",");
        sb.append(getContentId(path));
        sb.append(",");
        sb.append(super.resultInfo.get(HASH));
        return sb.toString();
    }

    /**
     * protected for unit testing
     */
    protected String getSpaceId(String path) {
        if (null == path) {
            return "null-space";
        }

        int protoIndex = path.indexOf("://");
        if (protoIndex == -1) {
            return "malformed-path-no-proto-" + path;
        }

        int dotIndex = path.indexOf('.', protoIndex);
        if (dotIndex == -1) {
            return "malformed-path-no-dot-" + path;
        }

        int slashIndex = path.indexOf('/', dotIndex);
        if (slashIndex == -1) {
            return "malformed-path-no-slash-" + path;
        }

        return path.substring(dotIndex + 1, slashIndex);
    }

    /**
     * protected for unit testing
     */
    protected String getContentId(String path) {
        if (null == path) {
            return "null-content-id";
        }

        String proto = "://";
        int protoIndex = path.indexOf(proto);
        if (protoIndex == -1) {
            return "malformed-path-no-proto-" + path;
        }

        int slashIndex = path.indexOf('/', protoIndex + proto.length());
        if (slashIndex == -1) {
            return "malformed-path-no-slash-" + path;
        }

        return path.substring(slashIndex + 1);
    }

    private void closeQuietly(InputStream... streams) {
        for (InputStream stream : streams) {
            IOUtils.closeQuietly(stream);
        }
    }
}
