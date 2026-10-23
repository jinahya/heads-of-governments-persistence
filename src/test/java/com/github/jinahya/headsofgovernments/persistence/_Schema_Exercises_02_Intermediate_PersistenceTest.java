package com.github.jinahya.headsofgovernments.persistence;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Function;

import static com.github.jinahya.headsofgovernments.persistence.__Exercises_TestUtils.row;
import static com.github.jinahya.headsofgovernments.persistence.__Exercises_TestUtils.rows;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Answers the exercises of {@code src/test/sql/_schema_exercises_02_intermediate.sql}, against {@code
 * db/heads_of_governments.sqlite3}; a nested class per exercise, which answers it with JPQL and with the Criteria
 * API, or with a native query where neither can, and checks that it gives what the SQL of the file gives.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@Slf4j
class _Schema_Exercises_02_Intermediate_PersistenceTest {

    @DisplayName("exercise 1: Presidents against prime ministers")
    @Nested
    class Exercise1Test {

        /**
         * The average counts each open term to today, so it is worked out here: the days of the completed
         * terms, which do not change, plus those of the open terms to today, over the terms.
         */
        private static final List<List<Object>> EXPECTED = List.of(
                row("PRESIDENT", 99L, 84L, averageDays(167226L, 99L, "2022-05-14", "2025-06-04", "2025-01-20")),
                row("PRIME_MINISTER", 186L, 125L, averageDays(162223L, 186L, "2026-07-20", "2026-02-18"))
        );

        private static long averageDays(final long completedDays, final long terms, final String... openTermStarts) {
            final var today = LocalDate.now(ZoneOffset.UTC);
            var days = completedDays;
            for (final var openTermStart : openTermStarts) {
                days += ChronoUnit.DAYS.between(LocalDate.parse(openTermStart), today);
            }
            return days / terms; // CAST(... AS INTEGER) truncates
        }

        // JPQL has no difference of two dates, nor today's date as text; julianday() and date('now') are
        // SQLite's own, and the Criteria API has neither
        @Test
        void nativeQuery() {
            final var list = __Exercises_TestUtils.apply(em -> (List<?>) em.createNativeQuery(
                            """
                                    SELECT g.head_kind,
                                           COUNT(*)                                                                                       AS terms,
                                           COUNT(DISTINCT g.country || ' ' || h.name)                                                     AS persons,
                                           CAST(AVG(julianday(COALESCE(h.term_end, date('now'))) - julianday(h.term_start)) AS INTEGER)   AS average_days
                                    FROM office g
                                             JOIN head h ON h.country = g.country
                                    GROUP BY g.head_kind
                                    ORDER BY g.head_kind ASC"""
                    )
                    .getResultList());
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("exercise 2: Numbering in practice")
    @Nested
    class Exercise2Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", 31L, 25L, 25L, "ordinal of the person by first election; a person re-elected has one line per term and keeps the same number on each"),
                row("GB", 81L, 59L, 59L, "ordinal of the person by first appointment; a person who served non-consecutive terms has one line per term and keeps the same number on each"),
                row("JP", 105L, 105L, 105L, "代, one per appointment; a cabinet re-formed after a general election starts a new 代, so a person keeps one line per 代 with a new number on each"),
                row("KR", 21L, 21L, 21L, "대, one per election; a person re-elected keeps one line per term with consecutive numbers"),
                row("US", 47L, 47L, 47L, "presidency number, one per non-consecutive presidency; a person returning after an absence gets a new number")
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT g.country, COUNT(h), COUNT(DISTINCT h.number), MAX(h.number), g.numbering
                                    FROM Office AS g
                                         JOIN g.heads AS h
                                    GROUP BY g.country, g.numbering
                                    ORDER BY g.country ASC""",
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
                cq.select(cb.array(g.get(Office_.country), cb.count(h), cb.countDistinct(h.get(Head_.number)),
                                   cb.max(h.get(Head_.number)), g.get(Office_.numbering)))
                        .groupBy(g.get(Office_.country), g.get(Office_.numbering))
                        .orderBy(cb.asc(g.get(Office_.country)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("exercise 3: The span of each list")
    @Nested
    class Exercise3Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", "Président de la République", "1848-12-20", "2022-05-14", 174L),
                row("GB", "Prime Minister", "1721-04-03", "2026-07-20", 305L),
                row("JP", "内閣総理大臣", "1885-12-22", "2026-02-18", 141L),
                row("KR", "대통령", "1948-07-24", "2025-06-04", 77L),
                row("US", "President", "1789-04-30", "2025-01-20", 236L)
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT g.country,
                                           g.headTitle,
                                           MIN(h.termStart),
                                           MAX(h.termStart),
                                           CAST(SUBSTRING(MAX(h.termStart), 1, 4) AS INTEGER) - CAST(SUBSTRING(MIN(h.termStart), 1, 4) AS INTEGER)
                                    FROM Office AS g
                                         JOIN g.heads AS h
                                    GROUP BY g.country, g.headTitle
                                    ORDER BY g.country ASC""",
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
                final var first = cb.least(h.get(Head_.termStart));
                final var latest = cb.greatest(h.get(Head_.termStart));
                cq.select(cb.array(g.get(Office_.country), g.get(Office_.headTitle), first, latest,
                                   cb.diff(cb.substring(latest, 1, 4).cast(Integer.class),
                                           cb.substring(first, 1, 4).cast(Integer.class))))
                        .groupBy(g.get(Office_.country), g.get(Office_.headTitle))
                        .orderBy(cb.asc(g.get(Office_.country)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("exercise 4: Living heads per office")
    @Nested
    class Exercise4Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", "Président de la République", 3L),
                row("GB", "Prime Minister", 10L),
                row("JP", "内閣総理大臣", 12L),
                row("KR", "대통령", 5L),
                row("US", "President", 5L)
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT g.country, g.headTitle, COUNT(DISTINCT h.name)
                                    FROM Office AS g
                                         JOIN g.heads AS h
                                    WHERE h.died IS NULL
                                    GROUP BY g.country, g.headTitle
                                    ORDER BY g.country ASC""",
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
                cq.select(cb.array(g.get(Office_.country), g.get(Office_.headTitle), cb.countDistinct(h.get(Head_.name))))
                        .where(cb.isNull(h.get(Head_.died)))
                        .groupBy(g.get(Office_.country), g.get(Office_.headTitle))
                        .orderBy(cb.asc(g.get(Office_.country)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("exercise 5: Terms per century, side by side")
    @Nested
    class Exercise5Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", "Président de la République", 0L, 9L, 17L, 5L),
                row("GB", "Prime Minister", 16L, 33L, 24L, 8L),
                row("JP", "内閣総理大臣", 0L, 9L, 75L, 21L),
                row("KR", "대통령", 0L, 0L, 15L, 6L),
                row("US", "President", 2L, 23L, 17L, 5L)
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT g.country,
                                           g.headTitle,
                                           SUM(CASE WHEN h.termStart < '1800' THEN 1 ELSE 0 END),
                                           SUM(CASE WHEN h.termStart >= '1800' AND h.termStart < '1900' THEN 1 ELSE 0 END),
                                           SUM(CASE WHEN h.termStart >= '1900' AND h.termStart < '2000' THEN 1 ELSE 0 END),
                                           SUM(CASE WHEN h.termStart >= '2000' THEN 1 ELSE 0 END)
                                    FROM Office AS g
                                         JOIN g.heads AS h
                                    GROUP BY g.country, g.headTitle
                                    ORDER BY g.country ASC""",
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
                final var termStart = h.get(Head_.termStart);
                final Function<Predicate, Expression<Integer>> count
                        = p -> cb.sum(cb.<Integer>selectCase().when(p, 1).otherwise(0));
                cq.select(cb.array(g.get(Office_.country), g.get(Office_.headTitle),
                                   count.apply(cb.lessThan(termStart, "1800")),
                                   count.apply(cb.and(cb.greaterThanOrEqualTo(termStart, "1800"), cb.lessThan(termStart, "1900"))),
                                   count.apply(cb.and(cb.greaterThanOrEqualTo(termStart, "1900"), cb.lessThan(termStart, "2000"))),
                                   count.apply(cb.greaterThanOrEqualTo(termStart, "2000"))))
                        .groupBy(g.get(Office_.country), g.get(Office_.headTitle))
                        .orderBy(cb.asc(g.get(Office_.country)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("exercise 6: Age at first taking office, by the kind of head")
    @Nested
    class Exercise6Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("PRESIDENT", 84L, 40L, 79L, 58.1d),
                row("PRIME_MINISTER", 125L, 24L, 77L, 58.2d)
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT g.headKind,
                                           COUNT(h),
                                           MIN(CAST(SUBSTRING(h.termStart, 1, 4) AS INTEGER) - h.born),
                                           MAX(CAST(SUBSTRING(h.termStart, 1, 4) AS INTEGER) - h.born),
                                           ROUND(AVG(CAST(SUBSTRING(h.termStart, 1, 4) AS INTEGER) - h.born), 1)
                                    FROM Office AS g
                                         JOIN g.heads AS h
                                    WHERE h.termStart = (SELECT MIN(f.termStart)
                                                         FROM Head AS f
                                                         WHERE f.country = h.country
                                                           AND f.name = h.name
                                                           AND f.born = h.born)
                                    GROUP BY g.headKind
                                    ORDER BY g.headKind ASC""",
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
                // each person's first term, in place of the SQL's derived table of (country, name, born, MIN(term_start))
                final var first = cq.subquery(String.class);
                final var f = first.from(Head.class);
                first.select(cb.least(f.get(Head_.termStart)))
                        .where(cb.equal(f.get(Head_.country), h.get(Head_.country)),
                               cb.equal(f.get(Head_.name), h.get(Head_.name)),
                               cb.equal(f.get(Head_.born), h.get(Head_.born)));
                final var age = cb.diff(cb.substring(h.get(Head_.termStart), 1, 4).cast(Integer.class), h.get(Head_.born));
                cq.select(cb.array(g.get(Office_.headKind), cb.count(h), cb.min(age), cb.max(age), cb.round(cb.avg(age), 1)))
                        .where(cb.equal(h.get(Head_.termStart), first))
                        .groupBy(g.get(Office_.headKind))
                        .orderBy(cb.asc(g.get(Office_.headKind)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("exercise 7: The shortest completed term of each office")
    @Nested
    class Exercise7Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", "Président de la République", "Jean Casimir-Perier", "1894-06-27", "1895-01-16", 203L),
                row("GB", "Prime Minister", "Arthur Wellesley", "1834-11-17", "1834-12-09", 22L),
                row("JP", "内閣総理大臣", "岸田文雄", "2021-10-04", "2021-11-10", 37L),
                row("KR", "대통령", "전두환", "1980-08-27", "1981-02-24", 181L),
                row("US", "President", "William Henry Harrison", "1841-03-04", "1841-04-04", 31L)
        );

        // JPQL has no difference of two dates; julianday() is SQLite's own, and the Criteria API has none
        @Test
        void nativeQuery() {
            final var list = __Exercises_TestUtils.apply(em -> (List<?>) em.createNativeQuery(
                            """
                                    SELECT g.country,
                                           g.head_title,
                                           h.name,
                                           h.term_start,
                                           h.term_end,
                                           CAST(julianday(h.term_end) - julianday(h.term_start) AS INTEGER) AS days
                                    FROM office g
                                             JOIN head h ON h.country = g.country
                                    WHERE julianday(h.term_end) - julianday(h.term_start)
                                              = (SELECT MIN(julianday(s.term_end) - julianday(s.term_start))
                                                 FROM head s
                                                 WHERE s.country = g.country)
                                    ORDER BY g.country ASC, h.term_start ASC"""
                    )
                    .getResultList());
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }
}
