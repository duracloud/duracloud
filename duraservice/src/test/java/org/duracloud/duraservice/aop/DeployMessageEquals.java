/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.aop;

import org.duracloud.serviceapi.aop.DeployMessage;
import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;

public class DeployMessageEquals implements IArgumentMatcher {
    private DeployMessage deployMessage;

    public DeployMessageEquals(DeployMessage deployMessage) {
        this.deployMessage = deployMessage;
    }

    public boolean matches(Object actual) {
        if (!(actual instanceof DeployMessage)) {
            return false;
        }
        DeployMessage message = (DeployMessage) actual;

        boolean matches = true;

        matches = deployMessage.getServiceId() == message.getServiceId();

        if (!matches) {
            return false;
        }

        if (deployMessage.getServiceHost() != null) {
            matches = deployMessage.getServiceHost()
                .equals(message.getServiceHost());
        } else if (message.getServiceHost() != null) {
            return false;
        }

        if (!matches) {
            return false;
        }

        matches = deployMessage.getDeploymentId() == message.getDeploymentId();

        return matches;
    }

    public void appendTo(StringBuffer buffer) {
        buffer.append("eqDeployMessage(");
        buffer.append(deployMessage.getClass().getName());
        buffer.append(" with service id \"");
        buffer.append(deployMessage.getServiceId());
        buffer.append("\" and with service host \"");
        buffer.append(deployMessage.getServiceHost());
        buffer.append("\")");
    }

    public static <T extends DeployMessage> T eqDeployMessage(T in) {
        EasyMock.reportMatcher(new DeployMessageEquals(in));
        return null;
    }
}
