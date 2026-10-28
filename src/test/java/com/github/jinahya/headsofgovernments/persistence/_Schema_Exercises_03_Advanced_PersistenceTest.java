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

import jakarta.persistence.criteria.JoinType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.jinahya.headsofgovernments.persistence.__Exercises_TestUtils.row;
import static com.github.jinahya.headsofgovernments.persistence.__Exercises_TestUtils.rows;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Answers the exercises of {@code src/test/sql/exercises/_schema_exercises_03_advanced.sql}, against {@code
 * db/heads_of_governments.sqlite3}; a nested class per exercise, which answers it with JPQL and with the Criteria
 * API, or with a native query where neither can, and checks that it gives what the SQL of the file gives.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@Slf4j
class _Schema_Exercises_03_Advanced_PersistenceTest {

    @DisplayName("HOG_SCHEMA_A_01: Each office's longest-serving head")
    @Nested
    class HOG_SCHEMA_A_01_Test {

        /**
         * Each place is held by a completed term or terms, so what date('now') adds to an open term does not
         * move it.
         */
        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", "Président de la République", "François Mitterrand", 2L, 5109L),
                row("GB", "Prime Minister", "Robert Walpole", 1L, 7619L),
                row("JP", "内閣総理大臣", "安倍晋三", 4L, 3186L),
                row("KR", "대통령", "박정희", 5L, 5788L),
                row("US", "President", "Franklin D. Roosevelt", 1L, 4422L)
        );

        // JPQL has no difference of two dates, nor window functions; julianday() and date('now') are SQLite's
        // own and RANK() OVER (PARTITION BY ...) is SQL's, and the Criteria API has none of them
        @Test
        void nativeQuery() {
            final var list = __Exercises_TestUtils.apply(em -> (List<?>) em.createNativeQuery(
                            """
                                    SELECT country, head_title, name, terms, days
                                    FROM (SELECT g.country,
                                                 g.head_title,
                                                 h.name,
                                                 COUNT(*)                                                                                   AS terms,
                                                 CAST(SUM(julianday(COALESCE(h.term_end, date('now'))) - julianday(h.term_start)) AS INTEGER) AS days,
                                                 RANK() OVER (PARTITION BY g.country
                                                     ORDER BY SUM(julianday(COALESCE(h.term_end, date('now'))) - julianday(h.term_start)) DESC) AS place
                                          FROM office g
                                                   JOIN head h ON h.country = g.country
                                          GROUP BY g.country, g.head_title, h.name)
                                    WHERE place = 1
                                    ORDER BY country ASC"""
                    )
                    .getResultList());
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_SCHEMA_A_02: Every office on a day, vacant or not")
    @Nested
    class HOG_SCHEMA_A_02_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", "Président de la République", "(vacant)", null, null),
                row("GB", "Prime Minister", "Winston Churchill", "1940-05-10", "1945-07-26"),
                row("JP", "内閣総理大臣", "小磯國昭", "1944-07-22", "1945-04-07"),
                row("KR", "대통령", "(vacant)", null, null),
                row("US", "President", "Franklin D. Roosevelt", "1933-03-04", "1945-04-12")
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT g, COALESCE(h.name, '(vacant)'), h
                                    FROM Office AS g
                                         LEFT JOIN g.heads AS h ON h.termStart <= :date AND (h.termEnd IS NULL OR h.termEnd >= :date)
                                    ORDER BY g.country ASC""",
                            Object[].class
                    )
                    .setParameter("date", "1945-01-01")
                    .getResultList());
            assertThat(rows(list.stream()
                                    .map(r -> {
                                        final var g = (Office) r[0];
                                        final var h = (Head) r[2];
                                        return new Object[]{g.getCountry(), g.getHeadTitle(), r[1], h == null ? null : h.getTermStart(),
                                                            h == null ? null : h.getTermEnd()};
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
                final var h = g.join(Office_.heads, JoinType.LEFT);
                final var date = cb.parameter(String.class, "date");
                h.on(cb.lessThanOrEqualTo(h.get(Head_.termStart), date),
                     cb.or(cb.isNull(h.get(Head_.termEnd)), cb.greaterThanOrEqualTo(h.get(Head_.termEnd), date)));
                cq.select(cb.array(g, cb.coalesce(h.get(Head_.name), "(vacant)"), h))
                        .orderBy(cb.asc(g.get(Office_.country)));
                return em.createQuery(cq)
                        .setParameter("date", "1945-01-01")
                        .getResultList();
            });
            assertThat(rows(list.stream()
                                    .map(r -> {
                                        final var g = (Office) r[0];
                                        final var h = (Head) r[2];
                                        return new Object[]{g.getCountry(), g.getHeadTitle(), r[1], h == null ? null : h.getTermStart(),
                                                            h == null ? null : h.getTermEnd()};
                                    })
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_SCHEMA_A_03: The incumbents' contemporaries")
    @Nested
    class HOG_SCHEMA_A_03_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", "Emmanuel Macron", "2022-05-14", "GB", "Prime Minister", "Boris Johnson"),
                row("FR", "Emmanuel Macron", "2022-05-14", "JP", "内閣総理大臣", "岸田文雄"),
                row("FR", "Emmanuel Macron", "2022-05-14", "KR", "대통령", "윤석열"),
                row("FR", "Emmanuel Macron", "2022-05-14", "US", "President", "Joe Biden"),
                row("GB", "Andy Burnham", "2026-07-20", "FR", "Président de la République", "Emmanuel Macron"),
                row("GB", "Andy Burnham", "2026-07-20", "JP", "内閣総理大臣", "高市早苗"),
                row("GB", "Andy Burnham", "2026-07-20", "KR", "대통령", "이재명"),
                row("GB", "Andy Burnham", "2026-07-20", "US", "President", "Donald Trump"),
                row("JP", "高市早苗", "2026-02-18", "FR", "Président de la République", "Emmanuel Macron"),
                row("JP", "高市早苗", "2026-02-18", "GB", "Prime Minister", "Keir Starmer"),
                row("JP", "高市早苗", "2026-02-18", "KR", "대통령", "이재명"),
                row("JP", "高市早苗", "2026-02-18", "US", "President", "Donald Trump"),
                row("KR", "이재명", "2025-06-04", "FR", "Président de la République", "Emmanuel Macron"),
                row("KR", "이재명", "2025-06-04", "GB", "Prime Minister", "Keir Starmer"),
                row("KR", "이재명", "2025-06-04", "JP", "内閣総理大臣", "石破茂"),
                row("KR", "이재명", "2025-06-04", "US", "President", "Donald Trump"),
                row("US", "Donald Trump", "2025-01-20", "FR", "Président de la République", "Emmanuel Macron"),
                row("US", "Donald Trump", "2025-01-20", "GB", "Prime Minister", "Keir Starmer"),
                row("US", "Donald Trump", "2025-01-20", "JP", "内閣総理大臣", "石破茂"),
                row("US", "Donald Trump", "2025-01-20", "KR", "대통령", "윤석열")
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT i, o, g
                                    FROM Head AS i
                                         JOIN Head AS o ON o.country <> i.country
                                                       AND o.termStart <= i.termStart
                                                       AND (o.termEnd IS NULL OR o.termEnd >= i.termStart)
                                         JOIN o.office AS g
                                    WHERE i.termEnd IS NULL
                                    ORDER BY i.country ASC, o.country ASC""",
                            Object[].class
                    )
                    .getResultList());
            assertThat(rows(list.stream()
                                    .map(r -> {
                                        final var i = (Head) r[0];
                                        final var o = (Head) r[1];
                                        final var g = (Office) r[2];
                                        return new Object[]{i.getCountry(), i.getName(), i.getTermStart(), o.getCountry(),
                                                            g.getHeadTitle(), o.getName()};
                                    })
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }

        @Test
        void __CriteriaApi() {
            final var list = __Exercises_TestUtils.apply(em -> {
                final var cb = em.getCriteriaBuilder();
                final var cq = cb.createQuery(Object[].class);
                final var i = cq.from(Head.class);
                final var o = i.join(Head.class);
                o.on(cb.notEqual(o.get(Head_.country), i.get(Head_.country)),
                     cb.lessThanOrEqualTo(o.get(Head_.termStart), i.get(Head_.termStart)),
                     cb.or(cb.isNull(o.get(Head_.termEnd)), cb.greaterThanOrEqualTo(o.get(Head_.termEnd), i.get(Head_.termStart))));
                final var g = o.join(Head_.office);
                cq.select(cb.array(i, o, g))
                        .where(cb.isNull(i.get(Head_.termEnd)))
                        .orderBy(cb.asc(i.get(Head_.country)), cb.asc(o.get(Head_.country)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list.stream()
                                    .map(r -> {
                                        final var i = (Head) r[0];
                                        final var o = (Head) r[1];
                                        final var g = (Office) r[2];
                                        return new Object[]{i.getCountry(), i.getName(), i.getTermStart(), o.getCountry(),
                                                            g.getHeadTitle(), o.getName()};
                                    })
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_SCHEMA_A_04: Each office's longest-lived head")
    @Nested
    class HOG_SCHEMA_A_04_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", "Président de la République", "Valéry Giscard d'Estaing", 1926L, 2020L, 94L),
                row("GB", "Prime Minister", "James Callaghan", 1912L, 2005L, 93L),
                row("JP", "内閣総理大臣", "東久邇宮稔彦王", 1887L, 1990L, 103L),
                row("KR", "대통령", "윤보선", 1897L, 1990L, 93L),
                row("US", "President", "Jimmy Carter", 1924L, 2024L, 100L)
        );

        // selects no entity: a row is a person, DISTINCT over the terms of one, not a head
        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT DISTINCT g.country, g.headTitle, h.name, h.born, h.died, h.died - h.born
                                    FROM Office AS g
                                         JOIN g.heads AS h
                                    WHERE h.died IS NOT NULL
                                      AND h.died - h.born = (SELECT MAX(s.died - s.born)
                                                             FROM Head AS s
                                                             WHERE s.country = g.country
                                                               AND s.died IS NOT NULL)
                                    ORDER BY g.country ASC, h.name ASC""",
                            Object[].class
                    )
                    .getResultList());
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }

        @Test
        void __CriteriaApi() {
            final var list = __Exercises_TestUtils.apply(em -> {
                final var cb = em.getCriteriaBuilder();
                final var cq = cb.createQuery(Object[].class);
                final var g = cq.from(Office.class);
                final var h = g.join(Office_.heads);
                final var age = cb.diff(h.get(Head_.died), h.get(Head_.born));
                final var oldest = cq.subquery(Integer.class);
                final var s = oldest.from(Head.class);
                oldest.select(cb.max(cb.diff(s.get(Head_.died), s.get(Head_.born))))
                        .where(cb.equal(s.get(Head_.country), g.get(Office_.country)), cb.isNotNull(s.get(Head_.died)));
                cq.select(cb.array(g.get(Office_.country), g.get(Office_.headTitle), h.get(Head_.name), h.get(Head_.born),
                                   h.get(Head_.died), age))
                        .distinct(true)
                        .where(cb.isNotNull(h.get(Head_.died)), cb.equal(age, oldest))
                        .orderBy(cb.asc(g.get(Office_.country)), cb.asc(h.get(Head_.name)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_SCHEMA_A_05: Presidents and the prime ministers of their time")
    @Nested
    class HOG_SCHEMA_A_05_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("US", "Franklin D. Roosevelt", "1933-03-04", "1945-04-12", 15L),
                row("FR", "Alexandre Millerand", "1920-09-23", "1924-06-11", 10L),
                row("FR", "Albert Lebrun", "1932-05-10", "1939-05-10", 10L),
                row("FR", "François Mitterrand", "1988-05-21", "1995-05-17", 9L),
                row("US", "Bill Clinton", "1993-01-20", "2001-01-20", 9L),
                row("FR", "Nicolas Sarkozy", "2007-05-16", "2012-05-15", 9L),
                row("US", "Calvin Coolidge", "1923-08-02", "1929-03-04", 8L),
                row("US", "Harry S. Truman", "1945-04-12", "1953-01-20", 8L),
                row("US", "Dwight D. Eisenhower", "1953-01-20", "1961-01-20", 8L),
                row("FR", "Valéry Giscard d'Estaing", "1974-05-27", "1981-05-21", 8L)
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT p,
                                           COUNT(DISTINCT CONCAT(m.country, ' ', m.name)) AS primeMinisters
                                    FROM Head AS p
                                         JOIN p.office AS pg
                                         JOIN Head AS m ON m.termStart <= COALESCE(p.termEnd, '9999-12-31')
                                                       AND COALESCE(m.termEnd, '9999-12-31') >= p.termStart
                                         JOIN m.office AS mg
                                    WHERE pg.headKind = 'PRESIDENT'
                                      AND mg.headKind = 'PRIME_MINISTER'
                                    GROUP BY p
                                    ORDER BY primeMinisters DESC, p.termStart ASC, p.country ASC""",
                            Object[].class
                    )
                    .setMaxResults(10)
                    .getResultList());
            assertThat(rows(list.stream()
                                    .map(r -> {
                                        final var p = (Head) r[0];
                                        return new Object[]{p.getCountry(), p.getName(), p.getTermStart(), p.getTermEnd(), r[1]};
                                    })
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }

        @Test
        void __CriteriaApi() {
            final var list = __Exercises_TestUtils.apply(em -> {
                final var cb = em.getCriteriaBuilder();
                final var cq = cb.createQuery(Object[].class);
                final var p = cq.from(Head.class);
                final var pg = p.join(Head_.office);
                final var m = p.join(Head.class);
                m.on(cb.lessThanOrEqualTo(m.get(Head_.termStart), cb.coalesce(p.get(Head_.termEnd), "9999-12-31")),
                     cb.greaterThanOrEqualTo(cb.coalesce(m.get(Head_.termEnd), "9999-12-31"), p.get(Head_.termStart)));
                final var mg = m.join(Head_.office);
                final var primeMinisters = cb.countDistinct(
                        cb.concat(List.of(m.get(Head_.country), cb.literal(" "), m.get(Head_.name))));
                cq.select(cb.array(p, primeMinisters))
                        .where(cb.equal(pg.get(Office_.headKind), Office.HeadKind.PRESIDENT.name()),
                               cb.equal(mg.get(Office_.headKind), Office.HeadKind.PRIME_MINISTER.name()))
                        .groupBy(p)
                        .orderBy(cb.desc(primeMinisters), cb.asc(p.get(Head_.termStart)), cb.asc(p.get(Head_.country)));
                return em.createQuery(cq)
                        .setMaxResults(10)
                        .getResultList();
            });
            assertThat(rows(list.stream()
                                    .map(r -> {
                                        final var p = (Head) r[0];
                                        return new Object[]{p.getCountry(), p.getName(), p.getTermStart(), p.getTermEnd(), r[1]};
                                    })
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_SCHEMA_A_06: New heads per century, with a running total")
    @Nested
    class HOG_SCHEMA_A_06_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", "Président de la République", 19L, 8L, 8L),
                row("FR", "Président de la République", 20L, 14L, 22L),
                row("FR", "Président de la République", 21L, 3L, 25L),
                row("GB", "Prime Minister", 18L, 14L, 14L),
                row("GB", "Prime Minister", 19L, 18L, 32L),
                row("GB", "Prime Minister", 20L, 19L, 51L),
                row("GB", "Prime Minister", 21L, 8L, 59L),
                row("JP", "内閣総理大臣", 19L, 5L, 5L),
                row("JP", "内閣総理大臣", 20L, 49L, 54L),
                row("JP", "内閣総理大臣", 21L, 12L, 66L),
                row("KR", "대통령", 20L, 8L, 8L),
                row("KR", "대통령", 21L, 6L, 14L),
                row("US", "President", 18L, 2L, 2L),
                row("US", "President", 19L, 22L, 24L),
                row("US", "President", 20L, 17L, 41L),
                row("US", "President", 21L, 4L, 45L)
        );

        // JPQL has no WITH, no subquery in the FROM clause and no window functions; the running total is
        // SUM() OVER (PARTITION BY ... ORDER BY ...), which is SQL's, and the Criteria API has none of them
        @Test
        void nativeQuery() {
            final var list = __Exercises_TestUtils.apply(em -> (List<?>) em.createNativeQuery(
                            """
                                    WITH firsts AS (SELECT country, name, MIN(term_start) AS first_start
                                                    FROM head
                                                    GROUP BY country, name),
                                         per_century AS (SELECT g.country,
                                                                g.head_title,
                                                                CAST(SUBSTR(f.first_start, 1, 2) AS INTEGER) + 1 AS century,
                                                                COUNT(*)                                         AS new_persons
                                                         FROM office g
                                                                  JOIN firsts f ON f.country = g.country
                                                         GROUP BY g.country, g.head_title, century)
                                    SELECT country,
                                           head_title,
                                           century,
                                           new_persons,
                                           SUM(new_persons) OVER (PARTITION BY country ORDER BY century) AS persons_so_far
                                    FROM per_century
                                    ORDER BY country ASC, century ASC"""
                    )
                    .getResultList());
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_SCHEMA_A_07: Decades in which every office changed hands")
    @Nested
    class HOG_SCHEMA_A_07_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("1940s", 18L),
                row("1950s", 16L),
                row("1960s", 15L),
                row("1970s", 18L),
                row("1990s", 14L),
                row("2000s", 16L),
                row("2010s", 13L),
                row("2020s", 16L)
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT CONCAT(SUBSTRING(h.termStart, 1, 3), '0s') AS decade, COUNT(h)
                                    FROM Head AS h
                                    GROUP BY CONCAT(SUBSTRING(h.termStart, 1, 3), '0s')
                                    HAVING COUNT(DISTINCT h.country) = (SELECT COUNT(o) FROM Office AS o)
                                    ORDER BY decade ASC""",
                            Object[].class
                    )
                    .getResultList());
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }

        @Test
        void __CriteriaApi() {
            final var list = __Exercises_TestUtils.apply(em -> {
                final var cb = em.getCriteriaBuilder();
                final var cq = cb.createQuery(Object[].class);
                final var h = cq.from(Head.class);
                final var decade = cb.concat(cb.substring(h.get(Head_.termStart), 1, 3), "0s");
                final var offices = cq.subquery(Long.class);
                offices.select(cb.count(offices.from(Office.class)));
                cq.select(cb.array(decade, cb.count(h)))
                        .groupBy(decade)
                        .having(cb.equal(cb.countDistinct(h.get(Head_.country)), offices))
                        .orderBy(cb.asc(decade));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }
}
