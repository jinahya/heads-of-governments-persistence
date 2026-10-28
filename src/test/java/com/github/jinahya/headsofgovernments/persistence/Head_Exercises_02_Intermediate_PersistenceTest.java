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
import jakarta.persistence.criteria.Nulls;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.github.jinahya.headsofgovernments.persistence.__Exercises_TestUtils.row;
import static com.github.jinahya.headsofgovernments.persistence.__Exercises_TestUtils.rows;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Answers the exercises of {@code src/test/sql/exercises/head_exercises_02_intermediate.sql}, against {@code
 * db/heads_of_governments.sqlite3}; a nested class per exercise, which answers it with JPQL and with the Criteria
 * API, or with a native query where neither can, and checks that it gives what the SQL of the file gives.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@Slf4j
class Head_Exercises_02_Intermediate_PersistenceTest {

    @DisplayName("HOG_HEAD_I_01: The longest terms of a country")
    @Nested
    class HOG_HEAD_I_01_Test {

        /**
         * The completed terms; the incumbent's, measured to date('now'), is checked against today.
         */
        private static final List<List<Object>> EXPECTED = List.of(
                row(229L, "KR", 12L, "1981-02-25", "1988-02-24", "전두환", "민주정의당", 1931L, 2021L, 2555L),
                row(225L, "KR", 8L, "1972-12-27", "1978-12-26", "박정희", "민주공화당", 1917L, 1979L, 2190L),
                row(230L, "KR", 13L, "1988-02-25", "1993-02-24", "노태우", "민주정의당/민주자유당/무소속", 1932L, 2021L, 1826L),
                row(234L, "KR", 17L, "2008-02-25", "2013-02-24", "이명박", "한나라당/새누리당", 1941L, null, 1826L),
                row(231L, "KR", 14L, "1993-02-25", "1998-02-24", "김영삼", "민주자유당/신한국당/무소속", 1929L, 2015L, 1825L),
                row(232L, "KR", 15L, "1998-02-25", "2003-02-24", "김대중", "새정치국민회의/새천년민주당/무소속", 1924L, 2009L, 1825L),
                row(233L, "KR", 16L, "2003-02-25", "2008-02-24", "노무현", "새천년민주당/무소속/열린우리당/무소속", 1946L, 2009L, 1825L),
                row(236L, "KR", 19L, "2017-05-10", "2022-05-09", "문재인", "더불어민주당", 1953L, null, 1825L),
                row(218L, "KR", 1L, "1948-07-24", "1952-08-14", "이승만", "대한독립촉성국민회/자유당", 1875L, 1965L, 1482L),
                row(235L, "KR", 18L, "2013-02-25", "2017-03-10", "박근혜", "새누리당/자유한국당", 1952L, null, 1474L),
                row(219L, "KR", 2L, "1952-08-15", "1956-08-14", "이승만", "자유당", 1875L, 1965L, 1460L),
                row(223L, "KR", 6L, "1967-07-01", "1971-06-30", "박정희", "민주공화당", 1917L, 1979L, 1460L),
                row(220L, "KR", 3L, "1956-08-15", "1960-04-27", "이승만", "자유당", 1875L, 1965L, 1351L),
                row(222L, "KR", 5L, "1963-12-17", "1967-06-30", "박정희", "민주공화당", 1917L, 1979L, 1291L),
                row(237L, "KR", 20L, "2022-05-10", "2025-04-04", "윤석열", "국민의힘", 1960L, null, 1060L),
                row(221L, "KR", 4L, "1960-08-12", "1962-03-22", "윤보선", "무소속", 1897L, 1990L, 587L),
                row(224L, "KR", 7L, "1971-07-01", "1972-12-26", "박정희", "민주공화당", 1917L, 1979L, 544L),
                row(226L, "KR", 9L, "1978-12-27", "1979-10-26", "박정희", "민주공화당", 1917L, 1979L, 303L),
                row(227L, "KR", 10L, "1979-12-06", "1980-08-16", "최규하", "무소속", 1919L, 2006L, 254L),
                row(228L, "KR", 11L, "1980-08-27", "1981-02-24", "전두환", "무소속/민주정의당", 1931L, 2021L, 181L)
        );

        // JPQL has no difference of two dates, nor today's date as text; julianday() and date('now') are
        // SQLite's own, and the Criteria API has neither
        @Test
        void nativeQuery() {
            final var list = __Exercises_TestUtils.apply(em -> (List<?>) em.createNativeQuery(
                            """
                                    SELECT *,
                                           CAST(julianday(COALESCE(term_end, date ('now'))) - julianday(term_start) AS INTEGER) AS days
                                    FROM head
                                    WHERE country = :country
                                    ORDER BY days DESC, term_start ASC"""
                    )
                    .setParameter("country", "KR")
                    .getResultList());
            // the incumbent's term runs to today, so its length is checked apart
            final var rows = rows(list);
            assertThat(rows).filteredOn(r -> r.get(4) != null).containsExactlyElementsOf(EXPECTED);
            assertThat(rows).filteredOn(r -> r.get(4) == null).singleElement().satisfies(r -> {
                final var today = LocalDate.now(ZoneOffset.UTC);
                assertThat(r.get(9)).isEqualTo(ChronoUnit.DAYS.between(LocalDate.parse((String) r.get(3)), today));
            });
        }
    }

    @DisplayName("HOG_HEAD_I_02: More than one term")
    @Nested
    class HOG_HEAD_I_02_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", "Jules Grévy", 2L, "4, 4"),
                row("FR", "Albert Lebrun", 2L, "15, 15"),
                row("FR", "Charles de Gaulle", 2L, "18, 18"),
                row("FR", "François Mitterrand", 2L, "21, 21"),
                row("FR", "Jacques Chirac", 2L, "22, 22"),
                row("FR", "Emmanuel Macron", 2L, "25, 25"),
                row("GB", "Thomas Pelham-Holles", 2L, "4, 4"),
                row("GB", "Charles Watson-Wentworth", 2L, "8, 8"),
                row("GB", "William Cavendish-Bentinck", 2L, "13, 13"),
                row("GB", "William Pitt the Younger", 2L, "14, 14"),
                row("GB", "Arthur Wellesley", 2L, "21, 21"),
                row("GB", "William Lamb", 2L, "23, 23"),
                row("GB", "Robert Peel", 2L, "24, 24"),
                row("GB", "John Russell", 2L, "25, 25"),
                row("GB", "Edward Smith-Stanley", 3L, "26, 26, 26"),
                row("GB", "Henry John Temple", 2L, "28, 28"),
                row("GB", "Benjamin Disraeli", 2L, "29, 29"),
                row("GB", "William Ewart Gladstone", 4L, "30, 30, 30, 30"),
                row("GB", "Robert Gascoyne-Cecil", 3L, "31, 31, 31"),
                row("GB", "Stanley Baldwin", 3L, "38, 38, 38"),
                row("GB", "Ramsay MacDonald", 2L, "39, 39"),
                row("GB", "Winston Churchill", 2L, "41, 41"),
                row("GB", "Harold Wilson", 2L, "46, 46"),
                row("JP", "伊藤博文", 4L, "1, 5, 7, 10"),
                row("JP", "山県有朋", 2L, "3, 9"),
                row("JP", "松方正義", 2L, "4, 6"),
                row("JP", "大隈重信", 2L, "8, 17"),
                row("JP", "桂太郎", 3L, "11, 13, 15"),
                row("JP", "西園寺公望", 2L, "12, 14"),
                row("JP", "山本権兵衛", 2L, "16, 22"),
                row("JP", "若槻禮次郎", 2L, "25, 28"),
                row("JP", "近衛文麿", 3L, "34, 38, 39"),
                row("JP", "吉田茂", 5L, "45, 48, 49, 50, 51"),
                row("JP", "鳩山一郎", 3L, "52, 53, 54"),
                row("JP", "岸信介", 2L, "56, 57"),
                row("JP", "池田勇人", 3L, "58, 59, 60"),
                row("JP", "佐藤栄作", 3L, "61, 62, 63"),
                row("JP", "田中角栄", 2L, "64, 65"),
                row("JP", "大平正芳", 2L, "68, 69"),
                row("JP", "中曽根康弘", 3L, "71, 72, 73"),
                row("JP", "海部俊樹", 2L, "76, 77"),
                row("JP", "橋本龍太郎", 2L, "82, 83"),
                row("JP", "森喜朗", 2L, "85, 86"),
                row("JP", "小泉純一郎", 3L, "87, 88, 89"),
                row("JP", "安倍晋三", 4L, "90, 96, 97, 98"),
                row("JP", "岸田文雄", 2L, "100, 101"),
                row("JP", "石破茂", 2L, "102, 103"),
                row("JP", "高市早苗", 2L, "104, 105"),
                row("KR", "이승만", 3L, "1, 2, 3"),
                row("KR", "박정희", 5L, "5, 6, 7, 8, 9"),
                row("KR", "전두환", 2L, "11, 12"),
                row("US", "Grover Cleveland", 2L, "22, 24"),
                row("US", "Donald Trump", 2L, "45, 47")
        );

        // JPQL has no string aggregation; GROUP_CONCAT() is SQLite's own, and the Criteria API has none either
        @Test
        void nativeQuery() {
            final var list = __Exercises_TestUtils.apply(em -> (List<?>) em.createNativeQuery(
                            """
                                    SELECT country, name, COUNT(*) AS terms, GROUP_CONCAT(number, ', ' ORDER BY term_start) AS numbers
                                    FROM head
                                    GROUP BY country, name
                                    HAVING COUNT(*) > 1
                                    ORDER BY country ASC, MIN(term_start) ASC"""
                    )
                    .getResultList());
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_HEAD_I_03: Age on taking office")
    @Nested
    class HOG_HEAD_I_03_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", 40L, 79L, 61.3d),
                row("GB", 24L, 83L, 56.3d),
                row("JP", 44L, 77L, 63.1d),
                row("KR", 46L, 81L, 61.2d),
                row("US", 43L, 79L, 56.7d)
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT o,
                                           MIN(CAST(SUBSTRING(h.termStart, 1, 4) AS INTEGER) - h.born),
                                           MAX(CAST(SUBSTRING(h.termStart, 1, 4) AS INTEGER) - h.born),
                                           ROUND(AVG(CAST(SUBSTRING(h.termStart, 1, 4) AS INTEGER) - h.born), 1)
                                    FROM Head AS h
                                         JOIN h.office AS o
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
                final var age = cb.diff(cb.substring(h.get(Head_.termStart), 1, 4).cast(Integer.class), h.get(Head_.born));
                cq.select(cb.array(o, cb.min(age), cb.max(age), cb.round(cb.avg(age), 1)))
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

    @DisplayName("HOG_HEAD_I_04: Terms per party")
    @Nested
    class HOG_HEAD_I_04_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("민주공화당", 5L, 1L),
                row("더불어민주당", 2L, 2L),
                row("무소속", 2L, 2L),
                row("자유당", 2L, 1L),
                row("국민의힘", 1L, 1L),
                row("대한독립촉성국민회/자유당", 1L, 1L),
                row("무소속/민주정의당", 1L, 1L),
                row("민주자유당/신한국당/무소속", 1L, 1L),
                row("민주정의당", 1L, 1L),
                row("민주정의당/민주자유당/무소속", 1L, 1L),
                row("새누리당/자유한국당", 1L, 1L),
                row("새정치국민회의/새천년민주당/무소속", 1L, 1L),
                row("새천년민주당/무소속/열린우리당/무소속", 1L, 1L),
                row("한나라당/새누리당", 1L, 1L)
        );

        // selects no entity: a row is a party, which no entity maps
        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT h.party, COUNT(h) AS terms, COUNT(DISTINCT h.name)
                                    FROM Head AS h
                                    WHERE h.country = :country
                                    GROUP BY h.party
                                    ORDER BY terms DESC, h.party ASC""",
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
                final var h = cq.from(Head.class);
                final var terms = cb.count(h);
                cq.select(cb.array(h.get(Head_.party), terms, cb.countDistinct(h.get(Head_.name))))
                        .where(cb.equal(h.get(Head_.country), cb.parameter(String.class, "country")))
                        .groupBy(h.get(Head_.party))
                        .orderBy(cb.desc(terms), cb.asc(h.get(Head_.party)));
                return em.createQuery(cq)
                        .setParameter("country", "KR")
                        .getResultList();
            });
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_HEAD_I_05: Living former heads")
    @Nested
    class HOG_HEAD_I_05_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", "François Hollande", 1954L, "2017-05-14"),
                row("FR", "Nicolas Sarkozy", 1955L, "2012-05-15"),
                row("GB", "Keir Starmer", 1962L, "2026-07-20"),
                row("GB", "Rishi Sunak", 1980L, "2024-07-05"),
                row("GB", "Liz Truss", 1975L, "2022-10-25"),
                row("GB", "Boris Johnson", 1964L, "2022-09-06"),
                row("GB", "Theresa May", 1956L, "2019-07-24"),
                row("GB", "David Cameron", 1966L, "2016-07-13"),
                row("GB", "Gordon Brown", 1951L, "2010-05-11"),
                row("GB", "Tony Blair", 1953L, "2007-06-27"),
                row("GB", "John Major", 1943L, "1997-05-02"),
                row("JP", "石破茂", 1957L, "2025-10-21"),
                row("JP", "岸田文雄", 1957L, "2024-10-01"),
                row("JP", "菅義偉", 1948L, "2021-10-04"),
                row("JP", "野田佳彦", 1957L, "2012-12-26"),
                row("JP", "菅直人", 1946L, "2011-09-02"),
                row("JP", "鳩山由紀夫", 1947L, "2010-06-08"),
                row("JP", "麻生太郎", 1940L, "2009-09-16"),
                row("JP", "福田康夫", 1936L, "2008-09-24"),
                row("JP", "小泉純一郎", 1942L, "2006-09-26"),
                row("JP", "森喜朗", 1937L, "2001-04-26"),
                row("JP", "細川護熙", 1938L, "1994-04-28"),
                row("KR", "윤석열", 1960L, "2025-04-04"),
                row("KR", "문재인", 1953L, "2022-05-09"),
                row("KR", "박근혜", 1952L, "2017-03-10"),
                row("KR", "이명박", 1941L, "2013-02-24"),
                row("US", "Joe Biden", 1942L, "2025-01-20"),
                row("US", "Barack Obama", 1961L, "2017-01-20"),
                row("US", "George W. Bush", 1946L, "2009-01-20"),
                row("US", "Bill Clinton", 1946L, "2001-01-20")
        );

        // selects no entity: a row is a person, a group of the terms of one, not a head
        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT h.country, h.name, h.born, MAX(h.termEnd) AS lastTermEnd
                                    FROM Head AS h
                                    WHERE h.died IS NULL
                                    GROUP BY h.country, h.name, h.born
                                    HAVING COUNT(h) = COUNT(h.termEnd)
                                    ORDER BY h.country ASC, lastTermEnd DESC NULLS LAST""",
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
                final var lastTermEnd = cb.greatest(h.get(Head_.termEnd));
                cq.select(cb.array(h.get(Head_.country), h.get(Head_.name), h.get(Head_.born), lastTermEnd))
                        .where(cb.isNull(h.get(Head_.died)))
                        .groupBy(h.get(Head_.country), h.get(Head_.name), h.get(Head_.born))
                        .having(cb.equal(cb.count(h), cb.count(h.get(Head_.termEnd))))
                        .orderBy(cb.asc(h.get(Head_.country)), cb.desc(lastTermEnd, Nulls.LAST));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_HEAD_I_06: Terms begun per decade")
    @Nested
    class HOG_HEAD_I_06_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("1720s", 1L, "GB"),
                row("1740s", 2L, "GB"),
                row("1750s", 3L, "GB"),
                row("1760s", 5L, "GB"),
                row("1770s", 1L, "GB"),
                row("1780s", 5L, "GB,US"),
                row("1790s", 1L, "US"),
                row("1800s", 7L, "GB,US"),
                row("1810s", 2L, "GB,US"),
                row("1820s", 5L, "GB,US"),
                row("1830s", 6L, "GB,US"),
                row("1840s", 7L, "FR,GB,US"),
                row("1850s", 8L, "GB,US"),
                row("1860s", 7L, "GB,US"),
                row("1870s", 5L, "FR,GB,US"),
                row("1880s", 13L, "FR,GB,JP,US"),
                row("1890s", 14L, "FR,GB,JP,US"),
                row("1900s", 10L, "FR,GB,JP,US"),
                row("1910s", 9L, "FR,GB,JP,US"),
                row("1920s", 19L, "FR,GB,JP,US"),
                row("1930s", 15L, "FR,GB,JP,US"),
                row("1940s", 18L, "FR,GB,JP,KR,US"),
                row("1950s", 16L, "FR,GB,JP,KR,US"),
                row("1960s", 15L, "FR,GB,JP,KR,US"),
                row("1970s", 18L, "FR,GB,JP,KR,US"),
                row("1980s", 14L, "FR,JP,KR,US"),
                row("1990s", 14L, "FR,GB,JP,KR,US"),
                row("2000s", 16L, "FR,GB,JP,KR,US"),
                row("2010s", 13L, "FR,GB,JP,KR,US"),
                row("2020s", 16L, "FR,GB,JP,KR,US")
        );

        // JPQL has no string aggregation; GROUP_CONCAT(DISTINCT ...) is SQLite's own, and the Criteria API has
        // none either
        @Test
        void nativeQuery() {
            final var list = __Exercises_TestUtils.apply(em -> (List<?>) em.createNativeQuery(
                            """
                                    SELECT SUBSTR(term_start, 1, 3) || '0s' AS decade,
                                           COUNT(*)                         AS terms,
                                           GROUP_CONCAT(DISTINCT country)   AS countries
                                    FROM head
                                    GROUP BY decade
                                    ORDER BY decade ASC"""
                    )
                    .getResultList());
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_HEAD_I_07: The latest terms of each country")
    @Nested
    class HOG_HEAD_I_07_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", 25L, "Emmanuel Macron", "2017-05-14", "2022-05-14"),
                row("FR", 25L, "Emmanuel Macron", "2022-05-14", null),
                row("GB", 58L, "Keir Starmer", "2024-07-05", "2026-07-20"),
                row("GB", 59L, "Andy Burnham", "2026-07-20", null),
                row("JP", 104L, "高市早苗", "2025-10-21", "2026-02-18"),
                row("JP", 105L, "高市早苗", "2026-02-18", null),
                row("KR", 20L, "윤석열", "2022-05-10", "2025-04-04"),
                row("KR", 21L, "이재명", "2025-06-04", null),
                row("US", 46L, "Joe Biden", "2021-01-20", "2025-01-20"),
                row("US", 47L, "Donald Trump", "2025-01-20", null)
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT h
                                    FROM Head AS h
                                    WHERE (SELECT COUNT(l)
                                           FROM Head AS l
                                           WHERE l.country = h.country
                                             AND l.termStart > h.termStart) < :n
                                    ORDER BY h.country ASC, h.termStart ASC""",
                            Head.class
                    )
                    .setParameter("n", 2L)
                    .getResultList());
            assertThat(rows(list.stream()
                                    .map(h -> new Object[]{h.getCountry(), h.getNumber(), h.getName(), h.getTermStart(), h.getTermEnd()})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }

        @Test
        void __CriteriaApi() {
            final var list = __Exercises_TestUtils.apply(em -> {
                final var cb = em.getCriteriaBuilder();
                final var cq = cb.createQuery(Head.class);
                final var h = cq.from(Head.class);
                final var later = cq.subquery(Long.class);
                final var l = later.from(Head.class);
                later.select(cb.count(l))
                        .where(cb.equal(l.get(Head_.country), h.get(Head_.country)),
                               cb.greaterThan(l.get(Head_.termStart), h.get(Head_.termStart)));
                cq.select(h)
                        .where(cb.lessThan(later, cb.parameter(Long.class, "n")))
                        .orderBy(cb.asc(h.get(Head_.country)), cb.asc(h.get(Head_.termStart)));
                return em.createQuery(cq)
                        .setParameter("n", 2L)
                        .getResultList();
            });
            assertThat(rows(list.stream()
                                    .map(h -> new Object[]{h.getCountry(), h.getNumber(), h.getName(), h.getTermStart(), h.getTermEnd()})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_HEAD_I_08: The first and the latest terms of each country")
    @Nested
    class HOG_HEAD_I_08_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", "first", 1L, "Louis-Napoléon Bonaparte", "1848-12-20", "1852-12-02"),
                row("FR", "first", 2L, "Adolphe Thiers", "1871-08-31", "1873-05-24"),
                row("FR", "latest", 25L, "Emmanuel Macron", "2017-05-14", "2022-05-14"),
                row("FR", "latest", 25L, "Emmanuel Macron", "2022-05-14", null),
                row("GB", "first", 1L, "Robert Walpole", "1721-04-03", "1742-02-11"),
                row("GB", "first", 2L, "Spencer Compton", "1742-02-16", "1743-07-02"),
                row("GB", "latest", 58L, "Keir Starmer", "2024-07-05", "2026-07-20"),
                row("GB", "latest", 59L, "Andy Burnham", "2026-07-20", null),
                row("JP", "first", 1L, "伊藤博文", "1885-12-22", "1888-04-30"),
                row("JP", "first", 2L, "黒田清隆", "1888-04-30", "1889-10-25"),
                row("JP", "latest", 104L, "高市早苗", "2025-10-21", "2026-02-18"),
                row("JP", "latest", 105L, "高市早苗", "2026-02-18", null),
                row("KR", "first", 1L, "이승만", "1948-07-24", "1952-08-14"),
                row("KR", "first", 2L, "이승만", "1952-08-15", "1956-08-14"),
                row("KR", "latest", 20L, "윤석열", "2022-05-10", "2025-04-04"),
                row("KR", "latest", 21L, "이재명", "2025-06-04", null),
                row("US", "first", 1L, "George Washington", "1789-04-30", "1797-03-04"),
                row("US", "first", 2L, "John Adams", "1797-03-04", "1801-03-04"),
                row("US", "latest", 46L, "Joe Biden", "2021-01-20", "2025-01-20"),
                row("US", "latest", 47L, "Donald Trump", "2025-01-20", null)
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT h, CASE WHEN COUNT(e) < :n THEN 'first' ELSE 'latest' END
                                    FROM Head AS h
                                         LEFT JOIN Head AS e ON e.country = h.country AND e.termStart < h.termStart
                                    GROUP BY h
                                    HAVING COUNT(e) < :n
                                        OR (SELECT COUNT(l)
                                            FROM Head AS l
                                            WHERE l.country = h.country
                                              AND l.termStart > h.termStart) < :n
                                    ORDER BY h.country ASC, h.termStart ASC""",
                            Object[].class
                    )
                    .setParameter("n", 2L)
                    .getResultList());
            assertThat(rows(list.stream()
                                    .map(r -> {
                                        final var h = (Head) r[0];
                                        return new Object[]{h.getCountry(), r[1], h.getNumber(), h.getName(), h.getTermStart(),
                                                            h.getTermEnd()};
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
                final var n = cb.parameter(Long.class, "n");
                final var e = h.join(Head.class, JoinType.LEFT);
                e.on(cb.equal(e.get(Head_.country), h.get(Head_.country)),
                     cb.lessThan(e.get(Head_.termStart), h.get(Head_.termStart)));
                final var earlier = cb.count(e);
                final var later = cq.subquery(Long.class);
                final var l = later.from(Head.class);
                later.select(cb.count(l))
                        .where(cb.equal(l.get(Head_.country), h.get(Head_.country)),
                               cb.greaterThan(l.get(Head_.termStart), h.get(Head_.termStart)));
                cq.select(cb.array(h, cb.<String>selectCase().when(cb.lessThan(earlier, n), "first").otherwise("latest")))
                        .groupBy(h)
                        .having(cb.or(cb.lessThan(earlier, n), cb.lessThan(later, n)))
                        .orderBy(cb.asc(h.get(Head_.country)), cb.asc(h.get(Head_.termStart)));
                return em.createQuery(cq)
                        .setParameter("n", 2L)
                        .getResultList();
            });
            assertThat(rows(list.stream()
                                    .map(r -> {
                                        final var h = (Head) r[0];
                                        return new Object[]{h.getCountry(), r[1], h.getNumber(), h.getName(), h.getTermStart(),
                                                            h.getTermEnd()};
                                    })
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_HEAD_I_09: The first two terms of each country")
    @Nested
    class HOG_HEAD_I_09_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", "Louis-Napoléon Bonaparte", "1848-12-20", "1852-12-02"),
                row("FR", "Adolphe Thiers", "1871-08-31", "1873-05-24"),
                row("GB", "Robert Walpole", "1721-04-03", "1742-02-11"),
                row("GB", "Spencer Compton", "1742-02-16", "1743-07-02"),
                row("JP", "伊藤博文", "1885-12-22", "1888-04-30"),
                row("JP", "黒田清隆", "1888-04-30", "1889-10-25"),
                row("KR", "이승만", "1948-07-24", "1952-08-14"),
                row("KR", "이승만", "1952-08-15", "1956-08-14"),
                row("US", "George Washington", "1789-04-30", "1797-03-04"),
                row("US", "John Adams", "1797-03-04", "1801-03-04")
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT h
                                    FROM Head AS h
                                    WHERE (SELECT COUNT(e)
                                           FROM Head AS e
                                           WHERE e.country = h.country
                                             AND e.termStart < h.termStart) < 2
                                    ORDER BY h.country ASC, h.termStart ASC""",
                            Head.class
                    )
                    .getResultList());
            assertThat(rows(list.stream()
                                    .map(h -> new Object[]{h.getCountry(), h.getName(), h.getTermStart(), h.getTermEnd()})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }

        @Test
        void __CriteriaApi() {
            final var list = __Exercises_TestUtils.apply(em -> {
                final var cb = em.getCriteriaBuilder();
                final var cq = cb.createQuery(Head.class);
                final var h = cq.from(Head.class);
                final var earlier = cq.subquery(Long.class);
                final var e = earlier.from(Head.class);
                earlier.select(cb.count(e))
                        .where(cb.equal(e.get(Head_.country), h.get(Head_.country)),
                               cb.lessThan(e.get(Head_.termStart), h.get(Head_.termStart)));
                cq.select(h)
                        .where(cb.lt(earlier, 2))
                        .orderBy(cb.asc(h.get(Head_.country)), cb.asc(h.get(Head_.termStart)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list.stream()
                                    .map(h -> new Object[]{h.getCountry(), h.getName(), h.getTermStart(), h.getTermEnd()})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_HEAD_I_10: Two or more terms, the top three per country")
    @Nested
    class HOG_HEAD_I_10_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", "Jules Grévy", 2L),
                row("FR", "Albert Lebrun", 2L),
                row("FR", "Charles de Gaulle", 2L),
                row("GB", "William Ewart Gladstone", 4L),
                row("GB", "Edward Smith-Stanley", 3L),
                row("GB", "Robert Gascoyne-Cecil", 3L),
                row("JP", "吉田茂", 5L),
                row("JP", "伊藤博文", 4L),
                row("JP", "安倍晋三", 4L),
                row("KR", "박정희", 5L),
                row("KR", "이승만", 3L),
                row("KR", "전두환", 2L),
                row("US", "Grover Cleveland", 2L),
                row("US", "Donald Trump", 2L)
        );

        // selects no entity: a row is a person, a group of the terms of one, not a head
        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT h.country, h.name, COUNT(h)
                                    FROM Head AS h
                                    GROUP BY h.country, h.name
                                    HAVING COUNT(h) >= 2
                                       AND (SELECT COUNT(DISTINCT o.name)
                                            FROM Head AS o
                                            WHERE o.country = h.country
                                              AND ((SELECT COUNT(ot) FROM Head AS ot WHERE ot.country = o.country AND ot.name = o.name)
                                                       > (SELECT COUNT(ht) FROM Head AS ht WHERE ht.country = h.country AND ht.name = h.name)
                                                   OR ((SELECT COUNT(ot) FROM Head AS ot WHERE ot.country = o.country AND ot.name = o.name)
                                                           = (SELECT COUNT(ht) FROM Head AS ht WHERE ht.country = h.country AND ht.name = h.name)
                                                       AND (SELECT MIN(ot.termStart) FROM Head AS ot WHERE ot.country = o.country AND ot.name = o.name)
                                                               < (SELECT MIN(ht.termStart) FROM Head AS ht
                                                                  WHERE ht.country = h.country AND ht.name = h.name)))) < 3
                                    ORDER BY h.country ASC, COUNT(h) DESC, MIN(h.termStart) ASC""",
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
                final var country = h.get(Head_.country);
                final var name = h.get(Head_.name);
                // the persons of the country who place before this one: more terms, or as many and an earlier first term
                final var before = cq.subquery(Long.class);
                final var o = before.from(Head.class);
                final var oTerms = before.subquery(Long.class);
                final var ot = oTerms.from(Head.class);
                oTerms.select(cb.count(ot))
                        .where(cb.equal(ot.get(Head_.country), o.get(Head_.country)),
                               cb.equal(ot.get(Head_.name), o.get(Head_.name)));
                final var oFirst = before.subquery(String.class);
                final var of = oFirst.from(Head.class);
                oFirst.select(cb.least(of.get(Head_.termStart)))
                        .where(cb.equal(of.get(Head_.country), o.get(Head_.country)),
                               cb.equal(of.get(Head_.name), o.get(Head_.name)));
                final var hTerms = before.subquery(Long.class);
                final var ht = hTerms.from(Head.class);
                hTerms.select(cb.count(ht))
                        .where(cb.equal(ht.get(Head_.country), country), cb.equal(ht.get(Head_.name), name));
                final var hFirst = before.subquery(String.class);
                final var hf = hFirst.from(Head.class);
                hFirst.select(cb.least(hf.get(Head_.termStart)))
                        .where(cb.equal(hf.get(Head_.country), country), cb.equal(hf.get(Head_.name), name));
                before.select(cb.countDistinct(o.get(Head_.name)))
                        .where(cb.equal(o.get(Head_.country), country),
                               cb.or(cb.greaterThan(oTerms, hTerms),
                                     cb.and(cb.equal(oTerms, hTerms), cb.lessThan(oFirst, hFirst))));
                cq.select(cb.array(country, name, cb.count(h)))
                        .groupBy(country, name)
                        .having(cb.ge(cb.count(h), 2), cb.lt(before, 3))
                        .orderBy(cb.asc(country), cb.desc(cb.count(h)), cb.asc(cb.least(h.get(Head_.termStart))));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_HEAD_I_11: Party share of completed terms, the top three per country")
    @Nested
    class HOG_HEAD_I_11_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", "Alliance_républicaine_démocratique", 14.6d),
                row("FR", "PS", 12.8d),
                row("FR", "Républicain_opportuniste", 10.8d),
                row("GB", "Conservative", 32.1d),
                row("GB", "Whig", 22.8d),
                row("GB", "Tory", 19.3d),
                row("JP", "自由民主党", 46.1d),
                row("JP", "無所属", 24.6d),
                row("JP", "立憲政友会", 8.5d),
                row("KR", "민주공화당", 21.3d),
                row("KR", "자유당", 10.4d),
                row("KR", "민주정의당", 9.4d),
                row("US", "Democratic", 39.0d),
                row("US", "Republican", 37.3d),
                row("US", "Democratic-Republican", 10.2d)
        );

        // JPQL has no difference of two dates, nor window functions; julianday() is SQLite's own, and
        // SUM() OVER and ROW_NUMBER() OVER (PARTITION BY ...) are SQL's; the Criteria API has none of them
        @Test
        void nativeQuery() {
            final var list = __Exercises_TestUtils.apply(em -> (List<?>) em.createNativeQuery(
                            """
                                    SELECT country, party, tenure_share
                                    FROM (SELECT country,
                                                 party,
                                                 ROUND(100.0 * SUM(julianday(term_end) - julianday(term_start))
                                                           / SUM(SUM(julianday(term_end) - julianday(term_start))) OVER (PARTITION BY country),
                                                       1) AS  tenure_share,
                                                 ROW_NUMBER() OVER (PARTITION BY country
                                                     ORDER BY SUM(julianday(term_end) - julianday(term_start)) DESC)             AS place
                                          FROM head
                                          WHERE term_end IS NOT NULL
                                          GROUP BY country, party)
                                    WHERE place <= 3
                                    ORDER BY country ASC, tenure_share DESC"""
                    )
                    .getResultList());
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_HEAD_I_12: Terms that ended in the year the head died")
    @Nested
    class HOG_HEAD_I_12_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", 4L),
                row("GB", 11L),
                row("JP", 9L),
                row("KR", 1L),
                row("US", 9L)
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT o, COUNT(h)
                                    FROM Head AS h
                                         JOIN h.office AS o
                                    WHERE h.died = CAST(SUBSTRING(h.termEnd, 1, 4) AS INTEGER)
                                    GROUP BY o
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
                final var h = cq.from(Head.class);
                final var o = h.join(Head_.office);
                cq.select(cb.array(o, cb.count(h)))
                        .where(cb.equal(h.get(Head_.died), cb.substring(h.get(Head_.termEnd), 1, 4).cast(Integer.class)))
                        .groupBy(o)
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

    @DisplayName("HOG_HEAD_I_13: Terms begun per day of the week")
    @Nested
    class HOG_HEAD_I_13_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("Sunday", 18L),
                row("Monday", 35L),
                row("Tuesday", 49L),
                row("Wednesday", 59L),
                row("Thursday", 46L),
                row("Friday", 43L),
                row("Saturday", 35L)
        );

        // JPQL's EXTRACT has no day of the week, and term_start is text; strftime('%w', ...) is SQLite's own,
        // and the Criteria API's extract has no day of the week either
        @Test
        void nativeQuery() {
            final var list = __Exercises_TestUtils.apply(em -> (List<?>) em.createNativeQuery(
                            """
                                    SELECT CASE strftime('%w', term_start)
                                               WHEN '0' THEN 'Sunday'
                                               WHEN '1' THEN 'Monday'
                                               WHEN '2' THEN 'Tuesday'
                                               WHEN '3' THEN 'Wednesday'
                                               WHEN '4' THEN 'Thursday'
                                               WHEN '5' THEN 'Friday'
                                               ELSE 'Saturday'
                                               END  AS weekday,
                                           COUNT(*) AS terms
                                    FROM head
                                    GROUP BY strftime('%w', term_start)
                                    ORDER BY strftime('%w', term_start) ASC"""
                    )
                    .getResultList());
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_HEAD_I_14: Years lived after leaving office")
    @Nested
    class HOG_HEAD_I_14_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("JP", "東久邇宮稔彦王", "1945-10-09", 1990L, 45L),
                row("US", "Jimmy Carter", "1981-01-20", 2024L, 43L),
                row("GB", "Augustus FitzRoy", "1770-01-28", 1811L, 41L),
                row("GB", "Henry Addington", "1804-05-10", 1844L, 40L),
                row("FR", "Valéry Giscard d'Estaing", "1981-05-21", 2020L, 39L),
                row("GB", "Archibald Primrose", "1895-06-22", 1929L, 34L),
                row("KR", "전두환", "1988-02-24", 2021L, 33L),
                row("JP", "中曽根康弘", "1987-11-06", 2019L, 32L),
                row("GB", "Alec Douglas-Home", "1964-10-16", 1995L, 31L),
                row("GB", "Edward Heath", "1974-03-04", 2005L, 31L)
        );

        // selects no entity: a row is a person, a group of the terms of one, not a head
        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT h.country,
                                           h.name,
                                           MAX(h.termEnd),
                                           h.died,
                                           h.died - CAST(SUBSTRING(MAX(h.termEnd), 1, 4) AS INTEGER) AS yearsAfter
                                    FROM Head AS h
                                    WHERE h.died IS NOT NULL
                                    GROUP BY h.country, h.name, h.died
                                    ORDER BY yearsAfter DESC NULLS LAST, h.country ASC, h.name ASC""",
                            Object[].class
                    )
                    .setMaxResults(10)
                    .getResultList());
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }

        @Test
        void __CriteriaApi() {
            final var list = __Exercises_TestUtils.apply(em -> {
                final var cb = em.getCriteriaBuilder();
                final var cq = cb.createQuery(Object[].class);
                final var h = cq.from(Head.class);
                final var lastTermEnd = cb.greatest(h.get(Head_.termEnd));
                final var yearsAfter = cb.diff(h.get(Head_.died), cb.substring(lastTermEnd, 1, 4).cast(Integer.class));
                cq.select(cb.array(h.get(Head_.country), h.get(Head_.name), lastTermEnd, h.get(Head_.died), yearsAfter))
                        .where(cb.isNotNull(h.get(Head_.died)))
                        .groupBy(h.get(Head_.country), h.get(Head_.name), h.get(Head_.died))
                        .orderBy(cb.desc(yearsAfter, Nulls.LAST), cb.asc(h.get(Head_.country)), cb.asc(h.get(Head_.name)));
                return em.createQuery(cq)
                        .setMaxResults(10)
                        .getResultList();
            });
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }
}
