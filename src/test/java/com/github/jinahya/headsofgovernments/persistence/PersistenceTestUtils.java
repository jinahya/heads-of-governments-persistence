package com.github.jinahya.headsofgovernments.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.Objects;
import java.util.function.Function;

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
    private PersistenceTestUtils() {
        throw new AssertionError("instantiation is not allowed");
    }
}
