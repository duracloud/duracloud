/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.duracloud.chunk.error.NotFoundException;
import org.duracloud.chunk.writer.ContentWriter;
import org.duracloud.chunk.writer.DuracloudContentWriter;
import org.duracloud.chunk.writer.FilesystemContentWriter;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.model.Credential;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a commandline interface for initiating the read of local
 * content, chunking it, then writing it via a provided ContentWriter.
 *
 * @author Andrew Woods
 *         Date: Feb 5, 2010
 */
public class FileChunkerDriver {

    private static void chunk(File fromDir,
                              File toSpace,
                              FileChunkerOptions options,
                              ContentWriter writer) throws NotFoundException {

        if (!fromDir.isDirectory()) {
            throw new DuraCloudRuntimeException("Invalid dir: " + fromDir);
        }

        FileChunker chunker = new FileChunker(writer, options);
        chunker.addContentFrom(fromDir, toSpace.getPath());

        File report = new File("chunker-report.csv");
        chunker.writeReport(report);
        System.out.println("see report at: " + report.getAbsolutePath());
    }

    private static Long getChunkSize(String arg) {
        char unit = arg.toLowerCase().charAt(arg.length() - 1);
        if (unit != 'k' && unit != 'm' && unit != 'g') {
            throw new DuraCloudRuntimeException(
                "Chunk size must be of the form: <digit(s)><K|M|G>");
        }

        int multiplier = Integer.parseInt(arg.substring(0, arg.length() - 1));

        final long KB = 1000;
        final long MB = 1000*KB;
        final long GB = 1000*MB;

        long chunkSize = 1 * MB;
        switch (unit) {
            case 'k':
                chunkSize = multiplier * KB;
                break;
            case 'm':
                chunkSize = multiplier * MB;
                break;
            case 'g':
                chunkSize = multiplier * GB;
                break;
        }
        return chunkSize;
    }

    private static Options getOptions() {

        Option username = new Option("u",
                                     "username",
                                     true,
                                     "username of duracloud instance");
        username.setArgs(1);
        username.setArgName("username");

        Option password = new Option("p",
                                     "password",
                                     true,
                                     "password of duracloud instance");
        password.setArgs(1);
        password.setArgName("password");

        Option create = new Option("g",
                                   "generate",
                                   true,
                                   "generate test data to <outFile> of " +
                                       "<size> bytes");
        create.setArgs(2);
        create.setArgName("outFile numBytes");
        create.setValueSeparator(' ');

        Option add = new Option("a",
                                "add",
                                true,
                                "add content from dir:<f> to space:<t> of max" +
                                    " chunk size:<s> in units of K,M,G");
        add.setArgs(3);
        add.setArgName("f t s{K|M|G}");
        add.setValueSeparator(' ');

        Option fileFiltered = new Option("f",
                                         "file-filter",
                                         true,
                                         "limit processed files to those " +
                                             "listed in file-list:<l>");
        fileFiltered.setArgs(1);
        fileFiltered.setArgName("l");

        Option dirFiltered = new Option("d",
                                        "dir-filter",
                                        true,
                                        "limit processed directories to " +
                                            "those listed in file-list:<l>");
        dirFiltered.setArgs(1);
        dirFiltered.setArgName("l");

        Option cloud = new Option("c",
                                  "cloud-store",
                                  true,
                                  "use cloud store found at <host>:<port> " +
                                      "as content dest");
        cloud.setArgs(2);
        cloud.setArgName("host:port");
        cloud.setValueSeparator(':');

        Option excludeChunkMD5s = new Option("x",
                                             "exclude-chunk-md5s",
                                             false,
                                             "if this option is set, chunk " +
                                                 "MD5s will NOT be preserved " +
                                                 "in the manifest");

        Option ignoreLargeFiles = new Option("i",
                                             "ignore-large-files",
                                             false,
                                             "if this option is set, files " +
                                                 "over the chunk size " +
                                                 "specified in the 'add' " +
                                                 "option will be ignored.");

        Options options = new Options();
        options.addOption(username);
        options.addOption(password);
        options.addOption(create);
        options.addOption(add);
        options.addOption(fileFiltered);
        options.addOption(dirFiltered);
        options.addOption(cloud);
        options.addOption(excludeChunkMD5s);
        options.addOption(ignoreLargeFiles);

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
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws Exception {

        CommandLine cmd = parseArgs(args);

        ensureWritePermissionToLocalDir();

        // Are there credentials?
        Credential credential = null;
        String username = "";
        if (cmd.hasOption("username") || cmd.hasOption("password")) {
            username = cmd.getOptionValue("username");
            credential = getCredentials(username,
                                        cmd.getOptionValue("password"));
        }

        // Where will content be written?
        ContentWriter writer;
        if (cmd.hasOption("cloud-store")) {
            String[] vals = cmd.getOptionValues("cloud-store");
            String host = vals[0];
            String port = vals[1];
            ContentStoreManager mgr = new ContentStoreManagerImpl(host, port);

            if (credential != null) {
                mgr.login(credential);
            }

            writer = new DuracloudContentWriter(mgr.getPrimaryContentStore(),
                                                username);
        } else {
            writer = new FilesystemContentWriter();
        }

        // Will Chunk MD5's be preserved?
        boolean chunkMD5 = true;
        if (cmd.hasOption("exclude-chunk-md5s")) {
            chunkMD5 = false;
        }

        // Will large files be ignored?
        boolean ignoreLarge = false;
        if (cmd.hasOption("ignore-large-files")) {
            ignoreLarge = true;
        }

        // Will files be filtered?
        IOFileFilter fileFilter = TrueFileFilter.TRUE;
        if (cmd.hasOption("file-filter")) {
            String[] filterVals = cmd.getOptionValues("file-filter");
            fileFilter = buildFilter(new File(filterVals[0]));
        }

        // Will directories be filtered?
        IOFileFilter dirFilter = TrueFileFilter.TRUE;
        if (cmd.hasOption("dir-filter")) {
            String[] filterVals = cmd.getOptionValues("dir-filter");
            dirFilter = buildFilter(new File(filterVals[0]));
        }

        // Add content?
        FileChunkerOptions options;
        if (cmd.hasOption("add")) {
            String[] vals = cmd.getOptionValues("add");
            File fromDir = new File(vals[0]);
            File toDir = new File(vals[1]);
            Long chunkSize = getChunkSize(vals[2]);

            options = new FileChunkerOptions(fileFilter,
                                             dirFilter,
                                             chunkSize,
                                             chunkMD5,
                                             ignoreLarge);
            chunk(fromDir, toDir, options, writer);

            // ...or generate test data
        } else if (cmd.hasOption("generate")) {
            String[] vals = cmd.getOptionValues("generate");
            File outFile = new File(vals[0]);
            long contentSize = Long.parseLong(vals[1]);

            FileChunker.createTestContent(outFile, contentSize);

        } else {
            usage();
        }
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

    private static void ensureWritePermissionToLocalDir() throws Exception {
        try {
            File tmp = new File("remove-me.txt");
            FileOutputStream fos = new FileOutputStream(tmp);
            fos.write("hello".getBytes());
            IOUtils.closeQuietly(fos);
            FileUtils.deleteQuietly(tmp);
        } catch (IOException e) {
            String border = "\n---------------------------------------------\n";
            StringBuilder sb = new StringBuilder(border);
            sb.append("User must have permissions to write to the current " +
                "working directory.");
            sb.append(border);
            throw new Exception(sb.toString(), e);
        }
    }

    private static IOFileFilter buildFilter(File titlesFile) {
        List<IOFileFilter> filters = new ArrayList<IOFileFilter>();

        InputStream input = getInputStream(titlesFile);
        BufferedReader br = new BufferedReader(new InputStreamReader(input));

        String line = readLine(br);
        while (line != null) {
            filters.add(new NameFileFilter(line.trim()));
            line = readLine(br);
        }

        IOUtils.closeQuietly(br);
        return new OrFileFilter(filters);
    }

    private static InputStream getInputStream(File file) {
        try {
            return new AutoCloseInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static String readLine(BufferedReader br) {
        try {
            return br.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
