# Deliverables & Review Checklist

Tick each box (`[x]`) in a PR as the item is completed. The team lead verifies everything
before the final review.

## Repository contents
- [ ] Working CLI application (`mvn -q compile exec:java` starts it)
- [ ] Required tests all passing (`mvn verify`)
- [ ] Test coverage report committed (`docs/coverage/index.html`), target 100%
- [ ] Seeded DB file `data/app.db` with **at least 6 listed items**
- [ ] ER diagram of all 3 tables (columns, types, constraints, relationships) in `docs/er-diagram.md`
- [ ] Git history shows teamwork:
  - [ ] Branches merged via **reviewed** PRs
  - [ ] Small, focused commits
  - [ ] Clear commit messages (see CONTRIBUTING.md)

## Automated tests (real temp SQLite, no mocks)
### Data layer (repository)
- [ ] Insert users, items, rentals
- [ ] Query by ID and by filter
- [ ] Bad inputs fail correctly without crashing
- [ ] Update item status and rental status

### Constraints (repository, exact exception type asserted)
- [ ] Duplicate username -> UNIQUE
- [ ] Rental pointing to non-existent item -> FOREIGN KEY
- [ ] Item missing required field -> NOT NULL
- [ ] Item with out-of-range status -> CHECK

### Behaviour (service layer)
State transitions
- [ ] First launch creates the user; second run loads the same account
- [ ] Listing sets item to `available`
- [ ] Recording a rental sets item to `rented`
- [ ] Confirming return sets item to `available`
- [ ] ...unless it was delisted while out -> stays `unlisted`

Business rules
- [ ] Already-rented item cannot be rented again
- [ ] Delisted item cannot be selected for rental
- [ ] Renting to an existing username reuses that record
- [ ] End time = start time + number of days entered

### Quality bar
- [ ] Every case above has a test
- [ ] Normal AND edge cases covered
- [ ] Tests isolated (each builds own data; pass in any order)

## Features (demo-ready)
- [ ] First launch asks for username; later runs load the account
- [ ] Main menu loop with clean Exit (DB connection closed)
- [ ] List an item (name, description, cost per day)
- [ ] Paginated lists (5 per page, next/previous only when they exist, back to menu)
- [ ] Inventory + item detail with delist / relist
- [ ] Record a rental (new renter created, existing renter reused)
- [ ] Confirm a return (rental closed, `returned_at` set, item status correct)
- [ ] Rentals never deleted (history preserved)
- [ ] State machine enforced in code, not only by DB CHECK

## Database failure handling
- [ ] Typed exception: cannot open database
- [ ] Typed exception: constraint violation
- [ ] Typed exception: not found
- [ ] Typed exception: mapping exception
- [ ] Repository catches `SQLException` and throws the typed exception
- [ ] Menu shows a friendly message and returns to menu instead of crashing

## Be ready to demonstrate
- [ ] First launch through to main menu
- [ ] Listing an item
- [ ] Viewing inventory and delisting
- [ ] Recording a rental with a new renter
- [ ] Confirming a return
- [ ] Running the test suite

## Be ready to explain
- [ ] How the layered architecture is enforced in the codebase
- [ ] How database exceptions are caught and handled in the repository layer