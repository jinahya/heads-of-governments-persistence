package com.github.jinahya.headsofgovernments.persistence;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
class GovernmentPersistenceTest {

    @DisplayName("EntityManager#find(entityClass, primaryKey)")
    @Nested
    class FindTest {

        @ValueSource(strings = {
                "FR",
                "GB",
                "JP",
                "KR",
                "US"
        })
        @ParameterizedTest
        void __1(final String country) {
            final var found = PersistenceTestUtils.applyEntityManager(em -> {
                return em.find(Government.class, country);
            });
            assertThat(found)
                    .isNotNull()
                    .extracting(Government::getCountry)
                    .isEqualTo(country);
        }

        private static Stream<String> countries() {
            return Locale.availableLocales()
                    .map(Locale::getCountry)
                    .distinct();
        }

        @MethodSource({"countries"})
        @ParameterizedTest
        void __2(final String country) {
            final var found = PersistenceTestUtils.applyEntityManager(em -> {
                return em.find(Government.class, country);
            });
            if (found == null) {
                log.debug("no government found for {}", country);
                return;
            }
            assertThat(found)
                    .extracting(Government::getCountry)
                    .isEqualTo(country);
        }
    }

    @Nested
    class SelectRandomTest {

        @Test
        void __() {
            final var random = PersistenceTestUtils.selectRandom(Government.class);
            assumeThat(random).isNotEmpty();
            final var found = random.get();
        }
    }

    @DisplayName("SELECT e FROM Government AS e WHERE e.headKind = :headKind ORDER BY e.country ASC")
    @Nested
    class SelectAll_WhereHeadKindEqual_OrderByCountryAsc_Test {

        private static void verify(final List<Government> list, final String headKind) {
            assertThat(list)
                    .isNotEmpty()
                    .allSatisfy(e -> assertThat(e.getHeadKind()).isEqualTo(headKind))
                    .isSortedAccordingTo(Comparator.comparing(Government::getCountry));
        }

        @ValueSource(strings = {
                "PRESIDENT",
                "PRIME_MINISTER"
        })
        @ParameterizedTest
        void __NamedQuery(final String headKind) {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                return em.createNamedQuery(
                                "Government.selectAll_WhereHeadKindEqual_OrderByCountryAsc",
                                Government.class
                        )
                        .setParameter("headKind", headKind)
                        .getResultList();
            });
            verify(list, headKind);
        }

        @ValueSource(strings = {
                "PRESIDENT",
                "PRIME_MINISTER"
        })
        @ParameterizedTest
        void __QueryLanguage(final String headKind) {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                return em.createQuery(
                                """
                                        SELECT e
                                        FROM Government AS e
                                        WHERE e.headKind = :headKind
                                        ORDER BY e.country ASC""",
                                Government.class
                        )
                        .setParameter("headKind", headKind)
                        .getResultList();
            });
            verify(list, headKind);
        }

        @ValueSource(strings = {
                "PRESIDENT",
                "PRIME_MINISTER"
        })
        @ParameterizedTest
        void __CriteriaApi(final String headKind) {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                final var builder = em.getCriteriaBuilder();
                final var query = builder.createQuery(Government.class);
                final var root = query.from(Government.class);
                query.select(root)
                        .where(builder.equal(root.get(Government_.headKind), headKind))
                        .orderBy(builder.asc(root.get(Government_.country)));
                return em.createQuery(query).getResultList();
            });
            verify(list, headKind);
        }
    }
}
