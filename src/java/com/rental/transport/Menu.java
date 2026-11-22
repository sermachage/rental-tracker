package com.rental.transport;

import java.util.List;
import java.util.Scanner;

import com.rental.domain.*;
import com.rental.repository.*;
import com.rental.service.RentalService;

/** Interactive command-line transport for the rental tracker. */
public class Menu {
    private static final int PAGE_SIZE = 5;

    private final RentalService service;
    private final UserRepository users;
    private final ItemRepository items;
    private final Scanner input;
    private User owner;

    public Menu(RentalService service, UserRepository users, ItemRepository items) {
        this(service, users, items, new Scanner(System.in));
    }

    /** Creates the menu with an input source, allowing the console flow to be tested. */
    public Menu(RentalService service, UserRepository users, ItemRepository items, Scanner input) {
        this.service = service;
        this.users = users;
        this.items = items;
        this.input = input;
    }

    /** Runs the main menu until the user chooses Exit. */
    public void run() {
        owner = service.findOwner().orElseGet(this::createOwner);
        System.out.println("Welcome, " + owner.username() + ".");
        System.out.println(AppBanner());
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = input.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> listItem();
                    case "2" -> inventory();
                    case "3" -> recordRental();
                    case "4" -> confirmReturn();
                    case "5", "0" -> running = false;
                    default -> System.out.println("Please choose an option from the menu.");
                }
            } catch (RuntimeException exception) {
                printFriendlyError(exception);
            }
        }
        System.out.println("Goodbye.");
    }

    private User createOwner() {
        System.out.print("Choose a username: ");
        return service.loadOwner(input.nextLine());
    }

    private void printMainMenu() {
        System.out.println("\n=== Rental tracker ===");
        System.out.println("1) List an item");
        System.out.println("2) View my inventory");
        System.out.println("3) Record a rental");
        System.out.println("4) Confirm a return");
        System.out.println("5) Exit");
        System.out.print("> ");
    }

    private void listItem() {
        System.out.print("Item name: ");
        String name = input.nextLine();
        System.out.print("Description: ");
        String description = input.nextLine();
        System.out.print("Cost per day: ");
        int cost = readInt();
        Item item = service.listItem(owner.id(), name, description, cost);
        System.out.println("Listed " + item.itemName() + ".");
    }

    private void inventory() {
        pagedItems("=== My inventory ===", service.inventory(owner.id()), false);
    }

    private void recordRental() {
        pagedItems("=== Available items ===", service.availableInventory(owner.id()), true);
    }

    private void pagedItems(String title, List<Item> rows, boolean rentalMode) {
        int page = 0;
        while (true) {
            System.out.println("\n" + title);
            int start = page * PAGE_SIZE;
            int end = Math.min(start + PAGE_SIZE, rows.size());
            for (int i = start; i < end; i++) {
                Item item = rows.get(i);
                System.out.println((i - start + 1) + ") " + item.itemName()
                        + "  " + (rentalMode ? item.costPerDay() + " per day" : item.status().dbValue()));
            }
            int choiceNumber = printPageControls(page, rows.size());
            System.out.print("> ");
            String choice = input.nextLine().trim();
            if (choice.equalsIgnoreCase("back")) {
                return;
            }
            if (choice.equalsIgnoreCase("next") && end < rows.size()) {
                page++;
                continue;
            }
            if (choice.equalsIgnoreCase("previous") && page > 0) {
                page--;
                continue;
            }
            try {
                int selected = Integer.parseInt(choice);
                if (selected >= 1 && selected <= end - start) {
                    Item item = rows.get(start + selected - 1);
                    if (rentalMode) {
                        rentalDetail(item);
                    } else {
                        inventoryDetail(item);
                    }
                    return;
                }
            } catch (NumberFormatException ignored) {
                // Invalid controls are handled as a menu choice error.
            }
            System.out.println("Please choose a listed control.");
        }
    }

    private int printPageControls(int page, int size) {
        int end = Math.min((page + 1) * PAGE_SIZE, size);
        int option = 1;
        if (page > 0) {
            System.out.println("previous");
        }
        if (end < size) {
            System.out.println("next");
        }
        System.out.println("back");
        return option;
    }

    private void inventoryDetail(Item item) {
        printItemDetail(item);
        System.out.println("1) " + (item.status() == ItemStatus.UNLISTED ? "Relist" : "Delist"));
        System.out.println("2) Back to list");
        System.out.print("> ");
        String choice = input.nextLine().trim();
        if ("1".equals(choice)) {
            Item updated = item.status() == ItemStatus.UNLISTED
                    ? service.relist(owner.id(), item.itemId())
                    : service.delist(owner.id(), item.itemId());
            System.out.println("Item status: " + updated.status().dbValue());
        }
    }

    private void rentalDetail(Item item) {
        printItemDetail(item);
        System.out.print("Renter name: ");
        String renter = input.nextLine();
        System.out.print("Duration in days: ");
        int days = readInt();
        Rental rental = service.rent(owner.id(), item.itemId(), renter, days);
        System.out.println("Rental recorded until " + rental.endTime() + ".");
    }

    private void printItemDetail(Item item) {
        User itemOwner = users.findById(item.ownerId()).orElseThrow(
                () -> new NotFoundException("Owner not found"));
        System.out.println("\n=== " + item.itemName() + " ===");
        System.out.println("description    " + item.description());
        System.out.println("cost per day   " + item.costPerDay());
        System.out.println("status         " + item.status().dbValue());
        System.out.println("owner          " + itemOwner.username());
        System.out.println("listed         " + item.createdAt());
    }

    private void confirmReturn() {
        List<Rental> rentals = service.activeRentals(owner.id());
        for (int i = 0; i < rentals.size(); i++) {
            Rental rental = rentals.get(i);
            Item item = items.findById(rental.itemId()).orElseThrow(
                    () -> new NotFoundException("Rental item not found"));
            User renter = users.findById(rental.renterId()).orElseThrow(
                    () -> new NotFoundException("Renter not found"));
            System.out.println((i + 1) + ") " + item.itemName() + "  " + renter.username()
                    + "  due " + rental.endTime());
        }
        if (rentals.isEmpty()) {
            System.out.println("There are no active rentals.");
            return;
        }
        System.out.println("back");
        System.out.print("> ");
        String choice = input.nextLine().trim();
        if (choice.equalsIgnoreCase("back")) {
            return;
        }
        try {
            int selected = Integer.parseInt(choice);
            if (selected >= 1 && selected <= rentals.size()) {
                returnDetail(rentals.get(selected - 1));
            }
        } catch (NumberFormatException ignored) {
            System.out.println("Please choose an active rental.");
        }
    }

    private void returnDetail(Rental rental) {
        Item item = items.findById(rental.itemId()).orElseThrow(
                () -> new NotFoundException("Rental item not found"));
        User renter = users.findById(rental.renterId()).orElseThrow(
                () -> new NotFoundException("Renter not found"));
        System.out.println("\n=== " + item.itemName() + " ===");
        System.out.println("renter   " + renter.username());
        System.out.println("start    " + rental.startTime());
        System.out.println("end      " + rental.endTime());
        System.out.println("status   " + rental.status().dbValue());
        System.out.println("1) Confirm return");
        System.out.println("2) Back to list");
        System.out.print("> ");
        if ("1".equals(input.nextLine().trim())) {
            service.confirmReturn(owner.id(), rental.rentalId());
            System.out.println("Rental returned.");
        }
    }

    private int readInt() {
        try {
            return Integer.parseInt(input.nextLine().trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Please enter a whole number");
        }
    }

    private void printFriendlyError(RuntimeException exception) {
        if (exception instanceof ConstraintViolationException) {
            System.out.println("That change conflicts with saved data.");
        } else if (exception instanceof NotFoundException) {
            System.out.println("The requested record could not be found.");
        } else if (exception instanceof MappingException) {
            System.out.println("Saved data could not be read.");
        } else if (exception instanceof DatabaseException) {
            System.out.println("The database could not complete the operation.");
        } else {
            System.out.println(exception.getMessage());
        }
    }

    private String AppBanner() {
        return "=== Rental tracker ===";
    }
}
