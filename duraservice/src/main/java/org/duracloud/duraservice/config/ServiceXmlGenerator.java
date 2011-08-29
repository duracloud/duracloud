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

    private static final String SEP = File.separator;

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
        generateServiceXmlProfessional(dirPath);
        generateServiceXmlPreservation(dirPath);
        generateServiceXmlMedia(dirPath);
        generateServiceXmlTrial(dirPath);
    }

    private void generateServiceXmlProfessional(String dirPath)
        throws IOException {
        doGenerateServiceXml(dirPath, getRepositoryName(), getServices());
    }

    private void generateServiceXmlPreservation(String dirPath)
        throws IOException {
        doGenerateServiceXml(dirPath,
                             getRepositoryNamePreservation(),
                             getServicesPreservation());
    }

    private void generateServiceXmlMedia(String dirPath) throws IOException {
        doGenerateServiceXml(dirPath,
                             getRepositoryNameMedia(),
                             getServicesMedia());
    }

    private void generateServiceXmlTrial(String dirPath) throws IOException {
        doGenerateServiceXml(dirPath,
                             getRepositoryNameTrial(),
                             getServicesTrial());
    }

    private void doGenerateServiceXml(String dirPath,
                                      String repoName,
                                      List<ServiceInfo> services)
        throws IOException {
        String filePath = dirPath + SEP + repoName + ".xml";
        System.out.println("Writing Services Xml File to: " + filePath);
        File servicesXmlFile = new File(filePath);
        FileUtils.writeStringToFile(servicesXmlFile,
                                    getServicesAsXml(services),
                                    "UTF-8");
    }

    private String getRepositoryName() {
        return new ServiceRegistryName(version).getName();
    }

    private String getRepositoryNamePreservation() {
        return new ServiceRegistryName(version).getNamePreservation();
    }

    private String getRepositoryNameMedia() {
        return new ServiceRegistryName(version).getNameMedia();
    }

    private String getRepositoryNameTrial() {
        return new ServiceRegistryName(version).getNameTrial();
    }

    private String getServicesAsXml(List<ServiceInfo> services) {
        return ServicesConfigDocument.getServiceListAsXML(services);
    }

    protected List<ServiceInfo> getServices() {
        List<ServiceInfo> servicesList = new ArrayList<ServiceInfo>();
        int index = 0;
        for (AbstractServiceInfo serviceInfo : serviceInfos) {
            servicesList.add(serviceInfo.getServiceXml(index++, version));
        }
        return servicesList;
    }

    private List<ServiceInfo> getServicesPreservation() {
        List<ServiceInfo> results = new ArrayList<ServiceInfo>();

        List<ServiceInfo> services = getServices();
        results.add(services.get(0));
        results.add(services.get(2));

        return results;
    }

    private List<ServiceInfo> getServicesMedia() {
        List<ServiceInfo> results = new ArrayList<ServiceInfo>();

        List<ServiceInfo> services = getServices();
        results.add(services.get(5));
        results.add(services.get(6));
        results.add(services.get(8));
        results.add(services.get(9));
        results.add(services.get(10));

        return results;
    }

    private List<ServiceInfo> getServicesTrial() {
        List<ServiceInfo> results = new ArrayList<ServiceInfo>();

        List<ServiceInfo> services = getServices();
        results.add(services.get(0));
        results.add(services.get(2));
        results.add(services.get(4));
        results.add(services.get(5));
        results.add(services.get(6));
        results.add(services.get(8));
        results.add(services.get(9));
        results.add(services.get(10));

        return results;
    }

    private static String usage() {
        StringBuilder sb = new StringBuilder();
        sb.append("----------------------------------\n");
        sb.append("Usage:");
        sb.append("\n");
        sb.append("ServiceXmlGenerator <version> [output-dir]");
        sb.append("\n\n");
        sb.append("Where <version> is the artifact version for all services.");
        sb.append("\n");
        sb.append("Examples: ");
        sb.append("\n\t");
        sb.append("ServiceXmlGenerator 0.7.0-SNAPSHOT");
        sb.append("\n\t");
        sb.append("ServiceXmlGenerator 0.7.0-SNAPSHOT /tmp/xml/");
        sb.append("\n\n");
        sb.append("----------------------------------\n");

        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1 && args.length != 2) {
            System.err.println(usage());
            System.exit(1);
        }

        String version = args[0];

        String outputDir;
        if (args.length == 2) {
            outputDir = args[1];
        } else {
            // current directory
            outputDir = new File(".").getCanonicalPath();
        }

        // make sure output directory exists
        File verifyOutputDir = new File(outputDir);
        if (!verifyOutputDir.exists()) {
            System.err.println("Output Directory does not exist: " + outputDir);
            System.exit(1);
        }

        ServiceXmlGenerator xmlGenerator = new ServiceXmlGenerator(version);
        xmlGenerator.generateServiceXml(outputDir);
    }

}
