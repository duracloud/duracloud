/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.imageconversion;

import org.apache.commons.io.FileUtils;
import org.duracloud.services.hadoop.base.ProcessFileMapper;
import java.io.File;
import java.io.IOException;

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
        String namePrefix = jobConf.get(ICInitParamParser.NAME_PREFIX);
        String nameSuffix = jobConf.get(ICInitParamParser.NAME_SUFFIX);

        if(!fileName.endsWith(".txt")) {
            fileName += ".txt";
        }
        File resultFile = new File(getTempDir(), fileName);

        String outputText = "Performing Image Conversion" +
                            " to desFormat: " + destFormat +
                            " to colorSpace: " + colorSpace +
                            " to namePrefix: " + namePrefix +
                            " to nameSuffix: " + nameSuffix;
        FileUtils.writeStringToFile(resultFile, outputText, "UTF-8");

        return resultFile;
    }

}
