/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.duracloud.common.error.DuraCloudRuntimeException;

/**
 * Provides utility methods for I/O.
 *
 * @author Bill Branan
 */
public class IOUtil {

    public static String readStringFromStream(InputStream stream)
        throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(stream, writer, "UTF-8");
        stream.close();
        return writer.toString();
    }

    public static InputStream writeStringToStream(String string)
        throws IOException {
        return IOUtils.toInputStream(string, "UTF-8");
    }

    public static OutputStream getOutputStream(File file) {
        OutputStream output;
        try {
            output = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new DuraCloudRuntimeException(e);
        }
        return output;
    }

    public static void copy(InputStream input, OutputStream output) {
        try {
            IOUtils.copy(input, output);
        } catch (IOException e) {
            throw new DuraCloudRuntimeException(e);
        }
    }

    public static void copyFileToDirectory(File file, File dir) {
        try {
            FileUtils.copyFileToDirectory(file, dir);
        } catch (IOException e) {
            throw new DuraCloudRuntimeException(e);
        }
    }

    public static void fileFindReplace(File file, String find, String replace) 
        throws IOException {
        String fileContents = FileUtils.readFileToString(file);
        fileContents = fileContents.replaceAll("\\Q" + find + "\\E", replace);
        FileUtils.writeStringToFile(file, fileContents);
    }

    public static File writeStreamToFile(InputStream inStream) {
        File file = null;
        OutputStream outStream = null;
        try {
            file = File.createTempFile("file", ".tmp");
            outStream = FileUtils.openOutputStream(file);
            IOUtils.copy(inStream, outStream);
        } catch (IOException e) {
            String err = "Error writing stream to file: " + e.getMessage();
            throw new DuraCloudRuntimeException(err, e);
        } finally {
            if(null != inStream) {
                IOUtils.closeQuietly(inStream);
            }
            if(null != outStream) {
                IOUtils.closeQuietly(outStream);
            }
        }
        return file;
    }

    public static InputStream getFileStream(File file) {
        try {
            return new AutoCloseInputStream(FileUtils.openInputStream(file));
        } catch (IOException e) {
            String err = "Error opening stream from file " +
                         file.getAbsolutePath() + ": " + e.getMessage();
            throw new DuraCloudRuntimeException(err, e);
        }
    }
    
    /**
     * Adds the specified file to the zip output stream.
     * @param file
     * @param zipOs
     */
    public static void addFileToZipOutputStream(File file, ZipOutputStream zipOs) throws IOException {
        String fileName = file.getName();
        try (FileInputStream fos = new FileInputStream(file)){
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipEntry.setSize(file.length());
            zipEntry.setTime(System.currentTimeMillis());
            zipOs.putNextEntry(zipEntry);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = fos.read(buf)) > 0) {
                zipOs.write(buf, 0, bytesRead);
            }
            zipOs.closeEntry();
            fos.close();
        }     
    }

}
