/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.computeprovider.aop;

import java.lang.reflect.Method;

import org.springframework.aop.AfterReturningAdvice;

public class LoggingAdvice
        implements AfterReturningAdvice {

    public void afterReturning(Object arg0,
                               Method arg1,
                               Object[] arg2,
                               Object arg3) throws Throwable {
        String pre0 = "--------------------------";
        String pre1 = pre0 + "--";
        String pre2 = pre1 + "--";

        System.out.println(pre0 + "advice todo: nothing yet");
        if (arg3 != null && arg3.getClass() != null) {
            System.out.println(pre1 + "object: " + arg3.getClass().getName());
        }
        if (arg1 != null) {
            System.out.println(pre1 + "method: " + arg1.getName());
        }
        if (arg2 != null) {
            for (Object obj : arg2) {
                System.out.print(pre2 + "method-arg: ");
                System.out.println(obj.toString());
            }
        }
    }

}
