/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.bitintegritytools;

import org.duracloud.services.ComputeService;
import org.duracloud.services.fixity.FixityService;
import org.osgi.service.cm.ManagedService;

/**
 * This class exposes FixityServices. It would not be needed if the
 * DuraConfigAdminImpl were able to not choke on bundle-ownership of
 * configurations when deploying fixityservicetools before the legacy
 * fixityservice.
 *
 * @author Andrew Woods
 *         Date: Dec 7, 2010
 */
public class BitIntegrityTools extends FixityService implements ComputeService, ManagedService {
}
