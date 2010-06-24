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
import org.duracloud.services.common.error.ServiceException;
import org.duracloud.servicesutil.util.ServiceUninstaller;
import org.duracloud.servicesutil.util.catalog.BundleCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Andrew Woods
 */
public class ServiceUninstallerImpl extends ServiceInstallBase
        implements ServiceUninstaller {

    private final Logger log = LoggerFactory.getLogger(ServiceUninstallerImpl.class);


    public void uninstall(String name) throws Exception {
        log.info("bundleHome: '" + getBundleHome().getBaseDir() + "'");

        if (isJar(name)) {
            uninstallBundleFromHomeAndAttic(name);
        } else if (isZip(name)) {
            uninstallBagAndBundles(name);
            removeWorkDir(name);
        } else {
            throwServiceException("Unsupported filetype: '" + name + "'");
        }
    }

    private void uninstallBundleFromHomeAndAttic(String name)
        throws ServiceException {
        if (BundleCatalog.unRegister(name)) {
            delete(getBundleHome().getContainer(), name);
            delete(getBundleHome().getAttic(), name);
        }
    }

    private void uninstallBagAndBundles(String zipName)
        throws IOException, ServiceException {
        ZipFile zip = new ZipFile(getBundleHome().getFromAttic(zipName));
        Enumeration entries = zip.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            String entryName = FilenameUtils.getName(entry.getName());

            if (isJar(entryName) && BundleCatalog.unRegister(entryName)) {
                delete(getBundleHome().getContainer(), entryName);

            } else if (isWar(entryName)) {                
                String serviceId = FilenameUtils.getBaseName(zipName);
                delete(getBundleHome().getServiceWork(serviceId), entryName);
            }
        }

        try {
            zip.close();
        } catch(IOException e) {
            throw new RuntimeException("Could not close zip file: " +
                                       zip.getName(), e);
        }

        delete(getBundleHome().getAttic(), zipName);
    }

    private void removeWorkDir(String name) throws IOException, ServiceException {
        String serviceId = FilenameUtils.getBaseName(name);
        File serviceWorkDir = getBundleHome().getServiceWork(serviceId);  
        if(serviceWorkDir.exists()) {
            FileUtils.deleteDirectory(serviceWorkDir);
        }
    }

    private void delete(File dir, String name) throws ServiceException {
        boolean success = false;
        for (File file : dir.listFiles()) {
            String fileName = file.getName();
            log.debug("found in " + dir.getPath() + ": '" + fileName + "'");

            if (fileName.contains(name)) {
                log.debug("about to delete: " + fileName);
                success = file.delete();
                break;
            }
        }

        if (!success) {
            String msg = "Unable to uninstall service: '" + name + "'";
            log.error(msg);
            super.throwServiceException(msg);
        }
    }

    protected void init() throws Exception {
        // do nothing.
    }
}
