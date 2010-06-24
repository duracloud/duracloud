/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesadmin.control;

import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import org.duracloud.servicesutil.util.ServiceInstaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class InstallController
        extends AbstractController {

    private final Logger log = LoggerFactory.getLogger(InstallController.class);

    private ServiceInstaller serviceInstaller;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response)
            throws Exception {
        ServletOutputStream out = response.getOutputStream();
        out.println("in install-controller");
        out.close();

        if (ServletFileUpload.isMultipartContent(request)) {
            ServletFileUpload upload = new ServletFileUpload();

            try {
                FileItemIterator iter = upload.getItemIterator(request);

                while (iter.hasNext()) {
                    FileItemStream item = iter.next();
                    String name = item.getFieldName();
                    InputStream stream = item.openStream();
                    if (item.isFormField()) {
                        log.info("Form field " + name + " with value "
                                + Streams.asString(stream) + " detected.");
                    } else {
                        log.info("File field " + name + " with file name "
                                + item.getName() + " detected.");
                        // Process the input stream
                        this.getServiceInstaller().install(name, stream);
                    }
                }
            } catch (FileUploadException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            log.warn("Not multipart request.");
        }

        return null;
    }

    public ServiceInstaller getServiceInstaller() {
        return serviceInstaller;
    }

    public void setServiceInstaller(ServiceInstaller serviceInstaller) {
        this.serviceInstaller = serviceInstaller;
    }

}
