package com.github.jinahya.headsofgovernments.persistence.hog.schema;

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

import com.github.jinahya.headsofgovernments.persistence.Head_;
import com.github.jinahya.headsofgovernments.persistence.Office;
import com.github.jinahya.headsofgovernments.persistence.Office_;
import com.github.jinahya.headsofgovernments.persistence.exercises.__Exercises_TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.jinahya.headsofgovernments.persistence.exercises.__Exercises_TestUtils.row;
import static com.github.jinahya.headsofgovernments.persistence.exercises.__Exercises_TestUtils.rows;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Answers {@code HOG_SCHEMA_I_02} of {@code src/test/sql-exercises/__/02_intermediate.sql} with
 * JPQL and with the Criteria API, or with a native query where neither can.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@DisplayName("HOG_SCHEMA_I_02: Numbering in practice")
class HOG_SCHEMA_I_02_PersistenceIT {

    private static final List<List<Object>> EXPECTED = List.of(
            row("FR", 31L, 25L, 25L,
                "ordinal of the person by first election; a person re-elected has one line " +
                "per term and keeps the same number on each"),
            row("GB", 81L, 59L, 59L,
                "ordinal of the person by first appointment; a person who served " +
                "non-consecutive terms has one line per term and keeps the same number on " +
                "each"),
            row("JP", 105L, 105L, 105L,
                "代, one per appointment; a cabinet re-formed after a general election starts " +
                "a new 代, so a person keeps one line per 代 with a new number on each"),
            row("KR", 21L, 21L, 21L,
                "대, one per election; a person re-elected keeps one line per term with " +
                "consecutive numbers"),
            row("US", 47L, 47L, 47L,
                "presidency number, one per non-consecutive presidency; a person returning " +
                "after an absence gets a new number")
    );

    @Test
    void __QueryLanguage() {
        final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                        """
                                SELECT g, COUNT(h), COUNT(DISTINCT h.number), MAX(h.number)
                                FROM Office AS g
                                     JOIN g.heads AS h
                                GROUP BY g
                                ORDER BY g.country ASC""",
                        Object[].class
                )
                .getResultList());
        assertThat(rows(list.stream()
                                .map(r -> {
                                    final var g = (Office) r[0];
                                    return new Object[]{g.getCountry(), r[1], r[2], r[3],
                                            g.getNumbering()};
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
            final var h = g.join(Office_.heads);
            cq.select(cb.array(g, cb.count(h), cb.countDistinct(h.get(Head_.number)),
                               cb.max(h.get(Head_.number))))
                    .groupBy(g)
                    .orderBy(cb.asc(g.get(Office_.country)));
            return em.createQuery(cq)
                    .getResultList();
        });
        assertThat(rows(list.stream()
                                .map(r -> {
                                    final var g = (Office) r[0];
                                    return new Object[]{g.getCountry(), r[1], r[2], r[3],
                                            g.getNumbering()};
                                })
                                .toList()))
                .containsExactlyElementsOf(EXPECTED);
    }

}
