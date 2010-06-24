/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util.metrics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.duracloud.common.util.metrics.Metric.MetricElement;

/**
 * This class aggregates Metrics into a hierarchy roughly similar to a call
 * stack.
 *
 * @author Andrew Woods
 */
public class MetricsTable {

    private final LinkedHashMap<Metric, Map<MetricElement, MetricsTable>> table;

    public MetricsTable() {
        table = new LinkedHashMap<Metric, Map<MetricElement, MetricsTable>>();
    }

    public void addMetric(Metric metric) {
        table.put(metric, new HashMap<MetricElement, MetricsTable>());
    }

    public void addSubMetric(Metric parent, MetricsTable subTable) {
        table.get(parent).put(parent.currentElement(), subTable);
    }

    public Iterator<Metric> getMetrics() {
        return table.keySet().iterator();
    }

    public Iterator<MetricElement> getElements(Metric parent) {
        return parent.getElements();
    }

    public MetricsTable getSubTable(Metric parent, MetricElement elem)
            throws MetricException {
        if (!table.containsKey(parent)) {
            throw new MetricException("No parent metric found: "
                    + parent.toString());
        }
        if (!table.get(parent).containsKey(elem)) {
            throw new MetricException("No element found for parent metric: "
                    + elem.toString() + " | " + parent.toString());
        }

        return table.get(parent).get(elem);
    }

}
