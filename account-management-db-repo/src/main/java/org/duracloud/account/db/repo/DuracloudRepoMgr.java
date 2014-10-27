/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Erik Paulsson
 *         Date: 7/17/13
 */
@Component(value="repoMgr")
public class DuracloudRepoMgr {

    @Autowired
    private DuracloudUserRepo userRepo;

    @Autowired
    private DuracloudGroupRepo groupRepo;

    @Autowired
    private DuracloudAccountRepo accountRepo;

    @Autowired
    private DuracloudRightsRepo rightsRepo;

    @Autowired
    private DuracloudUserInvitationRepo userInvitationRepo;

    @Autowired
    private DuracloudInstanceRepo instanceRepo;

    @Autowired
    private DuracloudServerImageRepo serverImageRepo;

    @Autowired
    private DuracloudComputeProviderAccountRepo computeProviderAccountRepo;

    @Autowired
    private DuracloudStorageProviderAccountRepo storageProviderAccountRepo;

    @Autowired
    private DuracloudServerDetailsRepo serverDetailsRepo;

    @Autowired
    private DuracloudAccountClusterRepo accountClusterRepo;

    public DuracloudUserRepo getUserRepo() {
        return userRepo;
    }

    public DuracloudGroupRepo getGroupRepo() {
        return groupRepo;
    }

    public DuracloudAccountRepo getAccountRepo() {
        return accountRepo;
    }

    public DuracloudRightsRepo getRightsRepo() {
        return rightsRepo;
    }

    public DuracloudUserInvitationRepo getUserInvitationRepo() {
        return userInvitationRepo;
    }

    public DuracloudInstanceRepo getInstanceRepo() {
        return instanceRepo;
    }

    public DuracloudServerImageRepo getServerImageRepo() {
        return serverImageRepo;
    }

    public DuracloudComputeProviderAccountRepo getComputeProviderAccountRepo() {
        return computeProviderAccountRepo;
    }

    public DuracloudStorageProviderAccountRepo getStorageProviderAccountRepo() {
        return storageProviderAccountRepo;
    }

    public DuracloudServerDetailsRepo getServerDetailsRepo() {
        return serverDetailsRepo;
    }

    public DuracloudAccountClusterRepo getAccountClusterRepo() {
        return accountClusterRepo;
    }

    public Set<JpaRepository> getAllRepos() {
        Set<JpaRepository> repos = new HashSet<>();
        repos.add(userRepo);
        repos.add(groupRepo);
        repos.add(accountRepo);
        repos.add(rightsRepo);
        repos.add(userInvitationRepo);
        repos.add(instanceRepo);
        repos.add(serverDetailsRepo);
        repos.add(computeProviderAccountRepo);
        repos.add(storageProviderAccountRepo);
        repos.add(serverDetailsRepo);
        repos.add(accountClusterRepo);
        return repos;
    }
}
