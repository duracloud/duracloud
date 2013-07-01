package org.duracloud.syncui.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.duracloud.syncui.config.SyncUIConfig;
import org.duracloud.syncui.domain.SyncProcessState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

/**
 * Represents the persistent runtime state of the process.
 * @author Daniel Bernstein
 *
 */
public class RuntimeStateMemento {
    private static Logger log = LoggerFactory.getLogger(RuntimeStateMemento.class);
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
        try {
            File stateFile = getStateFile();
            if(stateFile.exists()){
                log.debug("retrieving state from {}", stateFile.getAbsolutePath());
                XStream xstream = new XStream();
                FileInputStream fis = new FileInputStream(stateFile);
                return (RuntimeStateMemento)xstream.fromXML(fis);
            }else{
                log.debug("not state file found at {}: creating new memento", stateFile.getAbsolutePath());
                return new RuntimeStateMemento();
            }
        } catch (IOException e) {
            //should never happen
            log.error("Failed to persist internal state: should never happen", e);
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    
    public static void persist(RuntimeStateMemento state) {
        try {
            File stateFile = RuntimeStateMemento.getStateFile();
            new XStream().toXML(state, new FileOutputStream(stateFile));
            log.debug("successfully saved {} to {}",state, stateFile.getAbsolutePath());

        } catch (IOException e) {
            //should never happen
            log.error("Failed to persist internal state: should never happen", e);
            e.printStackTrace();
            System.exit(1);
        }
    }
}
