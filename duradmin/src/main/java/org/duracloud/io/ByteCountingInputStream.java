/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream wrapper that tracks the number of bytes that have been read off the underlying stream
 * @author Daniel Bernstein
 *
 */
public class ByteCountingInputStream extends InputStream{
	private InputStream is;
	private long bytesRead = 0;
	
	public ByteCountingInputStream(InputStream is){
		super();
		this.is = is;
	}

	public long getBytesRead(){
		return this.bytesRead;
	}

	@Override
	public int available() throws IOException {
		return this.is.available();
	}
	
	private void count(int bytes){
		if(bytes > 0){
			this.bytesRead +=bytes;
		}
	}
	 
	@Override
	public int read(byte[] b) throws IOException {
		int read = this.is.read(b);
		count(read);
		return read;
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int read =  this.is.read(b, off, len);
		count(read);
		return read;

	}
	
	@Override
	public int read() throws IOException {
		int read = this.is.read();
		count(read);
		return read;
	}
	
	@Override
	public long skip(long n) throws IOException {
		return this.is.skip(n);
	}
	
	@Override
	public synchronized void mark(int readlimit) {
		this.is.mark(readlimit);
	}
	
	@Override
	public boolean markSupported() {
		return this.is.markSupported();
	}
	
	@Override
	public synchronized void reset() throws IOException {
		this.is.reset();
	}
	
	@Override
	public void close() throws IOException {
		this.is.close();
	}
}
