package com.github.jinahya.headsofgovernments.persistence;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.jinahya.headsofgovernments.persistence.__Exercises_TestUtils.row;
import static com.github.jinahya.headsofgovernments.persistence.__Exercises_TestUtils.rows;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Answers the exercises of {@code src/test/sql/office_exercises_02_intermediate.sql}, against {@code
 * db/heads_of_governments.sqlite3}; a nested class per exercise, which answers it with JPQL and with the Criteria
 * API, or with a native query where neither can, and checks that it gives what the SQL of the file gives.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@Slf4j
class Office_Exercises_02_Intermediate_PersistenceTest {

    @DisplayName("exercise 1: Offices by the kind of head")
    @Nested
    class Exercise1Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("PRESIDENT", 3L, "FR, KR, US"),
                row("PRIME_MINISTER", 2L, "GB, JP")
        );

        // JPQL has no string aggregation; GROUP_CONCAT() is SQLite's own, and the Criteria API has none either
        @Test
        void nativeQuery() {
            final var list = __Exercises_TestUtils.apply(em -> (List<?>) em.createNativeQuery(
                            """
                                    SELECT head_kind, COUNT(*) AS offices, GROUP_CONCAT(country, ', ' ORDER BY country) AS countries
                                    FROM office
                                    GROUP BY head_kind
                                    ORDER BY head_kind ASC"""
                    )
                    .getResultList());
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("exercise 2: Titles outside ASCII")
    @Nested
    class Exercise2Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("GB", "Prime Minister", "ASCII"),
                row("US", "President", "ASCII"),
                row("FR", "Président de la République", "non-ASCII"),
                row("JP", "内閣総理大臣", "non-ASCII"),
                row("KR", "대통령", "non-ASCII")
        );

        // JPQL has no character class; LIKE matches no range of characters, GLOB '*[^ -~]*' is SQLite's own,
        // and the Criteria API has no pattern but LIKE's either
        @Test
        void nativeQuery() {
            final var list = __Exercises_TestUtils.apply(em -> (List<?>) em.createNativeQuery(
                            """
                                    SELECT country,
                                           head_title,
                                           CASE WHEN head_title GLOB '*[^ -~]*' THEN 'non-ASCII' ELSE 'ASCII' END AS script
                                    FROM office
                                    ORDER BY script ASC, country ASC"""
                    )
                    .getResultList());
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("exercise 3: Offices without an incumbent")
    @Nested
    class Exercise3Test {

        private static final List<List<Object>> EXPECTED = List.of();

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT o.country, o.headTitle
                                    FROM Office AS o
                                    WHERE NOT EXISTS (SELECT h
                                                      FROM Head AS h
                                                      WHERE h.office = o
                                                        AND h.termEnd IS NULL)
                                    ORDER BY o.country ASC""",
                            Object[].class
                    )
                    .getResultList());
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }

        @Test
        void __CriteriaApi() {
            final var list = __Exercises_TestUtils.apply(em -> {
                final var cb = em.getCriteriaBuilder();
                final var cq = cb.createQuery(Object[].class);
                final var o = cq.from(Office.class);
                final var incumbent = cq.subquery(Head.class);
                final var h = incumbent.from(Head.class);
                incumbent.select(h)
                        .where(cb.equal(h.get(Head_.office), o), cb.isNull(h.get(Head_.termEnd)));
                cq.select(cb.array(o.get(Office_.country), o.get(Office_.headTitle)))
                        .where(cb.not(cb.exists(incumbent)))
                        .orderBy(cb.asc(o.get(Office_.country)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }
}
