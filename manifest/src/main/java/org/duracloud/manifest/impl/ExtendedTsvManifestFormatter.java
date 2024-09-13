package org.duracloud.manifest.impl;

import static org.duracloud.common.util.bulk.ManifestVerifier.DELIM;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.duracloud.mill.db.model.ManifestItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which formats manifests with additional headers (content-size, mime-type) as tab separated values
 *
 * @author mikejritter
 */
public class ExtendedTsvManifestFormatter extends ManifestFormatterBase {
    private static final Logger logger = LoggerFactory.getLogger(ExtendedTsvManifestFormatter.class);

    private static final Pattern LINE_PATTERN =
        Pattern.compile("(.+)" + DELIM + "(.+)" + DELIM + "(.+)" + DELIM + "(.+)" + DELIM + "(.+)");

    private static final String HEADER =
        "space-id" + DELIM + "content-id" + DELIM + "MD5" + DELIM + "content-size" + DELIM + "mime-type";

    @Override
    protected Logger log() {
        return logger;
    }

    @Override
    public String formatLine(ManifestItem item) {
        return item.getSpaceId() + DELIM + item.getContentId() + DELIM +
               item.getContentChecksum() + DELIM + item.getContentSize() + DELIM +
               item.getContentMimetype();
    }

    @Override
    public String getHeader() {
        return HEADER;
    }

    @Override
    public ManifestItem parseLine(String line) throws ParseException {
        Matcher matcher = LINE_PATTERN.matcher(line);
        if (!matcher.find()) {
            throw new ParseException("Line doesn't match tsv format: unable to parse line: ->" + line + "<-", 0);
        }

        ManifestItem item = new ManifestItem();
        item.setSpaceId(matcher.group(1));
        item.setContentId(matcher.group(2));
        item.setContentChecksum(matcher.group(3));
        item.setContentSize(matcher.group(4));
        item.setContentMimetype(matcher.group(5));

        return item;
    }
}
