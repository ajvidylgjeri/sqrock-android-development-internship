package com.sqrock.freshcart.logic;

import java.util.Locale;

/** Built-in demo coupons. */
public final class Coupons {
    private Coupons() {}

    public static final class Result {
        public final boolean valid, freeShipping;
        public final double discount;
        public final String message;

        Result(boolean valid, double discount, boolean freeShipping, String message) {
            this.valid = valid; this.discount = discount; this.freeShipping = freeShipping; this.message = message;
        }
    }

    public static Result apply(String code, double subtotal) {
        if (code == null || code.trim().isEmpty()) return new Result(false, 0, false, "");
        String c = code.trim().toUpperCase(Locale.ROOT);
        switch (c) {
            case "FRESH10":
                return new Result(true, Math.min(10.0, Pricing.round2(subtotal * 0.10)), false, "FRESH10: 10% off (max $10)");
            case "WELCOME5":
                if (subtotal < 20) return new Result(false, 0, false, "WELCOME5 needs a basket of at least $20");
                return new Result(true, 5.0, false, "WELCOME5: $5 off");
            case "FREESHIP":
                return new Result(true, 0, true, "FREESHIP: free delivery");
            default:
                return new Result(false, 0, false, "Unknown coupon code");
        }
    }
}
