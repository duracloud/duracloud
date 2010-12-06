/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.util;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.duracloud.serviceconfig.user.MultiSelectUserConfig;
import org.duracloud.serviceconfig.user.Option;
import org.duracloud.serviceconfig.user.SingleSelectUserConfig;
import org.duracloud.serviceconfig.user.TextUserConfig;
import org.duracloud.serviceconfig.user.UserConfig;
import org.duracloud.serviceconfig.user.UserConfigMode;
import org.duracloud.serviceconfig.user.UserConfigModeSet;


public class ServiceInfoUtil {
    
    /**
     * 
     * @param userConfig
     * @param parameters
     * @return true if value changed
     */
    public static boolean applyValues(TextUserConfig userConfig, Map<String,String> parameters, String namespace){
        String newValue = parameters.get(namespace+"."+userConfig.getName());
        String oldValue = userConfig.getValue();
        userConfig.setValue(newValue);

        return !StringUtils.equals(newValue, oldValue);
    }

    /**
     * 
     * @param userConfig
     * @param parameters
     * @return true if value changed
     */
    public static boolean applyValues(SingleSelectUserConfig userConfig, Map<String,String> parameters,String namespace){
        String newValue = parameters.get(namespace+"."+userConfig.getName());
        String oldValue = userConfig.getSelectedValue();
        userConfig.select(newValue);
        return !StringUtils.equals(newValue, oldValue);
    }


    /**
     * 
     * @param userConfig
     * @param parameters
     * @return true if value changed
     */
    public static boolean applyValues(MultiSelectUserConfig userConfig, Map<String,String> parameters,String namespace){
        String name = namespace+"."+userConfig.getName();
        String oldValue = getValuesAsString(userConfig);
        userConfig.deselectAll();

        for(String key : parameters.keySet()){
            if(key.startsWith(name+"-checkbox-")){
                int index = Integer.valueOf(key.substring(key.lastIndexOf("-")+1));
                userConfig.getOptions().get(index).setSelected(true);
            }
        }
        
        String newValue = getValuesAsString(userConfig);
        
        return !StringUtils.equals(newValue, oldValue);
    }
    
    private static String getValuesAsString(MultiSelectUserConfig uc){
        StringBuffer b = new StringBuffer();
        for(Option o : uc.getOptions()){
            if(o.isSelected()){
                b.append(o.getValue());
            }
        }
        
        return b.toString();
    }

    
    public static void applyValues(List<UserConfigModeSet> userConfigModeSets,
           	HttpServletRequest request) {
    	Map<String,String> map = new HashMap<String,String>();
    	Map parameters = request.getParameterMap();
    	Enumeration<String> e = request.getParameterNames();
    	while(e.hasMoreElements()){
    		String key = e.nextElement();
    		map.put(key, request.getParameter(key));
    	}
    	
    	for(UserConfigModeSet userConfigModeSet : userConfigModeSets){
    		applyValues(userConfigModeSet, map,null);
    	}
	}

    public static void applyValues(UserConfigModeSet userConfigModeSet,
			Map<String, String> map, String namespace) {
    	String name = userConfigModeSet.getName();
    	String key = (namespace == null ? name : namespace + "." + name); 
    	String newValue = map.get(key);
        userConfigModeSet.setValue(newValue);
    	
        boolean hasSingleMode = userConfigModeSet.getModes().size() == 1;

    	for(UserConfigMode mode : userConfigModeSet.getModes()){
    		
    		mode.setSelected(hasSingleMode ?  true : mode.getName().equals(newValue));
    		String modeNameSpace = key + "." +  mode.getName();

    		if(mode.getUserConfigs() != null){
    			for(UserConfig userConfig : mode.getUserConfigs()){
	    			applyValues(userConfig,map,modeNameSpace);
	    		}
    		}

    		if(mode.getUserConfigModeSets() != null){
    			for(UserConfigModeSet modeSet : mode.getUserConfigModeSets()){
    				applyValues(modeSet, map, modeNameSpace);
    			}
    		}
		}
	}

	private static boolean applyValues(UserConfig userConfig,
                                    Map<String, String> parameters,
                                    String namespace) {
        if(userConfig instanceof TextUserConfig){
            return applyValues((TextUserConfig)userConfig, parameters,namespace);
        }else if(userConfig instanceof SingleSelectUserConfig){
            return applyValues((SingleSelectUserConfig)userConfig, parameters,namespace);
        }else if(userConfig instanceof MultiSelectUserConfig){
            return applyValues((MultiSelectUserConfig)userConfig, parameters,namespace);
        }else{
            throw new UnsupportedOperationException(userConfig.getClass().getCanonicalName() + " not recognized.");
        }
    }

}

