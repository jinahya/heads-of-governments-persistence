package com.github.jinahya.headsofgovernments.persistence;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionImplementor;

@Slf4j
final class HibernateOrmUtils {

    static void logEntities(final EntityManager entityManager) {
        final var session = entityManager.unwrap(Session.class);
        final var implementor = (SessionImplementor) session;
        final var context = implementor.getPersistenceContextInternal();
        final var entities = context.getEntitiesByKey().values();
        for (final var entity : entities) {
            log.debug("entity: {}", entity);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    private HibernateOrmUtils() {
        throw new AssertionError("instantiation is not allowed");
    }
}
