/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig.user;

import java.util.Collections;
import java.util.List;


public abstract class SelectableUserConfig extends UserConfig implements Cloneable {

    private static final long serialVersionUID = 560564548722671194L;

    private List<Option> options;

    public SelectableUserConfig(String name,
                                String displayName,
                                List<Option> options) {
        super(name, displayName);
        init(options);
    }

    public SelectableUserConfig(String name,
                                String displayName,
                                List<Option> options,
                                String exclusion) {
        super(name, displayName, exclusion);
        init(options);
    }

    private void init(List<Option> options) {
        this.options = Collections.unmodifiableList(options);
    }

    public List<Option> getOptions() {
        return options;
    }

    public void deselectAll() {
        for (Option o : options) {
            o.setSelected(false);
        }
    }

    public void select(Option option) {
        if (this instanceof SingleSelectUserConfig) {
            deselectAll();
        }
        option.setSelected(true);
    }

    public void select(String optionValue) {
        for (Option o : options) {
            if (o.getValue().equals(optionValue)) {
                select(o);
                return;
            }
        }
    }
    
    @Override
    public String getDisplayValue() {
        StringBuffer b = new StringBuffer();
        int count = 0;
        for(Option o : options){
            if(o.isSelected()){
                if(count > 0) b.append(", "); 
                b.append(o.getDisplayName());
                count++;
            }
        }
        
        return b.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SelectableUserConfig)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        SelectableUserConfig that = (SelectableUserConfig) o;

        if (options != null ? !optionsEqual(that.options) :
            that.options != null) {
            return false;
        }

        return true;
    }

    private boolean optionsEqual(List<Option> otherOpts) {
        if (null == otherOpts) {
            return false;
        }

        int numFound = 0;
        for (Option opt : options) {
            for (Option otherOpt : otherOpts) {
                if (opt.equals(otherOpt)) {
                    numFound++;
                }
            }
        }
        return numFound == options.size();
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (options != null ? optionsHashCode() : 0);
        return result;
    }

    private int optionsHashCode() {
        int sum = 0;
        for (Option opt : options) {
            sum += opt.hashCode();
        }
        return sum;
    }

    public SelectableUserConfig clone() throws CloneNotSupportedException {
        SelectableUserConfig clone = (SelectableUserConfig)super.clone();
        return clone;
    }
}
