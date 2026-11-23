package com.sqrock.freshcart.logic;

/** Price arithmetic shared by the cart, checkout and order code. All amounts are dollars rounded to cents. */
public final class Pricing {
    public static final double FREE_DELIVERY_FROM = 30.0;
    public static final double DELIVERY_FEE = 2.99;

    private Pricing() {}

    public static double round2(double v) { return Math.round(v * 100.0) / 100.0; }

    public static double unitPrice(double price, int discountPct) {
        if (discountPct < 0 || discountPct > 90) throw new IllegalArgumentException("discount must be 0-90%");
        return round2(price * (100 - discountPct) / 100.0);
    }

    public static double lineTotal(double price, int discountPct, int qty) {
        if (qty < 0) throw new IllegalArgumentException("quantity cannot be negative");
        return round2(unitPrice(price, discountPct) * qty);
    }

    public static double delivery(double subtotalAfterDiscount, boolean freeShippingCoupon) {
        if (subtotalAfterDiscount <= 0 || freeShippingCoupon || subtotalAfterDiscount >= FREE_DELIVERY_FROM) return 0;
        return DELIVERY_FEE;
    }

    /** Result of pricing a basket. */
    public static final class Totals {
        public final double subtotal, couponDiscount, delivery, total;
        public final String couponMessage;

        Totals(double subtotal, double couponDiscount, double delivery, String couponMessage) {
            this.subtotal = subtotal; this.couponDiscount = couponDiscount; this.delivery = delivery;
            this.total = round2(subtotal - couponDiscount + delivery); this.couponMessage = couponMessage;
        }
    }

    public static Totals totals(double subtotal, String couponCode) {
        Coupons.Result c = Coupons.apply(couponCode, subtotal);
        double after = round2(subtotal - c.discount);
        return new Totals(round2(subtotal), c.discount, delivery(after, c.freeShipping), c.message);
    }
}
