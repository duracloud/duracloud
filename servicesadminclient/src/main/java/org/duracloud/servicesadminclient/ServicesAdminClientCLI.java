/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesadminclient;

import org.duracloud.common.web.RestHttpHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Dec 18, 2009
 */
public class ServicesAdminClientCLI {

    private ServicesAdminClient client;
    private static String version;

    public static enum ADMIN_OPTION {
        INSTALL("i", "[i]nstall"),
        START("a", "st[a]rt"),
        PROPS("p", "[p]rops"),
        STOP("o", "st[o]p"),
        UNINSTALL("u", "[u]ninstall"),
        QUIT("q", "[q]uit"),
        UNKNOWN("?", "unknown");

        private String shorthand;
        private String description;

        private ADMIN_OPTION(String shorthand, String desc) {
            this.shorthand = shorthand;
            this.description = desc;
        }

        public String getDescription() {
            return description;
        }

        public static ADMIN_OPTION fromString(String s) {
            for (ADMIN_OPTION v : values()) {
                if (v.shorthand.equalsIgnoreCase(s)) {
                    return v;
                }
            }
            return UNKNOWN;
        }
    }

    public static enum SERVICE {
        HELLO("h", "[h]elloservice", "helloservice-" + version, "jar"),
        HELLOWEBAPP("hellow",
                    "[hellow]ebapp",
                    "hellowebappwrapper-" + version,
                    "zip"),
        REPLICATION("rep",
                    "[rep]licationservice",
                    "replicationservice-" + version,
                    "zip"),
        WEBAPPUTIL("util",
                   "webapp[util]",
                   "webapputilservice-" + version,
                   "zip"),
        J2K("j", "[j]2k", "j2kservice-" + version, "zip"),
        IMAGECONVERSION("i",
                        "[i]mageconversion",
                        "imageconversionservice-" + version,
                        "zip"),
        IMAGEMAGICK("magick",
                    "image[magick]",
                    "imagemagickservice-" + version,
                    "zip"),
        UNKNOWN("?", "unknown", "unknown-id", "no-ext");

        private File basePackageRepository = new File("../services/packages");

        private String shorthand;
        private String description;
        private String id;
        private String ext;

        private SERVICE(String shorthand, String desc, String id, String ext) {
            this.shorthand = shorthand;
            this.description = desc;
            this.id = id;
            this.ext = ext;
        }

        public String getDescription() {
            return description;
        }

        public String getServiceId() {
            return id;
        }

        public String getPackageName() {
            return getServiceId() + "." + getExt();
        }

        private String getExt() {
            return ext;
        }

        public File getPackage() {
            return new File(basePackageRepository, getPackageName());
        }

        public static SERVICE fromString(String s) {
            for (SERVICE v : values()) {
                if (v.shorthand.equalsIgnoreCase(s)) {
                    return v;
                }
            }
            return UNKNOWN;
        }

    }

    public ServicesAdminClientCLI(String host, String version) {
        System.out.println("constructor version-0: '"+version+"'");
        this.version = version;
        client = new ServicesAdminClient();
        client.setBaseURL(createBaseUrl(host));
        client.setRester(new RestHttpHelper());
    }

    private String createBaseUrl(String host) {
        // Port:8089 is defined in the 'tomcatconfig' project
        StringBuilder url = new StringBuilder("http://" + host);
        System.out.println("version: '"+version+"'");
        url.append(":8089/org.duracloud.services.admin_" + version.replace("-",
                                                                           "."));
        System.out.println("Using url: '" + url.toString() + "'");
        return url.toString();
    }

    private void inputCommands() {
        while (true) {
            ADMIN_OPTION option = inputOption();
            if (option.equals(ADMIN_OPTION.INSTALL)) {
                installService(inputService());
            } else if (option.equals(ADMIN_OPTION.START)) {
                startService(inputService());
            } else if (option.equals(ADMIN_OPTION.PROPS)) {
                getProps(inputService());
            } else if (option.equals(ADMIN_OPTION.STOP)) {
                stopService(inputService());
            } else if (option.equals(ADMIN_OPTION.UNINSTALL)) {
                uninstallService(inputService());
            } else if (option.equals(ADMIN_OPTION.QUIT)) {
                break;
            }
        }
    }

    private ADMIN_OPTION inputOption() {
        ADMIN_OPTION option = ADMIN_OPTION.UNKNOWN;
        while (option.equals(ADMIN_OPTION.UNKNOWN)) {
            StringBuilder sb = new StringBuilder("Enter option: ");
            String sep = " | ";
            for (ADMIN_OPTION value : ADMIN_OPTION.values()) {
                if (!value.equals(ADMIN_OPTION.UNKNOWN)) {
                    sb.append(value.getDescription());
                    sb.append(sep);
                }
            }
            sb.replace(sb.length() - sep.length(), sb.length(), " => ");
            System.out.println(sb.toString());
            option = ADMIN_OPTION.fromString(readLine().trim());
        }
        return option;
    }

    private void installService(SERVICE service) {
        try {
            client.postServiceBundle(service.getPackage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void startService(SERVICE service) {
        try {
            client.startServiceBundle(service.getServiceId());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void getProps(SERVICE service) {
        Map<String, String> props = null;
        try {
            props = client.getServiceProps(service.getServiceId());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        if (null == props) {
            System.out.println("Error: props is null.");
        } else {
            System.out.println(props.toString());
        }
    }

    private void stopService(SERVICE service) {
        try {
            client.stopServiceBundle(service.getServiceId());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void uninstallService(SERVICE service) {
        try {
            client.deleteServiceBundle(service.getPackageName());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private SERVICE inputService() {
        SERVICE service = SERVICE.UNKNOWN;
        while (service.equals(SERVICE.UNKNOWN)) {
            StringBuilder sb = new StringBuilder("Enter service: ");
            String sep = " | ";
            for (SERVICE value : SERVICE.values()) {
                if (!value.equals(SERVICE.UNKNOWN)) {
                    sb.append(value.getDescription());
                    sb.append(sep);
                }
            }

            sb.replace(sb.length() - sep.length(), sb.length(), " => ");
            System.out.println(sb.toString());
            service = SERVICE.fromString(readLine().trim());
        }


        return service;
    }

    private String readLine() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                System.in));
            return reader.readLine();
        } catch (Exception e) {
            throw new RuntimeException("Error: Unable to read from STDIN");
        }
    }

    private static void usage() {
        StringBuilder sb = new StringBuilder("Usage:");
        sb.append("ServicesAdminClientCLI [host] [service-version]\n");
        sb.append("\twhere [host] and [service-version] are optional");
        sb.append("\tbut [service-version] can not be present w/o [host].");
        System.out.println(sb.toString());
    }

    public static void main(String[] args) {
        if (args.length > 2 || (args.length == 1 &&
            (args[0].equals("help") || args[0].equals("-h")))) {
            usage();
            System.exit(1);
        }

        String host = "localhost";
        if (args.length >= 1) {
            host = args[0];
        }

        String version = "0.4.0-SNAPSHOT";
        if (args.length == 2) {
            version = args[1];
        }

        System.out.println("main version: '"+version+"'");
        ServicesAdminClientCLI cli = new ServicesAdminClientCLI(host, version);
        cli.inputCommands();
    }

}
