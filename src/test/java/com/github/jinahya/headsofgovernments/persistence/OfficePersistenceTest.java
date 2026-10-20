package com.github.jinahya.headsofgovernments.persistence;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
class OfficePersistenceTest {

    @DisplayName("EntityManager#find(entityClass, primaryKey)")
    @Nested
    class FindTest {

        @Nested
        class FetchTypeTest {

            @Test
            void __1() {
                final var found = PersistenceTestUtils.applyEntityManager(em -> {
                    try {
                        return em.find(Office.class, "FR");
                    } finally {
                        HibernateOrmUtils.logEntities(em);
                    }
                });
                assertThat(found)
                        .isNotNull()
                        .extracting(Office::getCountry)
                        .isEqualTo("FR");
            }

            @Test
            void __QueryLanguage() {
                final var found = PersistenceTestUtils.applyEntityManager(em -> {
                    try {
                        return em.createQuery(
                                        """
                                                SELECT e
                                                FROM Office AS e
                                                JOIN FETCH e.heads
                                                WHERE e.country = :country""",
                                        Office.class
                                )
                                .setParameter("country", "FR")
                                .getSingleResult();
                    } finally {
                        HibernateOrmUtils.logEntities(em);
                    }
                });
                assertThat(found)
                        .isNotNull()
                        .extracting(Office::getHeads)
                        .asInstanceOf(InstanceOfAssertFactories.LIST)
                        .hasSize(HeadPersistenceTest.HEAD_COUNTS.get("FR").intValue());
            }

            @Test
            void __CriteriaApi() {
                final var found = PersistenceTestUtils.applyEntityManager(em -> {
                    final var builder = em.getCriteriaBuilder();
                    final var query = builder.createQuery(Office.class);
                    final var root = query.from(Office.class);
                    root.fetch(Office_.heads);
                    query.select(root)
                            .where(builder.equal(root.get(Office_.country), "FR"));
                    try {
                        return em.createQuery(query).getSingleResult();
                    } finally {
                        HibernateOrmUtils.logEntities(em);
                    }
                });
                assertThat(found)
                        .isNotNull()
                        .extracting(Office::getHeads)
                        .asInstanceOf(InstanceOfAssertFactories.LIST)
                        .hasSize(HeadPersistenceTest.HEAD_COUNTS.get("FR").intValue());
            }
        }

        @ValueSource(strings = {"FR", "GB", "JP", "KR", "US"})
        @ParameterizedTest
        void __1(final String country) {
            final var found = PersistenceTestUtils.applyEntityManager(em -> {
                return em.find(Office.class, country);
            });
            assertThat(found)
                    .isNotNull()
                    .extracting(Office::getCountry)
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
                return em.find(Office.class, country);
            });
            if (found == null) {
                log.debug("no office found for {}", country);
                return;
            }
            assertThat(found)
                    .extracting(Office::getCountry)
                    .isEqualTo(country);
        }
    }

    @Nested
    class SelectRandomTest {

        @Test
        void __() {
            final var random = PersistenceTestUtils.selectRandom(Office.class);
            assumeThat(random).isNotEmpty();
            final var found = random.get();
        }
    }

    @DisplayName("SELECT e FROM Office AS e WHERE e.headKind = :headKind ORDER BY e.country ASC")
    @Nested
    class SelectAll_WhereHeadKindEqual_OrderByCountryAsc_Test {

        private static void verify(final List<Office> list, final String headKind) {
            assertThat(list)
                    .isNotEmpty()
                    .allSatisfy(e -> assertThat(e.getHeadKind()).isEqualTo(headKind))
                    .isSortedAccordingTo(Comparator.comparing(Office::getCountry));
        }

        @ValueSource(strings = {
                "PRESIDENT",
                "PRIME_MINISTER"
        })
        @ParameterizedTest
        void __NamedQuery(final String headKind) {
            final var list = PersistenceTestUtils.applyEntityManager(em -> {
                return em.createNamedQuery(
                                "Office.selectAll_WhereHeadKindEqual_OrderByCountryAsc",
                                Office.class
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

        @ValueSource(strings = {
                "PRESIDENT",
                "PRIME_MINISTER"
        })
        @ParameterizedTest
        void __CriteriaApi(final String headKind) {
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

    @DisplayName("Aggregate Functions in the SELECT Clause")
    @Nested
    class SelectAggregateTest {

        @DisplayName("SELECT e, COUNT(h) FROM Office AS e JOIN e.heads AS h GROUP BY e")
        @Nested
        class CountHeadsTest {

            private static void verify(final List<Object[]> list) {
                final var counts = list.stream().collect(Collectors.toMap(
                        r -> ((Office) r[0]).getCountry(),
                        r -> (Long) r[1]
                ));
                assertThat(counts).isEqualTo(HeadPersistenceTest.HEAD_COUNTS);
            }

            @Test
            void __NamedQuery() {
                final var list = PersistenceTestUtils.applyEntityManager(em -> {
                    return em.createNamedQuery("Office.selectAllAndCountOfHeads_GroupByAll", Object[].class)
                            .getResultList();
                });
                verify(list);
            }

            @Test
            void __QueryLanguage() {
                final var list = PersistenceTestUtils.applyEntityManager(em -> {
                    return em.createQuery(
                                    """
                                            SELECT e, COUNT(h)
                                            FROM Office AS e JOIN e.heads AS h
                                            GROUP BY e""",
                                    Object[].class
                            )
                            .getResultList();
                });
                verify(list);
            }

            @Test
            void __CriteriaApi() {
                final var list = PersistenceTestUtils.applyEntityManager(em -> {
                    final var builder = em.getCriteriaBuilder();
                    final var query = builder.createQuery(Object[].class);
                    final var root = query.from(Office.class);
                    final var heads = root.join(Office_.heads);
                    query.select(builder.array(root, builder.count(heads)))
                            .groupBy(root);
                    return em.createQuery(query).getResultList();
                });
                verify(list);
            }
        }

        @DisplayName("SELECT e, MIN(h.termStart), MAX(h.termStart) FROM Office AS e JOIN e.heads AS h GROUP BY e")
        @Nested
        class MinMaxTermStartOfHeadsTest {

            private static void verify(final List<Object[]> list) {
                final var ranges = list.stream().collect(Collectors.toMap(
                        r -> ((Office) r[0]).getCountry(),
                        r -> List.of((LocalDate) r[1], (LocalDate) r[2])
                ));
                assertThat(ranges).isEqualTo(HeadPersistenceTest.HEAD_TERM_START_RANGES);
            }

            @Test
            void __NamedQuery() {
                final var list = PersistenceTestUtils.applyEntityManager(em -> {
                    return em.createNamedQuery("Office.selectAllAndMinMaxTermStartOfHeads_GroupByAll", Object[].class)
                            .getResultList();
                });
                verify(list);
            }

            @Test
            void __QueryLanguage() {
                final var list = PersistenceTestUtils.applyEntityManager(em -> {
                    return em.createQuery(
                                    """
                                            SELECT e, MIN(h.termStart), MAX(h.termStart)
                                            FROM Office AS e JOIN e.heads AS h
                                            GROUP BY e""",
                                    Object[].class
                            )
                            .getResultList();
                });
                verify(list);
            }

            @Test
            void __CriteriaApi() {
                final var list = PersistenceTestUtils.applyEntityManager(em -> {
                    final var builder = em.getCriteriaBuilder();
                    final var query = builder.createQuery(Object[].class);
                    final var root = query.from(Office.class);
                    final var heads = root.join(Office_.heads);
                    final var termStart = heads.get(Head_.termStart);
                    query.select(builder.array(root, builder.least(termStart), builder.greatest(termStart)))
                            .groupBy(root);
                    return em.createQuery(query).getResultList();
                });
                verify(list);
            }
        }
    }
}
