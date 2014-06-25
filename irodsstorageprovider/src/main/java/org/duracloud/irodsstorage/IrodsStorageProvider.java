/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.duracloud.irodsstorage;

import static org.duracloud.storage.domain.StorageAccount.OPTS.BASE_DIRECTORY;
import static org.duracloud.storage.domain.StorageAccount.OPTS.HOST;
import static org.duracloud.storage.domain.StorageAccount.OPTS.PORT;
import static org.duracloud.storage.domain.StorageAccount.OPTS.RESOURCE;
import static org.duracloud.storage.domain.StorageAccount.OPTS.TEMP_PATH;
import static org.duracloud.storage.domain.StorageAccount.OPTS.ZONE;
import static org.duracloud.storage.error.StorageException.RETRY;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProviderBase;
import org.duracloud.storage.util.StorageProviderUtil;

import edu.umiacs.irods.api.IRodsConnection;
import edu.umiacs.irods.api.IRodsRequestException;
import edu.umiacs.irods.api.pi.ErrorEnum;
import edu.umiacs.irods.api.pi.GenQueryEnum;
import edu.umiacs.irods.api.pi.ObjTypeEnum;
import edu.umiacs.irods.api.pi.RodsObjStat_PI;
import edu.umiacs.irods.operation.ConnectOperation;
import edu.umiacs.irods.operation.IrodsOperations;
import edu.umiacs.irods.operation.IrodsOutputStream;
import edu.umiacs.irods.operation.IrodsProxyInputStream;
import edu.umiacs.irods.operation.MetaDataMap;
import edu.umiacs.irods.operation.QueryBuilder;
import edu.umiacs.irods.operation.QueryResult;
import edu.umiacs.irods.operation.UnknownSizeOutputStream;

/**
 * The irods provider assumes that storage spaces are subdirectories in the base directory given to the constructor. All files in the subdir will be listed recursively back to a client as contents of that space.
 * 
 * spaceid - directory name under base directory - spaceid reserved words {spaces,security,stores}
 * 
 * @author toaster
 */
public class IrodsStorageProvider extends StorageProviderBase {

    private String password;
    private String username;
    private String zone;
    private int port;
    private String host;
    private String baseDirectory;
    private String storageResource;
    private String tempPath;
    private static final int BLOCK_SIZE = 32768;

    public IrodsStorageProvider(String username, String password, Map<String, String> options) {
        if (options == null) {
            throw new StorageException("Missing required options");
        }
        this.password = password;
        this.username = username;
        this.zone = getOptionString(ZONE.name(), options);
        this.port = getOptionInt(PORT.name(), options);
        this.host = getOptionString(HOST.name(), options);
        this.baseDirectory = getOptionString(BASE_DIRECTORY.name(), options);
        this.storageResource = getOptionString(RESOURCE.name(), options);
        this.tempPath = getOptionString(TEMP_PATH.name(), options);
        log.info("Creating new irods provider " + username + "#" + zone + "@" + host + ":" + port + baseDirectory + " rsrc " + storageResource);
    }

    private String getOptionString(String name, Map<String, String> options) {
        if (!options.containsKey(name) && options.get(name) != null && !options.get(name).equals("")) {
            throw new StorageException("Missing required option: " + name);
        }
        return options.get(name);
    }

    private int getOptionInt(String name, Map<String, String> options) {
        if (!options.containsKey(name)) {
            throw new StorageException("Missing required option: " + name);
        }

        String value = options.get(name);

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException nfe) {
            throw new StorageException("Option is not an integer : " + name + " value: " + value);
        }
    }

    @Override
    public Iterator<String> getSpaces() {
        ConnectOperation co = new ConnectOperation(host, port, username, password, zone);

        log.info("Listing spaces");
        try {
            return listDirectories(baseDirectory, co.getConnection());
        } catch (IOException e) {
            log.error("Could not connect to iRODS", e);
            throw new StorageException(e);
        }
    }

    private Iterator<String> listDirectories(String path, IRodsConnection connection) {

        QueryBuilder qb;
        QueryResult qr;

        try {

            qb = new QueryBuilder(GenQueryEnum.COL_COLL_NAME);

            qb.eq(GenQueryEnum.COL_COLL_PARENT_NAME, path);
            log.info("Sending query: " + qb);
            qr = qb.execute(connection);

            return new QueryIterator(qr, "", path.length() + 1, GenQueryEnum.COL_COLL_NAME);

        } catch (IRodsRequestException ex) {
            log.error("Error listing directories", ex);
            if (ex.getErrorCode() == ErrorEnum.CAT_NO_ROWS_FOUND) {
                return new QueryIterator(null, null, 0);
            }
            throw new StorageException(ex);
        }

    }

    private class QueryIterator implements Iterator<String> {

        private QueryResult qr;
        private GenQueryEnum[] columns;
        private String seperator;
        private int substr;

        public QueryIterator(QueryResult qr, String seperator, int substr, GenQueryEnum... columns) {
            this.qr = qr;
            this.columns = columns;
            this.seperator = seperator;
            this.substr = substr;
        }

        @Override
        public boolean hasNext() {
            if (qr == null) {
                return false;
            }
            return qr.hasNext();
        }

        @Override
        public String next() {
            if (qr == null || !qr.next()) {
                throw new NoSuchElementException();
            }

            StringBuilder sb = new StringBuilder();
            sb.append(qr.getValue(columns[0]));
            for (int i = 1; i < columns.length; i++) {
                sb.append(seperator);
                sb.append(qr.getValue(columns[i]));
            }
            return sb.toString().substring(substr);

        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove not supported");
        }
    }

    @Override
    public Iterator<String> getSpaceContents(String spaceId, String prefix) {
        ConnectOperation co = new ConnectOperation(host, port, username, password, zone);

        String path;
        if (prefix != null && !prefix.equals("")) {
            path = baseDirectory + "/" + spaceId + "/" + prefix;
        } else {
            path = baseDirectory + "/" + spaceId;
        }

        log.info("listing space contents for " + path);
        try {
            return listRecursiveFiles(path, co.getConnection());
        } catch (IOException e) {
            log.error("Could not connect to iRODS", e);
            throw new StorageException(e);
        }
    }

    private Iterator<String> listRecursiveFiles(String path, IRodsConnection connection) {

        QueryBuilder qb;
        QueryResult qr;

        try {
            qb = new QueryBuilder(GenQueryEnum.COL_COLL_NAME, GenQueryEnum.COL_DATA_NAME);

            qb.mCmp(GenQueryEnum.COL_COLL_NAME, new QueryBuilder.Condition(QueryBuilder.ConditionType.LIKE, path + "/%"), new QueryBuilder.Condition(QueryBuilder.ConditionType.EQ, path));
            qr = qb.execute(connection);

            return new QueryIterator(qr, "/", path.length() + 1, GenQueryEnum.COL_COLL_NAME, GenQueryEnum.COL_DATA_NAME);

        } catch (IRodsRequestException ex) {
            log.error("Error listing directories", ex);
            if (ex.getErrorCode() == ErrorEnum.CAT_NO_ROWS_FOUND) {
                return new QueryIterator(null, null, 0);
            }
            throw new StorageException(ex);
        }

    }

    @Override
    public List<String> getSpaceContentsChunked(String spaceId, String prefix, long maxResults, String marker) {

        ConnectOperation co = new ConnectOperation(host, port, username, password, zone);
        List<String> retList = new ArrayList<String>();

        String spacepath = baseDirectory + "/" + spaceId;
        String querypath;
        if (prefix != null && !prefix.equals("")) {
            querypath = spacepath + "/" + prefix;
        } else {
            querypath = spacepath;
        }

        log.info(querypath + " prefix " + prefix + " max " + maxResults + " start " + marker);
        if (maxResults > Integer.MAX_VALUE) {
            throw new StorageException("Cannot return list of size: " + maxResults);
        }

        QueryBuilder qb;
        QueryResult qr;

        try {

            qb = new QueryBuilder(GenQueryEnum.COL_COLL_NAME, GenQueryEnum.COL_DATA_NAME);
            qb.mCmp(GenQueryEnum.COL_COLL_NAME, new QueryBuilder.Condition(QueryBuilder.ConditionType.LIKE, querypath + "/%"), new QueryBuilder.Condition(QueryBuilder.ConditionType.EQ, querypath));

            log.info("Sending query " + qb);
            qr = qb.execute(co.getConnection());

            String markerPath = spaceId + "/" + marker;

            if (marker != null && !marker.equals("")) {
                while (qr.next() && !pathMatches(qr, markerPath))
                    ;
            }

            qr.resetReturnCount();
            qr.setMaxReturned((int) maxResults);

            while (qr.next()) {
                String dir = qr.getValue(GenQueryEnum.COL_COLL_NAME);
                String file = qr.getValue(GenQueryEnum.COL_DATA_NAME);
                String resultPath = dir + "/" + file;
                resultPath = resultPath.substring(spacepath.length() + 1);
                retList.add(resultPath);
                log.info("Retrieving path: " + resultPath);
            }
            return retList;

        } catch (IOException ex) {
            log.error("Error listing directories", ex);
            if (ex instanceof IRodsRequestException && ((IRodsRequestException) ex).getErrorCode() == ErrorEnum.CAT_NO_ROWS_FOUND) {
                return retList;
            }
            throw new StorageException(ex);
        }
    }

    private boolean pathMatches(QueryResult qr, String pathSuffix) {
        String dir = qr.getValue(GenQueryEnum.COL_COLL_NAME);
        String file = qr.getValue(GenQueryEnum.COL_DATA_NAME);
        String fullPath = dir + "/" + file;

        return fullPath.endsWith(pathSuffix);
    }

    @Override
    public void createSpace(String spaceId) {
        ConnectOperation co = new ConnectOperation(host, port, username, password, zone);
        try {
            IrodsOperations io = new IrodsOperations(co);
            io.mkdir(baseDirectory + "/" + spaceId);
            log.info("Created space/directory: " + baseDirectory + "/" + spaceId);
        } catch (IOException e) {
            log.error("Could not connect to iRODS", e);
            throw new StorageException(e);
        }

    }

    @Override
    public String addContent(String spaceId, String contentId, String contentMimeType, Map<String, String> userProperties, long contentSize, String contentChecksum, InputStream content) {
        String path = baseDirectory + "/" + spaceId + "/" + contentId;

        log.info("Writing to irods path: " + path + " resource: " + storageResource);

        ConnectOperation co = new ConnectOperation(host, port, username, password, zone);
        byte[] buffer = new byte[BLOCK_SIZE];

        try {
            // if there's a directory/space/catalog path in the contentID
            if (contentId.contains("/")) {
                // make sure the contentID prefixed directory/space/catalog exists in the destination
                int lastPos = contentId.lastIndexOf("/");
                String prefixedContentPath = contentId.substring(0, lastPos + 1);
                String newContentID = contentId.substring(lastPos + 1, contentId.length());

                // for each subdirectory in prefixedContentPath, make sure it exists in destination
                String[] subdirs = prefixedContentPath.split("/");
                String appended = "";
                for (String dir : subdirs) {
                    String thisPath = baseDirectory + "/" + spaceId + "/" + appended + dir;
                    IrodsOperations io = new IrodsOperations(co);
                    RodsObjStat_PI stat = io.stat(thisPath);
                    // if the destination directory/collection doesn't exist, create it
                    if (stat == null) {
                        log.info("Creating destination directory/collection for " + newContentID + " at " + appended);
                        io.mkdir(thisPath);
                    }
                    appended += dir + "/";
                }
            }
            OutputStream ios;

            if (contentSize > 0) {
                ios = new IrodsOutputStream(co.getConnection(), path, storageResource, contentSize);
            } else {
                ios = new UnknownSizeOutputStream(co.getConnection(), path, storageResource, true);
            }
            int read = 0;
            long total = 0;
            while ((read = content.read(buffer)) > -1) {
                total += read;
                ios.write(buffer, 0, read);
            }
            ios.close();
            log.info("Finished writing irods path: " + path + " resource: " + storageResource + " actual read: " + total + " contentSize: " + contentSize);

            if (userProperties != null) {
                MetaDataMap mDataMap = new MetaDataMap(path, co);
                mDataMap.clear();
                for (String e : userProperties.keySet()) {
                    mDataMap.put(e, userProperties.get(e), null);
                }
            }

            return new IrodsOperations(co).stat(path).getChksum();
        } catch (IOException e) {
            log.error("Error ingesting file", e);
            throw new StorageException(e);
        }
    }

    @Override
    public String copyContent(String sourceSpaceId, String sourceContentId, String destSpaceId, String destContentId) {
        String sourcePath = baseDirectory + "/" + sourceSpaceId + "/" + sourceContentId;
        String destPath = baseDirectory + "/" + destSpaceId + "/" + destContentId;
        ConnectOperation co = new ConnectOperation(host, port, username, password, zone);
        RodsObjStat_PI result = null;
        try {
            IrodsOperations io = new IrodsOperations(co);
            io.cp(sourcePath, destPath);
            result = io.stat(destPath);
        } catch (IOException e) {
            log.error("Could not connect to iRODS", e);
            throw new StorageException(e);
        }
        if (result != null) {
            return StorageProviderUtil.compareChecksum(this, sourceSpaceId, sourceContentId, result.getChksum());
        } else {
            return null;
        }
    }

    @Override
    public InputStream getContent(String spaceId, String contentId) {
        // log.info("getContent -> space: " + spaceId + " content:" + contentId);
        String path = baseDirectory + "/" + spaceId + "/" + contentId;
        ConnectOperation co = new ConnectOperation(host, port, username, password, zone);
        // log.info("ConnectOperation host:" + host + " port:" + port + " username:" + username + " password:" + password + " zone:" + zone);
        try {
            // IrodsOperations io = new IrodsOperations(co);
            // RodsObjStat_PI stat = io.stat(path);
            // ObjTypeEnum type = stat.getObjType();
            // log.info("Opening inputstream to irods path: " + path + " type " + type);
            IrodsProxyInputStream pxyInputStream = new IrodsProxyInputStream(path, tempPath, co);
            // log.info("IrodsProxyInputStream created");
            return pxyInputStream;
        } catch (IOException e) {
            log.error("Could not connect to iRODS", e);
            throw new StorageException(e);
        }
    }

    @Override
    public void deleteContent(String spaceId, String contentId) {
        String path = baseDirectory + "/" + spaceId + "/" + contentId;
        ConnectOperation co = new ConnectOperation(host, port, username, password, zone);
        IrodsOperations ops = new IrodsOperations(co);
        try {
            ObjTypeEnum type;
            if ((type = ops.stat(path).getObjType()) == ObjTypeEnum.DATA_OBJ_T) {
                log.info("Removing irods file " + path);
                ops.rm(path);

            } else {
                log.info("Cannot remove file: " + path + ", type: " + type);
                throw new StorageException("Attempt to remove " + "non-directory path");
            }
        } catch (IOException e) {
            log.error("Could not connect to iRODS", e);
            throw new StorageException(e);
        }
    }

    @Override
    public void setContentProperties(String spaceId, String contentId, Map<String, String> contentProperties) {
        String path;
        if ("spaces".equals(spaceId)) {
            path = baseDirectory + "/" + contentId;
        } else {
            path = baseDirectory + "/" + spaceId + "/" + contentId;
        }
        setProperties(path, contentProperties);
    }

    private void setProperties(String path, Map<String, String> properties) {
        ConnectOperation co = new ConnectOperation(host, port, username, password, zone);

        log.info("Writing properties for " + path + " elements: " + properties.size());
        properties.remove(PROPERTIES_CONTENT_MODIFIED);
        properties.remove(PROPERTIES_CONTENT_SIZE);
        properties.remove(PROPERTIES_CONTENT_MD5);
        properties.remove(PROPERTIES_CONTENT_CHECKSUM);

        try {
            MetaDataMap mDataMap = new MetaDataMap(path, co);
            mDataMap.clear();
            for (String e : properties.keySet()) {
                mDataMap.put(e, properties.get(e), null);
            }
        } catch (IOException e) {
            log.error("Could not connect to iRODS", e);
            throw new StorageException(e);
        }
    }

    @Override
    public Map<String, String> getContentProperties(String spaceId, String contentId) {
        String path;
        if ("spaces".equals(spaceId)) {
            path = baseDirectory + "/" + contentId;
        } else {
            path = baseDirectory + "/" + spaceId + "/" + contentId;
        }
        ConnectOperation co = new ConnectOperation(host, port, username, password, zone);
        try {
            Map<String, String> results = getProperties(path, co);
            IrodsOperations ops = new IrodsOperations(co);
            RodsObjStat_PI stat = ops.stat(path);
            if (stat != null) {
                results.put(PROPERTIES_CONTENT_MODIFIED, formattedDate(stat.getModifyTime()));
                results.put(PROPERTIES_CONTENT_SIZE, Long.toString(stat.getObjSize()));
                results.put(PROPERTIES_CONTENT_CHECKSUM, stat.getChksum());
                results.put(PROPERTIES_CONTENT_MD5, stat.getChksum());
            }
            return results;
        } catch (NotFoundException e) {
            throw e;
        } catch (IOException e) {
            log.error("Could not connect to iRODS", e);
            throw new StorageException(e);
        }
    }

    private Map<String, String> getProperties(String path, ConnectOperation co) {
        Map<String, String> results = new HashMap<String, String>();

        try {

            MetaDataMap mDataMap = new MetaDataMap(path, co);
            if (log.isTraceEnabled()) {
                log.info("Retrieving properties for " + path + mDataMap);
            }
            for (String e : mDataMap.keySet()) {
                results.put(e, mDataMap.get(e));
            }
            return results;
        } catch (FileNotFoundException e) {
            throw new NotFoundException("File Not Found on iRods");
        } catch (IOException e) {
            log.error("Could not connect to iRODS", e);
            throw new StorageException(e, RETRY);
        }
    }

    private String formattedDate(Date created) {
        ISO8601_DATE_FORMAT.setTimeZone(TimeZone.getDefault());
        return ISO8601_DATE_FORMAT.format(created);
    }

    @Override
    protected boolean spaceExists(String spaceId) {
        ConnectOperation co = new ConnectOperation(host, port, username, password, zone);
        String path = baseDirectory + "/" + spaceId;
        IrodsOperations ops = new IrodsOperations(co);
        RodsObjStat_PI stats = null;
        try {
            stats = ops.stat(path);
        } catch (IRodsRequestException e) {
            // DO NOTHING
        }
        return (stats != null);
    }

    @Override
    protected void removeSpace(String spaceId) {
        ConnectOperation co = new ConnectOperation(host, port, username, password, zone);
        try {
            IrodsOperations io = new IrodsOperations(co);
            io.rmdir(baseDirectory + "/" + spaceId, true);
            log.info("Removed space/directory: " + baseDirectory + "/" + spaceId);

        } catch (IOException e) {
            log.error("Could not connect to iRODS", e);
            throw new StorageException(e);
        }
    }

    @Override
    protected Map<String, String> getAllSpaceProperties(String spaceId) {
        ConnectOperation co = new ConnectOperation(host, port, username, password, zone);
        try {
            String path = baseDirectory + "/" + spaceId;
            Map<String, String> properties = getProperties(path, co);
            IrodsOperations ops = new IrodsOperations(co);
            RodsObjStat_PI stat = ops.stat(path);
            properties.put(PROPERTIES_SPACE_CREATED, formattedDate(stat.getModifyTime()));
            // properties.put(PROPERTIES_SPACE_COUNT, getSpaceCount(co,path);
            properties.put(PROPERTIES_SPACE_COUNT, "1+");
            return properties;
        } catch (IOException e) {
            log.error("Could not connect to iRODS", e);
            throw new StorageException(e);
        }
    }

    @Override
    protected void doSetSpaceProperties(String spaceId, Map<String, String> spaceProps) {
        String path = baseDirectory + "/" + spaceId;
        setProperties(path, spaceProps);
    }

}
