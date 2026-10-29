package com.github.jinahya.headsofgovernments.persistence.exercises;

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

import com.github.jinahya.headsofgovernments.persistence.Office;
import com.github.jinahya.headsofgovernments.persistence.Office_;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.jinahya.headsofgovernments.persistence.exercises.__Exercises_TestUtils.row;
import static com.github.jinahya.headsofgovernments.persistence.exercises.__Exercises_TestUtils.rows;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Answers the exercises of {@code src/test/sql/exercises/office_exercises_03_advanced.sql}, against
 * {@code heads_of_governments.sqlite3}; a nested class per exercise, which answers it with JPQL
 * and with the Criteria API, or with a native query where neither can, and checks that it gives
 * what the SQL of the file gives.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@Slf4j
class Office_Exercises_03_Advanced_PersistenceTest {

    @DisplayName("HOG_OFFICE_A_01: What number counts, in a phrase")
    @Nested
    class HOG_OFFICE_A_01_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", "ordinal of the person by first election"),
                row("GB", "ordinal of the person by first appointment"),
                row("JP", "代"),
                row("KR", "대"),
                row("US", "presidency number")
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT o,
                                           CASE
                                               WHEN LOCATE(',', o.numbering) > 0
                                                    AND (LOCATE(';', o.numbering) = 0 OR LOCATE(',', o.numbering) < LOCATE(';', o.numbering))
                                                   THEN SUBSTRING(o.numbering, 1, LOCATE(',', o.numbering) - 1)
                                               WHEN LOCATE(';', o.numbering) > 0
                                                   THEN SUBSTRING(o.numbering, 1, LOCATE(';', o.numbering) - 1)
                                               ELSE o.numbering
                                               END
                                    FROM Office AS o
                                    ORDER BY o.country ASC""",
                            Object[].class
                    )
                    .getResultList());
            assertThat(rows(list.stream()
                                    .map(r -> new Object[]{((Office) r[0]).getCountry(), r[1]})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }

        @Test
        void __CriteriaApi() {
            final var list = __Exercises_TestUtils.apply(em -> {
                final var cb = em.getCriteriaBuilder();
                final var cq = cb.createQuery(Object[].class);
                final var o = cq.from(Office.class);
                final var numbering = o.get(Office_.numbering);
                final var comma = cb.locate(numbering, ",");
                final var semicolon = cb.locate(numbering, ";");
                cq.select(cb.array(
                                o,
                                cb.<String>selectCase()
                                        .when(cb.and(cb.gt(comma, 0), cb.or(cb.equal(semicolon, 0),
                                                                            cb.lessThan(comma,
                                                                                        semicolon))),
                                              cb.substring(numbering, cb.literal(1),
                                                           cb.diff(comma, 1)))
                                        .when(cb.gt(semicolon, 0),
                                              cb.substring(numbering, cb.literal(1),
                                                           cb.diff(semicolon, 1)))
                                        .otherwise(numbering)))
                        .orderBy(cb.asc(o.get(Office_.country)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list.stream()
                                    .map(r -> new Object[]{((Office) r[0]).getCountry(), r[1]})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }
    }
}
