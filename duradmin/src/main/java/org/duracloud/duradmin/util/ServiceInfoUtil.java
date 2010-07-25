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


public class ServiceInfoUtil {
    
    /**
     * 
     * @param userConfig
     * @param parameters
     * @return true if value changed
     */
    public static boolean applyValues(TextUserConfig userConfig, Map<String,String> parameters){
        String newValue = parameters.get(userConfig.getName());
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
    public static boolean applyValues(SingleSelectUserConfig userConfig, Map<String,String> parameters){
        String newValue = parameters.get(userConfig.getName());
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
    public static boolean applyValues(MultiSelectUserConfig userConfig, Map<String,String> parameters){
        String name = userConfig.getName();
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

    
    public static void applyValues(List<UserConfig> userConfigs,
                                   Map<String, String> parameters) {
        for(UserConfig userConfig : userConfigs){
            applyValues(userConfig,parameters);
        }
    }

    public static void applyValues(List<UserConfig> userConfigs,
           	HttpServletRequest request) {
    	Map<String,String> map = new HashMap<String,String>();
    	Map parameters = request.getParameterMap();
    	Enumeration<String> e = request.getParameterNames();
    	while(e.hasMoreElements()){
    		String key = e.nextElement();
    		map.put(key, request.getParameter(key));
    	}
    	
		for(UserConfig userConfig : userConfigs){
			applyValues(userConfig,map);
		}
	}

    private static boolean applyValues(UserConfig userConfig,
                                    Map<String, String> parameters) {
        if(userConfig instanceof TextUserConfig){
            return applyValues((TextUserConfig)userConfig, parameters);
        }else if(userConfig instanceof SingleSelectUserConfig){
            return applyValues((SingleSelectUserConfig)userConfig, parameters);
        }else if(userConfig instanceof MultiSelectUserConfig){
            return applyValues((MultiSelectUserConfig)userConfig, parameters);
        }else{
            throw new UnsupportedOperationException(userConfig.getClass().getCanonicalName() + " not recognized.");
        }
    }

}

