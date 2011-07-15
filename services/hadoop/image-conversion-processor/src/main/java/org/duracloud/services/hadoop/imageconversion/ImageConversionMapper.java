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
import org.duracloud.services.hadoop.base.ProcessResult;
import org.duracloud.services.hadoop.store.FileWithMD5;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.duracloud.services.hadoop.base.Constants.DELIM;
import static org.duracloud.storage.domain.HadoopTypes.*;

/**
 * Mapper used to perform image conversion.
 *
 * @author: Bill Branan
 * Date: Aug 13, 2010
 */
public class ImageConversionMapper extends ProcessFileMapper {

    public static final String PROC_TIME = "processing-time";
    public static final String SRC_SIZE = "source-file-bytes";

    /**
     * Converts an image file.
     *
     * @param fileWithMD5 the file to be converted
     * @param origContentId the original ID of the file
     * @return the converted file
     */
    @Override
    protected ProcessResult processFile(FileWithMD5 fileWithMD5, String origContentId)
        throws IOException {
        if (null == fileWithMD5 || null == fileWithMD5.getFile()) {
            throw new IOException("arg file is null.");
        }
        File file = fileWithMD5.getFile();
        resultInfo.put(SRC_SIZE, String.valueOf(file.length()));

        String destFormat = jobConf.get(TASK_PARAMS.DEST_FORMAT.getLongForm());
        String colorSpace = jobConf.get(TASK_PARAMS.COLOR_SPACE.getLongForm());

        File workDir = file.getParentFile();
        File script = createScript(workDir, colorSpace, destFormat);

        long startProcTime = System.currentTimeMillis();
        File resultFile = convertImage(script.getAbsolutePath(),
                                       file,
                                       destFormat,
                                       workDir);
        long processingTime = System.currentTimeMillis() - startProcTime;
        resultInfo.put(PROC_TIME, String.valueOf(processingTime));

        String contentId =
            origContentId.replace(file.getName(), resultFile.getName());

        return new ProcessResult(resultFile, contentId);
    }

    @Override
    protected String collectResult() throws IOException {
        String result = resultInfo.get(RESULT);

        String errMsg = resultInfo.get(ERR_MESSAGE);
        if (errMsg != null) {
            result += "-" + errMsg;
        }

        result += DELIM + resultInfo.get(INPUT_PATH);
        result += DELIM + resultInfo.get(RESULT_PATH);

        result += DELIM + resultInfo.get(PROC_TIME);
        result += DELIM +  resultInfo.get(SRC_SIZE);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String now = format.format(new Date(System.currentTimeMillis()));
        result += DELIM + now;

        return result;
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

        String name = fileName.trim();
        name = name.replaceAll(" ", "_");

        if(!fileName.equals(name)) {
            File noSpaceFile = new File(workDir, name);
            sourceFile.renameTo(noSpaceFile);
        }

        ProcessBuilder pb =
            new ProcessBuilder(convertScript, toFormat, name);
        pb.directory(workDir);
        Process p = pb.start();

        System.out.println("Image conversion started for " + fileName);

        try {
            p.waitFor();  // Wait for the conversion to complete
        } catch (InterruptedException e) {
            throw new IOException("Conversion process interruped for " +
                fileName, e);
        }

        System.out.println("Image conversion complete for " + fileName);

        String convertedFileName = FilenameUtils.getBaseName(fileName);
        convertedFileName += "." + toFormat;
        File convertedFile = new File(workDir, convertedFileName);

        if(!fileName.equals(name)) {
            File noSpaceFile = new File(workDir,
                                        FilenameUtils.getBaseName(name) +
                                            "." + toFormat);
            noSpaceFile.renameTo(convertedFile);
        }

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
    protected File createScript(File workDir,
                                String colorSpace,
                                String destFormat)
        throws IOException {
        String fileName = "convert.sh";
        List<String> scriptLines = new ArrayList<String>();
        scriptLines.add("#!/bin/bash");

        if(colorSpace != null && colorSpace.equals("sRGB")) {
            String csFileName = "sRGB.icm";
            copyFileToWork(workDir, csFileName);
            scriptLines.add("sudo mogrify -profile "+csFileName+" $2");
        }

        StringBuilder transformLine = new StringBuilder("sudo mogrify ");
        if(destFormat.equalsIgnoreCase("jp2")) {
            // Adds settings which limit the conversion memory footprint
            transformLine.append("-define jp2:tilewidth=256 " +
                                 "-define jp2:tileheight=256 ");
        }
        transformLine.append("-format $1 $2");

        scriptLines.add(transformLine.toString());

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
