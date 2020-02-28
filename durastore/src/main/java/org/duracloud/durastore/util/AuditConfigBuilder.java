/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.util;

import org.duracloud.account.db.model.DuracloudMill;
import org.duracloud.account.db.repo.DuracloudMillRepo;
import org.duracloud.storage.domain.AuditConfig;

/**
 * @author Daniel Bernstein
 */
public class AuditConfigBuilder {
    private DuracloudMillRepo millRepo;

    public AuditConfigBuilder(DuracloudMillRepo millRepo) {
        this.millRepo = millRepo;
    }

    public AuditConfig build() {
        AuditConfig config = new AuditConfig();
        DuracloudMill mill = millRepo.findAll().get(0);
        config.setAuditLogSpaceId(mill.getAuditLogSpaceId());
        config.setAuditQueueName(mill.getAuditQueue());
        config.setAuditQueueType(mill.getAuditQueueType());
        config.setRabbitmqHost(mill.getRabbitmqHost());
        config.setRabbitmqPort(mill.getRabbitmqPort());
        config.setRabbitmqVhost(mill.getRabbitmqVhost());
        config.setRabbitmqExchange(mill.getRabbitmqExchange());
        config.setRabbitmqUsername(mill.getRabbitmqUsername());
        config.setRabbitmqPassword(mill.getRabbitmqPassword());
        return config;
    }

}
