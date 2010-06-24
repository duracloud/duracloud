/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.webapputil;

import org.duracloud.services.ComputeService;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * This interface abstracts the ability to (un)deploy a webapp to/from an
 * application server.
 *
 * @author Andrew Woods
 *         Date: Dec 7, 2009
 */
public interface WebAppUtil extends ComputeService {

    /**
     * This method deploys the arg war to a new application server under the
     * arg context.
     * The arg war inputstream is closed by this method.
     *
     * @param context
     * @param war
     * @return URL of running webapp
     */
    public URL deploy(String context, InputStream war);

    public URL deploy(String context, InputStream war, Map<String, String> env);

    /**
     * This method deploys the arg war to a newly created appserver under the
     * arg serviceId context.
     *
     * @param serviceId   is the name of the context of deployed webapp
     * @param war         to be deployed
     * @param env         of tomcat that will be installed/started
     * @param filterNames are names of files in the arg war to be filtered with
     *                    host and port. Any text in the named files with the
     *                    Strings $DURA_HOST$ or $DURA_PORT$ will be swapped
     *                    with the host and port of the compute instance.
     * @return URL of deployed webapp
     */
    public URL filteredDeploy(String serviceId,
                              InputStream war,
                              Map<String, String> env,
                              List<String> filterNames);

    /**
     * This method undeploys the webapp currently running at the arg url and
     * destroys the application server that was hosting it.
     *
     * @param url of webapp to undeploy
     */
    public void unDeploy(URL url);

}
