/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.replicationod;

import org.duracloud.storage.domain.HadoopTypes;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.duracloud.storage.domain.HadoopTypes.TASK_PARAMS;

/**
 * @author: Bill Branan
 * Date: Sept 29, 2010
 */
public class ReplicationOnDemandServiceTest {

    @Test
    public void testCollectTaskParams() {
        String jobType = HadoopTypes.JOB_TYPES.REP_ON_DEMAND.name();
        String workSpaceId = "test-work";
        String sourceSpaceId = "test-source";
        String repStoreId = "0";
        String repSpaceId = "rep-space";
        String destSpaceId = "test-dest";
        String instanceType = "c1.xlarge";
        String numInstances = "8";
        String dcHost = "dchost";
        String dcPort = "443";
        String dcContext = "durastore";
        String dcUser = "user";
        String dcPass = "pass";

        ReplicationOnDemandService service = new ReplicationOnDemandService();
        service.setWorkSpaceId(workSpaceId);
        service.setSourceSpaceId(sourceSpaceId);
        service.setRepStoreId(repStoreId);
        service.setRepSpaceId(repSpaceId);
        service.setDestSpaceId(destSpaceId);
        service.setInstanceType(instanceType);
        service.setNumInstances(numInstances);
        service.setDuraStoreHost(dcHost);
        service.setDuraStorePort(dcPort);
        service.setDuraStoreContext(dcContext);
        service.setUsername(dcUser);
        service.setPassword(dcPass);

        Map<String, String> params =  service.collectTaskParams();
        assertEquals(jobType, params.get(TASK_PARAMS.JOB_TYPE.name()));
        assertEquals(workSpaceId, params.get(TASK_PARAMS.WORKSPACE_ID.name()));
        assertEquals(sourceSpaceId, params.get(TASK_PARAMS.SOURCE_SPACE_ID.name()));
        assertEquals(repStoreId, params.get(TASK_PARAMS.REP_STORE_ID.name()));
        assertEquals(repSpaceId, params.get(TASK_PARAMS.REP_SPACE_ID.name()));
        assertEquals(destSpaceId, params.get(TASK_PARAMS.DEST_SPACE_ID.name()));
        assertEquals(instanceType, params.get(TASK_PARAMS.INSTANCE_TYPE.name()));
        assertEquals(numInstances, params.get(TASK_PARAMS.NUM_INSTANCES.name()));
        assertEquals(dcHost, params.get(TASK_PARAMS.DC_HOST.name()));
        assertEquals(dcPort, params.get(TASK_PARAMS.DC_PORT.name()));
        assertEquals(dcContext, params.get(TASK_PARAMS.DC_CONTEXT.name()));
        assertEquals(dcUser, params.get(TASK_PARAMS.DC_USERNAME.name()));
        assertEquals(dcPass, params.get(TASK_PARAMS.DC_PASSWORD.name()));
    }

    @Test
    public void testReplicationOnDemandParameters() {
        ReplicationOnDemandService service = new ReplicationOnDemandService();

        service.setDuraStoreHost(null);
        assertEquals("localhost", service.getDuraStoreHost());
        service.setDuraStoreHost("testhost");
        assertEquals("testhost", service.getDuraStoreHost());

        service.setDuraStorePort(null);
        assertEquals("8080", service.getDuraStorePort());
        service.setDuraStorePort("9999");
        assertEquals("9999", service.getDuraStorePort());

        service.setDuraStoreContext(null);
        assertEquals("durastore", service.getDuraStoreContext());
        service.setDuraStoreContext("testcontext");
        assertEquals("testcontext", service.getDuraStoreContext());

        service.setUsername("me");
        assertEquals("me", service.getUsername());

        service.setPassword("secret");
        assertEquals("secret", service.getPassword());

        service.setSourceSpaceId(null);
        assertEquals("service-source", service.getSourceSpaceId());
        service.setSourceSpaceId("test-source");
        assertEquals("test-source", service.getSourceSpaceId());

        service.setRepStoreId(null);
        assertEquals("0", service.getRepStoreId());
        service.setRepStoreId("5");
        assertEquals("5", service.getRepStoreId());

        service.setRepSpaceId(null);
        assertEquals("replication-space", service.getRepSpaceId());
        service.setRepSpaceId("test-rep");
        assertEquals("test-rep", service.getRepSpaceId());        

        service.setDestSpaceId(null);
        assertEquals("service-dest", service.getDestSpaceId());
        service.setDestSpaceId("test-dest");
        assertEquals("test-dest", service.getDestSpaceId());

        service.setWorkSpaceId(null);
        assertEquals("service-work", service.getWorkSpaceId());
        service.setWorkSpaceId("test-work");
        assertEquals("test-work", service.getWorkSpaceId());

        service.setNumInstances(null);
        assertEquals("1", service.getNumInstances());
        service.setNumInstances("test");
        assertEquals("1", service.getNumInstances());
        service.setNumInstances("8");
        assertEquals("8", service.getNumInstances());

        service.setInstanceType(null);
        assertEquals("m1.small", service.getInstanceType());
        service.setInstanceType("c1.xlarge");
        assertEquals("c1.xlarge", service.getInstanceType());

        service.setMappersPerInstance(null);
        assertEquals("1", service.getMappersPerInstance());
        service.setMappersPerInstance("test");
        assertEquals("1", service.getMappersPerInstance());
        service.setMappersPerInstance("8");
        assertEquals("8", service.getMappersPerInstance());        
    }

    @Test
    public void testGetNumMappers() {
        ReplicationOnDemandService service = new ReplicationOnDemandService();

        String num = service.getNumMappers(HadoopTypes.INSTANCES.SMALL.getId());
        verifyNumMappers(num, "2");

        num = service.getNumMappers(HadoopTypes.INSTANCES.LARGE.getId());
        verifyNumMappers(num, "4");

        num = service.getNumMappers(HadoopTypes.INSTANCES.XLARGE.getId());
        verifyNumMappers(num, "8");
    }

    private void verifyNumMappers(String num, String expected) {
        Assert.assertNotNull(num);
        Assert.assertEquals(expected, num);
    }
}
