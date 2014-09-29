/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and service online at
 *
 *     http://duracloud.org/license/
 */

import org.duracloud.client.report.StorageReportManager;
import org.duracloud.client.report.StorageReportManagerImpl;
import org.duracloud.common.model.Credential;
import org.duracloud.reportdata.storage.StorageReport;
import org.duracloud.reportdata.storage.StorageReportInfo;
import org.duracloud.reportdata.storage.metrics.SpaceMetrics;
import org.duracloud.reportdata.storage.metrics.StorageMetrics;
import org.duracloud.reportdata.storage.metrics.StorageProviderMetrics;

import java.lang.System;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Example code which connects to the DuraCloud DuraBoss reporting REST API
 * by using the ReportClient.
 *
 * @author Bill Branan
 * Date: 6/2/11
 */
public class ExampleReportClient {

    private static final String USERNAME = "user";  // replace as necessary
    private static final String PASSWORD = "upw";   // replace as necessary
    private static final String HOST = "localhost"; // replace as necessary
    private static final String PORT = "8080";      // replace as necessary
    private static final String CONTEXT = "duraboss";

    private StorageReportManager storageReportManager;

    public ExampleReportClient() {
        storageReportManager = new StorageReportManagerImpl(HOST, PORT, CONTEXT);
        storageReportManager.login(new Credential(USERNAME, PASSWORD));
    }

    public void runExample() throws Exception {
        runStorageExample();
    }

    private void runStorageExample() throws Exception {
        StorageReportInfo reportInfo =
            storageReportManager.getStorageReportInfo();
        System.out.println("Current Storage Report Status: " + reportInfo.getStatus());

        List<String> reports = storageReportManager.getStorageReportList();
        System.out.println("Available Storage Reports:");
        for(String report : reports) {
            System.out.println("  " + report);
        }

        if(reports.size() > 0) {
            StorageReport report =
                storageReportManager.getLatestStorageReport();

            System.out.println("Latest Storage Report:");
            Date completionDate = new Date(report.getCompletionTime());
            System.out.println("  Completed At: " + completionDate.toString());
            long minElapsed = report.getElapsedTime() / 60000;
            System.out.println("  Minutes To Complete: " + minElapsed);

            StorageMetrics metrics = report.getStorageMetrics();
            System.out.println("  Total Item Count: " +
                               metrics.getTotalItems());
            System.out.println("  Total Size (bytes): " +
                               metrics.getTotalSize());

            List<StorageProviderMetrics> spMetrics =
                metrics.getStorageProviderMetrics();
            for(StorageProviderMetrics provider : spMetrics) {
                String providerType = provider.getStorageProviderType();
                System.out.println();
                System.out.println("  Storage Provider Type: " + providerType);
                System.out.println("    Total Item Count: " +
                                   provider.getTotalItems());
                System.out.println("    Total Size (bytes): " +
                                   provider.getTotalSize());

                List<SpaceMetrics> spaceMetrics = provider.getSpaceMetrics();
                for(SpaceMetrics space : spaceMetrics) {
                    System.out.println("    Space Name: " +
                                       space.getSpaceName());
                    System.out.println(
                        "      Total Item Count: " + space.getTotalItems());
                    System.out.println(
                        "      Total Size (bytes): " + space.getTotalSize());
                }
            }
        }
    }

    /**
     * This is the main method that runs the example client.
     *
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        ExampleReportClient reportClient = new ExampleReportClient();

        try {
            reportClient.runExample();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}