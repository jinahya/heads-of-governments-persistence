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
import jakarta.persistence.criteria.JoinType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.jinahya.headsofgovernments.persistence.exercises.__Exercises_TestUtils.row;
import static com.github.jinahya.headsofgovernments.persistence.exercises.__Exercises_TestUtils.rows;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Answers {@code HOG_HEAD_A_05} of {@code src/test/sql-exercises/head/03_advanced.sql} with JPQL
 * and with the Criteria API, or with a native query where neither can.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@DisplayName("HOG_HEAD_A_05: Predecessor and successor")
class HOG_HEAD_A_05_PersistenceIT {

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
                                  AND (p.termStart IS NULL
                                       OR p.termStart = (SELECT MAX(p2.termStart)
                                                         FROM Head AS p2
                                                         WHERE p2.country = h.country
                                                           AND p2.termStart < h.termStart))
                                  AND (s.termStart IS NULL
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
                                    return new Object[]{h.getOffice().getCountry(),
                                            h.getNumber(), h.getTermStart(), h.getTermEnd(),
                                            p == null ? null : p.getName(),
                                            s == null ? null : s.getName()};
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
                           cb.or(cb.isNull(p.get(Head_.termStart)),
                                 cb.equal(p.get(Head_.termStart), previous)),
                           cb.or(cb.isNull(s.get(Head_.termStart)),
                                 cb.equal(s.get(Head_.termStart), next)))
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
                                    return new Object[]{h.getOffice().getCountry(),
                                            h.getNumber(), h.getTermStart(), h.getTermEnd(),
                                            p == null ? null : p.getName(),
                                            s == null ? null : s.getName()};
                                })
                                .toList()))
                .containsExactlyElementsOf(EXPECTED);
    }

}
