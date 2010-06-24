/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util.metrics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Date;
import java.util.Formatter;
import java.util.Iterator;

import org.duracloud.common.util.metrics.Metric.MetricElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class writes the contents of a MetricsTable to the provided
 * metricsFileName.
 *
 * @author Andrew Woods
 */
public class MetricsReport {

    protected final Logger log = LoggerFactory.getLogger(MetricsReport.class);

    private final String title;

    private final BufferedWriter writer;

    private final Formatter formatter;

    private final int LINE_WIDTH = 80;

    final int LEVEL_0 = 0;

    public MetricsReport(String title, String metricsFileName)
            throws IOException {
        this.title = title;
        writer = new BufferedWriter(new FileWriter(new File(metricsFileName)));
        formatter = new Formatter(writer);
    }

    public void writeReport(MetricsTable table) {
        writeProlog();
        writeMetrics(table, LEVEL_0);

        formatter.flush();
        formatter.close();
    }

    private void writeMetrics(MetricsTable table, int level) {
        Iterator<Metric> metrics = table.getMetrics();
        while (metrics.hasNext()) {
            Metric parent = metrics.next();
            writeMetricHeader(parent, level);

            Iterator<MetricElement> elems = parent.getElements();
            while (elems.hasNext()) {
                MetricElement elem = elems.next();
                writeMetricElement(elem, level + 1);
                try {
                    writeMetrics(table.getSubTable(parent, elem), level + 1);
                } catch (MetricException e) {
                    log.debug("Error writing metrics", e);
                }
            }
            separator('-');
        }
    }

    private void writeProlog() {
        separator('=');
        String text = "Test metrics for: " + title;
        formatter.format("%s", text);

        int width = this.LINE_WIDTH - text.length();
        formatter.format("%1$" + width + "tc%n", new Date());
        separator('=');
    }

    private void writeMetricHeader(Metric metric, int level) {
        if (level == LEVEL_0) {
            String text = "Measuring: " + metric.getHeader();
            formatter.format("%n%n%s", text);
            int width = this.LINE_WIDTH - text.length();
            formatter.format("%1$" + width + "s%n", "elapsed secs");
        }

        separator('-');
    }

    private void writeMetricElement(MetricElement elem, int level) {
        String text = prefix('.', level) + elem.getSubHeader();
        formatter.format("%1$s", text);

        int width = this.LINE_WIDTH - text.length();
        formatter.format("%1$" + width + ".3f%n", elem.elapsedSecs());
    }

    private void separator(char c) {
        StringBuffer sep = new StringBuffer();
        for (int i = 0; i < LINE_WIDTH; ++i) {
            sep.append(c);
        }
        formatter.format("%s%n", sep);
    }

    private String prefix(char c, int times) {
        StringBuffer pre = new StringBuffer();
        for (int i = 0; i < times * 2; ++i) {
            pre.append(c);
        }
        return pre.toString();
    }
}
