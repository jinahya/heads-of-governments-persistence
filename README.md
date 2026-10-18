# heads-of-governments-persistence

Jakarta Persistence mappings for a small SQLite database of heads of government: the presidents of
France, South Korea and the United States, and the prime ministers of Japan and the United Kingdom,
one row per term of office.

| | |
| --- | --- |
| database | `db/heads_of_governments.sqlite3`, opened read-only |
| entities | `Government` (one per country) and `Head` (one per term), in `com.github.jinahya.headsofgovernments.persistence` |
| sources | `docs/*.txt`, one tab-separated list per country, each headed by the pages it came from |
| platform | Jakarta EE 11 / Jakarta Persistence 3.2, with Hibernate ORM or EclipseLink as the provider |

## The schema

```
government                          head
----------                          ----
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
$ sqlite3 -header db/heads_of_governments.sqlite3 "select * from government"
```

## Building

Main sources target Java 17 and test sources Java 25, so the build needs a **JDK 25 or newer**.

```shell
$ ./_mvn_jakarta_ee_11_test_hibernate-orm.sh      # unit tests, Hibernate ORM
$ ./_mvn_jakarta_ee_11_test_eclipselink.sh        # unit tests, EclipseLink
$ ./_mvn_jakarta_ee_11_verify_hibernate-orm.sh    # and the ITs (the `failsafe` profile)
$ ./_mvn_jakarta_ee_11_verify_eclipselink.sh
```

Every script starts with `clean`, and that matters: each provider profile generates the static
metamodel (`Government_`, `Head_`) with its own annotation processor, and a build without a clean
in between would compile against the other provider's output. There is no server to bring up; every
test reads the SQLite file.

## Jakarta EE alignment

Every `jakarta.*` API comes from the `jakarta.jakartaee-bom` 11.0.0 import and is never pinned
directly. Implementations are pinned to their EE 11 aligned releases in `pom.xml` `<properties>`:

| Spec | Implementation |
| --- | --- |
| Jakarta Persistence 3.2 | Hibernate ORM 7 (`jakarta-ee-11-hibernate-orm`, default) or EclipseLink 5 (`jakarta-ee-11-eclipselink`) |

Jakarta Persistence is the only spec implemented; there is no Validation or CDI on the classpath and
the unit's `<validation-mode>` is `NONE`. The provider profile is the only build axis. It chooses the provider on the test classpath, the
`<provider>` the persistence unit names, and the metamodel processor. Dependency convergence is
enforced on every build; see `CLAUDE.md` for the rules.

## The persistence unit

`src/main/resources/META-INF/persistence.xml` declares one unit, `heads-of-governments`, over

```
jdbc:sqlite:db/heads_of_governments.sqlite3?open_mode=1
```

`open_mode=1` is `SQLITE_OPEN_READONLY`; a write fails at the driver. The path is relative to the
working directory, the project root under Maven and in the IDE. The descriptor sits in main
resources rather than test resources because EclipseLink's metamodel processor has to find it on the
annotation-processor path; the jar carries it as a result, which is fine for a module nothing
depends on.

## Things that bit us

- **SQLite `INTEGER` validates as `Types#INTEGER`.** Hibernate's `hbm2ddl.auto=validate` rejects a
  `Long` identifier over `head.id` ("found integer, but expecting bigint"); the id is an `Integer`.
- **`SQLiteDialect` is not in `hibernate-core`.** It ships in `org.hibernate.orm:hibernate-community-dialects`,
  which moves in step with `hibernate-core`.
- **EclipseLink has no SQLite platform.** The unit sets `eclipselink.target-database` to the generic
  `org.eclipse.persistence.platform.database.DatabasePlatform`, which is enough for reads.
- **EclipseLink 5.0.2 crashes on `@CheckConstraint`.** Predeployment throws a `NullPointerException`
  from `DatabaseTable.getCheckConstraints()` when a table or column declares one, so the database's
  `CHECK` constraints are not repeated in the annotations.
- **EclipseLink's metamodel processor needs `persistence.xml` on the processor path.** With the
  descriptor under `src/test/resources` it generated nothing, silently. That is the reason the
  descriptor lives under `src/main/resources`.
- **`-DskipTests` no longer reaches failsafe** as of maven-failsafe-plugin 3.6.0. Use `-DskipITs` to
  drop the ITs from a `verify`.

## Links

- [SQLite datatypes](https://www.sqlite.org/datatype3.html) -- storage classes and type affinity;
  why `INTEGER` has no precision and `TEXT` no length.
- [sqlite-jdbc](https://github.com/xerial/sqlite-jdbc) -- the driver, and its `open_mode` URL parameter.
- [Hibernate community dialects](https://github.com/hibernate/hibernate-orm/tree/main/hibernate-community-dialects)
- [Jakarta Persistence 3.2](https://jakarta.ee/specifications/persistence/3.2/)
