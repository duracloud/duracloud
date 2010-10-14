/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.store;

/**
 * @author Andrew Woods
 *         Date: Oct 12, 2010
 */
public class UriPathUtil {

    public String getSpaceId(String path) {
        if (null == path) {
            return "null-space";
        }

        int protoIndex = path.indexOf("://");
        if (protoIndex == -1) {
            return "malformed-path-no-proto-" + path;
        }

        int dotIndex = path.indexOf('.', protoIndex);
        if (dotIndex == -1) {
            return "malformed-path-no-dot-" + path;
        }

        int slashIndex = path.indexOf('/', dotIndex);
        if (slashIndex == -1) {
            return "malformed-path-no-slash-" + path;
        }

        return path.substring(dotIndex + 1, slashIndex);
    }

    public String getContentId(String path) {
        if (null == path) {
            return "null-content-id";
        }

        String proto = "://";
        int protoIndex = path.indexOf(proto);
        if (protoIndex == -1) {
            return "malformed-path-no-proto-" + path;
        }

        int slashIndex = path.indexOf('/', protoIndex + proto.length());
        if (slashIndex == -1) {
            return "malformed-path-no-slash-" + path;
        }

        return path.substring(slashIndex + 1);
    }
}
