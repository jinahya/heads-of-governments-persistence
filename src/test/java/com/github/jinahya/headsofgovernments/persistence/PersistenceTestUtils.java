package com.github.jinahya.headsofgovernments.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

@Slf4j
final class PersistenceTestUtils {

    /**
     * The name of the persistence unit declared in {@code META-INF/persistence.xml}.
     */
    static final String PERSISTENCE_UNIT_NAME = "heads-of-governments";

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Applies a newly created entity manager factory, for the {@link #PERSISTENCE_UNIT_NAME} unit, to the specified
     * function, and returns the result. The factory is closed after the function returns.
     *
     * @param function the function to be applied with the entity manager factory.
     * @param <R>      result type parameter
     * @return the result of the {@code function}.
     */
    static <R> R applyEntityManagerFactory(final Function<? super EntityManagerFactory, ? extends R> function) {
        Objects.requireNonNull(function, "function is null");
        try (var factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME)) {
            return function.apply(factory);
        }
    }

    /**
     * Applies an entity manager, created from a newly created entity manager factory, to the specified function, and
     * returns the result. Both the entity manager and its factory are closed after the function returns.
     *
     * @param function the function to be applied with the entity manager.
     * @param <R>      result type parameter
     * @return the result of the {@code function}.
     * @see #applyEntityManagerFactory(Function)
     */
    static <R> R applyEntityManager(final Function<? super EntityManager, ? extends R> function) {
        Objects.requireNonNull(function, "function is null");
        return applyEntityManagerFactory(factory -> {
            try (var manager = factory.createEntityManager()) {
                return function.apply(manager);
            }
        });
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Counts all entities of the specified class, using an entity manager created from a newly created entity manager
     * factory. Whether the count is issued as a query-language string or built with the Criteria API is decided at
     * random, so that callers exercise both over time.
     *
     * @param entityClass the class of the entities to count.
     * @return the number of entities of {@code entityClass}.
     * @see #applyEntityManager(Function)
     */
    static long selectCount(final Class<?> entityClass) {
        Objects.requireNonNull(entityClass, "entityClass is null");
        return applyEntityManager(em -> {
            if (ThreadLocalRandom.current().nextBoolean()) {
                final var entityName = em.getMetamodel().entity(entityClass).getName();
                return em.createQuery("SELECT COUNT(e) FROM " + entityName + " AS e", Long.class)
                        .getSingleResult();
            }
            final var builder = em.getCriteriaBuilder();
            final var query = builder.createQuery(Long.class);
            final var root = query.from(entityClass);
            query.select(builder.count(root));
            return em.createQuery(query).getSingleResult();
        });
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Selects the entity at the specified offset without {@link TypedQuery#setFirstResult(int)}, by fetching
     * {@code offset + 1} rows and taking the last. EclipseLink's generic platform, which it runs on for SQLite,
     * implements a first result by scrolling the JDBC cursor, and the SQLite driver supports forward-only cursors
     * only, so {@link #selectRandom(Class)} falls back to this when the standard way fails.
     */
    private static <T> Optional<T> selectRandomEclipseLink(final Class<T> entityClass, final int offset) {
        return applyEntityManager(em -> {
            final List<T> list;
            if (ThreadLocalRandom.current().nextBoolean()) {
                final var entityName = em.getMetamodel().entity(entityClass).getName();
                list = em.createQuery("SELECT e FROM " + entityName + " AS e", entityClass)
                        .setMaxResults(offset + 1)
                        .getResultList();
            } else {
                final var builder = em.getCriteriaBuilder();
                final var query = builder.createQuery(entityClass);
                final var root = query.from(entityClass);
                query.select(root);
                list = em.createQuery(query)
                        .setMaxResults(offset + 1)
                        .getResultList();
            }
            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(list.size() - 1));
        });
    }

    /**
     * Selects a random entity of the specified class, using an entity manager created from a newly created entity
     * manager factory. The entity is picked by a random offset below the {@link #selectCount(Class) count}, and whether
     * the selection is issued as a query-language string or built with the Criteria API is decided at random, so that
     * callers exercise both over time.
     *
     * @param entityClass the class of the entity to select.
     * @param <T>         entity type parameter
     * @return an optional of a random entity of {@code entityClass}; empty when there is none.
     * @see #selectCount(Class)
     * @see #applyEntityManager(Function)
     */
    static <T> Optional<T> selectRandom(final Class<T> entityClass) {
        Objects.requireNonNull(entityClass, "entityClass is null");
        final var count = selectCount(entityClass);
        if (count == 0L) {
            return Optional.empty();
        }
        final var offset = (int) ThreadLocalRandom.current().nextLong(count);
        try {
            return applyEntityManager(em -> {
                final TypedQuery<T> query;
                if (ThreadLocalRandom.current().nextBoolean()) {
                    final var entityName = em.getMetamodel().entity(entityClass).getName();
                    query = em.createQuery("SELECT e FROM " + entityName + " AS e", entityClass);
                } else {
                    final var builder = em.getCriteriaBuilder();
                    final var criteria = builder.createQuery(entityClass);
                    final var root = criteria.from(entityClass);
                    criteria.select(root);
                    query = em.createQuery(criteria);
                }
                return query.setFirstResult(offset)
                        .setMaxResults(1)
                        .getResultStream()
                        .findFirst();
            });
        } catch (final PersistenceException pe) {
            log.debug("failed to select with a first result; falling back", pe);
            return selectRandomEclipseLink(entityClass, offset);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    private PersistenceTestUtils() {
        throw new AssertionError("instantiation is not allowed");
    }
}
