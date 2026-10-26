# Learning Notes (plain-English)

Short, non-technical explanations of what we built and how the pieces fit together.
A new section is added after every step.

---

## The big picture: our app is like a restaurant

| Restaurant | Our app | Job |
|---|---|---|
| The **waiter** who talks to customers | **Transport** | Shows menus, asks questions, reads answers. Makes no decisions. |
| The **manager** who enforces house rules | **Service** | Decides "can this item be rented?" |
| The **storeroom clerk** | **Repository** | The only one allowed to fetch or store things in the storeroom (database). |
| The **storeroom door and keys** | **Infrastructure** | Opens the connection to the database file. |
| The **menu vocabulary** (dish names, order slips) | **Domain** | The shared words everyone uses: User, Item, Rental. |
| The **storeroom** itself | **SQLite file** | Where information is kept so it survives when the app closes. |

The waiter never walks into the storeroom, and the clerk never talks to customers.
Everyone passes information down the line, and only down.
That keeps things tidy: if we change how the menu looks, the storeroom is untouched.

---

## Step 1: Setting up the workshop

**What we did:** created an empty, organised project folder and the team's ground rules.

| Piece | In plain words |
|---|---|
| **Maven / `pom.xml`** | A shopping list plus an assistant. It lists the tools and libraries we need (the SQLite driver, the test tool, the coverage tool), fetches them, and builds and tests the project with one command. |
| **Java 21** | The language we write in, version 21. |
| **`.gitignore`** | A "don't pack these" list. Build leftovers and personal editor settings stay out of the shared project. |
| **Package folders** | One labelled drawer per layer (domain, service, ...) so everyone knows where code belongs. |
| **`App.java`** | The "on" switch of the program. For now it only prints a title, to prove the build works. |
| **`CONTRIBUTING.md`** | The team handbook: how to work on your own copy, how to ask for review, how to name things. |
| **`DELIVERABLES.md`** | The checklist of everything the assignment wants handed in. |
| **Git and GitHub** | Git is a time machine for your files that remembers every saved change. GitHub is the shared online home where the team's copies meet. |
| **Branches** | Everyone works on their own *photocopy* of the project so nobody breaks the original. |
| **Pull request (PR)** | Raising your hand: "I finished something, please check it before it goes into the original." A teammate reviews and approves it. |
| **Protected `main`** | `main` is the master copy. A lock on it means changes only get in after review. |

**How it connects:** this step builds the workshop. Nothing works yet, but everything built later goes into these drawers, is built by Maven, and is shared through these Git rules.

---

## Step 2: The Domain (our shared vocabulary)

**What we did:** defined the *things* in our app and the *rules for an item's life*, with no database and no menus yet.

| Piece | In plain words |
|---|---|
| **User** | A person's card: id, username, (later) password, sign-up time. The same card type is used for owners and renters. |
| **Item** | A listing card: name, description, price per day, status, owner. |
| **Rental** | A loan slip: which item, who borrowed it, start, due date, return time, status. |
| **Record (Java)** | A card that is *sealed* once written. To change something you write a fresh copy, so two people can never accidentally scribble on the same card. |
| **ItemStatus** | The traffic light for an item: **available**, **rented** or **unlisted**. |
| **State machine** | The traffic-light rulebook. From each state only certain moves are allowed, for example you can't rent an unlisted item. |
| **RentalStatus** | A loan slip is either **active** (item is out) or **closed** (item came back). It only goes one way. |
| **Exception** | A loud alarm that rings when someone breaks a rule, instead of silently doing the wrong thing. |
| **Tests** | A robot inspector that checks every rule. If someone changes a rule by mistake, the robot complains. |

**The item's life:**

```
 listed -> AVAILABLE --rent--> RENTED --return--> AVAILABLE
              |                   |
            delist              delist (while out)
              v                   v
            UNLISTED  <-----------+
              |
            relist (only when the item is not out)
              v
           AVAILABLE
```

**How it connects to everything else:**
- The **Service** (manager) will ask the rulebook before changing any status: "is this move allowed?"
- The **Repository** (clerk) will turn database rows into these cards and cards back into rows.
- The **Transport** (waiter) will show data from these cards on screen.
- All of them use the same cards, so everyone speaks the same language.
- The Domain depends on nobody. It is the dictionary everyone else reads.

**Why no checking of empty names in the cards?** Checking user input is the manager's job (Service). Also, we need to prove later that the *database itself* refuses bad data, so the cards must be able to carry bad data that far.

---

## Where we are on the map

```
[Step 1 DONE] workshop + rules
[Step 2 DONE] vocabulary + item life rules       <- you are here
[Step 3 next] storeroom door + shelves (database connection and tables)
[Step 4     ] storeroom clerk (repositories + error alarms)
[Step 5     ] manager (services)
[Step 6     ] waiter (menus)
[Step 7     ] seed data, diagram, coverage report
```