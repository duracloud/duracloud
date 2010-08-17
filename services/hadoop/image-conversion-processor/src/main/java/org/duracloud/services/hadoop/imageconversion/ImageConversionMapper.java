/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.imageconversion;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.duracloud.services.hadoop.base.ProcessFileMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper used to perform image conversion.
 *
 * @author: Bill Branan
 * Date: Aug 13, 2010
 */
public class ImageConversionMapper extends ProcessFileMapper {

    /**
     * Converts an image file.
     *
     * @param file the file to be converted
     * @param fileName the original name of the image file
     * @return the converted file
     */
    protected File processFile(File file, String fileName) throws IOException {
        String destFormat = jobConf.get(ICInitParamParser.DEST_FORMAT);
        String colorSpace = jobConf.get(ICInitParamParser.COLOR_SPACE);

        File workDir = file.getParentFile();
        File script = createScript(workDir, colorSpace);
        return convertImage(script.getAbsolutePath(),
                            file,
                            destFormat,
                            workDir);
    }

    /*
     * Converts a local image to a given format using ImageMagick.
     * Returns the name of the converted image.
     */
    private File convertImage(String convertScript,
                              File sourceFile,
                              String toFormat,
                              File workDir)
        throws IOException {
        String fileName = sourceFile.getName();

        ProcessBuilder pb =
            new ProcessBuilder(convertScript, toFormat, fileName);
        pb.directory(workDir);
        Process p = pb.start();

        try {
            p.waitFor();  // Wait for the conversion to complete
        } catch (InterruptedException e) {
            throw new IOException("Conversion process interruped for " +
                fileName, e);
        }

        String convertedFileName = FilenameUtils.getBaseName(fileName);
        convertedFileName += "." + toFormat;
        File convertedFile = new File(workDir, convertedFileName);
        if(convertedFile.exists()) {
            return convertedFile;
        } else {
            throw new IOException("Could not find converted file: " +
                convertedFileName);
        }
    }

    /*
     * Creates the script used to perform conversions
     */
    protected File createScript(File workDir, String colorSpace)
        throws IOException {
        String fileName = "convert.sh";
        List<String> scriptLines = new ArrayList<String>();
        scriptLines.add("#!/bin/bash");
        if(colorSpace != null && colorSpace.equals("sRGB")) {
            String csFileName = "sRGB.icm";
            copyFileToWork(workDir, csFileName);
            scriptLines.add("sudo mogrify -profile "+csFileName+" $2");
        }
        scriptLines.add("sudo mogrify -format $1 $2");

        File scriptFile = new File(workDir, fileName);
        FileUtils.writeLines(scriptFile, scriptLines);
        scriptFile.setExecutable(true);
        return scriptFile;
    }

    private void copyFileToWork(File workDir, String fileName)
        throws IOException {
        InputStream inStream = ImageConversionMapper.class.getClassLoader().
                               getResourceAsStream(fileName);
        FileOutputStream outStream =
            new FileOutputStream(new File(workDir, fileName));
        IOUtils.copy(inStream, outStream);
    }

}
