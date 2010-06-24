/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ec2typicacomputeprovider.mgmt;

import java.util.List;
import java.util.NoSuchElementException;

import com.xerox.amazonws.ec2.ReservationDescription;
import com.xerox.amazonws.ec2.ReservationDescription.Instance;

public class EC2Helper {

    protected static Instance getFirstRunningInstance(ReservationDescription response) {
        List<Instance> instances = response.getInstances();
        if (instances != null) {
            for (Instance instance : instances) {
                if (instance != null) {
                    return instance;
                }
            }
        }
        throw new NoSuchElementException("RunningInstance not found.");
    }

    protected static Instance getFirstRunningInstance(List<ReservationDescription> response) {
        if (response != null) {
            Instance instance = null;
            for (ReservationDescription desc : response) {
                try {
                    instance = getFirstRunningInstance(desc);
                    return instance;
                } catch (Exception e) { // do nothing.
                }
            }
        }
        throw new NoSuchElementException("RunningInstance not found.");
    }

    // TODO: awoods: remove?
    //    protected static TerminatingInstance getFirstStoppingInstance(TerminateInstancesResponse response) {
    //        if (response.isSetTerminateInstancesResult()) {
    //            TerminateInstancesResult result =
    //                    response.getTerminateInstancesResult();
    //            if (result.isSetTerminatingInstance()) {
    //                return result.getTerminatingInstance().get(0);
    //            }
    //        }
    //        throw new NoSuchElementException("TerminatingInstance not found.");
    //    }
}
