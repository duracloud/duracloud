/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.manifest.error.ManifestArgumentException;
import org.duracloud.manifest.error.ManifestEmptyException;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.util.StorageProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST interface for the Manifest Generator.
 *
 * @author Andrew Woods
 *         Date: 3/29/12
 */
@Path("/manifest")
public class ManifestRest extends BaseRest {

    private final Logger log = LoggerFactory.getLogger(ManifestRest.class);

    private ManifestResource manifestResource;

    private StorageProviderFactory storageProviderFactory;

    public ManifestRest(ManifestResource manifestResource, StorageProviderFactory storageProviderFactory) {
        this.storageProviderFactory = storageProviderFactory;
        this.manifestResource = manifestResource;
    }

    @Path("/{spaceId}")
    @GET
    public Response getManifest(@PathParam("spaceId") String spaceId,
                                @QueryParam("format") String format,
                                @QueryParam("storeID") String storeId) {
        log.info("getting manifest, {}:{} [{}]",
                 new Object[]{storeId, spaceId, format});

        try {
            if(StringUtils.isBlank(storeId)){
                for(StorageAccount storageAccount: this.storageProviderFactory.getStorageAccounts()){
                    if(storageAccount.isPrimary()){
                        storeId = storageAccount.getId();
                        break;
                    }
                }
                
                if(StringUtils.isBlank(storeId)){
                    throw new DuraCloudRuntimeException("storeId is blank and no primary storage account is indicated.");
                }
                
            }

            
            InputStream manifest = manifestResource.getManifest(storeId,
                                                                spaceId,
                                                                format);
            
            
            return responseOkStream(manifest);

        } catch (ManifestArgumentException e) {
            log.error("Error for, {}:{} [{}]",
                      new Object[]{storeId, spaceId, format, e});
            return responseBadRequest(e);

        } catch (ManifestEmptyException e) {
            log.error("Error for, {}:{} [{}]",
                      new Object[]{storeId, spaceId, format, e});
            return responseNotFound(e.getMessage());

        } catch (Exception e) {
            log.error("Error for, {}:{} [{}]",
                      new Object[]{storeId, spaceId, format, e});
            return responseBad(e);
        }
    }

}
