/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

/**
 * This class holds timing metrics for a set of related events (MetricElements).
 *
 * @author Andrew Woods
 */
public class Metric {

    private final Logger log = LoggerFactory.getLogger(Metric.class);

    private final String header;

    private final LinkedList<MetricElement> elements;

    private final Stack<MetricElement> timerStack;

    public class MetricElement {

        private final String subHeader;

        private volatile long startTime;

        private volatile long stopTime;

        private MetricElement(String subHeader) {
            this.subHeader = subHeader;
        }

        public void start() {
            startTime = System.currentTimeMillis();
        }

        public void stop() {
            stopTime = System.currentTimeMillis();
        }

        public float elapsedSecs() {
            return (stopTime - startTime) / 1000f;
        }

        public String getSubHeader() {
            return subHeader;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("MetricElement: " + subHeader);
            sb.append("[" + startTime + " to " + stopTime + "]");
            return sb.toString();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Metric: " + header);
        sb.append(" [");
        Iterator<MetricElement> elems = getElements();
        while (elems.hasNext()) {
            sb.append(elems.next().getSubHeader() + ", ");
        }

        int len = sb.length();
        if (sb.charAt(len - 1) != '[') {
            sb.replace(len - 2, len, "]");
        }
        sb.append("\n");

        return sb.toString();
    }

    public Metric(String header, String subHeader) {
        this.header = header;
        this.elements = new LinkedList<MetricElement>();
        this.timerStack = new Stack<MetricElement>();

        MetricElement elem = new MetricElement(subHeader);
        this.elements.add(elem);
        this.timerStack.push(elem);
    }

    public void start(String name) {
        MetricElement elem = timerStack.peek();
        if (!name.equals(elem.getSubHeader())) {
            String msg =
                    "Element to start does not match top of stack: " + name
                            + ", " + elem.getSubHeader();
            throw new RuntimeException(new MetricException(msg));
        }
        elem.start();
    }

    public void stop(String name) {
        MetricElement elem = timerStack.pop();
        while (elem != null && !name.equals(elem.getSubHeader())) {
            elem.stop();
            elem = timerStack.pop();
        }

        if (elem == null) {
            String msg = "Element to stop not found on stack: " + name;
            throw new RuntimeException(new MetricException(msg));
        }
        elem.stop();
    }

    public void addElement(String subHeader) {
        MetricElement elem = new MetricElement(subHeader);
        elements.add(elem);
        timerStack.push(elem);
    }

    public String getHeader() {
        return header;
    }

    public String getSubHeader() {
        return currentElement().getSubHeader();
    }

    public Iterator<MetricElement> getElements() {
        return elements.iterator();
    }

    protected MetricElement currentElement() {
        return elements.getLast();
    }

}
