package com.sqrock.freshcart.logic;

/** Order life-cycle. Customers may cancel only before the order is dispatched. */
public enum OrderStatus {
    CONFIRMED("Order Confirmed"), PREPARING("Preparing"), OUT_FOR_DELIVERY("Out for Delivery"), DELIVERED("Delivered"), CANCELLED("Cancelled");

    public final String label;

    OrderStatus(String label) { this.label = label; }

    public boolean canCancel() { return this == CONFIRMED || this == PREPARING; }

    public boolean isFinal() { return this == DELIVERED || this == CANCELLED; }

    /** Next step of the delivery flow, or null when the order is finished. */
    public OrderStatus next() {
        switch (this) {
            case CONFIRMED: return PREPARING;
            case PREPARING: return OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY: return DELIVERED;
            default: return null;
        }
    }

    /** 0..3 for the progress tracker, -1 for cancelled. */
    public int step() { return this == CANCELLED ? -1 : ordinal(); }

    public static OrderStatus parse(String s) {
        try { return valueOf(s); } catch (IllegalArgumentException | NullPointerException e) { return CONFIRMED; }
    }
}
