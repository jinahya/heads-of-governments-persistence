package com.github.jinahya.headsofgovernments.persistence;

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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.jinahya.headsofgovernments.persistence.PersistenceTestUtils.applyEntityManager;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reads the {@code contemporary} view, which no entity maps, into {@link Contemporary} through a native query.
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
class Contemporary_PersistenceTest {

    private static List<Contemporary> selectAll() {
        return applyEntityManager(em -> {
            @SuppressWarnings({"unchecked"})
            final List<Object[]> rows = em.createNativeQuery(
                    """
                            SELECT country_left, name_left, country_right, name_right,
                                   overlap_start, overlap_end
                            FROM contemporary"""
            ).getResultList();
            return rows.stream()
                    .map(r -> new Contemporary((String) r[0], (String) r[1], (String) r[2], (String) r[3],
                                               (String) r[4], (String) r[5]))
                    .toList();
        });
    }

    @Test
    void __() {
        final var list = selectAll();
        assertThat(list).isNotEmpty().allSatisfy(c -> {
            assertThat(c.toString()).isNotBlank();
            // a pair is listed once, the countries in order
            assertThat(c.getCountryLeft()).isLessThan(c.getCountryRight());
            assertThat(c.getNameLeft()).isNotBlank();
            assertThat(c.getNameRight()).isNotBlank();
            // the overlap is a period, open while both still hold office
            assertThat(c.getOverlapStartAsLocalDate()).isNotNull();
            if (c.getOverlapEnd() != null) {
                assertThat(c.getOverlapEndAsLocalDate()).isAfterOrEqualTo(c.getOverlapStartAsLocalDate());
            }
        });
    }

    @Test
    void __incumbents() {
        // every pair of today's heads overlaps, and is still open
        final var open = selectAll().stream().filter(c -> c.getOverlapEnd() == null).toList();
        final var countries = Office_PersistenceTest.COUNTRY_LIST.size();
        assertThat(open).hasSize(countries * (countries - 1) / 2);
    }
}

