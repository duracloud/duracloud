/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.model.util;

import org.duracloud.account.db.model.Identifiable;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentityGenerator;

import java.io.Serializable;

/**
 * This class is needed to be able to use specified entity IDs instead of the
 * default auto-incremented IDs.  This was necessary so that exported data from
 * the old management console using SimpleDB could be imported into a relational
 * database while keeping the same IDs for the entities from the exported data.
 * @author Erik Paulsson
 *         Date: 5/20/14
 */
public class UseIdOrGenerate extends IdentityGenerator {

    @Override
    public Serializable generate(SessionImplementor session, Object obj) throws HibernateException {
        if (obj == null) throw new HibernateException(new NullPointerException()) ;

        if ((((Identifiable) obj).getId()) == null) {
            Serializable id = super.generate(session, obj) ;
            return id;
        } else {
            return ((Identifiable) obj).getId();
        }
    }
}
