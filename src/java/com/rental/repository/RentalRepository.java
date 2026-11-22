package com.rental.repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.rental.domain.Rental;
import com.rental.domain.RentalStatus;
import com.rental.infrastructure.Database;

/** Stores rental history and active rentals. */
public class RentalRepository {
    private final Database database;

    public RentalRepository(Database database) {
        this.database = database;
    }

    /** Inserts a rental and reloads it. */
    public Rental insert(Rental rental) {
        try (PreparedStatement statement = database.connection().prepareStatement("""
                INSERT INTO rentals(item_id, renter_id, start_time, end_time, returned_at, status)
                VALUES (?, ?, ?, ?, ?, ?)""", Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, rental.itemId());
            statement.setLong(2, rental.renterId());
            statement.setString(3, rental.startTime().toString());
            statement.setString(4, rental.endTime().toString());
            if (rental.returnedAt() == null) {
                statement.setNull(5, Types.VARCHAR);
            } else {
                statement.setString(5, rental.returnedAt().toString());
            }
            statement.setString(6, rental.status().dbValue());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new MappingException("Rental insert did not return an id", null);
                }
                return findById(keys.getLong(1)).orElseThrow(
                        () -> new NotFoundException("Rental inserted but could not be reloaded"));
            }
        } catch (ConstraintViolationException | NotFoundException | MappingException exception) {
            throw exception;
        } catch (SQLException exception) {
            throw translate("insert rental", exception);
        }
    }

    /** Updates a rental to closed. */
    public Rental update(Rental rental) {
        try (PreparedStatement statement = database.connection().prepareStatement("""
                UPDATE rentals SET returned_at = ?, status = ?
                WHERE rental_id = ?""")) {
            statement.setString(1, rental.returnedAt().toString());
            statement.setString(2, rental.status().dbValue());
            statement.setLong(3, rental.rentalId());
            if (statement.executeUpdate() == 0) {
                throw new NotFoundException("Rental not found: " + rental.rentalId());
            }
            return rental;
        } catch (ConstraintViolationException | NotFoundException exception) {
            throw exception;
        } catch (SQLException exception) {
            throw translate("update rental", exception);
        }
    }

    /** Finds a rental by id. */
    public Optional<Rental> findById(long id) {
        try (PreparedStatement statement = database.connection().prepareStatement(
                "SELECT * FROM rentals WHERE rental_id = ?")) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(map(rows)) : Optional.empty();
            }
        } catch (MappingException exception) {
            throw exception;
        } catch (SQLException exception) {
            throw translate("find rental", exception);
        }
    }

    /** Finds active rentals for an owner's items, sorted by due date. */
    public List<Rental> findActiveByOwner(long ownerId) {
        List<Rental> rentals = new ArrayList<>();
        try (PreparedStatement statement = database.connection().prepareStatement("""
                SELECT r.* FROM rentals r
                JOIN listed_items i ON i.item_id = r.item_id
                WHERE i.owner_id = ? AND r.status = 'active'
                ORDER BY r.end_time, r.rental_id""")) {
            statement.setLong(1, ownerId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    rentals.add(map(rows));
                }
            }
            return rentals;
        } catch (MappingException exception) {
            throw exception;
        } catch (SQLException exception) {
            throw translate("query active rentals", exception);
        }
    }

    /** Returns true if an item has an active rental. */
    public boolean hasActiveRental(long itemId) {
        try (PreparedStatement statement = database.connection().prepareStatement("""
                SELECT 1 FROM rentals
                WHERE item_id = ? AND status = 'active'
                LIMIT 1""")) {
            statement.setLong(1, itemId);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        } catch (SQLException exception) {
            throw translate("check active rental", exception);
        }
    }

    private static Rental map(ResultSet rows) {
        try {
            String returned = rows.getString("returned_at");
            return new Rental(rows.getLong("rental_id"), rows.getLong("item_id"),
                    rows.getLong("renter_id"),
                    parseRequired(rows.getString("start_time"), "start_time"),
                    parseRequired(rows.getString("end_time"), "end_time"),
                    returned == null ? null : LocalDateTime.parse(returned),
                    RentalStatus.fromDbValue(rows.getString("status")));
        } catch (SQLException | DateTimeParseException | IllegalArgumentException exception) {
            throw new MappingException("Could not map rental row", exception);
        }
    }

    private static LocalDateTime parseRequired(String text, String column) {
        if (text == null) {
            throw new MappingException("Unexpected NULL in " + column, null);
        }
        return LocalDateTime.parse(text);
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
