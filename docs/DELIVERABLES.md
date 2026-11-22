# Deliverables & Review Checklist

Tick each box (`[x]`) in a PR as the item is completed. The team lead verifies everything
before the final review.

## Repository contents
- [x] Working CLI application (`mvn -q compile exec:java` starts it)
- [x] Required tests all passing (`mvn verify`)
- [x] Test coverage report committed (`docs/coverage/index.html`)
- [x] Seeded DB file `data/app.db` with **at least 6 listed items**
- [x] ER diagram of the 3 domain tables and owner-setup configuration table (columns, constraints, relationships) in `docs/er-diagram.md`
- [x] Git history shows teamwork:
  - [x] Branches merged via local feature-branch merges
  - [x] Small, focused commits
  - [x] Clear commit messages (see CONTRIBUTING.md)

## Automated tests (real temp SQLite, no mocks)
### Data layer (repository)
- [x] Insert users, items, rentals
- [x] Query by ID and by filter
- [x] Bad inputs fail correctly without crashing
- [x] Update item status and rental status

### Constraints (repository, exact exception type asserted)
- [x] Duplicate username -> UNIQUE
- [x] Rental pointing to non-existent item -> FOREIGN KEY
- [x] Item missing required field -> NOT NULL
- [x] Item with out-of-range status -> CHECK

### Behaviour (service layer)
State transitions
- [x] First launch creates the user; second run loads the same account
- [x] Listing sets item to `available`
- [x] Recording a rental sets item to `rented`
- [x] Confirming return sets item to `available`
- [x] ...unless it was delisted while out -> stays `unlisted`

Business rules
- [x] Already-rented item cannot be rented again
- [x] Delisted item cannot be selected for rental
- [x] Renting to an existing username reuses that record
- [x] End time = start time + number of days entered

### Quality bar
- [x] Every case above has a test
- [x] Normal AND edge cases covered
- [x] Tests isolated (each builds own data; pass in any order)

## Features (demo-ready)
- [x] First launch asks for username; later runs load the account
- [x] Main menu loop with clean Exit (DB connection closed)
- [x] List an item (name, description, cost per day)
- [x] Paginated lists (5 per page, next/previous only when they exist, back to menu)
- [x] Inventory + item detail with delist / relist
- [x] Record a rental (new renter created, existing renter reused)
- [x] Confirm a return (rental closed, `returned_at` set, item status correct)
- [x] Rentals never deleted (history preserved)
- [x] State machine enforced in code, not only by DB CHECK

## Database failure handling
- [x] Typed exception: cannot open database
- [x] Typed exception: constraint violation
- [x] Typed exception: not found
- [x] Typed exception: mapping exception
- [x] Repository catches `SQLException` and throws the typed exception
- [x] Menu shows a friendly message and returns to menu instead of crashing

## Be ready to demonstrate
- [x] First launch through to main menu
- [x] Listing an item
- [x] Viewing inventory and delisting
- [x] Recording a rental with a new renter
- [x] Confirming a return
- [x] Running the test suite

## Be ready to explain
- [x] How the layered architecture is enforced in the codebase
- [x] How database exceptions are caught and handled in the repository layer