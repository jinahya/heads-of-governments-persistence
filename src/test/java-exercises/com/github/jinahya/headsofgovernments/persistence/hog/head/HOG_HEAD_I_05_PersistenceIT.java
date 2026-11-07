package com.github.jinahya.headsofgovernments.persistence.hog.head;

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

import com.github.jinahya.headsofgovernments.persistence.Head;
import com.github.jinahya.headsofgovernments.persistence.Head_;
import com.github.jinahya.headsofgovernments.persistence.exercises.__Exercises_TestUtils;
import jakarta.persistence.criteria.Nulls;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.jinahya.headsofgovernments.persistence.exercises.__Exercises_TestUtils.row;
import static com.github.jinahya.headsofgovernments.persistence.exercises.__Exercises_TestUtils.rows;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Answers {@code HOG_HEAD_I_05} of {@code src/test/sql-exercises/head/02_intermediate.sql} with
 * JPQL and with the Criteria API, or with a native query where neither can.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@DisplayName("HOG_HEAD_I_05: Living former heads")
class HOG_HEAD_I_05_PersistenceIT {

    private static final List<List<Object>> EXPECTED = List.of(
            row("FR", "François Hollande", 1954L, "2017-05-14"),
            row("FR", "Nicolas Sarkozy", 1955L, "2012-05-15"),
            row("GB", "Keir Starmer", 1962L, "2026-07-20"),
            row("GB", "Rishi Sunak", 1980L, "2024-07-05"),
            row("GB", "Liz Truss", 1975L, "2022-10-25"),
            row("GB", "Boris Johnson", 1964L, "2022-09-06"),
            row("GB", "Theresa May", 1956L, "2019-07-24"),
            row("GB", "David Cameron", 1966L, "2016-07-13"),
            row("GB", "Gordon Brown", 1951L, "2010-05-11"),
            row("GB", "Tony Blair", 1953L, "2007-06-27"),
            row("GB", "John Major", 1943L, "1997-05-02"),
            row("JP", "石破茂", 1957L, "2025-10-21"),
            row("JP", "岸田文雄", 1957L, "2024-10-01"),
            row("JP", "菅義偉", 1948L, "2021-10-04"),
            row("JP", "野田佳彦", 1957L, "2012-12-26"),
            row("JP", "菅直人", 1946L, "2011-09-02"),
            row("JP", "鳩山由紀夫", 1947L, "2010-06-08"),
            row("JP", "麻生太郎", 1940L, "2009-09-16"),
            row("JP", "福田康夫", 1936L, "2008-09-24"),
            row("JP", "小泉純一郎", 1942L, "2006-09-26"),
            row("JP", "森喜朗", 1937L, "2001-04-26"),
            row("JP", "細川護熙", 1938L, "1994-04-28"),
            row("KR", "윤석열", 1960L, "2025-04-04"),
            row("KR", "문재인", 1953L, "2022-05-09"),
            row("KR", "박근혜", 1952L, "2017-03-10"),
            row("KR", "이명박", 1941L, "2013-02-24"),
            row("US", "Joe Biden", 1942L, "2025-01-20"),
            row("US", "Barack Obama", 1961L, "2017-01-20"),
            row("US", "George W. Bush", 1946L, "2009-01-20"),
            row("US", "Bill Clinton", 1946L, "2001-01-20")
    );

    // selects no entity: a row is a person, a group of the terms of one, not a head
    @Test
    void __QueryLanguage() {
        final var list = __Exercises_TestUtils.apply(em -> em.createQuery(
                        """
                                SELECT h.country, h.name, h.born, MAX(h.termEnd) AS lastTermEnd
                                FROM Head AS h
                                WHERE h.died IS NULL
                                GROUP BY h.country, h.name, h.born
                                HAVING COUNT(h) = COUNT(h.termEnd)
                                ORDER BY h.country ASC, lastTermEnd DESC NULLS LAST""",
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
            final var h = cq.from(Head.class);
            final var lastTermEnd = cb.greatest(h.get(Head_.termEnd));
            cq.select(cb.array(h.get(Head_.country), h.get(Head_.name), h.get(Head_.born),
                               lastTermEnd))
                    .where(cb.isNull(h.get(Head_.died)))
                    .groupBy(h.get(Head_.country), h.get(Head_.name), h.get(Head_.born))
                    .having(cb.equal(cb.count(h), cb.count(h.get(Head_.termEnd))))
                    .orderBy(cb.asc(h.get(Head_.country)), cb.desc(lastTermEnd, Nulls.LAST));
            return em.createQuery(cq)
                    .getResultList();
        });
        assertThat(rows(list)).containsExactlyElementsOf(EXPECTED);
    }

}
