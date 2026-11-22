import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.rental.domain.*;
import com.rental.infrastructure.Database;
import com.rental.repository.*;
import com.rental.service.RentalService;

/** Tests the main rental workflow against real SQLite. */
class RentalServiceTest {
    @Test
    void listsRentsAndReturnsAnItem(@TempDir Path temp) throws Exception {
        try (Database database = new Database(temp.resolve("app.db"))) {
            UserRepository users = new UserRepository(database);
            ItemRepository items = new ItemRepository(database);
            RentalRepository rentals = new RentalRepository(database);
            RentalService service = new RentalService(users, items, rentals);

            User owner = service.loadOwner("liisa");
            Item item = service.listItem(owner.id(), "Ladder", "6 steps", 5);
            Rental rental = service.rent(owner.id(), item.itemId(), "meelis", 3);
            assertEquals(RentalStatus.ACTIVE, rental.status());
            assertEquals(ItemStatus.RENTED, items.findById(item.itemId()).orElseThrow().status());

            Rental closed = service.confirmReturn(owner.id(), rental.rentalId());
            assertEquals(RentalStatus.CLOSED, closed.status());
            assertEquals(ItemStatus.AVAILABLE, items.findById(item.itemId()).orElseThrow().status());
        }
    }

    @Test
    void reusesExistingRenter(@TempDir Path temp) throws Exception {
        try (Database database = new Database(temp.resolve("app.db"))) {
            UserRepository users = new UserRepository(database);
            ItemRepository items = new ItemRepository(database);
            RentalRepository rentals = new RentalRepository(database);
            RentalService service = new RentalService(users, items, rentals);

            User owner = service.loadOwner("liisa");
            Item first = service.listItem(owner.id(), "Ladder", "Tall", 5);
            Item second = service.listItem(owner.id(), "Drill", "Cordless", 8);
            User renter = service.renter("meelis");
            Rental one = service.rent(owner.id(), first.itemId(), renter.username(), 2);
            Rental two = service.rent(owner.id(), second.itemId(), renter.username(), 2);
            assertEquals(renter.id(), one.renterId());
            assertEquals(renter.id(), two.renterId());
        }
    }

    @Test
    void delistedRentedItemStaysUnlistedOnReturn(@TempDir Path temp) throws Exception {
        try (Database database = new Database(temp.resolve("app.db"))) {
            UserRepository users = new UserRepository(database);
            ItemRepository items = new ItemRepository(database);
            RentalRepository rentals = new RentalRepository(database);
            RentalService service = new RentalService(users, items, rentals);

            User owner = service.loadOwner("liisa");
            Item item = service.listItem(owner.id(), "Tent", "Camping", 10);
            Rental rental = service.rent(owner.id(), item.itemId(), "meelis", 2);
            service.delist(owner.id(), item.itemId());
            service.confirmReturn(owner.id(), rental.rentalId());
            assertEquals(ItemStatus.UNLISTED, items.findById(item.itemId()).orElseThrow().status());
            assertThrows(IllegalStateException.class,
                    () -> service.relist(owner.id(), item.itemId()));
        }
    }

    @Test
    void rejectsRentingAnUnavailableItem(@TempDir Path temp) throws Exception {
        try (Database database = new Database(temp.resolve("app.db"))) {
            UserRepository users = new UserRepository(database);
            ItemRepository items = new ItemRepository(database);
            RentalRepository rentals = new RentalRepository(database);
            RentalService service = new RentalService(users, items, rentals);

            User owner = service.loadOwner("liisa");
            Item item = service.listItem(owner.id(), "Sander", "Fine", 4);
            service.delist(owner.id(), item.itemId());
            assertThrows(IllegalStateException.class,
                    () -> service.rent(owner.id(), item.itemId(), "meelis", 1));
        }
    }
}
