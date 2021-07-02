/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.util;

import org.duracloud.account.db.model.DuracloudMill;
import org.duracloud.account.db.model.RabbitmqConfig;
import org.duracloud.account.db.repo.DuracloudMillRepo;
import org.duracloud.common.queue.QueueType;
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
        RabbitmqConfig rmqConf = mill.getRabbitmqConfig();
        config.setAuditLogSpaceId(mill.getAuditLogSpaceId());
        config.setAuditQueueName(mill.getAuditQueue());
        config.setQueueType(QueueType.fromString(mill.getQueueType()));
        config.setRabbitmqHost(rmqConf.getHost());
        config.setRabbitmqPort(rmqConf.getPort());
        config.setRabbitmqVhost(rmqConf.getVhost());
        config.setRabbitmqExchange(mill.getRabbitmqExchange());
        config.setRabbitmqUsername(rmqConf.getUsername());
        config.setRabbitmqPassword(rmqConf.getPassword());
        return config;
    }

}
