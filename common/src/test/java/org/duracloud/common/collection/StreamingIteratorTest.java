/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.collection;

import java.util.Arrays;
import java.util.NoSuchElementException;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(EasyMockRunner.class)
public class StreamingIteratorTest extends EasyMockSupport{

    
    @Mock
    private IteratorSource<String> source;
    
    @After
    public void teartDown(){
        verifyAll();
    }
    
    @Test
    public void test() {
        EasyMock.expect(source.getNext()).andReturn(Arrays.asList("1","2", "3"));
        EasyMock.expect(source.getNext()).andReturn(Arrays.asList("4","5", "6"));
        EasyMock.expect(source.getNext()).andReturn(null);
        replayAll();
        StreamingIterator<String> it = new StreamingIterator(source);
        int count = 0;
        while(it.hasNext()){
            it.next();
            count++;
        }
        Assert.assertEquals(6, count);
    }

    
    @Test
    public void testNullSource() {
        EasyMock.expect(source.getNext()).andReturn(null);
        replayAll();
        StreamingIterator<String> it = new StreamingIterator(source);
        int count = 0;
        while(it.hasNext()){
            it.next();
            count++;
        }
        Assert.assertEquals(0, count);
    }
    
    @Test
    public void testNextOnEmptyIterator() {
        EasyMock.expect(source.getNext()).andReturn(null);
        replayAll();
        StreamingIterator<String> it = new StreamingIterator(source);
        
        try{
            it.next();
            Assert.fail();
        }catch(NoSuchElementException ex){
        }
    }
    
    @Test
    public void testRemoveNotSupported() {
        StreamingIterator<String> it = new StreamingIterator(source);
        replayAll();
        try{
            it.remove();
            Assert.fail();
        }catch(UnsupportedOperationException ex){
        }
    }

}
