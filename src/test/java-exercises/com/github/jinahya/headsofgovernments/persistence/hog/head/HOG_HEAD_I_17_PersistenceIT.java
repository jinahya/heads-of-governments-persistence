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
 * Answers {@code HOG_HEAD_I_17} of {@code src/test/sql-exercises/head/02_intermediate.sql} with
 * JPQL and with the Criteria API, or with a native query where neither can.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@DisplayName("HOG_HEAD_I_17: Terms begun fifteen years younger than the country's average")
class HOG_HEAD_I_17_PersistenceIT {

    private static final List<List<Object>> EXPECTED = List.of(
            row("FR", 1L, "Louis-Napoléon Bonaparte", "1848-12-20", 40L),
            row("FR", 25L, "Emmanuel Macron", "2017-05-14", 40L),
            row("FR", 25L, "Emmanuel Macron", "2022-05-14", 45L),
            row("GB", 5L, "William Cavendish", "1756-11-16", 36L),
            row("GB", 8L, "Charles Watson-Wentworth", "1765-07-13", 35L),
            row("GB", 10L, "Augustus FitzRoy", "1768-10-14", 33L),
            row("GB", 11L, "Frederick North", "1770-01-28", 38L),
            row("GB", 14L, "William Pitt the Younger", "1783-12-19", 24L),
            row("JP", 1L, "伊藤博文", "1885-12-22", 44L),
            row("JP", 2L, "黒田清隆", "1888-04-30", 48L),
            row("JP", 34L, "近衛文麿", "1937-06-04", 46L),
            row("KR", 5L, "박정희", "1963-12-17", 46L)
    );

    @Test
    void __QueryLanguage() {
        final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                        """
                                SELECT h, CAST(SUBSTRING(h.termStart, 1, 4) AS INTEGER) - h.born
                                FROM Head AS h
                                WHERE CAST(SUBSTRING(h.termStart, 1, 4) AS INTEGER) - h.born + 15
                                          <= (SELECT AVG(CAST(SUBSTRING(s.termStart, 1, 4) AS INTEGER) - s.born)
                                              FROM Head AS s
                                              WHERE s.country = h.country)
                                ORDER BY h.country ASC, h.termStart ASC""",
                        Object[].class
                )
                .getResultList());
        assertThat(rows(list.stream()
                                .map(r -> {
                                    final var h = (Head) r[0];
                                    return new Object[]{h.getOffice().getCountry(),
                                            h.getNumber(), h.getName(), h.getTermStart(),
                                            r[1]};
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
            final var age =
                    cb.diff(cb.substring(h.get(Head_.termStart), 1, 4).cast(Integer.class),
                            h.get(Head_.born));
            final var average = cq.subquery(Double.class);
            final var s = average.from(Head.class);
            average.select(cb.avg(
                            cb.diff(cb.substring(s.get(Head_.termStart), 1, 4).cast(Integer.class),
                                    s.get(Head_.born))))
                    .where(cb.equal(s.get(Head_.country), h.get(Head_.country)));
            cq.select(cb.array(h, age))
                    .where(cb.le(cb.sum(age, 15), average))
                    .orderBy(cb.asc(h.get(Head_.country)), cb.asc(h.get(Head_.termStart)));
            return em.createQuery(cq)
                    .getResultList();
        });
        assertThat(rows(list.stream()
                                .map(r -> {
                                    final var h = (Head) r[0];
                                    return new Object[]{h.getOffice().getCountry(),
                                            h.getNumber(), h.getName(), h.getTermStart(),
                                            r[1]};
                                })
                                .toList()))
                .containsExactlyElementsOf(EXPECTED);
    }

}
