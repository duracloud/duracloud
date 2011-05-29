/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.duracloud.irodsstorage;

import edu.umiacs.irods.api.IRodsConnection;
import edu.umiacs.irods.api.IRodsRequestException;
import edu.umiacs.irods.operation.IrodsOperations;
import edu.umiacs.irods.api.pi.ErrorEnum;
import edu.umiacs.irods.api.pi.GenQueryEnum;
import edu.umiacs.irods.api.pi.ObjTypeEnum;
import edu.umiacs.irods.api.pi.RodsObjStat_PI;
import java.io.InputStream;
import java.util.Iterator;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;

import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.provider.StorageProvider;
import edu.umiacs.irods.operation.ConnectOperation;
import edu.umiacs.irods.operation.IrodsProxyInputStream;
import edu.umiacs.irods.operation.IrodsOutputStream;
import edu.umiacs.irods.operation.MetaDataMap;
import edu.umiacs.irods.operation.QueryBuilder;
import edu.umiacs.irods.operation.QueryResult;
import edu.umiacs.irods.operation.UnknownSizeOutputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.TimeZone;
import org.duracloud.storage.error.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.duracloud.storage.domain.StorageAccount.OPTS.*;

/**
 * The irods provider assumes that storage spaces are subdirectories in the
 * base directory given to the constructor. All files in the subdir will be
 * listed recursively back to a client as contents of that space.
 *
 *  spaceid - directory name under base directory
 *  - spaceid reserved words {spaces,security,stores}
 * 
 * @author toaster
 */
public class IrodsStorageProvider implements StorageProvider {

    private final Logger log = LoggerFactory.getLogger(IrodsStorageProvider.class);
    private String baseDirectory;
    private String username;
    private String password;
    private int port;
    private String zone;
    private String host;
    private String storageResource;
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
        log.trace("Creating new irods provider " + username + "#" + zone + "@" + host
                + ":" + port + baseDirectory + " rsrc " + storageResource);

    }

    /**
     * Return a list of irods spaces. IRODS spaces are directories under the baseDirectory
     * of this provider.
     * 
     * @return
     */
    @Override
    public Iterator<String> getSpaces() {
        ConnectOperation co = new ConnectOperation(host, port, username, password, zone);

        log.trace("Listing spaces");
        try {
            return listDirectories(baseDirectory, co.getConnection());
        } catch (IOException e) {
            log.error("Could not connect to iRODS", e);
            throw new StorageException(e);
        }
    }

    /**
     * Prefix is assumed to be part of the collection name.
     * This also has an issue where items in /irods/home/account/dir1 will br
     * returned if /irods/home/account/dir is asked for (ie, spaceId=dir)
     * 
     * @param spaceId
     * @param prefix
     * @return
     */
    @Override
    public Iterator<String> getSpaceContents(String spaceId, String prefix) {
        ConnectOperation co = new ConnectOperation(host, port, username, password, zone);

        String path;
        if (prefix != null && !prefix.equals("")) {
            path = baseDirectory + "/" + spaceId + "/" + prefix;
        } else {
            path = baseDirectory + "/" + spaceId;
        }

        log.trace("listing space contents for " + path);
        try {
            return listRecursiveFiles(path, co.getConnection());
        } catch (IOException e) {
            log.error("Could not connect to iRODS", e);
            throw new StorageException(e);
        }
    }

    @Override
    @SuppressWarnings("empty-statement")
    public List<String> getSpaceContentsChunked(String spaceId, String prefix,
            long maxResults, String marker) {
        ConnectOperation co = new ConnectOperation(host, port, username, password, zone);
        List<String> retList = new ArrayList();

        String spacepath = baseDirectory + "/" + spaceId;
        String querypath;
        if (prefix != null && !prefix.equals("")) {
            querypath = spacepath + "/" + prefix;
        } else {
            querypath = spacepath;
        }

        log.trace(querypath + " prefix " + prefix
                + " max " + maxResults + " start " + marker);
        if (maxResults > Integer.MAX_VALUE) {
            throw new StorageException("Cannot return list of size: " + maxResults);
        }

        QueryBuilder qb;
        QueryResult qr;

        try {

            qb = new QueryBuilder(GenQueryEnum.COL_COLL_NAME, GenQueryEnum.COL_DATA_NAME);
            qb.mCmp(GenQueryEnum.COL_COLL_NAME,
                    new QueryBuilder.Condition(QueryBuilder.ConditionType.LIKE, querypath + "/%"),
                    new QueryBuilder.Condition(QueryBuilder.ConditionType.EQ, querypath));

            log.trace("Sending query " + qb);
            qr = qb.execute(co.getConnection());

            String markerPath = spaceId + "/" + marker;

            if (marker != null && !marker.equals("")) {
                while (qr.next() && !pathMatches(qr, markerPath));
            }

            qr.resetReturnCount();
            qr.setMaxReturned((int) maxResults);


            while (qr.next()) {
                String dir = qr.getValue(GenQueryEnum.COL_COLL_NAME);
                String file = qr.getValue(GenQueryEnum.COL_DATA_NAME);
                String resultPath = dir + "/" + file;
                resultPath = resultPath.substring(spacepath.length() + 1);
                retList.add(resultPath);
                log.trace("Retrieving path: " + resultPath);
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

    /**
     * Create a new directory under the baseDirectory
     * 
     * @param spaceId
     */
    @Override
    public void createSpace(String spaceId) {
        ConnectOperation co = new ConnectOperation(host, port, username, password, zone);
        try {
            IrodsOperations io = new IrodsOperations(co);
            io.mkdir(baseDirectory + "/" + spaceId);
            log.trace("Created space/directory: " + baseDirectory + "/" + spaceId);
        } catch (IOException e) {
            log.error("Could not connect to iRODS", e);
            throw new StorageException(e);
        }
    }

    @Override
    public void deleteSpace(String spaceId) {
        ConnectOperation co = new ConnectOperation(host, port, username, password, zone);
        try {
            IrodsOperations io = new IrodsOperations(co);
            io.rmdir(baseDirectory + "/" + spaceId, true);
            log.trace("Removed space/directory: " + baseDirectory + "/" + spaceId);

        } catch (IOException e) {
            log.error("Could not connect to iRODS", e);
            throw new StorageException(e);
        }
    }

    @Override
    public Map<String, String> getSpaceMetadata(String spaceId) {
        ConnectOperation co = new ConnectOperation(host, port, username, password, zone);
        try {
            String path = baseDirectory + "/" + spaceId;
            Map<String,String> metadata = getMetadata(path,co);
            IrodsOperations ops = new IrodsOperations(co);
            RodsObjStat_PI stat = ops.stat(path);
            metadata.put(METADATA_SPACE_CREATED, formattedDate(stat.getModifyTime()));
            //metadata.put(METADATA_SPACE_COUNT, getSpaceCount(co,path);
            metadata.put(METADATA_SPACE_COUNT, "1+");
            return metadata;
        }catch (IOException e) {
            log.error("Could not connect to iRODS", e);
            throw new StorageException(e);
        }

        
    }

    @Override
    public void setSpaceMetadata(String spaceId, Map<String, String> spaceMetadata) {
        setMetadata(baseDirectory + "/" + spaceId, spaceMetadata);
    }

    /**
     * Always return 'open'
     * 
     * @param spaceId
     * @return
     */
    @Override
    public AccessType getSpaceAccess(String spaceId) {
        return AccessType.OPEN;
    }

    /**
     * Does nothing
     *
     * @param spaceId
     * @param access
     */
    @Override
    public void setSpaceAccess(String spaceId, AccessType access) {
        //NoOp
    }

    /**
     *  Add new content to irods, content path is baseDirectory /spaceId/contentId
     * 
     * @param spaceId
     * @param contentId
     * @param contentMimeType
     * @param contentSize may be set to 0 in some cases (entry through admin client)
     * @param contentChecksum
     * @param content
     * @return
     */
    @Override
    public String addContent(String spaceId, String contentId, String contentMimeType,
            long contentSize, String contentChecksum, InputStream content) {
        String path = baseDirectory + "/" + spaceId + "/" + contentId;

        log.trace("Writing to irods path: " + path + " resource: " + storageResource);

        ConnectOperation co = new ConnectOperation(host, port, username, password, zone);
        byte[] buffer = new byte[BLOCK_SIZE];

        try {
            OutputStream ios;

            if (contentSize > 0) {
                ios = new IrodsOutputStream(co.getConnection(), path,
                        storageResource, contentSize);
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
            log.trace("Finished writing irods path: " + path + " resource: "
                    + storageResource + " actual read: " + total + " contentSize: " + contentSize);

            return new IrodsOperations(co).stat(path).getChksum();
        } catch (IOException e) {
            log.error("Error ingesting file", e);
            throw new StorageException(e);
        }
    }

    @Override
    public InputStream getContent(String spaceId, String contentId) {
        String path = baseDirectory + "/" + spaceId + "/" + contentId;
        ConnectOperation co = new ConnectOperation(host, port, username, password, zone);
        try {
            ObjTypeEnum type = new IrodsOperations(co).stat(path).getObjType();
            log.trace("Opening inputstream to irods path: " + path + " type " + type);
            return new BufferedInputStream(
                    new IrodsProxyInputStream(path, co.getConnection()),
                    BLOCK_SIZE);

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
                log.trace("Removing irods file " + path);
                ops.rm(path);

            } else {
                log.info("Cannot remove file: " + path + ", type: " + type);
                throw new StorageException("Attempt to remove non-directory path");
            }
        } catch (IOException e) {
            log.error("Could not connect to iRODS", e);
            throw new StorageException(e);
        }
    }

    @Override
    public void setContentMetadata(String spaceId, String contentId, Map<String, String> contentMetadata) {
        String path;
        if ("spaces".equals(spaceId)) {
            path = baseDirectory + "/" + contentId;
        } else {
            path = baseDirectory + "/" + spaceId + "/" + contentId;
        }
        setMetadata(path, contentMetadata);
    }

    @Override
    public Map<String, String> getContentMetadata(String spaceId, String contentId) {
        String path;
        if ("spaces".equals(spaceId)) {
            path = baseDirectory + "/" + contentId;
        } else {
            path = baseDirectory + "/" + spaceId + "/" + contentId;
        }
        ConnectOperation co = new ConnectOperation(host, port, username, password, zone);
        try {
            Map<String,String> results = getMetadata(path,co);
            IrodsOperations ops = new IrodsOperations(co);
            RodsObjStat_PI stat = ops.stat(path);
            if (stat != null) {
                results.put(METADATA_CONTENT_MODIFIED, formattedDate(stat.getModifyTime()));
                results.put(METADATA_CONTENT_SIZE, Long.toString(stat.getObjSize()));
                results.put(METADATA_CONTENT_CHECKSUM, stat.getChksum());
                results.put(METADATA_CONTENT_MD5, stat.getChksum());
            }
            return results;
        }
        catch (IOException e) {        
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
            log.trace("Sending query: " + qb);
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

    private Iterator<String> listRecursiveFiles(String path, IRodsConnection connection) {

        QueryBuilder qb;
        QueryResult qr;

        try {

            qb = new QueryBuilder(GenQueryEnum.COL_COLL_NAME, GenQueryEnum.COL_DATA_NAME);

            qb.mCmp(GenQueryEnum.COL_COLL_NAME,
                    new QueryBuilder.Condition(QueryBuilder.ConditionType.LIKE, path + "/%"),
                    new QueryBuilder.Condition(QueryBuilder.ConditionType.EQ, path));
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

    private void setMetadata(String path, Map<String, String> metadata) {
        ConnectOperation co = new ConnectOperation(host, port, username, password, zone);

        log.trace("Writing metadata for " + path + " elements: " + metadata.size());
        metadata.remove(METADATA_CONTENT_MODIFIED);
        metadata.remove(METADATA_CONTENT_SIZE);
        metadata.remove(METADATA_CONTENT_MD5);
        metadata.remove(METADATA_CONTENT_CHECKSUM);

        try {
            MetaDataMap mDataMap = new MetaDataMap(path, co);
            mDataMap.clear();
            for (String e : metadata.keySet()) {
                mDataMap.put(e, metadata.get(e), null);
            }
        } catch (IOException e) {
            log.error("Could not connect to iRODS", e);
            throw new StorageException(e);
        }
    }

    private Map<String, String> getMetadata(String path,ConnectOperation co) {
        Map<String, String> results = new HashMap<String, String>();

        try {

            MetaDataMap mDataMap = new MetaDataMap(path, co);
            if (log.isTraceEnabled()) {
                log.trace("Retrieving metadata for " + path + mDataMap);
            }
            for (String e : mDataMap.keySet()) {
                results.put(e, mDataMap.get(e));
            }
            return results;
        } catch (IOException e) {
            log.error("Could not connect to iRODS", e);
            throw new StorageException(e);
        }
    }

    private String formattedDate(Date created) {
        RFC822_DATE_FORMAT.setTimeZone(TimeZone.getDefault());
        return RFC822_DATE_FORMAT.format(created);
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
}
