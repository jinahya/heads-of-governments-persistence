package com.github.jinahya.headsofgovernments.persistence;

/*-
 * #%L
 * heads-of-governments-persistence
 * %%
 * Copyright (C) 2026 Jinahya, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class HibernateOrmUtils {

    static void logEntities(final EntityManager entityManager) {
        final var session = entityManager.unwrap(org.hibernate.Session.class);
        final var implementor = (org.hibernate.engine.spi.SessionImplementor) session;
        final var context = implementor.getPersistenceContextInternal();
        final var entities = context.getEntitiesByKey().values();
        for (final var entity : entities) {
            log.debug("entity: {}", entity);
        }
    }

    // ---------------------------------------------------------------------------------------------
    private HibernateOrmUtils() {
        throw new AssertionError("instantiation is not allowed");
    }
}
