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
 * Answers {@code HOG_HEAD_B_10} of {@code src/test/sql-exercises/head/01_basic.sql} with JPQL and
 * with the Criteria API, or with a native query where neither can.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@DisplayName("HOG_HEAD_B_10: A country's list, ten terms a page")
class HOG_HEAD_B_10_PersistenceIT {

    private static final List<List<Object>> EXPECTED = List.of(
            row(11L, "전두환", "1980-08-27"),
            row(12L, "전두환", "1981-02-25"),
            row(13L, "노태우", "1988-02-25"),
            row(14L, "김영삼", "1993-02-25"),
            row(15L, "김대중", "1998-02-25"),
            row(16L, "노무현", "2003-02-25"),
            row(17L, "이명박", "2008-02-25"),
            row(18L, "박근혜", "2013-02-25"),
            row(19L, "문재인", "2017-05-10"),
            row(20L, "윤석열", "2022-05-10")
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
                .setFirstResult(10)
                .setMaxResults(10)
                .getResultList());
        assertThat(rows(list.stream()
                                .map(h -> new Object[]{h.getNumber(), h.getName(),
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
                    .where(cb.equal(h.get(Head_.country),
                                    cb.parameter(String.class, "country")))
                    .orderBy(cb.asc(h.get(Head_.termStart)));
            return em.createQuery(cq)
                    .setParameter("country", "KR")
                    .setFirstResult(10)
                    .setMaxResults(10)
                    .getResultList();
        });
        assertThat(rows(list.stream()
                                .map(h -> new Object[]{h.getNumber(), h.getName(),
                                        h.getTermStart()})
                                .toList()))
                .containsExactlyElementsOf(EXPECTED);
    }

}
