package com.rental.repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.rental.domain.Item;
import com.rental.domain.ItemStatus;
import com.rental.infrastructure.Database;

/** Stores and queries listed items. */
public class ItemRepository {
    private final Database database;

    public ItemRepository(Database database) {
        this.database = database;
    }

    /** Inserts a new item and reloads it with its database id and timestamp. */
    public Item insert(Item item) {
        try (PreparedStatement statement = database.connection().prepareStatement("""
                INSERT INTO listed_items(owner_id, item_name, description, cost_per_day, status)
                VALUES (?, ?, ?, ?, ?)""", Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, item.ownerId());
            statement.setString(2, item.itemName());
            statement.setString(3, item.description());
            statement.setInt(4, item.costPerDay());
            statement.setString(5, item.status().dbValue());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new MappingException("Item insert did not return an id", null);
                }
                return findById(keys.getLong(1)).orElseThrow(
                        () -> new NotFoundException("Item inserted but could not be reloaded"));
            }
        } catch (ConstraintViolationException | NotFoundException | MappingException exception) {
            throw exception;
        } catch (SQLException exception) {
            throw translate("insert item", exception);
        }
    }

    /** Updates an item's status. */
    public Item updateStatus(Item item) {
        try (PreparedStatement statement = database.connection().prepareStatement(
                "UPDATE listed_items SET status = ? WHERE item_id = ?")) {
            statement.setString(1, item.status().dbValue());
            statement.setLong(2, item.itemId());
            if (statement.executeUpdate() == 0) {
                throw new NotFoundException("Item not found: " + item.itemId());
            }
            return item;
        } catch (ConstraintViolationException | NotFoundException exception) {
            throw exception;
        } catch (SQLException exception) {
            throw translate("update item status", exception);
        }
    }

    /** Assigns seeded listings to the account selected during first-run setup. */
    public void transferOwner(long previousOwnerId, long newOwnerId) {
        try (PreparedStatement statement = database.connection().prepareStatement(
                "UPDATE listed_items SET owner_id = ? WHERE owner_id = ?")) {
            statement.setLong(1, newOwnerId);
            statement.setLong(2, previousOwnerId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw translate("transfer item ownership", exception);
        }
    }

    /** Finds an item by id. */
    public Optional<Item> findById(long id) {
        try (PreparedStatement statement = database.connection().prepareStatement(
                "SELECT * FROM listed_items WHERE item_id = ?")) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(map(rows)) : Optional.empty();
            }
        } catch (MappingException exception) {
            throw exception;
        } catch (SQLException exception) {
            throw translate("find item", exception);
        }
    }

    /** Lists an owner's items in database id order. */
    public List<Item> findByOwner(long ownerId) {
        return query("SELECT * FROM listed_items WHERE owner_id = ? ORDER BY item_id", ownerId);
    }

    /** Lists currently available items owned by a user. */
    public List<Item> findAvailableByOwner(long ownerId) {
        return query("""
                SELECT * FROM listed_items
                WHERE owner_id = ? AND status = 'available'
                ORDER BY item_id""", ownerId);
    }

    private List<Item> query(String sql, long ownerId) {
        List<Item> items = new ArrayList<>();
        try (PreparedStatement statement = database.connection().prepareStatement(sql)) {
            statement.setLong(1, ownerId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    items.add(map(rows));
                }
            }
            return items;
        } catch (MappingException exception) {
            throw exception;
        } catch (SQLException exception) {
            throw translate("query items", exception);
        }
    }

    private static Item map(ResultSet rows) {
        try {
            String created = rows.getString("created_at");
            return new Item(rows.getLong("item_id"), rows.getLong("owner_id"),
                    rows.getString("item_name"), rows.getString("description"),
                    rows.getInt("cost_per_day"),
                    ItemStatus.fromDbValue(rows.getString("status")),
                    created == null ? null : LocalDateTime.parse(created.replace(' ', 'T')));
        } catch (SQLException | RuntimeException exception) {
            throw new MappingException("Could not map item row", exception);
        }
    }

    private static RuntimeException translate(String operation, SQLException exception) {
        String text = exception.getMessage() == null ? "" : exception.getMessage().toLowerCase();
        if (text.contains("unique") || text.contains("not null") || text.contains("check")
                || text.contains("foreign key")) {
            return new ConstraintViolationException("Constraint violation while trying to " + operation, exception);
        }
        return new DatabaseException("Database failure while trying to " + operation, exception);
    }
}
