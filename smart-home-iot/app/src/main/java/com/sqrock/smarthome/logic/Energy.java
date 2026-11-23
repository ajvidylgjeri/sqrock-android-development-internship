package com.sqrock.smarthome.logic;

/** Energy and cost maths. */
public final class Energy {
    private Energy() {}

    public static final double TARIFF_PER_KWH = 0.15;

    /** Watt-hours used by a constant load over some minutes. */
    public static double wh(double watts, int minutes) { return watts * minutes / 60.0; }

    public static double kwh(double wh) { return wh / 1000.0; }

    public static double cost(double wh) { return Math.round(kwh(wh) * TARIFF_PER_KWH * 100.0) / 100.0; }

    public static String formatKwh(double wh) { return String.format(java.util.Locale.US, "%.2f kWh", kwh(wh)); }
}
