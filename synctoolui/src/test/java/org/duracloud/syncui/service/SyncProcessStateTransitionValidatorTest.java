/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.service;

import java.util.Arrays;
import java.util.List;

import org.duracloud.syncui.domain.SyncProcessState;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SyncProcessStateTransitionValidatorTest {
    private SyncProcessStateTransitionValidator v =
        new SyncProcessStateTransitionValidator();

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testValidate() {

        validate(SyncProcessState.STOPPED, SyncProcessState.STARTING);
        
        validate(SyncProcessState.STARTING,
                 SyncProcessState.RUNNING,
                 SyncProcessState.STOPPING,
                 SyncProcessState.ERROR);

        validate(SyncProcessState.RUNNING,
                 SyncProcessState.STOPPING,
                 SyncProcessState.PAUSING,
                 SyncProcessState.ERROR);

        validate(SyncProcessState.PAUSING,
                 SyncProcessState.PAUSED,
                 SyncProcessState.ERROR);

        validate(SyncProcessState.PAUSED,
                 SyncProcessState.RESUMING,
                 SyncProcessState.ERROR,
                 SyncProcessState.STOPPING);

        validate(SyncProcessState.RESUMING,
                 SyncProcessState.RUNNING,
                 SyncProcessState.ERROR);

        validate(SyncProcessState.STOPPING,
                 SyncProcessState.STOPPED,
                 SyncProcessState.ERROR);
    }

    private void validate(SyncProcessState from, SyncProcessState... to) {
        List<SyncProcessState> states =
            Arrays.asList(SyncProcessState.values());
        List<SyncProcessState> toStates = Arrays.asList(to);

        // for each possible state
        for (SyncProcessState state : states) {
            // if it is in the to states list, affirm that transition is valid
            if (toStates.contains(state)) {
                Assert.assertTrue(v.validate(from, state));
            } else {
                // otherwise assume false
                Assert.assertFalse(v.validate(from, state));
            }
        }
    }

}
