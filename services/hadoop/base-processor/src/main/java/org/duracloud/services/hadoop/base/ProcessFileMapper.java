/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.base;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Mapper used to process files.
 *
 * @author: Bill Branan
 * Date: Aug 5, 2010
 */
public class ProcessFileMapper extends MapReduceBase
	implements Mapper<Text, Text, Text, Text>
{
    public static final String LOCAL_FS = "file://";

    public static final String RESULT = "result";
    public static final String SUCCESS = "success";
    public static final String FAILURE = "failure";

    public static final String INPUT_PATH = "input-file-path";
    public static final String RESULT_PATH = "result-file-path";
    public static final String ERR_MESSAGE = "error-message";

    protected JobConf jobConf;
    protected Map<String, String> resultInfo;

    public ProcessFileMapper() {
        resultInfo = new HashMap<String, String>();
    }
    
    @Override
    public void configure(JobConf job){
        this.jobConf = job;
    }

    /**
     * Performs the actual file processing.
     */
    @Override
    public void map(Text key,
                    Text value,
                    OutputCollector<Text, Text> output,
                    Reporter reporter) throws IOException {
        String filePath = key.toString();
        String outputPath = value.toString();

        File localFile = null;
        File resultFile = null;
        try {
            reporter.setStatus("Processing file: " + filePath);
            System.out.println("Starting map processing for file: " + filePath);
            resultInfo.put(INPUT_PATH, filePath);

            // Copy the input file to local storage
            Path remotePath = new Path(filePath);
            localFile = copyFileLocal(remotePath);

            // Process the local file
            resultFile = processFile(localFile);

            System.out.println("File processing complete, result file " +
                               "generated: " + resultFile.getName());

            // Move the result file to the output location
            String finalResultFilePath =
                moveToOutput(resultFile, resultFile.getName(), outputPath);

            // Collect result information
            resultInfo.put(RESULT, SUCCESS);
            resultInfo.put(RESULT_PATH, finalResultFilePath);

            System.out.println("Map processing completed successfully for: " +
                               filePath);
        } catch(IOException e) {
            resultInfo.put(RESULT, FAILURE);
            resultInfo.put(ERR_MESSAGE, e.getMessage());
            
            System.out.println("Map processing failed for: " +
                               filePath + " due to: " + e.getMessage());
            e.printStackTrace(System.err);
        } finally {
            // Delete the local file
            if(localFile != null && localFile.exists()) {
                FileUtils.deleteQuietly(localFile);
            }

            // Delete the result file
            if(resultFile != null && resultFile.exists()) {
                FileUtils.deleteQuietly(resultFile);
            }

            output.collect(new Text(collectResult()), new Text(""));

            reporter.setStatus("Processing complete for file: " + filePath);
        }
    }

    /**
     * Copies a file from a remote file system to local storage
     *
     * @param remotePath path to remote file
     * @return local file
     */
    protected File copyFileLocal(Path remotePath) throws IOException {
        String fileName = remotePath.getName();

        FileSystem fs = remotePath.getFileSystem(new JobConf());

        if(fs.isFile(remotePath)) {
            File localFile = new File(getTempDir(), fileName);
            Path localPath = new Path(LOCAL_FS + localFile.getAbsolutePath());

            System.out.println("Copying file (" + fileName +
                               ") to local file system");

            fs.copyToLocalFile(remotePath, localPath);

            if(localFile.exists()) {
                System.out.println("File moved to local storage successfully.");
                return localFile;
            } else {
                String error = "Failure attempting to move remote file (" +
                    fileName + ") to local filesystem; local file (" +
                    localFile.getAbsolutePath() + ") not found after transfer.";
                System.out.println(error);
                throw new IOException(error);
            }
        } else {
            String error = "Failure attempting to access remote file (" +
                fileName + "), the file could not be found";
            System.out.println(error);
            throw new IOException(error);
        }
    }

    /**
     * Processes a file and produces a result file. The result file should
     * be named as intended for the final output file.
     *
     * A default implementation is provided, but this method should be
     * overridden by subclasses.
     *
     * @param file the file to be processed
     * @return the file resulting from the processing
     */
    protected File processFile(File file) throws IOException {
        String fileName = "result-" + file.getName();        
        if(!fileName.endsWith(".txt")) {
            fileName += ".txt";
        }
        File resultFile = new File(getTempDir(), fileName);

        String outputText = "Processed local file: " + file.getAbsolutePath() +
                            " in ProcessFileMapper";
        FileUtils.writeStringToFile(resultFile, outputText, "UTF-8");
        return resultFile;
    }

    /**
     * Moves the result file to the output location with the given filename.
     *
     * @param resultFile the file to move to output
     * @param fileName the name to give the file in the output filesystem
     * @param outputPath the path to where the file should be written
     * @return the path of the new file at the output location
     */
    protected String moveToOutput(File resultFile,
                                  String fileName,
                                  String outputPath) throws IOException {
            if(outputPath != null) {
                Path resultFilePath =
                    new Path(LOCAL_FS + resultFile.getAbsolutePath());
                Path outputFilePath = new Path(outputPath, fileName);

                System.out.println("Moving file: " + resultFilePath.toString() +
                                   " to output " + outputFilePath.toString());

                FileSystem outputFS =
                    outputFilePath.getFileSystem(new JobConf());
                outputFS.moveFromLocalFile(resultFilePath, outputFilePath);

                return outputFilePath.toString();
            } else {
                String error = "Output path is null, not able to " +
                               "store result of processing local file";
                System.out.println(error);
                throw new IOException(error);
            }
    }

    /**
     * Collects the result of the mapping process. This method may be
     * overridden to provide more specific result info.
     */
    protected String collectResult() throws IOException {
        String result =
            RESULT + "=" + resultInfo.get(RESULT)
            + ", " + INPUT_PATH + "=" + resultInfo.get(INPUT_PATH)
            + ", " + RESULT_PATH + "=" + resultInfo.get(RESULT_PATH);

        String errMsg = resultInfo.get(ERR_MESSAGE);
        if(errMsg != null) {
            result += ", " + ERR_MESSAGE + "=" + errMsg;
        }

        return result;
    }

    /**
     * Retrieves a temporary directory on the local file system.
     */
    public File getTempDir() {
        return new File(System.getProperty("java.io.tmpdir"));
    }

}
