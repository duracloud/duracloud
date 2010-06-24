/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.computeprovider.domain;

public enum ComputeProviderType {
    AMAZON_EC2("amazon-ec2", "http://aws.amazon.com/ec2"),
    MICROSOFT_AZURE("ms-azure", "http://www.microsoft.com/azure"),
    RACKSPACE_CLOUDSERVERS("rackspace-cloudservers",
                           "http://www.rackspacecloud.com/cloud_hosting_products/servers"),
    EMC_ATMOS("emc-atmos",
              "http://www.emc.com/products/category/subcategory/cloud-infrastructure.htm"),
    LOCAL("local", "http://localhost:8080"),
    UNKNOWN("unknown", "http://www.google.com");

    private final String text;

    private String url;

    private ComputeProviderType(String pt, String url) {
        this.text = pt;
        this.url = url;
    }

    public static ComputeProviderType fromString(String pt) {
        for (ComputeProviderType pType : values()) {
            if (pType.text.equalsIgnoreCase(pt) ||
                pType.name().equalsIgnoreCase(pt)) {
                return pType;
            }
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return text;
    }

    public String getUrl() {
        return url;
    }

}