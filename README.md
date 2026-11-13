# FoodPal

A multi-user recipe and meal-planning desktop app with real-time sync. Built by a team of 6 at TU Delft (Software Engineering Project, 2025–2026).

## What it does

- **Recipes:** create, edit and search recipes with ingredient quantities and units. Search supports multi-keyword AND queries (`Banana, Honey`).
- **Real-time collaboration:** changes made by one user appear for other connected clients right away, over WebSockets.
- **Shopping list:** build a shopping list from recipes, and create new food items straight from the list.
- **Live language switching:** 6 languages (English, Dutch, German, Romanian, Bulgarian, Slovak), switchable without restarting.

## Architecture

```
JavaFX client  ──REST──▶  Spring Boot server  ──▶  persistence layer
      ▲                          │
      └──────── WebSockets ──────┘   (live updates pushed to all clients)
```

- **`client/`**: JavaFX desktop UI
- **`server/`**: Spring Boot REST API, WebSocket handlers, data persistence
- **`commons/`**: shared domain models used by both sides

## My contributions

- Designed and implemented server-side **data persistence models** in Spring Boot
- Built the **WebSocket/REST communication handlers** that keep clients in sync
- Wrote **JUnit + Mockito test suites** for server logic, including edge cases
- Took part in code review through merge requests and in weekly team meetings

## Tech stack

Java 25 · Spring Boot · JavaFX · WebSockets · REST · Maven · JUnit · Mockito · GitLab CI · Checkstyle

## Running it locally

Requires **JDK 25** (Maven is included via `mvnw`).

```bash
# 1. Start the server
mvn -pl server -am spring-boot:run

# 2. In a new terminal, start the client
mvn clean compile
mvn -pl client -am javafx:run
```

The server must be running before you start the client. To run the client from an IDE, see [docs/USER_GUIDE.md](docs/USER_GUIDE.md).

## Team

Built with 5 teammates as part of CSE1105 at TU Delft. Original development took place on the university GitLab, and the full commit history is kept here.Click the ingredient name dropdown menu.
Scroll to the very bottom of the list.
Select ‘+ Create New Food Item…’
Result: You will be redirected to the "Add Food Item" screen. After creating the new item and clicking ‘Confirm’, you will be returned to your recipe draft exactly where you left off.
2. Via the Shopping List
Click the add button (‘+’) on the bottom left of the shopping list view.
Type the name of the new item into the input field (first box) instead of selecting an existing one from the dropdown menu.
Fill in the other 2 fields (quantity and unit) and click ‘OK’.
Result: A prompt will ask if you want to create this unknown item. Click ‘OK’ to be redirected to the "Add Food Item" screen. After clicking ‘Confirm’, you are automatically returned to your shopping list, allowing you to continue where you left off.

Notes for using the searching feature:
A comma (“,”) is used as an AND operator. So for complex searches with multiple keywords, separate the keywords by a comma and a space; an example is: “Banana, Honey”

Notes for editing/deleting a food item:
After editing or removing a food item, make sure to re-select the recipe in order to see the updated changes.

