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
import com.github.jinahya.headsofgovernments.persistence.exercises.__Exercises_TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.jinahya.headsofgovernments.persistence.exercises.__Exercises_TestUtils.row;
import static com.github.jinahya.headsofgovernments.persistence.exercises.__Exercises_TestUtils.rows;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Answers {@code HOG_SCHEMA_A_03} of {@code src/test/sql-exercises/__/03_advanced.sql} with JPQL
 * and with the Criteria API, or with a native query where neither can.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@DisplayName("HOG_SCHEMA_A_03: The incumbents' contemporaries")
class HOG_SCHEMA_A_03_PersistenceIT {

    private static final List<List<Object>> EXPECTED = List.of(
            row("FR", "Emmanuel Macron", "2022-05-14", "GB", "Prime Minister", "Boris Johnson"),
            row("FR", "Emmanuel Macron", "2022-05-14", "JP", "内閣総理大臣", "岸田文雄"),
            row("FR", "Emmanuel Macron", "2022-05-14", "KR", "대통령", "윤석열"),
            row("FR", "Emmanuel Macron", "2022-05-14", "US", "President", "Joe Biden"),
            row("GB", "Andy Burnham", "2026-07-20", "FR", "Président de la République",
                "Emmanuel Macron"),
            row("GB", "Andy Burnham", "2026-07-20", "JP", "内閣総理大臣", "高市早苗"),
            row("GB", "Andy Burnham", "2026-07-20", "KR", "대통령", "이재명"),
            row("GB", "Andy Burnham", "2026-07-20", "US", "President", "Donald Trump"),
            row("JP", "高市早苗", "2026-02-18", "FR", "Président de la République",
                "Emmanuel Macron"),
            row("JP", "高市早苗", "2026-02-18", "GB", "Prime Minister", "Keir Starmer"),
            row("JP", "高市早苗", "2026-02-18", "KR", "대통령", "이재명"),
            row("JP", "高市早苗", "2026-02-18", "US", "President", "Donald Trump"),
            row("KR", "이재명", "2025-06-04", "FR", "Président de la République",
                "Emmanuel Macron"),
            row("KR", "이재명", "2025-06-04", "GB", "Prime Minister", "Keir Starmer"),
            row("KR", "이재명", "2025-06-04", "JP", "内閣総理大臣", "石破茂"),
            row("KR", "이재명", "2025-06-04", "US", "President", "Donald Trump"),
            row("US", "Donald Trump", "2025-01-20", "FR", "Président de la République",
                "Emmanuel Macron"),
            row("US", "Donald Trump", "2025-01-20", "GB", "Prime Minister", "Keir Starmer"),
            row("US", "Donald Trump", "2025-01-20", "JP", "内閣総理大臣", "石破茂"),
            row("US", "Donald Trump", "2025-01-20", "KR", "대통령", "윤석열")
    );

    @Test
    void __QueryLanguage() {
        final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                        """
                                SELECT i, o, g
                                FROM Head AS i
                                     JOIN Head AS o ON o.country <> i.country
                                                   AND o.termStart <= i.termStart
                                                   AND (o.termEnd IS NULL OR o.termEnd >= i.termStart)
                                     JOIN o.office AS g
                                WHERE i.termEnd IS NULL
                                ORDER BY i.country ASC, o.country ASC""",
                        Object[].class
                )
                .getResultList());
        assertThat(rows(list.stream()
                                .map(r -> {
                                    final var i = (Head) r[0];
                                    final var o = (Head) r[1];
                                    final var g = (Office) r[2];
                                    return new Object[]{i.getOffice().getCountry(),
                                            i.getName(), i.getTermStart(),
                                            o.getOffice().getCountry(),
                                            g.getHeadTitle(), o.getName()};
                                })
                                .toList()))
                .containsExactlyElementsOf(EXPECTED);
    }

    @Test
    void __CriteriaApi() {
        final var list = __Exercises_TestUtils.apply(em -> {
            final var cb = em.getCriteriaBuilder();
            final var cq = cb.createQuery(Object[].class);
            final var i = cq.from(Head.class);
            final var o = i.join(Head.class);
            o.on(cb.notEqual(o.get(Head_.country), i.get(Head_.country)),
                 cb.lessThanOrEqualTo(o.get(Head_.termStart), i.get(Head_.termStart)),
                 cb.or(cb.isNull(o.get(Head_.termEnd)),
                       cb.greaterThanOrEqualTo(o.get(Head_.termEnd).as(String.class), i.get(Head_.termStart))));
            final var g = o.join(Head_.office);
            cq.select(cb.array(i, o, g))
                    .where(cb.isNull(i.get(Head_.termEnd)))
                    .orderBy(cb.asc(i.get(Head_.country)), cb.asc(o.get(Head_.country)));
            return em.createQuery(cq)
                    .getResultList();
        });
        assertThat(rows(list.stream()
                                .map(r -> {
                                    final var i = (Head) r[0];
                                    final var o = (Head) r[1];
                                    final var g = (Office) r[2];
                                    return new Object[]{i.getOffice().getCountry(),
                                            i.getName(), i.getTermStart(),
                                            o.getOffice().getCountry(),
                                            g.getHeadTitle(), o.getName()};
                                })
                                .toList()))
                .containsExactlyElementsOf(EXPECTED);
    }

}
