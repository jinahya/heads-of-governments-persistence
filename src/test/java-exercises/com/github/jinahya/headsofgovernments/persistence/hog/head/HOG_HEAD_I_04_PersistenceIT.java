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
 * Answers {@code HOG_HEAD_I_04} of {@code src/test/sql-exercises/head/02_intermediate.sql} with
 * JPQL and with the Criteria API, or with a native query where neither can.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@DisplayName("HOG_HEAD_I_04: Terms per party")
class HOG_HEAD_I_04_PersistenceIT {

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
                    .where(cb.equal(h.get(Head_.country),
                                    cb.parameter(String.class, "country")))
                    .groupBy(h.get(Head_.party))
                    .orderBy(cb.desc(terms), cb.asc(h.get(Head_.party)));
            return em.createQuery(cq)
                    .setParameter("country", "KR")
                    .getResultList();
        });
        assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
    }

}
