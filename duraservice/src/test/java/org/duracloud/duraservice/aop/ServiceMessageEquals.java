/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.aop;

import org.duracloud.serviceapi.aop.ServiceMessage;
import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;

public class ServiceMessageEquals implements IArgumentMatcher {
    private ServiceMessage serviceMessage;

    public ServiceMessageEquals(ServiceMessage serviceMessage) {
        this.serviceMessage = serviceMessage;
    }

    public boolean matches(Object actual) {
        if (!(actual instanceof ServiceMessage)) {
            return false;
        }
        ServiceMessage message = (ServiceMessage) actual;

        boolean matches = true;

        if(serviceMessage.getServiceId() != null)
            matches = serviceMessage.getServiceId().equals(message.getServiceId());
        else if(message.getServiceId() != null)
            return false;

        if(!matches)
            return false;

        if(serviceMessage.getDeploymentId() != null)
            matches = serviceMessage.getDeploymentId().equals(message.getDeploymentId());
        else if(message.getDeploymentId() != null)
            return false;

        return matches;
    }

    public void appendTo(StringBuffer buffer) {
        buffer.append("eqServiceMessage(");
        buffer.append(serviceMessage.getClass().getName());
        buffer.append(" with service id \"");
        buffer.append(serviceMessage.getServiceId());
        buffer.append("\" and with deployment id \"");
        buffer.append(serviceMessage.getDeploymentId());
        buffer.append("\")");
    }

    public static <T extends ServiceMessage> T eqServiceMessage(T in) {
        EasyMock.reportMatcher(new ServiceMessageEquals(in));
        return null;
    }
}
