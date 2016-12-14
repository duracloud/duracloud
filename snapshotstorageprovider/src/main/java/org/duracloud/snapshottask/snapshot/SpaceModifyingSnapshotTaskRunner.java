/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Properties;

import org.duracloud.common.constant.Constants;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.model.AclType;
import org.duracloud.common.retry.Retriable;
import org.duracloud.common.retry.Retrier;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.util.IOUtil;
import org.duracloud.snapshotstorage.SnapshotStorageProvider;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.error.TaskException;
import org.duracloud.storage.provider.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author: Daniel Bernstein
 *          Date: 2/1/13
 */
public abstract class SpaceModifyingSnapshotTaskRunner extends AbstractSnapshotTaskRunner {

    private Logger log = LoggerFactory.getLogger(SpaceModifyingSnapshotTaskRunner.class);

    private StorageProvider snapshotProvider;
    private SnapshotStorageProvider unwrappedSnapshotProvider;
    private String dcSnapshotUser;

    public SpaceModifyingSnapshotTaskRunner(StorageProvider snapshotProvider,
                                    SnapshotStorageProvider unwrappedSnapshotProvider,
                                    String dcSnapshotUser,
                                    String bridgeAppHost,
                                    String bridgeAppPort,
                                    String bridgeAppUser,
                                    String bridgeAppPass) {
        super(bridgeAppHost, bridgeAppPort, bridgeAppUser, bridgeAppPass);
        this.snapshotProvider = snapshotProvider;
        this.unwrappedSnapshotProvider = unwrappedSnapshotProvider;
        this.dcSnapshotUser = dcSnapshotUser;
    }
    
    protected StorageProvider getStorageProvider(){
        return this.snapshotProvider;
    }


    protected String getSnapshotUser(){
        return this.dcSnapshotUser;
    }
    
    
    /*
     * Adds a snapshot ID property to the space
     */
    protected void addSnapshotIdToSpaceProps(String spaceId, String snapshotId) {
        Map<String, String> spaceProps =
            snapshotProvider.getSpaceProperties(spaceId);
        spaceProps.put(Constants.SNAPSHOT_ID_PROP, snapshotId);
        unwrappedSnapshotProvider.setNewSpaceProperties(spaceId, spaceProps);
    }
    
    
      /**
      * Stores a set of snapshot properties in the given space as a properties
      * file.
      *
      * @param spaceId the space in which the properties file should be stored
      * @param serializedProps properties in serialized format
      */
     protected void storeSnapshotProps(String spaceId, String serializedProps) {
         InputStream propsStream;
         try {
             propsStream = IOUtil.writeStringToStream(serializedProps);
         } catch(IOException e) {
             throw new TaskException("Unable to build stream from serialized " +
                                     "snapshot properties due to: " +
                                     e.getMessage());
         }
         ChecksumUtil checksumUtil = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
         String propsChecksum = checksumUtil.generateChecksum(serializedProps);
    
         snapshotProvider.addContent(spaceId,
                                     Constants.SNAPSHOT_PROPS_FILENAME,
                                     "text/x-java-properties",
                                     null,
                                     serializedProps.length(),
                                     propsChecksum,
                                     propsStream);
     }
    
     /**
      * Returns snapshot from the snapshot properties file if it exists
      *
      * @param spaceId
      * @return snapshot from the snapshot properties file if it exists, otherwise null
      */
     protected String getSnapshotIdFromProperties(String spaceId){
         Properties props = new Properties();
         try(InputStream is = this.snapshotProvider.getContent(spaceId, Constants.SNAPSHOT_PROPS_FILENAME)) {
            props.load(is);
            return props.getProperty(Constants.SNAPSHOT_ID_PROP);
        } catch(NotFoundException ex){
            return null;
        }catch (Exception e) {
            throw new TaskException( MessageFormat.format("Call to create snapshot failed, " +
                "unable to determine existence of snapshot properties file in {0}. " + 
                "Error: {1}",  spaceId, e.getMessage()));
        }
     }

     
    /*
     * Removes the snapshot ID property from a space
     */
    protected void removeSnapshotIdFromSpaceProps(String spaceId) {
        log.debug("Removing " + Constants.SNAPSHOT_ID_PROP +
                  " property from space " + spaceId);
        Map<String, String> spaceProps =
            snapshotProvider.getSpaceProperties(spaceId);
        if(spaceProps.remove(Constants.SNAPSHOT_ID_PROP) != null){
            unwrappedSnapshotProvider.setNewSpaceProperties(spaceId, spaceProps);
            log.info("Removed " + Constants.SNAPSHOT_ID_PROP +
                     " from  space properties for space " + spaceId);
        }else{
            log.debug("Property " + Constants.SNAPSHOT_ID_PROP +
                      " does not exist in space properties for " + spaceId +
                      ". No need to update space properties.");
        }
    }
    /*
     * Give the snapshot user the necessary permissions to pull content from
     * the snapshot space.
     */
    protected String setSnapshotUserPermissions(final String spaceId) {
        try {
            Retrier retrier = new Retrier();
            return retrier.execute(new Retriable() {
                @Override
                public String retry() throws Exception {
                    // The actual method being executed
                    Map<String, AclType> spaceACLs =
                        snapshotProvider.getSpaceACLs(spaceId);
                    spaceACLs.put(StorageProvider.PROPERTIES_SPACE_ACL +
                                  dcSnapshotUser, AclType.READ);
                    snapshotProvider.setSpaceACLs(spaceId, spaceACLs);
                    return spaceId;
                }
            });
        } catch(Exception e) {
            throw new TaskException("Unable to create snapshot, failed" +
                                    "setting space permissions due to: " +
                                    e.getMessage(), e);
        }
    }
    
    /*
     * Give the snapshot user the necessary permissions to pull content from
     * the snapshot space.
     */
    protected String removeSnapshotUserPermissions(final String spaceId) {
        try {
            Retrier retrier = new Retrier();
            return retrier.execute(new Retriable() {
                @Override
                public String retry() throws Exception {
                    // The actual method being executed
                    Map<String, AclType> spaceACLs =
                        snapshotProvider.getSpaceACLs(spaceId);
                    spaceACLs.remove(StorageProvider.PROPERTIES_SPACE_ACL +
                                  dcSnapshotUser);
                    snapshotProvider.setSpaceACLs(spaceId, spaceACLs);
                    return spaceId;
                }
            });
        } catch(Exception e) {
            throw new TaskException("Failed" +
                                    "to remove read only permissions " +
                                    "for snapshot user due to: " +
                                    e.getMessage(), e);
        }
    }

 
    protected void removeSnapshotProps(String spaceId) {
        snapshotProvider.deleteContent(spaceId, Constants.SNAPSHOT_PROPS_FILENAME);
    }

    /**
     * Checks if the snapshot props file is in the space.
     * @param spaceId
     * @return
     */
    protected boolean snapshotPropsPresentInSpace(String spaceId) {
        try {
            snapshotProvider.getContentProperties(spaceId, Constants.SNAPSHOT_PROPS_FILENAME);
            return true;
        }catch(NotFoundException ex){
            return false;
        }
    }
}
