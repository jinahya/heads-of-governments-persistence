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

import jakarta.persistence.criteria.Nulls;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
class HeadPersistenceNamedQueryTest {

    static final List<String> OFFICE_COUNTRIES = OfficePersistenceTest.COUNTRY_LIST;

    // every office has terms starting on or after this
    static final String TERM_START_MIN_INCLUSIVE = "1950-01-01";

    // only the incumbent of KR, whose first term starts in 1948, comes before this
    static final LocalDate TERM_END_MAX_EXCLUSIVE = LocalDate.of(1900, 1, 1);

    // every office has heads born in or after this
    static final Integer BORN_MIN_INCLUSIVE = 1950;

    // every office has heads who died before this
    static final Year DIED_MAX_EXCLUSIVE = Year.of(2000);

    // ---------------------------------------------------------------------------------------------
    @DisplayName("Head.selectAllByOfficeOrderByNumberAsc")
    @Nested
    class SelectAllByOfficeOrderByNumberAscTest {

        private static Stream<String> officeCountryStream() {
            return OFFICE_COUNTRIES.stream();
        }

        private static void verify(final List<Head> list, final String officeCountry) {
            assertThat(list)
                    .isNotEmpty()
                    .allSatisfy(e -> assertThat(e.getOffice().getCountry()).isEqualTo(
                            officeCountry))
                    .isSortedAccordingTo(Comparator.comparing(Head::getNumber));
        }

        @MethodSource({"officeCountryStream"})
        @ParameterizedTest
        void __NamedQuery(final String officeCountry) {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                final var office = em.getReference(Office.class, officeCountry);
                return em.createNamedQuery("Head.selectAllByOfficeOrderByNumberAsc", Head.class)
                        .setParameter("office", office)
                        .getResultList();
            });
            verify(list, officeCountry);
        }

        @MethodSource({"officeCountryStream"})
        @ParameterizedTest
        void __QueryLanguage(final String officeCountry) {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                final var office = em.getReference(Office.class, officeCountry);
                return em.createQuery(
                                """
                                        SELECT e
                                        FROM Head AS e
                                        WHERE e.office = :office
                                        ORDER BY e.number ASC""",
                                Head.class
                        )
                        .setParameter("office", office)
                        .getResultList();
            });
            verify(list, officeCountry);
        }

        @MethodSource({"officeCountryStream"})
        @ParameterizedTest
        void __CriteriaApi(final String officeCountry) {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                final var office = em.getReference(Office.class, officeCountry);
                final var builder = em.getCriteriaBuilder();
                final var query = builder.createQuery(Head.class);
                final var root = query.from(Head.class);
                query.select(root)
                        .where(builder.equal(root.get(Head_.office), office))
                        .orderBy(builder.asc(root.get(Head_.number)));
                return em.createQuery(query).getResultList();
            });
            verify(list, officeCountry);
        }
    }

    // ---------------------------------------------------------------------------------------------
    @DisplayName("Head.selectAllByOfficeOrderByTermStartDesc")
    @Nested
    class SelectAllByOfficeOrderByTermStartDescTest {

        private static Stream<String> officeCountryStream() {
            return OFFICE_COUNTRIES.stream();
        }

        private static void verify(final List<Head> list, final String officeCountry) {
            assertThat(list)
                    .isNotEmpty()
                    .allSatisfy(e -> assertThat(e.getOffice().getCountry()).isEqualTo(
                            officeCountry))
                    .isSortedAccordingTo(Comparator.comparing(Head::getTermStart).reversed());
        }

        @MethodSource({"officeCountryStream"})
        @ParameterizedTest
        void __NamedQuery(final String officeCountry) {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                final var office = em.getReference(Office.class, officeCountry);
                return em.createNamedQuery("Head.selectAllByOfficeOrderByTermStartDesc", Head.class)
                        .setParameter("office", office)
                        .getResultList();
            });
            verify(list, officeCountry);
        }

        @MethodSource({"officeCountryStream"})
        @ParameterizedTest
        void __QueryLanguage(final String officeCountry) {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                final var office = em.getReference(Office.class, officeCountry);
                return em.createQuery(
                                """
                                        SELECT e
                                        FROM Head AS e
                                        WHERE e.office = :office
                                        ORDER BY e.termStart DESC""",
                                Head.class
                        )
                        .setParameter("office", office)
                        .getResultList();
            });
            verify(list, officeCountry);
        }

        @MethodSource({"officeCountryStream"})
        @ParameterizedTest
        void __CriteriaApi(final String officeCountry) {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                final var office = em.getReference(Office.class, officeCountry);
                final var builder = em.getCriteriaBuilder();
                final var query = builder.createQuery(Head.class);
                final var root = query.from(Head.class);
                query.select(root)
                        .where(builder.equal(root.get(Head_.office), office))
                        .orderBy(builder.desc(root.get(Head_.termStart)));
                return em.createQuery(query).getResultList();
            });
            verify(list, officeCountry);
        }
    }

    // ---------------------------------------------------------------------------------------------
    @DisplayName("Head.selectAllByOfficeAndTermStartGreaterThanOrEqualTo")
    @Nested
    class SelectAllByOfficeAndTermStartGreaterThanOrEqualToTest {

        private static Stream<Arguments> officeCountryAndTermStartMinInclusiveStream() {
            return OFFICE_COUNTRIES.stream()
                    .map(c -> Arguments.of(c, TERM_START_MIN_INCLUSIVE));
        }

        private static void verify(final List<Head> list, final String officeCountry,
                                   final String termStartMinInclusive) {
            assertThat(list)
                    .isNotEmpty()
                    .allSatisfy(e -> {
                        assertThat(e.getOffice().getCountry()).isEqualTo(officeCountry);
                        assertThat(e.getTermStart()).isGreaterThanOrEqualTo(termStartMinInclusive);
                    })
                    .isSortedAccordingTo(Comparator.comparing(Head::getTermStart));
        }

        @MethodSource({"officeCountryAndTermStartMinInclusiveStream"})
        @ParameterizedTest
        void __NamedQuery(final String officeCountry, final String termStartMinInclusive) {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                final var office = em.getReference(Office.class, officeCountry);
                return em.createNamedQuery(
                                "Head.selectAllByOfficeAndTermStartGreaterThanOrEqualTo",
                                Head.class
                        )
                        .setParameter("office", office)
                        .setParameter("termStartMinInclusive", termStartMinInclusive)
                        .getResultList();
            });
            verify(list, officeCountry, termStartMinInclusive);
        }

        @MethodSource({"officeCountryAndTermStartMinInclusiveStream"})
        @ParameterizedTest
        void __QueryLanguage(final String officeCountry, final String termStartMinInclusive) {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                final var office = em.getReference(Office.class, officeCountry);
                return em.createQuery(
                                """
                                        SELECT e
                                        FROM Head AS e
                                        WHERE e.office = :office
                                          AND e.termStart >= :termStartMinInclusive
                                        ORDER BY e.termStart ASC""",
                                Head.class
                        )
                        .setParameter("office", office)
                        .setParameter("termStartMinInclusive", termStartMinInclusive)
                        .getResultList();
            });
            verify(list, officeCountry, termStartMinInclusive);
        }

        @MethodSource({"officeCountryAndTermStartMinInclusiveStream"})
        @ParameterizedTest
        void __CriteriaApi(final String officeCountry, final String termStartMinInclusive) {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                final var office = em.getReference(Office.class, officeCountry);
                final var builder = em.getCriteriaBuilder();
                final var query = builder.createQuery(Head.class);
                final var root = query.from(Head.class);
                query.select(root)
                        .where(builder.equal(root.get(Head_.office), office),
                               builder.greaterThanOrEqualTo(root.get(Head_.termStart),
                                                            termStartMinInclusive))
                        .orderBy(builder.asc(root.get(Head_.termStart)));
                return em.createQuery(query).getResultList();
            });
            verify(list, officeCountry, termStartMinInclusive);
        }
    }

    // ---------------------------------------------------------------------------------------------
    @DisplayName("Head.selectAllByOfficeAndTermEndNullOrLessThan")
    @Nested
    class SelectAllByOfficeAndTermEndNullOrLessThanTest {

        private static Stream<Arguments> officeCountryAndTermEndMaxExclusiveStream() {
            return OFFICE_COUNTRIES.stream()
                    .map(c -> Arguments.of(c, TERM_END_MAX_EXCLUSIVE));
        }

        // every office has one incumbent, whose term end is null and comes first
        private static void verify(final List<Head> list, final String officeCountry,
                                   final LocalDate termEndMaxExclusive) {
            assertThat(list)
                    .isNotEmpty()
                    .allSatisfy(e -> {
                        assertThat(e.getOffice().getCountry()).isEqualTo(officeCountry);
                        if (e.getTermEnd() != null) {
                            assertThat(e.getTermEnd()).isBefore(termEndMaxExclusive);
                        }
                    })
                    .isSortedAccordingTo(Comparator.comparing(
                            Head::getTermEnd,
                            Comparator.nullsFirst(Comparator.<LocalDate>reverseOrder())));
            assertThat(list.getFirst().getTermEnd()).isNull();
        }

        @MethodSource({"officeCountryAndTermEndMaxExclusiveStream"})
        @ParameterizedTest
        void __NamedQuery(final String officeCountry, final LocalDate termEndMaxExclusive) {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                final var office = em.getReference(Office.class, officeCountry);
                return em.createNamedQuery(
                                "Head.selectAllByOfficeAndTermEndNullOrLessThan",
                                Head.class
                        )
                        .setParameter("office", office)
                        .setParameter("termEndMaxExclusive", termEndMaxExclusive)
                        .getResultList();
            });
            verify(list, officeCountry, termEndMaxExclusive);
        }

        @MethodSource({"officeCountryAndTermEndMaxExclusiveStream"})
        @ParameterizedTest
        void __QueryLanguage(final String officeCountry, final LocalDate termEndMaxExclusive) {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                final var office = em.getReference(Office.class, officeCountry);
                return em.createQuery(
                                """
                                        SELECT e
                                        FROM Head AS e
                                        WHERE e.office = :office
                                          AND (e.termEnd IS NULL OR e.termEnd < :termEndMaxExclusive)
                                        ORDER BY e.termEnd DESC NULLS FIRST""",
                                Head.class
                        )
                        .setParameter("office", office)
                        .setParameter("termEndMaxExclusive", termEndMaxExclusive)
                        .getResultList();
            });
            verify(list, officeCountry, termEndMaxExclusive);
        }

        @MethodSource({"officeCountryAndTermEndMaxExclusiveStream"})
        @ParameterizedTest
        void __CriteriaApi(final String officeCountry, final LocalDate termEndMaxExclusive) {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                final var office = em.getReference(Office.class, officeCountry);
                final var builder = em.getCriteriaBuilder();
                final var query = builder.createQuery(Head.class);
                final var root = query.from(Head.class);
                query.select(root)
                        .where(builder.equal(root.get(Head_.office), office),
                               builder.or(builder.isNull(root.get(Head_.termEnd)),
                                          builder.lessThan(root.get(Head_.termEnd),
                                                           termEndMaxExclusive)))
                        .orderBy(builder.desc(root.get(Head_.termEnd), Nulls.FIRST));
                return em.createQuery(query).getResultList();
            });
            verify(list, officeCountry, termEndMaxExclusive);
        }
    }

    // ---------------------------------------------------------------------------------------------
    @DisplayName("Head.selectAllByOfficeAndBornGreaterThanOrEqualTo")
    @Nested
    class SelectAllByOfficeAndBornGreaterThanOrEqualToTest {

        private static Stream<Arguments> officeCountryAndBornMinInclusiveStream() {
            return OFFICE_COUNTRIES.stream()
                    .map(c -> Arguments.of(c, BORN_MIN_INCLUSIVE));
        }

        private static void verify(final List<Head> list, final String officeCountry,
                                   final Integer bornMinInclusive) {
            assertThat(list)
                    .isNotEmpty()
                    .allSatisfy(e -> {
                        assertThat(e.getOffice().getCountry()).isEqualTo(officeCountry);
                        assertThat(e.getBorn()).isGreaterThanOrEqualTo(bornMinInclusive);
                    })
                    .isSortedAccordingTo(Comparator.comparing(Head::getBorn));
        }

        @MethodSource({"officeCountryAndBornMinInclusiveStream"})
        @ParameterizedTest
        void __NamedQuery(final String officeCountry, final Integer bornMinInclusive) {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                final var office = em.getReference(Office.class, officeCountry);
                return em.createNamedQuery("Head.selectAllByOfficeAndBornGreaterThanOrEqualTo",
                                           Head.class)
                        .setParameter("office", office)
                        .setParameter("bornMinInclusive", bornMinInclusive)
                        .getResultList();
            });
            verify(list, officeCountry, bornMinInclusive);
        }

        @MethodSource({"officeCountryAndBornMinInclusiveStream"})
        @ParameterizedTest
        void __QueryLanguage(final String officeCountry, final Integer bornMinInclusive) {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                final var office = em.getReference(Office.class, officeCountry);
                return em.createQuery(
                                """
                                        SELECT e
                                        FROM Head AS e
                                        WHERE e.office = :office
                                          AND e.born >= :bornMinInclusive
                                        ORDER BY e.born ASC""",
                                Head.class
                        )
                        .setParameter("office", office)
                        .setParameter("bornMinInclusive", bornMinInclusive)
                        .getResultList();
            });
            verify(list, officeCountry, bornMinInclusive);
        }

        @MethodSource({"officeCountryAndBornMinInclusiveStream"})
        @ParameterizedTest
        void __CriteriaApi(final String officeCountry, final Integer bornMinInclusive) {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                final var office = em.getReference(Office.class, officeCountry);
                final var builder = em.getCriteriaBuilder();
                final var query = builder.createQuery(Head.class);
                final var root = query.from(Head.class);
                query.select(root)
                        .where(builder.equal(root.get(Head_.office), office),
                               builder.ge(root.get(Head_.born), bornMinInclusive))
                        .orderBy(builder.asc(root.get(Head_.born)));
                return em.createQuery(query).getResultList();
            });
            verify(list, officeCountry, bornMinInclusive);
        }
    }

    // ---------------------------------------------------------------------------------------------
    @DisplayName("Head.selectAllByOfficeAndDiedNullOrLessThan")
    @Nested
    class SelectAllByOfficeAndDiedNullOrLessThanTest {

        private static Stream<Arguments> officeCountryAndDiedMaxExclusiveStream() {
            return OFFICE_COUNTRIES.stream()
                    .map(c -> Arguments.of(c, DIED_MAX_EXCLUSIVE));
        }

        // every office has living heads, whose died is null and come first
        private static void verify(final List<Head> list, final String officeCountry,
                                   final Year diedMaxExclusive) {
            assertThat(list)
                    .isNotEmpty()
                    .allSatisfy(e -> {
                        assertThat(e.getOffice().getCountry()).isEqualTo(officeCountry);
                        if (e.getDied() != null) {
                            assertThat(e.getDied()).isLessThan(diedMaxExclusive);
                        }
                    })
                    .isSortedAccordingTo(Comparator.comparing(
                            Head::getDied,
                            Comparator.nullsFirst(Comparator.<Year>reverseOrder())));
            assertThat(list.getFirst().getDied()).isNull();
        }

        @MethodSource({"officeCountryAndDiedMaxExclusiveStream"})
        @ParameterizedTest
        void __NamedQuery(final String officeCountry, final Year diedMaxExclusive) {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                final var office = em.getReference(Office.class, officeCountry);
                return em.createNamedQuery("Head.selectAllByOfficeAndDiedNullOrLessThan",
                                           Head.class)
                        .setParameter("office", office)
                        .setParameter("diedMaxExclusive", diedMaxExclusive)
                        .getResultList();
            });
            verify(list, officeCountry, diedMaxExclusive);
        }

        @MethodSource({"officeCountryAndDiedMaxExclusiveStream"})
        @ParameterizedTest
        void __QueryLanguage(final String officeCountry, final Year diedMaxExclusive) {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                final var office = em.getReference(Office.class, officeCountry);
                return em.createQuery(
                                """
                                        SELECT e
                                        FROM Head AS e
                                        WHERE e.office = :office
                                          AND (e.died IS NULL OR e.died < :diedMaxExclusive)
                                        ORDER BY e.died DESC NULLS FIRST""",
                                Head.class
                        )
                        .setParameter("office", office)
                        .setParameter("diedMaxExclusive", diedMaxExclusive)
                        .getResultList();
            });
            verify(list, officeCountry, diedMaxExclusive);
        }

        @MethodSource({"officeCountryAndDiedMaxExclusiveStream"})
        @ParameterizedTest
        void __CriteriaApi(final String officeCountry, final Year diedMaxExclusive) {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                final var office = em.getReference(Office.class, officeCountry);
                final var builder = em.getCriteriaBuilder();
                final var query = builder.createQuery(Head.class);
                final var root = query.from(Head.class);
                query.select(root)
                        .where(builder.equal(root.get(Head_.office), office),
                               builder.or(builder.isNull(root.get(Head_.died)),
                                          builder.lessThan(root.get(Head_.died), diedMaxExclusive)))
                        .orderBy(builder.desc(root.get(Head_.died), Nulls.FIRST));
                return em.createQuery(query).getResultList();
            });
            verify(list, officeCountry, diedMaxExclusive);
        }
    }
}
