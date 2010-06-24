/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.unittestdb;

import org.duracloud.common.model.Credential;
import org.duracloud.common.util.TableSpec;
import org.duracloud.unittestdb.domain.ResourceType;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PasswordRepositoryDBImpl
        extends SimpleJdbcDaoSupport {

    private static final String tablename = "passwords";

    private final static String idCol = "id";

    // TODO: Col name of actual table should be renamed 'resourceType'
    private static final String resourceTypeCol = "providerType";

    private final static String usernameCol = "username";

    private final static String passwordCol = "password";

    private final String PASSWORD_INSERT =
            "INSERT INTO " + tablename + " (" + resourceTypeCol + ", "
                    + usernameCol + ", " + passwordCol + ") " + "VALUES (:"
                    + resourceTypeCol + ", :" + usernameCol + ", :"
                    + passwordCol + ")";

    private final String PASSWORD_SELECT =
            "SELECT " + passwordCol + " FROM " + tablename;

    private final String CREDENTIAL_SELECT_BY_RESOURCE_TYPE =
            "SELECT " + usernameCol + ", " + passwordCol + " FROM " + tablename
                    + " WHERE " + resourceTypeCol + " = ? ";

    private final String PASSWORD_SELECT_BY_RESOURCE_TYPE_AND_USERNAME =
            PASSWORD_SELECT + " WHERE " + resourceTypeCol + " = ? AND "
                    + usernameCol + " = ? ";

    private static final String ddl =
            "CREATE TABLE passwords (id INT GENERATED ALWAYS AS IDENTITY,"
                    + "providerType VARCHAR(32) NOT NULL,"
                    + "username VARCHAR(64) NOT NULL,"
                    + "password VARCHAR(64) NOT NULL)";

    public void insertPassword(ResourceType resource,
                               String username,
                               String password) {
        Map<String, String> params = new HashMap<String, String>();
        params.put(resourceTypeCol, resource.toString());
        params.put(usernameCol, username);
        params.put(passwordCol, password);

        this.getSimpleJdbcTemplate().update(PASSWORD_INSERT, params);
    }

    public Credential findCredentialByResourceType(ResourceType resourceType)
            throws Exception {
        List<Credential> credentials =
                this.getSimpleJdbcTemplate()
                        .query(CREDENTIAL_SELECT_BY_RESOURCE_TYPE,
                               new ParameterizedRowMapper<Credential>() {

                                   public Credential mapRow(ResultSet rs,
                                                            int rowNum)
                                           throws SQLException {
                                       String username =
                                               rs.getString(usernameCol);
                                       String password =
                                               rs.getString(passwordCol);
                                       return new Credential(username, password);
                                   }
                               },
                               resourceType.toString());
        if (credentials.size() == 0) {
            throw new Exception("Table is empty: '" + tablename + "'");
        }
        if (credentials.size() != 1) {
            throw new Exception(tablename
                    + " contains more than one entry for resourceType: "
                    + resourceType.toString());
        }

        return credentials.get(0);

    }

    public String findPasswordByResourceTypeAndUsername(ResourceType resourceType,
                                                        String username)
            throws Exception {
        List<String> passwords =
                this.getSimpleJdbcTemplate()
                        .query(PASSWORD_SELECT_BY_RESOURCE_TYPE_AND_USERNAME,
                               new ParameterizedRowMapper<String>() {

                                   public String mapRow(ResultSet rs, int rowNum)
                                           throws SQLException {
                                       return rs.getString(passwordCol);
                                   }
                               },
                               resourceType.toString(),
                               username);
        if (passwords.size() == 0) {
            throw new Exception("Table is empty: '" + tablename + "'");
        }
        if (passwords.size() != 1) {
            throw new Exception(tablename
                    + " contains more than one entry for resourceType and username : ["
                    + resourceType.toString() + "|" + username + "]");
        }

        return passwords.get(0);
    }

    public static TableSpec getTableSpec() {
        TableSpec ts = new TableSpec();
        ts.setTableName(tablename);
        ts.setPrimaryKey(idCol);
        ts.setDdl(ddl);
        return ts;
    }

}
