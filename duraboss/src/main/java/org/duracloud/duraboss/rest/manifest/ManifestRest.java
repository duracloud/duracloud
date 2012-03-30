/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraboss.rest.manifest;

import org.duracloud.duraboss.rest.BaseRest;
import org.duracloud.manifest.error.ManifestArgumentException;
import org.duracloud.manifest.error.ManifestEmptyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.InputStream;

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

    public ManifestRest(ManifestResource manifestResource) {
        this.manifestResource = manifestResource;
    }

    @Path("/{spaceId}")
    @GET
    public Response getManifest(@PathParam("spaceId") String spaceId,
                                @QueryParam("format") String format,
                                @QueryParam("date") String date,
                                @QueryParam("storeID") String storeId) {
        log.info("getting manifest, {}:{}.{} [{}]",
                 new Object[]{storeId, spaceId, format, date});

        try {
            InputStream manifest = manifestResource.getManifest(storeId,
                                                                spaceId,
                                                                format,
                                                                date);
            return responseOkStream(manifest);

        } catch (ManifestArgumentException e) {
            log.error("Error for, {}:{}.{} [{}]",
                      new Object[]{storeId, spaceId, format, date, e});
            return responseBadRequest(e);

        } catch (ManifestEmptyException e) {
            log.error("Error for, {}:{}.{} [{}]",
                      new Object[]{storeId, spaceId, format, date, e});
            return responseNotFound(e.getMessage());

        } catch (Exception e) {
            log.error("Error for, {}:{}.{} [{}]",
                      new Object[]{storeId, spaceId, format, date, e});
            return responseBad(e);
        }
    }

}
