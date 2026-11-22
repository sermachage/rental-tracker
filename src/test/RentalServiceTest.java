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
            RentalService service = service(database, users, items, rentals);

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
            RentalService service = service(database, users, items, rentals);

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
            RentalService service = service(database, users, items, rentals);

            User owner = service.loadOwner("liisa");
            Item item = service.listItem(owner.id(), "Tent", "Camping", 10);
            Rental rental = service.rent(owner.id(), item.itemId(), "meelis", 2);
            service.delist(owner.id(), item.itemId());
            assertThrows(IllegalStateException.class,
                    () -> service.relist(owner.id(), item.itemId()));
            service.confirmReturn(owner.id(), rental.rentalId());
            assertEquals(ItemStatus.UNLISTED, items.findById(item.itemId()).orElseThrow().status());
            assertEquals(ItemStatus.AVAILABLE, service.relist(owner.id(), item.itemId()).status());
        }
    }

    @Test
    void rejectsRentingAnUnavailableItem(@TempDir Path temp) throws Exception {
        try (Database database = new Database(temp.resolve("app.db"))) {
            UserRepository users = new UserRepository(database);
            ItemRepository items = new ItemRepository(database);
            RentalRepository rentals = new RentalRepository(database);
            RentalService service = service(database, users, items, rentals);

            User owner = service.loadOwner("liisa");
            Item item = service.listItem(owner.id(), "Sander", "Fine", 4);
            service.delist(owner.id(), item.itemId());
            assertThrows(IllegalStateException.class,
                    () -> service.rent(owner.id(), item.itemId(), "meelis", 1));
        }
    }

    @Test
    void firstRunClaimsSeedOwnerAndLoadsItOnLaterRuns(@TempDir Path temp) throws Exception {
        try (Database database = new Database(temp.resolve("app.db"))) {
            UserRepository users = new UserRepository(database);
            ItemRepository items = new ItemRepository(database);
            RentalRepository rentals = new RentalRepository(database);
            RentalService service = service(database, users, items, rentals);

            User seededOwner = users.insert(User.newUser("demo-owner"));
            Item seededItem = items.insert(Item.newListing(
                    seededOwner.id(), "Ladder", "6 steps", 5));

            assertTrue(service.findOwner().isEmpty());
            User selectedOwner = service.loadOwner("liisa");

            assertEquals("liisa", selectedOwner.username());
            assertEquals(selectedOwner.id(),
                    items.findById(seededItem.itemId()).orElseThrow().ownerId());
            assertEquals(selectedOwner, service.findOwner().orElseThrow());
            assertEquals(selectedOwner, service.loadOwner("ignored-after-setup"));
        }
    }

    private static RentalService service(Database database, UserRepository users,
                                         ItemRepository items, RentalRepository rentals) {
        return new RentalService(users, items, rentals,
                new AppConfigurationRepository(database));
    }
}
