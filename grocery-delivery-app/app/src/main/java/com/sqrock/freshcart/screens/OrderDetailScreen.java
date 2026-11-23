package com.sqrock.freshcart.screens;

import android.view.View;
import android.widget.LinearLayout;
import com.sqrock.freshcart.MainActivity;
import com.sqrock.freshcart.data.Models.Order;
import com.sqrock.freshcart.data.Models.OrderItem;
import com.sqrock.freshcart.logic.OrderStatus;
import com.sqrock.freshcart.ui.Ui;

/** Order tracking and cancellation (allowed until the order is dispatched). */
public class OrderDetailScreen implements Screen {
    private final MainActivity a;
    private final long id;

    public OrderDetailScreen(MainActivity a, long id) { this.a = a; this.id = id; }

    @Override public String title() { return "Order #" + id; }

    @Override public View build() {
        Order o = a.db.order(id);
        if (o == null) return Views.empty(a, "❓", "Order not found.");
        LinearLayout col = Views.page(a);
        col.addView(Ui.badge(a, o.status.label, OrdersScreen.statusColor(o.status)));
        col.addView(Ui.muted(a, "Placed " + o.createdAt));

        col.addView(Ui.sectionTitle(a, "Tracking"));
        LinearLayout t = Ui.card(a);
        if (o.status == OrderStatus.CANCELLED) t.addView(Ui.text(a, "✖ This order was cancelled", 15, Ui.RED, true));
        else {
            OrderStatus[] flow = {OrderStatus.CONFIRMED, OrderStatus.PREPARING, OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED};
            for (OrderStatus s : flow) {
                boolean done = s.step() <= o.status.step();
                t.addView(Ui.text(a, (done ? "✔  " : "○  ") + s.label, 15, done ? Ui.primary() : Ui.muted(), done));
            }
        }
        col.addView(t);

        col.addView(Ui.sectionTitle(a, "Items"));
        LinearLayout items = Ui.card(a);
        for (OrderItem i : o.items) items.addView(Ui.body(a, i.emoji + " " + i.name + "  ×" + i.qty + "   " + Ui.money(i.unitPrice * i.qty)));
        col.addView(items);

        col.addView(Ui.sectionTitle(a, "Payment & delivery"));
        LinearLayout p = Ui.card(a);
        p.addView(Ui.body(a, "Items: " + Ui.money(o.subtotal)));
        if (o.discount > 0) p.addView(Ui.body(a, "Coupon" + (o.coupon == null || o.coupon.isEmpty() ? "" : " (" + o.coupon + ")") + ": -" + Ui.money(o.discount)));
        p.addView(Ui.body(a, "Delivery: " + (o.delivery == 0 ? "Free" : Ui.money(o.delivery))));
        p.addView(Ui.text(a, "Total: " + Ui.money(o.total), 17, Ui.primary(), true));
        p.addView(Ui.muted(a, o.payment));
        p.addView(Ui.muted(a, "📍 " + o.address));
        col.addView(p);

        if (o.status.canCancel()) {
            col.addView(Ui.button(a, "Cancel order", Ui.RED, android.graphics.Color.WHITE, v -> Ui.confirm(a, "Cancel order", "Cancel order #" + o.id + "?", () -> {
                try { a.db.cancelOrder(o.id, a.user.id); Ui.toast(a, "Order cancelled"); }
                catch (RuntimeException e) { Ui.info(a, "Cannot cancel", e.getMessage()); }
                a.refresh();
            })));
        } else if (!o.status.isFinal()) {
            col.addView(Ui.muted(a, "This order has been dispatched and can no longer be cancelled."));
        }
        return Ui.scroll(a, col);
    }
}
