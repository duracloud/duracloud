/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * @author Erik Paulsson
 *         Date: 7/11/13
 */
@Entity
public class UserInvitation extends BaseEntity {

    @ManyToOne(fetch= FetchType.EAGER, optional=true)
    @JoinColumn(name="account_id", nullable=true, columnDefinition = "bigint(20)")
    private AccountInfo account;

    private String accountName;
    private String accountOrg;
    private String accountDep;
    private String accountSubdomain;
    private String adminUsername;
    private String userEmail;
    private Date creationDate;
    private Date expirationDate;
    private String redemptionCode;

    public UserInvitation() {}

    public UserInvitation(
            Long id, AccountInfo account, String accountName, String accountOrg, String accountDep,
            String accountSubdomain, String adminUsername, String userEmail,
            int expirationDays, String redemptionCode) {
        this.id = id;
        this.account = account;
        this.accountName = accountName;
        this.accountOrg = accountOrg;
        this.accountDep = accountDep;
        this.accountSubdomain = accountSubdomain;
        this.adminUsername = adminUsername;
        this.userEmail = userEmail;

        this.creationDate = new Date();

        // milliseconds until expiration (days * millis in a day)
        long expMillis = expirationDays * 86400000;
        this.expirationDate = new Date(creationDate.getTime() + expMillis);

        this.redemptionCode = redemptionCode;
    }

    public AccountInfo getAccount() {
        return account;
    }

    public void setAccount(AccountInfo account) {
        this.account = account;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountOrg() {
        return accountOrg;
    }

    public void setAccountOrg(String accountOrg) {
        this.accountOrg = accountOrg;
    }

    public String getAccountDep() {
        return accountDep;
    }

    public void setAccountDep(String accountDep) {
        this.accountDep = accountDep;
    }

    public String getAccountSubdomain() {
        return accountSubdomain;
    }

    public void setAccountSubdomain(String accountSubdomain) {
        this.accountSubdomain = accountSubdomain;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getRedemptionCode() {
        return redemptionCode;
    }

    public void setRedemptionCode(String redemptionCode) {
        this.redemptionCode = redemptionCode;
    }





    @Override
    public String toString() {
        return "UserInvitation[id="
                + id + ", accountId=" + account.getId() + ", accountName=" + accountName
                + ", accountOrg=" + accountOrg + ", accountDep=" + accountDep
                + ", accountSubdomain=" + accountSubdomain + ", adminUsername=" + adminUsername
                + ", userEmail=" + userEmail
                + ", creationDate=" + creationDate + ", expirationDate="
                + expirationDate + ", redemptionCode=" + redemptionCode + "]";

    }
}
