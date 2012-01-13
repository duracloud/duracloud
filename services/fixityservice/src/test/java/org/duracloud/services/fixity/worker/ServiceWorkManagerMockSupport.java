/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.worker;

import org.duracloud.common.util.CountListener;
import org.duracloud.services.fixity.domain.ContentLocation;
import org.duracloud.services.fixity.results.ServiceResultListener;
import org.duracloud.services.fixity.results.ServiceResultListener.State;
import org.duracloud.services.fixity.results.ServiceResultListener.StatusMsg;
import org.easymock.IAnswer;
import org.easymock.classextension.EasyMock;

/**
 * @author Andrew Woods
 *         Date: Aug 9, 2010
 */
public class ServiceWorkManagerMockSupport {
    private final int NUM_WORK_ITEMS = 10000;
    private final static String SPACE_PREFIX = "space-prefix-";
    private final static String CONTENT_PREFIX = "content-prefix-";

    protected int callsMade = 0;
    protected StatusMsg STATUS_MSG = new StatusMsg(0,0,0, State.COMPLETE, "", "");

    protected ServiceWorkload createWorkload() {
        ServiceWorkload wl = EasyMock.createMock("ServiceWorkload",
                                                 ServiceWorkload.class);
        EasyMock.expect(wl.hasNext())
            .andReturn(true)
            .times(NUM_WORK_ITEMS)
            .andReturn(false);

        EasyMock.expect(wl.next()).andAnswer(new ContentLocationAnswer()).times(
            NUM_WORK_ITEMS).andReturn(null);

        wl.registerCountListener(EasyMock.<CountListener>anyObject());
        EasyMock.expectLastCall();

        EasyMock.makeThreadSafe(wl, true);

        EasyMock.replay(wl);
        return wl;
    }

    protected ServiceWorkerFactory createWorkerFactory() {
        Runnable worker = EasyMock.createMock(Runnable.class);
        ServiceWorkerFactory wf = EasyMock.createMock("ServiceWorkerFactory",
                                                      ServiceWorkerFactory.class);
        EasyMock.expect(wf.newWorker(EasyMock.<ContentLocation>anyObject()))
            .andReturn(worker)
            .times(NUM_WORK_ITEMS)
            .andReturn(null);

        EasyMock.replay(wf);
        return wf;
    }

    protected ServiceResultListener createResultListener() {
        ServiceResultListener rl = EasyMock.createMock("ServiceResultListener",
                                                       ServiceResultListener.class);
        EasyMock.expect(rl.getProcessingStatus())
            .andReturn(STATUS_MSG)
            .anyTimes();
        rl.setProcessingState(EasyMock.<ServiceResultListener.State>anyObject());
        EasyMock.expectLastCall().anyTimes();
        EasyMock.makeThreadSafe(rl, true);

        EasyMock.replay(rl);
        return rl;
    }


    private class ContentLocationAnswer implements IAnswer {
        @Override
        public Object answer() throws Throwable {
            return new ContentLocation(SPACE_PREFIX,
                                       CONTENT_PREFIX + callsMade++);
        }
    }
}
