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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
class HeadPersistenceTest {

    /**
     * The number of rows in the {@code head} table.
     */
    private static final int HEAD_COUNT = 285;

    @DisplayName("EntityManager#find(entityClass, primaryKey)")
    @Nested
    class FindTest {

        @ValueSource(ints = {
                1,
                HEAD_COUNT
        })
        @ParameterizedTest
        void __1(final int id) {
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

        @ValueSource(ints = {
                0,
                HEAD_COUNT + 1
        })
        @ParameterizedTest
        void _Null_Absent(final int id) {
            final var found = PersistenceTestUtils.applyEntityManager(em -> {
                return em.find(Head.class, id);
            });
            assertThat(found).isNull();
        }

        @Test
        void _Government_Walkable() {
            PersistenceTestUtils.applyEntityManager(em -> {
                final var head = em.find(Head.class, 1);
                assertThat(head).isNotNull();
                final var government = head.getGovernment();
                assertThat(government).isNotNull();
                assertThat(government.getHeads())
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

    @DisplayName("SELECT e FROM Head AS e WHERE e.government = :government ORDER BY e.termStart ASC")
    @Nested
    class SelectAll_WhereGovernmentEqual_OrderByTermStartAsc_Test {

        private static void verify(final List<Head> list, final Government government) {
            assertThat(list)
                    .isNotEmpty()
                    .allSatisfy(e -> assertThat(e.getGovernment().getCountry()).isEqualTo(government.getCountry()))
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
                final var government = em.find(Government.class, country);
                assertThat(government).isNotNull();
                final var list = em.createNamedQuery(
                                "Head.selectAll_WhereGovernmentEqual_OrderByTermStartAsc",
                                Head.class
                        )
                        .setParameter("government", government)
                        .getResultList();
                verify(list, government);
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
                final var government = em.find(Government.class, country);
                assertThat(government).isNotNull();
                final var list = em.createQuery(
                                """
                                        SELECT e
                                        FROM Head AS e
                                        WHERE e.government = :government
                                        ORDER BY e.termStart ASC""",
                                Head.class
                        )
                        .setParameter("government", government)
                        .getResultList();
                verify(list, government);
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
                final var government = em.find(Government.class, country);
                assertThat(government).isNotNull();
                final var builder = em.getCriteriaBuilder();
                final var query = builder.createQuery(Head.class);
                final var root = query.from(Head.class);
                query.select(root)
                        .where(builder.equal(root.get(Head_.government), government))
                        .orderBy(builder.asc(root.get(Head_.termStart)));
                final var list = em.createQuery(query).getResultList();
                verify(list, government);
                return null;
            });
        }
    }

    @DisplayName("SELECT COUNT(e) FROM Head AS e")
    @Nested
    class CountTest {

        @Test
        void __() {
            final var count = PersistenceTestUtils.selectCount(Head.class);
            assertThat(count).isEqualTo(HEAD_COUNT);
        }
    }
}
