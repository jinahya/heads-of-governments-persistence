package com.github.jinahya.headsofgovernments.persistence;

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
 * Answers the exercises of {@code src/test/sql/_schema_exercises_01_basic.sql}, against {@code
 * db/heads_of_governments.sqlite3}; a nested class per exercise, which answers it with JPQL and with the Criteria
 * API, or with a native query where neither can, and checks that it gives what the SQL of the file gives.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@Slf4j
class _Schema_Exercises_01_Basic_PersistenceTest {

    @DisplayName("exercise 1: The incumbents, with their titles")
    @Nested
    class Exercise1Test {

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
                                    SELECT g.country, g.headTitle, h.name, h.termStart
                                    FROM Office AS g
                                         JOIN g.heads AS h ON h.termEnd IS NULL
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
                h.on(cb.isNull(h.get(Head_.termEnd)));
                cq.select(cb.array(g.get(Office_.country), g.get(Office_.headTitle), h.get(Head_.name), h.get(Head_.termStart)))
                        .orderBy(cb.asc(g.get(Office_.country)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("exercise 2: A country's list, with the rule for its numbers")
    @Nested
    class Exercise2Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("대통령", "대, one per election; a person re-elected keeps one line per term with consecutive numbers", 1L, "1948-07-24", "1952-08-14", "이승만", "대한독립촉성국민회/자유당"),
                row("대통령", "대, one per election; a person re-elected keeps one line per term with consecutive numbers", 2L, "1952-08-15", "1956-08-14", "이승만", "자유당"),
                row("대통령", "대, one per election; a person re-elected keeps one line per term with consecutive numbers", 3L, "1956-08-15", "1960-04-27", "이승만", "자유당"),
                row("대통령", "대, one per election; a person re-elected keeps one line per term with consecutive numbers", 4L, "1960-08-12", "1962-03-22", "윤보선", "무소속"),
                row("대통령", "대, one per election; a person re-elected keeps one line per term with consecutive numbers", 5L, "1963-12-17", "1967-06-30", "박정희", "민주공화당"),
                row("대통령", "대, one per election; a person re-elected keeps one line per term with consecutive numbers", 6L, "1967-07-01", "1971-06-30", "박정희", "민주공화당"),
                row("대통령", "대, one per election; a person re-elected keeps one line per term with consecutive numbers", 7L, "1971-07-01", "1972-12-26", "박정희", "민주공화당"),
                row("대통령", "대, one per election; a person re-elected keeps one line per term with consecutive numbers", 8L, "1972-12-27", "1978-12-26", "박정희", "민주공화당"),
                row("대통령", "대, one per election; a person re-elected keeps one line per term with consecutive numbers", 9L, "1978-12-27", "1979-10-26", "박정희", "민주공화당"),
                row("대통령", "대, one per election; a person re-elected keeps one line per term with consecutive numbers", 10L, "1979-12-06", "1980-08-16", "최규하", "무소속"),
                row("대통령", "대, one per election; a person re-elected keeps one line per term with consecutive numbers", 11L, "1980-08-27", "1981-02-24", "전두환", "무소속/민주정의당"),
                row("대통령", "대, one per election; a person re-elected keeps one line per term with consecutive numbers", 12L, "1981-02-25", "1988-02-24", "전두환", "민주정의당"),
                row("대통령", "대, one per election; a person re-elected keeps one line per term with consecutive numbers", 13L, "1988-02-25", "1993-02-24", "노태우", "민주정의당/민주자유당/무소속"),
                row("대통령", "대, one per election; a person re-elected keeps one line per term with consecutive numbers", 14L, "1993-02-25", "1998-02-24", "김영삼", "민주자유당/신한국당/무소속"),
                row("대통령", "대, one per election; a person re-elected keeps one line per term with consecutive numbers", 15L, "1998-02-25", "2003-02-24", "김대중", "새정치국민회의/새천년민주당/무소속"),
                row("대통령", "대, one per election; a person re-elected keeps one line per term with consecutive numbers", 16L, "2003-02-25", "2008-02-24", "노무현", "새천년민주당/무소속/열린우리당/무소속"),
                row("대통령", "대, one per election; a person re-elected keeps one line per term with consecutive numbers", 17L, "2008-02-25", "2013-02-24", "이명박", "한나라당/새누리당"),
                row("대통령", "대, one per election; a person re-elected keeps one line per term with consecutive numbers", 18L, "2013-02-25", "2017-03-10", "박근혜", "새누리당/자유한국당"),
                row("대통령", "대, one per election; a person re-elected keeps one line per term with consecutive numbers", 19L, "2017-05-10", "2022-05-09", "문재인", "더불어민주당"),
                row("대통령", "대, one per election; a person re-elected keeps one line per term with consecutive numbers", 20L, "2022-05-10", "2025-04-04", "윤석열", "국민의힘"),
                row("대통령", "대, one per election; a person re-elected keeps one line per term with consecutive numbers", 21L, "2025-06-04", null, "이재명", "더불어민주당")
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT g.headTitle, g.numbering, h.number, h.termStart, h.termEnd, h.name, h.party
                                    FROM Office AS g
                                         JOIN g.heads AS h
                                    WHERE g.country = :country
                                    ORDER BY h.termStart ASC""",
                            Object[].class
                    )
                    .setParameter("country", "KR")
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
                cq.select(cb.array(g.get(Office_.headTitle), g.get(Office_.numbering), h.get(Head_.number),
                                   h.get(Head_.termStart), h.get(Head_.termEnd), h.get(Head_.name), h.get(Head_.party)))
                        .where(cb.equal(g.get(Office_.country), cb.parameter(String.class, "country")))
                        .orderBy(cb.asc(h.get(Head_.termStart)));
                return em.createQuery(cq)
                        .setParameter("country", "KR")
                        .getResultList();
            });
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("exercise 3: Terms per office")
    @Nested
    class Exercise3Test {

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
                                    SELECT g.country, g.headKind, COUNT(h.id)
                                    FROM Office AS g
                                         LEFT JOIN g.heads AS h
                                    GROUP BY g.country, g.headKind
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
                final var h = g.join(Office_.heads, JoinType.LEFT);
                cq.select(cb.array(g.get(Office_.country), g.get(Office_.headKind), cb.count(h.get(Head_.id))))
                        .groupBy(g.get(Office_.country), g.get(Office_.headKind))
                        .orderBy(cb.asc(g.get(Office_.country)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("exercise 4: The first head of each office")
    @Nested
    class Exercise4Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("GB", "Prime Minister", 1L, "Robert Walpole", "1721-04-03"),
                row("US", "President", 1L, "George Washington", "1789-04-30"),
                row("FR", "Président de la République", 1L, "Louis-Napoléon Bonaparte", "1848-12-20"),
                row("JP", "内閣総理大臣", 1L, "伊藤博文", "1885-12-22"),
                row("KR", "대통령", 1L, "이승만", "1948-07-24")
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT g.country, g.headTitle, h.number, h.name, h.termStart
                                    FROM Office AS g
                                         JOIN g.heads AS h
                                    WHERE h.number = 1
                                    ORDER BY h.termStart ASC""",
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
                cq.select(cb.array(g.get(Office_.country), g.get(Office_.headTitle), h.get(Head_.number), h.get(Head_.name),
                                   h.get(Head_.termStart)))
                        .where(cb.equal(h.get(Head_.number), 1))
                        .orderBy(cb.asc(h.get(Head_.termStart)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("exercise 5: Presidents since 2000")
    @Nested
    class Exercise5Test {

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
                row("2022-05-14", "FR", "Président de la République", "Emmanuel Macron", "LREM/Renaissance"),
                row("2025-01-20", "US", "President", "Donald Trump", "Republican"),
                row("2025-06-04", "KR", "대통령", "이재명", "더불어민주당")
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT h.termStart, g.country, g.headTitle, h.name, h.party
                                    FROM Office AS g
                                         JOIN g.heads AS h
                                    WHERE g.headKind = 'PRESIDENT'
                                      AND h.termStart >= '2000-01-01'
                                    ORDER BY h.termStart ASC, g.country ASC""",
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
                cq.select(cb.array(h.get(Head_.termStart), g.get(Office_.country), g.get(Office_.headTitle), h.get(Head_.name),
                                   h.get(Head_.party)))
                        .where(cb.equal(g.get(Office_.headKind), Office.HeadKind.PRESIDENT.name()),
                               cb.greaterThanOrEqualTo(h.get(Head_.termStart), "2000-01-01"))
                        .orderBy(cb.asc(h.get(Head_.termStart)), cb.asc(g.get(Office_.country)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("exercise 6: Presidents without a party")
    @Nested
    class Exercise6Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", "Président de la République", 12L, "Alexandre Millerand", "Indépendant", "1920-09-23"),
                row("FR", "Président de la République", 14L, "Paul Doumer", "Indépendant", "1931-06-13"),
                row("KR", "대통령", 4L, "윤보선", "무소속", "1960-08-12"),
                row("KR", "대통령", 10L, "최규하", "무소속", "1979-12-06"),
                row("US", "President", 1L, "George Washington", "Unaffiliated", "1789-04-30")
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT g.country, g.headTitle, h.number, h.name, h.party, h.termStart
                                    FROM Office AS g
                                         JOIN g.heads AS h
                                    WHERE g.headKind = 'PRESIDENT'
                                      AND h.party IN ('Indépendant', 'Unaffiliated', '무소속')
                                    ORDER BY g.country ASC, h.termStart ASC""",
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
                cq.select(cb.array(g.get(Office_.country), g.get(Office_.headTitle), h.get(Head_.number), h.get(Head_.name),
                                   h.get(Head_.party), h.get(Head_.termStart)))
                        .where(cb.equal(g.get(Office_.headKind), Office.HeadKind.PRESIDENT.name()),
                               h.get(Head_.party).in("Indépendant", "Unaffiliated", "무소속"))
                        .orderBy(cb.asc(g.get(Office_.country)), cb.asc(h.get(Head_.termStart)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("exercise 7: Terms since 2000 per office")
    @Nested
    class Exercise7Test {

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
                                    SELECT g.country, g.headTitle, COUNT(h.id) AS terms
                                    FROM Office AS g
                                         LEFT JOIN g.heads AS h ON h.termStart >= '2000-01-01'
                                    GROUP BY g.country, g.headTitle
                                    ORDER BY terms DESC, g.country ASC""",
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
                final var h = g.join(Office_.heads, JoinType.LEFT);
                h.on(cb.greaterThanOrEqualTo(h.get(Head_.termStart), "2000-01-01"));
                final var terms = cb.count(h.get(Head_.id));
                cq.select(cb.array(g.get(Office_.country), g.get(Office_.headTitle), terms))
                        .groupBy(g.get(Office_.country), g.get(Office_.headTitle))
                        .orderBy(cb.desc(terms), cb.asc(g.get(Office_.country)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }
}
