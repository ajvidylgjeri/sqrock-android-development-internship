package com.sqrock.smarthome.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.sqrock.smarthome.data.Models.*;
import com.sqrock.smarthome.logic.DeviceType;
import com.sqrock.smarthome.logic.Energy;
import com.sqrock.smarthome.logic.Scheduler;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** SQLite storage plus the home simulation (clock, energy meter and automations). */
public class Db extends SQLiteOpenHelper {
    public static final String[] ROOM_ICONS = {"🛋️", "🛏️", "🍳", "🚿", "🚪", "🌳", "💼"};
    private static final long START_CLOCK = 7 * 60; // day 1, 07:00

    public Db(Context c) { super(c, "smarthome.db", null, 1); }

    @Override public void onConfigure(SQLiteDatabase db) { db.setForeignKeyConstraintsEnabled(true); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, email TEXT UNIQUE NOT NULL, phone TEXT, salt TEXT, hash TEXT)");
        db.execSQL("CREATE TABLE rooms(id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, name TEXT NOT NULL, icon TEXT)");
        db.execSQL("CREATE TABLE devices(id INTEGER PRIMARY KEY AUTOINCREMENT, room_id INTEGER NOT NULL REFERENCES rooms(id) ON DELETE CASCADE, name TEXT NOT NULL, type TEXT NOT NULL, on_state INTEGER DEFAULT 0, level INTEGER DEFAULT 0, wh REAL DEFAULT 0)");
        db.execSQL("CREATE TABLE automations(id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, device_id INTEGER NOT NULL REFERENCES devices(id) ON DELETE CASCADE, name TEXT, minute INTEGER NOT NULL, turn_on INTEGER NOT NULL, enabled INTEGER DEFAULT 1)");
        db.execSQL("CREATE TABLE alerts(id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, message TEXT, at TEXT, is_read INTEGER DEFAULT 0)");
        db.execSQL("CREATE TABLE clocks(user_id INTEGER PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE, abs_min INTEGER NOT NULL)");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int o, int n) { /* version 1 only */ }

    // ---------------- users ----------------
    private User user(Cursor c) {
        User u = new User();
        u.id = c.getLong(0); u.name = c.getString(1); u.email = c.getString(2); u.phone = c.getString(3);
        return u;
    }

    public User register(String name, String email, String phone, String password) {
        name = name == null ? "" : name.trim();
        email = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (name.length() < 2) throw new IllegalArgumentException("Enter your name");
        if (!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) throw new IllegalArgumentException("Enter a valid email address");
        if (password == null || password.length() < 8) throw new IllegalArgumentException("Password must have at least 8 characters");
        SQLiteDatabase db = getWritableDatabase();
        try (Cursor c = db.rawQuery("SELECT 1 FROM users WHERE email=?", new String[]{email})) {
            if (c.moveToFirst()) throw new IllegalArgumentException("This email is already registered");
        }
        String salt = Security.newSalt();
        ContentValues v = new ContentValues();
        v.put("name", name); v.put("email", email); v.put("phone", phone == null ? "" : phone.trim()); v.put("salt", salt); v.put("hash", Security.hash(password, salt));
        long id = db.insert("users", null, v);
        ContentValues ck = new ContentValues();
        ck.put("user_id", id); ck.put("abs_min", START_CLOCK);
        db.insert("clocks", null, ck);
        seedHome(id);
        return user(id);
    }

    public User login(String email, String password) {
        if (email == null || password == null) return null;
        try (Cursor c = getReadableDatabase().rawQuery("SELECT id,name,email,phone,salt,hash FROM users WHERE email=?", new String[]{email.trim().toLowerCase(Locale.ROOT)})) {
            if (!c.moveToFirst() || !Security.matches(password, c.getString(4), c.getString(5))) return null;
            return user(c);
        }
    }

    public User user(long id) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT id,name,email,phone FROM users WHERE id=?", new String[]{String.valueOf(id)})) {
            return c.moveToFirst() ? user(c) : null;
        }
    }

    public void updateProfile(long id, String name, String phone) {
        if (name == null || name.trim().length() < 2) throw new IllegalArgumentException("Enter your name");
        ContentValues v = new ContentValues();
        v.put("name", name.trim()); v.put("phone", phone == null ? "" : phone.trim());
        getWritableDatabase().update("users", v, "id=?", new String[]{String.valueOf(id)});
    }

    // ---------------- rooms ----------------
    public long addRoom(long userId, String name, String icon) {
        if (name == null || name.trim().length() < 2) throw new IllegalArgumentException("Enter a room name");
        try (Cursor c = getReadableDatabase().rawQuery("SELECT 1 FROM rooms WHERE user_id=? AND lower(name)=lower(?)", new String[]{String.valueOf(userId), name.trim()})) {
            if (c.moveToFirst()) throw new IllegalArgumentException("You already have a room called " + name.trim());
        }
        ContentValues v = new ContentValues();
        v.put("user_id", userId); v.put("name", name.trim()); v.put("icon", icon);
        return getWritableDatabase().insert("rooms", null, v);
    }

    public void renameRoom(long id, String name) {
        if (name == null || name.trim().length() < 2) throw new IllegalArgumentException("Enter a room name");
        ContentValues v = new ContentValues();
        v.put("name", name.trim());
        getWritableDatabase().update("rooms", v, "id=?", new String[]{String.valueOf(id)});
    }

    /** Deleting a room also deletes its devices and their automations (foreign-key cascade). */
    public void deleteRoom(long id) { getWritableDatabase().delete("rooms", "id=?", new String[]{String.valueOf(id)}); }

    public List<Room> rooms(long userId) {
        List<Room> out = new ArrayList<>();
        String sql = "SELECT r.id,r.name,r.icon,COUNT(d.id),COALESCE(SUM(d.on_state),0) FROM rooms r LEFT JOIN devices d ON d.room_id=r.id WHERE r.user_id=? GROUP BY r.id ORDER BY r.id";
        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(userId)})) {
            while (c.moveToNext()) {
                Room r = new Room();
                r.id = c.getLong(0); r.name = c.getString(1); r.icon = c.getString(2); r.devices = c.getInt(3); r.devicesOn = c.getInt(4);
                out.add(r);
            }
        }
        return out;
    }

    public Room room(long id) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT id,name,icon FROM rooms WHERE id=?", new String[]{String.valueOf(id)})) {
            if (!c.moveToFirst()) return null;
            Room r = new Room();
            r.id = c.getLong(0); r.name = c.getString(1); r.icon = c.getString(2);
            return r;
        }
    }

    // ---------------- devices ----------------
    private static final String DCOLS = "d.id,d.room_id,d.name,d.type,d.on_state,d.level,d.wh,r.name";

    private Device device(Cursor c) {
        Device d = new Device();
        d.id = c.getLong(0); d.roomId = c.getLong(1); d.name = c.getString(2); d.type = DeviceType.parse(c.getString(3));
        d.on = c.getInt(4) == 1; d.level = c.getInt(5); d.whTotal = c.getDouble(6); d.roomName = c.getString(7);
        return d;
    }

    public List<Device> devices(long userId, long roomId) {
        List<Device> out = new ArrayList<>();
        String sql = "SELECT " + DCOLS + " FROM devices d JOIN rooms r ON r.id=d.room_id WHERE r.user_id=?" + (roomId > 0 ? " AND r.id=?" : "") + " ORDER BY r.id,d.id";
        String[] args = roomId > 0 ? new String[]{String.valueOf(userId), String.valueOf(roomId)} : new String[]{String.valueOf(userId)};
        try (Cursor c = getReadableDatabase().rawQuery(sql, args)) {
            while (c.moveToNext()) out.add(device(c));
        }
        return out;
    }

    public Device device(long id) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT " + DCOLS + " FROM devices d JOIN rooms r ON r.id=d.room_id WHERE d.id=?", new String[]{String.valueOf(id)})) {
            return c.moveToFirst() ? device(c) : null;
        }
    }

    public long addDevice(long roomId, String name, DeviceType type) {
        if (name == null || name.trim().length() < 2) throw new IllegalArgumentException("Enter a device name");
        ContentValues v = new ContentValues();
        v.put("room_id", roomId); v.put("name", name.trim()); v.put("type", type.name()); v.put("level", type.defaultLevel());
        return getWritableDatabase().insert("devices", null, v);
    }

    public void renameDevice(long id, String name) {
        if (name == null || name.trim().length() < 2) throw new IllegalArgumentException("Enter a device name");
        ContentValues v = new ContentValues();
        v.put("name", name.trim());
        getWritableDatabase().update("devices", v, "id=?", new String[]{String.valueOf(id)});
    }

    public void deleteDevice(long id) { getWritableDatabase().delete("devices", "id=?", new String[]{String.valueOf(id)}); }

    /** Switches a device and records a notification when {@code notify} is true. */
    public void setPower(long userId, long deviceId, boolean on, String source) {
        Device d = device(deviceId);
        if (d == null) return;
        ContentValues v = new ContentValues();
        v.put("on_state", on ? 1 : 0);
        getWritableDatabase().update("devices", v, "id=?", new String[]{String.valueOf(deviceId)});
        if (d.on != on) addAlert(userId, d.name + " (" + d.roomName + ") " + (on ? d.type.onWord() : d.type.offWord()).toLowerCase(Locale.ROOT) + " · " + source);
    }

    public void setLevel(long deviceId, int level) {
        Device d = device(deviceId);
        if (d == null) return;
        ContentValues v = new ContentValues();
        v.put("level", d.type.clamp(level));
        getWritableDatabase().update("devices", v, "id=?", new String[]{String.valueOf(deviceId)});
    }

    // ---------------- clock, energy, automations ----------------
    public long clock(long userId) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT abs_min FROM clocks WHERE user_id=?", new String[]{String.valueOf(userId)})) {
            return c.moveToFirst() ? c.getLong(0) : START_CLOCK;
        }
    }

    /**
     * Moves the simulated clock forward: energy is metered for every device that is on, then each automation
     * whose time falls in the elapsed window is executed (and a notification is created).
     * @return number of automations that fired
     */
    public int advance(long userId, int minutes) {
        if (minutes <= 0 || minutes > 7 * Scheduler.DAY) throw new IllegalArgumentException("Choose between 1 minute and 7 days");
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            long from = clock(userId), to = from + minutes;
            // Meter energy in steps so automations that switch devices mid-way are respected.
            List<Automation> rules = automations(userId);
            long cursor = from;
            int fired = 0;
            while (cursor < to) {
                long next = to;
                for (Automation a : rules) {
                    if (!a.enabled) continue;
                    long first = Math.floorDiv(cursor - a.minuteOfDay, (long) Scheduler.DAY) + 1;
                    long t = first * Scheduler.DAY + a.minuteOfDay;
                    if (t > cursor && t <= to && t < next) next = t;
                }
                meter(userId, (int) (next - cursor));
                for (Automation a : rules) {
                    if (a.enabled && Scheduler.fires(a.minuteOfDay, cursor, next)) {
                        Device d = device(a.deviceId);
                        if (d == null) continue;
                        setPower(userId, a.deviceId, a.turnOn, "automation \"" + (a.name == null || a.name.isEmpty() ? "rule" : a.name) + "\" at " + Scheduler.clock(next));
                        fired++;
                    }
                }
                cursor = next;
            }
            ContentValues v = new ContentValues();
            v.put("abs_min", to);
            db.update("clocks", v, "user_id=?", new String[]{String.valueOf(userId)});
            db.setTransactionSuccessful();
            return fired;
        } finally {
            db.endTransaction();
        }
    }

    private void meter(long userId, int minutes) {
        if (minutes <= 0) return;
        for (Device d : devices(userId, 0)) {
            if (!d.on) continue;
            getWritableDatabase().execSQL("UPDATE devices SET wh = wh + ? WHERE id=?", new Object[]{Energy.wh(d.powerNow(), minutes), d.id});
        }
    }

    public double totalWh(long userId) {
        double t = 0;
        for (Device d : devices(userId, 0)) t += d.whTotal;
        return t;
    }

    public double powerNow(long userId) {
        double t = 0;
        for (Device d : devices(userId, 0)) t += d.powerNow();
        return t;
    }

    public void resetEnergy(long userId) {
        getWritableDatabase().execSQL("UPDATE devices SET wh=0 WHERE room_id IN (SELECT id FROM rooms WHERE user_id=?)", new Object[]{userId});
    }

    public List<Automation> automations(long userId) {
        List<Automation> out = new ArrayList<>();
        String sql = "SELECT a.id,a.device_id,a.name,a.minute,a.turn_on,a.enabled,d.name FROM automations a JOIN devices d ON d.id=a.device_id WHERE a.user_id=? ORDER BY a.minute,a.id";
        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(userId)})) {
            while (c.moveToNext()) {
                Automation a = new Automation();
                a.id = c.getLong(0); a.deviceId = c.getLong(1); a.name = c.getString(2); a.minuteOfDay = c.getInt(3);
                a.turnOn = c.getInt(4) == 1; a.enabled = c.getInt(5) == 1; a.deviceName = c.getString(6);
                out.add(a);
            }
        }
        return out;
    }

    public void addAutomation(long userId, long deviceId, String name, String time, boolean turnOn) {
        int m = Scheduler.parseTime(time);
        if (m < 0) throw new IllegalArgumentException("Time must look like 07:30");
        if (device(deviceId) == null) throw new IllegalArgumentException("Choose a device");
        ContentValues v = new ContentValues();
        v.put("user_id", userId); v.put("device_id", deviceId); v.put("name", name == null ? "" : name.trim()); v.put("minute", m); v.put("turn_on", turnOn ? 1 : 0);
        getWritableDatabase().insert("automations", null, v);
    }

    public void setAutomationEnabled(long id, boolean enabled) {
        ContentValues v = new ContentValues();
        v.put("enabled", enabled ? 1 : 0);
        getWritableDatabase().update("automations", v, "id=?", new String[]{String.valueOf(id)});
    }

    public void deleteAutomation(long id) { getWritableDatabase().delete("automations", "id=?", new String[]{String.valueOf(id)}); }

    // ---------------- notifications ----------------
    public void addAlert(long userId, String message) {
        ContentValues v = new ContentValues();
        v.put("user_id", userId); v.put("message", message); v.put("at", Scheduler.clock(clock(userId)));
        getWritableDatabase().insert("alerts", null, v);
    }

    public List<Alert> alerts(long userId) {
        List<Alert> out = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery("SELECT id,message,at,is_read FROM alerts WHERE user_id=? ORDER BY id DESC LIMIT 100", new String[]{String.valueOf(userId)})) {
            while (c.moveToNext()) {
                Alert a = new Alert();
                a.id = c.getLong(0); a.message = c.getString(1); a.time = c.getString(2); a.read = c.getInt(3) == 1;
                out.add(a);
            }
        }
        return out;
    }

    public int unreadAlerts(long userId) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM alerts WHERE user_id=? AND is_read=0", new String[]{String.valueOf(userId)})) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    public void markAllRead(long userId) { getWritableDatabase().execSQL("UPDATE alerts SET is_read=1 WHERE user_id=?", new Object[]{userId}); }

    public void clearAlerts(long userId) { getWritableDatabase().delete("alerts", "user_id=?", new String[]{String.valueOf(userId)}); }

    // ---------------- starter home ----------------
    private void seedHome(long userId) {
        long living = addRoom(userId, "Living room", ROOM_ICONS[0]);
        long bed = addRoom(userId, "Bedroom", ROOM_ICONS[1]);
        long kitchen = addRoom(userId, "Kitchen", ROOM_ICONS[2]);
        long hall = addRoom(userId, "Entrance", ROOM_ICONS[4]);
        long l1 = addDevice(living, "Ceiling light", DeviceType.LIGHT);
        addDevice(living, "Television", DeviceType.TV);
        addDevice(living, "Air conditioner", DeviceType.AC);
        long b1 = addDevice(bed, "Bedside lamp", DeviceType.LIGHT);
        addDevice(bed, "Ceiling fan", DeviceType.FAN);
        addDevice(kitchen, "Kitchen light", DeviceType.LIGHT);
        long door = addDevice(hall, "Front door lock", DeviceType.LOCK);
        addDevice(hall, "Door camera", DeviceType.CAMERA);
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE devices SET on_state=1 WHERE id IN (?,?)", new Object[]{door, l1});
        addAutomation(userId, l1, "Evening lights", "18:30", true);
        addAutomation(userId, l1, "Lights out", "23:00", false);
        addAutomation(userId, door, "Lock at night", "22:30", true);
        addAutomation(userId, b1, "Wake-up lamp", "06:45", true);
        addAlert(userId, "Welcome home! Your demo home has 4 rooms and 8 devices.");
    }
}
