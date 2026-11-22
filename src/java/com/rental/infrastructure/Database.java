package com.rental.infrastructure;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import com.rental.repository.DatabaseException;

/** Opens the SQLite database and owns the single application connection. */
public class Database implements AutoCloseable {

    private final Connection connection;

    /** Opens {@code databasePath}, creates parent directories, and initializes the schema. */
    public Database(Path databasePath) {
        try {
            if (databasePath.getParent() != null) {
                Files.createDirectories(databasePath.getParent());
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            connection.setAutoCommit(true);
            createSchema(connection);
        } catch (Exception exception) {
            throw new DatabaseException("Cannot open database", exception);
        }
    }

    /** @return the shared JDBC connection */
    public Connection connection() {
        return connection;
    }

    private static void createSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS app_configuration (
                    singleton_id INTEGER PRIMARY KEY CHECK (singleton_id = 1),
                    owner_id INTEGER NOT NULL,
                    FOREIGN KEY(owner_id) REFERENCES users(id)
                )
                """);
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS listed_items (
                    item_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    owner_id INTEGER NOT NULL,
                    item_name TEXT NOT NULL,
                    description TEXT NOT NULL,
                    cost_per_day INTEGER NOT NULL CHECK (cost_per_day >= 0),
                    status TEXT NOT NULL CHECK (status IN ('available', 'rented', 'unlisted')),
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY(owner_id) REFERENCES users(id)
                )
                """);
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS rentals (
                    rental_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    item_id INTEGER NOT NULL,
                    renter_id INTEGER NOT NULL,
                    start_time TEXT NOT NULL,
                    end_time TEXT NOT NULL,
                    returned_at TEXT,
                    status TEXT NOT NULL CHECK (status IN ('active', 'closed')),
                    FOREIGN KEY(item_id) REFERENCES listed_items(item_id),
                    FOREIGN KEY(renter_id) REFERENCES users(id)
                )
                """);
            statement.executeUpdate("PRAGMA foreign_keys = ON");
        }
    }

    /** Closes the database connection. */
    @Override
    public void close() throws Exception {
        connection.close();
    }
}
