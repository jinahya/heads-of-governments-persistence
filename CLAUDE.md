# CLAUDE.md

Guidance for Claude Code when working in this repository.

## What this project is

Jakarta Persistence mappings for `src/main/resources/heads_of_governments.sqlite3`, a small SQLite file listing the
heads of government of five countries. One Maven module, `io.github.jinahya:heads-of-governments-persistence`,
one package, `com.github.jinahya.headsofgovernments.persistence`, two entities. Nothing downstream
consumes the jar; the project exists to be read, built and run against the file.

The file is the database. It already holds the schema and the data, and the persistence unit opens it
read-only (`open_mode=1`), so Hibernate only reads from it and never generate DDL.
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
    country    TEXT    NOT NULL REFERENCES office (country),
    number     INTEGER NOT NULL,
    term_start TEXT    NOT NULL,
    term_end   TEXT,
    name       TEXT    NOT NULL,
    party      TEXT    NOT NULL,
    born       INTEGER NOT NULL,
    died       INTEGER,
    PRIMARY KEY (country, term_start),
    CHECK (term_end IS NULL OR term_end >= term_start),
    CHECK (died IS NULL OR died >= born)
) WITHOUT ROWID;
CREATE INDEX idx_head_country_number ON head (country, number);
CREATE INDEX idx_head_term_start ON head (term_start);
CREATE INDEX idx_head_term_end ON head (term_end);
CREATE INDEX idx_head_name ON head (name);
CREATE INDEX idx_head_born ON head (born);
CREATE INDEX idx_head_died ON head (died);
```

`office` has one row per country (`FR`, `GB`, `JP`, `KR`, `US`): what its head is called, and a
sentence saying how that country numbers them. `head` has one row per term of office. `number` is the
country's own ordinal and is **not unique** -- a person re-elected or re-appointed keeps a number across
several rows -- so `head` is keyed by `(country, term_start)`: a country starts at most one term on a given
day. The table is `WITHOUT ROWID` and has no surrogate.

Dates are ISO-8601 text and years are integers, exactly as SQLite stores them. `termStart`, part of the
key, is a `String`, so a query compares it as text (`h.termStart >= '2000-01-01'`), and
`getTermStartAsLocalDate()` is its typed, unmapped view. `termEnd` is a `LocalDate`, through
`@Convert(converter = Head.LocalDateConverter.class)`, a converter nested in `Head` and listed in the unit;
Hibernate types a converted attribute by its column, so JPQL string functions still work on it and `EXTRACT`
does not, and the Criteria API reaches the text with `.as(String.class)`. `born` is an `Integer`, with
`getBornAsYear()` as its typed view, and `died` is a `java.time.Year`, a basic type with no converter, which a
query does arithmetic on through a cast. `head_kind` is
mapped as the nested `Office.HeadKind` enum, with `@Enumerated(EnumType.STRING)`.

When the file changes, dump its schema (`sqlite3 src/main/resources/heads_of_governments.sqlite3 .schema`) and bring the
entities back into line with it, then run the tests. The unit sets no
`hibernate.hbm2ddl.auto`, so nothing checks the mappings at startup; a wrong column shows up as an
SQLite error from the first query that reads it.

### The entities

| table | entity | identifier | association |
| --- | --- | --- | --- |
| `office` | `Office` | `@Id String country` | `@OneToMany(mappedBy = "office") List<Head> heads` |
| `head` | `Head` | `@Id String country`, `@Id String termStart`, `@IdClass(HeadId.class)` | `@ManyToOne Office office` over `country` |

Both are standalone: no superclass and no shared base. `Head` keeps its two key columns as plain `@Id`
attributes, so a query reaches them as `h.country` and `h.termStart` and the Criteria API as `Head_.country` and
`Head_.termStart`, the same as any other column; this is an introductory project, so the entity keeps the shape of
the table, and `@EmbeddedId` is not used. `HeadId` is the id class: a `record HeadId(String country, String
termStart)`, which Jakarta Persistence 3.2 allows, with no annotation; its components match the `@Id` attributes by
name and type, and the record's own `equals` and `hashCode` are the value equality the specification asks for. The
specification does not require it to be `Serializable`, and it is not. The database is read-only, so a `HeadId` is only built to look a head up (`em.find(Head.class, new HeadId("FR", "1848-12-20"))`),
and it is not listed in the unit.

Conventions the two classes follow, and new members must keep:

- **Nothing is updatable, and only the `@Id` column is insertable.** Every `@Column` and the
  `@JoinColumn` carry `insertable = false, updatable = false`, except the `@Id` columns (`Office`'s
  `country`, `Head`'s `country` and `termStart`), which carry only `updatable = false` and so stay insertable
  by default. The file is opened read-only, so nothing is lost; the
  mappings say so explicitly rather than relying on the driver to refuse.
- **An element is left out only when its default is the right value.** So there is no
  `unique = false`, `length = 255`, `cascade = {}`, `catalog`/`schema`, `targetEntity` (the field
  type gives it), `referencedColumnName` (the referenced primary key). Names are always written
  out: `@Entity(name = ...)` and `@Table(name = ...)` on both entities and `name` on every `@Column`
  and the `@JoinColumn`, even where the name matches the class or the column the field. A `NOT NULL` column carries `@Basic(optional = false)`, ids
  included, as does the `@ManyToOne`; a nullable column says `@Basic(optional = true)` explicitly. The project
  only reads, so nothing that serves schema generation alone is written: no `nullable` on `@Column` or
  `@JoinColumn`, and no `check` on `@Table`. Every `@Basic`,
  `@ManyToOne` and `@OneToMany` names its `fetch` explicitly, default or not: `EAGER` on the basics
  and on `office`, `LAZY` on `heads`. Do not add
  `nullable`, `columnDefinition`, `options`, `check`, `comment`, `table`, `precision`, `scale`, `secondPrecision`, `indexes` or `uniqueConstraints`. The database's own `CHECK` constraints
  and indexes are not repeated in the annotations.
- **Attributes are declared in table column order**, accessors and fields alike, with an association
  placed directly after the column it maps (`office` right after `country`, which it maps read-only; the
  `@Id` attribute owns the column).
- **Member order inside a class**: nested types, then `// ----` the `protected` no-arg constructor,
  then the accessors under `// ----` markers, then the `private` fields last. Markers are line comments
  padded with dashes to column 120.
- `equals` and `hashCode` compare the identifier alone: `Office`'s `country`, and `Head`'s and `HeadId`'s
  `country` and `termStart`. An association never takes part in them, nor in `toString`.

### The persistence unit and the static metamodel

`src/test/resources/META-INF/persistence.xml` declares the single unit, `test`,
listing both entities. It sets no `<exclude-unlisted-classes>`, `<shared-cache-mode>` or `<validation-mode>`:
the unit's root, the test classes directory, holds no entity, so scanning adds nothing to the list, and the
defaults suit a read-only file with no validator on the classpath. Its JDBC URL is
relative to the working directory, which is the project root under Maven and in the IDE.

The descriptor lives under **test**, not main, resources, so the jar does not carry it:
`heads-of-governments-spring-data` depends on the jar, and Spring's `DefaultPersistenceUnitManager` reads
`classpath*:META-INF/persistence.xml` from every jar. Resource filtering is on for test resources, which is what
resolves `${persistence.version}` and `${persistence.schemaLocation}` in it; the `<provider>` is written out, since
Hibernate ORM is the only one.

The generated `Office_` and `Head_`, from EclipseLink's processor by default, are what persistence-context code uses
for criteria paths;
`mappedBy` and other annotation values stay string literals, since an annotation value must be a
compile-time constant.

### The SQL files

`src/test/sql` holds hand-written SQL against the file. They are for a reader with the `sqlite3` tool or an IDE;
the build does not run them. `sqlite3 src/main/resources/heads_of_governments.sqlite3 '.read src/test/sql/head.sql'` runs one whole.

- `office.sql` and `head.sql` profile a table, a section per column under a ruler, each query
  prefaced by what it looks for and what the result should be; `-- no rows:` marks a check that passes
  when nothing comes back. The last section covers the table as a whole.
- `src/test/sql-exercises/<table>/<nn>_<level>.sql` pose a problem per query, a file per level (`01_basic`,
  `02_intermediate`, `03_advanced`; the number only orders the files), and
  `src/test/sql-exercises/__/<nn>_<level>.sql` the same for the whole schema. Each
  carries an id, `HOG_<TABLE>_<B|I|A>_<nn>` (`HOG_SCHEMA_` for the whole schema), and restates the
  request it answers. Parameters are bind variables (`:name`); the shell binds one with
  `.parameter set :name 'value'`. `EXERCISES.asciidoc` states every problem, and `EXERCISES.yaml` registers every id; ids are permanent, a new exercise takes
  the next unused number of its section and tier, and a removed one's id is marked `retired: true`, never reused.

Every file starts with the Apache 2.0 header in the `-- #%L` form, rulers are 120 columns, and lines
stay within 120. A sort key that can be NULL (`term_end`, `died`, an aggregate of either, `sqlite_stat1.idx`)
says `NULLS FIRST` or `NULLS LAST`, even where that is SQLite's default; a `NOT NULL` key does not. When the file
changes, rerun the files and read what they return: a `no rows:` claim or a comment that names a country can go
stale.

## Building and testing

`maven.compiler.release` is 21 for main sources and 25 for test sources, so **building needs a JDK 25
or newer**. One script per depth, each naming the `jakartaee-11` profile and prepending `clean`:

| runs | script |
| --- | --- |
| unit tests | `_mvn_jakarta_ee_11_test.sh` |
| unit tests and ITs | `_mvn_jakarta_ee_11_verify.sh` |
| no tests, but compiles them | `_mvn_jakarta_ee_11_verify_notests.sh` |

The `_verify` scripts name the `failsafe` profile, which binds `maven-failsafe-plugin` to `**/*IT.java`;
the plain `verify` in CI does not, so no `*IT` class runs there. Nothing needs a server: the unit tests
and any IT read the SQLite file in `db/`. As of maven-failsafe-plugin 3.6.0 `-DskipTests` reaches
surefire alone, so dropping the ITs from a `_verify` run takes `-DskipITs`.

A check against the file that proves a mapping change is sound: open the unit, `find` a `Head` by its `HeadId`,
walk to its `Office` and back to `getHeads()`, and count both tables -- 285 heads, 5 offices.

## Dependency convergence

Dependency convergence is **very important** here. `enforce-dependency-convergence` runs on every
build and is not skipped; treat a divergence as a build defect, not a warning.

### Origin: the Jakarta EE umbrella platform

- `jakarta.platform:jakarta.jakartaee-bom` is imported in `<dependencyManagement>`, pinned by
  `version.jakarta.jakartaee-bom` (`11.0.0`). **Jakarta EE 11 is the only supported generation so
  far.** The per-generation values (`version.jakarta.jakartaee-bom`, `version.org.hibernate.orm`,
  `persistence.version` 3.2, `persistence.schemaLocation`) live **only** in that generation's
  `jakarta-ee-NN` profile, and the root `<properties>` gives them no default: a build with no
  generation active fails at the BOM import rather than quietly building one.
- **Hibernate ORM is the only persistence provider**, and the project stays Hibernate-only. Profiles
  exist only per Jakarta EE generation: `jakartaee-11` (`activeByDefault`) is the one so far, and a
  new generation gets a `jakarta-ee-NN` profile, never a provider. The one other axis is the static metamodel
  generator, which is not a provider: EclipseLink's processor is the root default, and
  `jakartaee-11-metamodel-hibernate-orm`, named beside the generation
  (`-Pjakartaee-11,jakartaee-11-metamodel-hibernate-orm`), switches to `hibernate-jpamodelgen`;
  `jakartaee-11-metamodel-eclipselink` only names the default explicitly. Their names carry the generation, as
  each generation brings its own provider versions. EclipseLink's processor reads its own descriptor,
  `src/metamodel-eclipselink/META-INF/metamodel-eclipselink.xml`, which the build copies into `target/classes` and
  keeps out of the jar; `version.org.eclipse.persistence` sits in the
  generation profile, aligned with the sibling projects. Jakarta Persistence is the only
  spec implemented here -- there is no Jakarta Validation and no CDI on any classpath, so the unit's
  default `<validation-mode>` of `AUTO` finds nothing to use. The dependencies themselves
  (`hibernate-core`, `hibernate-community-dialects`, test scope) are declared once in the root
  `<dependencies>`; a generation profile carries only `<properties>`.
- Do not add a profile to test a second version of anything. Any generation profile that is added must carry the
  `jakarta-ee-NN` form and actually be certified for that generation.
- Naming any profile on the command line deactivates every `activeByDefault` profile. With no
  default values in the root, naming only another profile (`-Pfailsafe` alone) leaves no generation
  active and fails the build, which is intended: the scripts and CI always name the generation too.
  It is also why a second axis must not be added casually.

Consequences:

- Never pin a `jakarta.*` API artifact to an explicit version. The umbrella BOM decides.
- When a provider drags in a `jakarta.*` API newer or older than the BOM, align the provider with the
  platform generation; do not override the API version.
- Check convergence:
  `./mvnw -q -Pjakartaee-11 enforcer:enforce -Drules=dependencyConvergence`.

### Keeping specs and implementations aligned

| Spec (API, from the umbrella BOM) | Implementation | Version property |
| --- | --- | --- |
| Jakarta Persistence (`jakarta.persistence-api`) | Hibernate ORM (`org.hibernate.orm:hibernate-core`), plus `hibernate-community-dialects` for `SQLiteDialect` | `version.org.hibernate.orm` |

- `hibernate-community-dialects` and `hibernate-jpamodelgen` move with `hibernate-core`.
- The SQLite driver is `org.xerial:sqlite-jdbc` (`version.org.xerial.sqlite-jdbc`), test scope.

### Detecting and fixing convergence problems

- `./mvnw -q dependency:tree -Dverbose` marks divergent transitive versions.
- Do **not** silently accept Maven's "nearest wins". Resolve `jakarta.*` at the platform level; pin
  anything else in `<dependencyManagement>`, preferring the highest version any path requires. Use
  `<exclusions>` only when a dependency genuinely must not be on the classpath.
- If a conflict cannot be resolved without a behavioural change, stop and report it rather than
  choosing a version unilaterally.
