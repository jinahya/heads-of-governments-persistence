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
 * Answers the exercises of {@code src/test/sql/exercises/head_exercises_01_basic.sql}, against
 * {@code heads_of_governments.sqlite3}; a nested class per exercise, which answers it with JPQL
 * and with the Criteria API, or with a native query where neither can, and checks that it gives
 * what the SQL of the file gives.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@Slf4j
class Head_Exercises_01_Basic_PersistenceTest {

    @DisplayName("HOG_HEAD_B_01: Who held office on a day")
    @Nested
    class HOG_HEAD_B_01_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", 18L, "Charles de Gaulle", "UNR", "1959-01-08", "1966-01-08"),
                row("GB", 44L, "Harold Macmillan", "Conservative", "1957-01-10", "1963-10-18"),
                row("JP", 57L, "岸信介", "自由民主党", "1958-06-12", "1960-07-19"),
                row("KR", 3L, "이승만", "자유당", "1956-08-15", "1960-04-27"),
                row("US", 34L, "Dwight D. Eisenhower", "Republican", "1953-01-20", "1961-01-20")
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT h
                                    FROM Head AS h
                                    WHERE h.termStart <= :dateText
                                      AND (h.termEnd IS NULL OR h.termEnd >= :date)
                                    ORDER BY h.country ASC""",
                            Head.class
                    )
                    .setParameter("date", LocalDate.of(1960, 1, 1))
                    .setParameter("dateText", "1960-01-01")
                    .getResultList());
            assertThat(rows(list.stream()
                                    .map(h -> new Object[]{h.getOffice().getCountry(),
                                            h.getNumber(), h.getName(), h.getParty(),
                                            h.getTermStart(),
                                            h.getTermEnd()})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }

        @Test
        void __CriteriaApi() {
            final var list = __Exercises_TestUtils.apply(em -> {
                final var cb = em.getCriteriaBuilder();
                final var cq = cb.createQuery(Head.class);
                final var h = cq.from(Head.class);
                final var date = cb.parameter(LocalDate.class, "date");
                final var dateText = cb.parameter(String.class, "dateText");
                cq.select(h)
                        .where(cb.lessThanOrEqualTo(h.get(Head_.termStart), dateText),
                               cb.or(cb.isNull(h.get(Head_.termEnd)),
                                     cb.greaterThanOrEqualTo(h.get(Head_.termEnd), date)))
                        .orderBy(cb.asc(h.get(Head_.country)));
                return em.createQuery(cq)
                        .setParameter("date", LocalDate.of(1960, 1, 1))
                        .setParameter("dateText", "1960-01-01")
                        .getResultList();
            });
            assertThat(rows(list.stream()
                                    .map(h -> new Object[]{h.getOffice().getCountry(),
                                            h.getNumber(), h.getName(), h.getParty(),
                                            h.getTermStart(),
                                            h.getTermEnd()})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_HEAD_B_02: One country's list")
    @Nested
    class HOG_HEAD_B_02_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row(218L, "KR", 1L, "1948-07-24", "1952-08-14", "이승만", "대한독립촉성국민회/자유당", 1875L,
                    1965L),
                row(219L, "KR", 2L, "1952-08-15", "1956-08-14", "이승만", "자유당", 1875L, 1965L),
                row(220L, "KR", 3L, "1956-08-15", "1960-04-27", "이승만", "자유당", 1875L, 1965L),
                row(221L, "KR", 4L, "1960-08-12", "1962-03-22", "윤보선", "무소속", 1897L, 1990L),
                row(222L, "KR", 5L, "1963-12-17", "1967-06-30", "박정희", "민주공화당", 1917L, 1979L),
                row(223L, "KR", 6L, "1967-07-01", "1971-06-30", "박정희", "민주공화당", 1917L, 1979L),
                row(224L, "KR", 7L, "1971-07-01", "1972-12-26", "박정희", "민주공화당", 1917L, 1979L),
                row(225L, "KR", 8L, "1972-12-27", "1978-12-26", "박정희", "민주공화당", 1917L, 1979L),
                row(226L, "KR", 9L, "1978-12-27", "1979-10-26", "박정희", "민주공화당", 1917L, 1979L),
                row(227L, "KR", 10L, "1979-12-06", "1980-08-16", "최규하", "무소속", 1919L, 2006L),
                row(228L, "KR", 11L, "1980-08-27", "1981-02-24", "전두환", "무소속/민주정의당", 1931L, 2021L),
                row(229L, "KR", 12L, "1981-02-25", "1988-02-24", "전두환", "민주정의당", 1931L, 2021L),
                row(230L, "KR", 13L, "1988-02-25", "1993-02-24", "노태우", "민주정의당/민주자유당/무소속", 1932L,
                    2021L),
                row(231L, "KR", 14L, "1993-02-25", "1998-02-24", "김영삼", "민주자유당/신한국당/무소속", 1929L,
                    2015L),
                row(232L, "KR", 15L, "1998-02-25", "2003-02-24", "김대중", "새정치국민회의/새천년민주당/무소속", 1924L,
                    2009L),
                row(233L, "KR", 16L, "2003-02-25", "2008-02-24", "노무현", "새천년민주당/무소속/열린우리당/무소속",
                    1946L, 2009L),
                row(234L, "KR", 17L, "2008-02-25", "2013-02-24", "이명박", "한나라당/새누리당", 1941L, null),
                row(235L, "KR", 18L, "2013-02-25", "2017-03-10", "박근혜", "새누리당/자유한국당", 1952L, null),
                row(236L, "KR", 19L, "2017-05-10", "2022-05-09", "문재인", "더불어민주당", 1953L, null),
                row(237L, "KR", 20L, "2022-05-10", "2025-04-04", "윤석열", "국민의힘", 1960L, null),
                row(238L, "KR", 21L, "2025-06-04", null, "이재명", "더불어민주당", 1963L, null)
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT h
                                    FROM Head AS h
                                    WHERE h.country = :country
                                    ORDER BY h.termStart ASC""",
                            Head.class
                    )
                    .setParameter("country", "KR")
                    .getResultList());
            assertThat(rows(list.stream()
                                    .map(h -> new Object[]{h.getId(), h.getOffice().getCountry(),
                                            h.getNumber(), h.getTermStart(),
                                            h.getTermEnd(), h.getName(), h.getParty(), h.getBorn(),
                                            h.getDied()})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }

        @Test
        void __CriteriaApi() {
            final var list = __Exercises_TestUtils.apply(em -> {
                final var cb = em.getCriteriaBuilder();
                final var cq = cb.createQuery(Head.class);
                final var h = cq.from(Head.class);
                cq.select(h)
                        .where(cb.equal(h.get(Head_.country),
                                        cb.parameter(String.class, "country")))
                        .orderBy(cb.asc(h.get(Head_.termStart)));
                return em.createQuery(cq)
                        .setParameter("country", "KR")
                        .getResultList();
            });
            assertThat(rows(list.stream()
                                    .map(h -> new Object[]{h.getId(), h.getOffice().getCountry(),
                                            h.getNumber(), h.getTermStart(),
                                            h.getTermEnd(), h.getName(), h.getParty(), h.getBorn(),
                                            h.getDied()})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_HEAD_B_03: The incumbents")
    @Nested
    class HOG_HEAD_B_03_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", 25L, "Emmanuel Macron", "LREM/Renaissance", "2022-05-14"),
                row("GB", 59L, "Andy Burnham", "Labour", "2026-07-20"),
                row("JP", 105L, "高市早苗", "自由民主党", "2026-02-18"),
                row("KR", 21L, "이재명", "더불어민주당", "2025-06-04"),
                row("US", 47L, "Donald Trump", "Republican", "2025-01-20")
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT h
                                    FROM Head AS h
                                    WHERE h.termEnd IS NULL
                                    ORDER BY h.country ASC""",
                            Head.class
                    )
                    .getResultList());
            assertThat(rows(list.stream()
                                    .map(h -> new Object[]{h.getOffice().getCountry(),
                                            h.getNumber(), h.getName(), h.getParty(),
                                            h.getTermStart()})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }

        @Test
        void __CriteriaApi() {
            final var list = __Exercises_TestUtils.apply(em -> {
                final var cb = em.getCriteriaBuilder();
                final var cq = cb.createQuery(Head.class);
                final var h = cq.from(Head.class);
                cq.select(h)
                        .where(cb.isNull(h.get(Head_.termEnd)))
                        .orderBy(cb.asc(h.get(Head_.country)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list.stream()
                                    .map(h -> new Object[]{h.getOffice().getCountry(),
                                            h.getNumber(), h.getName(), h.getParty(),
                                            h.getTermStart()})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_HEAD_B_04: Terms by name")
    @Nested
    class HOG_HEAD_B_04_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("GB", 41L, "Winston Churchill", "1940-05-10", "1945-07-26"),
                row("GB", 41L, "Winston Churchill", "1951-10-26", "1955-04-05")
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT h
                                    FROM Head AS h
                                    WHERE h.name LIKE CONCAT('%', :name, '%')
                                    ORDER BY h.country ASC, h.termStart ASC""",
                            Head.class
                    )
                    .setParameter("name", "churchill")
                    .getResultList());
            assertThat(rows(list.stream()
                                    .map(h -> new Object[]{h.getOffice().getCountry(),
                                            h.getNumber(), h.getName(), h.getTermStart(),
                                            h.getTermEnd()})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }

        @Test
        void __CriteriaApi() {
            final var list = __Exercises_TestUtils.apply(em -> {
                final var cb = em.getCriteriaBuilder();
                final var cq = cb.createQuery(Head.class);
                final var h = cq.from(Head.class);
                cq.select(h)
                        .where(cb.like(h.get(Head_.name),
                                       cb.concat(List.of(cb.literal("%"),
                                                         cb.parameter(String.class, "name"),
                                                         cb.literal("%")))))
                        .orderBy(cb.asc(h.get(Head_.country)), cb.asc(h.get(Head_.termStart)));
                return em.createQuery(cq)
                        .setParameter("name", "churchill")
                        .getResultList();
            });
            assertThat(rows(list.stream()
                                    .map(h -> new Object[]{h.getOffice().getCountry(),
                                            h.getNumber(), h.getName(), h.getTermStart(),
                                            h.getTermEnd()})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_HEAD_B_05: Terms that began in a year")
    @Nested
    class HOG_HEAD_B_05_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("1945-04-07", "JP", 42L, "鈴木貫太郎"),
                row("1945-04-12", "US", 33L, "Harry S. Truman"),
                row("1945-07-26", "GB", 42L, "Clement Attlee"),
                row("1945-08-17", "JP", 43L, "東久邇宮稔彦王"),
                row("1945-10-09", "JP", 44L, "幣原喜重郎")
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT h
                                    FROM Head AS h
                                    WHERE h.termStart BETWEEN CONCAT(:year, '-01-01') AND CONCAT(:year, '-12-31')
                                    ORDER BY h.termStart ASC, h.country ASC""",
                            Head.class
                    )
                    .setParameter("year", "1945")
                    .getResultList());
            assertThat(rows(list.stream()
                                    .map(h -> new Object[]{h.getTermStart(),
                                            h.getOffice().getCountry(), h.getNumber(), h.getName()})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }

        @Test
        void __CriteriaApi() {
            final var list = __Exercises_TestUtils.apply(em -> {
                final var cb = em.getCriteriaBuilder();
                final var cq = cb.createQuery(Head.class);
                final var h = cq.from(Head.class);
                final var year = cb.parameter(String.class, "year");
                cq.select(h)
                        .where(cb.between(h.get(Head_.termStart),
                                          cb.concat(year, "-01-01"), cb.concat(year, "-12-31")))
                        .orderBy(cb.asc(h.get(Head_.termStart)), cb.asc(h.get(Head_.country)));
                return em.createQuery(cq)
                        .setParameter("year", "1945")
                        .getResultList();
            });
            assertThat(rows(list.stream()
                                    .map(h -> new Object[]{h.getTermStart(),
                                            h.getOffice().getCountry(), h.getNumber(), h.getName()})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_HEAD_B_06: The oldest living heads")
    @Nested
    class HOG_HEAD_B_06_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("JP", "福田康夫", 1936L),
                row("JP", "森喜朗", 1937L),
                row("JP", "細川護熙", 1938L),
                row("JP", "麻生太郎", 1940L),
                row("KR", "이명박", 1941L),
                row("JP", "小泉純一郎", 1942L),
                row("US", "Joe Biden", 1942L),
                row("GB", "John Major", 1943L),
                row("JP", "菅直人", 1946L),
                row("US", "Bill Clinton", 1946L)
        );

        // selects no entity: a row is a person, DISTINCT over the terms of one, not a head
        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT DISTINCT h.country, h.name, h.born
                                    FROM Head AS h
                                    WHERE h.died IS NULL
                                    ORDER BY h.born ASC, h.country ASC, h.name ASC""",
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
                cq.select(cb.array(h.get(Head_.country), h.get(Head_.name), h.get(Head_.born)))
                        .distinct(true)
                        .where(cb.isNull(h.get(Head_.died)))
                        .orderBy(cb.asc(h.get(Head_.born)), cb.asc(h.get(Head_.country)),
                                 cb.asc(h.get(Head_.name)));
                return em.createQuery(cq)
                        .setMaxResults(10)
                        .getResultList();
            });
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_HEAD_B_07: Terms begun during the Second World War")
    @Nested
    class HOG_HEAD_B_07_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("1940-01-16", "JP", 37L, "米内光政", "無所属"),
                row("1940-05-10", "GB", 41L, "Winston Churchill", "Conservative"),
                row("1940-07-22", "JP", 38L, "近衛文麿", "大政翼賛会"),
                row("1941-07-18", "JP", 39L, "近衛文麿", "大政翼賛会"),
                row("1941-10-18", "JP", 40L, "東條英機", "大政翼賛会"),
                row("1944-07-22", "JP", 41L, "小磯國昭", "大政翼賛会"),
                row("1945-04-07", "JP", 42L, "鈴木貫太郎", "大政翼賛会"),
                row("1945-04-12", "US", 33L, "Harry S. Truman", "Democratic"),
                row("1945-07-26", "GB", 42L, "Clement Attlee", "Labour"),
                row("1945-08-17", "JP", 43L, "東久邇宮稔彦王", "無所属")
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT h
                                    FROM Head AS h
                                    WHERE h.termStart BETWEEN '1939-09-01' AND '1945-09-02'
                                    ORDER BY h.termStart ASC, h.country ASC""",
                            Head.class
                    )
                    .getResultList());
            assertThat(rows(list.stream()
                                    .map(h -> new Object[]{h.getTermStart(),
                                            h.getOffice().getCountry(), h.getNumber(),
                                            h.getName(), h.getParty()})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }

        @Test
        void __CriteriaApi() {
            final var list = __Exercises_TestUtils.apply(em -> {
                final var cb = em.getCriteriaBuilder();
                final var cq = cb.createQuery(Head.class);
                final var h = cq.from(Head.class);
                cq.select(h)
                        .where(cb.between(h.get(Head_.termStart), "1939-09-01", "1945-09-02"))
                        .orderBy(cb.asc(h.get(Head_.termStart)), cb.asc(h.get(Head_.country)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list.stream()
                                    .map(h -> new Object[]{h.getTermStart(),
                                            h.getOffice().getCountry(), h.getNumber(),
                                            h.getName(), h.getParty()})
                                    .toList()))
                    .containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("HOG_HEAD_B_08: Heads who lived past ninety-two")
    @Nested
    class HOG_HEAD_B_08_Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("JP", "東久邇宮稔彦王", 1887L, 1990L, 103L),
                row("JP", "中曽根康弘", 1918L, 2019L, 101L),
                row("JP", "村山富市", 1924L, 2025L, 101L),
                row("US", "Jimmy Carter", 1924L, 2024L, 100L),
                row("FR", "Valéry Giscard d'Estaing", 1926L, 2020L, 94L),
                row("US", "George H. W. Bush", 1924L, 2018L, 94L),
                row("GB", "James Callaghan", 1912L, 2005L, 93L),
                row("JP", "鈴木善幸", 1911L, 2004L, 93L),
                row("KR", "윤보선", 1897L, 1990L, 93L),
                row("US", "Gerald Ford", 1913L, 2006L, 93L),
                row("US", "Ronald Reagan", 1911L, 2004L, 93L)
        );

        // selects no entity: a row is a person, DISTINCT over the terms of one, not a head
        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT DISTINCT h.country, h.name, h.born, h.died, CAST(h.died AS INTEGER) - h.born AS age
                                    FROM Head AS h
                                    WHERE CAST(h.died AS INTEGER) - h.born >= 93
                                    ORDER BY age DESC, h.country ASC, h.name ASC""",
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
                final var age = cb.diff(h.get(Head_.died).cast(Integer.class), h.get(Head_.born));
                cq.select(cb.array(h.get(Head_.country), h.get(Head_.name), h.get(Head_.born),
                                   h.get(Head_.died), age))
                        .distinct(true)
                        .where(cb.ge(age, 93))
                        .orderBy(cb.desc(age), cb.asc(h.get(Head_.country)),
                                 cb.asc(h.get(Head_.name)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }
}
