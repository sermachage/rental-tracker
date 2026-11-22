package com.rental.repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

import com.rental.domain.User;
import com.rental.infrastructure.Database;

/** Stores and loads users and renters. */
public class UserRepository {
    private final Database database;

    public UserRepository(Database database) {
        this.database = database;
    }

    /** Inserts a user and returns the saved copy with its generated id. */
    public User insert(User user) {
        try (PreparedStatement statement = database.connection().prepareStatement(
                "INSERT INTO users(username, password) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.username());
            statement.setString(2, user.password());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new MappingException("User insert did not return an id", null);
                }
                return findById(keys.getLong(1)).orElseThrow(
                        () -> new NotFoundException("User inserted but could not be reloaded"));
            }
        } catch (ConstraintViolationException | NotFoundException | MappingException exception) {
            throw exception;
        } catch (SQLException exception) {
            throw translate("insert user", exception);
        }
    }

    /** Finds a user by id. */
    public Optional<User> findById(long id) {
        try (PreparedStatement statement = database.connection().prepareStatement(
                "SELECT id, username, password, created_at FROM users WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(map(rows)) : Optional.empty();
            }
        } catch (MappingException exception) {
            throw exception;
        } catch (SQLException exception) {
            throw translate("find user", exception);
        }
    }

    /** Finds a user by exact username, or returns empty. */
    public Optional<User> findByUsername(String username) {
        try (PreparedStatement statement = database.connection().prepareStatement(
                "SELECT id, username, password, created_at FROM users WHERE username = ?")) {
            statement.setString(1, username);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(map(rows)) : Optional.empty();
            }
        } catch (MappingException exception) {
            throw exception;
        } catch (SQLException exception) {
            throw translate("find user", exception);
        }
    }

    private static User map(ResultSet rows) {
        try {
            String created = rows.getString("created_at");
            return new User(rows.getLong("id"), rows.getString("username"),
                    rows.getString("password"),
                    created == null ? null : LocalDateTime.parse(created.replace(' ', 'T')));
        } catch (SQLException | RuntimeException exception) {
            throw new MappingException("Could not map user row", exception);
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
