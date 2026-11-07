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
import jakarta.persistence.criteria.JoinType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static com.github.jinahya.headsofgovernments.persistence.exercises.__Exercises_TestUtils.row;
import static com.github.jinahya.headsofgovernments.persistence.exercises.__Exercises_TestUtils.rows;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Answers {@code HOG_SCHEMA_A_02} of {@code src/test/sql-exercises/__/03_advanced.sql} with JPQL
 * and with the Criteria API, or with a native query where neither can.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@DisplayName("HOG_SCHEMA_A_02: Every office on a day, vacant or not")
class HOG_SCHEMA_A_02_PersistenceIT {

    private static final List<List<Object>> EXPECTED = List.of(
            row("FR", "Président de la République", "(vacant)", null, null),
            row("GB", "Prime Minister", "Winston Churchill", "1940-05-10", "1945-07-26"),
            row("JP", "内閣総理大臣", "小磯國昭", "1944-07-22", "1945-04-07"),
            row("KR", "대통령", "(vacant)", null, null),
            row("US", "President", "Franklin D. Roosevelt", "1933-03-04", "1945-04-12")
    );

    @Test
    void __QueryLanguage() {
        final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                        """
                                SELECT g, COALESCE(h.name, '(vacant)'), h
                                FROM Office AS g
                                     LEFT JOIN g.heads AS h ON h.termStart <= :dateText AND (h.termEnd IS NULL OR h.termEnd >= :date)
                                ORDER BY g.country ASC""",
                        Object[].class
                )
                .setParameter("date", LocalDate.of(1945, 1, 1))
                .setParameter("dateText", "1945-01-01")
                .getResultList());
        assertThat(rows(list.stream()
                                .map(r -> {
                                    final var g = (Office) r[0];
                                    final var h = (Head) r[2];
                                    return new Object[]{g.getCountry(), g.getHeadTitle(), r[1],
                                            h == null ? null : h.getTermStart(),
                                            h == null ? null : h.getTermEnd()};
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
            final var h = g.join(Office_.heads, JoinType.LEFT);
            final var date = cb.parameter(LocalDate.class, "date");
            final var dateText = cb.parameter(String.class, "dateText");
            h.on(cb.lessThanOrEqualTo(h.get(Head_.termStart), dateText),
                 cb.or(cb.isNull(h.get(Head_.termEnd)),
                       cb.greaterThanOrEqualTo(h.get(Head_.termEnd), date)));
            cq.select(cb.array(g, cb.coalesce(h.get(Head_.name), "(vacant)"), h))
                    .orderBy(cb.asc(g.get(Office_.country)));
            return em.createQuery(cq)
                    .setParameter("date", LocalDate.of(1945, 1, 1))
                    .setParameter("dateText", "1945-01-01")
                    .getResultList();
        });
        assertThat(rows(list.stream()
                                .map(r -> {
                                    final var g = (Office) r[0];
                                    final var h = (Head) r[2];
                                    return new Object[]{g.getCountry(), g.getHeadTitle(), r[1],
                                            h == null ? null : h.getTermStart(),
                                            h == null ? null : h.getTermEnd()};
                                })
                                .toList()))
                .containsExactlyElementsOf(EXPECTED);
    }

}
