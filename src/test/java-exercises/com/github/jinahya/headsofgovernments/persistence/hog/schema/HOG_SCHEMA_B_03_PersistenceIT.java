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
import jakarta.persistence.criteria.JoinType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.jinahya.headsofgovernments.persistence.exercises.__Exercises_TestUtils.row;
import static com.github.jinahya.headsofgovernments.persistence.exercises.__Exercises_TestUtils.rows;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Answers {@code HOG_SCHEMA_B_03} of {@code src/test/sql-exercises/__/01_basic.sql} with JPQL and
 * with the Criteria API, or with a native query where neither can.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@DisplayName("HOG_SCHEMA_B_03: Terms per office")
class HOG_SCHEMA_B_03_PersistenceIT {

    private static final List<List<Object>> EXPECTED = List.of(
            row("FR", "PRESIDENT", 31L),
            row("GB", "PRIME_MINISTER", 81L),
            row("JP", "PRIME_MINISTER", 105L),
            row("KR", "PRESIDENT", 21L),
            row("US", "PRESIDENT", 47L)
    );

    @Test
    void __QueryLanguage() {
        final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                        """
                                SELECT g, COUNT(h.termStart)
                                FROM Office AS g
                                     LEFT JOIN g.heads AS h
                                GROUP BY g
                                ORDER BY g.country ASC""",
                        Object[].class
                )
                .getResultList());
        assertThat(rows(list.stream()
                                .map(r -> new Object[]{((Office) r[0]).getCountry(),
                                        ((Office) r[0]).getHeadKind(), r[1]})
                                .toList()))
                .containsExactlyElementsOf(EXPECTED);
    }

    @Test
    void __CriteriaApi() {
        final var list = __Exercises_TestUtils.apply(em -> {
            final var cb = em.getCriteriaBuilder();
            final var cq = cb.createQuery(Object[].class);
            final var g = cq.from(Office.class);
            final var h = g.join(Office_.heads, JoinType.LEFT);
            cq.select(cb.array(g, cb.count(h.get(Head_.termStart))))
                    .groupBy(g)
                    .orderBy(cb.asc(g.get(Office_.country)));
            return em.createQuery(cq)
                    .getResultList();
        });
        assertThat(rows(list.stream()
                                .map(r -> new Object[]{((Office) r[0]).getCountry(),
                                        ((Office) r[0]).getHeadKind(), r[1]})
                                .toList()))
                .containsExactlyElementsOf(EXPECTED);
    }

}
