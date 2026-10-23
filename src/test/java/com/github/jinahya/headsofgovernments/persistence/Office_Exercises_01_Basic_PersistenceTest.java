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
 * Answers the exercises of {@code src/test/sql/office_exercises_01_basic.sql}, against {@code
 * db/heads_of_governments.sqlite3}; a nested class per exercise, which answers it with JPQL and with the Criteria
 * API, or with a native query where neither can, and checks that it gives what the SQL of the file gives.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@Slf4j
class Office_Exercises_01_Basic_PersistenceTest {

    @DisplayName("exercise 1: An office by country")
    @Nested
    class Exercise1Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("KR", "PRESIDENT", "대통령", "대, one per election; a person re-elected keeps one line per term with consecutive numbers")
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT o.country, o.headKind, o.headTitle, o.numbering
                                    FROM Office AS o
                                    WHERE o.country = UPPER(:country)""",
                            Object[].class
                    )
                    .setParameter("country", "kr")
                    .getResultList());
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }

        @Test
        void __CriteriaApi() {
            final var list = __Exercises_TestUtils.apply(em -> {
                final var cb = em.getCriteriaBuilder();
                final var cq = cb.createQuery(Object[].class);
                final var o = cq.from(Office.class);
                cq.select(cb.array(o.get(Office_.country), o.get(Office_.headKind), o.get(Office_.headTitle),
                                   o.get(Office_.numbering)))
                        .where(cb.equal(o.get(Office_.country), cb.upper(cb.parameter(String.class, "country"))));
                return em.createQuery(cq)
                        .setParameter("country", "kr")
                        .getResultList();
            });
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("exercise 2: The offices")
    @Nested
    class Exercise2Test {

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
                                    SELECT o.country, o.headKind, o.headTitle
                                    FROM Office AS o
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
                cq.select(cb.array(o.get(Office_.country), o.get(Office_.headKind), o.get(Office_.headTitle)))
                        .orderBy(cb.asc(o.get(Office_.country)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }

    @DisplayName("exercise 3: Offices where a number can repeat")
    @Nested
    class Exercise3Test {

        private static final List<List<Object>> EXPECTED = List.of(
                row("FR", "Président de la République", "ordinal of the person by first election; a person re-elected has one line per term and keeps the same number on each"),
                row("GB", "Prime Minister", "ordinal of the person by first appointment; a person who served non-consecutive terms has one line per term and keeps the same number on each")
        );

        @Test
        void __QueryLanguage() {
            final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                            """
                                    SELECT o.country, o.headTitle, o.numbering
                                    FROM Office AS o
                                    WHERE o.numbering LIKE '%keeps the same number%'
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
                cq.select(cb.array(o.get(Office_.country), o.get(Office_.headTitle), o.get(Office_.numbering)))
                        .where(cb.like(o.get(Office_.numbering), "%keeps the same number%"))
                        .orderBy(cb.asc(o.get(Office_.country)));
                return em.createQuery(cq)
                        .getResultList();
            });
            assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
        }
    }
}
