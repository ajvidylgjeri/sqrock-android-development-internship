package com.sqrock.freshcart.screens;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.sqrock.freshcart.MainActivity;
import com.sqrock.freshcart.data.Models.Product;
import com.sqrock.freshcart.ui.Ui;

/** Reusable product widgets. */
final class Views {
    private Views() {}

    static View priceLine(MainActivity a, Product p, float sp) {
        LinearLayout row = Ui.hbox(a);
        TextView now = Ui.text(a, Ui.money(p.finalPrice()), sp, Ui.primary(), true);
        row.addView(now);
        if (p.discountPct > 0) {
            TextView old = Ui.text(a, Ui.money(p.price), sp - 2, Ui.muted(), false);
            old.setPaintFlags(old.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            old.setPadding(Ui.dp(a, 6), 0, 0, 0);
            row.addView(old);
        }
        return row;
    }

    /** Vertical card for horizontal carousels. */
    static View card(MainActivity a, Product p) {
        LinearLayout c = Ui.card(a);
        c.setLayoutParams(Ui.margin(a, Ui.dp(a, 150), ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 10, 4));
        TextView emoji = Ui.text(a, p.emoji, 40, Ui.text(), false);
        emoji.setGravity(Gravity.CENTER);
        c.addView(emoji, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        c.addView(Ui.text(a, p.name, 14, Ui.text(), true));
        c.addView(Ui.muted(a, p.unit));
        c.addView(priceLine(a, p, 15));
        if (p.discountPct > 0) c.addView(Ui.badge(a, "-" + p.discountPct + "%", Ui.RED));
        c.addView(Ui.spacer(a, 6));
        c.addView(addButton(a, p));
        c.setOnClickListener(v -> a.show(new ProductScreen(a, p.id)));
        return c;
    }

    /** Horizontal list row. */
    static View row(MainActivity a, Product p) {
        LinearLayout c = Ui.card(a);
        LinearLayout r = Ui.hbox(a);
        TextView emoji = Ui.text(a, p.emoji, 34, Ui.text(), false);
        emoji.setPadding(0, 0, Ui.dp(a, 12), 0);
        LinearLayout mid = Ui.vbox(a);
        mid.addView(Ui.text(a, p.name, 15, Ui.text(), true));
        mid.addView(Ui.muted(a, p.unit + (p.stock == 0 ? "  ·  out of stock" : "")));
        mid.addView(priceLine(a, p, 15));
        r.addView(emoji);
        r.addView(mid, Ui.weight(1));
        r.addView(addButton(a, p));
        c.addView(r);
        c.setOnClickListener(v -> a.show(new ProductScreen(a, p.id)));
        return c;
    }

    static View addButton(MainActivity a, Product p) {
        View b = p.stock <= 0 ? Ui.button(a, "Sold out", Ui.line(), Ui.muted(), null) : Ui.primaryButton(a, "Add", v -> {
            a.db.addToCart(a.user.id, p.id, 1);
            Ui.toast(a, p.name + " added to cart");
            a.refresh();
        });
        b.setEnabled(p.stock > 0);
        return b;
    }

    static View empty(MainActivity a, String emoji, String message) {
        LinearLayout l = Ui.vbox(a);
        l.setGravity(Gravity.CENTER);
        l.setPadding(Ui.dp(a, 24), Ui.dp(a, 48), Ui.dp(a, 24), Ui.dp(a, 24));
        TextView e = Ui.text(a, emoji, 52, Color.GRAY, false);
        e.setGravity(Gravity.CENTER);
        TextView m = Ui.muted(a, message);
        m.setGravity(Gravity.CENTER);
        l.addView(e, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        l.addView(m, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return l;
    }

    static LinearLayout page(MainActivity a) {
        LinearLayout l = Ui.vbox(a);
        l.setPadding(Ui.dp(a, 14), Ui.dp(a, 10), Ui.dp(a, 14), Ui.dp(a, 20));
        return l;
    }
}
