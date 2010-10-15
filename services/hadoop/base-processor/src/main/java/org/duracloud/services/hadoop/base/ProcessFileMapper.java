/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.base;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.hadoop.store.FileWithMD5;
import org.duracloud.services.hadoop.store.JobContentStoreManagerFactory;
import org.duracloud.services.hadoop.store.UriPathUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.duracloud.storage.domain.HadoopTypes.*;

/**
 * Mapper used to process files.
 *
 * @author: Bill Branan
 * Date: Aug 5, 2010
 */
public class ProcessFileMapper extends MapReduceBase implements Mapper<Text, Text, Text, Text> {
    public static final String RESULT = "result";
    public static final String SUCCESS = "success";
    public static final String FAILURE = "failure";

    public static final String INPUT_PATH = "input-file-path";
    public static final String RESULT_PATH = "result-file-path";
    public static final String ERR_MESSAGE = "error-message";

    protected static final UriPathUtil pathUtil = new UriPathUtil();

    protected JobConf jobConf;
    protected Map<String, String> resultInfo;

    private ContentStoreManager storeManager;

    public ProcessFileMapper() {
        resultInfo = new HashMap<String, String>();
    }

    @Override
    public void configure(JobConf job) {
        this.jobConf = job;

        JobContentStoreManagerFactory factory = new JobContentStoreManagerFactory();
        setStoreManager(factory.getContentStoreManager(jobConf));
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

        FileWithMD5 localFile = null;
        File resultFile = null;
        try {
            reporter.setStatus("Processing file: " + filePath);
            System.out.println("Starting map processing for file: " + filePath);
            resultInfo.put(INPUT_PATH, filePath);

            // Copy the input file to local storage
            Path remotePath = new Path(filePath);
            localFile = copyFileLocal(remotePath, reporter);

            // Determine the contentId
            String contentId = pathUtil.getContentId(filePath);
            if(contentId == null) {
                contentId = remotePath.getName();
            }

            // Process the local file
            ProcessResult result = processFile(localFile, contentId);
            if(null != result) {
                resultFile = result.getFile();
                if (null != resultFile) {
                    String resultId = result.getContentId();
                    if(resultId == null) {
                        resultId = resultFile.getName();
                    }
                    storeResultFile(outputPath, resultFile, resultId, reporter);
                }
            }

            // Collect result information
            resultInfo.put(RESULT, SUCCESS);

            System.out.println(
                "Map processing completed successfully for: " + filePath);
        } catch (IOException e) {
            resultInfo.put(RESULT, FAILURE);
            resultInfo.put(ERR_MESSAGE, e.getMessage());

            System.out.println(
                "Map processing failed for: " + filePath + " due to: " +
                    e.getMessage());
            e.printStackTrace(System.err);
        } finally {
            // Delete the local file
            if (localFile != null && localFile.getFile().exists()) {
                FileUtils.deleteQuietly(localFile.getFile());
            }

            // Delete the result file
            if (resultFile != null && resultFile.exists()) {
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
    protected FileWithMD5 copyFileLocal(Path remotePath, Reporter reporter)
        throws IOException {
        reporter.setStatus("Copying file to local");

        String fileName = remotePath.getName();
        File localFile = new File(getTempDir(), fileName);
        boolean toLocal = true;

        String md5 = doCopy(localFile, remotePath, toLocal, reporter);
        return new FileWithMD5(localFile, md5);
    }

    /**
     * Processes a file and produces a ProcessResult, which includes the file
     * and the contentId under which the file should be stored.
     * <p/>
     * A default implementation is provided, but this method should be
     * overridden by subclasses.
     *
     * @param fileWithMD5 the file to be processed
     * @param origContentId the original ID of the file
     * @return ProcessResult including the file resulting from the processing
     *         to be moved to output along with its corresponding content ID
     *         or null if no file result exists
     */
    protected ProcessResult processFile(FileWithMD5 fileWithMD5, String origContentId)
        throws IOException {
        File file = fileWithMD5.getFile();
        String fileName = "result-" + file.getName();
        if (!fileName.endsWith(".txt")) {
            fileName += ".txt";
        }
        File resultFile = new File(getTempDir(), fileName);

        String outputText = "Processed local file: " + file.getAbsolutePath() +
            " in ProcessFileMapper";
        FileUtils.writeStringToFile(resultFile, outputText, "UTF-8");
        return new ProcessResult(resultFile, origContentId);
    }

    private void storeResultFile(String outputPath,
                                 File resultFile,
                                 String resultId,
                                 Reporter reporter) throws IOException {
        System.out.println("File processing complete, result file generated: " +
            resultFile.getName());

        // Move the result file to the output location
        String finalResultFilePath = moveToOutput(resultFile,
                                                  resultId,
                                                  outputPath,
                                                  reporter);

        resultInfo.put(RESULT_PATH, finalResultFilePath);
    }

    /**
     * Moves the result file to the output location with the given filename.
     *
     * @param resultFile the file to move to output
     * @param fileName   the name to give the file in the output filesystem
     * @param outputPath the path to where the file should be written
     * @return the path of the new file at the output location
     */
    protected String moveToOutput(File resultFile,
                                  String fileName,
                                  String outputPath,
                                  Reporter reporter) throws IOException {
        reporter.setStatus("Copying file from local");

        if (null == outputPath) {
            String error = "Output path is null, not able to " +
                "store result of processing local file";
            System.out.println(error);
            throw new IOException(error);
        }

        Path remotePath = new Path(outputPath, fileName);
        boolean toLocal = false;

        doCopy(resultFile, remotePath, toLocal, reporter);
        return remotePath.toString();
    }

    /**
     * This method starts the copy thread and lets the hadoop framework know
     * that it is still alive, even if the file transfer takes a long time.
     * By default, hadoop times-out after ten minutes if it does not hear back
     * from a work node.
     */
    private String doCopy(File localFile,
                        Path remotePath,
                        boolean toLocal,
                        Reporter reporter) throws IOException {
        ContentStore store = getContentStore();
        FileCopier copier = new FileCopier(localFile,
                                           remotePath,
                                           toLocal,
                                           store);
        Thread thread = new Thread(copier);
        thread.start();

        while (thread.isAlive()) {
            sleep(500);
            reporter.progress();
        }

        return copier.getMd5();
    }

    protected ContentStore getContentStore() throws IOException {
        String storeId = getStoreId();
        try {
            return getStoreManager().getContentStore(storeId);

        } catch (ContentStoreException e) {
            System.out.println("Error getting content store: " + storeId);
            throw new IOException(e);
        }
    }

    /**
     * Collects the result of the mapping process. This method may be
     * overridden to provide more specific result info.
     */
    protected String collectResult() throws IOException {
        String result =
            RESULT + "=" + resultInfo.get(RESULT) + ", " + INPUT_PATH + "=" +
                resultInfo.get(INPUT_PATH) + ", " + RESULT_PATH + "=" +
                resultInfo.get(RESULT_PATH);

        String errMsg = resultInfo.get(ERR_MESSAGE);
        if (errMsg != null) {
            result += ", " + ERR_MESSAGE + "=" + errMsg;
        }

        return result;
    }

    /**
     * Retrieves a temporary directory on the local file system.
     */
    private File getTempDir() {
        return new File(System.getProperty("java.io.tmpdir"));
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // do nothing
        }
    }

    public ContentStoreManager getStoreManager() {
        return storeManager;
    }

    public void setStoreManager(ContentStoreManager storeManager) {
        this.storeManager = storeManager;
    }

    private String getStoreId() throws IOException {
        String id = jobConf.get(TASK_PARAMS.DC_STORE_ID.getLongForm());
        if (null == id) {
            try {
                id = getStoreManager().getPrimaryContentStore().getStoreId();

            } catch (ContentStoreException e) {
                System.out.println("Unable the get store id.");
                e.printStackTrace();
                throw new IOException(e);
            }
        }

        return id;
    }

}
