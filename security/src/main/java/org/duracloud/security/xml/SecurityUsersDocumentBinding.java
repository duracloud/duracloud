/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.xml;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.duracloud.SecurityUsersDocument;
import org.duracloud.SecurityUsersType;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.security.domain.SecurityUserBean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;


/**
 * This class is a helper utility for binding SecurityUserBean objects to a
 * SecurityUsers xml document.
 *
 * @author Andrew Woods
 *         Date: Apr 15, 2010
 */
public class SecurityUsersDocumentBinding {

    /**
     * This method binds a SecurityUserBean list to the content of the arg xml.
     *
     * @param xml document to be bound to SecurityUserBean list
     * @return SecurityUserBean list
     */
    public static List<SecurityUserBean> createSecurityUsersFrom(InputStream xml) {
        try {
            SecurityUsersDocument doc = SecurityUsersDocument.Factory
                .parse(xml);
            return SecurityUserElementReader.createSecurityUsersFrom(doc);
        } catch (XmlException e) {
            throw new DuraCloudRuntimeException(e);
        } catch (IOException e) {
            throw new DuraCloudRuntimeException(e);
        }
    }

    /**
     * This method serializes the arg SecurityUserBean list into an xml document.
     *
     * @param users SecurityUserBean list to be serialized
     * @return SecurityUsers xml document
     */
    public static String createDocumentFrom(Collection<SecurityUserBean> users) {
        SecurityUsersDocument doc = SecurityUsersDocument.Factory.newInstance();
        if (null != users) {
            SecurityUsersType usersType = SecurityUserElementWriter.createSecurityUsersElementFrom(
                users);
            doc.setSecurityUsers(usersType);
        }
        return docToString(doc);
    }

    private static String docToString(XmlObject doc) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            doc.save(outputStream);
        } catch (IOException e) {
            throw new DuraCloudRuntimeException(e);
        } finally {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                throw new DuraCloudRuntimeException(e);
            }
        }
        return outputStream.toString();
    }

}