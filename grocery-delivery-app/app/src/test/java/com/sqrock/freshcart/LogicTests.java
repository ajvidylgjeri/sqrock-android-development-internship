package com.sqrock.freshcart;

import com.sqrock.freshcart.logic.Coupons;
import com.sqrock.freshcart.logic.OrderStatus;
import com.sqrock.freshcart.logic.Pricing;

/** Dependency-free tests of the pure business rules. Run: see README ("Run the logic tests"). */
public class LogicTests {
    static int passed, failed;

    static void eq(String name, Object exp, Object act) {
        if (exp.equals(act)) passed++; else { failed++; System.out.println("FAIL " + name + ": expected " + exp + " got " + act); }
    }

    public static void main(String[] args) {
        eq("discounted unit price", 1.8, Pricing.unitPrice(2.0, 10));
        eq("no discount", 2.0, Pricing.unitPrice(2.0, 0));
        eq("line total", 5.4, Pricing.lineTotal(2.0, 10, 3));
        eq("round2", 1.24, Pricing.round2(1.2449));

        eq("delivery fee under threshold", 2.99, Pricing.delivery(10, false));
        eq("free delivery at threshold", 0.0, Pricing.delivery(30, false));
        eq("free delivery with coupon", 0.0, Pricing.delivery(10, true));
        eq("empty basket has no delivery", 0.0, Pricing.delivery(0, false));

        eq("FRESH10 10%", 2.0, Coupons.apply("FRESH10", 20).discount);
        eq("FRESH10 capped at 10", 10.0, Coupons.apply("fresh10", 500).discount);
        eq("WELCOME5 needs $20", false, Coupons.apply("WELCOME5", 19.99).valid);
        eq("WELCOME5 ok", 5.0, Coupons.apply("WELCOME5", 25).discount);
        eq("FREESHIP flag", true, Coupons.apply("FREESHIP", 5).freeShipping);
        eq("unknown coupon", false, Coupons.apply("NOPE", 50).valid);
        eq("blank coupon", false, Coupons.apply("  ", 50).valid);

        Pricing.Totals t = Pricing.totals(20, "FRESH10");
        eq("totals: 20 - 2 + 2.99", 20.99, t.total);
        eq("totals: free delivery over 30", 31.0, Pricing.totals(31, "").total);
        eq("totals: freeship", 12.0, Pricing.totals(12, "FREESHIP").total);

        eq("cancel confirmed", true, OrderStatus.CONFIRMED.canCancel());
        eq("cancel preparing", true, OrderStatus.PREPARING.canCancel());
        eq("no cancel when out for delivery", false, OrderStatus.OUT_FOR_DELIVERY.canCancel());
        eq("no cancel when delivered", false, OrderStatus.DELIVERED.canCancel());
        eq("flow 1", OrderStatus.PREPARING, OrderStatus.CONFIRMED.next());
        eq("flow 2", OrderStatus.OUT_FOR_DELIVERY, OrderStatus.PREPARING.next());
        eq("flow 3", OrderStatus.DELIVERED, OrderStatus.OUT_FOR_DELIVERY.next());
        eq("flow end", null == OrderStatus.DELIVERED.next(), true);
        eq("cancelled step", -1, OrderStatus.CANCELLED.step());
        eq("parse unknown", OrderStatus.CONFIRMED, OrderStatus.parse("???"));

        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }
}
