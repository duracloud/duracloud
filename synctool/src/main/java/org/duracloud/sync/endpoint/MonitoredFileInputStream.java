/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author: Bill Branan
 * Date: 10/20/11
 */
public class MonitoredFileInputStream extends FileInputStream {

    long bytesRead;

    public MonitoredFileInputStream(File file) throws FileNotFoundException {
        super(file);

        this.bytesRead = 0;
    }

    @Override
    public int read() throws IOException {
        int value = super.read();
        if(value > -1) {
            bytesRead++;
        }
        return value;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return updateBytesRead(super.read(b));
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return updateBytesRead(super.read(b, off, len));
    }

    public int updateBytesRead(int byteCount) {
        if(byteCount > 0) {
            bytesRead += byteCount;
        }
        return byteCount;
    }

    public long getBytesRead() {
        return bytesRead;
    }

}
