/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.contentindex.client;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.rules.ExternalResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

/**
 * A JUnit ExternalResource for running an embedded ElasticSearch server node.
 * Borrowed from: "http://blog.florian-hopf.de/2013/01/junit-rule-for-elasticsearch.html"
 * @author Erik Paulsson
 *         Date: 3/12/14
 */
public class ElasticSearchTestNode extends ExternalResource {

    private Node node;
    private Path dataDirectory;

    @Override
    protected void before() throws Throwable {
        try {
            dataDirectory = Files
                .createTempDirectory("es-test", new FileAttribute[]{});
            System.out.println("########## " + dataDirectory.toString());
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }

        ImmutableSettings.Builder elasticsearchSettings =
            ImmutableSettings.settingsBuilder()
                             .put("http.enabled", "true")
                             .put("path.data", dataDirectory.toString());

        node = NodeBuilder.nodeBuilder()
                          .local(true)
                          .settings(elasticsearchSettings.build())
                          .node();
    }

    @Override
    protected void after() {
        node.close();
        try {
            FileUtils.deleteDirectory(dataDirectory.toFile());
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public Client getClient() {
        return node.client();
    }
}