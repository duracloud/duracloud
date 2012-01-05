/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
/**
 * 
 * @author Daniel Bernstein
 *
 */
public class LineParsingIteratorTest {

    @Test
    public void testLineParsingIteratorReader() {
        String string = "";
        int lineCount = 5;
        for(int i = 0; i < lineCount; i++){
            string +="1\t2\t3\n";
        }
        
        Reader reader =
            new InputStreamReader(new ByteArrayInputStream(string.getBytes()));
        LineParsingIterator it = new LineParsingIterator(reader);

        int count = 0;
        
        while(it.hasNext()){
            List<String> parsedList = it.next();
            Assert.assertEquals(3, parsedList.size());
            for(int i = 0; i < parsedList.size(); i++){
                Assert.assertEquals((i+1)+"", parsedList.get(i));
            }
            count++;
        }
        
        Assert.assertEquals(lineCount, count);
    }

}
