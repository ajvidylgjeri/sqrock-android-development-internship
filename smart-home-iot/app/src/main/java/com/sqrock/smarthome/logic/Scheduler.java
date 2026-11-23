package com.sqrock.smarthome.logic;

/** Time helpers for the simulated clock and automation rules. Times are absolute minutes since day 1, 00:00. */
public final class Scheduler {
    private Scheduler() {}

    public static final int DAY = 1440;

    /** True when a rule set for minute-of-day {@code ruleMin} fires in the interval (fromAbs, toAbs]. */
    public static boolean fires(int ruleMin, long fromAbs, long toAbs) {
        if (toAbs <= fromAbs) return false;
        long first = Math.floorDiv(fromAbs - ruleMin, (long) DAY) + 1;
        return first * DAY + ruleMin <= toAbs;
    }

    public static int minuteOfDay(long abs) { return (int) Math.floorMod(abs, (long) DAY); }

    public static int dayNumber(long abs) { return (int) Math.floorDiv(abs, (long) DAY) + 1; }

    public static String hhmm(int minuteOfDay) { return String.format(java.util.Locale.US, "%02d:%02d", minuteOfDay / 60, minuteOfDay % 60); }

    public static String clock(long abs) { return "Day " + dayNumber(abs) + " · " + hhmm(minuteOfDay(abs)); }

    /** Parses "HH:mm" (also "H:mm" / "HHmm") into minute-of-day, or -1 when invalid. */
    public static int parseTime(String s) {
        if (s == null) return -1;
        String t = s.trim();
        try {
            int h, m;
            if (t.contains(":")) { String[] p = t.split(":"); if (p.length != 2) return -1; h = Integer.parseInt(p[0].trim()); m = Integer.parseInt(p[1].trim()); }
            else if (t.length() == 4) { h = Integer.parseInt(t.substring(0, 2)); m = Integer.parseInt(t.substring(2)); }
            else return -1;
            return h < 0 || h > 23 || m < 0 || m > 59 ? -1 : h * 60 + m;
        } catch (NumberFormatException e) { return -1; }
    }
}
