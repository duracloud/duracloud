/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig.user;

import java.util.List;



public class SingleSelectUserConfig extends SelectableUserConfig{
    private static final long serialVersionUID = -2912715735337021361L;

    public SingleSelectUserConfig(String name, String displayName, List<Option> options){
        super(name,displayName, options);
        
        boolean hasSelected = false;
        for(Option o : options){
            if(o.isSelected()){
                if(hasSelected){
                    throw new IllegalArgumentException("the option list contains more than one selected option");
                }else{
                    hasSelected = true;
                }
            }
        }
    }
    
    
    public Option getSelectedOption(){
        for(Option o : getOptions()){
            if(o.isSelected()){
                return o;
            }
        }
        
        return null;
    }
    
    
    public String getSelectedValue(){
        Option selected = getSelectedOption();
        if(selected != null){
            return selected.getValue();
        }
        
        return null;
    }
    
    public InputType getInputType() {
        return InputType.SINGLESELECT;
    }

}
