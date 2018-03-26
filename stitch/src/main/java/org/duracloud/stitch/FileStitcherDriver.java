/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.stitch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.duracloud.chunk.FileChunker;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.model.Credential;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.stitch.datasource.DataSource;
import org.duracloud.stitch.datasource.impl.DuraStoreDataSource;
import org.duracloud.stitch.impl.FileStitcherImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the command-line driver for reconstituting a single content
 * item which has previously been chunked into DuraStore.
 *
 * @author Andrew Woods
 * Date: 9/5/11
 */
public class FileStitcherDriver {

    private Logger log = LoggerFactory.getLogger(FileStitcherDriver.class);

    private FileStitcher stitcher;

    public FileStitcherDriver(DataSource dataSource) {
        this.stitcher = new FileStitcherImpl(dataSource);
    }

    /**
     * This method retrieves the chunks manifest specified by the arg space-id
     * and manifest-id (content-id), then reconstitues the chunks defined in
     * the manifest into the original file at the arg to-directory.
     *
     * @param spaceId    containing chunks manifest
     * @param manifestId of the manifest (content-id)
     * @param toDir      destination of reconstituted original content item
     * @throws Exception on error
     */
    public void stitch(String spaceId, String manifestId, File toDir)
        throws Exception {
        verifyDir(toDir);

        Content content = stitcher.getContentFromManifest(spaceId, manifestId);
        writeContentToDir(content, toDir);
    }

    private void verifyDir(File dir) throws Exception {
        if (!dir.isDirectory() && !dir.mkdirs()) {
            throw new DuraCloudRuntimeException("Invalid dir: " + dir);
        }

        try {
            File tmp = new File(dir, "remove-me.txt");
            FileOutputStream fos = new FileOutputStream(tmp);
            fos.write("hello".getBytes());
            IOUtils.closeQuietly(fos);
            FileUtils.deleteQuietly(tmp);

        } catch (IOException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("User must have permissions to write to the directory: ");
            sb.append(dir.getAbsolutePath());
            throw new Exception(sb.toString(), e);
        }
    }

    private void writeContentToDir(Content content, File toDir) {
        File outFile = new File(toDir, content.getId());
        log.info("Writing to '{}'.", outFile.getAbsolutePath());

        OutputStream outputStream = null;
        try {
            // Create any needed subdirectories
            File parentDir = outFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
                parentDir.setWritable(true);
            }

            // Write content
            outputStream = new FileOutputStream(outFile);
            IOUtils.copyLarge(content.getStream(), outputStream);

        } catch (IOException e) {
            StringBuilder msg = new StringBuilder();
            msg.append("Error writing content: ");
            msg.append(content.getId());
            msg.append(" to output file: ");
            msg.append(outFile.getAbsolutePath());
            throw new DuraCloudRuntimeException(msg.toString(), e);

        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    private static Options getOptions() {
        Option host = new Option("h",
                                 "host",
                                 true,
                                 "hostname of duracloud instance");

        Option port = new Option("r",
                                 "port",
                                 true,
                                 "port of duracloud instance");

        Option username = new Option("u",
                                     "username",
                                     true,
                                     "username of duracloud instance");

        Option password = new Option("p",
                                     "password",
                                     true,
                                     "password of duracloud instance");

        Option storeId = new Option("i",
                                    "store-id",
                                    true,
                                    "store-id of duracloud storage provider");

        Option spaceId = new Option("s",
                                    "space-id",
                                    true,
                                    "space-id of duracloud space where " +
                                    "manifest and chunks reside");

        Option manifestId = new Option("m",
                                       "manifest-id",
                                       true,
                                       "manifest-id of chunks manifest");

        Option toDir = new Option("d",
                                  "to-dir",
                                  true,
                                  "destination directory of full content");
        host.setRequired(true);
        port.setRequired(false);
        username.setRequired(true);
        password.setRequired(true);
        storeId.setRequired(false);
        spaceId.setRequired(true);
        manifestId.setRequired(true);
        toDir.setRequired(true);

        Options options = new Options();
        options.addOption(host);
        options.addOption(port);
        options.addOption(username);
        options.addOption(password);
        options.addOption(storeId);
        options.addOption(spaceId);
        options.addOption(manifestId);
        options.addOption(toDir);

        return options;
    }

    private static CommandLine parseArgs(String[] args) {
        CommandLineParser parser = new GnuParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(getOptions(), args);

        } catch (ParseException e) {
            System.err.println(e);
            die();
        }
        return cmd;
    }

    private static void usage() {
        HelpFormatter help = new HelpFormatter();
        help.setWidth(80);
        help.printHelp(FileChunker.class.getCanonicalName(), getOptions());
    }

    private static void die() {
        usage();
        System.exit(1);
    }

    /**
     * Main
     */
    public static void main(String[] args) {
        CommandLine cmd = parseArgs(args);

        String spaceId = cmd.getOptionValue("space-id");
        String manifestId = cmd.getOptionValue("manifest-id");
        String toDir = cmd.getOptionValue("to-dir");

        // do the stitching.
        try {
            DataSource dataSource = getDataSource(cmd);
            FileStitcherDriver driver = new FileStitcherDriver(dataSource);

            driver.stitch(spaceId, manifestId, new File(toDir));

        } catch (Exception e) {
            System.err.println("Error stitching content: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }

        System.out.println(
            "Successfully downloaded: " + spaceId + "/" + manifestId);
    }

    private static DataSource getDataSource(CommandLine cmd)
        throws ContentStoreException {
        String host = cmd.getOptionValue("host");
        String port = "443";
        if (cmd.hasOption("port")) {
            port = cmd.getOptionValue("port");
        }

        String username = cmd.getOptionValue("username");
        String password = cmd.getOptionValue("password");

        ContentStoreManager mgr = new ContentStoreManagerImpl(host, port);
        mgr.login(getCredentials(username, password));

        ContentStore contentStore;
        if (cmd.hasOption("store-id")) {
            contentStore = mgr.getContentStore(cmd.getOptionValue("store-id"));
        } else {
            contentStore = mgr.getPrimaryContentStore();
        }
        return new DuraStoreDataSource(contentStore);
    }

    private static Credential getCredentials(String username, String password) {
        if (null == username || null == password) {
            String border = "**************\n";
            StringBuilder msg = new StringBuilder(border);
            msg.append("If either username or password are provided,\n");
            msg.append("they both must be provided.\n");
            msg.append(border);
            System.out.println(msg);
            die();
        }

        return new Credential(username, password);
    }

}
