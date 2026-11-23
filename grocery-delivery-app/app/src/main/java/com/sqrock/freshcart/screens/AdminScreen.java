package com.sqrock.freshcart.screens;

import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.sqrock.freshcart.MainActivity;
import com.sqrock.freshcart.data.Models;
import com.sqrock.freshcart.data.Models.Order;
import com.sqrock.freshcart.data.Models.Product;
import com.sqrock.freshcart.data.Models.SalesStats;
import com.sqrock.freshcart.logic.OrderStatus;
import com.sqrock.freshcart.ui.Ui;

/** Admin panel: sales dashboard, order status updates and product management. */
public class AdminScreen implements Screen {
    private final MainActivity a;
    private int tab; // 0 dashboard, 1 orders, 2 products

    public AdminScreen(MainActivity a) { this.a = a; }

    @Override public String title() { return "Admin panel"; }

    @Override public View build() {
        LinearLayout col = Views.page(a);
        LinearLayout tabs = Ui.hbox(a);
        String[] names = {"Dashboard", "Orders", "Products"};
        for (int i = 0; i < names.length; i++) { final int idx = i; tabs.addView(Ui.chip(a, names[i], tab == i, v -> { tab = idx; a.refresh(); })); }
        col.addView(Ui.hscroll(a, tabs));
        if (tab == 0) dashboard(col); else if (tab == 1) orders(col); else products(col);
        return Ui.scroll(a, col);
    }

    private void dashboard(LinearLayout col) {
        SalesStats s = a.db.salesStats();
        LinearLayout k = Ui.card(a);
        k.addView(Ui.text(a, Ui.money(s.revenue), 28, Ui.primary(), true));
        k.addView(Ui.muted(a, "Revenue (cancelled orders excluded)"));
        col.addView(k);
        LinearLayout c = Ui.card(a);
        c.addView(Ui.body(a, "Orders: " + s.orders));
        c.addView(Ui.body(a, "Active: " + s.active + "   Delivered: " + s.delivered + "   Cancelled: " + s.cancelled));
        col.addView(c);
        col.addView(Ui.sectionTitle(a, "Orders by status"));
        int max = 1;
        for (int n : s.byStatus) max = Math.max(max, n);
        for (OrderStatus st : OrderStatus.values()) {
            LinearLayout row = Ui.hbox(a);
            row.addView(Ui.text(a, st.label, 13, Ui.text(), false), Ui.weight(1.2f));
            LinearLayout barBox = Ui.hbox(a);
            View bar = new View(a);
            bar.setBackground(Ui.rounded(a, OrdersScreen.statusColor(st), 4, 0));
            int w = Math.max(Ui.dp(a, 4), Ui.dp(a, 160) * s.byStatus[st.ordinal()] / max);
            barBox.addView(bar, Ui.lp(w, Ui.dp(a, 14)));
            barBox.addView(Ui.text(a, "  " + s.byStatus[st.ordinal()], 13, Ui.text(), true));
            row.addView(barBox, Ui.weight(2f));
            row.setPadding(0, Ui.dp(a, 4), 0, Ui.dp(a, 4));
            col.addView(row);
        }
    }

    private void orders(LinearLayout col) {
        java.util.List<Order> all = a.db.allOrders();
        if (all.isEmpty()) col.addView(Ui.muted(a, "No orders yet."));
        for (Order o : all) {
            LinearLayout c = Ui.card(a);
            c.addView(Ui.text(a, "#" + o.id + "  " + o.customer + "  ·  " + Ui.money(o.total), 15, Ui.text(), true));
            c.addView(Ui.muted(a, o.createdAt + "  ·  " + o.items.size() + " items"));
            c.addView(Ui.badge(a, o.status.label, OrdersScreen.statusColor(o.status)));
            OrderStatus next = o.status.next();
            if (next != null) c.addView(Ui.primaryButton(a, "Mark as " + next.label, v -> { a.db.updateStatus(o.id, next); a.refresh(); }));
            if (!o.status.isFinal()) c.addView(Ui.ghostButton(a, "Cancel order", v -> Ui.confirm(a, "Cancel", "Cancel order #" + o.id + "?", () -> { a.db.updateStatus(o.id, OrderStatus.CANCELLED); a.refresh(); })));
            col.addView(c);
        }
    }

    private void products(LinearLayout col) {
        col.addView(Ui.primaryButton(a, "+ New product", v -> a.show(new ProductFormScreen(a, 0))));
        for (Product p : a.db.products(null, "")) {
            LinearLayout c = Ui.card(a);
            c.addView(Ui.text(a, p.emoji + " " + p.name, 15, Ui.text(), true));
            c.addView(Ui.muted(a, p.category + " · " + Ui.money(p.price) + " · stock " + p.stock + (p.discountPct > 0 ? " · -" + p.discountPct + "%" : "")));
            LinearLayout b = Ui.hbox(a);
            b.addView(Ui.ghostButton(a, "Edit", v -> a.show(new ProductFormScreen(a, p.id))));
            b.addView(Ui.ghostButton(a, "Delete", v -> Ui.confirm(a, "Delete product", "Delete " + p.name + "?", () -> {
                try { a.db.deleteProduct(p.id); } catch (RuntimeException e) { Ui.info(a, "Cannot delete", "This product is used in existing orders or carts. Set its stock to 0 instead."); }
                a.refresh();
            })));
            c.addView(b);
            col.addView(c);
        }
    }
}
