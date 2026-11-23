# FreshCart – Grocery Delivery App (Android)

Sqrock IT Solutions · Android Development Internship · Phase 2 · Task 1

A native Android grocery shopping app written in **Java** with a local **SQLite** database.
It has no third-party dependencies (framework APIs only), so it builds with just Android Studio.

## Features
- **Auth** – register / login, PBKDF2-hashed passwords, session kept across restarts, logout
- **Browse** – 7 categories, search, daily offers, featured and best-selling carousels, 19 seeded products
- **Product page** – description, stock, discount, quantity picker, wishlist (♡)
- **Cart** – change quantities, stock limits, coupons (`FRESH10`, `WELCOME5`, `FREESHIP`), live totals, free delivery from $30
- **Checkout** – saved addresses, cash or demo-card payment (simulated, nothing is charged or stored), transactional order placement with stock decrement
- **Orders** – history, tracking (Confirmed → Preparing → Out for delivery → Delivered), cancellation until dispatch (stock is returned)
- **Profile** – edit name/phone, addresses, wishlist, dark mode toggle
- **Admin panel** – sales dashboard (revenue, order counts, bar chart by status), advance order status, product CRUD

Demo admin: `admin@freshcart.app` / `Admin@123` (seeded on first launch). Change it before any real use.

## Setup
1. Install **Android Studio** (Koala or newer) with an Android SDK for API 34.
2. `File ▸ Open` this folder. Let Gradle sync (Studio creates the Gradle wrapper and downloads AGP 8.7.3).
3. Pick an emulator or a device (Android 8.0 / API 26 or newer) and press **Run ▶**.

## Architecture
```
app/src/main/java/com/sqrock/freshcart
├── MainActivity.java      single activity: navigation stack, top bar, bottom nav, session, theme
├── logic/                 pure Java business rules (Pricing, Coupons, OrderStatus) – unit-testable
├── data/                  Models, Db (SQLiteOpenHelper + all queries), Security (PBKDF2)
├── screens/               one class per screen (Auth, Home, ProductList, Product, Cart, Checkout,
│                          Orders, OrderDetail, Profile, Admin, ProductForm)
└── ui/Ui.java             small helpers that build views in code (no XML layouts)
```
Order placement and cancellation run inside SQLite transactions so stock and orders never get out of sync.

## Run the logic tests
The pricing, coupon and order-status rules are plain Java and can be tested without Android:
```bash
javac -d /tmp/lt app/src/main/java/com/sqrock/freshcart/logic/*.java app/src/test/java/com/sqrock/freshcart/LogicTests.java
java -cp /tmp/lt com.sqrock.freshcart.LogicTests
```
Last result: 28 passed, 0 failed (`docs/logic_test_results.txt`).

## Honest status
- All sources compile against the Android API 33 `android.jar`, and the logic tests pass.
- The app was **not run on an emulator or device** in the environment where it was written (no Android SDK / Gradle access),
  so there are no real screenshots or APK yet. Build it in Android Studio and add screenshots to a `screenshots/` folder.
- Payments are simulated; products are seed data; no backend or push notifications.
