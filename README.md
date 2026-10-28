# heads-of-governments-persistence

Jakarta Persistence mappings for a small SQLite database of heads of government: the presidents of
France, South Korea and the United States, and the prime ministers of Japan and the United Kingdom,
one row per term of office.

| | |
| --- | --- |
| database | `db/heads_of_governments.sqlite3`, opened read-only |
| entities | `Office` (one per country) and `Head` (one per term), in `com.github.jinahya.headsofgovernments.persistence` |
| sources | `docs/*.txt`, one tab-separated list per country, each headed by the pages it came from |
| platform | Jakarta EE 11 / Jakarta Persistence 3.2, with Hibernate ORM as the provider |

## The schema

```
office                              head
------                              ----
country    TEXT  PK    <---------+  id         INTEGER PK
head_kind  TEXT  PRESIDENT |     +- country    TEXT    FK
                 PRIME_MINISTER     number     INTEGER   the country's own ordinal; not unique
head_title TEXT                     term_start TEXT      yyyy-MM-dd
numbering  TEXT                     term_end   TEXT      yyyy-MM-dd, or NULL for the incumbent
                                    name       TEXT
                                    party      TEXT
                                    born       INTEGER   year
                                    died       INTEGER   year, or NULL
```

`numbering` is a sentence per country saying how that country counts: France numbers people by first
election and a re-elected president keeps the number; Japan numbers cabinets (代), so one person can
hold several numbers. That is why `number` is not unique and `head` carries a surrogate `id`.

```shell
$ sqlite3 db/heads_of_governments.sqlite3 .schema
$ sqlite3 -header db/heads_of_governments.sqlite3 "select * from office"
```

Queries to profile each table are under [src/test/sql](src/test/sql), and the exercises on them under
[src/test/sql/exercises](src/test/sql/exercises).
The exercises are listed, with their ids, in [EXERCISES.asciidoc](EXERCISES.asciidoc).

## Building

Main sources target Java 17 and test sources Java 25, so the build needs a **JDK 25 or newer**.

```shell
$ ./_mvn_jakarta_ee_11_test.sh              # unit tests
$ ./_mvn_jakarta_ee_11_verify.sh            # and the ITs (the `failsafe` profile)
$ ./_mvn_jakarta_ee_11_verify_notests.sh    # the whole lifecycle, tests compiled but not run
```

There is no server to bring up; every test reads the SQLite file.

## Jakarta EE alignment

Every `jakarta.*` API comes from the `jakarta.jakartaee-bom` 11.0.0 import and is never pinned
directly. Implementations are pinned to their EE 11 aligned releases in the `jakarta-ee-11` profile's
`<properties>`:

| Spec | Implementation |
| --- | --- |
| Jakarta Persistence 3.2 | Hibernate ORM 7 |

Jakarta Persistence is the only spec implemented; there is no Validation or CDI on the classpath, so
the unit leaves `<validation-mode>` at its `AUTO` default. Hibernate ORM is the only provider, so the
Jakarta EE generation is the only build axis: one profile per generation, `jakarta-ee-11` (the
default) so far. Dependency convergence is enforced on every build; see `CLAUDE.md` for the rules.

## The persistence unit

`src/main/resources/META-INF/persistence.xml` declares one unit, `heads-of-governments`, over

```
jdbc:sqlite:db/heads_of_governments.sqlite3?open_mode=1
```

`open_mode=1` is `SQLITE_OPEN_READONLY`; a write fails at the driver. The path is relative to the
working directory, the project root under Maven and in the IDE. The descriptor sits in main
resources, so the jar carries it, which is fine for a module nothing depends on.

## Exercises

_To be written._

## Things that bit us

- **SQLite `INTEGER` validates as `Types#INTEGER`.** `head.id` is a 64-bit `rowid` alias and is mapped as a
  `Long`, but Hibernate's `hbm2ddl.auto=validate` rejects that ("found integer, but expecting bigint"), which
  is one reason the unit does not turn validation on.
- **`SQLiteDialect` is not in `hibernate-core`.** It ships in `org.hibernate.orm:hibernate-community-dialects`,
  which moves in step with `hibernate-core`; Hibernate picks it up from the JDBC metadata, so the unit
  names no `hibernate.dialect`.
- **`-DskipTests` no longer reaches failsafe** as of maven-failsafe-plugin 3.6.0. Use `-DskipITs` to
  drop the ITs from a `verify`.

## Links

- [SQLite datatypes](https://www.sqlite.org/datatype3.html) -- storage classes and type affinity;
  why `INTEGER` has no precision and `TEXT` no length.
- [sqlite-jdbc](https://github.com/xerial/sqlite-jdbc) -- the driver, and its `open_mode` URL parameter.
- [Hibernate community dialects](https://github.com/hibernate/hibernate-orm/tree/main/hibernate-community-dialects)
- [Jakarta Persistence 3.2](https://jakarta.ee/specifications/persistence/3.2/)
