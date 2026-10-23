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
-- Exercises at the advanced level on the whole schema, a problem per query that reads both tables. Each problem states
-- the request it answers, then the SQL that answers it.
--
-- Parameters are bind variables (:name); the sqlite3 shell binds one with `.parameter set :name 'value'`, and an IDE
-- prompts for them when a statement runs.
--


-- ------------------------------------------------------------------ HOG-SCHEMA-A-01 Each office's longest-serving head
-- Per office, the person with the most days in office over all their terms, with the title they held; ties
-- share the place.
SELECT country, head_title, name, terms, days
FROM (SELECT g.country,
             g.head_title,
             h.name,
             COUNT(*)                                                                                   AS terms,
             CAST(SUM(julianday(COALESCE(h.term_end, date('now'))) - julianday(h.term_start)) AS INTEGER) AS days,
             RANK() OVER (PARTITION BY g.country
                 ORDER BY SUM(julianday(COALESCE(h.term_end, date('now'))) - julianday(h.term_start)) DESC) AS place
      FROM office g
               JOIN head h ON h.country = g.country
      GROUP BY g.country, g.head_title, h.name)
WHERE place = 1
ORDER BY country ASC
;


-- ---------------------------------------------------------------- HOG-SCHEMA-A-02 Every office on a day, vacant or not
-- A reader gives a date and wants every office with its head on that day, or 'vacant' where the office was empty
-- or the list had not yet begun.
SELECT g.*, COALESCE(h.name, '(vacant)') AS name, h.*
FROM office g
         LEFT JOIN head h ON h.country = g.country
    AND h.term_start <= :date
    AND (h.term_end IS NULL OR h.term_end >= :date)
ORDER BY g.country ASC
;


-- ---------------------------------------------------------------------- HOG-SCHEMA-A-03 The incumbents' contemporaries
-- On the day each incumbent took office, who headed the other offices? One row per pair, the incumbent first.
SELECT i.*, o.*, g.*
FROM head i
         JOIN head o ON o.country <> i.country
    AND o.term_start <= i.term_start
    AND (o.term_end IS NULL OR o.term_end >= i.term_start)
         JOIN office g ON g.country = o.country
WHERE i.term_end IS NULL
ORDER BY i.country ASC, o.country ASC
;


-- -------------------------------------------------------------------- HOG-SCHEMA-A-04 Each office's longest-lived head
-- Per office, with its title, the head who lived the most years (died less born) among those who have died; ties share
-- the place and are listed by name.
SELECT country, head_title, name, born, died, age
FROM (SELECT DISTINCT g.country,
                      g.head_title,
                      h.name,
                      h.born,
                      h.died,
                      h.died - h.born                                                    AS age,
                      RANK() OVER (PARTITION BY g.country ORDER BY h.died - h.born DESC) AS place
      FROM office g
               JOIN head h ON h.country = g.country
      WHERE h.died IS NOT NULL)
WHERE place = 1
ORDER BY country ASC, name ASC
;


-- ---------------------------------------------------- HOG-SCHEMA-A-05 Presidents and the prime ministers of their time
-- For each president's term, how many different prime ministers, of any office, were in office at some point during it;
-- the ten terms with the most first and, at a tie, the earlier term first, then by country. Two terms overlap when each
-- starts on or before the other's end, the handover day counting for both; an open term ends at '9999-12-31'.
SELECT p.*, COUNT(DISTINCT m.country || ' ' || m.name) AS prime_ministers
FROM head p
         JOIN office pg ON pg.country = p.country AND pg.head_kind = 'PRESIDENT'
         JOIN head m ON m.term_start <= COALESCE(p.term_end, '9999-12-31')
    AND COALESCE(m.term_end, '9999-12-31') >= p.term_start
         JOIN office mg ON mg.country = m.country AND mg.head_kind = 'PRIME_MINISTER'
GROUP BY p.id
ORDER BY prime_ministers DESC, p.term_start ASC, p.country ASC
LIMIT 10
;


-- --------------------------------------------------------- HOG-SCHEMA-A-06 New heads per century, with a running total
-- Per office, with its title, how many persons first took office in each century, and the running total of persons up
-- to that century, by country, then by century. The century is the first two digits of the year plus one, so 1800
-- counts in the 19th; a name is taken as one person within a country.
WITH firsts AS (SELECT country, name, MIN(term_start) AS first_start
                FROM head
                GROUP BY country, name),
     per_century AS (SELECT g.country,
                            g.head_title,
                            CAST(SUBSTR(f.first_start, 1, 2) AS INTEGER) + 1 AS century,
                            COUNT(*)                                         AS new_persons
                     FROM office g
                              JOIN firsts f ON f.country = g.country
                     GROUP BY g.country, g.head_title, century)
SELECT country,
       head_title,
       century,
       new_persons,
       SUM(new_persons) OVER (PARTITION BY country ORDER BY century) AS persons_so_far
FROM per_century
ORDER BY country ASC, century ASC
;


-- --------------------------------------------------------- HOG-SCHEMA-A-07 Decades in which every office changed hands
-- The decades in which a term began in every office, with how many terms began in all, oldest first. This is relational
-- division: a decade qualifies when the number of distinct countries with a term begun in it equals the number of
-- offices.
SELECT SUBSTR(h.term_start, 1, 3) || '0s' AS decade, COUNT(*) AS terms
FROM head h
GROUP BY decade
HAVING COUNT(DISTINCT h.country) = (SELECT COUNT(*) FROM office)
ORDER BY decade ASC
;
