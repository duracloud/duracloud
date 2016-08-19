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
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author: Bill Branan
 * Date: 10/20/11
 */
public class MonitoredInputStream extends FilterInputStream {

    long bytesRead;

    public MonitoredInputStream(File file) throws FileNotFoundException {
        this(new FileInputStream(file));
    }
    
    public MonitoredInputStream(InputStream is)  {
        super(is);
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
        return read(b, 0, b.length);
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
