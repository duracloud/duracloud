/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.util;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import org.duracloud.services.ComputeService;
import org.duracloud.services.beans.ComputeServiceBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XMLServiceSerializerImpl
        implements ServiceSerializer {

    private final Logger log = LoggerFactory.getLogger(XMLServiceSerializerImpl.class);

    public String serialize(List<ComputeService> services) throws Exception {
        List<String> beans = new ArrayList<String>();
        for (ComputeService service : services) {
            beans.add(service.describe());
        }
        return doSerializeList(beans);
    }

    public List<ComputeServiceBean> deserializeList(String xml) {
        log.debug("arg xml: '" + xml + "'");

        List<ComputeServiceBean> beans = new ArrayList<ComputeServiceBean>();
        List<Object> objs = doDeserializeList(xml);
        for (Object obj : objs) {
            beans.add(new ComputeServiceBean((String) obj));
        }
        return beans;

    }

    private String doSerializeList(List<String> beans) {
        return getXStream().toXML(beans);
    }

    private List<Object> doDeserializeList(String xml) {
        return (List<Object>) getXStream().fromXML(xml);
    }

    public ComputeServiceBean deserializeBean(String xml) {
        return new ComputeServiceBean((String) getXStream().fromXML(xml));
    }

    public String serialize(ComputeServiceBean bean) throws Exception {
        return getXStream().toXML(bean.getServiceName());
    }

    private XStream getXStream() {
        XStream xstream = new XStream(new DomDriver());
        xstream.alias("ComputeServiceBean", ComputeServiceBean.class);

        return xstream;
    }
}
