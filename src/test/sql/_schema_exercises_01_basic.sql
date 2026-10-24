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
-- Exercises at the basic level on the whole schema, a problem per query that reads both tables. Each problem states the
-- request it answers, then the SQL that answers it.
--
-- Parameters are bind variables (:name); the sqlite3 shell binds one with `.parameter set :name 'value'`, and an IDE
-- prompts for them when a statement runs.
--


-- ------------------------------------------------------------------- HOG_SCHEMA_B_01 The incumbents, with their titles
-- The front page names each country's head of government today, with the title the country gives the office.
SELECT g.*, h.*
FROM office g
         JOIN head h ON h.country = g.country AND h.term_end IS NULL
ORDER BY g.country ASC
;


-- ----------------------------------------------------- HOG_SCHEMA_B_02 A country's list, with the rule for its numbers
-- A reader asks for one country's list and should see, with it, what number counts there.
SELECT g.*, h.*
FROM office g
         JOIN head h ON h.country = g.country
WHERE g.country = :country
ORDER BY h.term_start ASC
;


-- ------------------------------------------------------------------------------------ HOG_SCHEMA_B_03 Terms per office
-- Each office with the kind of head it has and how many terms the list holds.
SELECT g.*, COUNT(h.id) AS terms
FROM office g
         LEFT JOIN head h ON h.country = g.country
GROUP BY g.country
ORDER BY g.country ASC
;


-- ----------------------------------------------------------------------- HOG_SCHEMA_B_04 The first head of each office
-- Each office's head numbered 1, with the title of the office, the oldest list first.
SELECT g.*, h.*
FROM office g
         JOIN head h ON h.country = g.country
WHERE h.number = 1
ORDER BY h.term_start ASC
;


-- ------------------------------------------------------------------------------- HOG_SCHEMA_B_05 Presidents since 2000
-- Every term of a president (head_kind 'PRESIDENT') that began in 2000 or later, with the title, in date order and, on
-- the same day, by country.
SELECT g.*, h.*
FROM office g
         JOIN head h ON h.country = g.country
WHERE g.head_kind = 'PRESIDENT'
  AND h.term_start >= '2000-01-01'
ORDER BY h.term_start ASC, g.country ASC
;


-- -------------------------------------------------------------------------- HOG_SCHEMA_B_06 Presidents without a party
-- The presidents' terms whose party is one of 'Indépendant', 'Unaffiliated' or '무소속', by country, then in date order. A
-- succession that includes one of them ('무소속/민주정의당') is a different value and does not match.
SELECT g.*, h.*
FROM office g
         JOIN head h ON h.country = g.country
WHERE g.head_kind = 'PRESIDENT'
  AND h.party IN ('Indépendant', 'Unaffiliated', '무소속')
ORDER BY g.country ASC, h.term_start ASC
;


-- ------------------------------------------------------------------------- HOG_SCHEMA_B_07 Terms since 2000 per office
-- Each office with the number of terms that began in 2000 or later, most first and, at a tie, by country code. The date
-- goes into the join condition, not WHERE, so an office with no such term would still show, with 0.
SELECT g.*, COUNT(h.id) AS terms
FROM office g
         LEFT JOIN head h ON h.country = g.country AND h.term_start >= '2000-01-01'
GROUP BY g.country
ORDER BY terms DESC, g.country ASC
;
