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
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
class Office_NamedQuery_PersistenceTest {

    static final List<String> COUNTRY_LIST = Office_PersistenceTest.COUNTRY_LIST;

    // ---------------------------------------------------------------------------------------------
    @DisplayName("Office.selectAll")
    @Nested
    class SelectAllTest {

        private static void verify(final List<Office> list) {
            assertThat(list)
                    .extracting(Office::getCountry)
                    .containsExactlyElementsOf(COUNTRY_LIST);
        }

        @Test
        void __NamedQuery() {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                return em.createNamedQuery("Office.selectAll", Office.class)
                        .getResultList();
            });
            verify(list);
        }

        @Test
        void __QueryLanguage() {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                return em.createQuery(
                                """
                                        SELECT e
                                        FROM Office AS e
                                        ORDER BY e.country ASC""",
                                Office.class
                        )
                        .getResultList();
            });
            verify(list);
        }

        @Test
        void __CriteriaApi() {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                final var builder = em.getCriteriaBuilder();
                final var query = builder.createQuery(Office.class);
                final var root = query.from(Office.class);
                query.select(root)
                        .orderBy(builder.asc(root.get(Office_.country)));
                return em.createQuery(query).getResultList();
            });
            verify(list);
        }
    }

    // ---------------------------------------------------------------------------------------------
    @DisplayName("Office.selectAllByHeadKind")
    @Nested
    class SelectAllByHeadKindTest {

        private static void verify(final List<Office> list, final Office.HeadKind headKind) {
            assertThat(list)
                    .isNotEmpty()
                    .allSatisfy(e -> assertThat(e.getHeadKind()).isEqualTo(headKind))
                    .isSortedAccordingTo(Comparator.comparing(Office::getCountry));
        }

        @EnumSource(Office.HeadKind.class)
        @ParameterizedTest
        void __NamedQuery(final Office.HeadKind headKind) {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                return em.createNamedQuery("Office.selectAllByHeadKind", Office.class)
                        .setParameter("headKind", headKind)
                        .getResultList();
            });
            verify(list, headKind);
        }

        @EnumSource(Office.HeadKind.class)
        @ParameterizedTest
        void __QueryLanguage(final Office.HeadKind headKind) {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                return em.createQuery(
                                """
                                        SELECT e
                                        FROM Office AS e
                                        WHERE e.headKind = :headKind
                                        ORDER BY e.country ASC""",
                                Office.class
                        )
                        .setParameter("headKind", headKind)
                        .getResultList();
            });
            verify(list, headKind);
        }

        @EnumSource(Office.HeadKind.class)
        @ParameterizedTest
        void __CriteriaApi(final Office.HeadKind headKind) {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                final var builder = em.getCriteriaBuilder();
                final var query = builder.createQuery(Office.class);
                final var root = query.from(Office.class);
                query.select(root)
                        .where(builder.equal(root.get(Office_.headKind), headKind))
                        .orderBy(builder.asc(root.get(Office_.country)));
                return em.createQuery(query).getResultList();
            });
            verify(list, headKind);
        }
    }
}
