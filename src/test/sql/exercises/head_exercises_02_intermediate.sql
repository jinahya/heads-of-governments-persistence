-- #%L
-- heads-of-governments-persistence
-- %%
-- Copyright (C) 2026 Jinahya, Inc.
-- %%
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--      http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- #L%
---
--
-- Exercises at the intermediate level on head, a problem per query that reads head alone. Each problem states the
-- request it answers, then the SQL that answers it.
--
-- Parameters are bind variables (:name); the sqlite3 shell binds one with `.parameter set :name 'value'`, and an IDE
-- prompts for them when a statement runs. Dates are ISO-8601 text, so they compare as strings and date() and
-- julianday() read them directly; an incumbent's open term is measured to date('now'). A sort key that can be NULL
-- says NULLS FIRST or NULLS LAST, even where that is SQLite's default; a NOT NULL key does not.
--


-- ------------------------------------------------------------------------ HOG_HEAD_I_01 The longest terms of a country
-- For one country, each term with its length in days, longest first; the incumbent's term runs to today.
SELECT *,
       CAST(julianday(COALESCE(term_end, date('now'))) - julianday(term_start) AS INTEGER) AS days
FROM head
WHERE country = :country
ORDER BY days DESC, term_start ASC
;


-- ---------------------------------------------------------------------------------- HOG_HEAD_I_03 Age on taking office
-- Per country, the youngest, the oldest and the average age at which a term began, in whole years of the calendar
-- (the year of term_start less the year of birth; born is a year, so that is as fine as the data allows).
SELECT g.*,
       MIN(CAST(SUBSTR(h.term_start, 1, 4) AS INTEGER) - h.born)           AS youngest,
       MAX(CAST(SUBSTR(h.term_start, 1, 4) AS INTEGER) - h.born)           AS oldest,
       ROUND(AVG(CAST(SUBSTR(h.term_start, 1, 4) AS INTEGER) - h.born), 1) AS average
FROM head h
         JOIN office g ON g.country = h.country
GROUP BY g.country
ORDER BY g.country ASC
;


-- --------------------------------------------------------------------------------------- HOG_HEAD_I_04 Terms per party
-- For one country, each party with how many terms it held and how many different persons, most terms first. A party
-- written as a succession ('RPR/UMP') counts as its own value.
SELECT party, COUNT(*) AS terms, COUNT(DISTINCT name) AS persons
FROM head
WHERE country = :country
GROUP BY party
ORDER BY terms DESC, party ASC
;


-- ----------------------------------------------------------------------------------- HOG_HEAD_I_05 Living former heads
-- Per country, the former heads still living, with the day their last term ended, most recent first.
SELECT country, name, born, MAX(term_end) AS last_term_end
FROM head
WHERE died IS NULL
GROUP BY country, name
HAVING MAX(term_end IS NULL) = 0
ORDER BY country ASC, last_term_end DESC NULLS LAST
;


-- -------------------------------------------------------------------------------- HOG_HEAD_I_06 Terms begun per decade
-- Across all countries, how many terms began in each decade, with the countries that had one.
SELECT SUBSTR(term_start, 1, 3) || '0s' AS decade,
       COUNT(*)                         AS terms,
       GROUP_CONCAT(DISTINCT country)   AS countries
FROM head
GROUP BY decade
ORDER BY decade ASC
;


-- -------------------------------------------------------- HOG_HEAD_I_08 The first and the latest terms of each country
-- Per country, its first :n terms and its most recent :n, each row saying which it is. A row is 'first' when it is
-- among the first :n, so with a country of 2 * :n terms or fewer the two sets overlap and 'first' wins.
SELECT *,
       CASE WHEN first_rn <= :n THEN 'first' ELSE 'latest' END AS which
FROM (SELECT h.*,
             ROW_NUMBER() OVER (PARTITION BY country ORDER BY term_start ASC)  AS first_rn,
             ROW_NUMBER() OVER (PARTITION BY country ORDER BY term_start DESC) AS latest_rn
      FROM head AS h)
WHERE first_rn <= :n
   OR latest_rn <= :n
ORDER BY country ASC, term_start ASC
;


-- ------------------------------------------------------------------- HOG_HEAD_I_09 The first two terms of each country
-- Per country, its first two terms, the earlier first. Terms, not persons: a head whose first two terms were
-- consecutive appears twice. term_start is NOT NULL and distinct within a country, so the numbering has no ties.
SELECT *
FROM (SELECT h.*, ROW_NUMBER() OVER (PARTITION BY country ORDER BY term_start ASC) AS first_rn
      FROM head AS h)
WHERE first_rn <= 2
ORDER BY country ASC, term_start ASC
;

-- ---------------------------------------------------------- HOG_HEAD_I_10 Two or more terms, the top three per country
-- Per country, up to three heads with two or more terms, most terms first and, at a tie, the earlier first term first;
-- a name is taken as one person.
SELECT country, name, terms
FROM (SELECT country,
             name,
             COUNT(*)                                                                             AS terms,
             ROW_NUMBER() OVER (PARTITION BY country ORDER BY COUNT(*) DESC, MIN(term_start) ASC) AS place
      FROM head
      GROUP BY country, name
      HAVING COUNT(*) >= 2)
WHERE place <= 3
ORDER BY country ASC, place ASC
;


-- --------------------------------------------- HOG_HEAD_I_11 Party share of completed terms, the top three per country
-- Per country, over the terms that have ended, each party's share of the days in office as a percentage of that
-- country's total (its tenure share), the top three parties; a succession ('A/B') counts as a party of its own.
SELECT country, party, tenure_share
FROM (SELECT country,
             party,
             ROUND(100.0 * SUM(julianday(term_end) - julianday(term_start))
                       / SUM(SUM(julianday(term_end) - julianday(term_start)))
                             OVER (PARTITION BY country),
                   1)                                                            AS tenure_share,
             ROW_NUMBER() OVER (PARTITION BY country
                 ORDER BY SUM(julianday(term_end) - julianday(term_start)) DESC) AS place
      FROM head
      WHERE term_end IS NOT NULL
      GROUP BY country, party)
WHERE place <= 3
ORDER BY country ASC, tenure_share DESC
;


-- ------------------------------------------------------------ HOG_HEAD_I_12 Terms that ended in the year the head died
-- Per country, how many terms ended in the same year their head died, by country code. It catches the heads who died in
-- office, but also those who left office and died later that year; died is a year, so the data cannot tell the two
-- apart.
SELECT g.*, COUNT(*) AS terms
FROM head h
         JOIN office g ON g.country = h.country
WHERE h.died = CAST(SUBSTR(h.term_end, 1, 4) AS INTEGER)
GROUP BY g.country
ORDER BY g.country ASC
;


-- ----------------------------------------------------------------------- HOG_HEAD_I_13 Terms begun per day of the week
-- Across all countries, how many terms began on each day of the week, Sunday first. strftime('%w', ...) gives the day
-- as a digit, Sunday as 0; a CASE turns it into a name.
SELECT CASE strftime('%w', term_start)
           WHEN '0' THEN 'Sunday'
           WHEN '1' THEN 'Monday'
           WHEN '2' THEN 'Tuesday'
           WHEN '3' THEN 'Wednesday'
           WHEN '4' THEN 'Thursday'
           WHEN '5' THEN 'Friday'
           ELSE 'Saturday'
           END  AS weekday,
       COUNT(*) AS terms
FROM head
GROUP BY strftime('%w', term_start)
ORDER BY strftime('%w', term_start) ASC
;


-- ---------------------------------------------------------------------- HOG_HEAD_I_14 Years lived after leaving office
-- For the heads who have died, the years between the end of their last term and their death, the ten longest first. A
-- name is taken as one person within a country; at a tie, by country, then by name.
SELECT country,
       name,
       MAX(term_end)                                       AS last_term_end,
       died,
       died - CAST(SUBSTR(MAX(term_end), 1, 4) AS INTEGER) AS years_after
FROM head
WHERE died IS NOT NULL
GROUP BY country, name, died
ORDER BY years_after DESC NULLS LAST, country ASC, name ASC
LIMIT 10
;


-- ------------------------------------------------------- HOG_HEAD_I_15 Years with a new term in four or more countries
-- The years in which a term began in four or more of the countries, with how many terms began and in how many
-- countries, oldest first.
SELECT SUBSTR(term_start, 1, 4) AS year, COUNT(*) AS terms, COUNT(DISTINCT country) AS countries
FROM head
GROUP BY SUBSTR(term_start, 1, 4)
HAVING COUNT(DISTINCT country) >= 4
ORDER BY year ASC
;


-- ------------------------------------------------------------------------ HOG_HEAD_I_16 Overlapping terms in a country
-- Within a country, a term should not begin before the one before it has ended. List each pair of terms of a country
-- where the later begins before the earlier ends, an open term running to '9999-12-31', by country, then by the earlier
-- term; the check passes when nothing comes back. A handover day, the end of one term and the start of the next, is not
-- an overlap.
SELECT a.*, b.id AS later_id, b.name AS later_name, b.term_start AS later_term_start
FROM head a
         JOIN head b ON b.country = a.country
    AND b.term_start > a.term_start
    AND b.term_start < COALESCE(a.term_end, '9999-12-31')
ORDER BY a.country ASC, a.term_start ASC, b.term_start ASC
;


-- ------------------------------------------ HOG_HEAD_I_17 Terms begun fifteen years younger than the country's average
-- The terms whose head, in years of the calendar, was fifteen or more years younger than the average age at which that
-- country's terms began, by country, then in date order. A correlated subquery gives each term its own country's
-- average.
SELECT h.*, CAST(SUBSTR(h.term_start, 1, 4) AS INTEGER) - h.born AS age
FROM head h
WHERE CAST(SUBSTR(h.term_start, 1, 4) AS INTEGER) - h.born
          <= (SELECT AVG(CAST(SUBSTR(s.term_start, 1, 4) AS INTEGER) - s.born)
              FROM head s
              WHERE s.country = h.country) - 15
ORDER BY h.country ASC, h.term_start ASC
;
