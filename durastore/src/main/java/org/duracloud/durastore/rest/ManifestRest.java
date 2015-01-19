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

import org.duracloud.manifest.error.ManifestArgumentException;
import org.duracloud.manifest.error.ManifestNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * REST interface for the Manifest Generator.
 *
 * @author Andrew Woods
 *         Date: 3/29/12
 */
@Path("/manifest")
@Component
public class ManifestRest extends BaseRest {

    private final Logger log = LoggerFactory.getLogger(ManifestRest.class);

    private ManifestResource manifestResource;

    private boolean enabled = true;
    
    @Autowired
    public ManifestRest(ManifestResource manifestResource) {
        this.manifestResource = manifestResource;
    }

    @Path("/{spaceId}")
    @GET
    public Response getManifest(@PathParam("spaceId") String spaceId,
                                @QueryParam("format") String format,
                                @QueryParam("storeID") String storeId) {
        
        if(!enabled){
            return Response.status(501).entity("This endpoint is currently disabled.").build();
        }
        
        String account = getSubdomain();
        
        log.info("getting manifest, {}:{}:{} [{}]", account, storeId,
                                                               spaceId,
                                                               format );

        try {
            
            InputStream manifest = manifestResource.getManifest(account,
                                                                storeId,
                                                                spaceId,
                                                                format);
            
            
            return responseOkStream(manifest);

        } catch (ManifestArgumentException e) {
            log.error("Error for, {}:{} [{}]",
                      new Object[]{storeId, spaceId, format, e});
            return responseBadRequest(e);

        } catch (ManifestNotFoundException e) {
            log.error("Error for, {}:{} [{}]",
                      new Object[]{storeId, spaceId, format, e});
            return responseNotFound(e.getMessage());

        } catch (Exception e) {
            log.error("Error for, {}:{} [{}]",
                      new Object[]{storeId, spaceId, format, e});
            return responseBad(e);
        }
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
