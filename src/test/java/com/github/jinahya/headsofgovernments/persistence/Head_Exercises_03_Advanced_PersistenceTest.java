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
                row(2L, "FR", 2L, "1871-08-31", "1873-05-24", "Adolphe Thiers", "Orléaniste/Républicain_opportuniste", 1797L, 1877L, "Louis-Napoléon Bonaparte", "1852-12-02", 6845L),
                row(7L, "FR", 6L, "1894-06-27", "1895-01-16", "Jean Casimir-Perier", "Républicain_opportuniste", 1847L, 1907L, "Sadi Carnot", "1894-06-25", 1L),
                row(9L, "FR", 8L, "1899-02-18", "1906-02-18", "Émile Loubet", "Alliance_républicaine_démocratique", 1838L, 1929L, "Félix Faure", "1899-02-16", 1L),
                row(13L, "FR", 12L, "1920-09-23", "1924-06-11", "Alexandre Millerand", "Indépendant", 1859L, 1943L, "Paul Deschanel", "1920-09-21", 1L),
                row(14L, "FR", 13L, "1924-06-13", "1931-06-13", "Gaston Doumergue", "Parti_radical", 1863L, 1937L, "Alexandre Millerand", "1924-06-11", 1L),
                row(16L, "FR", 15L, "1932-05-10", "1939-05-10", "Albert Lebrun", "Alliance_démocratique", 1871L, 1950L, "Paul Doumer", "1932-05-07", 2L),
                row(18L, "FR", 16L, "1947-01-16", "1954-01-16", "Vincent Auriol", "SFIO", 1884L, 1966L, "Albert Lebrun", "1940-07-11", 2379L),
                row(22L, "FR", 19L, "1969-06-20", "1974-04-02", "Georges Pompidou", "UDR", 1911L, 1974L, "Charles de Gaulle", "1969-04-28", 52L),
                row(23L, "FR", 20L, "1974-05-27", "1981-05-21", "Valéry Giscard d'Estaing", "RI/UDF", 1926L, 2020L, "Georges Pompidou", "1974-04-02", 54L),
                row(33L, "GB", 2L, "1742-02-16", "1743-07-02", "Spencer Compton", "Whig", 1673L, 1743L, "Robert Walpole", "1742-02-11", 4L),
                row(34L, "GB", 3L, "1743-08-27", "1754-03-06", "Henry Pelham", "Whig", 1694L, 1754L, "Spencer Compton", "1743-07-02", 55L),
                row(35L, "GB", 4L, "1754-03-16", "1756-11-11", "Thomas Pelham-Holles", "Whig", 1693L, 1768L, "Henry Pelham", "1754-03-06", 9L),
                row(36L, "GB", 5L, "1756-11-16", "1757-06-29", "William Cavendish", "Whig", 1720L, 1764L, "Thomas Pelham-Holles", "1756-11-11", 4L),
                row(39L, "GB", 7L, "1763-04-16", "1765-07-10", "George Grenville", "Whig", 1712L, 1770L, "John Stuart", "1763-04-08", 7L),
                row(40L, "GB", 8L, "1765-07-13", "1766-07-30", "Charles Watson-Wentworth", "Whig", 1730L, 1782L, "George Grenville", "1765-07-10", 2L),
                row(45L, "GB", 12L, "1782-07-04", "1783-03-26", "William Petty", "Whig", 1737L, 1805L, "Charles Watson-Wentworth", "1782-07-01", 2L),
                row(46L, "GB", 13L, "1783-04-02", "1783-12-18", "William Cavendish-Bentinck", "Whig", 1738L, 1809L, "William Petty", "1783-03-26", 6L),
                row(48L, "GB", 15L, "1801-03-17", "1804-05-10", "Henry Addington", "Tory", 1757L, 1844L, "William Pitt the Younger", "1801-03-14", 2L),
                row(50L, "GB", 16L, "1806-02-11", "1807-03-25", "William Grenville", "Whig", 1759L, 1834L, "William Pitt the Younger", "1806-01-23", 18L),
                row(51L, "GB", 13L, "1807-03-31", "1809-10-04", "William Cavendish-Bentinck", "Tory", 1738L, 1809L, "William Grenville", "1807-03-25", 5L),
                row(53L, "GB", 18L, "1812-06-08", "1827-04-09", "Robert Jenkinson", "Tory", 1770L, 1828L, "Spencer Perceval", "1812-05-11", 27L),
                row(54L, "GB", 19L, "1827-04-12", "1827-08-08", "George Canning", "Tory", 1770L, 1827L, "Robert Jenkinson", "1827-04-09", 2L),
                row(55L, "GB", 20L, "1827-08-31", "1828-01-08", "Frederick John Robinson", "Tory", 1782L, 1859L, "George Canning", "1827-08-08", 22L),
                row(56L, "GB", 21L, "1828-01-22", "1830-11-16", "Arthur Wellesley", "Tory", 1769L, 1852L, "Frederick John Robinson", "1828-01-08", 13L),
                row(57L, "GB", 22L, "1830-11-22", "1834-07-09", "Charles Grey", "Whig", 1764L, 1845L, "Arthur Wellesley", "1830-11-16", 5L),
                row(58L, "GB", 23L, "1834-07-16", "1834-11-14", "William Lamb", "Whig", 1779L, 1848L, "Charles Grey", "1834-07-09", 6L),
                row(59L, "GB", 21L, "1834-11-17", "1834-12-09", "Arthur Wellesley", "Tory", 1769L, 1852L, "William Lamb", "1834-11-14", 2L),
                row(61L, "GB", 23L, "1835-04-18", "1841-08-30", "William Lamb", "Whig", 1779L, 1848L, "Robert Peel", "1835-04-08", 9L),
                row(64L, "GB", 26L, "1852-02-23", "1852-12-17", "Edward Smith-Stanley", "Conservative", 1799L, 1869L, "John Russell", "1852-02-21", 1L),
                row(65L, "GB", 27L, "1852-12-19", "1855-01-30", "George Hamilton-Gordon", "Peelite", 1784L, 1860L, "Edward Smith-Stanley", "1852-12-17", 1L),
                row(66L, "GB", 28L, "1855-02-06", "1858-02-19", "Henry John Temple", "Whig", 1784L, 1865L, "George Hamilton-Gordon", "1855-01-30", 6L),
                row(69L, "GB", 25L, "1865-10-29", "1866-06-26", "John Russell", "Liberal", 1792L, 1878L, "Henry John Temple", "1865-10-18", 10L),
                row(70L, "GB", 26L, "1866-06-28", "1868-02-25", "Edward Smith-Stanley", "Conservative", 1799L, 1869L, "John Russell", "1866-06-26", 1L),
                row(71L, "GB", 29L, "1868-02-27", "1868-12-01", "Benjamin Disraeli", "Conservative", 1804L, 1881L, "Edward Smith-Stanley", "1868-02-25", 1L),
                row(72L, "GB", 30L, "1868-12-03", "1874-02-17", "William Ewart Gladstone", "Liberal", 1809L, 1898L, "Benjamin Disraeli", "1868-12-01", 1L),
                row(73L, "GB", 29L, "1874-02-20", "1880-04-21", "Benjamin Disraeli", "Conservative", 1804L, 1881L, "William Ewart Gladstone", "1874-02-17", 2L),
                row(74L, "GB", 30L, "1880-04-23", "1885-06-09", "William Ewart Gladstone", "Liberal", 1809L, 1898L, "Benjamin Disraeli", "1880-04-21", 1L),
                row(75L, "GB", 31L, "1885-06-23", "1886-01-28", "Robert Gascoyne-Cecil", "Conservative", 1830L, 1903L, "William Ewart Gladstone", "1885-06-09", 13L),
                row(76L, "GB", 30L, "1886-02-01", "1886-07-20", "William Ewart Gladstone", "Liberal", 1809L, 1898L, "Robert Gascoyne-Cecil", "1886-01-28", 3L),
                row(77L, "GB", 31L, "1886-07-25", "1892-08-11", "Robert Gascoyne-Cecil", "Conservative", 1830L, 1903L, "William Ewart Gladstone", "1886-07-20", 4L),
                row(78L, "GB", 30L, "1892-08-15", "1894-03-02", "William Ewart Gladstone", "Liberal", 1809L, 1898L, "Robert Gascoyne-Cecil", "1892-08-11", 3L),
                row(79L, "GB", 32L, "1894-03-05", "1895-06-22", "Archibald Primrose", "Liberal", 1847L, 1929L, "William Ewart Gladstone", "1894-03-02", 2L),
                row(80L, "GB", 31L, "1895-06-25", "1902-07-11", "Robert Gascoyne-Cecil", "Conservative", 1830L, 1903L, "Archibald Primrose", "1895-06-22", 2L),
                row(83L, "GB", 35L, "1908-04-08", "1916-12-05", "H. H. Asquith", "Liberal", 1852L, 1928L, "Henry Campbell-Bannerman", "1908-04-03", 4L),
                row(85L, "GB", 37L, "1922-10-23", "1923-05-20", "Bonar Law", "Conservative", 1858L, 1923L, "David Lloyd George", "1922-10-19", 3L),
                row(86L, "GB", 38L, "1923-05-22", "1924-01-22", "Stanley Baldwin", "Conservative", 1867L, 1947L, "Bonar Law", "1923-05-20", 1L),
                row(115L, "JP", 3L, "1889-12-24", "1891-05-06", "山県有朋", "無所属", 1838L, 1922L, "黒田清隆", "1889-10-25", 59L),
                row(118L, "JP", 6L, "1896-09-18", "1898-01-12", "松方正義", "無所属", 1835L, 1924L, "伊藤博文", "1896-08-31", 17L),
                row(123L, "JP", 11L, "1901-06-02", "1906-01-07", "桂太郎", "無所属", 1848L, 1913L, "伊藤博文", "1901-05-10", 22L),
                row(132L, "JP", 20L, "1921-11-13", "1922-06-12", "高橋是清", "立憲政友会", 1854L, 1936L, "原敬", "1921-11-04", 8L),
                row(134L, "JP", 22L, "1923-09-02", "1924-01-07", "山本権兵衛", "無所属", 1852L, 1933L, "加藤友三郎", "1923-08-24", 8L),
                row(137L, "JP", 25L, "1926-01-30", "1927-04-20", "若槻禮次郎", "憲政会", 1866L, 1949L, "加藤高明", "1926-01-28", 1L),
                row(142L, "JP", 30L, "1932-05-26", "1934-07-08", "斎藤実", "無所属", 1858L, 1936L, "犬養毅", "1932-05-16", 9L),
                row(182L, "JP", 70L, "1980-07-17", "1982-11-27", "鈴木善幸", "自由民主党", 1911L, 2004L, "大平正芳", "1980-06-12", 34L),
                row(197L, "JP", 85L, "2000-04-05", "2000-07-04", "森喜朗", "自由民主党", 1937L, null, "小渕恵三", "2000-04-03", 1L),
                row(221L, "KR", 4L, "1960-08-12", "1962-03-22", "윤보선", "무소속", 1897L, 1990L, "이승만", "1960-04-27", 106L),
                row(222L, "KR", 5L, "1963-12-17", "1967-06-30", "박정희", "민주공화당", 1917L, 1979L, "윤보선", "1962-03-22", 634L),
                row(227L, "KR", 10L, "1979-12-06", "1980-08-16", "최규하", "무소속", 1919L, 2006L, "박정희", "1979-10-26", 40L),
                row(228L, "KR", 11L, "1980-08-27", "1981-02-24", "전두환", "무소속/민주정의당", 1931L, 2021L, "최규하", "1980-08-16", 10L),
                row(236L, "KR", 19L, "2017-05-10", "2022-05-09", "문재인", "더불어민주당", 1953L, null, "박근혜", "2017-03-10", 60L),
                row(238L, "KR", 21L, "2025-06-04", null, "이재명", "더불어민주당", 1963L, null, "윤석열", "2025-04-04", 60L)
        );

        // JPQL has no difference of two dates, nor window functions; julianday() is SQLite's own and LAG() is
        // SQL's, and the Criteria API has neither
        @Test
        void nativeQuery() {
            final var list = __Exercises_TestUtils.apply(em -> (List<?>) em.createNativeQuery(
                            """
                                    SELECT *,
                                           CAST(julianday(term_start) - julianday(previous_term_end) AS INTEGER) - 1 AS days_vacant
                                    FROM (SELECT h.*,
                                                 LAG(name) OVER (PARTITION BY country ORDER BY term_start)     AS previous_name, LAG(term_end) OVER (PARTITION BY country ORDER BY term_start) AS previous_term_end
                                          FROM head AS h)
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
                                    SELECT h, p, s
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
            assertThat(rows(list.stream()
                                    .map(r -> {
                                        final var h = (Head) r[0];
                                        final var p = (Head) r[1];
                                        final var s = (Head) r[2];
                                        return new Object[]{h.getCountry(), h.getNumber(), h.getTermStart(), h.getTermEnd(),
                                                            p == null ? null : p.getName(), s == null ? null : s.getName()};
                                    })
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
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
                cq.select(cb.array(h, p, s))
                        .where(cb.equal(h.get(Head_.name), cb.parameter(String.class, "name")),
                               cb.or(cb.isNull(p.get(Head_.id)), cb.equal(p.get(Head_.termStart), previous)),
                               cb.or(cb.isNull(s.get(Head_.id)), cb.equal(s.get(Head_.termStart), next)))
                        .orderBy(cb.asc(h.get(Head_.country)), cb.asc(h.get(Head_.termStart)));
                return em.createQuery(cq)
                        .setParameter("name", "Donald Trump")
                        .getResultList();
            });
            assertThat(rows(list.stream()
                                    .map(r -> {
                                        final var h = (Head) r[0];
                                        final var p = (Head) r[1];
                                        final var s = (Head) r[2];
                                        return new Object[]{h.getCountry(), h.getNumber(), h.getTermStart(), h.getTermEnd(),
                                                            p == null ? null : p.getName(), s == null ? null : s.getName()};
                                    })
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
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
                                    SELECT o,
                                           COUNT(h),
                                           SUM(CASE WHEN h.party <> p.party THEN 1 ELSE 0 END),
                                           ROUND(100.0 * SUM(CASE WHEN h.party <> p.party THEN 1 ELSE 0 END) / COUNT(h), 1)
                                    FROM Head AS h
                                         JOIN h.office AS o
                                         JOIN Head AS p ON p.country = h.country
                                    WHERE p.termStart = (SELECT MAX(p2.termStart)
                                                         FROM Head AS p2
                                                         WHERE p2.country = h.country
                                                           AND p2.termStart < h.termStart)
                                      AND h.name <> p.name
                                    GROUP BY o
                                    ORDER BY o.country ASC""",
                            Object[].class
                    )
                    .getResultList());
            assertThat(rows(list.stream()
                                    .map(r -> new Object[]{((Office) r[0]).getCountry(), r[1], r[2], r[3]})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }

        @Test
        void __CriteriaApi() {
            final var list = __Exercises_TestUtils.apply(em -> {
                final var cb = em.getCriteriaBuilder();
                final var cq = cb.createQuery(Object[].class);
                final var h = cq.from(Head.class);
                final var o = h.join(Head_.office);
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
                cq.select(cb.array(o, cb.count(h), changes,
                                   cb.round(cb.quot(cb.prod(cb.literal(100.0d), changes), cb.count(h)), 1)))
                        .where(cb.equal(p.get(Head_.termStart), previous),
                               cb.notEqual(h.get(Head_.name), p.get(Head_.name)))
                        .groupBy(o)
                        .orderBy(cb.asc(o.get(Office_.country)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list.stream()
                                    .map(r -> new Object[]{((Office) r[0]).getCountry(), r[1], r[2], r[3]})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
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
