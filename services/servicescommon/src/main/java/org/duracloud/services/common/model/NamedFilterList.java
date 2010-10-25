/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.common.model;

import org.duracloud.services.common.error.ServiceException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class provides a container for lists of name/value pairs (i.e. filters),
 * that each are associated with an id or name.
 *
 * @author Andrew Woods
 *         Date: Jan 27, 2010
 */
public class NamedFilterList {

    private List<NamedFilter> namedFilters;

    public NamedFilterList(List<NamedFilter> namedFilters) {
        this.namedFilters = namedFilters;
    }

    /**
     * This method returns all names of contained filters.
     *
     * @return list of names
     */
    public List<String> getNames() {
        List<String> names = new ArrayList<String>();
        for (NamedFilter filter : namedFilters) {
            names.add(filter.getName());
        }
        return names;
    }

    /**
     * This method return the filters associated with the arg name.
     *
     * @param name of filter
     * @return filter associated with arg name
     * @throws ServiceException if no filter existed for arg name.
     */
    public NamedFilter getFilter(String name) throws ServiceException {
        for (NamedFilter filter : namedFilters) {
            if (name.equals(filter.getName())) {
                return filter;
            }
        }
        throw new ServiceException("Filter not found: " + name);
    }

    /**
     * This inner class contains the name/value pair filters and the name
     * associated with them.
     */
    public static class NamedFilter {
        private String name;
        private Map<String, String> filters;

        public NamedFilter(String name, Map<String, String> filters) {
            this.name = name;
            this.filters = filters;
        }

        public String getName() {
            return name;
        }

        public Set<String> getFilterTargets() {
            return filters.keySet();
        }

        public String getFilterValue(String target) {
            return filters.get(target);
        }
    }
}
