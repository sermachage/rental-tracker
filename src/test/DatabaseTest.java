import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.sql.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.rental.infrastructure.Database;

/** Tests real SQLite schema creation and constraints. */
class DatabaseTest {
    @Test
    void createsAllTables(@TempDir Path temp) throws Exception {
        try (Database database = new Database(temp.resolve("app.db"))) {
            try (ResultSet rows = database.connection().createStatement().executeQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name")) {
                java.util.Set<String> names = new java.util.HashSet<>();
                while (rows.next()) names.add(rows.getString(1));
                assertTrue(names.contains("users"));
                assertTrue(names.contains("listed_items"));
                assertTrue(names.contains("rentals"));
                assertTrue(names.contains("app_configuration"));
            }
        }
    }

    @Test
    void rejectsDuplicateUsername(@TempDir Path temp) throws Exception {
        try (Database database = new Database(temp.resolve("app.db"))) {
            try (Statement statement = database.connection().createStatement()) {
                statement.executeUpdate("INSERT INTO users(username) VALUES ('liisa')");
                assertThrows(SQLException.class, () ->
                        statement.executeUpdate("INSERT INTO users(username) VALUES ('liisa')"));
            }
        }
    }

    @Test
    void rejectsUnknownForeignKey(@TempDir Path temp) throws Exception {
        try (Database database = new Database(temp.resolve("app.db"))) {
            assertThrows(SQLException.class, () -> {
                try (Statement statement = database.connection().createStatement()) {
                    statement.executeUpdate("""
                        INSERT INTO listed_items(owner_id, item_name, description, cost_per_day, status)
                        VALUES (999, 'Ladder', 'Tall', 5, 'available')""");
                }
            });
        }
    }
}
