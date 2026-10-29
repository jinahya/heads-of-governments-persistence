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
 * Answers the exercises of {@code src/test/sql/exercises/office_exercises_01_basic.sql}, against
 * {@code db/heads_of_governments.sqlite3}; a nested class per exercise, which answers it with JPQL
 * and with the Criteria API, or with a native query where neither can, and checks that it gives
 * what the SQL of the file gives.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@Slf4j
class Office_Exercises_01_Basic_PersistenceTest {

    @DisplayName("HOG_OFFICE_B_01: An office by country")
    @Nested
    class HOG_OFFICE_B_01_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("KR", "PRESIDENT", "대통령",
                    "대, one per election; a person re-elected keeps one line per term with " +
                    "consecutive numbers")
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT o
                                    FROM Office AS o
                                    WHERE o.country = UPPER(:country)""",
                            Office.class
                    )
                    .setParameter("country", "kr")
                    .getResultList());
            assertThat(rows(list.stream()
                                    .map(o -> new Object[]{o.getCountry(), o.getHeadKind(),
                                            o.getHeadTitle(), o.getNumbering()})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }

        @Test
        void __CriteriaApi() {
            final var list = __Exercises_TestUtils.apply(em -> {
                final var cb = em.getCriteriaBuilder();
                final var cq = cb.createQuery(Office.class);
                final var o = cq.from(Office.class);
                cq.select(o)
                        .where(cb.equal(o.get(Office_.country),
                                        cb.upper(cb.parameter(String.class, "country"))));
                return em.createQuery(cq)
                        .setParameter("country", "kr")
                        .getResultList();
            });
            assertThat(rows(list.stream()
                                    .map(o -> new Object[]{o.getCountry(), o.getHeadKind(),
                                            o.getHeadTitle(), o.getNumbering()})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_OFFICE_B_02: The offices")
    @Nested
    class HOG_OFFICE_B_02_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", "PRESIDENT", "Président de la République"),
                row("GB", "PRIME_MINISTER", "Prime Minister"),
                row("JP", "PRIME_MINISTER", "内閣総理大臣"),
                row("KR", "PRESIDENT", "대통령"),
                row("US", "PRESIDENT", "President")
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT o
                                    FROM Office AS o
                                    ORDER BY o.country ASC""",
                            Office.class
                    )
                    .getResultList());
            assertThat(rows(list.stream()
                                    .map(o -> new Object[]{o.getCountry(), o.getHeadKind(),
                                            o.getHeadTitle()})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }

        @Test
        void __CriteriaApi() {
            final var list = __Exercises_TestUtils.apply(em -> {
                final var cb = em.getCriteriaBuilder();
                final var cq = cb.createQuery(Office.class);
                final var o = cq.from(Office.class);
                cq.select(o)
                        .orderBy(cb.asc(o.get(Office_.country)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list.stream()
                                    .map(o -> new Object[]{o.getCountry(), o.getHeadKind(),
                                            o.getHeadTitle()})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_OFFICE_B_03: Offices where a number can repeat")
    @Nested
    class HOG_OFFICE_B_03_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", "Président de la République",
                    "ordinal of the person by first election; a person re-elected has one line " +
                    "per term and keeps the same number on each"),
                row("GB", "Prime Minister",
                    "ordinal of the person by first appointment; a person who served " +
                    "non-consecutive terms has one line per term and keeps the same number on each")
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT o
                                    FROM Office AS o
                                    WHERE o.numbering LIKE '%keeps the same number%'
                                    ORDER BY o.country ASC""",
                            Office.class
                    )
                    .getResultList());
            assertThat(rows(list.stream()
                                    .map(o -> new Object[]{o.getCountry(), o.getHeadTitle(),
                                            o.getNumbering()})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }

        @Test
        void __CriteriaApi() {
            final var list = __Exercises_TestUtils.apply(em -> {
                final var cb = em.getCriteriaBuilder();
                final var cq = cb.createQuery(Office.class);
                final var o = cq.from(Office.class);
                cq.select(o)
                        .where(cb.like(o.get(Office_.numbering), "%keeps the same number%"))
                        .orderBy(cb.asc(o.get(Office_.country)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list.stream()
                                    .map(o -> new Object[]{o.getCountry(), o.getHeadTitle(),
                                            o.getNumbering()})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }
    }
}
