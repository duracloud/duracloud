/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.service;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.duracloud.syncui.config.SyncUIConfig;
import org.duracloud.syncui.domain.SyncProcessState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Represents the persistent runtime state of the process.
 * @author Daniel Bernstein
 *
 */
public class RuntimeStateMemento {
    private static Logger log =
        LoggerFactory.getLogger(RuntimeStateMemento.class);
    private SyncProcessState syncProcessState;

    public SyncProcessState getSyncProcessState() {
        return syncProcessState;
    }

    public void setSyncProcessState(SyncProcessState syncProcessState) {
        this.syncProcessState = syncProcessState;
    }
    
    
    private static File getStateFile() {
        File workDir = SyncUIConfig.getWorkDir();
        File stateFile = new File(workDir, ".runtime-state.xml");
        return stateFile;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
    
    public static RuntimeStateMemento get() {
        File stateFile = getStateFile();
        if(stateFile.exists()){
            try (InputStream is = new FileInputStream(stateFile)) {
                log.debug("retrieving state from {}",
                          stateFile.getAbsolutePath());
                XStream xstream = new XStream();
                //FileInputStream fis = new FileInputStream(stateFile);
                return (RuntimeStateMemento)xstream.fromXML(is);
            } catch (IOException e) {
                //should never happen
                log.error("Failed to persist internal state: " +
                          "should never happen", e);
                System.exit(1);
                return null;
            }
        }else{
            log.debug("not state file found at {}: creating new memento",
                      stateFile.getAbsolutePath());
            return new RuntimeStateMemento();
        }
    }
    
    public static void persist(RuntimeStateMemento state) {
        File stateFile = RuntimeStateMemento.getStateFile();
        try (OutputStream os = new FileOutputStream(stateFile)) {
            new XStream().toXML(state, os);
            log.debug("successfully saved {} to {}",
                      state,
                      stateFile.getAbsolutePath());
        } catch (IOException e) {
            //should never happen
            log.error("Failed to persist internal state: " +
                      "should never happen", e);
            System.exit(1);
        }
    }
}
