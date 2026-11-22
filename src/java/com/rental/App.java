package com.rental;

import java.nio.file.Path;

import com.rental.infrastructure.Database;
import com.rental.repository.ItemRepository;
import com.rental.repository.RentalRepository;
import com.rental.repository.UserRepository;
import com.rental.repository.AppConfigurationRepository;
import com.rental.service.RentalService;
import com.rental.transport.Menu;

/**
 * Entry point of the Rental Tracker CLI.
 */
public class App {

    /** Returns the banner text (kept separate from main() so it is easy to test). */
    public static String banner() {
        return "=== Rental tracker ===";
    }

    /** JVM calls this method first when the program starts. */
    public static void main(String[] args) {
        try (Database database = new Database(Path.of("data", "app.db"))) {
            UserRepository users = new UserRepository(database);
            ItemRepository items = new ItemRepository(database);
            RentalRepository rentals = new RentalRepository(database);
            AppConfigurationRepository configuration = new AppConfigurationRepository(database);
            RentalService service = new RentalService(users, items, rentals, configuration);
            new Menu(service, users, items).run();
        } catch (Exception exception) {
            System.out.println("The application could not start: " + exception.getMessage());
        }
    }
}