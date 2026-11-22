# Rental Tracker

A Java command-line application for listing items, lending them to renters, and tracking returns. The app stores users, listings, and rental history in SQLite.

## Requirements

- Java 21 or newer
- Maven 3.9 or newer

## Build and test

```bash
mvn clean verify
```

The command compiles the application, runs the test suite against temporary SQLite databases, and generates a JaCoCo report at [`docs/coverage/index.html`](docs/coverage/index.html).

## Run

```bash
mvn -q compile exec:java
```

The application opens `data/app.db` and creates the database schema if needed. The repository includes a seeded demo database with an owner and six listings.

## Features

- Create and persist the owner account on first use; load the existing account on later runs.
- List items with a name, description, and daily cost.
- Browse inventory, inspect item details, and delist or relist items.
- Record rentals with a renter and duration; existing renter accounts are reused.
- View active rentals by due date and confirm returns without deleting rental history.
- Enforce item and rental status transitions in the domain model.
- Paginate item lists in groups of five.

## Architecture

The application is organized into five layers:

| Layer | Package | Responsibility |
|---|---|---|
| Transport | `com.rental.transport` | Menus, prompts, and command-line output |
| Service | `com.rental.service` | Business rules and rental workflows |
| Repository | `com.rental.repository` | SQL queries and typed database errors |
| Infrastructure | `com.rental.infrastructure` | SQLite connection and schema initialization |
| Domain | `com.rental.domain` | Users, items, rentals, and lifecycle state machines |

`App` wires the layers together using constructor injection.

## Item lifecycle

```text
AVAILABLE --rent--> RENTED --return--> AVAILABLE
AVAILABLE --delist------------------> UNLISTED
RENTED --delist---------------------> UNLISTED
UNLISTED --relist-------------------> AVAILABLE
```

If an item is delisted while rented, its return leaves it `UNLISTED`; it can then be relisted.

## Database

SQLite data is stored in `data/app.db`. The schema contains:

- `users` — owner and renter accounts
- `listed_items` — item listings and current status
- `rentals` — rental and return history

See [`docs/er-diagram.md`](docs/er-diagram.md) for table columns, constraints, and relationships.