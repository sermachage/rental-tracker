import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.Scanner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.rental.domain.User;
import com.rental.infrastructure.Database;
import com.rental.repository.*;
import com.rental.service.RentalService;
import com.rental.transport.Menu;

/** Exercises first-run account setup and the item-listing menu flow. */
class MenuTest {
    @Test
    void promptsForOwnerThenListsItemAndLoadsOwnerOnNextRun(@TempDir Path temp) throws Exception {
        try (Database database = new Database(temp.resolve("app.db"))) {
            UserRepository users = new UserRepository(database);
            ItemRepository items = new ItemRepository(database);
            RentalRepository rentals = new RentalRepository(database);
            RentalService service = new RentalService(users, items, rentals,
                    new AppConfigurationRepository(database));

            Menu firstRun = new Menu(service, users, items, new Scanner(
                    "liisa\n1\nLadder\n6-step aluminium ladder\n5\n5\n"));
            firstRun.run();

            User owner = service.findOwner().orElseThrow();
            assertEquals("liisa", owner.username());
            assertEquals(1, items.findByOwner(owner.id()).size());
            assertEquals("Ladder", items.findByOwner(owner.id()).get(0).itemName());

            new Menu(service, users, items, new Scanner("5\n")).run();
            assertEquals(owner, service.findOwner().orElseThrow());
        }
    }
}
