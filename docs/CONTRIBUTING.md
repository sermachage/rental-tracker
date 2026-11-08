# Contributing to Rental Tracker

Welcome! This guide explains how we work together. Read it fully before your first pull request (PR).
Team lead: [Twitter](https://twitter.com/sermachage). Ask questions early - nobody is expected to know all of this already.

---

## 1. One-time setup

1. Install **Java 21** and **Maven 3.9+** (`java -version`, `mvn -version` to check).
2. Clone the repo (copy the URL from the green *Code* button on GitHub):
   ```bash
   git clone <repo-url>
   cd rental-tracker
   ```
3. Tell git who you are (once per machine):
   ```bash
   git config --global user.name  "Your Name"
   git config --global user.email "you@example.com"
   ```
4. Check everything builds: `mvn verify` (should end with `BUILD SUCCESS`).

---

## 2. Branching rules

* `main` is **protected**: nobody pushes to it directly. It must always build and pass tests.
* All work happens on a **short-lived feature branch** created from an up-to-date `main`.
* Branch names: `<type>/<short-description>`

| Type | Use for | Example |
|---|---|---|
| `feature/` | new functionality | `feature/item-repository` |
| `fix/` | bug fixes | `fix/return-keeps-unlisted` |
| `test/` | tests only | `test/constraint-violations` |
| `docs/` | documentation | `docs/er-diagram` |
| `chore/` | tooling, config | `chore/jacoco-setup` |

---

## 3. Daily workflow

```bash
git checkout main                    # go to main
git pull origin main                 # get the latest team changes
git checkout -b feature/my-task      # create + switch to your branch

# ...edit code, run tests...

git status                           # what changed?
git add path/to/File.java            # stage specific files (avoid `git add .` blindly)
git commit -m "feat(repository): add ItemRepository.insert"
git push -u origin feature/my-task   # publish your branch
```

Then open a **Pull Request** on GitHub into `main`.

If `main` moved while you worked:
```bash
git fetch origin
git rebase origin/main    # replay your commits on top of the latest main
# fix any conflicts, then: git add <file> && git rebase --continue
git push --force-with-lease
```

---

## 4. Commit messages

Small, focused commits - **one logical change each**. Format:

```
<type>(<scope>): <short summary in imperative mood>
```

Types: `feat`, `fix`, `test`, `docs`, `refactor`, `chore`.
Scope = the layer: `domain`, `infra`, `repository`, `service`, `transport`.

Good: `feat(service): reject rental of already-rented item`
Bad: `stuff`, `fixed things`, `final version2`

Rule of thumb: if you need the word "and" in the summary, make two commits.

---

## 5. Pull request rules

1. PRs are **small** (ideally under ~300 changed lines) and do one thing.
2. Fill in the PR description: *what* changed, *why*, *how to test*.
3. **`mvn verify` must pass locally** before you open the PR.
4. **At least one teammate approves** before merge (the author never approves their own PR).
5. Reviewers: comment on logic, naming, layer violations and missing tests. Be kind and specific.
6. Merge with **Squash and merge** or **Rebase and merge** (team lead decides), then delete the branch.

### Reviewer checklist
- [ ] Code is in the correct layer (see section 6)
- [ ] New public classes/methods have comments explaining *why*, not just *what*
- [ ] Tests added for every new behaviour (including edge cases)
- [ ] No `System.out`/`System.in` outside the transport layer
- [ ] No SQL outside the repository layer
- [ ] Commit messages follow section 4

---

## 6. Architecture rules (the important part)

Dependencies only point **downward**:

```
Transport -> Service -> Repository -> Infrastructure -> SQLite
        \________\___________\______-> Domain (usable by all)
```

| Layer | Package | May do | Must NOT do |
|---|---|---|---|
| Transport | `com.rental.transport` | print menus, read input | contain business rules or SQL |
| Service | `com.rental.service` | business rules, state machine | print, read input, write SQL |
| Repository | `com.rental.repository` | SQL, translate `SQLException` into our typed exceptions | contain business decisions |
| Infrastructure | `com.rental.infrastructure` | open connection, create schema | know about domain rules |
| Domain | `com.rental.domain` | plain data classes + enums | import any other layer |

Objects are connected by **constructor injection** (a class receives its dependencies as constructor
arguments; it never builds them itself). Only `App.java` wires things together.

---

## 7. Testing rules

* **No mocks of the database.** Tests use a real SQLite file in a JUnit `@TempDir`.
* Each test builds its own data, runs **one** operation, then **checks the database directly**.
* Tests must pass **in any order** (no shared state).
* Constraint tests assert the **exact exception type**, not just "something failed".
* Target is **100% JaCoCo coverage**. Run `mvn verify`, open `docs/coverage/index.html`.

---

## 8. Code style

* Every class and public method gets a Javadoc comment; tricky lines get inline comments.
* Class names `PascalCase`, methods/variables `camelCase`, constants `UPPER_SNAKE_CASE`.
* Prefer small methods (one job each). Avoid magic numbers; name them as constants.
* Never commit: `target/`, IDE folders, passwords, personal DB files.

---

## 9. Getting help

Stuck for more than 30 minutes? Post in the team chat with: what you tried, the error message,
and your branch name. Asking early is a strength, not a weakness.