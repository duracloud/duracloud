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


public abstract class SelectableUserConfig extends UserConfig {

    private static final long serialVersionUID = 560564548722671194L;

    private List<Option> options;

    public SelectableUserConfig(String name,
                                String displayName,
                                List<Option> options) {
        super(name, displayName);
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

        if (options != null ? !options.equals(that.options) :
            that.options != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (options != null ? options.hashCode() : 0);
        return result;
    }
}
