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

import com.github.jinahya.headsofgovernments.persistence.Head;
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
 * Answers {@code HOG_SCHEMA_B_01} of {@code src/test/sql-exercises/__/01_basic.sql} with JPQL and
 * with the Criteria API, or with a native query where neither can.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@DisplayName("HOG_SCHEMA_B_01: The incumbents, with their titles")
class HOG_SCHEMA_B_01_PersistenceIT {

    private static final List<List<Object>> EXPECTED = List.of(
            row("FR", "Président de la République", "Emmanuel Macron", "2022-05-14"),
            row("GB", "Prime Minister", "Andy Burnham", "2026-07-20"),
            row("JP", "内閣総理大臣", "高市早苗", "2026-02-18"),
            row("KR", "대통령", "이재명", "2025-06-04"),
            row("US", "President", "Donald Trump", "2025-01-20")
    );

    @Test
    void __QueryLanguage() {
        final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                        """
                                SELECT g, h
                                FROM Office AS g
                                     JOIN g.heads AS h ON h.termEnd IS NULL
                                ORDER BY g.country ASC""",
                        Object[].class
                )
                .getResultList());
        assertThat(rows(list.stream()
                                .map(r -> {
                                    final var g = (Office) r[0];
                                    final var h = (Head) r[1];
                                    return new Object[]{g.getCountry(), g.getHeadTitle(),
                                            h.getName(), h.getTermStart()};
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
            h.on(cb.isNull(h.get(Head_.termEnd)));
            cq.select(cb.array(g, h))
                    .orderBy(cb.asc(g.get(Office_.country)));
            return em.createQuery(cq)
                    .getResultList();
        });
        assertThat(rows(list.stream()
                                .map(r -> {
                                    final var g = (Office) r[0];
                                    final var h = (Head) r[1];
                                    return new Object[]{g.getCountry(), g.getHeadTitle(),
                                            h.getName(), h.getTermStart()};
                                })
                                .toList()))
                .containsExactlyElementsOf(EXPECTED);
    }

}
