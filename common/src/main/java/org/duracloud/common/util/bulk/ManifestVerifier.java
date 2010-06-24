/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util.bulk;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.duracloud.common.error.ManifestVerifyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class compares two manifest files for
 * equal size and
 * checksum/entryname mappings
 * The expected format of the input files is
 * <checksum><whitespace><entryname>
 *
 * @author Andrew Woods
 *         Date: Oct 24, 2009
 */
public class ManifestVerifier {

    private final Logger log = LoggerFactory.getLogger(ManifestVerifier.class);

    private File file0;
    private File file1;

    private Map<String, String> entries0; // filename -> checksum
    private Map<String, String> entries1;

    private List<String> filters;

    private Map<String, ResultEntry> results; // filename -> resultEntry

    public ManifestVerifier(File file0, File file1) {
        this.file0 = file0;
        this.file1 = file1;
        entries0 = new HashMap<String, String>();
        entries1 = new HashMap<String, String>();
        filters = new ArrayList<String>();
        results = new HashMap<String, ResultEntry>();
    }

    public void report(OutputStream out) {
        StringBuilder sb = new StringBuilder();
        String cksum0 = "0:" + FilenameUtils.getName(file0.getName());
        String cksum1 = "1:" + FilenameUtils.getName(file1.getName());
        sb.append("title,file," + cksum0 + "," + cksum1 + ",state");
        sb.append("\n");

        for (ResultEntry result : results.values()) {
            sb.append(result.getTitle());
            sb.append(",");
            sb.append(result.getFile());
            sb.append(",");
            sb.append(result.getChecksum0());
            sb.append(",");
            sb.append(result.getChecksum1());
            sb.append(",");
            sb.append(result.getState());
            sb.append("\n");
        }

        try {
            out.write(sb.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method checks the provided manifest files for:
     * same number of manifest entries
     * equal checksums per entry
     *
     * @param filters List of names that if found in the manifests will be ignored.
     * @throws ManifestVerifyException if files differ in size or checksums
     */
    public void verify(String... filters) throws ManifestVerifyException {
        if (filters != null) {
            this.filters = Arrays.asList(filters);
            logFilters();
        }
        verify();
    }

    /**
     * This method checks the provided manifest files for:
     * same number of manifest entries
     * equal checksums per entry
     *
     * @throws ManifestVerifyException if files differ in size or checksums
     */
    public void verify() throws ManifestVerifyException {
        loadEntries();
        verifyFiles();
    }

    private void loadEntries() {
        loadEntries(file0, entries0);
        loadEntries(file1, entries1);
    }

    private void loadEntries(File file, Map<String, String> entries) {
        InputStream input = getInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(input));

        String line = readLine(br);
        while (line != null) {
            if (!isFiltered(line)) {
                addEntry(line, entries);
            }
            line = readLine(br);
        }
    }

    private boolean isFiltered(String line) {
        for (String filter : filters) {
            if (line.indexOf(filter) != -1) {
                return true;
            }
        }
        return false;
    }

    private void addEntry(String line, Map<String, String> entries) {
        String[] cksumFilenamePair = line.split("\\s");
        if (cksumFilenamePair == null || cksumFilenamePair.length != 2) {
            throw new RuntimeException("Invalid manifest file.");
        }

        entries.put(cksumFilenamePair[1], cksumFilenamePair[0]);
    }

    private InputStream getInputStream(File file) {
        try {
            return new AutoCloseInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private String readLine(BufferedReader br) {
        try {
            return br.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void verifyFiles() throws ManifestVerifyException {
        boolean hasErrors = entries0.size() != entries1.size();

        // Load the first set
        for (String name : entries0.keySet()) {
            ResultEntry entry = new ResultEntry();
            entry.setTitle(titleOf(name));
            entry.setFile(fileOf(name));
            entry.setChecksum0(entries0.get(name));
            entry.setState(State.MISSING_FROM_1);
            results.put(name, entry);
        }

        // Fill in from the second set
        for (String name : entries1.keySet()) {
            ResultEntry entry = results.get(name);

            // Entry not loaded from first set
            if (null == entry) {
                hasErrors = true;

                entry = new ResultEntry();
                entry.setTitle(titleOf(name));
                entry.setFile(fileOf(name));
                entry.setChecksum1(entries1.get(name));
                entry.setState(State.MISSING_FROM_0);
                results.put(name, entry);

                // Entry found in both sets
            } else {
                entry.setChecksum1(entries1.get(name));
                if (entry.getChecksum0().equals(entry.getChecksum1())) {
                    entry.setState(State.VALID);
                } else {
                    hasErrors = true;
                    entry.setState(State.MISMATCH);
                }
            }
        }

        if (hasErrors) {
            throw new ManifestVerifyException("Manifests do not match.");
        }

    }

    private String titleOf(String name) {
        String pre = "data/";
        int prefixIndex = name.startsWith(pre) ? pre.length() : 0;
        int suffixIndex = name.lastIndexOf('/');
        return name.substring(prefixIndex, suffixIndex);
    }

    private String fileOf(String name) {
        return FilenameUtils.getName(name);
    }

    private void logFilters() {
        StringBuilder sb = new StringBuilder();
        if (filters.size() > 0) {
            sb.append("Filters: [");

            for (String filter : filters) {
                sb.append("|" + filter);
            }
            sb.append("|]");
        } else {
            sb.append("NO-FILTERS");
        }

        log.info(sb.toString());
    }


    private class ResultEntry {
        private String title;
        private String file;
        private String checksum0 = "";
        private String checksum1 = "";
        private State state;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getChecksum0() {
            return checksum0;
        }

        public void setChecksum0(String checksum0) {
            this.checksum0 = checksum0;
        }

        public String getChecksum1() {
            return checksum1;
        }

        public void setChecksum1(String checksum1) {
            this.checksum1 = checksum1;
        }

        public State getState() {
            return state;
        }

        public void setState(State state) {
            this.state = state;
        }
    }

    private enum State {
        VALID, MISMATCH, MISSING_FROM_0, MISSING_FROM_1
    }

}
