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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Helpers for the {@code *_Exercises_*_PersistenceTest} classes, which answer the exercises of {@code src/test/sql/exercises}.
 * <p>
 * A result is compared as rows of values, each row a list. A number is widened, an integral one to {@link Long} and
 * any other to {@link Double}, since what type a query gives back for a column depends on the query (JPQL types an
 * expression; a native query takes what the driver reports for the value).
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@SuppressWarnings({
        "java:S101" // Class names should comply with a naming convention
})
@Slf4j
final class __Exercises_TestUtils {

    /**
     * Applies the specified function to an entity manager of the {@link PersistenceTestUtils#PERSISTENCE_UNIT_NAME}
     * unit, and returns the result.
     *
     * @param function the function to apply.
     * @param <R>      the type of the result.
     * @return the result of the {@code function}.
     */
    static <R> R apply(final Function<? super EntityManager, ? extends R> function) {
        return PersistenceTestUtils.applyEntityManager(function);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a row of the specified values, each number widened.
     *
     * @param values the values of the row; {@code null} is allowed.
     * @return a row of the {@code values}.
     */
    static List<Object> row(final Object... values) {
        return Arrays.asList(Arrays.stream(values).map(__Exercises_TestUtils::widen).toArray());
    }

    /**
     * Returns, and logs, the rows of the specified result list; an element which is an {@code Object[]} is a row of
     * its elements, and any other element is a row of itself alone.
     *
     * @param results the result list of a query.
     * @return a list of rows of the {@code results}.
     */
    static List<List<Object>> rows(final List<?> results) {
        Objects.requireNonNull(results, "results is null");
        final var rows = results.stream()
                .map(r -> r instanceof Object[] a ? row(a) : row(r))
                .toList();
        rows.forEach(r -> log.debug("{}", r));
        return rows;
    }

    private static Object widen(final Object value) {
        return switch (value) {
            case Byte b -> b.longValue();
            case Short s -> s.longValue();
            case Integer i -> i.longValue();
            case BigInteger i -> i.longValueExact();
            // a native query reads SQLite's REAL as a Float, the type the driver reports for it; widened through its
            // shortest decimal form, 14.6f gives 14.6, not 14.600000381469727
            case Float f -> Double.valueOf(f.toString());
            case BigDecimal d -> d.doubleValue();
            case null, default -> value;
        };
    }

    // ---------------------------------------------------------------------------------------------
    private __Exercises_TestUtils() {
        throw new AssertionError("instantiation is not allowed");
    }
}
