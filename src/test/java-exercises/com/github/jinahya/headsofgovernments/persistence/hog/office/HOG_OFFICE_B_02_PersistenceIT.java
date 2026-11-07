package com.github.jinahya.headsofgovernments.persistence.hog.office;

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
 * Answers {@code HOG_OFFICE_B_02} of {@code src/test/sql-exercises/office/01_basic.sql} with JPQL
 * and with the Criteria API, or with a native query where neither can.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@DisplayName("HOG_OFFICE_B_02: The offices")
class HOG_OFFICE_B_02_PersistenceIT {

    private static final List<List<Object>> EXPECTED = List.of(
            row("FR", "PRESIDENT", "Président de la République"),
            row("GB", "PRIME_MINISTER", "Prime Minister"),
            row("JP", "PRIME_MINISTER", "内閣総理大臣"),
            row("KR", "PRESIDENT", "대통령"),
            row("US", "PRESIDENT", "President")
    );

    @Test
    void __QueryLanguage() {
        final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                        """
                                SELECT o
                                FROM Office AS o
                                ORDER BY o.country ASC""",
                        Office.class
                )
                .getResultList());
        assertThat(rows(list.stream()
                                .map(o -> new Object[]{o.getCountry(), o.getHeadKind(),
                                        o.getHeadTitle()})
                                .toList()))
                .containsExactlyElementsOf(EXPECTED);
    }

    @Test
    void __CriteriaApi() {
        final var list = __Exercises_TestUtils.apply(em -> {
            final var cb = em.getCriteriaBuilder();
            final var cq = cb.createQuery(Office.class);
            final var o = cq.from(Office.class);
            cq.select(o)
                    .orderBy(cb.asc(o.get(Office_.country)));
            return em.createQuery(cq)
                    .getResultList();
        });
        assertThat(rows(list.stream()
                                .map(o -> new Object[]{o.getCountry(), o.getHeadKind(),
                                        o.getHeadTitle()})
                                .toList()))
                .containsExactlyElementsOf(EXPECTED);
    }

}
