# CLAUDE.md

Guidance for Claude Code when working in this repository.

## What this project is

Jakarta Persistence mappings for `db/heads_of_governments.sqlite3`, a small SQLite file listing the
heads of government of five countries. One Maven module, `io.github.jinahya:heads-of-governments-persistence`,
one package, `com.github.jinahya.headsofgovernments.persistence`, two entities. Nothing downstream
consumes the jar; the project exists to be read, built and run against the file.

The file is the database. It already holds the schema and the data, and the persistence unit opens it
read-only (`open_mode=1`), so the providers only read from it and never generate DDL.
The raw lists the rows came from are the tab-separated files under `docs/`, one per country, each
headed by the web pages it was transcribed from and a description of its columns.

### The schema

```sql
CREATE TABLE office (
    country    TEXT NOT NULL PRIMARY KEY,
    head_kind  TEXT NOT NULL CHECK (head_kind IN ('PRESIDENT', 'PRIME_MINISTER')),
    head_title TEXT NOT NULL,
    numbering  TEXT NOT NULL
) WITHOUT ROWID;

CREATE TABLE head (
    id         INTEGER PRIMARY KEY,
    country    TEXT    NOT NULL REFERENCES office (country),
    number     INTEGER NOT NULL,
    term_start TEXT    NOT NULL,
    term_end   TEXT,
    name       TEXT    NOT NULL,
    party      TEXT    NOT NULL,
    born       INTEGER NOT NULL,
    died       INTEGER,
    CHECK (term_end IS NULL OR term_end >= term_start),
    CHECK (died IS NULL OR died >= born)
);
CREATE INDEX idx_head_country_number ON head (country, number);
CREATE INDEX idx_head_name ON head (name);
```

`office` has one row per country (`FR`, `GB`, `JP`, `KR`, `US`): what its head is called, and a
sentence saying how that country numbers them. `head` has one row per term of office. `number` is the
country's own ordinal and is **not unique** -- a person re-elected or re-appointed keeps a number across
several rows -- which is why `head` carries a surrogate `id`.

Dates are ISO-8601 text and years are integers, exactly as SQLite stores them; the mappings follow
that (`String termStart`, `Integer born`) rather than converting. `head_kind` is likewise bound as a
`String`, with the nested `Office.HeadKind` enum kept beside it as the list of values.

When the file changes, dump its schema (`sqlite3 db/heads_of_governments.sqlite3 .schema`) and bring the
entities back into line with it, then run the tests under both profiles. The unit sets no
`hibernate.hbm2ddl.auto`, so nothing checks the mappings at startup; a wrong column shows up as an
SQLite error from the first query that reads it.

### The entities

| table | entity | identifier | association |
| --- | --- | --- | --- |
| `office` | `Office` | `@Id String country` | `@OneToMany(mappedBy = "office") List<Head> heads` |
| `head` | `Head` | `@Id Long id` | `@ManyToOne Office office` over `country` |

Both are standalone: no superclass, no shared base, no id class. `Head`'s identifier is `Long`: `id` is an
`INTEGER PRIMARY KEY`, an alias for SQLite's signed 64-bit `rowid`, and the row count does not bound its
values. Hibernate's `hibernate.hbm2ddl.auto=validate`, which the unit does not set, would refuse it, since
the driver reports the column as `Types#INTEGER` ("found integer, but expecting bigint").

Conventions the two classes follow, and new members must keep:

- **Nothing is updatable, and only the `@Id` column is insertable.** Every `@Column` and the
  `@JoinColumn` carry `insertable = false, updatable = false`, except the `@Id` column, which carries
  only `updatable = false` and so stays insertable by default. The file is opened read-only, so nothing is lost; the
  mappings say so explicitly rather than relying on the driver to refuse. The `@Id` exception is
  EclipseLink's: with both flags false it fails predeployment with `EclipseLink-46` ("There should be
  one non-read-only mapping defined for the primary key field"), since it counts a mapping as
  read-only only when both are false.
- **An element is left out only when its default is the right value.** So there is no
  `unique = false`, `length = 255`, `cascade = {}`, `catalog`/`schema`, `targetEntity` (the field
  type gives it), `referencedColumnName` (the referenced primary key), `@Entity` name (the class
  name). Names are always written
  out: `@Table(name = ...)` on both entities and `name` on every `@Column` and the `@JoinColumn`,
  even where the column matches the field. A `NOT NULL` column carries `@Basic(optional = false)` and
  `@Column(nullable = false)`, ids included, as do the `@ManyToOne`/`@JoinColumn` pair; a nullable column
  says `nullable = true` and `@Basic(optional = true)` explicitly. Every `@Basic`,
  `@ManyToOne` and `@OneToMany` names its `fetch` explicitly, default or not: `EAGER` on the basics
  and on `office`, `LAZY` on `heads`. Do not add
  `columnDefinition`, `options`, `check`, `comment`, `table`, `precision`, `scale`, `secondPrecision`, `indexes` or `uniqueConstraints`. The database's own `CHECK` constraints
  and indexes are not repeated in the annotations -- and EclipseLink 5.0.2 throws a
  `NullPointerException` from `DatabaseTable.getCheckConstraints()` at predeployment if a
  `@CheckConstraint` is present, so that one is not merely noise.
- **Attributes are declared in table column order**, accessors and fields alike, with an association
  placed directly after the column it maps (`office` right after `id`, as the next thing in `head`
  is `country`). `Head` does not map `country` as a basic attribute; the association owns the column.
- **Member order inside a class**: nested types, then `// ----` the `protected` no-arg constructor,
  then the accessors under `// ----` markers, then the `private` fields last. Markers are line comments
  padded with dashes to column 120.
- `toString`, `equals` and `hashCode` are not overridden; identity equality is fine for a read-only
  mapping, and an association must never take part in either anyway.

### The persistence unit and the static metamodel

`src/main/resources/META-INF/persistence.xml` declares the single unit, `heads-of-governments`,
listing both entities. It sets no `<exclude-unlisted-classes>`, `<shared-cache-mode>` or `<validation-mode>`:
the classes directory holds only the two entities, so scanning finds the same list, and the defaults suit
a read-only file with no validator on the classpath. Its JDBC URL is
relative to the working directory, which is the project root under Maven and in the IDE.

The descriptor lives under **main**, not test, resources, on purpose. EclipseLink's metamodel
processor reads `META-INF/persistence.xml` at annotation-processing time, and the processor path sees
main resources only, so this placement is what lets the `jakarta-ee-11-eclipselink` profile generate
the static metamodel with EclipseLink's own `org.eclipse.persistence.jpa.modelgen.processor` while the
Hibernate profile uses `hibernate-jpamodelgen`. The cost is that the descriptor is packaged into the
jar. That would be wrong for a library -- Spring's `DefaultPersistenceUnitManager` reads
`classpath*:META-INF/persistence.xml` from every jar -- but this module has no consumer, so it is
accepted. Resource filtering is on for main resources, which is what resolves `${persistence-unit.provider}`
and `${persistence.version}` in it.

The generated `Office_` and `Head_` are what persistence-context code uses for criteria paths;
`mappedBy` and other annotation values stay string literals, since an annotation value must be a
compile-time constant.

## Building and testing

`maven.compiler.release` is 17 for main sources and 25 for test sources, so **building needs a JDK 25
or newer**. One script per provider per depth, every one of them prepending `clean`:

| runs | Hibernate ORM | EclipseLink |
| --- | --- | --- |
| unit tests | `_mvn_jakarta_ee_11_test_hibernate-orm.sh` | `_mvn_jakarta_ee_11_test_eclipselink.sh` |
| unit tests and ITs | `_mvn_jakarta_ee_11_verify_hibernate-orm.sh` | `_mvn_jakarta_ee_11_verify_eclipselink.sh` |
| no tests, but compiles them | `_mvn_jakarta_ee_11_verify_notests_hibernate-orm.sh` | `_mvn_jakarta_ee_11_verify_notests_eclipselink.sh` |

The `clean` is not optional: the two profiles generate different `Head_` sources, and without a clean
in between the second provider compiles against the first's leftovers.

The `_verify_` scripts name the `failsafe` profile, which binds `maven-failsafe-plugin` to `**/*IT.java`;
the plain `verify` in CI does not, so no `*IT` class runs there. Nothing needs a server: the unit tests
and any IT read the SQLite file in `db/`. As of maven-failsafe-plugin 3.6.0 `-DskipTests` reaches
surefire alone, so dropping the ITs from a `_verify_` run takes `-DskipITs`.

A check against the file that proves a mapping change is sound: open the unit, `find` a `Head` by id,
walk to its `Office` and back to `getHeads()`, and count both tables -- 285 heads, 5 offices --
under each profile in turn.

## Dependency convergence

Dependency convergence is **very important** here. `enforce-dependency-convergence` runs on every
build and is not skipped; treat a divergence as a build defect, not a warning.

### Origin: the Jakarta EE umbrella platform

- `jakarta.platform:jakarta.jakartaee-bom` is imported in `<dependencyManagement>`, pinned by
  `version.jakarta.jakartaee-bom` (`11.0.0`). **Jakarta EE 11 is the only supported generation**; the
  platform values (`version.jakarta.jakartaee-bom`, `persistence.version` 3.2,
  `persistence.schemaLocation`) live in the root `<properties>`, never in a profile.
- **The persistence provider is the only implementation axis**, with exactly two profiles:
  `jakarta-ee-11-hibernate-orm` (`activeByDefault`) and `jakarta-ee-11-eclipselink`. Jakarta
  Persistence is the only spec implemented here -- there is no Jakarta Validation and no CDI on any
  classpath, so the unit's default `<validation-mode>` of `AUTO` finds nothing to use -- so nothing else gets a profile. A provider profile picks no version either: it decides which provider is on the
  test classpath, which `persistence-unit.provider` the descriptor names, and which
  `metamodel.generator.*` the annotation processor path uses.
- Do not add a profile to test a second version of anything. Any profile that is added must carry the
  `jakarta-ee-11-` prefix and actually be certified for that generation.
- Naming any profile on the command line deactivates every `activeByDefault` profile. Harmless while
  the provider is the only axis, but it is why a second axis must not be added casually.

Consequences:

- Never pin a `jakarta.*` API artifact to an explicit version. The umbrella BOM decides.
- When a provider drags in a `jakarta.*` API newer or older than the BOM, align the provider with the
  platform generation; do not override the API version.
- Check convergence against both providers:
  `./mvnw -q -Pjakarta-ee-11-hibernate-orm enforcer:enforce -Drules=dependencyConvergence`, then the
  same with `-Pjakarta-ee-11-eclipselink`.

### Keeping specs and implementations aligned

| Spec (API, from the umbrella BOM) | Implementation | Version property |
| --- | --- | --- |
| Jakarta Persistence (`jakarta.persistence-api`) | Hibernate ORM (`org.hibernate.orm:hibernate-core`), plus `hibernate-community-dialects` for `SQLiteDialect` | `version.org.hibernate.orm` |
| Jakarta Persistence (`jakarta.persistence-api`) | EclipseLink (`org.eclipse.persistence:org.eclipse.persistence.jpa`) | `version.org.eclipse.persistence` |

- `hibernate-community-dialects` moves with `hibernate-core`; EclipseLink ships no SQLite platform and
  runs on the generic `DatabasePlatform`.
- The SQLite driver is `org.xerial:sqlite-jdbc` (`version.org.xerial.sqlite-jdbc`), test scope.

### Detecting and fixing convergence problems

- `./mvnw -q dependency:tree -Dverbose` marks divergent transitive versions.
- Do **not** silently accept Maven's "nearest wins". Resolve `jakarta.*` at the platform level; pin
  anything else in `<dependencyManagement>`, preferring the highest version any path requires. Use
  `<exclusions>` only when a dependency genuinely must not be on the classpath.
- If a conflict cannot be resolved without a behavioural change, stop and report it rather than
  choosing a version unilaterally.
