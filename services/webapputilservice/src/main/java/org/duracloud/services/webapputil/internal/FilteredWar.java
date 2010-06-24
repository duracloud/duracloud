/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.webapputil.internal;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ProxyInputStream;
import org.duracloud.services.common.model.NamedFilterList;
import org.duracloud.services.webapputil.error.WebAppDeployerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * This class wraps the inputstream of a war file, and applies the filters
 * supplied in the constructor's namedFilters. In other words, this class
 * will apply filters (i.e. substitute text) to files contained within the
 * wrapped war inputstream.
 *
 * @author Andrew Woods
 *         Date: Jan 28, 2010
 */
public class FilteredWar extends ProxyInputStream {

    private final static Logger log = LoggerFactory.getLogger(FilteredWar.class);

    public FilteredWar(InputStream inputStream, NamedFilterList namedFilters) {
        super(filter(inputStream, namedFilters));
    }

    private static InputStream filter(InputStream inputStream,
                                      NamedFilterList namedFilters) {
        try {
            return doFilter(inputStream, namedFilters);

        } catch (IOException e) {
            String msg = "Error filtering war: " + e.getMessage();
            log.error(msg, e);
            throw new WebAppDeployerException(msg, e);
        }
    }

    private static InputStream doFilter(InputStream inputStream,
                                        NamedFilterList namedFilters)
        throws IOException {
        final int BUFFER = 2048;
        File tmpWar = createTempFile();

        ZipInputStream zis = new ZipInputStream(inputStream);
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tmpWar));

        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            log.debug("Extracting: " + entry);

            NamedFilterList.NamedFilter filter = getFilter(namedFilters, entry);
            ZipEntry newEntry = new ZipEntry(entry.getName());
            zos.putNextEntry(newEntry);

            int count;
            byte[] data = new byte[BUFFER];
            while ((count = zis.read(data, 0, BUFFER)) != -1) {
                byte[] tmpData = data;
                if (filter != null) {
                    tmpData = filterBuffer(data, count, filter);
                    count = tmpData.length;
                }
                zos.write(tmpData, 0, count);
            }
            zos.closeEntry();
        }

        zos.flush();
        IOUtils.closeQuietly(zis);
        IOUtils.closeQuietly(zos);

        return new FileInputStream(tmpWar);
    }

    private static byte[] filterBuffer(byte[] data,
                                       int count,
                                       NamedFilterList.NamedFilter namedFilter) {
        String text = new String(Arrays.copyOf(data, count));
        for (String target : namedFilter.getFilterTargets()) {
            text = text.replace(target, namedFilter.getFilterValue(target));
        }
        return text.trim().getBytes();
    }

    private static NamedFilterList.NamedFilter getFilter(NamedFilterList namedFilters,
                                                         ZipEntry entry) {
        NamedFilterList.NamedFilter filter = null;
        try {
            filter = namedFilters.getFilter(entry.getName());
        } catch (Exception e) {
            log.info("Filter not found: '" + entry.getName() + "'");
        }
        return filter;
    }

    private static File createTempFile() {
        try {
            return File.createTempFile("filtered", ".war");
        } catch (IOException e) {
            String msg = "Error creating tmp war file.";
            log.error(msg, e);
            throw new WebAppDeployerException(msg, e);
        }
    }
}
