package com.sqrock.freshcart.data;

import com.sqrock.freshcart.logic.OrderStatus;
import com.sqrock.freshcart.logic.Pricing;
import java.util.ArrayList;
import java.util.List;

/** Plain data holders. */
public final class Models {
    private Models() {}

    public static final String[] CATEGORIES = {"Fruits & Vegetables", "Dairy Products", "Snacks", "Beverages", "Bakery", "Household Items", "Personal Care"};

    public static class User {
        public long id;
        public String name, email, phone, role;
        public boolean isAdmin() { return "admin".equals(role); }
    }

    public static class Product {
        public long id;
        public String name, category, emoji, description, unit;
        public double price;
        public int discountPct, stock;
        public boolean featured, bestSeller;
        public double finalPrice() { return Pricing.unitPrice(price, discountPct); }
    }

    public static class CartLine {
        public Product product;
        public int qty;
        public double total() { return Pricing.lineTotal(product.price, product.discountPct, qty); }
    }

    public static class Address {
        public long id;
        public String label, line;
        @Override public String toString() { return label + " - " + line; }
    }

    public static class OrderItem {
        public String name, emoji;
        public double unitPrice;
        public int qty;
    }

    public static class Order {
        public long id, userId;
        public double subtotal, discount, delivery, total;
        public String payment, address, coupon, createdAt, customer;
        public OrderStatus status;
        public List<OrderItem> items = new ArrayList<>();
    }

    public static class SalesStats {
        public double revenue;
        public int orders, cancelled, delivered, active;
        public int[] byStatus = new int[OrderStatus.values().length];
    }
}
