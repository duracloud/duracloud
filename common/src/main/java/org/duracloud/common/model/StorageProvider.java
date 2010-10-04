/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.model;

import java.io.Serializable;

public class StorageProvider
        implements Serializable {

    private static final long serialVersionUID = 1L;

    public StorageProvider(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	private String id;
    private String name;

    public String getId() {
        return id;
    }

	public String getName() {
		return name;
	}

}
