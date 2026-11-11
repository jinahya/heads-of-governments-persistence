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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.github.jinahya.headsofgovernments.persistence.PersistenceTestUtils.applyEntityManager;
import static org.assertj.core.api.Assertions.assertThat;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
class Incumbent_PersistenceTest {

    // ---------------------------------------------------------------------------------------------
    @DisplayName("EntityManager#find(entityClass, primaryKey)")
    @Nested
    class FindTest {

        @ValueSource(strings = {"FR", "GB", "JP", "KR", "US"})
        @ParameterizedTest
        void __(final String country) {
            applyEntityManager(em -> {
                final var found = em.find(Incumbent.class, country);
                log.debug("found: {}", found);
                assertThat(found).isNotNull();
                assertThat(found.getCountry()).isEqualTo(country);
                // the incumbent is the head of the country whose term has not ended
                final var head = em.find(Head.class, new HeadId(country, found.getTermStart()));
                assertThat(head).isNotNull();
                assertThat(head.getTermEnd()).isNull();
                assertThat(head.getName()).isEqualTo(found.getName());
                assertThat(head.getNumber()).isEqualTo(found.getNumber());
                // and holds the office the country's row describes
                final var office = em.find(Office.class, country);
                assertThat(found.getHeadKind()).isEqualTo(office.getHeadKind());
                assertThat(found.getHeadTitle()).isEqualTo(office.getHeadTitle());
                return null;
            });
        }
    }

    // ---------------------------------------------------------------------------------------------
    @DisplayName("SELECT e FROM Incumbent")
    @Nested
    class SelectAllTest {

        @Test
        void __QueryLanguage() {
            final var list = applyEntityManager(
                    em -> em.createQuery(
                                    """
                                            SELECT e
                                            FROM Incumbent AS e
                                            ORDER BY e.country ASC""",
                                    Incumbent.class
                            )
                            .getResultList()
            );
            assertThat(list)
                    .extracting(Incumbent::getCountry)
                    .containsExactlyElementsOf(Office_PersistenceTest.COUNTRY_LIST);
        }

        @Test
        void __CriteriaApi() {
            final var list = applyEntityManager(em -> {
                final var builder = em.getCriteriaBuilder();
                final var query = builder.createQuery(Incumbent.class);
                final var root = query.from(Incumbent.class);
                query.select(root)
                        .orderBy(builder.asc(root.get(Incumbent_.country)));
                return em.createQuery(query).getResultList();
            });
            assertThat(list)
                    .extracting(Incumbent::getCountry)
                    .containsExactlyElementsOf(Office_PersistenceTest.COUNTRY_LIST);
        }
    }
}

