package com.rental.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import com.rental.infrastructure.Database;

/** Persists the owner account selected during first-run setup. */
public class AppConfigurationRepository {
    private final Database database;

    public AppConfigurationRepository(Database database) {
        this.database = database;
    }

    /** Returns the configured owner id, or empty before first-run setup. */
    public Optional<Long> findOwnerId() {
        try (PreparedStatement statement = database.connection().prepareStatement(
                "SELECT owner_id FROM app_configuration WHERE singleton_id = 1");
             ResultSet rows = statement.executeQuery()) {
            if (!rows.next()) {
                return Optional.empty();
            }
            long ownerId = rows.getLong("owner_id");
            if (rows.wasNull()) {
                throw new MappingException("Configured owner id is NULL", null);
            }
            return Optional.of(ownerId);
        } catch (MappingException exception) {
            throw exception;
        } catch (SQLException exception) {
            throw new DatabaseException("Could not load application configuration", exception);
        }
    }

    /** Saves or replaces the configured owner id. */
    public void saveOwnerId(long ownerId) {
        try (PreparedStatement statement = database.connection().prepareStatement("""
                INSERT INTO app_configuration(singleton_id, owner_id) VALUES (1, ?)
                ON CONFLICT(singleton_id) DO UPDATE SET owner_id = excluded.owner_id""")) {
            statement.setLong(1, ownerId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            String message = exception.getMessage() == null ? "" : exception.getMessage().toLowerCase();
            if (message.contains("constraint") || message.contains("foreign key")) {
                throw new ConstraintViolationException(
                        "Could not save configured owner", exception);
            }
            throw new DatabaseException("Could not save application configuration", exception);
        }
    }
}
