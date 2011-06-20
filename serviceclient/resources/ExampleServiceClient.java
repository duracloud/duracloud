/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and service online at
 *
 *     http://duracloud.org/license/
 */

import org.duracloud.client.ServicesManager;
import org.duracloud.client.ServicesManagerImpl;
import org.duracloud.client.error.ServicesException;
import org.duracloud.common.model.Credential;
import org.duracloud.common.util.DateUtil;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.user.Option;
import org.duracloud.serviceconfig.user.SingleSelectUserConfig;
import org.duracloud.serviceconfig.user.TextUserConfig;
import org.duracloud.serviceconfig.user.UserConfig;
import org.duracloud.serviceconfig.user.UserConfigMode;
import org.duracloud.serviceconfig.user.UserConfigModeSet;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Example code which connects to the DuraCloud DuraService REST API by using
 * the ServiceClient.
 *
 * @author Andrew Woods
 */
public class ExampleServiceClient {

    private static final String USERNAME = "user";  // replace as necessary
    private static final String PASSWORD = "upw";   // replace as necessary
    private static final String HOST = "localhost"; // replace as necessary
    private static final String PORT = "8080";      // replace as necessary
    private static final String CONTEXT = "duraservice";

    private ServicesManager servicesManager;

    public ExampleServiceClient() {
        servicesManager = new ServicesManagerImpl(HOST, PORT, CONTEXT);
        servicesManager.login(new Credential(USERNAME, PASSWORD));
    }

    /**
     * This method executes the complete cycle of the bit-integrity-service,
     * from deployment through undeployment.
     *
     * @param space over which service will be run
     * @throws Exception
     */
    public void runBitIntegrityOverSpace(String space) throws Exception {
        // Find all available services.
        List<ServiceInfo> services = getAvailableServices();

        // From available services, select out the bit-integrity-service.
        ServiceInfo service = getBitIntegrityService(services);

        // Get valid space options for the bit-integrity-service.
        List<String> spaces = getSpaceOptions(service);

        // Validate that the target space is a valid option.
        validateTargetSpace(space, spaces);

        // Deploy the service.
        int deploymentId = deployService(service, space);

        // Spin on the service, displaying status, until it has completed.
        monitorService(service.getId(), deploymentId);

        // Undeploy the service.
        undeployService(service.getId(), deploymentId);

        // Display the location of results.
        outputResults(space);
    }

    /**
     * This method retrieves the listing of all available services.
     *
     * @return listing of available services
     * @throws ServicesException
     */
    private List<ServiceInfo> getAvailableServices() throws ServicesException {
        List<ServiceInfo> services = servicesManager.getAvailableServices();

        System.out.println("- Available services:");
        for (ServiceInfo service : services) {
            System.out.printf("%1$2d: %2$s  %n",
                              service.getId(),
                              service.getDisplayName());
        }
        return services;
    }

    /**
     * This method finds the bit-integrity-service from within the listing of
     * all available services.
     *
     * @param services listing of all available services
     * @return the bit-integrity-service or throw a runtime exception
     */
    private ServiceInfo getBitIntegrityService(List<ServiceInfo> services) {
        for (ServiceInfo service : services) {
            String contentId = service.getContentId();
            if (contentId.startsWith("fixityservice")) {
                return service;
            }
        }
        throw new RuntimeException("Bit Integrity Service not found!");
    }

    /**
     * This method illustrates how the service-config-definintion can be used
     * to discover various service configuration options.
     * In the case of determining spaces, it would normally be more expedient
     * to make direct calls to durastore via the storeclient.
     * <p>
     * Also note, the hard-coded strings are found as Enums across the
     * - fixityservice &
     * - storageprovider
     * projects.
     * </p>
     * ...and yes, the nesting is crazy.
     *
     * @param service from which to gather space options
     * @return list of spaces
     */
    private List<String> getSpaceOptions(ServiceInfo service) {
        List<String> spaces = new ArrayList<String>();
        for (UserConfigModeSet modeSet : service.getUserConfigModeSets()) {

            for (UserConfigMode mode : modeSet.getModes()) {
                if ("all-in-one-for-space".equals(mode.getName())) {

                    for (UserConfigModeSet subModeSet : mode.getUserConfigModeSets()) {
                        if ("storeId".equals(subModeSet.getName())) {

                            for (UserConfigMode subMode : subModeSet.getModes()) {
                                if ("AMAZON_S3".equals(subMode.getDisplayName())) {

                                    for (UserConfig config : subMode.getUserConfigs()) {
                                        if ("targetSpaceId".equals(config.getName())) {

                                            SingleSelectUserConfig ssConfig = (SingleSelectUserConfig) config;
                                            for (Option opt : ssConfig.getOptions()) {
                                                spaces.add(opt.getValue());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return spaces;
    }

    /**
     * This method validates the target space is an actual space within the
     * user's duracloud account.
     *
     * @param target space to be validated
     * @param spaces listing of all valid spaces
     */
    private void validateTargetSpace(String target, List<String> spaces) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream printer = new PrintStream(out);

        if (null == target || !spaces.contains(target)) {
            String classname = this.getClass().getName();
            printer.printf("%n%n----------- UnSuccessful Run ------------%n%n");
            printer.printf("Usage: %1$s <target-space>%n%n", classname);
            printer.printf("Invalid target space: '%1$s' %n%n", target);
            printer.printf("  Valid space options are:%n");
            for (String space : spaces) {
                printer.printf("    %1$s%n", space);
            }
            printer.printf("%n%n----------- UnSuccessful Run ------------%n%n");
            printer.flush();
            throw new RuntimeException(out.toString());
        }
    }

    /**
     * This method deploys the service.
     *
     * @param service to deploy
     * @param space   over which service will be run
     * @return deployment-id of newly deployed service
     * @throws Exception
     */
    private int deployService(ServiceInfo service, String space)
        throws Exception {
        System.out.printf("%n- Deploying service%n");
        return servicesManager.deployService(service.getId(),
                                             null,
                                             service.getUserConfigVersion(),
                                             createDeploymentConfig(space));
    }

    /**
     * This method constructs a set of configuration elements needed to run the
     * bit-integrity-service.
     * <p>
     * Note, as with the getSpaceOptions() method above, the hard-coded strings
     * are found as Enums in the
     * - fixityservice
     * project.
     * </p>
     *
     * @param space over which service will be run
     * @return
     */
    private List<UserConfigModeSet> createDeploymentConfig(String space) {
        List<UserConfigModeSet> userConfigModeSets = new ArrayList<UserConfigModeSet>();

        List<UserConfig> configs = new ArrayList<UserConfig>();
        configs.add(new TextUserConfig("mode", "", "all-in-one-for-space"));
        configs.add(new TextUserConfig("storeId", "", "0"));
        configs.add(new TextUserConfig("targetSpaceId", "", space));

        UserConfigModeSet modeSet = new UserConfigModeSet(configs);
        userConfigModeSets.add(modeSet);

        return userConfigModeSets;
    }

    /**
     * This method spins on the running service, displaying its current status,
     * until it has completed.
     * <p>
     * Note, as with the getSpaceOptions() method above, the hard-coded strings
     * are found as Enums in the
     * - fixityservice
     * project.
     * </p>
     *
     * @param serviceId    of running service
     * @param deploymentId of running service
     * @throws Exception
     */
    private void monitorService(int serviceId, int deploymentId)
        throws Exception {
        System.out.printf("%n- Monitoring service%n");

        Map<String, String> serviceProps;
        for (int i = 0; i < 10; ++i) {
            serviceProps = servicesManager.getDeployedServiceProps(serviceId,
                                                                   deploymentId);

            String status = serviceProps.get("processing-status");
            if (null != status) {
                System.out.printf("  %1$s%n", status);

                if (status.startsWith("CompareHashes.COMPLETE")) {
                    System.out.println("  service completed.");
                    break;
                }
            }
            Thread.sleep(5000);
        }
    }

    /**
     * This method undeploys the service.
     *
     * @param serviceId    of running service
     * @param deploymentId of running service
     * @throws Exception
     */
    private void undeployService(int serviceId, int deploymentId)
        throws Exception {
        System.out.printf("%n- Undeploying service.%n");
        servicesManager.undeployService(serviceId, deploymentId);
    }

    /**
     * This method prints the duracloud URL of the resultant service report.
     *
     * @param space over which service was run
     */
    private void outputResults(String space) {
        String defaultOutputSpace = "x-service-out";

        StringBuilder sb = new StringBuilder("  http://");
        sb.append(HOST);
        sb.append(":");
        sb.append(PORT);
        sb.append("/durastore/");
        sb.append(defaultOutputSpace);
        sb.append("/bitintegrity/fixity-report-");
        sb.append(space);
        sb.append("-");
        sb.append(DateUtil.nowShort());
        sb.append(".csv");

        System.out.printf("%nFinal report found at: %n");
        System.out.println(sb);
    }

    /**
     * This is the main method that runs the example client.
     * It takes a single argument that is the space-name over which the
     * bit-integrity-service will be run.
     * By passing in zero arguments, the usage and valid spaces options will be
     * displayed.
     *
     * @param args array containing the single space-name element
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String space = args.length == 1 ? args[0] : null;

        ExampleServiceClient serviceClient = new ExampleServiceClient();

        try {
            serviceClient.runBitIntegrityOverSpace(space);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}