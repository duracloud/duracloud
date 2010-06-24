/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util.bulk;

import org.duracloud.common.error.ManifestVerifyException;

import java.io.File;

/**
 * @author Andrew Woods
 *         Date: Oct 24, 2009
 */
public class ManifestVerifierDriver {

    private static void verify(File file0, File file1, String[] filters) {
        ManifestVerifier verifier = new ManifestVerifier(file0, file1);
        try {
            verifier.verify(filters);
            success();
        } catch (ManifestVerifyException e) {
            reportError(e);
        }
        verifier.report(System.out);
    }

    private static void reportError(ManifestVerifyException e) {
        System.out.println(e.getFormattedMessage());
    }

    private static void success() {
        System.out.println("valid");
    }


    private static void usage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Usage: java ManifestVerifierDriver ");
        sb.append("<manifest0> <manifest1> [filters]");
        sb.append("\n");
        sb.append("\n\twhere <manifest[0|1]> are files containing only pairs ");
        sb.append("of checksums and entry-names separated by whitespace");
        sb.append("\n\t");
        sb.append("[filters] - optional, are ':' delimited names of manifest ");
        sb.append("entries that will be ignored during verification.");
        sb.append("\n");
        sb.append("\n\tExample:");
        sb.append("\n\t");
        sb.append("java ManifestVerifierDriver ");
        sb.append("/tmp/source-bag/manifest-md5.txt ");
        sb.append("/mnt/dura/duracloud-bag/manifest-md5.txt index.html:scandata.zip");
        sb.append("\n");
        System.out.println(sb.toString());
    }

    public static void main(String[] args) {
        if (args.length != 2 && args.length != 3) {
            usage();
            System.exit(1);
        }

        File file0 = new File(args[0]);
        File file1 = new File(args[1]);

        String[] filters = null;
        if (args.length == 3) {
            filters = args[2].split(":");
        }
        verify(file0, file1, filters);
    }


}
