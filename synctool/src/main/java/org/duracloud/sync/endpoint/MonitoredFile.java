/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.util.MimetypeUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

/**
 * @author: Bill Branan
 * Date: 10/20/11
 */
public class MonitoredFile {

    private File file;
    private MonitoredFileInputStream stream;
    private String checksum;
    private String mimetype;

    public MonitoredFile(File file) {
        this.file = file;
        this.stream = null;
        this.checksum = null;
        this.mimetype = null;
    }

    public File getFile() {
        return file;
    }

    
    public boolean exists() {
        return file.exists();
    }

    public String getName() {
        return file.getName();
    }

    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }

    public long length() {
        return file.length();
    }

    public URI toURI() {
        return file.toURI();
    }

    public long getStreamBytesRead() {
        if(null == stream) {
            return 0;
        }
        return stream.getBytesRead();
    }

    public MonitoredFileInputStream getStream() {
        if(null == stream) {
            try {
                stream = new MonitoredFileInputStream(file);
            } catch(FileNotFoundException e) {
                throw new RuntimeException("Could not get stream for " +
                    "file: " + file.getAbsolutePath() + " due to " +
                    e.getMessage(), e);
            }
        }
        return stream;
    }

    public String getChecksum() {
        if(null == checksum) {
            checksum = computeChecksum(file);
        }
        return checksum;
    }

    /*
     * Computes the checksum of a local file
     */
    private String computeChecksum(File file) {
        try {
            ChecksumUtil cksumUtil =
                new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
            return cksumUtil.generateChecksum(file);
        } catch(FileNotFoundException e) {
            throw new RuntimeException("File not found: " +
                                       file.getAbsolutePath(), e);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getMimetype() {
        if(null == mimetype) {
            mimetype = computeMimetype();
        }
        return mimetype;
    }

    private String computeMimetype() {
        MimetypeUtil mimeUtil = new MimetypeUtil();
        return mimeUtil.getMimeType(file);
    }

}
