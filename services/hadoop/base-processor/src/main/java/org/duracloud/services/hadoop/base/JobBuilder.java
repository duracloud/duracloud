/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.base;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

/**
 * This class constructs a hadoop job to process files.
 *
 * @author: Bill Branan
 * Date: Aug 5, 2010
 */
public class JobBuilder {

    private Map<String, String> initParams;

    /**
     * Constructs a Job builder
     *
     * @param initParams configuration for the job
     */
    public JobBuilder(final Map<String, String> initParams) {
        this.initParams = initParams;
    }

    /**
     * Constructs the JobConf to be used to run the map reduce job.
     */
    public JobConf getJobConf() throws IOException, ParseException {
        String inputPathPrefix = initParams.get(InitParamParser.INPUT_PATH);
        String outputPath = initParams.get(InitParamParser.OUTPUT_PATH);

        inputPathPrefix = appendTrailingSlash(inputPathPrefix);
        outputPath = appendTrailingSlash(outputPath);

        System.out.println(
            "Creating job to process files in " + inputPathPrefix +
            " and store results in " + outputPath);

        JobConf conf = new JobConf(JobBuilder.class);
        conf.setJobName(getJobName());
        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(Text.class);

        // Configure mappper
        conf.setMapperClass(getMapper());

        // Configure reducer
        conf.setReducerClass(getReducer());
        conf.setNumReduceTasks(1);

        // Configure input path
        WholeFileInputFormat.addInputPath(conf, new Path(inputPathPrefix));
        conf.setInputFormat(getInputFormat());

        // Configure output path
        AltTextOutputFormat.setOutputPath(conf, new Path(outputPath));
        conf.setOutputFormat(getOutputFormat());

        // Other config
        conf.setCompressMapOutput(false);

        for(String paramKey : initParams.keySet()) {
            conf.set(paramKey, initParams.get(paramKey));
        }

        return conf;
    }

    private static String appendTrailingSlash(String path) {
        if (!path.endsWith("/")) {
            path += "/";
        }
        return path;
    }

    /**
     * Retrieves the name of the hadoop job.
     * <p/>
     * This method can be overridden to provide an alternate job name.
     */
    protected String getJobName() {
        return "ProcessFiles";
    }

    /**
     * Retrieves the mapper class which will be used for perform the hadoop
     * mapping tasks. The default mapper performs a simple file processing task.
     * <p/>
     * This method can be overridden to provide an alternate mapper
     * implementation class, possibly a subclass of the default mapper.
     */
    protected Class getMapper() {
        return ProcessFileMapper.class;
    }

    /**
     * Retrieves the reducer class which will be used to perform the hadoop
     * reduction tasks. The default reducer simply collects all output name/value
     * pairs and writes it to an output file.
     * <p/>
     * This method can be overridden to provide an alternate reducer
     * implementation class.
     */
    protected Class getReducer() {
        return ResultsReducer.class;
    }

    /**
     * Retrieves the input format which will be used to determine the input
     * to the mapper functions.
     * <p/>
     * This method can be overridden to provide an alternate input format
     * implementation class. Note that the returned class is expected to be a
     * subclass of WholeFileInputFormat 
     */
    protected Class getInputFormat() {
        return WholeFileInputFormat.class;
    }

    /**
     * Retrieves the output format which will be used to write output files.
     * <p/>
     * This method can be overridden to provide an alternate output format
     * implementation class, such as to set the output file name. Note that
     * the returned class is expected to be a subclass of AltTextOutputFormat 
     */
    protected Class getOutputFormat() {
        return AltTextOutputFormat.class;
    }

}
