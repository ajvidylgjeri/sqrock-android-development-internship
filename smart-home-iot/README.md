# Smart Home IoT – Android App

Sqrock IT Solutions · Android Development Internship · Phase 2 · Task 3

A native Android app (Java, SQLite, no third-party libraries) that manages a simulated smart home:
rooms, devices, energy use and daily automations. Devices are simulated, so no hardware is needed.

## Features
- **Auth** – register / login with PBKDF2-hashed passwords, persistent session, logout. Each new account gets a starter home (4 rooms, 8 devices, 4 automations).
- **Dashboard** – live power draw, energy used and estimated cost, rooms carousel, devices that are currently on, and a **simulated clock** (+30 min / +1 h / +6 h / +1 day).
- **Device control** – on/off (lock/unlock), brightness / fan speed / AC temperature / volume via a slider, per-device energy, rename, remove.
- **Room management** – add rooms with an icon, rename, delete (devices and their automations are removed with it), add devices of 6 types.
- **Automation & scheduling** – daily rules "turn X on/off at HH:mm", pause/resume/delete. They run when the simulated clock passes the time, including across midnight; energy is metered in steps so a rule that switches a device mid-way is accounted correctly.
- **Notifications** – every manual change and automation run is logged, unread badge on the tab, mark read / clear.
- **Profile** – edit name/phone, dark mode, reset energy meters, logout.

Energy model: `Wh = watts × minutes / 60`, tariff $0.15 per kWh. Lights and fans scale with their level; AC, TV, camera and lock draw their rated power.

## Setup
1. Install **Android Studio** (Koala or newer) with an Android SDK for API 34.
2. `File ▸ Open` this folder and let Gradle sync (Studio creates the wrapper and downloads AGP 8.7.3).
3. Run on an emulator or device with Android 8.0 (API 26) or newer.

## Structure
```
app/src/main/java/com/sqrock/smarthome
├── MainActivity.java   navigation stack, top bar (shows the simulated clock), 5-tab bottom nav, session, theme
├── logic/              DeviceType, Energy, Scheduler – pure Java, unit-testable
├── data/               Models, Db (SQLiteOpenHelper, simulation engine), Security (PBKDF2)
├── screens/            Auth, Dashboard, Rooms, Room, Device, Automations, Alerts, Profile
└── ui/Ui.java          code-built views (no XML layouts)
```

## Run the logic tests
```bash
javac -d /tmp/lt app/src/main/java/com/sqrock/smarthome/logic/*.java app/src/test/java/com/sqrock/smarthome/LogicTests.java
java -cp /tmp/lt com.sqrock.smarthome.LogicTests
```
Last result: 28 passed, 0 failed (`docs/logic_test_results.txt`).

## Honest status
- All sources compile against the Android API 33 `android.jar` and the logic tests pass.
- The app was **not run on an emulator or device** where it was written (no Android SDK access), so there are no real screenshots or APK.
  Build it in Android Studio and add screenshots to a `screenshots/` folder.
- Devices and the clock are simulated; there is no cloud, MQTT or real IoT hardware, and notifications are in-app only.
