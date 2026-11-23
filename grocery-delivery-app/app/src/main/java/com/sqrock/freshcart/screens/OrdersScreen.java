package com.sqrock.freshcart.screens;

import android.view.View;
import android.widget.LinearLayout;
import com.sqrock.freshcart.MainActivity;
import com.sqrock.freshcart.data.Models.Order;
import com.sqrock.freshcart.logic.OrderStatus;
import com.sqrock.freshcart.ui.Ui;
import java.util.List;

/** Order history for the signed-in customer. */
public class OrdersScreen implements Screen {
    private final MainActivity a;

    public OrdersScreen(MainActivity a) { this.a = a; }

    @Override public String title() { return "My orders"; }
    @Override public boolean isTab() { return true; }

    @Override public View build() {
        List<Order> orders = a.db.orders(a.user.id);
        if (orders.isEmpty()) return Views.empty(a, "📦", "No orders yet.");
        LinearLayout col = Views.page(a);
        for (Order o : orders) {
            LinearLayout c = Ui.card(a);
            c.addView(Ui.text(a, "Order #" + o.id + "  ·  " + Ui.money(o.total), 16, Ui.text(), true));
            c.addView(Ui.muted(a, o.createdAt + "  ·  " + o.items.size() + " items"));
            c.addView(Ui.badge(a, o.status.label, statusColor(o.status)));
            c.setOnClickListener(v -> a.show(new OrderDetailScreen(a, o.id)));
            col.addView(c);
        }
        return Ui.scroll(a, col);
    }

    static int statusColor(OrderStatus s) {
        switch (s) {
            case DELIVERED: return Ui.primary();
            case CANCELLED: return Ui.RED;
            case OUT_FOR_DELIVERY: return Ui.BLUE;
            default: return Ui.ORANGE;
        }
    }
}
