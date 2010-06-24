/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.unittestdb;

import org.apache.commons.lang.StringUtils;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.model.Credential;
import org.duracloud.common.util.DatabaseUtil;
import org.duracloud.common.util.TableSpec;
import org.duracloud.unittestdb.domain.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class UnitTestDatabaseUtil
        extends DatabaseUtil {

    protected static final Logger log =
            LoggerFactory.getLogger(UnitTestDatabaseUtil.class);

    private final List<TableSpec> tableSpecs =
            Arrays.asList(PasswordRepositoryDBImpl.getTableSpec());

    PasswordRepositoryDBImpl repo;

    public UnitTestDatabaseUtil()
            throws Exception {
        this(getDatabaseCredential(), getDatabaseHome(), getBootPassword());
    }

    public UnitTestDatabaseUtil(Credential cred,
                                String baseDir,
                                String bootPassword)
            throws Exception {
        super(cred, baseDir, bootPassword);
        if (!isValid(baseDir, bootPassword)) {
            log.error("unit.database.home='" + baseDir + "'");
            log.error("unit.database.password='" + bootPassword + "'");
            throw new Exception(usage());
        }
        repo = createRepo();
    }

    private PasswordRepositoryDBImpl createRepo() {
        log.debug("creating Password DB");
        PasswordRepositoryDBImpl repo = new PasswordRepositoryDBImpl();
        repo.setDataSource(getDataSource());
        return repo;
    }

    @Override
    protected List<TableSpec> getTableSpecs() {
        return tableSpecs;
    }

    public void createNewDB() throws Exception {
        initializeDB();
    }

    public void connectToExistingDB() throws Exception {
        ensureDatabaseExists();
        ensureTablesExist();
    }

    public void insertCredentialForResource(ResourceType type,
                                            Credential cred) {
        repo.insertPassword(type, cred.getUsername(), cred.getPassword());
    }

    public Credential findCredentialForResource(ResourceType resource)
            throws Exception {
        return repo.findCredentialByResourceType(resource);
    }

    public static boolean isValid(String... texts) {
        boolean valid = true;
        for (String text : texts) {
            if (StringUtils.isBlank(text)) {
                valid = false;
            }
        }
        return valid;
    }

    private static Credential getDatabaseCredential() {
        return new Credential("duracloud", "duracloud");
    }

    private static String getBootPassword() {
        String password = System.getProperty("unit.database.password");
        if (null == password) {
            throw new DuraCloudRuntimeException(usage());
        }
        return password;
    }

    private static String getDatabaseHome() {
        String home = System.getProperty("unit.database.home");
        if (null == home) {
            throw new DuraCloudRuntimeException(usage());
        }
        return home;
    }

    public static String usage() {
        StringBuilder sb =
                new StringBuilder("\n----------------------------\n");
        sb.append("Usage:");
        sb.append("\n\tUnitTestDatabaseUtil ");
        sb.append("-Dunit.database.password=<boot-password> ");
        sb.append("-Dunit.database.home=<location-of-database>");
        sb.append("\n\n\tWhere <boot-password> is the password to ");
        sb.append("boot the encrypted database.");
        sb.append("\n\n\tAnd <location-of-database> is the full path to ");
        sb.append("\n\tthe desired location of the populated database.");
        sb.append("\n\n\tNOTE: When running storage-provider unit tests ");
        sb.append("in maven,");
        sb.append("\n\tthe maven settings.xml should hold the ");
        sb.append("unit.database.password and unit.database.home.");
        sb.append("\n\tSee notes in resources/install-notes.txt");
        sb.append("\n----------------------------\n");
        return sb.toString();
    }

}
