# Rental Tracker ER Diagram

```text
+-------------+       +----------------+       +----------------+
| users       |       | listed_items   |       | rentals        |
+-------------+       +----------------+       +----------------+
| id PK       |<--|   | item_id PK     |<--|   | rental_id PK   |
| username    |  owner  | owner_id FK    |  item | item_id FK     |
| password    |       | item_name      |       | renter_id FK   |
| created_at  |       | description    |       | start_time     |
+-------------+       | cost_per_day   |       | end_time       |
                       | status         |       | returned_at    |
                       | created_at     |       | status         |
                       +----------------+       +----------------+
                               owner_id references users.id
                               rental.item_id references listed_items.item_id
                               rental.renter_id references users.id

                       +--------------------+
                       | app_configuration  |
                       +--------------------+
                       | singleton_id PK    |
                       | owner_id FK        |----> users.id
                       +--------------------+
```

## Constraints

- `app_configuration` contains at most one row (`singleton_id = 1`) and stores the selected owner.
- `users.username` is `NOT NULL` and `UNIQUE`.
- `listed_items.owner_id`, `item_name`, `description`, `cost_per_day`, and `status` are required.
- `cost_per_day >= 0`.
- `listed_items.status` is `available`, `rented`, or `unlisted`.
- `rentals.item_id`, `renter_id`, `start_time`, `end_time`, and `status` are required.
- `rentals.status` is `active` or `closed`.
