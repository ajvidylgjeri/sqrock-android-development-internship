package com.sqrock.smarthome.logic;

/** Kinds of devices with their nominal power draw and what the "level" means. */
public enum DeviceType {
    LIGHT("Light", "💡", 9, "Brightness", 0, 100, "%"),
    FAN("Fan", "🌀", 45, "Speed", 0, 100, "%"),
    AC("Air conditioner", "❄️", 1200, "Target", 16, 30, "°C"),
    TV("Television", "📺", 100, "Volume", 0, 100, "%"),
    CAMERA("Camera", "📷", 6, "", 0, 0, ""),
    LOCK("Smart lock", "🔒", 2, "", 0, 0, "");

    public final String label, icon, levelName, unit;
    public final int watts, min, max;

    DeviceType(String label, String icon, int watts, String levelName, int min, int max, String unit) {
        this.label = label; this.icon = icon; this.watts = watts; this.levelName = levelName; this.min = min; this.max = max; this.unit = unit;
    }

    public boolean hasLevel() { return max > min; }

    public int defaultLevel() { return this == AC ? 24 : hasLevel() ? 60 : 0; }

    public int clamp(int level) { return hasLevel() ? Math.max(min, Math.min(max, level)) : 0; }

    /** Present power draw in watts for an on/off state and level. */
    public double powerNow(boolean on, int level) {
        if (!on) return 0;
        if (this == LIGHT || this == FAN) return watts * clamp(level) / 100.0;
        return watts;
    }

    public String onWord() { return this == LOCK ? "Locked" : "On"; }
    public String offWord() { return this == LOCK ? "Unlocked" : "Off"; }

    public static DeviceType parse(String s) {
        try { return valueOf(s); } catch (RuntimeException e) { return LIGHT; }
    }
}
