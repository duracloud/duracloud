/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.bulkimageconversion;

import org.duracloud.storage.domain.HadoopTypes;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.duracloud.storage.domain.HadoopTypes.INSTANCES;
import static org.duracloud.storage.domain.HadoopTypes.TASK_PARAMS;

/**
 * @author: Bill Branan
 * Date: Aug 23, 2010
 */
public class BulkImageConversionServiceTest {

    @Test
    public void testCollectTaskParams() {
        BulkImageConversionService service = new BulkImageConversionService();
        service.setWorkSpaceId("test-work");
        service.setSourceSpaceId("test-source");
        service.setDestSpaceId("test-dest");
        service.setToFormat("png");
        service.setNamePrefix("test-");
        service.setNameSuffix("-test");
        service.setColorSpace("sRGB");
        service.setOptimizeMode("advanced");
        service.setInstanceType("c1.xlarge");
        service.setNumInstances("8");

        Map<String, String> params =  service.collectTaskParams();
        assertEquals(HadoopTypes.JOB_TYPES.BULK_IMAGE_CONVERSION.name(),
                     params.get(TASK_PARAMS.JOB_TYPE.name()));
        assertEquals("test-work", params.get(TASK_PARAMS.WORKSPACE_ID.name()));
        assertEquals("test-source", params.get(TASK_PARAMS.SOURCE_SPACE_ID.name()));
        assertEquals("test-dest", params.get(TASK_PARAMS.DEST_SPACE_ID.name()));
        assertEquals("png", params.get(TASK_PARAMS.DEST_FORMAT.name()));
        assertEquals("test-", params.get(TASK_PARAMS.NAME_PREFIX.name()));
        assertEquals("-test", params.get(TASK_PARAMS.NAME_SUFFIX.name()));
        assertEquals("sRGB", params.get(TASK_PARAMS.COLOR_SPACE.name()));
        assertEquals("c1.xlarge", params.get(TASK_PARAMS.INSTANCE_TYPE.name()));
        assertEquals("8", params.get(TASK_PARAMS.NUM_INSTANCES.name()));
    }

    @Test
    public void testBulkImageConversionParameters() {
        BulkImageConversionService service = new BulkImageConversionService();

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

        service.setToFormat(null);
        assertEquals("jp2", service.getToFormat());
        service.setToFormat("png");
        assertEquals("png", service.getToFormat());

        service.setColorSpace(null);
        assertEquals("source", service.getColorSpace());
        service.setColorSpace("sRGB");
        assertEquals("sRGB", service.getColorSpace());

        service.setSourceSpaceId(null);
        assertEquals("service-source", service.getSourceSpaceId());
        service.setSourceSpaceId("test-source");
        assertEquals("test-source", service.getSourceSpaceId());

        service.setDestSpaceId(null);
        assertEquals(null, service.getDestSpaceId());
        service.setDestSpaceId("test-dest");
        assertEquals("test-dest", service.getDestSpaceId());

        service.setWorkSpaceId(null);
        assertEquals(null, service.getWorkSpaceId());
        service.setWorkSpaceId("test-work");
        assertEquals("test-work", service.getWorkSpaceId());

        service.setNamePrefix(null);
        assertEquals("", service.getNamePrefix());
        service.setNamePrefix("test-");
        assertEquals("test-", service.getNamePrefix());

        service.setNameSuffix(null);
        assertEquals("", service.getNameSuffix());
        service.setNameSuffix("-test");
        assertEquals("-test", service.getNameSuffix());

        service.setNumInstances(null);
        assertEquals("1", service.getNumInstances());
        service.setNumInstances("test");
        assertEquals("1", service.getNumInstances());
        service.setNumInstances("8");
        assertEquals("8", service.getNumInstances());

        service.setInstanceType(null);
        assertEquals(INSTANCES.SMALL.getId(), service.getInstanceType());
        service.setInstanceType(INSTANCES.XLARGE.getId());
        assertEquals(INSTANCES.XLARGE.getId(), service.getInstanceType());

        service.setMappersPerInstance(null);
        assertEquals("1", service.getMappersPerInstance());
        service.setMappersPerInstance("test");
        assertEquals("1", service.getMappersPerInstance());
        service.setMappersPerInstance("8");
        assertEquals("8", service.getMappersPerInstance());        
    }

    @Test
    public void testOptmizationConfig() {
        String instanceType = "m1.xlarge";
        String numOfInstances = "10";

        BulkImageConversionService service = new BulkImageConversionService();
        service.setOptimizeMode("standard");
        service.setOptimizeType("optimize_for_speed");
        service.setSpeedInstanceType(instanceType);
        service.setSpeedNumInstances(numOfInstances);
        assertEquals(instanceType,service.getInstancesType());
        assertEquals(numOfInstances,service.getNumOfInstances());

        instanceType = "m1.large";
        numOfInstances = "3";
        service.setOptimizeType("optimize_for_cost");
        service.setSpeedInstanceType(null);
        service.setSpeedNumInstances(null);
        service.setCostInstanceType(instanceType);
        service.setCostNumInstances(numOfInstances);
        assertEquals(instanceType,service.getInstancesType());
        assertEquals(numOfInstances,service.getNumOfInstances());
    }

    @Test
    public void testGetNumMappers() {
        BulkImageConversionService service = new BulkImageConversionService();

        String num = service.getNumMappers(HadoopTypes.INSTANCES.SMALL.getId());
        verifyNumMappers(num, "1");

        num = service.getNumMappers(HadoopTypes.INSTANCES.LARGE.getId());
        verifyNumMappers(num, "2");

        num = service.getNumMappers(HadoopTypes.INSTANCES.XLARGE.getId());
        verifyNumMappers(num, "4");
    }

    private void verifyNumMappers(String num, String expected) {
        Assert.assertNotNull(num);
        Assert.assertEquals(expected, num);
    }

}
