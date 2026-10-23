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
-- Exercises at the advanced level on head, a problem per query that reads head alone. Each problem states the request
-- it answers, then the SQL that answers it.
--
-- Parameters are bind variables (:name); the sqlite3 shell binds one with `.parameter set :name 'value'`, and an IDE
-- prompts for them when a statement runs. Dates are ISO-8601 text, so they compare as strings and date() and
-- julianday() read them directly; an incumbent's open term is measured to date('now'). A sort key that can be NULL
-- says NULLS FIRST or NULLS LAST, even where that is SQLite's default; a NOT NULL key does not.
--


-- --------------------------------------------------------------------------------------------- HOG-HEAD-A-01 Vacancies
-- Per country, each gap of more than a day between a term's end and the next term's start, with its length; a day
-- between is only how some sources write a succession, so it does not count.
SELECT country,
       previous_name,
       previous_term_end,
       name,
       term_start,
       CAST(julianday(term_start) - julianday(previous_term_end) AS INTEGER) - 1 AS days_vacant
FROM (SELECT country,
             name,
             term_start,
             LAG(name) OVER (PARTITION BY country ORDER BY term_start)     AS previous_name, LAG(term_end) OVER (PARTITION BY country ORDER BY term_start) AS previous_term_end
      FROM head)
WHERE julianday(term_start) - julianday(previous_term_end) > 1
ORDER BY country ASC, term_start ASC
;


-- ------------------------------------------------------------------------------------- HOG-HEAD-A-02 Longest in office
-- Per country, the person with the most days in office over all their terms, counting an open term to today; ties
-- share the place.
SELECT country, name, terms, days
FROM (SELECT country,
             name,
             COUNT(*)                                                                                  AS terms,
             CAST(SUM(julianday(COALESCE(term_end, date ('now'))) - julianday(term_start)) AS INTEGER) AS days,
             RANK()                                                                                       OVER (PARTITION BY country
                 ORDER BY SUM(julianday(COALESCE(term_end, date('now'))) - julianday(term_start)) DESC) AS place
      FROM head
      GROUP BY country, name)
WHERE place = 1
ORDER BY country ASC
;


-- -------------------------------------------------------------------------- HOG-HEAD-A-03 The longest run of one party
-- Per country, the longest unbroken run of consecutive terms held by the same party, with when it began and ended;
-- a run is counted in terms, not days, and a succession ('UNR/UDR') is a party of its own.
WITH ordered AS (SELECT country,
                        party,
                        term_start,
                        term_end,
                        ROW_NUMBER() OVER (PARTITION BY country ORDER BY term_start)        AS seq, ROW_NUMBER() OVER (PARTITION BY country, party ORDER BY term_start) AS seq_in_party
                 FROM head),
     runs AS (SELECT country,
                     party,
                     COUNT(*)                              AS terms,
                     MIN(term_start)                       AS since,
                     MAX(COALESCE(term_end, date ('now'))) AS until
              FROM ordered
              GROUP BY country, party, seq - seq_in_party),
     placed AS (SELECT *, RANK() OVER (PARTITION BY country ORDER BY terms DESC, since ASC) AS place
                FROM runs)
SELECT country, party, terms, since, until
FROM placed
WHERE place = 1
ORDER BY country ASC
;


-- -------------------------------------------------------------------- HOG-HEAD-A-04 Every party of a head, one per row
-- party may be a succession, 'A/B', when the party was renamed or changed under the head. Give the heads whose party
-- is one, with each party on its own row and its position in the succession.
WITH RECURSIVE parts(id, country, name, term_start, position, party, rest) AS
                   (SELECT id, country, name, term_start, 0, NULL, party || '/'
                    FROM head
                    WHERE party LIKE '%/%'
                    UNION ALL
                    SELECT id,
                           country,
                           name,
                           term_start,
                           position + 1,
                           SUBSTR(rest, 1, INSTR(rest, '/') - 1),
                           SUBSTR(rest, INSTR(rest, '/') + 1)
                    FROM parts
                    WHERE rest <> '')
SELECT country, name, term_start, position, party
FROM parts
WHERE position > 0
ORDER BY country ASC, term_start ASC, position ASC
;


-- ----------------------------------------------------------------------------- HOG-HEAD-A-05 Predecessor and successor
-- For a person named exactly, each of their terms with who held the office immediately before and after it in that
-- country; the incumbent has no successor.
SELECT country, number, term_start, term_end, predecessor, successor
FROM (SELECT country,
             number,
             name,
             term_start,
             term_end,
             LAG(name) OVER (PARTITION BY country ORDER BY term_start)  AS predecessor, LEAD(name) OVER (PARTITION BY country ORDER BY term_start) AS successor
      FROM head)
WHERE name = :name
ORDER BY country ASC, term_start ASC
;


-- --------------------------------------------------------------- HOG-HEAD-A-06 How often a new head brings a new party
-- Per country, over the successions where the head changed (not a head following themselves), how many also changed the
-- party, and that as a percentage, by country code. LAG gives the previous term's name and party; a succession ('A/B')
-- counts as a party of its own.
SELECT country,
       COUNT(*)                                                  AS successions,
       SUM(party <> previous_party)                              AS party_changes,
       ROUND(100.0 * SUM(party <> previous_party) / COUNT(*), 1) AS change_share
FROM (SELECT country,
             name,
             party,
             LAG(name) OVER (PARTITION BY country ORDER BY term_start)  AS previous_name,
             LAG(party) OVER (PARTITION BY country ORDER BY term_start) AS previous_party
      FROM head)
WHERE previous_name IS NOT NULL
  AND name <> previous_name
GROUP BY country
ORDER BY country ASC
;


-- --------------------------------------------------------------------------------------------- HOG-HEAD-A-07 Comebacks
-- A stint is an unbroken run of terms by one person; a head who left and came back has more than one. Give the heads
-- with three stints or more, with the year each stint began, most stints first and, at a tie, by country, then by the
-- first term. A term starts a stint when the term before it in that country was someone else's.
WITH marked AS (SELECT country,
                       name,
                       term_start,
                       LAG(name) OVER (PARTITION BY country ORDER BY term_start) AS previous_name
                FROM head)
SELECT country,
       name,
       COUNT(*)                                                         AS stints,
       GROUP_CONCAT(SUBSTR(term_start, 1, 4), ', ' ORDER BY term_start) AS stint_years
FROM marked
WHERE previous_name IS NULL
   OR previous_name <> name
GROUP BY country, name
HAVING COUNT(*) >= 3
ORDER BY stints DESC, country ASC, MIN(term_start) ASC
;
