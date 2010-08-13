/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.imageconversion;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.duracloud.services.hadoop.base.InitParamParser;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Bill Branan
 * Date: Aug 13, 2010
 */
public class ICInitParamParser extends InitParamParser {

    public static final String DEST_FORMAT = "destFormat";
    public static final String COLOR_SPACE = "colorSpace";
    public static final String NAME_PREFIX = "namePrefix";
    public static final String NAME_SUFFIX = "nameSuffix";    

    /**
     * {@inheritDoc}
     */
    protected Options createOptions() {
        Options options = super.createOptions();

        String destFormatDesc = "The destination format for the conversion, " +
                                "e.g. GIF, JPG, JP2, etc";
        Option destFormatOption =
            new Option("f", DEST_FORMAT, true, destFormatDesc);
        destFormatOption.setRequired(true);
        options.addOption(destFormatOption);

        String colorSpaceDesc = "Output color space, either source or sRGB, " +
            "if this parameter is not included, source is assumed";
        Option colorSpaceOption =
            new Option("c", COLOR_SPACE, true, colorSpaceDesc);
        colorSpaceOption.setRequired(false);
        options.addOption(colorSpaceOption);

        String namePrefixDesc = "Files beginning with this prefix are " +
            "converted, if this parameter is not included all prefix " +
            "values are included in the conversion";
        Option namePrefixOption =
            new Option("p", NAME_PREFIX, true, namePrefixDesc);
        namePrefixOption.setRequired(false);
        options.addOption(namePrefixOption);

        String nameSuffixDesc = "Files ending with this suffix are " +
            "converted, if this parameter is not included all suffix " +
            "values are included in the conversion";
        Option nameSuffixOption =
            new Option("s", NAME_SUFFIX, true, nameSuffixDesc);
        nameSuffixOption.setRequired(false);
        options.addOption(nameSuffixOption);

        return options;
    }
}
