/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.unittestdb.domain;

import org.duracloud.common.model.DuraCloudUserType;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.unittestdb.error.UnknownResourceTypeException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class creates a virtual union enum from StorageProviderType and
 * DuraCloudUserType.
 *
 * @author Andrew Woods
 *         Date: Mar 15, 2010
 */
public class ResourceType {

    private String id;

    private ResourceType(String id) {
        this.id = id;
    }

    /**
     * This method returns a ResourceType from the arg type id.
     * Allowable types are the names from the two enums:
     * - StorageProviderType
     * - DuraCloudUserType
     *
     * @param type id for ResourceType
     * @return ResourceType corresponding to the arg type id.
     * @throws UnknownResourceTypeException if arg type id not recognized.
     */
    public static ResourceType fromString(String type) {
        // check if arg is a StorageProviderType
        for (StorageProviderType pType : StorageProviderType.values()) {
            if (pType.toString().equalsIgnoreCase(type) ||
                pType.name().equalsIgnoreCase(type)) {
                return new ResourceType(type);
            }
        }

        // check if arg is a DuraCloudUserType
        for (DuraCloudUserType uType : DuraCloudUserType.values()) {
            if (uType.toString().equalsIgnoreCase(type) ||
                uType.name().equalsIgnoreCase(type)) {
                return new ResourceType(type);
            }
        }

        throw new UnknownResourceTypeException(type);
    }

    public static ResourceType fromStorageProviderType(StorageProviderType type) {
        return new ResourceType(type.toString());
    }

    public static ResourceType fromDuraCloudUserType(DuraCloudUserType type) {
        return new ResourceType(type.toString());
    }

    /**
     * This method returns the union of StorageProviderTypes and
     * DuraCloudUserTypes.
     *
     * @return list of types union
     */
    public static List<ResourceType> values() {
        List<ResourceType> types = new ArrayList<ResourceType>();
        for (StorageProviderType type : StorageProviderType.values()) {
            types.add(new ResourceType(type.toString()));
        }

        for (DuraCloudUserType type : DuraCloudUserType.values()) {
            types.add(new ResourceType(type.toString()));
        }

        return types;
    }

    public String toString() {
        return id;
    }

}
