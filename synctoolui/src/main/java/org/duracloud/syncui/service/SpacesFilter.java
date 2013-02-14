/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.service;

import java.util.ArrayList;
import java.util.List;

import org.duracloud.common.constant.Constants;
import org.springframework.stereotype.Component;

/**
 * This class filters out the duracloud system spaces from a list of spaces.
 * @author Daniel Bernstein
 * 
 */
@Component("spacesFilter")
public class SpacesFilter {

    public List<String> filter(List<String> spaces){
        List<String> results = new ArrayList<String>();
        results.addAll(spaces);
        results.removeAll(Constants.SYSTEM_SPACES);
        return results;
    }

}
