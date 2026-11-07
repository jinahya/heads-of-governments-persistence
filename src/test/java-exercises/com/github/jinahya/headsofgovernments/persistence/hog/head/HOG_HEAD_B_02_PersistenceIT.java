package com.github.jinahya.headsofgovernments.persistence.hog.head;

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
import com.github.jinahya.headsofgovernments.persistence.exercises.__Exercises_TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.jinahya.headsofgovernments.persistence.exercises.__Exercises_TestUtils.row;
import static com.github.jinahya.headsofgovernments.persistence.exercises.__Exercises_TestUtils.rows;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Answers {@code HOG_HEAD_B_02} of {@code src/test/sql-exercises/head/01_basic.sql} with JPQL and
 * with the Criteria API, or with a native query where neither can.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@DisplayName("HOG_HEAD_B_02: One country's list")
class HOG_HEAD_B_02_PersistenceIT {

    private static final List<List<Object>> EXPECTED = List.of(
            row("KR", 1L, "1948-07-24", "1952-08-14", "이승만", "대한독립촉성국민회/자유당", 1875L,
                1965L),
            row("KR", 2L, "1952-08-15", "1956-08-14", "이승만", "자유당", 1875L, 1965L),
            row("KR", 3L, "1956-08-15", "1960-04-27", "이승만", "자유당", 1875L, 1965L),
            row("KR", 4L, "1960-08-12", "1962-03-22", "윤보선", "무소속", 1897L, 1990L),
            row("KR", 5L, "1963-12-17", "1967-06-30", "박정희", "민주공화당", 1917L, 1979L),
            row("KR", 6L, "1967-07-01", "1971-06-30", "박정희", "민주공화당", 1917L, 1979L),
            row("KR", 7L, "1971-07-01", "1972-12-26", "박정희", "민주공화당", 1917L, 1979L),
            row("KR", 8L, "1972-12-27", "1978-12-26", "박정희", "민주공화당", 1917L, 1979L),
            row("KR", 9L, "1978-12-27", "1979-10-26", "박정희", "민주공화당", 1917L, 1979L),
            row("KR", 10L, "1979-12-06", "1980-08-16", "최규하", "무소속", 1919L, 2006L),
            row("KR", 11L, "1980-08-27", "1981-02-24", "전두환", "무소속/민주정의당", 1931L, 2021L),
            row("KR", 12L, "1981-02-25", "1988-02-24", "전두환", "민주정의당", 1931L, 2021L),
            row("KR", 13L, "1988-02-25", "1993-02-24", "노태우", "민주정의당/민주자유당/무소속", 1932L,
                2021L),
            row("KR", 14L, "1993-02-25", "1998-02-24", "김영삼", "민주자유당/신한국당/무소속", 1929L,
                2015L),
            row("KR", 15L, "1998-02-25", "2003-02-24", "김대중", "새정치국민회의/새천년민주당/무소속", 1924L,
                2009L),
            row("KR", 16L, "2003-02-25", "2008-02-24", "노무현", "새천년민주당/무소속/열린우리당/무소속",
                1946L, 2009L),
            row("KR", 17L, "2008-02-25", "2013-02-24", "이명박", "한나라당/새누리당", 1941L, null),
            row("KR", 18L, "2013-02-25", "2017-03-10", "박근혜", "새누리당/자유한국당", 1952L, null),
            row("KR", 19L, "2017-05-10", "2022-05-09", "문재인", "더불어민주당", 1953L, null),
            row("KR", 20L, "2022-05-10", "2025-04-04", "윤석열", "국민의힘", 1960L, null),
            row("KR", 21L, "2025-06-04", null, "이재명", "더불어민주당", 1963L, null)
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
                                .map(h -> new Object[]{h.getOffice().getCountry(),
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
                                .map(h -> new Object[]{h.getOffice().getCountry(),
                                        h.getNumber(), h.getTermStart(),
                                        h.getTermEnd(), h.getName(), h.getParty(), h.getBorn(),
                                        h.getDied()})
                                .toList()))
                .containsExactlyElementsOf(EXPECTED);
    }

}
