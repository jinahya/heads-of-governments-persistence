# Branches

Each branch listed here carries one small change on top of `develop`, so a reader can switch to it,
run the same tests, and see what that change does. Nothing else differs: the tests are shared, and the
branch's diff against `develop` is only its change.

| branch | differs from `develop` by | what to run and look at |
|---|---|---|
| `mapping/head-office-lazy` | `Head.office` uses `@ManyToOne(fetch = FetchType.LAZY)` | `HeadPersistenceTest$FindTest$FetchTypeTest`: `__1` sends no `join office`; `__QueryLanguage` and `__CriteriaApi` join it again through `JOIN FETCH` |
| `mapping/head-java-time` | `Head.termStart`/`termEnd` are `LocalDate` through `@Convert(converter = LocalDateConverter.class)`; `Head.born`/`died` are `Year`, mapped without a converter | `HeadPersistenceTest` and `OfficePersistenceTest`: `develop`'s tests pass unchanged; `MIN`/`MAX` of `termStart` come back as `LocalDate` |
| `mapping/office-head-kind-enum` | `Office.headKind` is the `Office.HeadKind` enum, with `@Enumerated(EnumType.STRING)` | `OfficePersistenceTest`: `develop`'s tests pass unchanged; `WHERE e.headKind = :headKind` is bound with a `HeadKind` |

## Naming

`<area>/<subject>-<change>`, lowercase and kebab-case. `area` is the kind of change (`mapping`, `query`,
`config`, ...); `subject-change` says what differs from `develop`. No issue numbers and no chapter numbers:
the name alone has to say what the branch is for.

## Rules

- A branch carries **one** minimal change on top of `develop`. The tests stay `develop`'s; where a branch changes a
  type, `develop`'s tests are written so they hold for both.
- After every change to `develop`, **merge** `develop` into every branch listed here. Never rebase: readers
  clone these branches, and a rebase would rewrite what they already have.
- Never merge a branch back into `develop`.
- After each merge, run `./mvnw test` on the branch, and check that `git diff develop <branch>` is still only
  the branch's change.
- A new branch starts from `develop` and gets a row here, in the same commit on `develop`.
