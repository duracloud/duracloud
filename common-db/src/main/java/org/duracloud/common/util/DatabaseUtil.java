/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import java.io.File;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.derby.jdbc.EmbeddedDataSource;

import org.duracloud.common.model.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * <pre>
 * This abstract class provides the ability to create/clear/remove a database
 * and its tables.
 * It also provides a 'javax.sql.DataSource' for the database.
 * Finally, sql operations can be executed on the database through the retrieval
 * of the 'JdbcOperations' object provided by the method: getOps().
 * </pre>
 *
 * @author Andrew Woods
 */
public abstract class DatabaseUtil {

    protected final Logger log = LoggerFactory.getLogger(DatabaseUtil.class);

    private final EmbeddedDataSource dataSource;

    private final JdbcTemplate jdbcTemplate;

    private final String baseDir;

    public static String NOT_ENCRYPTED = "NOT_ENCRYPTED";

    public DatabaseUtil(Credential cred, String baseDir) {
        this(cred, baseDir, NOT_ENCRYPTED);
    }

    public DatabaseUtil(Credential cred, String baseDir, String bootPassword) {
        this.baseDir = baseDir;

        dataSource = new EmbeddedDataSource();
        dataSource.setUser(cred.getUsername());
        dataSource.setPassword(cred.getPassword());
        dataSource.setDatabaseName(baseDir);
        if (!bootPassword.equals(NOT_ENCRYPTED)) {
            String connAtts =
                    "dataEncryption=true;bootPassword=" + bootPassword;
            dataSource.setConnectionAttributes(connAtts);
        }
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    protected abstract List<TableSpec> getTableSpecs();

    /**
     * This method sets-up empty tables for this database.
     *
     * @throws Exception
     */
    public void initializeDB() throws Exception {
        log.debug("initializing db");
        ensureDatabaseExists();
        ensureTablesExist();
        ensureTablesCleared();
    }

    public void ensureDatabaseExists() {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
        } catch (SQLException e) {
            log.info("creating database... " + baseDir);
            dataSource.setCreateDatabase("create");
        } finally {
            close(conn);
        }
    }

    public void ensureTablesExist() throws SQLException {

        for (TableSpec ts : getTableSpecs()) {
            try {
                execute("SELECT " + ts.getPrimaryKey() + " FROM "
                        + ts.getTableName());
            } catch (Exception e) {
                log.info("creating table... " + ts.getTableName());
                execute(ts.getDdl());
            }
        }

        // TODO: eventually use DatabaseMetaData to create proper table schema.
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            DatabaseMetaData metadata = conn.getMetaData();
            ResultSet results = metadata.getTables(null, null, "*", null);
            while (results.next()) {
                log.info("examining tables:" + results.getObject(0).toString());
            }
        } catch (SQLException e) {
            log.error("Database should already exist.");
            throw e;
        } finally {
            close(conn);
        }
    }

    public void clearDB() {
        clearTables();
    }

    private void ensureTablesCleared() {
        clearTables();
    }

    private void clearTables() {
        // Tables cleared in reverse order to maintain data integrity.
        LinkedList<TableSpec> reversedSpecs = new LinkedList<TableSpec>();
        for (TableSpec spec : getTableSpecs()) {
            reversedSpecs.addFirst(spec);
        }

        for (TableSpec spec : reversedSpecs) {
            execute("DELETE FROM " + spec.getTableName() + " WHERE "
                    + spec.getPrimaryKey() + " IS NOT NULL");
        }
    }

    private void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
            }
        }
    }

    public void disconnect() {
        dataSource.setShutdownDatabase("shutdown");
    }

    public JdbcTemplate getSimpleJdbcTemplate() {
        return jdbcTemplate;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public JdbcOperations getOps() {
        return getSimpleJdbcTemplate();
    }

    protected void execute(String sql) {
        getOps().execute(sql);
    }

    @SuppressWarnings("unused")
    private void dropTables() {
        for (TableSpec ts : getTableSpecs()) {
            execute("DROP TABLE " + ts.getTableName());
        }
    }

    @SuppressWarnings("unused")
    private void deleteDB(String baseDir) throws IOException {
        File base = new File(baseDir);
        deleteFiles(base);
    }

    private void deleteFiles(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                deleteFiles(f);
            }
        }
        file.delete();
    }
}
