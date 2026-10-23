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
 * Answers the exercises of {@code src/test/sql/head_exercises_03_advanced.sql}, against {@code
 * db/heads_of_governments.sqlite3}; a nested class per exercise, which answers it with JPQL and with the Criteria
 * API, or with a native query where neither can, and checks that it gives what the SQL of the file gives.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@Slf4j
class Head_Exercises_03_Advanced_PersistenceTest {

    @DisplayName("exercise 1: Vacancies")
    @Nested
    class Exercise1Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", "Louis-Napoléon Bonaparte", "1852-12-02", "Adolphe Thiers", "1871-08-31", 6845L),
                row("FR", "Sadi Carnot", "1894-06-25", "Jean Casimir-Perier", "1894-06-27", 1L),
                row("FR", "Félix Faure", "1899-02-16", "Émile Loubet", "1899-02-18", 1L),
                row("FR", "Paul Deschanel", "1920-09-21", "Alexandre Millerand", "1920-09-23", 1L),
                row("FR", "Alexandre Millerand", "1924-06-11", "Gaston Doumergue", "1924-06-13", 1L),
                row("FR", "Paul Doumer", "1932-05-07", "Albert Lebrun", "1932-05-10", 2L),
                row("FR", "Albert Lebrun", "1940-07-11", "Vincent Auriol", "1947-01-16", 2379L),
                row("FR", "Charles de Gaulle", "1969-04-28", "Georges Pompidou", "1969-06-20", 52L),
                row("FR", "Georges Pompidou", "1974-04-02", "Valéry Giscard d'Estaing", "1974-05-27", 54L),
                row("GB", "Robert Walpole", "1742-02-11", "Spencer Compton", "1742-02-16", 4L),
                row("GB", "Spencer Compton", "1743-07-02", "Henry Pelham", "1743-08-27", 55L),
                row("GB", "Henry Pelham", "1754-03-06", "Thomas Pelham-Holles", "1754-03-16", 9L),
                row("GB", "Thomas Pelham-Holles", "1756-11-11", "William Cavendish", "1756-11-16", 4L),
                row("GB", "John Stuart", "1763-04-08", "George Grenville", "1763-04-16", 7L),
                row("GB", "George Grenville", "1765-07-10", "Charles Watson-Wentworth", "1765-07-13", 2L),
                row("GB", "Charles Watson-Wentworth", "1782-07-01", "William Petty", "1782-07-04", 2L),
                row("GB", "William Petty", "1783-03-26", "William Cavendish-Bentinck", "1783-04-02", 6L),
                row("GB", "William Pitt the Younger", "1801-03-14", "Henry Addington", "1801-03-17", 2L),
                row("GB", "William Pitt the Younger", "1806-01-23", "William Grenville", "1806-02-11", 18L),
                row("GB", "William Grenville", "1807-03-25", "William Cavendish-Bentinck", "1807-03-31", 5L),
                row("GB", "Spencer Perceval", "1812-05-11", "Robert Jenkinson", "1812-06-08", 27L),
                row("GB", "Robert Jenkinson", "1827-04-09", "George Canning", "1827-04-12", 2L),
                row("GB", "George Canning", "1827-08-08", "Frederick John Robinson", "1827-08-31", 22L),
                row("GB", "Frederick John Robinson", "1828-01-08", "Arthur Wellesley", "1828-01-22", 13L),
                row("GB", "Arthur Wellesley", "1830-11-16", "Charles Grey", "1830-11-22", 5L),
                row("GB", "Charles Grey", "1834-07-09", "William Lamb", "1834-07-16", 6L),
                row("GB", "William Lamb", "1834-11-14", "Arthur Wellesley", "1834-11-17", 2L),
                row("GB", "Robert Peel", "1835-04-08", "William Lamb", "1835-04-18", 9L),
                row("GB", "John Russell", "1852-02-21", "Edward Smith-Stanley", "1852-02-23", 1L),
                row("GB", "Edward Smith-Stanley", "1852-12-17", "George Hamilton-Gordon", "1852-12-19", 1L),
                row("GB", "George Hamilton-Gordon", "1855-01-30", "Henry John Temple", "1855-02-06", 6L),
                row("GB", "Henry John Temple", "1865-10-18", "John Russell", "1865-10-29", 10L),
                row("GB", "John Russell", "1866-06-26", "Edward Smith-Stanley", "1866-06-28", 1L),
                row("GB", "Edward Smith-Stanley", "1868-02-25", "Benjamin Disraeli", "1868-02-27", 1L),
                row("GB", "Benjamin Disraeli", "1868-12-01", "William Ewart Gladstone", "1868-12-03", 1L),
                row("GB", "William Ewart Gladstone", "1874-02-17", "Benjamin Disraeli", "1874-02-20", 2L),
                row("GB", "Benjamin Disraeli", "1880-04-21", "William Ewart Gladstone", "1880-04-23", 1L),
                row("GB", "William Ewart Gladstone", "1885-06-09", "Robert Gascoyne-Cecil", "1885-06-23", 13L),
                row("GB", "Robert Gascoyne-Cecil", "1886-01-28", "William Ewart Gladstone", "1886-02-01", 3L),
                row("GB", "William Ewart Gladstone", "1886-07-20", "Robert Gascoyne-Cecil", "1886-07-25", 4L),
                row("GB", "Robert Gascoyne-Cecil", "1892-08-11", "William Ewart Gladstone", "1892-08-15", 3L),
                row("GB", "William Ewart Gladstone", "1894-03-02", "Archibald Primrose", "1894-03-05", 2L),
                row("GB", "Archibald Primrose", "1895-06-22", "Robert Gascoyne-Cecil", "1895-06-25", 2L),
                row("GB", "Henry Campbell-Bannerman", "1908-04-03", "H. H. Asquith", "1908-04-08", 4L),
                row("GB", "David Lloyd George", "1922-10-19", "Bonar Law", "1922-10-23", 3L),
                row("GB", "Bonar Law", "1923-05-20", "Stanley Baldwin", "1923-05-22", 1L),
                row("JP", "黒田清隆", "1889-10-25", "山県有朋", "1889-12-24", 59L),
                row("JP", "伊藤博文", "1896-08-31", "松方正義", "1896-09-18", 17L),
                row("JP", "伊藤博文", "1901-05-10", "桂太郎", "1901-06-02", 22L),
                row("JP", "原敬", "1921-11-04", "高橋是清", "1921-11-13", 8L),
                row("JP", "加藤友三郎", "1923-08-24", "山本権兵衛", "1923-09-02", 8L),
                row("JP", "加藤高明", "1926-01-28", "若槻禮次郎", "1926-01-30", 1L),
                row("JP", "犬養毅", "1932-05-16", "斎藤実", "1932-05-26", 9L),
                row("JP", "大平正芳", "1980-06-12", "鈴木善幸", "1980-07-17", 34L),
                row("JP", "小渕恵三", "2000-04-03", "森喜朗", "2000-04-05", 1L),
                row("KR", "이승만", "1960-04-27", "윤보선", "1960-08-12", 106L),
                row("KR", "윤보선", "1962-03-22", "박정희", "1963-12-17", 634L),
                row("KR", "박정희", "1979-10-26", "최규하", "1979-12-06", 40L),
                row("KR", "최규하", "1980-08-16", "전두환", "1980-08-27", 10L),
                row("KR", "박근혜", "2017-03-10", "문재인", "2017-05-10", 60L),
                row("KR", "윤석열", "2025-04-04", "이재명", "2025-06-04", 60L)
        );

        // JPQL has no difference of two dates, nor window functions; julianday() is SQLite's own and LAG() is
        // SQL's, and the Criteria API has neither
        @Test
        void nativeQuery() {
            final var list = __Exercises_TestUtils.apply(em -> (List<?>) em.createNativeQuery(
                            """
                                    SELECT country,
                                           previous_name,
                                           previous_term_end,
                                           name,
                                           term_start,
                                           CAST(julianday(term_start) - julianday(previous_term_end) AS INTEGER) - 1 AS days_vacant
                                    FROM (SELECT country,
                                                 name,
                                                 term_start,
                                                 LAG(name) OVER (PARTITION BY country ORDER BY term_start)     AS previous_name, LAG(term_end) OVER (PARTITION BY country ORDER BY term_start) AS previous_term_end
                                          FROM head)
                                    WHERE julianday(term_start) - julianday(previous_term_end) > 1
                                    ORDER BY country ASC, term_start ASC"""
                    )
                    .getResultList());
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("exercise 2: Longest in office")
    @Nested
    class Exercise2Test {

        /**
         * Each place is held by a completed term or terms, so what date('now') adds to an open term does not
         * move it.
         */
        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", "François Mitterrand", 2L, 5109L),
                row("GB", "Robert Walpole", 1L, 7619L),
                row("JP", "安倍晋三", 4L, 3186L),
                row("KR", "박정희", 5L, 5788L),
                row("US", "Franklin D. Roosevelt", 1L, 4422L)
        );

        // JPQL has no difference of two dates, nor window functions; julianday() and date('now') are SQLite's
        // own and RANK() OVER (PARTITION BY ...) is SQL's, and the Criteria API has none of them
        @Test
        void nativeQuery() {
            final var list = __Exercises_TestUtils.apply(em -> (List<?>) em.createNativeQuery(
                            """
                                    SELECT country, name, terms, days
                                    FROM (SELECT country,
                                                 name,
                                                 COUNT(*)                                                                                  AS terms,
                                                 CAST(SUM(julianday(COALESCE(term_end, date ('now'))) - julianday(term_start)) AS INTEGER) AS days,
                                                 RANK()                                                                                       OVER (PARTITION BY country
                                                     ORDER BY SUM(julianday(COALESCE(term_end, date('now'))) - julianday(term_start)) DESC) AS place
                                          FROM head
                                          GROUP BY country, name)
                                    WHERE place = 1
                                    ORDER BY country ASC"""
                    )
                    .getResultList());
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("exercise 3: The longest run of one party")
    @Nested
    class Exercise3Test {

        /**
         * Each run ended, so date('now'), which closes an open run, does not move it.
         */
        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", "Républicain_opportuniste", 4L, "1879-01-30", "1895-01-16"),
                row("GB", "Whig", 6L, "1721-04-03", "1762-05-26"),
                row("JP", "自由民主党", 25L, "1955-11-22", "1993-08-09"),
                row("KR", "민주공화당", 5L, "1963-12-17", "1979-10-26"),
                row("US", "Republican", 4L, "1869-03-04", "1885-03-04")
        );

        // JPQL has no WITH, no subquery in the FROM clause and no window functions; the runs are the islands
        // of ROW_NUMBER() OVER (...) less ROW_NUMBER() OVER (PARTITION BY party ...), which only SQL can number
        @Test
        void nativeQuery() {
            final var list = __Exercises_TestUtils.apply(em -> (List<?>) em.createNativeQuery(
                            """
                                    WITH ordered AS (SELECT country,
                                                            party,
                                                            term_start,
                                                            term_end,
                                                            ROW_NUMBER() OVER (PARTITION BY country ORDER BY term_start)        AS seq, ROW_NUMBER() OVER (PARTITION BY country, party ORDER BY term_start) AS seq_in_party
                                                     FROM head),
                                         runs AS (SELECT country,
                                                         party,
                                                         COUNT(*)                              AS terms,
                                                         MIN(term_start)                       AS since,
                                                         MAX(COALESCE(term_end, date ('now'))) AS until
                                                  FROM ordered
                                                  GROUP BY country, party, seq - seq_in_party),
                                         placed AS (SELECT *, RANK() OVER (PARTITION BY country ORDER BY terms DESC, since ASC) AS place
                                                    FROM runs)
                                    SELECT country, party, terms, since, until
                                    FROM placed
                                    WHERE place = 1
                                    ORDER BY country ASC"""
                    )
                    .getResultList());
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("exercise 4: Every party of a head, one per row")
    @Nested
    class Exercise4Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", "Adolphe Thiers", "1871-08-31", 1L, "Orléaniste"),
                row("FR", "Adolphe Thiers", "1871-08-31", 2L, "Républicain_opportuniste"),
                row("FR", "Félix Faure", "1895-01-17", 1L, "Républicain_opportuniste"),
                row("FR", "Félix Faure", "1895-01-17", 2L, "Républicain_progressiste"),
                row("FR", "Charles de Gaulle", "1966-01-08", 1L, "UNR"),
                row("FR", "Charles de Gaulle", "1966-01-08", 2L, "UDR"),
                row("FR", "Valéry Giscard d'Estaing", "1974-05-27", 1L, "RI"),
                row("FR", "Valéry Giscard d'Estaing", "1974-05-27", 2L, "UDF"),
                row("FR", "Jacques Chirac", "2002-05-17", 1L, "RPR"),
                row("FR", "Jacques Chirac", "2002-05-17", 2L, "UMP"),
                row("FR", "Emmanuel Macron", "2022-05-14", 1L, "LREM"),
                row("FR", "Emmanuel Macron", "2022-05-14", 2L, "Renaissance"),
                row("GB", "Ramsay MacDonald", "1929-06-05", 1L, "Labour"),
                row("GB", "Ramsay MacDonald", "1929-06-05", 2L, "National_Labour"),
                row("JP", "吉田茂", "1949-02-16", 1L, "民主自由党"),
                row("JP", "吉田茂", "1949-02-16", 2L, "自由党"),
                row("KR", "이승만", "1948-07-24", 1L, "대한독립촉성국민회"),
                row("KR", "이승만", "1948-07-24", 2L, "자유당"),
                row("KR", "전두환", "1980-08-27", 1L, "무소속"),
                row("KR", "전두환", "1980-08-27", 2L, "민주정의당"),
                row("KR", "노태우", "1988-02-25", 1L, "민주정의당"),
                row("KR", "노태우", "1988-02-25", 2L, "민주자유당"),
                row("KR", "노태우", "1988-02-25", 3L, "무소속"),
                row("KR", "김영삼", "1993-02-25", 1L, "민주자유당"),
                row("KR", "김영삼", "1993-02-25", 2L, "신한국당"),
                row("KR", "김영삼", "1993-02-25", 3L, "무소속"),
                row("KR", "김대중", "1998-02-25", 1L, "새정치국민회의"),
                row("KR", "김대중", "1998-02-25", 2L, "새천년민주당"),
                row("KR", "김대중", "1998-02-25", 3L, "무소속"),
                row("KR", "노무현", "2003-02-25", 1L, "새천년민주당"),
                row("KR", "노무현", "2003-02-25", 2L, "무소속"),
                row("KR", "노무현", "2003-02-25", 3L, "열린우리당"),
                row("KR", "노무현", "2003-02-25", 4L, "무소속"),
                row("KR", "이명박", "2008-02-25", 1L, "한나라당"),
                row("KR", "이명박", "2008-02-25", 2L, "새누리당"),
                row("KR", "박근혜", "2013-02-25", 1L, "새누리당"),
                row("KR", "박근혜", "2013-02-25", 2L, "자유한국당"),
                row("US", "John Quincy Adams", "1825-03-04", 1L, "Democratic-Republican"),
                row("US", "John Quincy Adams", "1825-03-04", 2L, "National_Republican"),
                row("US", "Abraham Lincoln", "1861-03-04", 1L, "Republican"),
                row("US", "Abraham Lincoln", "1861-03-04", 2L, "National_Union"),
                row("US", "Andrew Johnson", "1865-04-15", 1L, "National_Union"),
                row("US", "Andrew Johnson", "1865-04-15", 2L, "Democratic")
        );

        // JPQL has no recursive query; WITH RECURSIVE is SQL's, and the Criteria API has none either
        @Test
        void nativeQuery() {
            final var list = __Exercises_TestUtils.apply(em -> (List<?>) em.createNativeQuery(
                            """
                                    WITH RECURSIVE parts(id, country, name, term_start, position, party, rest) AS
                                                       (SELECT id, country, name, term_start, 0, NULL, party || '/'
                                                        FROM head
                                                        WHERE party LIKE '%/%'
                                                        UNION ALL
                                                        SELECT id,
                                                               country,
                                                               name,
                                                               term_start,
                                                               position + 1,
                                                               SUBSTR(rest, 1, INSTR(rest, '/') - 1),
                                                               SUBSTR(rest, INSTR(rest, '/') + 1)
                                                        FROM parts
                                                        WHERE rest <> '')
                                    SELECT country, name, term_start, position, party
                                    FROM parts
                                    WHERE position > 0
                                    ORDER BY country ASC, term_start ASC, position ASC"""
                    )
                    .getResultList());
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("exercise 5: Predecessor and successor")
    @Nested
    class Exercise5Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("US", 45L, "2017-01-20", "2021-01-20", "Barack Obama", "Joe Biden"),
                row("US", 47L, "2025-01-20", null, "Joe Biden", null)
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT h.country, h.number, h.termStart, h.termEnd, p.name, s.name
                                    FROM Head AS h
                                         LEFT JOIN Head AS p ON p.country = h.country AND p.termStart < h.termStart
                                         LEFT JOIN Head AS s ON s.country = h.country AND s.termStart > h.termStart
                                    WHERE h.name = :name
                                      AND (p.id IS NULL
                                           OR p.termStart = (SELECT MAX(p2.termStart)
                                                             FROM Head AS p2
                                                             WHERE p2.country = h.country
                                                               AND p2.termStart < h.termStart))
                                      AND (s.id IS NULL
                                           OR s.termStart = (SELECT MIN(s2.termStart)
                                                             FROM Head AS s2
                                                             WHERE s2.country = h.country
                                                               AND s2.termStart > h.termStart))
                                    ORDER BY h.country ASC, h.termStart ASC""",
                            Object[].class
                    )
                    .setParameter("name", "Donald Trump")
                    .getResultList());
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }

        @Test
        void __CriteriaApi() {
            final var list = __Exercises_TestUtils.apply(em -> {
                final var cb = em.getCriteriaBuilder();
                final var cq = cb.createQuery(Object[].class);
                final var h = cq.from(Head.class);
                final var p = h.join(Head.class, JoinType.LEFT);
                p.on(cb.equal(p.get(Head_.country), h.get(Head_.country)),
                     cb.lessThan(p.get(Head_.termStart), h.get(Head_.termStart)));
                final var s = h.join(Head.class, JoinType.LEFT);
                s.on(cb.equal(s.get(Head_.country), h.get(Head_.country)),
                     cb.greaterThan(s.get(Head_.termStart), h.get(Head_.termStart)));
                final var previous = cq.subquery(String.class);
                final var p2 = previous.from(Head.class);
                previous.select(cb.greatest(p2.get(Head_.termStart)))
                        .where(cb.equal(p2.get(Head_.country), h.get(Head_.country)),
                               cb.lessThan(p2.get(Head_.termStart), h.get(Head_.termStart)));
                final var next = cq.subquery(String.class);
                final var s2 = next.from(Head.class);
                next.select(cb.least(s2.get(Head_.termStart)))
                        .where(cb.equal(s2.get(Head_.country), h.get(Head_.country)),
                               cb.greaterThan(s2.get(Head_.termStart), h.get(Head_.termStart)));
                cq.select(cb.array(h.get(Head_.country), h.get(Head_.number), h.get(Head_.termStart), h.get(Head_.termEnd),
                                   p.get(Head_.name), s.get(Head_.name)))
                        .where(cb.equal(h.get(Head_.name), cb.parameter(String.class, "name")),
                               cb.or(cb.isNull(p.get(Head_.id)), cb.equal(p.get(Head_.termStart), previous)),
                               cb.or(cb.isNull(s.get(Head_.id)), cb.equal(s.get(Head_.termStart), next)))
                        .orderBy(cb.asc(h.get(Head_.country)), cb.asc(h.get(Head_.termStart)));
                return em.createQuery(cq)
                        .setParameter("name", "Donald Trump")
                        .getResultList();
            });
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("exercise 6: How often a new head brings a new party")
    @Nested
    class Exercise6Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", 24L, 19L, 79.2d),
                row("GB", 80L, 42L, 52.5d),
                row("JP", 79L, 32L, 40.5d),
                row("KR", 13L, 13L, 100.0d),
                row("US", 46L, 29L, 63.0d)
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT h.country,
                                           COUNT(h),
                                           SUM(CASE WHEN h.party <> p.party THEN 1 ELSE 0 END),
                                           ROUND(100.0 * SUM(CASE WHEN h.party <> p.party THEN 1 ELSE 0 END) / COUNT(h), 1)
                                    FROM Head AS h
                                         JOIN Head AS p ON p.country = h.country
                                    WHERE p.termStart = (SELECT MAX(p2.termStart)
                                                         FROM Head AS p2
                                                         WHERE p2.country = h.country
                                                           AND p2.termStart < h.termStart)
                                      AND h.name <> p.name
                                    GROUP BY h.country
                                    ORDER BY h.country ASC""",
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
                final var p = h.join(Head.class);
                p.on(cb.equal(p.get(Head_.country), h.get(Head_.country)));
                final var previous = cq.subquery(String.class);
                final var p2 = previous.from(Head.class);
                previous.select(cb.greatest(p2.get(Head_.termStart)))
                        .where(cb.equal(p2.get(Head_.country), h.get(Head_.country)),
                               cb.lessThan(p2.get(Head_.termStart), h.get(Head_.termStart)));
                final var changes = cb.sum(cb.<Integer>selectCase()
                                                   .when(cb.notEqual(h.get(Head_.party), p.get(Head_.party)), 1)
                                                   .otherwise(0));
                cq.select(cb.array(h.get(Head_.country), cb.count(h), changes,
                                   cb.round(cb.quot(cb.prod(cb.literal(100.0d), changes), cb.count(h)), 1)))
                        .where(cb.equal(p.get(Head_.termStart), previous),
                               cb.notEqual(h.get(Head_.name), p.get(Head_.name)))
                        .groupBy(h.get(Head_.country))
                        .orderBy(cb.asc(h.get(Head_.country)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("exercise 7: Comebacks")
    @Nested
    class Exercise7Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("GB", "William Ewart Gladstone", 4L, "1868, 1880, 1886, 1892"),
                row("JP", "伊藤博文", 4L, "1885, 1892, 1898, 1900"),
                row("GB", "Edward Smith-Stanley", 3L, "1852, 1858, 1866"),
                row("GB", "Robert Gascoyne-Cecil", 3L, "1885, 1886, 1895"),
                row("GB", "Stanley Baldwin", 3L, "1923, 1924, 1935"),
                row("JP", "桂太郎", 3L, "1901, 1908, 1912")
        );

        // JPQL has no string aggregation; GROUP_CONCAT() is SQLite's own, and the Criteria API has none either
        @Test
        void nativeQuery() {
            final var list = __Exercises_TestUtils.apply(em -> (List<?>) em.createNativeQuery(
                            """
                                    WITH marked AS (SELECT country,
                                                           name,
                                                           term_start,
                                                           LAG(name) OVER (PARTITION BY country ORDER BY term_start) AS previous_name
                                                    FROM head)
                                    SELECT country,
                                           name,
                                           COUNT(*)                                                         AS stints,
                                           GROUP_CONCAT(SUBSTR(term_start, 1, 4), ', ' ORDER BY term_start) AS stint_years
                                    FROM marked
                                    WHERE previous_name IS NULL
                                       OR previous_name <> name
                                    GROUP BY country, name
                                    HAVING COUNT(*) >= 3
                                    ORDER BY stints DESC, country ASC, MIN(term_start) ASC"""
                    )
                    .getResultList());
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }
}
