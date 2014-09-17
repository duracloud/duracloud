/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.mill.db.util;

import org.duracloud.common.util.SystemPropertiesVerifier;

/**
 * @author Daniel Bernstein
 *         Date: Sep 12, 2014
 */
public class MillJpaPropertiesVerifier extends SystemPropertiesVerifier{
    /**
     * 
     */
    public MillJpaPropertiesVerifier() {
        super(new String[]{
                "mill.db.host",
                "mill.db.port",
                "mill.db.user",
                "mill.db.pass",
                "mill.db.name"
        });
    }
}
