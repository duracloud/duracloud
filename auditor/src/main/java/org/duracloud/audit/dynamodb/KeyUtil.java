package org.duracloud.audit.dynamodb;

import javax.xml.bind.DatatypeConverter;

import org.duracloud.common.error.DuraCloudRuntimeException;

import com.amazonaws.util.Md5Utils;
import com.amazonaws.util.StringUtils;

public class KeyUtil {

    public static String calculateAuditLogHashKey(String storeId, String spaceId, String contentId, String datetime) {
        return computeMd5(StringUtils.join("/",
                                           storeId, 
                                           spaceId,
                                           contentId,
                                           datetime));
   }
    private static String computeMd5(String value){
        try {
            return DatatypeConverter.printHexBinary(Md5Utils.computeMD5Hash(value.getBytes()));
        } catch (Exception e) {
            throw new DuraCloudRuntimeException(e);
        }
    }

    public static  String calculateAccountSpaceIdHash(String account, String spaceId) {
        return computeMd5(StringUtils.join("/",
                                           account,
                                           spaceId));
    }

}
