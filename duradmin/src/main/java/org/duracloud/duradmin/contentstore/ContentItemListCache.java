/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.contentstore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.duracloud.error.ContentStoreException;

public class ContentItemListCache {

    public static void refresh(HttpServletRequest request,
                                      String spaceId,
                                      ContentStoreProvider contentStoreProvider) {
    
        get(request,spaceId, contentStoreProvider).markForUpdate();
    }
    
    public static ContentItemList get(HttpServletRequest request,
                                      String spaceId,
                                      ContentStoreProvider contentStoreProvider) {
        try {

            HttpSession session = request.getSession();
            ContentItemList list =
                    (ContentItemList) session.getAttribute(getKey(spaceId, contentStoreProvider));
            if (list == null) {
                list = new ContentItemList(spaceId, contentStoreProvider.getContentStore());
                session.setAttribute(getKey(spaceId, contentStoreProvider), list);
            }
            return list;

        } catch (ContentStoreException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String getKey(String spaceId,
                                 ContentStoreProvider contentStoreProvider)
            throws ContentStoreException {
        return "content-list-"
                + spaceId
                + "-"
                + contentStoreProvider.getContentStore()
                        .getStoreId();
    }
    
    
}
