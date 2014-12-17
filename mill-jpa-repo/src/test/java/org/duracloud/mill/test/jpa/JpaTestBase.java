/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.mill.test.jpa;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.duracloud.mill.test.AbstractTestBase;
import org.easymock.Capture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author Daniel Bernstein
 *         Date: Sep 3, 2014
 */
public abstract class JpaTestBase<T> extends AbstractTestBase{

    protected void verifyPageable(Capture<Pageable> capture) {
        Pageable pageable = capture.getValue();
        assertNotNull(pageable);
        assertEquals(0,pageable.getPageNumber());
    }

    protected void verifyIterator(int count, Iterator it) {
        int recount = 0;
        while(it.hasNext()){
            it.next();
            recount++;
        }
        assertEquals(count, recount);
    }
    
    protected Page<T> setupPage(int count) {
        Page<T> page = createMock(Page.class);
        
         List<T> items = new ArrayList<>();
        for(int i = 0; i < count; i++){
            items.add(create());
        }
        expect(page.getContent()).andReturn(items);
        expect(page.getTotalPages()).andReturn(1);
        return page;
    }

    /**
     * @return
     */
    protected abstract T create();

}
