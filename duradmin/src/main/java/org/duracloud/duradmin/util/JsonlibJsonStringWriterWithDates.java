/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.duradmin.util;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonBeanProcessor;
import net.sf.json.processors.JsonValueProcessorMatcher;

import org.duracloud.snapshot.dto.SnapshotHistoryItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.view.json.JsonWriterConfiguratorTemplateRegistry;
import org.springframework.web.servlet.view.json.writer.jsonlib.AllwaysMatchingValueProcessorMatcher;
import org.springframework.web.servlet.view.json.writer.jsonlib.JsonlibJsonStringWriter;
import org.springframework.web.servlet.view.json.writer.jsonlib.JsonlibJsonWriterConfiguratorTemplate;
import org.springframework.web.servlet.view.json.writer.jsonlib.NullPropertyFilter;
import org.springframework.web.servlet.view.json.writer.jsonlib.PropertyEditorRegistryValueProcessor;

/**
 * This is an exact copy of JsonlibJsonStringWriter
 * We're only interested in @Override of convertAndWrite() to extend functionality through JsonConfig to allow of adding of custom parsing for Date objects
 * 
 * @author Gad Krumholz
 * Date: 7/02/2015
 */
public class JsonlibJsonStringWriterWithDates extends JsonlibJsonStringWriter {
    protected final Logger logger = LoggerFactory.getLogger(JsonlibJsonStringWriterWithDates.class);
    
    @Override
    @SuppressWarnings("rawtypes")
    public void convertAndWrite(Map model, JsonWriterConfiguratorTemplateRegistry configuratorTemplateRegistry, Writer writer, BindingResult br) throws IOException {

        JsonConfig jsonConfig = null;

        JsonlibJsonWriterConfiguratorTemplate configuratorTemplate = (JsonlibJsonWriterConfiguratorTemplate) configuratorTemplateRegistry.findConfiguratorTemplate(JsonlibJsonWriterConfiguratorTemplate.class.getName());

        if (isEnableJsonConfigSupport() && configuratorTemplate != null) {
            jsonConfig = (JsonConfig) configuratorTemplate.getConfigurator();
        }

        if (jsonConfig == null)
            jsonConfig = new JsonConfig();

        if (jsonConfig.getJsonPropertyFilter() == null && !isKeepNullProperties())
            jsonConfig.setJsonPropertyFilter(new NullPropertyFilter());

        if (jsonConfig.getJsonValueProcessorMatcher().getClass().equals(JsonValueProcessorMatcher.DEFAULT.getClass())) {
            PropertyEditorRegistry per = null;
            String objektName = null;

            if (br != null) {
                per = br.getPropertyEditorRegistry();
                objektName = br.getObjectName();
            }

            PropertyEditorRegistryValueProcessor valueProzessor = new PropertyEditorRegistryValueProcessor(per);
            valueProzessor.setConvertAllMapValues(isConvertAllMapValues());
            valueProzessor.setObjektName(objektName);

            jsonConfig.registerJsonValueProcessor(AllwaysMatchingValueProcessorMatcher.class, valueProzessor);
            jsonConfig.setJsonValueProcessorMatcher(new AllwaysMatchingValueProcessorMatcher());

            jsonConfig.addJsonEventListener(valueProzessor);
            jsonConfig.enableEventTriggering();

        }

        // add a bean processor for SnapshotHistoryItem because we want it's historyDate (Date) as epoch time
        jsonConfig.registerJsonBeanProcessor(SnapshotHistoryItem.class, new SnapshotHistoryItemJsonBeanProcessor());

        JSON json = JSONSerializer.toJSON(model, jsonConfig);
        if (logger.isDebugEnabled())
            logger.debug(json.toString());

        json.write(writer);
        writer.flush();
    }

    public static class SnapshotHistoryItemJsonBeanProcessor implements JsonBeanProcessor {
        @Override
        public JSONObject processBean(Object paramObject, JsonConfig paramJsonConfig) {
            SnapshotHistoryItem snapmeta = (SnapshotHistoryItem) paramObject;
            JSONObject history = new JSONObject();
            history.put("historyDate", snapmeta.getHistoryDate().getTime());
            history.put("history", "'" + snapmeta.getHistory() + "'"); // wrap single quotes around the string so that JSONObject doesn't try to parse the String as possibly valid JSON String
            return history;
        }
    }
}
