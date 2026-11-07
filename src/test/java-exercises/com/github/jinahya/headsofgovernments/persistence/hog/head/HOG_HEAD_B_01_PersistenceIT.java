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

import java.time.LocalDate;
import java.util.List;

import static com.github.jinahya.headsofgovernments.persistence.exercises.__Exercises_TestUtils.row;
import static com.github.jinahya.headsofgovernments.persistence.exercises.__Exercises_TestUtils.rows;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Answers {@code HOG_HEAD_B_01} of {@code src/test/sql-exercises/head/01_basic.sql} with JPQL and
 * with the Criteria API, or with a native query where neither can.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@DisplayName("HOG_HEAD_B_01: Who held office on a day")
class HOG_HEAD_B_01_PersistenceIT {

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
