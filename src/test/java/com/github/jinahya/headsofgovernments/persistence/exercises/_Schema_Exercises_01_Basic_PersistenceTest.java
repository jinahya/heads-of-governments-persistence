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
import jakarta.persistence.criteria.JoinType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static com.github.jinahya.headsofgovernments.persistence.exercises.__Exercises_TestUtils.row;
import static com.github.jinahya.headsofgovernments.persistence.exercises.__Exercises_TestUtils.rows;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Answers the exercises of {@code src/test/sql/exercises/_schema_exercises_01_basic.sql}, against
 * {@code heads_of_governments.sqlite3}; a nested class per exercise, which answers it with JPQL
 * and with the Criteria API, or with a native query where neither can, and checks that it gives
 * what the SQL of the file gives.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@Slf4j
class _Schema_Exercises_01_Basic_PersistenceTest {

    @DisplayName("HOG_SCHEMA_B_01: The incumbents, with their titles")
    @Nested
    class HOG_SCHEMA_B_01_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", "Président de la République", "Emmanuel Macron", "2022-05-14"),
                row("GB", "Prime Minister", "Andy Burnham", "2026-07-20"),
                row("JP", "内閣総理大臣", "高市早苗", "2026-02-18"),
                row("KR", "대통령", "이재명", "2025-06-04"),
                row("US", "President", "Donald Trump", "2025-01-20")
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT g, h
                                    FROM Office AS g
                                         JOIN g.heads AS h ON h.termEnd IS NULL
                                    ORDER BY g.country ASC""",
                            Object[].class
                    )
                    .getResultList());
            assertThat(rows(list.stream()
                                    .map(r -> {
                                        final var g = (Office) r[0];
                                        final var h = (Head) r[1];
                                        return new Object[]{g.getCountry(), g.getHeadTitle(),
                                                h.getName(), h.getTermStart()};
                                    })
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }

        @Test
        void __CriteriaApi() {
            final var list = __Exercises_TestUtils.apply(em -> {
                final var cb = em.getCriteriaBuilder();
                final var cq = cb.createQuery(Object[].class);
                final var g = cq.from(Office.class);
                final var h = g.join(Office_.heads);
                h.on(cb.isNull(h.get(Head_.termEnd)));
                cq.select(cb.array(g, h))
                        .orderBy(cb.asc(g.get(Office_.country)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list.stream()
                                    .map(r -> {
                                        final var g = (Office) r[0];
                                        final var h = (Head) r[1];
                                        return new Object[]{g.getCountry(), g.getHeadTitle(),
                                                h.getName(), h.getTermStart()};
                                    })
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_SCHEMA_B_03: Terms per office")
    @Nested
    class HOG_SCHEMA_B_03_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", "PRESIDENT", 31L),
                row("GB", "PRIME_MINISTER", 81L),
                row("JP", "PRIME_MINISTER", 105L),
                row("KR", "PRESIDENT", 21L),
                row("US", "PRESIDENT", 47L)
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT g, COUNT(h.id)
                                    FROM Office AS g
                                         LEFT JOIN g.heads AS h
                                    GROUP BY g
                                    ORDER BY g.country ASC""",
                            Object[].class
                    )
                    .getResultList());
            assertThat(rows(list.stream()
                                    .map(r -> new Object[]{((Office) r[0]).getCountry(),
                                            ((Office) r[0]).getHeadKind(), r[1]})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }

        @Test
        void __CriteriaApi() {
            final var list = __Exercises_TestUtils.apply(em -> {
                final var cb = em.getCriteriaBuilder();
                final var cq = cb.createQuery(Object[].class);
                final var g = cq.from(Office.class);
                final var h = g.join(Office_.heads, JoinType.LEFT);
                cq.select(cb.array(g, cb.count(h.get(Head_.id))))
                        .groupBy(g)
                        .orderBy(cb.asc(g.get(Office_.country)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list.stream()
                                    .map(r -> new Object[]{((Office) r[0]).getCountry(),
                                            ((Office) r[0]).getHeadKind(), r[1]})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_SCHEMA_B_05: Presidents since 2000")
    @Nested
    class HOG_SCHEMA_B_05_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("2001-01-20", "US", "President", "George W. Bush", "Republican"),
                row("2002-05-17", "FR", "Président de la République", "Jacques Chirac", "RPR/UMP"),
                row("2003-02-25", "KR", "대통령", "노무현", "새천년민주당/무소속/열린우리당/무소속"),
                row("2007-05-16", "FR", "Président de la République", "Nicolas Sarkozy", "UMP"),
                row("2008-02-25", "KR", "대통령", "이명박", "한나라당/새누리당"),
                row("2009-01-20", "US", "President", "Barack Obama", "Democratic"),
                row("2012-05-15", "FR", "Président de la République", "François Hollande", "PS"),
                row("2013-02-25", "KR", "대통령", "박근혜", "새누리당/자유한국당"),
                row("2017-01-20", "US", "President", "Donald Trump", "Republican"),
                row("2017-05-10", "KR", "대통령", "문재인", "더불어민주당"),
                row("2017-05-14", "FR", "Président de la République", "Emmanuel Macron", "LREM"),
                row("2021-01-20", "US", "President", "Joe Biden", "Democratic"),
                row("2022-05-10", "KR", "대통령", "윤석열", "국민의힘"),
                row("2022-05-14", "FR", "Président de la République", "Emmanuel Macron",
                    "LREM/Renaissance"),
                row("2025-01-20", "US", "President", "Donald Trump", "Republican"),
                row("2025-06-04", "KR", "대통령", "이재명", "더불어민주당")
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT g, h
                                    FROM Office AS g
                                         JOIN g.heads AS h
                                    WHERE g.headKind = 'PRESIDENT'
                                      AND h.termStart >= '2000-01-01'
                                    ORDER BY h.termStart ASC, g.country ASC""",
                            Object[].class
                    )
                    .getResultList());
            assertThat(rows(list.stream()
                                    .map(r -> {
                                        final var g = (Office) r[0];
                                        final var h = (Head) r[1];
                                        return new Object[]{h.getTermStart(), g.getCountry(),
                                                g.getHeadTitle(), h.getName(), h.getParty()};
                                    })
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }

        @Test
        void __CriteriaApi() {
            final var list = __Exercises_TestUtils.apply(em -> {
                final var cb = em.getCriteriaBuilder();
                final var cq = cb.createQuery(Object[].class);
                final var g = cq.from(Office.class);
                final var h = g.join(Office_.heads);
                cq.select(cb.array(g, h))
                        .where(cb.equal(g.get(Office_.headKind), Office.HeadKind.PRESIDENT.name()),
                               cb.greaterThanOrEqualTo(h.get(Head_.termStart), "2000-01-01"))
                        .orderBy(cb.asc(h.get(Head_.termStart)), cb.asc(g.get(Office_.country)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list.stream()
                                    .map(r -> {
                                        final var g = (Office) r[0];
                                        final var h = (Head) r[1];
                                        return new Object[]{h.getTermStart(), g.getCountry(),
                                                g.getHeadTitle(), h.getName(), h.getParty()};
                                    })
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_SCHEMA_B_06: Presidents without a party")
    @Nested
    class HOG_SCHEMA_B_06_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", "Président de la République", 12L, "Alexandre Millerand", "Indépendant",
                    "1920-09-23"),
                row("FR", "Président de la République", 14L, "Paul Doumer", "Indépendant",
                    "1931-06-13"),
                row("KR", "대통령", 4L, "윤보선", "무소속", "1960-08-12"),
                row("KR", "대통령", 10L, "최규하", "무소속", "1979-12-06"),
                row("US", "President", 1L, "George Washington", "Unaffiliated", "1789-04-30")
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT g, h
                                    FROM Office AS g
                                         JOIN g.heads AS h
                                    WHERE g.headKind = 'PRESIDENT'
                                      AND h.party IN ('Indépendant', 'Unaffiliated', '무소속')
                                    ORDER BY g.country ASC, h.termStart ASC""",
                            Object[].class
                    )
                    .getResultList());
            assertThat(rows(list.stream()
                                    .map(r -> {
                                        final var g = (Office) r[0];
                                        final var h = (Head) r[1];
                                        return new Object[]{g.getCountry(), g.getHeadTitle(),
                                                h.getNumber(), h.getName(), h.getParty(),
                                                h.getTermStart()};
                                    })
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }

        @Test
        void __CriteriaApi() {
            final var list = __Exercises_TestUtils.apply(em -> {
                final var cb = em.getCriteriaBuilder();
                final var cq = cb.createQuery(Object[].class);
                final var g = cq.from(Office.class);
                final var h = g.join(Office_.heads);
                cq.select(cb.array(g, h))
                        .where(cb.equal(g.get(Office_.headKind), Office.HeadKind.PRESIDENT.name()),
                               h.get(Head_.party).in("Indépendant", "Unaffiliated", "무소속"))
                        .orderBy(cb.asc(g.get(Office_.country)), cb.asc(h.get(Head_.termStart)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list.stream()
                                    .map(r -> {
                                        final var g = (Office) r[0];
                                        final var h = (Head) r[1];
                                        return new Object[]{g.getCountry(), g.getHeadTitle(),
                                                h.getNumber(), h.getName(), h.getParty(),
                                                h.getTermStart()};
                                    })
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_SCHEMA_B_07: Terms since 2000 per office")
    @Nested
    class HOG_SCHEMA_B_07_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("JP", "内閣総理大臣", 21L),
                row("GB", "Prime Minister", 8L),
                row("KR", "대통령", 6L),
                row("FR", "Président de la République", 5L),
                row("US", "President", 5L)
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT g, COUNT(h.id) AS terms
                                    FROM Office AS g
                                         LEFT JOIN g.heads AS h ON h.termStart >= '2000-01-01'
                                    GROUP BY g
                                    ORDER BY terms DESC, g.country ASC""",
                            Object[].class
                    )
                    .getResultList());
            assertThat(rows(list.stream()
                                    .map(r -> new Object[]{((Office) r[0]).getCountry(),
                                            ((Office) r[0]).getHeadTitle(), r[1]})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }

        @Test
        void __CriteriaApi() {
            final var list = __Exercises_TestUtils.apply(em -> {
                final var cb = em.getCriteriaBuilder();
                final var cq = cb.createQuery(Object[].class);
                final var g = cq.from(Office.class);
                final var h = g.join(Office_.heads, JoinType.LEFT);
                h.on(cb.greaterThanOrEqualTo(h.get(Head_.termStart), "2000-01-01"));
                final var terms = cb.count(h.get(Head_.id));
                cq.select(cb.array(g, terms))
                        .groupBy(g)
                        .orderBy(cb.desc(terms), cb.asc(g.get(Office_.country)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list.stream()
                                    .map(r -> new Object[]{((Office) r[0]).getCountry(),
                                            ((Office) r[0]).getHeadTitle(), r[1]})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }
    }
}
