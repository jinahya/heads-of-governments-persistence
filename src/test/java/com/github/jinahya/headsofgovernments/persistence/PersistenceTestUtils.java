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
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

@Slf4j
public final class PersistenceTestUtils {

    /**
     * The name of the persistence unit declared in {@code META-INF/persistence.xml}.
     */
    public static final String PERSISTENCE_UNIT_NAME = "test";

    // ---------------------------------------------------------------------------------------------

    /**
     * Applies a newly created entity manager factory, for the {@link #PERSISTENCE_UNIT_NAME} unit,
     * to the specified function, and returns the result. The factory is closed after the function
     * returns.
     *
     * @param function the function to be applied with the entity manager factory.
     * @param <R>      result type parameter
     * @return the result of the {@code function}.
     */
    public static <R> R applyEntityManagerFactory(
            final Function<? super EntityManagerFactory, ? extends R> function) {
        Objects.requireNonNull(function, "function is null");
        try (var factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME)) {
            return function.apply(factory);
        }
    }

    /**
     * Applies an entity manager, created from a newly created entity manager factory, to the
     * specified function, and returns the result. Both the entity manager and its factory are
     * closed after the function returns.
     *
     * @param function the function to be applied with the entity manager.
     * @param <R>      result type parameter
     * @return the result of the {@code function}.
     * @see #applyEntityManagerFactory(Function)
     */
    public static <R> R applyEntityManager(
            final Function<? super EntityManager, ? extends R> function) {
        Objects.requireNonNull(function, "function is null");
        return applyEntityManagerFactory(factory -> {
            try (var manager = factory.createEntityManager()) {
                return function.apply(manager);
            }
        });
    }

    // ---------------------------------------------------------------------------------------------
    @Deprecated(forRemoval = true)
    private static String getEntityName(final EntityManager entityManager,
                                        final Class<?> entityClass) {
        Objects.requireNonNull(entityClass, "entityClass is null");
        return entityManager.getMetamodel().entity(entityClass).getName();
    }

    @Deprecated(forRemoval = true)
    static String getEntityName(final Class<?> entityClass) {
        return applyEntityManager(em -> getEntityName(em, entityClass));
    }

    // ---------------------------------------------------------------------------------------------
    private static long selectEntityCount(final EntityManager entityManager,
                                          final Class<?> entityClass) {
        Objects.requireNonNull(entityManager, "entityManager is null");
        Objects.requireNonNull(entityClass, "entityClass is null");
        final var builder = entityManager.getCriteriaBuilder();
        final var query = builder.createQuery(Long.class);
        final var root = query.from(entityClass); // FROM {entityName} AS e
        query.select(builder.count(root));        // SELECT COUNT(e)
        return entityManager.createQuery(query).getSingleResult();
    }

    static long selectEntityCount(final Class<?> entityClass) {
        Objects.requireNonNull(entityClass, "entityClass is null");
        return applyEntityManager(em -> selectEntityCount(em, entityClass));
    }

    // ---------------------------------------------------------------------------------------------
    static <T> Optional<T> selectRandomEntity(final Class<T> entityClass) {
        Objects.requireNonNull(entityClass, "entityClass is null");
        return applyEntityManager(em -> {
            final var count = selectEntityCount(em, entityClass);
            if (count == 0L) {
                return Optional.empty();
            }
            final var offset = (int) ThreadLocalRandom.current().nextLong(count);
            final var builder = em.getCriteriaBuilder();
            final var criteria = builder.createQuery(entityClass);
            final var root = criteria.from(entityClass); // FROM {entityName} AS e
            criteria.select(root);                       // SELECT e
            final var query = em.createQuery(criteria);
            return query
                    .setFirstResult(offset)             // LIMIT {offset},
                    .setMaxResults(1)                   // 1
                    .getResultStream()
                    .findFirst();
        });
    }

    // ---------------------------------------------------------------------------------------------
    private PersistenceTestUtils() {
        throw new AssertionError("instantiation is not allowed");
    }
}
