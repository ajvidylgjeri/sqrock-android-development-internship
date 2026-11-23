package com.sqrock.smarthome;

import com.sqrock.smarthome.logic.DeviceType;
import com.sqrock.smarthome.logic.Energy;
import com.sqrock.smarthome.logic.Scheduler;

/** Dependency-free tests of the pure rules (energy, scheduling, device power). */
public class LogicTests {
    static int passed, failed;

    static void eq(String name, Object exp, Object act) {
        if (exp.equals(act)) passed++; else { failed++; System.out.println("FAIL " + name + ": expected " + exp + " got " + act); }
    }

    public static void main(String[] args) {
        eq("100W for 30 min = 50Wh", 50.0, Energy.wh(100, 30));
        eq("1500Wh = 1.5kWh", 1.5, Energy.kwh(1500));
        eq("cost of 10 kWh", 1.5, Energy.cost(10_000));
        eq("cost rounding", 0.0, Energy.cost(10));

        eq("light at 50% draws 4.5W", 4.5, DeviceType.LIGHT.powerNow(true, 50));
        eq("off draws 0", 0.0, DeviceType.AC.powerNow(false, 24));
        eq("AC draws full power", 1200.0, DeviceType.AC.powerNow(true, 20));
        eq("level clamp max", 100, DeviceType.LIGHT.clamp(500));
        eq("AC clamp min", 16, DeviceType.AC.clamp(5));
        eq("lock has no level", false, DeviceType.LOCK.hasLevel());
        eq("parse unknown type", DeviceType.LIGHT, DeviceType.parse("???"));

        eq("fires inside window", true, Scheduler.fires(8 * 60, 7 * 60, 9 * 60));
        eq("fires at end (inclusive)", true, Scheduler.fires(9 * 60, 7 * 60, 9 * 60));
        eq("not at start (exclusive)", false, Scheduler.fires(7 * 60, 7 * 60, 8 * 60));
        eq("not outside window", false, Scheduler.fires(20 * 60, 7 * 60, 9 * 60));
        eq("fires across midnight", true, Scheduler.fires(60, 23 * 60, 25 * 60 + 30));
        eq("fires on a later day", true, Scheduler.fires(6 * 60, 7 * 60, 2 * 1440));
        eq("empty interval", false, Scheduler.fires(8 * 60, 9 * 60, 9 * 60));
        eq("minute of day", 90, Scheduler.minuteOfDay(1440 + 90));
        eq("day number", 2, Scheduler.dayNumber(1440));
        eq("clock text", "Day 2 · 01:30", Scheduler.clock(1440 + 90));
        eq("parse 07:30", 450, Scheduler.parseTime("07:30"));
        eq("parse 7:05", 425, Scheduler.parseTime("7:05"));
        eq("parse 2230", 1350, Scheduler.parseTime("2230"));
        eq("reject 24:00", -1, Scheduler.parseTime("24:00"));
        eq("reject 12:60", -1, Scheduler.parseTime("12:60"));
        eq("reject text", -1, Scheduler.parseTime("abc"));
        eq("reject null", -1, Scheduler.parseTime(null));

        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }
}
