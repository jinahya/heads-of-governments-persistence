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
-- Exercises at the intermediate level on the whole schema, a problem per query that reads both tables. Each problem
-- states the request it answers, then the SQL that answers it.
--
-- Parameters are bind variables (:name); the sqlite3 shell binds one with `.parameter set :name 'value'`, and an IDE
-- prompts for them when a statement runs.
--


-- ------------------------------------------------------------------ HOG_SCHEMA_I_01 Presidents against prime ministers
-- By the kind of head: how many terms, how many different persons, and the average length of a term in days, an open
-- term counted to today.
SELECT g.head_kind,
       COUNT(*)                                      AS terms,
       COUNT(DISTINCT g.country || ' ' || h.name)    AS persons,
       CAST(AVG(julianday(COALESCE(h.term_end, date('now'))) -
                julianday(h.term_start)) AS INTEGER) AS average_days
FROM office g
         JOIN head h ON h.country = g.country
GROUP BY g.head_kind
ORDER BY g.head_kind ASC
;


-- ------------------------------------------------------------------------------- HOG_SCHEMA_I_02 Numbering in practice
-- Per office: how many terms, how many distinct numbers, the highest number, and the rule; where terms exceed
-- numbers, number is the person's and repeats.
SELECT g.*,
       COUNT(*)                 AS terms,
       COUNT(DISTINCT h.number) AS numbers,
       MAX(h.number)            AS highest
FROM office g
         JOIN head h ON h.country = g.country
GROUP BY g.country
ORDER BY g.country ASC
;


-- ------------------------------------------------------------------------------- HOG_SCHEMA_I_03 The span of each list
-- Per office, the first term's start, the latest term's start and the years between them, with the head's title.
SELECT g.*,
       MIN(h.term_start)                                      AS first_term_start,
       MAX(h.term_start)                                      AS latest_term_start,
       CAST(SUBSTR(MAX(h.term_start), 1, 4) AS INTEGER)
           - CAST(SUBSTR(MIN(h.term_start), 1, 4) AS INTEGER) AS years
FROM office g
         JOIN head h ON h.country = g.country
GROUP BY g.country
ORDER BY g.country ASC
;


-- ----------------------------------------------------------------------------- HOG_SCHEMA_I_04 Living heads per office
-- Per office, with its title, how many of its heads are still living, the incumbent included, by country code. A name
-- is taken as one person within a country.
SELECT g.*, COUNT(DISTINCT h.name) AS living
FROM office g
         JOIN head h ON h.country = g.country
WHERE h.died IS NULL
GROUP BY g.country
ORDER BY g.country ASC
;


-- --------------------------------------------------------------------- HOG_SCHEMA_I_05 Terms per century, side by side
-- Per office, with its title, the terms begun in each century from the 18th to the 21st, one column per century, by
-- country code. A SUM of CASE per column turns the rows of a GROUP BY into columns.
SELECT g.*,
       SUM(CASE WHEN h.term_start < '1800' THEN 1 ELSE 0 END)                            AS c18,
       SUM(CASE WHEN h.term_start >= '1800' AND h.term_start < '1900' THEN 1 ELSE 0 END) AS c19,
       SUM(CASE WHEN h.term_start >= '1900' AND h.term_start < '2000' THEN 1 ELSE 0 END) AS c20,
       SUM(CASE WHEN h.term_start >= '2000' THEN 1 ELSE 0 END)                           AS c21
FROM office g
         JOIN head h ON h.country = g.country
GROUP BY g.country
ORDER BY g.country ASC
;


-- ----------------------------------------------------- HOG_SCHEMA_I_06 Age at first taking office, by the kind of head
-- By the kind of head: how many persons, and the youngest, the oldest and the average age at which each first took
-- office, in years of the calendar. Each person counts once, at their first term, unlike HOG_HEAD_I_03, which counts
-- every term; a name is taken as one person within a country.
SELECT g.head_kind,
       COUNT(*)                                                             AS persons,
       MIN(CAST(SUBSTR(f.first_start, 1, 4) AS INTEGER) - f.born)           AS youngest,
       MAX(CAST(SUBSTR(f.first_start, 1, 4) AS INTEGER) - f.born)           AS oldest,
       ROUND(AVG(CAST(SUBSTR(f.first_start, 1, 4) AS INTEGER) - f.born), 1) AS average
FROM office g
         JOIN (SELECT country, name, born, MIN(term_start) AS first_start
               FROM head
               GROUP BY country, name, born) f ON f.country = g.country
GROUP BY g.head_kind
ORDER BY g.head_kind ASC
;


-- ---------------------------------------------------------- HOG_SCHEMA_I_07 The shortest completed term of each office
-- Per office, with its title, the completed term with the fewest days and who held it, by country code; at a tie, every
-- such term, the earlier first. An open term has no term_end, so julianday gives NULL and the term drops out.
SELECT g.*,
       h.*,
       CAST(julianday(h.term_end) - julianday(h.term_start) AS INTEGER) AS days
FROM office g
         JOIN head h ON h.country = g.country
WHERE julianday(h.term_end) - julianday(h.term_start)
          = (SELECT MIN(julianday(s.term_end) - julianday(s.term_start))
             FROM head s
             WHERE s.country = g.country)
ORDER BY g.country ASC, h.term_start ASC
;
