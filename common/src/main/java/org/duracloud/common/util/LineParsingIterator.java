/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * This class iterates over and parses the lines of a stream.
 * By default it parses tab separated lines.
 *
 * @author Daniel Bernstein
 */
public class LineParsingIterator implements Iterator<List<String>> {
    private BufferedReader reader;
    private String currentLine = null;
    private Parseable parsingStrategy;

    private static final Parseable TAB_SEPARATED_STRATEGY;

    static {
        TAB_SEPARATED_STRATEGY = new Parseable() {
            @Override
            public List<String> parseLine(String line) {
                return Arrays.asList(line.split("[\t]"));
            }
        };
    }

    public LineParsingIterator(Reader reader) {
        this(reader, TAB_SEPARATED_STRATEGY);
    }

    public LineParsingIterator(Reader reader, Parseable parsingStrategy) {
        this.reader = new BufferedReader(reader);

        if (parsingStrategy == null) {
            throw new IllegalArgumentException("parsingStrategy must not be null");
        }

        this.parsingStrategy = parsingStrategy;

        readNextLine();
    }

    private void readNextLine() {
        try {
            this.currentLine = this.reader.readLine();
        } catch (IOException e) {
            this.currentLine = null;
            e.printStackTrace();
        }
    }

    private List<String> parseLine(String line) {
        return this.parsingStrategy.parseLine(line);
    }

    @Override
    public final List<String> next() {
        List<String> object = parseLine(this.currentLine);
        readNextLine();
        return object;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean hasNext() {
        return currentLine != null;
    }

    public static interface Parseable {
        List<String> parseLine(String line);
    }

}