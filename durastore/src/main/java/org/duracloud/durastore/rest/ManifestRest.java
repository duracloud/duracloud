/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.http.client.utils.URIBuilder;
import org.duracloud.common.constant.Constants;
import org.duracloud.common.constant.ManifestFormat;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.retry.Retriable;
import org.duracloud.common.retry.Retrier;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.util.ChecksumUtil.Algorithm;
import org.duracloud.common.util.DateUtil;
import org.duracloud.common.util.DateUtil.DateFormat;
import org.duracloud.common.util.IOUtil;
import org.duracloud.manifest.error.ManifestArgumentException;
import org.duracloud.manifest.error.ManifestNotFoundException;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.util.StorageProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * REST interface for the Manifest Generator.
 *
 * @author Andrew Woods Date: 3/29/12
 */
@Path("/manifest")
@Component
public class ManifestRest extends BaseRest {

    private final Logger log = LoggerFactory.getLogger(ManifestRest.class);

    private ManifestResource manifestResource;

    private boolean enabled = true;

    private StorageProviderFactory storageProviderFactory;

    private static Executor executor = Executors.newFixedThreadPool(10);

    private static final String DEFAULT_FORMAT = ManifestFormat.TSV.name();

    @Autowired
    public ManifestRest(ManifestResource manifestResource,
                        StorageProviderFactory storageProviderFactory) {
        this.manifestResource = manifestResource;
        this.storageProviderFactory = storageProviderFactory;
    }

    @Path("/{spaceId}")
    @GET
    public Response getManifest(@PathParam("spaceId") String spaceId,
                                @QueryParam("format") String format,
                                @QueryParam("storeID") String storeId) {

        if (!enabled) {
            return Response.status(501)
                           .entity("This endpoint is currently disabled.")
                           .build();
        }

        if (format == null) {
            format = DEFAULT_FORMAT;
        }

        String account = getSubdomain();
        log.info("getting manifest, {}:{}:{} [{}]",
                 account,
                 storeId,
                 spaceId,
                 format);

        try {

            InputStream manifest =
                manifestResource.getManifest(account, storeId, spaceId, format);

            return Response.ok(manifest)
                           .type(ManifestFormat.valueOf(format)
                                               .getMimeType()).build();

        } catch (ManifestArgumentException e) {
            log.error("Error for, {}:{} [{}]",
                      new Object[] {storeId, spaceId, format, e});
            return responseBadRequest(e);

        } catch (ManifestNotFoundException e) {
            log.error("Error for, {}:{} [{}]",
                      new Object[] {storeId, spaceId, format, e});
            return responseNotFound(e.getMessage());

        } catch (Exception e) {
            log.error("Error for, {}:{} [{}]",
                      new Object[] {storeId, spaceId, format, e});
            return responseBad(e);
        }
    }

    @Path("/{spaceId}")
    @POST
    public Response generateManifest(@PathParam("spaceId") String spaceId,
                                     @QueryParam("format") String format,
                                     @QueryParam("storeID") String storeId) {

        if (!enabled) {
            return Response.status(501)
                           .entity("This endpoint is currently disabled.")
                           .build();
        }

        if (format == null) {
            format = DEFAULT_FORMAT;
        }

        String account = getSubdomain();
        log.info("generating manifest, {}:{}:{} [{}]", account, storeId, spaceId, format);

        try {
            URI uri = generateAsynchronously(account, spaceId, storeId, format);
            return Response.accepted("We are processing your manifest generation request. " +
                                     "To retrieve your file, please poll the URI in the Location " +
                                     "header of this response: (" + uri + ").").location(uri).build();
        } catch (ManifestArgumentException e) {
            log.error("Error for, {}:{} [{}]",
                      new Object[] {storeId, spaceId, format, e});
            return responseBadRequest(e);

        } catch (ManifestNotFoundException e) {
            log.error("Error for, {}:{} [{}]",
                      new Object[] {storeId, spaceId, format, e});
            return responseNotFound(e.getMessage());

        } catch (Exception e) {
            log.error("Error for, {}:{} [{}]",
                      new Object[] {storeId, spaceId, format, e});
            return responseBad(e);
        }
    }

    /**
     * Generates a manifest file asynchronously and uploads to DuraCloud
     *
     * @param account
     * @param spaceId
     * @param storeId
     * @param format
     * @return The URI of the generated manifest.
     */
    private URI generateAsynchronously(String account,
                                       String spaceId,
                                       String storeId,
                                       String format)
        throws Exception {
        StorageProviderType providerType = getStorageProviderType(storeId);

        InputStream manifest =
            manifestResource.getManifest(account, storeId, spaceId, format);

        String contentId =
            MessageFormat.format("generated-manifests/manifest-{0}_{1}_{2}.txt{3}",
                                 spaceId,
                                 providerType.name().toLowerCase(),
                                 DateUtil.convertToString(System.currentTimeMillis(), DateFormat.PLAIN_FORMAT),
                                 ".gz");

        String adminSpace = "x-duracloud-admin";

        URI uri = buildURI(adminSpace, contentId);

        StorageProvider provider = storageProviderFactory.getStorageProvider();

        executor.execute(() -> {

            try {
                // write file to disk
                File file = IOUtil.writeStreamToFile(manifest, true);

                // upload to the default storage provider with retries
                uploadManifestToDefaultStorageProvider(format,
                                                       adminSpace,
                                                       contentId,
                                                       file,
                                                       provider);
            } catch (Exception ex) {
                log.error("failed to generate manifest for space: spaceId="
                          + spaceId
                          + ", storeId="
                          + storeId
                          + " : "
                          + ex.getMessage(),
                          ex);
            }
        });

        return uri;
    }

    private StorageProviderType getStorageProviderType(String storeId) {
        for (StorageAccount a : this.storageProviderFactory.getStorageAccounts()) {
            if (storeId == null) {
                if (a.isPrimary()) {
                    return a.getType();
                }
            } else {
                if (storeId.equals(a.getId())) {
                    return a.getType();
                }
            }
        }

        //this should never happen.
        throw new DuraCloudRuntimeException("storage provider type could not be resolved");
    }

    protected URI buildURI(String adminSpace, String contentId)
        throws URISyntaxException {
        String host = request.getAttribute(Constants.SERVER_HOST).toString();
        int port = (Integer) request.getAttribute(Constants.SERVER_PORT);
        String context = request.getContextPath();

        URIBuilder builder = new URIBuilder().setHost(host)
                                             .setScheme("http" + (port == 443 ? "s" : ""))
                                             .setPath(context + "/" + adminSpace + "/" + contentId);

        if (port != 443 && port != 80) {
            builder = builder.setPort(port);
        }

        return builder.build();
    }

    protected void uploadManifestToDefaultStorageProvider(String format,
                                                          String adminSpace,
                                                          String contentId,
                                                          File file,
                                                          StorageProvider provider)
        throws Exception {
        try {
            // calculate the md5
            ChecksumUtil util = new ChecksumUtil(Algorithm.MD5);
            String checksum = util.generateChecksum(file);

            new Retrier().execute(new Retriable() {
                @Override
                public Object retry() throws Exception {
                    try (FileInputStream content = new FileInputStream(file)) {

                        return provider.addContent(adminSpace,
                                                   contentId,
                                                   ManifestFormat.valueOf(format.toUpperCase()).getMimeType(),
                                                   null,
                                                   file.length(),
                                                   checksum,
                                                   content);
                    } catch (Exception ex) {
                        throw new DuraCloudRuntimeException(ex);
                    }
                }
            });
        } finally {
            file.delete();
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
