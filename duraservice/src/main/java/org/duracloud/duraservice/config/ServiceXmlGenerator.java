/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.config;

import org.apache.commons.io.FileUtils;
import org.duracloud.common.model.ServiceRegistryName;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.ServicesConfigDocument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Bill Branan
 * Date: Nov 20, 2009
 */
public class ServiceXmlGenerator {

    /**
     * Add new service to this list and it will be included in the master
     * repository XML service configuration file.
     */
    private AbstractServiceInfo[] serviceInfos = {//new HelloServiceInfo(),
                                                new FixityServiceInfo(),
                                                new AmazonFixityServiceInfo(),
                                                new FixityToolsServiceInfo(),
                                                new ReplicationOnDemandServiceInfo(),
                                                new DuplicationServiceInfo(),
                                                //new HelloWebappWrapperServiceInfo(),
                                                new J2kServiceInfo(),
                                                new ImageConversionServiceInfo(),
                                                new BulkImageConversionServiceInfo(),
                                                new MediaStreamingServiceInfo(),
                                                new ImageMagickServiceInfo(),
                                                new WebappUtilServiceInfo()};

    private String version;

    public ServiceXmlGenerator(String version) {
        this.version = version;
    }

    /**
     * Creates service xml and writes it to a file in the directory
     * indicated by dirPath.
     *
     * @param dirPath the full path of the directory in which to write the file
     * @throws IOException
     */
    public void generateServiceXml(String dirPath) throws IOException {
        String sep = File.separator;
        String filePath = dirPath + sep + getRepositoryName() + ".xml";
        System.out.println("Writing Services Xml File to: " + filePath);
        File servicesXmlFile = new File(filePath);
        FileUtils.writeStringToFile(servicesXmlFile,
                                    getServicesListAsXml(),
                                    "UTF-8");
    }

    private String getRepositoryName() {
        return new ServiceRegistryName(version).getName();
    }

    private String getServicesListAsXml() {
        List<ServiceInfo> services = buildServiceList();
        ServicesConfigDocument configDoc = new ServicesConfigDocument();
        return configDoc.getServiceListAsXML(services);
    }

    protected List<ServiceInfo> buildServiceList() {
        List<ServiceInfo> servicesList = new ArrayList<ServiceInfo>();
        int index = 0;
        for (AbstractServiceInfo serviceInfo : serviceInfos) {
            servicesList.add(serviceInfo.getServiceXml(index++, version));
        }
        return servicesList;
    }

    private static String usage() {
        StringBuilder sb = new StringBuilder();
        sb.append("----------------------------------\n");
        sb.append("Usage:");
        sb.append("\n");
        sb.append("ServiceXmlGenerator <version>");
        sb.append("\n\n");
        sb.append("Where <version> is the artifact version for all services.");
        sb.append("\n");
        sb.append("Example: ");
        sb.append("\n\t");
        sb.append("ServiceXmlGenerator 0.7.0-SNAPSHOT");
        sb.append("\n\n");
        sb.append("----------------------------------\n");

        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println(usage());
            System.exit(1);
        }

        String version = args[0];
        String currentDir = new File(".").getCanonicalPath();
        
        ServiceXmlGenerator xmlGenerator = new ServiceXmlGenerator(version);
        xmlGenerator.generateServiceXml(currentDir);
    }

}
