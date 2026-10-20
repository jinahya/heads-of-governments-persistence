package com.github.jinahya.headsofgovernments.persistence;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
class HeadPersistenceTest {

    /**
     * The number of rows in the {@code head} table.
     */
    private static final int HEAD_COUNT = 285;

    /**
     * The number of rows in the {@code head} table, by {@code country}.
     */
    static final Map<String, Long> HEAD_COUNTS = Map.of(
            "FR", 31L,
            "GB", 81L,
            "JP", 105L,
            "KR", 21L,
            "US", 47L
    );

    /**
     * The earliest and the latest {@code term_start} in the {@code head} table, by {@code country}.
     */
    static final Map<String, List<String>> HEAD_TERM_START_RANGES = Map.of(
            "FR", List.of("1848-12-20", "2022-05-14"),
            "GB", List.of("1721-04-03", "2026-07-20"),
            "JP", List.of("1885-12-22", "2026-02-18"),
            "KR", List.of("1948-07-24", "2025-06-04"),
            "US", List.of("1789-04-30", "2025-01-20")
    );

    @DisplayName("EntityManager#find(entityClass, primaryKey)")
    @Nested
    class FindTest {

        @Nested
        class FetchTypeTest {

            // 1. Louis-Napoléon Bonaparte
            @Test
            void __1() {
                final var found = PersistenceTestUtils.applyEntityManager(em -> {
                    try {
                        return em.find(Head.class, 1L);
                    } finally {
                        HibernateOrmUtils.logEntities(em);
                    }
                });
                assertThat(found)
                        .isNotNull()
                        .extracting(Head::getName)
                        .isEqualTo("Louis-Napoléon Bonaparte");
            }

            @Test
            void __QueryLanguage() {
                final var found = PersistenceTestUtils.applyEntityManager(em -> {
                    try {
                        return em.createQuery(
                                        """
                                                SELECT e
                                                FROM Head AS e
                                                JOIN FETCH e.office
                                                WHERE e.id = :id""",
                                        Head.class
                                )
                                .setParameter("id", 1L)
                                .getSingleResult();
                    } finally {
                        HibernateOrmUtils.logEntities(em);
                    }
                });
                assertThat(found)
                        .isNotNull()
                        .extracting(Head::getName)
                        .isEqualTo("Louis-Napoléon Bonaparte");
            }
            @Test
            void __CriteriaApi() {
                final var found = PersistenceTestUtils.applyEntityManager(em -> {
                    final var builder = em.getCriteriaBuilder();
                    final var query = builder.createQuery(Head.class);
                    final var root = query.from(Head.class);
                    root.fetch(Head_.office);
                    query.select(root)
                            .where(builder.equal(root.get(Head_.id), 1L));
                    try {
                        return em.createQuery(query).getSingleResult();
                    } finally {
                        HibernateOrmUtils.logEntities(em);
                    }
                });
                assertThat(found)
                        .isNotNull()
                        .extracting(Head::getName)
                        .isEqualTo("Louis-Napoléon Bonaparte");
            }
        }

        @ValueSource(longs = {
                1,
                HEAD_COUNT
        })
        @ParameterizedTest
        void __1(final long id) {
            final var found = PersistenceTestUtils.applyEntityManager(em -> {
                return em.find(Head.class, id);
            });
            assertThat(found)
                    .isNotNull()
                    .extracting(Head::getId)
                    .isEqualTo(id);
        }

        @RepeatedTest(8)
        void __2() {
            final var random = PersistenceTestUtils.selectRandom(Head.class).orElseThrow();
            final var id = random.getId();
            final var found = PersistenceTestUtils.applyEntityManager(em -> {
                return em.find(Head.class, id);
            });
            assertThat(found)
                    .isNotNull()
                    .satisfies(h -> {
                        assertThat(h.getId()).isEqualTo(id);
                        assertThat(h.getNumber()).isEqualTo(random.getNumber());
                        assertThat(h.getTermStart()).isEqualTo(random.getTermStart());
                        assertThat(h.getTermEnd()).isEqualTo(random.getTermEnd());
                        assertThat(h.getName()).isEqualTo(random.getName());
                        assertThat(h.getParty()).isEqualTo(random.getParty());
                        assertThat(h.getBorn()).isEqualTo(random.getBorn());
                        assertThat(h.getDied()).isEqualTo(random.getDied());
                        assertThat(h.getNumber()).isPositive();
                        assertThat(h.getTermStart()).isNotBlank();
                        if (h.getTermEnd() != null) {
                            assertThat(h.getTermEnd()).isGreaterThanOrEqualTo(h.getTermStart());
                        }
                        assertThat(h.getName()).isNotBlank();
                        assertThat(h.getParty()).isNotBlank();
                        assertThat(h.getBorn()).isPositive();
                        if (h.getDied() != null) {
                            assertThat(h.getDied()).isGreaterThanOrEqualTo(h.getBorn());
                        }
                    });
        }

        @ValueSource(longs = {
                0,
                HEAD_COUNT + 1
        })
        @ParameterizedTest
        void _Null_Absent(final long id) {
            final var found = PersistenceTestUtils.applyEntityManager(em -> {
                return em.find(Head.class, id);
            });
            assertThat(found).isNull();
        }

        @Test
        void _Office_Walkable() {
            PersistenceTestUtils.applyEntityManager(em -> {
                final var head = em.find(Head.class, 1L);
                assertThat(head).isNotNull();
                final var office = head.getOffice();
                assertThat(office).isNotNull();
                assertThat(office.getHeads())
                        .isNotEmpty()
                        .anySatisfy(h -> assertThat(h.getId()).isEqualTo(head.getId()));
                return null;
            });
        }
    }

    @Nested
    class SelectRandomTest {

        @Test
        void __() {
            final var random = PersistenceTestUtils.selectRandom(Head.class);
            assumeThat(random).isNotEmpty();
            final var found = random.get();
        }
    }

    @DisplayName("SELECT e FROM Head AS e WHERE e.office = :office ORDER BY e.termStart ASC")
    @Nested
    class SelectAll_WhereOfficeEqual_OrderByTermStartAsc_Test {

        private static void verify(final List<Head> list, final Office office) {
            assertThat(list)
                    .isNotEmpty()
                    .allSatisfy(e -> assertThat(e.getOffice().getCountry()).isEqualTo(office.getCountry()))
                    .isSortedAccordingTo(Comparator.comparing(Head::getTermStart));
        }

        @ValueSource(strings = {
                "FR",
                "GB",
                "JP",
                "KR",
                "US"
        })
        @ParameterizedTest
        void __NamedQuery(final String country) {
            PersistenceTestUtils.applyEntityManager(em -> {
                final var office = em.find(Office.class, country);
                assertThat(office).isNotNull();
                final var list = em.createNamedQuery(
                                "Head.selectAll_WhereOfficeEqual_OrderByTermStartAsc",
                                Head.class
                        )
                        .setParameter("office", office)
                        .getResultList();
                verify(list, office);
                return null;
            });
        }

        @ValueSource(strings = {
                "FR",
                "GB",
                "JP",
                "KR",
                "US"
        })
        @ParameterizedTest
        void __QueryLanguage(final String country) {
            PersistenceTestUtils.applyEntityManager(em -> {
                final var office = em.find(Office.class, country);
                assertThat(office).isNotNull();
                final var list = em.createQuery(
                                """
                                        SELECT e
                                        FROM Head AS e
                                        WHERE e.office = :office
                                        ORDER BY e.termStart ASC""",
                                Head.class
                        )
                        .setParameter("office", office)
                        .getResultList();
                verify(list, office);
                return null;
            });
        }

        @ValueSource(strings = {
                "FR",
                "GB",
                "JP",
                "KR",
                "US"
        })
        @ParameterizedTest
        void __CriteriaApi(final String country) {
            PersistenceTestUtils.applyEntityManager(em -> {
                final var office = em.find(Office.class, country);
                assertThat(office).isNotNull();
                final var builder = em.getCriteriaBuilder();
                final var query = builder.createQuery(Head.class);
                final var root = query.from(Head.class);
                query.select(root)
                        .where(builder.equal(root.get(Head_.office), office))
                        .orderBy(builder.asc(root.get(Head_.termStart)));
                final var list = em.createQuery(query).getResultList();
                verify(list, office);
                return null;
            });
        }
    }

    @DisplayName("SELECT COUNT(e) FROM Head AS e")
    @Nested
    class CountTest {

        @Test
        void __NamedQuery() {
            final var count = PersistenceTestUtils.applyEntityManager(em -> {
                return em.createNamedQuery("Head.selectCount", Long.class)
                        .getSingleResult();
            });
            assertThat(count).isEqualTo(HEAD_COUNT);
        }

        @Test
        void __QueryLanguage() {
            final var count = PersistenceTestUtils.applyEntityManager(em -> {
                return em.createQuery(
                                """
                                        SELECT COUNT(e)
                                        FROM Head AS e""",
                                Long.class
                        )
                        .getSingleResult();
            });
            assertThat(count).isEqualTo(HEAD_COUNT);
        }

        @Test
        void __CriteriaApi() {
            final var count = PersistenceTestUtils.applyEntityManager(em -> {
                final var builder = em.getCriteriaBuilder();
                final var query = builder.createQuery(Long.class);
                final var root = query.from(Head.class);
                query.select(builder.count(root));
                return em.createQuery(query).getSingleResult();
            });
            assertThat(count).isEqualTo(HEAD_COUNT);
        }

        @Test
        void __PersistenceTestUtils() {
            final var count = PersistenceTestUtils.selectCount(Head.class);
            assertThat(count).isEqualTo(HEAD_COUNT);
        }
    }

    @DisplayName("Aggregate Functions in the SELECT Clause")
    @Nested
    class SelectAggregateTest {

        @DisplayName("SELECT e.office, COUNT(e) FROM Head AS e GROUP BY e.office")
        @Nested
        class CountGroupByOfficeTest {

            private static void verify(final List<Object[]> list) {
                final var counts = list.stream().collect(Collectors.toMap(
                        r -> ((Office) r[0]).getCountry(),
                        r -> (Long) r[1]
                ));
                assertThat(counts).isEqualTo(HEAD_COUNTS);
            }

            @Test
            void __NamedQuery() {
                final var list = PersistenceTestUtils.applyEntityManager(em -> {
                    return em.createNamedQuery("Head.selectOfficeAndCount_GroupByOffice", Object[].class)
                            .getResultList();
                });
                verify(list);
            }

            @Test
            void __QueryLanguage() {
                final var list = PersistenceTestUtils.applyEntityManager(em -> {
                    return em.createQuery(
                                    """
                                            SELECT e.office, COUNT(e)
                                            FROM Head AS e
                                            GROUP BY e.office""",
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
                    final var root = query.from(Head.class);
                    final var office = root.get(Head_.office);
                    query.select(builder.array(office, builder.count(root)))
                            .groupBy(office);
                    return em.createQuery(query).getResultList();
                });
                verify(list);
            }
        }

        @DisplayName("SELECT e.office, MIN(e.termStart), MAX(e.termStart) FROM Head AS e GROUP BY e.office")
        @Nested
        class MinMaxTermStartGroupByOfficeTest {

            private static void verify(final List<Object[]> list) {
                final var ranges = list.stream().collect(Collectors.toMap(
                        r -> ((Office) r[0]).getCountry(),
                        r -> List.of((String) r[1], (String) r[2])
                ));
                assertThat(ranges).isEqualTo(HEAD_TERM_START_RANGES);
            }

            @Test
            void __NamedQuery() {
                final var list = PersistenceTestUtils.applyEntityManager(em -> {
                    return em.createNamedQuery("Head.selectOfficeAndMinMaxTermStart_GroupByOffice", Object[].class)
                            .getResultList();
                });
                verify(list);
            }

            @Test
            void __QueryLanguage() {
                final var list = PersistenceTestUtils.applyEntityManager(em -> {
                    return em.createQuery(
                                    """
                                            SELECT e.office, MIN(e.termStart), MAX(e.termStart)
                                            FROM Head AS e
                                            GROUP BY e.office""",
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
                    final var root = query.from(Head.class);
                    final var office = root.get(Head_.office);
                    final var termStart = root.get(Head_.termStart);
                    query.select(builder.array(office, builder.least(termStart), builder.greatest(termStart)))
                            .groupBy(office);
                    return em.createQuery(query).getResultList();
                });
                verify(list);
            }
        }
    }
}
