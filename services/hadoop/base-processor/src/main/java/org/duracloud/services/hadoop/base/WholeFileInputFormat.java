/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.base;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.InvalidInputException;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Input format which defines that files are not split and uses the
 * SimpleFileRecordReader to produce key/value pairs based only on file path.
 *
 * @author: Bill Branan
 * Date: Aug 5, 2010
 */
public class WholeFileInputFormat extends FileInputFormat {

    @Override
    protected boolean isSplitable(FileSystem fs, Path filename) {
        return false;
    }

    @Override
    public RecordReader getRecordReader(InputSplit inputSplit,
                                        JobConf jobConf,
                                        Reporter reporter) throws IOException {
        return new SimpleFileRecordReader((FileSplit) inputSplit,
                                          jobConf,
                                          reporter);
    }

    /**
     * This method overrides FileInputFormat.listStatus() in order to
     * recursively collect the FileStatus objects from the input-path-dirs.
     *
     * @param job
     * @return
     * @throws IOException
     */
    @Override
    protected FileStatus[] listStatus(JobConf job) throws IOException {
        Path[] dirs = getInputPaths(job);
        if (dirs.length == 0) {
            throw new IOException("No input paths specified in job");
        }

        List<FileStatus> result = new ArrayList<FileStatus>();
        List<IOException> errors = new ArrayList<IOException>();

        // creates a MultiPathFilter with the hiddenFileFilter and the
        // user provided one (if any).
        List<PathFilter> filters = new ArrayList<PathFilter>();
        filters.add(new HiddenPathFilter());
        PathFilter jobFilter = getInputPathFilter(job);
        if (jobFilter != null) {
            filters.add(jobFilter);
        }
        PathFilter inputFilter = new MultiPathFilter(filters);

        for (Path p : dirs) {
            FileSystem fs = p.getFileSystem(job);
            FileStatus[] matches = fs.globStatus(p, inputFilter);
            if (matches == null) {
                errors.add(new IOException("Input path does not exist: " + p));
            } else if (matches.length == 0) {
                errors.add(new IOException(
                    "Input Pattern " + p + " matches 0 files"));
            } else {
                for (FileStatus globStat : matches) {
                    getStats(globStat, result, fs, inputFilter);
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new InvalidInputException(errors);
        }
        System.out.println("Total input paths to process : " + result.size());
        return result.toArray(new FileStatus[result.size()]);
    }

    private void getStats(FileStatus globStat,
                          List<FileStatus> result,
                          FileSystem fs,
                          PathFilter inputFilter) throws IOException {
        System.out.println(
            "globStat (dir=" + globStat.isDir() + ")" + globStat.getPath());
        if (globStat.isDir()) {
            for (FileStatus stat : fs.listStatus(globStat.getPath(),
                                                 inputFilter)) {
                System.out.println("stat: " + stat.getPath());
                getStats(stat, result, fs, inputFilter);
            }
            
        } else {
            System.out.println("adding stat: " + globStat.getPath());
            result.add(globStat);
        }
    }

    private static class HiddenPathFilter implements PathFilter {
        public boolean accept(Path p) {
            String name = p.getName();
            return !name.startsWith("_") && !name.startsWith(".");
        }
    }

    private static class MultiPathFilter implements PathFilter {
        private List<PathFilter> filters;

        public MultiPathFilter(List<PathFilter> filters) {
            this.filters = filters;
        }

        public boolean accept(Path path) {
            for (PathFilter filter : filters) {
                if (!filter.accept(path)) {
                    return false;
                }
            }
            return true;
        }
    }

}

