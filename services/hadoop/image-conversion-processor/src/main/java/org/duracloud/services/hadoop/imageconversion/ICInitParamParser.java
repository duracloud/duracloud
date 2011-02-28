/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.imageconversion;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.duracloud.services.hadoop.base.InitParamParser;

import static org.duracloud.storage.domain.HadoopTypes.*;

/**
 * @author: Bill Branan
 * Date: Aug 13, 2010
 */
public class ICInitParamParser extends InitParamParser {

    /**
     * {@inheritDoc}
     */
    protected Options createOptions() {
        Options options = super.createOptions();

        String destFormatDesc = "The destination format for the conversion, " +
                                "e.g. GIF, JPG, JP2, etc";
        Option destFormatOption =
            new Option("f", TASK_PARAMS.DEST_FORMAT.getLongForm(), true, destFormatDesc);
        destFormatOption.setRequired(true);
        options.addOption(destFormatOption);

        String colorSpaceDesc = "Output color space, either source or sRGB, " +
            "if this parameter is not included, source is assumed";
        Option colorSpaceOption =
            new Option("c", TASK_PARAMS.COLOR_SPACE.getLongForm(), true, colorSpaceDesc);
        colorSpaceOption.setRequired(false);
        options.addOption(colorSpaceOption);

        String namePrefixDesc = "Files beginning with this prefix are " +
            "converted, if this parameter is not included all prefix " +
            "values are included in the conversion";
        Option namePrefixOption =
            new Option("b", TASK_PARAMS.NAME_PREFIX.getLongForm(), true, namePrefixDesc);
        namePrefixOption.setRequired(false);
        options.addOption(namePrefixOption);

        String nameSuffixDesc = "Files ending with this suffix are " +
            "converted, if this parameter is not included all suffix " +
            "values are included in the conversion";
        Option nameSuffixOption =
            new Option("a", TASK_PARAMS.NAME_SUFFIX.getLongForm(), true, nameSuffixDesc);
        nameSuffixOption.setRequired(false);
        options.addOption(nameSuffixOption);

        String outputSpaceId = "Space to put converted image files";
        Option outputSpaceIdOption =
            new Option("o", TASK_PARAMS.OUTPUT_SPACE_ID.getLongForm(), true, outputSpaceId);
        nameSuffixOption.setRequired(true);
        options.addOption(outputSpaceIdOption);

        return options;
    }
}
