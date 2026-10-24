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
-- Exercises at the basic level on head, a problem per query that reads head alone. Each problem states the request it
-- answers, then the SQL that answers it.
--
-- Parameters are bind variables (:name); the sqlite3 shell binds one with `.parameter set :name 'value'`, and an IDE
-- prompts for them when a statement runs. Dates are ISO-8601 text, so they compare as strings and date() and
-- julianday() read them directly; an incumbent's open term is measured to date('now'). A sort key that can be NULL
-- says NULLS FIRST or NULLS LAST, even where that is SQLite's default; a NOT NULL key does not.
--


-- ------------------------------------------------------------------------------ HOG_HEAD_B_01 Who held office on a day
-- A reader gives a date and wants each country's head on that day. A country whose office was vacant is absent.
SELECT *
FROM head
WHERE term_start <= :date
  AND (term_end IS NULL OR term_end >= :date)
ORDER BY country ASC
;


-- ------------------------------------------------------------------------------------ HOG_HEAD_B_02 One country's list
-- A reader wants a country's heads, first to latest, as the source lists them.
SELECT *
FROM head
WHERE country = :country
ORDER BY term_start ASC
;


-- ---------------------------------------------------------------------------------------- HOG_HEAD_B_03 The incumbents
-- The dashboard shows who is in office now, per country, and since when.
SELECT *
FROM head
WHERE term_end IS NULL
ORDER BY country ASC
;


-- ----------------------------------------------------------------------------------------- HOG_HEAD_B_04 Terms by name
-- A reader types part of a name and wants every term of anyone matching it. LIKE folds case for ASCII letters only,
-- so 'churchill' finds Churchill and '吉田' finds 吉田茂; the index on name does not help a leading wildcard.
SELECT *
FROM head
WHERE name LIKE '%' || :name || '%'
ORDER BY country ASC, term_start ASC
;


-- ---------------------------------------------------------------------------- HOG_HEAD_B_05 Terms that began in a year
-- A timeline wants every term that began in a given year, in any country, in date order.
SELECT *
FROM head
WHERE term_start BETWEEN :year || '-01-01' AND :year || '-12-31'
ORDER BY term_start ASC, country ASC
;


-- ------------------------------------------------------------------------------- HOG_HEAD_B_06 The oldest living heads
-- The ten earliest-born heads still living, in any country, oldest first. A person with several terms appears once; at
-- the same year of birth, by country, then by name.
SELECT DISTINCT country, name, born
FROM head
WHERE died IS NULL
ORDER BY born ASC, country ASC, name ASC
LIMIT 10
;


-- --------------------------------------------------------------- HOG_HEAD_B_07 Terms begun during the Second World War
-- Every term that began between 1 September 1939 and 2 September 1945, both days included, in date order and, on the
-- same day, by country.
SELECT *
FROM head
WHERE term_start BETWEEN '1939-09-01' AND '1945-09-02'
ORDER BY term_start ASC, country ASC
;


-- ----------------------------------------------------------------------- HOG_HEAD_B_08 Heads who lived past ninety-two
-- The heads whose year of death less their year of birth is 93 or more, once per person, the greatest first and, at a
-- tie, by country, then by name. born and died are years, so the age is as fine as the data allows and may be a year
-- too high.
SELECT DISTINCT country, name, born, died, died - born AS age
FROM head
WHERE died - born >= 93
ORDER BY age DESC, country ASC, name ASC
;
