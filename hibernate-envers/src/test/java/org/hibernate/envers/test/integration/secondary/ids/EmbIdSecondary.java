/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2008, Red Hat Middleware LLC or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.envers.test.integration.secondary.ids;

import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.envers.test.AbstractEntityTest;
import org.hibernate.envers.test.Priority;
import org.hibernate.envers.test.entities.ids.EmbId;
import org.hibernate.mapping.Join;
import org.junit.Test;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class EmbIdSecondary extends AbstractEntityTest {
    private EmbId id;

    public void configure(Ejb3Configuration cfg) {
        cfg.addAnnotatedClass(SecondaryEmbIdTestEntity.class);
    }

    @Test
    @Priority(10)
    public void initData() {
        id = new EmbId(1, 2);

        SecondaryEmbIdTestEntity ste = new SecondaryEmbIdTestEntity(id, "a", "1");

        // Revision 1
        EntityManager em = getEntityManager();
        em.getTransaction().begin();

        em.persist(ste);

        em.getTransaction().commit();

        // Revision 2
        em.getTransaction().begin();

        ste = em.find(SecondaryEmbIdTestEntity.class, ste.getId());
        ste.setS1("b");
        ste.setS2("2");

        em.getTransaction().commit();
    }

    @Test
    public void testRevisionsCounts() {
        assert Arrays.asList(1, 2).equals(getAuditReader().getRevisions(SecondaryEmbIdTestEntity.class, id));
    }

    @Test
    public void testHistoryOfId() {
        SecondaryEmbIdTestEntity ver1 = new SecondaryEmbIdTestEntity(id, "a", "1");
        SecondaryEmbIdTestEntity ver2 = new SecondaryEmbIdTestEntity(id, "b", "2");

        assert getAuditReader().find(SecondaryEmbIdTestEntity.class, id, 1).equals(ver1);
        assert getAuditReader().find(SecondaryEmbIdTestEntity.class, id, 2).equals(ver2);
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void testTableNames() {
        assert "sec_embid_versions".equals(((Iterator<Join>)
                getCfg().getClassMapping("org.hibernate.envers.test.integration.secondary.ids.SecondaryEmbIdTestEntity_AUD")
                        .getJoinIterator())
                .next().getTable().getName());
    }
}