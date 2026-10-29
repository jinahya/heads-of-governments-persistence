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

import com.github.jinahya.headsofgovernments.persistence.Head;
import com.github.jinahya.headsofgovernments.persistence.Head_;
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
 * Answers the exercises of {@code src/test/sql/exercises/office_exercises_02_intermediate.sql},
 * against {@code db/heads_of_governments.sqlite3}; a nested class per exercise, which answers it
 * with JPQL and with the Criteria API, or with a native query where neither can, and checks that it
 * gives what the SQL of the file gives.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@Slf4j
class Office_Exercises_02_Intermediate_PersistenceTest {

    @DisplayName("HOG_OFFICE_I_01: Offices by the kind of head")
    @Nested
    class HOG_OFFICE_I_01_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("PRESIDENT", 3L, "FR, KR, US"),
                row("PRIME_MINISTER", 2L, "GB, JP")
        );

        // JPQL has no string aggregation; GROUP_CONCAT() is SQLite's own, and the Criteria API
        // has none either
        @Test
        void nativeQuery() {
            final var list = __Exercises_TestUtils.apply(em -> (List<?>) em.createNativeQuery(
                            """
                                    SELECT head_kind, COUNT(*) AS offices, GROUP_CONCAT(country, ', ' ORDER BY country) AS countries
                                    FROM office
                                    GROUP BY head_kind
                                    ORDER BY head_kind ASC"""
                    )
                    .getResultList());
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_OFFICE_I_02: Titles outside ASCII")
    @Nested
    class HOG_OFFICE_I_02_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("GB", "PRIME_MINISTER", "Prime Minister",
                    "ordinal of the person by first appointment; a person who served " +
                    "non-consecutive terms has one line per term and keeps the same number on each",
                    "ASCII"),
                row("US", "PRESIDENT", "President",
                    "presidency number, one per non-consecutive presidency; a person returning " +
                    "after an absence gets a new number",
                    "ASCII"),
                row("FR", "PRESIDENT", "Président de la République",
                    "ordinal of the person by first election; a person re-elected has one line " +
                    "per term and keeps the same number on each",
                    "non-ASCII"),
                row("JP", "PRIME_MINISTER", "内閣総理大臣",
                    "代, one per appointment; a cabinet re-formed after a general election starts " +
                    "a new 代, so a person keeps one line per 代 with a new number on each",
                    "non-ASCII"),
                row("KR", "PRESIDENT", "대통령",
                    "대, one per election; a person re-elected keeps one line per term with " +
                    "consecutive numbers",
                    "non-ASCII")
        );

        // JPQL has no character class; LIKE matches no range of characters, GLOB '*[^ -~]*' is
        // SQLite's own,
        // and the Criteria API has no pattern but LIKE's either
        @Test
        void nativeQuery() {
            final var list = __Exercises_TestUtils.apply(em -> (List<?>) em.createNativeQuery(
                            """
                                    SELECT *,
                                           CASE WHEN head_title GLOB '*[^ -~]*' THEN 'non-ASCII' ELSE 'ASCII' END AS script
                                    FROM office
                                    ORDER BY script ASC, country ASC"""
                    )
                    .getResultList());
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_OFFICE_I_03: Offices without an incumbent")
    @Nested
    class HOG_OFFICE_I_03_Test {

        private static final List<List<Object>> EXPECTED = List.of();

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT o
                                    FROM Office AS o
                                    WHERE NOT EXISTS (SELECT h
                                                      FROM Head AS h
                                                      WHERE h.office = o
                                                        AND h.termEnd IS NULL)
                                    ORDER BY o.country ASC""",
                            Office.class
                    )
                    .getResultList());
            assertThat(rows(list.stream()
                                    .map(o -> new Object[]{o.getCountry(), o.getHeadTitle()})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }

        @Test
        void __CriteriaApi() {
            final var list = __Exercises_TestUtils.apply(em -> {
                final var cb = em.getCriteriaBuilder();
                final var cq = cb.createQuery(Office.class);
                final var o = cq.from(Office.class);
                final var incumbent = cq.subquery(Head.class);
                final var h = incumbent.from(Head.class);
                incumbent.select(h)
                        .where(cb.equal(h.get(Head_.office), o), cb.isNull(h.get(Head_.termEnd)));
                cq.select(o)
                        .where(cb.not(cb.exists(incumbent)))
                        .orderBy(cb.asc(o.get(Office_.country)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list.stream()
                                    .map(o -> new Object[]{o.getCountry(), o.getHeadTitle()})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }
    }
}
