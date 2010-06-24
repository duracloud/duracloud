/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.util.internal;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.services.common.error.ServiceException;
import org.duracloud.servicesutil.util.ServiceInstaller;
import org.duracloud.servicesutil.util.catalog.BundleCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Andrew Woods
 */
public class ServiceInstallerImpl extends ServiceInstallBase implements ServiceInstaller {

    protected final Logger log = LoggerFactory.getLogger(ServiceInstallerImpl.class);

    public void init() throws Exception {
        log.info("initializing SerivceInstallerImpl");
        createHoldingDirs();
    }

    private void createHoldingDirs() throws ServiceException {
        File home = getBundleHome().getHome();
        if (!home.exists() && !home.mkdirs()) {
            throwServiceException(home);
        }

        File attic = getBundleHome().getAttic();
        if (!attic.exists() && !attic.mkdir()) {
            throwServiceException(attic);
        }

        File container = getBundleHome().getContainer();
        if (!container.exists() && !container.mkdir()) {
            throwServiceException(container);
        }

        File work = getBundleHome().getWork();
        if (!work.exists() && !work.mkdir()) {
            throwServiceException(work);
        }
    }

    private void throwServiceException(File dir) throws ServiceException {
        String msg = "Unable to find/create dir: " + dir.getAbsolutePath();
        log.error(msg);
        throwServiceException(msg);
    }

    public void install(String name, InputStream bundle)
        throws ServiceException {
        log.info("bundleHome: '" + getBundleHome().getBaseDir() + "'");

        ensureFileTypeSupported(name);
        storeInAttic(name, bundle);

        if (isJar(name)) {
            installBundleFromAttic(name);
        } else if (isZip(name)) {
            explodeAndInstall(name);
        } else {
            throwServiceException("Unsupported filetype: '" + name + "'");
        }
    }

    private void ensureFileTypeSupported(String name) throws ServiceException {
        if (!isJar(name) && !isZip(name)) {
            throwServiceException("Extension not supported: '" + name + "'");
        }
    }

    private void storeInAttic(String name, InputStream bundle) {
        storeInDir(getBundleHome().getAttic(), name, bundle);
    }

    private void storeInDir(File dir, String name, InputStream bundle) {
        FileOutputStream fileOutput = null;
        try {
            File file = new File(dir, name);
            fileOutput = FileUtils.openOutputStream(file);
            IOUtils.copy(bundle, fileOutput);

            log.debug("bundle name  : " + file.getName());
            log.debug("bundle length: " + file.length());

        } catch (IOException e) {
            throwRuntimeException("storeInAttic(): '" + name + "'", e);

        } finally {
            IOUtils.closeQuietly(fileOutput);
        }
    }

    private void installBundleFromAttic(String name) {
        if (BundleCatalog.register(name)) {
            File atticFile = getBundleHome().getFromAttic(name);
            File container = getBundleHome().getContainer();

            try {
                FileUtils.copyFileToDirectory(atticFile, container);
            } catch (IOException e) {
                throwRuntimeException("installBundle(): '" + name + "'", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void explodeAndInstall(String name) throws ServiceException {
        File atticFile = getBundleHome().getFromAttic(name);
        ZipFile zip = getZipFile(atticFile);

        Enumeration entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            String entryName = FilenameUtils.getName(entry.getName());

            if (isJar(entryName)) {
                if(BundleCatalog.register(entryName)) {
                    InputStream entryStream = getZipEntryStream(zip, entry);
                    installBundleFromStream(entryName, entryStream);
                } else {
                    // Ignore Jars which are already deployed
                }
            } else {
                InputStream entryStream = getZipEntryStream(zip, entry);
                String serviceId = FilenameUtils.getBaseName(name);
                storeInWorkDir(serviceId, entryName, entryStream);

            }
        }

        try {
            zip.close();
        } catch(IOException e) {
            throwRuntimeException("Could not close zip file: " + zip.getName(),
                                  e);
        }
    }

    private ZipFile getZipFile(File atticFile) {
        ZipFile zip = null;
        try {
            zip = new ZipFile(atticFile);
        } catch (IOException e) {
            throwRuntimeException("Getting zip: " + atticFile.getName(), e);
        }
        return zip;
    }

    private InputStream getZipEntryStream(ZipFile zip, ZipEntry entry) {
        InputStream entryStream = null;
        try {
            entryStream = zip.getInputStream(entry);
        } catch (IOException e) {
            throwRuntimeException("Getting zip stream: " + entry.getName(), e);
        }
        return entryStream;
    }

    private void storeInWorkDir(String serviceId,
                                String name,
                                InputStream stream) {
        File serviceWorkDir = getBundleHome().getServiceWork(serviceId);    
        storeInDir(serviceWorkDir, name, stream);
    }

    private void installBundleFromStream(String name, InputStream inStream) {
        File container = getBundleHome().getContainer();
        File installedBundleFile = new File(container, name);
        OutputStream installedBundle = null;
        try {
            installedBundle = new FileOutputStream(installedBundleFile);
            IOUtils.copy(inStream, installedBundle);

        } catch (IOException e) {
            throwRuntimeException("installBundleFromStream(): " + name, e);

        } finally {
            IOUtils.closeQuietly(installedBundle);
            IOUtils.closeQuietly(inStream);
        }
    }

    private void throwRuntimeException(String msg, Throwable t) {
        log.error("Error: " + msg, t);
        throw new DuraCloudRuntimeException(msg, t);
    }

}
