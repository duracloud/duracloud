/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk.writer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.duracloud.chunk.ChunkableContent;
import org.duracloud.chunk.error.NotFoundException;
import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.chunk.stream.ChunkInputStream;
import org.duracloud.chunk.stream.KnownLengthInputStream;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the ContentWriter interface to write the provided
 * content to a local filesystem.
 *
 * @author Andrew Woods
 * Date: Feb 5, 2010
 */
@Deprecated
public class FilesystemContentWriter implements ContentWriter {

    private final Logger log = LoggerFactory.getLogger(FilesystemContentWriter.class);
    private static final long TWO_GB = 2000000000;

    private List<AddContentResult> results = new ArrayList<AddContentResult>();

    /**
     * This method returns the results of the content write requests.
     *
     * @return List of results
     */
    public List<AddContentResult> getResults() {
        return results;
    }

    public void ignore(String spaceId, String contentId, long contentSize) {
        AddContentResult result = new AddContentResult(spaceId,
                                                       contentId,
                                                       contentSize);
        result.setState(AddContentResult.State.IGNORED);
        results.add(result);
    }

    /**
     * This method implements the ContentWriter interface for writing content
     * to a DataStore. In this case, the DataStore is a local filesystem.
     * The arg spaceId is the path to the destination directory.
     *
     * @param spaceId   destination where arg chunkable content will be written
     * @param chunkable content to be written
     */
    public ChunksManifest write(String spaceId,
                                ChunkableContent chunkable)
        throws NotFoundException {
        return write(spaceId, chunkable, null);
    }

    /**
     * This method implements the ContentWriter interface for writing content
     * to a DataStore. In this case, the DataStore is a local filesystem.
     * The arg spaceId is the path to the destination directory.
     *
     * @param spaceId           destination where arg chunkable content will be written
     * @param contentProperties user defined properties to be associated with content.
     * @param chunkable         content to be written
     */
    public ChunksManifest write(String spaceId,
                                ChunkableContent chunkable,
                                Map<String, String> contentProperties)
        throws NotFoundException {
        for (ChunkInputStream chunk : chunkable) {
            writeSingle(spaceId, null, chunk, null);
        }

        ChunksManifest manifest = chunkable.finalizeManifest();
        KnownLengthInputStream manifestStream = manifest.getBody();

        AddContentResult result =
            writeContent(spaceId,
                         manifest.getManifestId(),
                         manifestStream,
                         manifestStream.getLength(),
                         contentProperties);
        result.setMd5("md5-not-collected-for-manifest");

        return manifest;
    }

    /**
     * This method implements the ContentWriter interface for writing content
     * to a DataStore. In this case, the DataStore is a local filesystem.
     * The arg spaceId is the path to the destination directory.
     *
     * @param spaceId    destination where arg chunk content will be written
     * @param chunk      content to be written
     * @param properties user-defined properties associated with content
     * @return MD5 of content
     * @throws NotFoundException
     */
    @Override
    public String writeSingle(String spaceId,
                              String chunkChecksum,
                              ChunkInputStream chunk,
                              Map<String, String> properties)
        throws NotFoundException {
        AddContentResult result = writeContent(spaceId,
                                               chunk.getChunkId(),
                                               chunk,
                                               chunk.getChunkSize(),
                                               properties);
        String finalChecksum = chunk.getMD5();
        if (chunkChecksum != null && chunk.md5Preserved()) {
            if (!chunkChecksum.equals(finalChecksum)) {
                result.setState(AddContentResult.State.ERROR);
            }
        }

        result.setMd5(finalChecksum);
        return finalChecksum;
    }

    @Override
    public String writeSingle(String spaceId,
                              String chunkChecksum,
                              ChunkInputStream chunk)
        throws NotFoundException {
        return writeSingle(spaceId, chunkChecksum, chunk, null);
    }

    private AddContentResult writeContent(String spaceId,
                                          String contentId,
                                          InputStream inputStream,
                                          long contentSize,
                                          Map<String, String> contentProperties) {

        File spaceDir = getSpaceDir(spaceId);
        OutputStream outStream = getOutputStream(spaceDir, contentId);

        AddContentResult result = new AddContentResult(spaceId,
                                                       contentId,
                                                       contentSize);
        result.setState(AddContentResult.State.SUCCESS);
        try {
            copy(inputStream, outStream);
        } catch (Exception e) {
            result.setState(AddContentResult.State.ERROR);
        }

        flushAndClose(outStream);

        results.add(result);
        return result;
    }

    private void copy(InputStream chunk, OutputStream outStream) {
        try {
            IOUtil.copy(chunk, outStream);
        } catch (DuraCloudRuntimeException e) {
            String msg = "Error in copy: " + chunk.toString() + ": ";
            log.error(msg, e);
            throw new DuraCloudRuntimeException(msg + e.getMessage(), e);
        }
    }

    private OutputStream getOutputStream(File spaceDir, String contentId) {
        File outFile = getContentFile(spaceDir, contentId);
        return getOutputStream(outFile);
    }

    private OutputStream getOutputStream(File outFile) {
        final int BUFFER_SIZE = 8192;
        try {
            return new BufferedOutputStream(new FileOutputStream(outFile),
                                            BUFFER_SIZE);
        } catch (FileNotFoundException e) {
            throw new DuraCloudRuntimeException(e.getMessage(), e);
        }
    }

    private void flushAndClose(OutputStream outStream) {
        try {
            outStream.flush();
        } catch (IOException e) {
            // do nothing
        } finally {
            IOUtils.closeQuietly(outStream);
        }
    }

    private File getContentFile(File spaceDir, String contentId) {
        File contentFile = new File(spaceDir, contentId);
        File parent = contentFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        return contentFile;
    }

    private File getSpaceDir(String spaceId) {
        File spaceDir = new File(spaceId);
        if (!spaceDir.exists()) {
            spaceDir.mkdirs();
        }
        return spaceDir;
    }

}
