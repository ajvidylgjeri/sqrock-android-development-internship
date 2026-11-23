package com.sqrock.smarthome.data;

import com.sqrock.smarthome.logic.DeviceType;

public final class Models {
    private Models() {}

    public static class User { public long id; public String name, email, phone; }

    public static class Room {
        public long id;
        public String name, icon;
        public int devices, devicesOn;
    }

    public static class Device {
        public long id, roomId;
        public String name, roomName;
        public DeviceType type;
        public boolean on;
        public int level;
        public double whTotal;
        public double powerNow() { return type.powerNow(on, level); }
        public String stateText() { return on ? type.onWord() + (type.hasLevel() ? " · " + level + type.unit : "") : type.offWord(); }
    }

    public static class Automation {
        public long id, deviceId;
        public String deviceName, name;
        public int minuteOfDay;
        public boolean turnOn, enabled;
        public int lastFiredDay;
    }

    public static class Alert {
        public long id;
        public String message, time;
        public boolean read;
    }
}
